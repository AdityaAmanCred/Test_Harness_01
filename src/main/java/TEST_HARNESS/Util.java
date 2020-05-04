package TEST_HARNESS;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
        File folder = new File(getPropertyFromFile("application.properties").getProperty(folderName));
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

    public static void setParameters() {
        if (getPropertyFromFile("application.properties").getProperty("PARSER_NAME").equalsIgnoreCase("PANDORA")) {
            Application.setParserName(ParserName.PANDORASTREET);
        } else if (getPropertyFromFile("application.properties").getProperty("PARSER_NAME").equalsIgnoreCase("OPTIMUS")) {
            Application.setParserName(ParserName.OPTIMUS);
        } else {
            Application.setParserName(ParserName.BUMBLEBEE);
        }
        if (getPropertyFromFile("application.properties").getProperty("COMPARISON_MODE").equalsIgnoreCase("PROD")) {
            Application.setCompareAgainst(CompareAgainst.PROD);
        } else {
            Application.setCompareAgainst(CompareAgainst.MANUAL);
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

    public static void saveResponse(byte[] bytes, String fileName, Environment env) {
        File file;
        if (env == Environment.PROD) {
            file = new File(getPropertyFromFile("application.properties").getProperty("EXPECTED_DIR") + fileName.split("\\.")[0] + ".json");
        } else {
            file = new File(getPropertyFromFile("application.properties").getProperty("STAGE_DIR") + fileName.split("\\.")[0] + ".json");
        }

        try {

            OutputStream os = new FileOutputStream(file);

            os.write(bytes);
            if (env == Environment.PROD) {
                System.out.println("Prod response fetched for: " + fileName);
            } else {
                System.out.println("Stage response fetched for: " + fileName);
            }

            os.close();
        } catch (Exception e) {
            System.out.println(fileName + ".pdf: Exception: " + e);
        }
    }

}
