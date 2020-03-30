package TEST_HARNESS;

import static TEST_HARNESS.Util.getCommonFileNames;
import static TEST_HARNESS.Util.getPropertyFromFile;
import static TEST_HARNESS.Util.replaceNumbers;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.collections.map.HashedMap;
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
import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;

public class Comparator {
    private Map<String, Object> filteredLeftMap;

    private Map<String, Object> filteredRightMap;

    private String leftJson;

    private String rightJson;

    private ObjectMapper objMapper;

    private String keysToCompare;

    private static Map<String, List<String>> diffKeys = new HashedMap();

    private static Map<String, JSONArray> diffKeys2 = new HashedMap();

    private static Map<String, List<String>> aggregateMap = new HashMap<>();

    public Comparator() {
        this.objMapper = new ObjectMapper();
        this.keysToCompare = getPropertyFromFile("application.properties").getProperty("KEY_FILTER");
    }

    private void setLeftAndRightJson(String leftJson, String rightJson) {
        this.leftJson = leftJson;
        this.rightJson = rightJson;
    }

    private FileWiseResult compare(String fileName) throws JsonProcessingException, ParseException {
        System.out.println("Comparing responses for file: " + fileName);
        Map<String, Object> leftFlatMap = Util.flatten(filteredLeftMap);
        Map<String, Object> rightFlatMap = Util.flatten(filteredRightMap);

        MapDifference<String, Object> difference = Maps.difference(leftFlatMap, rightFlatMap);

        String entriesOnlyOnLeft = new ObjectMapper().writeValueAsString(difference.entriesOnlyOnLeft());
        JSONParser parser = new JSONParser();
        JSONObject leftOnlyJson = (JSONObject) parser.parse(entriesOnlyOnLeft);

        String entriesOnlyOnRight = new ObjectMapper().writeValueAsString(difference.entriesOnlyOnRight());
        JSONObject rightOnlyJson = (JSONObject) parser.parse(entriesOnlyOnRight);

        String identicalFields = new ObjectMapper().writeValueAsString(difference.entriesInCommon());
        JSONObject commonJson = (JSONObject) parser.parse(identicalFields);

        Map<String, DiffValues> diff = difference.entriesDiffering().entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey,
                e -> new DiffValues(e.getValue().leftValue() != null ? e.getValue().leftValue().toString() : "null",
                        e.getValue().rightValue() != null ? e.getValue().rightValue().toString() : "null")));

        String differingEntries = new ObjectMapper().writeValueAsString(diff);
        JSONObject diffJson = (JSONObject) parser.parse(differingEntries);

        FileWiseResult fileWiseResult = new FileWiseResult(fileName, leftOnlyJson, rightOnlyJson, commonJson, diffJson);
        for (String key : diff.keySet()) {
            if (diffKeys.containsKey(key)) {
                List<String> StringList = diffKeys.get(key);
                StringList.add(fileName);
                diffKeys.put(key, StringList);
            } else {
                List<String> newList = new ArrayList<>();
                newList.add(fileName);
                diffKeys.put(key, newList);
            }
        }

        for (String key : diff.keySet()) {

            if (diffKeys2.containsKey(key)) {
                JSONArray varList = diffKeys2.get(key);

                varList.add(new Variance(fileName, diff.get(key).getExpectedValue(), diff.get(key).getCapturedValue()));
                diffKeys2.put(key, varList);
            } else {
                JSONArray newList = new JSONArray();
                newList.add(new Variance(fileName, diff.get(key).getExpectedValue(), diff.get(key).getCapturedValue()));
                diffKeys2.put(key, newList);
            }
        }
        return fileWiseResult;
    }

    public JSONArray compareAll() throws IOException, ParseException {
        JSONArray jsonArray = new JSONArray();
        List<String> commonFileNames = getCommonFileNames("EXPECTED_DIR", "STAGE_DIR");
        for (String fileName : commonFileNames) {
            Object l_obj = new JSONParser()
                    .parse(new FileReader(getPropertyFromFile("application.properties").getProperty("EXPECTED_DIR") + fileName + ".json"));
            Object r_obj = new JSONParser()
                    .parse(new FileReader(getPropertyFromFile("application.properties").getProperty("STAGE_DIR") + fileName + ".json"));
            this.setLeftAndRightJson(JSONValue.toJSONString(l_obj), JSONValue.toJSONString(r_obj));
            this.createJsonMaps();
            jsonArray.add(this.compare(fileName));
        }
        for (String key : diffKeys2.keySet()) {
            String aggKey = replaceNumbers(key);
            if (aggregateMap.containsKey(aggKey)) {
                List<String> keyList = aggregateMap.get(aggKey);
                keyList.add(key);
                aggregateMap.put(aggKey, keyList);
            } else {
                List<String> keyList = new ArrayList<>();
                keyList.add(key);
                aggregateMap.put(aggKey, keyList);
            }
        }
        System.out.println("Results Generated for " + commonFileNames.size() + " files");
        return jsonArray;
    }

    public void createJsonMaps() throws IOException {
        ObjectMapper mapper = Squiggly.init(new ObjectMapper(), keysToCompare);

        TypeReference<Map<String, Object>> type = new TypeReference<Map<String, Object>>() {};

        filteredLeftMap = objMapper.readValue(SquigglyUtils.stringify(mapper, objMapper.readValue(leftJson, type)), type);
        filteredRightMap = objMapper.readValue(SquigglyUtils.stringify(mapper, objMapper.readValue(rightJson, type)), type);
    }

    public JSONArray getVarianceArray(String key) {
        return diffKeys2.get(key);
    }

    public JSONArray generateFieldWiseResults() {
        JSONArray jsonArray = new JSONArray();
        for (String aggKey : aggregateMap.keySet()) {
            JSONArray varianceArr = new JSONArray();
            Set<String> fileSet = new HashSet<>();
            List<String> keys = aggregateMap.get(aggKey);
            for (String key : keys) {
                JSONArray tmpVarArr = getVarianceArray(key);
                varianceArr.addAll(tmpVarArr);
                fileSet.addAll(getFileNamesFromArray(tmpVarArr));
            }
            Double varPercentage = percentage(fileSet.size(), getCommonFileNames("EXPECTED_DIR", "STAGE_DIR").size());
            FieldWiseResult fieldResult = new FieldWiseResult(aggKey, varPercentage, varianceArr);
            jsonArray.add(fieldResult);
        }
        return jsonArray;
    }

    private Double percentage(int count, int total_count) {
        return (double) count / total_count * 100;
    }

    public Set<String> getFileNamesFromArray(JSONArray jsonArray) {
        Set<String> files = new HashSet<>();
        for (Object obj : jsonArray) {
            files.add(((Variance) obj).getFileName());
        }
        return files;
    }

}


