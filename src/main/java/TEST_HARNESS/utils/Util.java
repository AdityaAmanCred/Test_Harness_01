package TEST_HARNESS.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.bohnman.squiggly.Squiggly;
import com.github.bohnman.squiggly.util.SquigglyUtils;
import com.google.common.collect.Sets;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.opencsv.CSVReader;
import TEST_HARNESS.config.CompareAgainst;
import TEST_HARNESS.config.Config;
import TEST_HARNESS.parse.Environment;
import TEST_HARNESS.result.pojos.Standalone;
import TEST_HARNESS.parse.ParserName;
import TEST_HARNESS.parse.ParserType;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class Util {
    private Util() {
        throw new AssertionError("No instances for you!");
    }

    public static Map<String, Object> flatten(Map<String, Object> map) {
        return map.entrySet().stream().flatMap(Util::flatten)
                  .collect(LinkedHashMap::new, (m, e) -> m.put(e.getKey(), e.getValue()), LinkedHashMap::putAll);
    }

    private static Stream<Entry<String, Object>> flatten(Entry<String, Object> entry) {

        if (entry == null) {
            return Stream.empty();
        }

        if (entry.getValue() instanceof Map<?, ?>) {
            Map<?, ?> properties = (Map<?, ?>) entry.getValue();
            return properties.entrySet().stream().flatMap(e -> flatten(new SimpleEntry<>(entry.getKey() + "." + e.getKey(), e.getValue())));
        }

        if (entry.getValue() instanceof List<?>) {
            List<?> list = (List<?>) entry.getValue();
            return IntStream.range(0, list.size()).mapToObj(i -> new SimpleEntry<String, Object>(entry.getKey() + "." + i, list.get(i)))
                            .flatMap(Util::flatten);
        }

        return Stream.of(entry);
    }

    public static List<String> getCommonFileNames(String folderLoc1, String folderLoc2) {
        Set<String> expectedOutputFiles = getNames(folderLoc1).stream().collect(Collectors.toSet());
        Set<String> stageFiles = getNames(folderLoc2).stream().collect(Collectors.toSet());
        return Sets.intersection(expectedOutputFiles, stageFiles).stream().collect(Collectors.toList());
    }

    public static Set<String> getNames(String folderName) {
        Set<String> stringList = new HashSet<>();
        File folder = new File(fetchProperty(folderName));
        File[] listOfFiles = folder.listFiles();
        //fileNames = new ArrayList<String>();
        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile()) {
                if (listOfFiles[i].getName().split("\\.").length == 2) {
                    String fileName = listOfFiles[i].getName().split("\\.")[0];
                    if (fileName.length() != 0) {
                        stringList.add(fileName);
                    }
                }
            }
        }
        return stringList;
    }

    public static Properties getPropertyFromFile(final String fileName) {
        InputStream inputStream = null;
        Properties properties = new Properties();
        try {
            inputStream = new FileInputStream(fileName);
            properties.load(inputStream);
            inputStream.close();
        } catch (Exception e) {
            log.error(String.valueOf(e));
        }
        return properties;
    }

    public static String replaceNumbers(String str1) {
        String output = str1.replaceAll("(.+?\\.)(\\d{1,})(\\..+?)", "$1n$3");
        return output;
    }

    public static List<String> commaSeperatedStrings(String s) {
        return Arrays.asList(s.split("\\,"));
    }

    public static Set<String> getFileNamesFromArray(JSONArray jsonArray) {
        Set<String> files = new HashSet<>();
        for (Object obj : jsonArray) {
            files.add(((Standalone) obj).getFileName());
        }
        return files;
    }

    public static Double percentage(int count, int total_count) {
        return (double) count / total_count * 100;
    }

    public static JSONObject mapToJSONConverter(Map<String, Object> map) throws JsonProcessingException, ParseException {
        String str = new ObjectMapper().writeValueAsString(map);
        JSONParser parser = new JSONParser();
        JSONObject jsonObject = (JSONObject) parser.parse(str);
        return jsonObject;
    }

    public static Map<String, Object> createJSONMap(Object object, String keysToCompare) throws IOException {
        ObjectMapper mapper = Squiggly.init(new ObjectMapper(), keysToCompare);

        TypeReference<Map<String, Object>> type = new TypeReference<Map<String, Object>>() {};
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(SquigglyUtils.stringify(mapper, objectMapper.readValue(JSONValue.toJSONString(object), type)), type);
    }

    public static List<List<String>> readCSVLineByLine(String fileLoc) {
        List<List<String>> stringMatrix = new ArrayList<>();
        try {
            CSVReader reader = new CSVReader(new FileReader(fileLoc));
            String[] nextLine;
            if ((nextLine = reader.readNext()) != null && !trimDoubleQuotes(nextLine[0]).contains("id")) {
                stringMatrix.add(Arrays.asList(nextLine).stream().map(s -> trimDoubleQuotes(s)).collect(Collectors.toList()));
            }
            while ((nextLine = reader.readNext()) != null) {
                stringMatrix.add(Arrays.asList(nextLine).stream().map(s -> trimDoubleQuotes(s)).collect(Collectors.toList()));
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stringMatrix;
    }

    public static Object removeRedundantDifference(Object obj) {
        if (obj == null || obj.toString().equals("")) {
            return "null";
        } else if ((obj instanceof Double) || (obj instanceof Integer)) {
            return Double.parseDouble(obj.toString());
        } else {
            return obj.toString();
        }
    }

    public static String fetchProperty(String placeholderName) {
        return getPropertyFromFile("application.properties").getProperty(placeholderName);
    }

    public static boolean isValidParserSelection() {
        ArrayList<ParserName> parsers = new ArrayList<>();

        parsers.add(Config.getPrimaryParserName());
        parsers.add(Config.getSecondaryParserName());
        if (parsers.get(0).equals(parsers.get(1)) && Config.getPrimaryParserEnv() == Config.getSecondaryParserEnv() && fetchProperty(
                "PRIMARY_PARSER_TEMPLATE_ID").equals(fetchProperty("SECONDARY_PARSER_TEMPLATE_ID"))) {
            log.error("Invalid Comparison. Can't compare identical combinations of parser,environment and template");
            return false;
        }
        if (parsers.contains(ParserName.BUMBLEBEE) && parsers.contains(ParserName.PANDORASTREET)) {
            log.error("Cannot compare BUMBLEEBEE responses with PANDORASTREET");
            return false;
        } else {
            return true;
        }
    }

    public static String trimDoubleQuotes(String text) {
        int textLength = text.length();
        if (textLength >= 2 && text.charAt(0) == '"' && text.charAt(textLength - 1) == '"') {
            return text.substring(1, textLength - 1);
        }
        return text;
    }

    public static Map<String, List<String>> generateAggregateMap(Map<String, JSONArray> map) {
        Map<String, List<String>> aggregatedMap = new HashMap<>();
        for (String key : map.keySet()) {
            String aggKey = replaceNumbers(key);
            if (aggregatedMap.containsKey(aggKey)) {
                List<String> keyList = aggregatedMap.get(aggKey);
                keyList.add(key);
                aggregatedMap.put(aggKey, keyList);
            } else {
                List<String> keyList = new ArrayList<>();
                keyList.add(key);
                aggregatedMap.put(aggKey, keyList);
            }
        }
        return aggregatedMap;
    }

    public static Object readJsonFile(String fileLocation) throws IOException, ParseException {
        return new JSONParser().parse(new FileReader(fileLocation));
    }

    public static JsonElement readGsonFile(String fileLocation) throws IOException, ParseException {
        return new JsonParser().parse(new FileReader(fileLocation));
    }

    public static JSONObject getIssuerDetails(JSONObject jsonObject, String issuer) {
        JSONArray issuers = (JSONArray) jsonObject.get("issuers");
        for (Object issuerJson : issuers) {
            if (((JSONObject) issuerJson).get("issuer_name").toString().replaceAll("\\s+", "").toLowerCase()
                                         .equals(issuer.replaceAll("\\s+", "").toLowerCase())) {
                return (JSONObject) issuerJson;
            }
        }
        return null;
    }

    public static String getMainJsonFieldName() {
        if (Config.getPrimaryParserName().equals(ParserName.BUMBLEBEE) || (Config.getPrimaryParserName().equals(ParserName.OPTIMUS) && Config
                .getSecondaryParserName().equals(ParserName.BUMBLEBEE))) {
            return "transformed_data";
        } else {
            return "json_object";
        }
    }

    public static void setComparisonParameter() {
        if (fetchProperty("COMPARISON_MODE").equalsIgnoreCase("SECONDARY")) {
            Config.setCompareAgainst(CompareAgainst.SECONDARY);
        } else if (fetchProperty("COMPARISON_MODE").equalsIgnoreCase("MANUAL")) {
            Config.setCompareAgainst(CompareAgainst.MANUAL);
        } else {
            Config.setCompareAgainst(CompareAgainst.STANDALONE);
        }
    }

    public static ParserName getParserNameForParserType(ParserType parserType) {
        ParserName parserName = null;
        if (parserType == ParserType.PRIMARY) {
            if (fetchProperty("PRIMARY_PARSER_NAME").equalsIgnoreCase("PANDORASTREET") || fetchProperty("PRIMARY_PARSER_NAME")
                    .equalsIgnoreCase("PANDORA")) {
                parserName = ParserName.PANDORASTREET;
            } else if (fetchProperty("PRIMARY_PARSER_NAME").equalsIgnoreCase("OPTIMUS")) {
                parserName = ParserName.OPTIMUS;
            } else if (fetchProperty("PRIMARY_PARSER_NAME").equalsIgnoreCase("BUMBLEBEE")) {
                parserName = ParserName.BUMBLEBEE;
            } else {
                parserName = ParserName.NIL;
            }
        } else {
            if (fetchProperty("SECONDARY_PARSER_NAME").equalsIgnoreCase("PANDORASTREET")) {
                parserName = ParserName.PANDORASTREET;
            } else if (fetchProperty("SECONDARY_PARSER_NAME").equalsIgnoreCase("OPTIMUS")) {
                parserName = ParserName.OPTIMUS;
            } else if (fetchProperty("SECONDARY_PARSER_NAME").equalsIgnoreCase("BUMBLEBEE")) {
                parserName = ParserName.BUMBLEBEE;
            } else {
                Config.setSecondaryParserName(ParserName.NIL);
                Config.setCompareAgainst(CompareAgainst.STANDALONE);
            }
        }
        return parserName;
    }

    public static Set presentInDIROneAndNotInDirTwo(String dirOneLoc, String dirTwoLoc) {
        Set dirOneFiles = getNames(dirOneLoc);
        Set dirTwoFiles = getNames(dirTwoLoc);
        dirOneFiles.removeAll(dirTwoFiles);
        return dirOneFiles;
    }

    public static void setParserNames() {
        if (fetchProperty("PRIMARY_PARSER_NAME").equalsIgnoreCase("bumblebee")) {
            Config.setPrimaryParserName(ParserName.BUMBLEBEE);
        } else if (fetchProperty("PRIMARY_PARSER_NAME").equalsIgnoreCase("optimus")) {
            Config.setPrimaryParserName(ParserName.OPTIMUS);
        } else {
            Config.setPrimaryParserName(ParserName.PANDORASTREET);
        }
        if (fetchProperty("SECONDARY_PARSER_NAME").equalsIgnoreCase("bumblebee")) {
            Config.setSecondaryParserName(ParserName.BUMBLEBEE);
        } else if (fetchProperty("SECONDARY_PARSER_NAME").equalsIgnoreCase("optimus")) {
            Config.setSecondaryParserName(ParserName.OPTIMUS);
        } else {
            Config.setSecondaryParserName(ParserName.PANDORASTREET);
        }
    }

    public static void setParserEnvironments() {
        Config.setPrimaryParserEnv(fetchProperty("PRIMARY_PARSER_ENV").equalsIgnoreCase("STAGE") ? Environment.STAGE : Environment.PROD);
        Config.setSecondaryParserEnv(fetchProperty("SECONDARY_PARSER_ENV").equalsIgnoreCase("STAGE") ? Environment.STAGE : Environment.PROD);
    }

    public static void writeTofile(String fileName, String jsonString) {
        //Write JSON file
        try (FileWriter file = new FileWriter(fetchProperty("RESULT_DIR") + fileName + ".json")) {
            file.write(jsonString);
            file.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
