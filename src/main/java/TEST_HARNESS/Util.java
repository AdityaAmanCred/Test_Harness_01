package TEST_HARNESS;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Scanner;
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

    public static List<String> getNames(String folderName) {
        List<String> stringList = new ArrayList<>();
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
        } catch (IOException e) {
            System.out.println(e);
        } catch (Exception e) {
            System.out.println(e);
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
            files.add(((Variance) obj).getFileName());
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

    public static void setParsingParameters() {
        if (fetchProperty("STAGE_PARSER_NAME").equalsIgnoreCase("PANDORASTREET")) {
            Application.setStageParserName(ParserName.PANDORASTREET);
        } else if (fetchProperty("STAGE_PARSER_NAME").equalsIgnoreCase("OPTIMUS")) {
            Application.setStageParserName(ParserName.OPTIMUS);
        } else if (fetchProperty("STAGE_PARSER_NAME").equalsIgnoreCase("BUMBLEBEE")) {
            Application.setStageParserName(ParserName.BUMBLEBEE);
        } else {
            Application.setStageParserName(ParserName.NIL);
        }
        if (fetchProperty("PROD_PARSER_NAME").equalsIgnoreCase("PANDORASTREET")) {
            Application.setProdParserName(ParserName.PANDORASTREET);
        } else if (fetchProperty("PROD_PARSER_NAME").equalsIgnoreCase("OPTIMUS")) {
            Application.setProdParserName(ParserName.OPTIMUS);
        } else if (fetchProperty("PROD_PARSER_NAME").equalsIgnoreCase("BUMBLEBEE")) {
            Application.setProdParserName(ParserName.BUMBLEBEE);
        } else {
            Application.setProdParserName(ParserName.NIL);
            Application.setCompareAgainst(CompareAgainst.STANDALONE);
        }

    }

    public static void setComparisonParameter() {
        if (fetchProperty("COMPARISON_MODE").equalsIgnoreCase("PROD")) {
            Application.setCompareAgainst(CompareAgainst.PROD);
        } else if (fetchProperty("COMPARISON_MODE").equalsIgnoreCase("MANUAL")) {
            Application.setCompareAgainst(CompareAgainst.MANUAL);
        } else {
            Application.setCompareAgainst(CompareAgainst.STANDALONE);
        }
    }

    public static List<String> readCSVLineByLine(String fileLoc) {
        List<String> lines = new ArrayList<>();
        try {
            Scanner scanner = new Scanner(new File(fileLoc));
            while (scanner.hasNextLine()) {
                String s = scanner.nextLine();
                if (s.length() > 0 && !s.trim().equalsIgnoreCase("id")) {
                    lines.add(s.trim());
                }
            }
            scanner.close();
        } catch (FileNotFoundException e) {
            System.out.println("File NOT FOUND at" + fileLoc);

        }
        return lines;
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

    public static void fetchParserResponses() {
        setParsingParameters();
        ParserResponses bumblebee = new Bumblebee(5.0);
        ParserResponses pandorastreet = new Pandorastreet(5.0);
        ParserResponses optimus = new Optimus(5.0);
        if (Application.getStageParserName() == ParserName.BUMBLEBEE) {
            bumblebee.fetchAllResponses(Environment.STAGE);
        } else if (Application.getStageParserName() == ParserName.OPTIMUS) {
            optimus.fetchAllResponses(Environment.STAGE);
        } else if (Application.getStageParserName() == ParserName.PANDORASTREET) {
            pandorastreet.fetchAllResponses(Environment.STAGE);
        }

        if (Application.getCompareAgainst() == CompareAgainst.PROD) {

            if (Application.getProdParserName() == ParserName.BUMBLEBEE) {
                bumblebee.fetchAllResponses(Environment.PROD);
            } else if (Application.getProdParserName() == ParserName.PANDORASTREET) {
                pandorastreet.fetchAllResponses(Environment.PROD);
            } else if (Application.getProdParserName() == ParserName.OPTIMUS) {
                optimus.fetchAllResponses(Environment.PROD);
            }
        } else if (Application.getCompareAgainst() == CompareAgainst.MANUAL) {
            CreateSkeletalJsons createSkeletalJsons = new CreateSkeletalJsons();
            createSkeletalJsons.createJsonFiles();
            Scanner sc = new Scanner(System.in);
            int userInput = 0;
            while (userInput != 1) {
                System.out.println(
                        "Make manual changes to skeletal json files in 'ExpectedResponses' directory.To proceed to perform comparison press 1. \n");
                userInput = sc.nextInt();
            }
        }
    }

    public static String fetchProperty(String placeholderName) {
        return getPropertyFromFile("application.properties").getProperty(placeholderName);
    }
}
