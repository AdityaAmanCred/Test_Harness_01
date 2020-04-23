package TEST_HARNESS;

import static TEST_HARNESS.Util.createJSONMap;
import static TEST_HARNESS.Util.getCommonFileNames;
import static TEST_HARNESS.Util.getPropertyFromFile;
import static TEST_HARNESS.Util.readCSVLineByLine;
import static TEST_HARNESS.Util.removeRedundantDifference;
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
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import lombok.Getter;

public class Comparator {
    private Map<String, Object> filteredLeftMap;

    private Map<String, Object> filteredRightMap;

    private String keysToCompare;

    @Getter
    private Map<String, JSONArray> diffKeys = new HashedMap();

    @Getter
    private Map<String, JSONArray> mfields = new HashedMap();

    @Getter
    private Map<String, List<String>> aggregateMap = new HashMap<>();

    @Getter
    private Map<String, JSONArray> keysOnLeft = new HashMap<>();

    @Getter
    private Map<String, JSONArray> keysOnRight = new HashMap<>();

    private Set<String> mandatoryFields;

    private Set<String> allfields = new HashSet<>();

    public Comparator() {
        keysToCompare = getPropertyFromFile("application.properties").getProperty("KEY_FILTER");
        mandatoryFields = readCSVLineByLine(getPropertyFromFile("application.properties").getProperty("MANDATORY_FIELDS_CSV")).stream().collect(
                Collectors.toSet());
    }

    private void compare(String fileName) throws JsonProcessingException, ParseException {

        System.out.println("Comparing responses for file: " + fileName);
        Map<String, Object> leftFlatMap = Util.flatten(filteredLeftMap);
        Map<String, Object> rightFlatMap = Util.flatten(filteredRightMap);
        leftFlatMap.keySet().forEach(k -> allfields.add(replaceNumbers(k)));
        rightFlatMap.keySet().forEach(k -> allfields.add(replaceNumbers(k)));
        MapDifference<String, Object> difference = Maps.difference(leftFlatMap, rightFlatMap);

        updateMap(difference.entriesOnlyOnLeft(), keysOnLeft, fileName);
        updateMap(difference.entriesOnlyOnRight(), keysOnRight, fileName);

        Map<String, DiffValues> diff = difference.entriesDiffering().entrySet().stream().collect(
                Collectors.toMap(Map.Entry::getKey, e -> new DiffValues(e.getValue().leftValue(), e.getValue().rightValue())));

        updateDiffKeys(diff, fileName);
        updateNullCheckMap(fileName);
    }

    public JSONArray compareAll() throws IOException, ParseException {
        JSONArray jsonArray = new JSONArray();
        List<String> commonFileNames = getCommonFileNames("EXPECTED_DIR", "STAGE_DIR");
        for (String fileName : commonFileNames) {
            Object l_obj = new JSONParser()
                    .parse(new FileReader(getPropertyFromFile("application.properties").getProperty("EXPECTED_DIR") + fileName + ".json"));
            Object r_obj = new JSONParser()
                    .parse(new FileReader(getPropertyFromFile("application.properties").getProperty("STAGE_DIR") + fileName + ".json"));
            filteredLeftMap = createJSONMap(l_obj, keysToCompare);
            filteredRightMap = createJSONMap(r_obj, keysToCompare);

            compare(fileName);
        }
        generateAggregateMap();
        System.out.println("Results Generated for " + commonFileNames.size() + " files");
        allfields.stream().forEach(s -> replaceNumbers(s));
        return jsonArray;
    }

    public void generateAggregateMap() {
        for (String key : diffKeys.keySet()) {
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
    }

    public void updateDiffKeys(Map<String, DiffValues> diff, String fileName) {
        for (String key : diff.keySet()) {
            JSONArray varList;
            if (diffKeys.containsKey(key)) {
                varList = diffKeys.get(key);

            } else {
                varList = new JSONArray();
            }
            Variance variance = new Variance(fileName, removeRedundantDifference(diff.get(key).getExpectedValue()),
                    removeRedundantDifference(diff.get(key).getCapturedValue()));
            if (!variance.getCapturedValue().equals(variance.getExpectedValue())) {
                varList.add(variance);
            }
            if (varList.size() > 0) {
                diffKeys.put(key, varList);
            }
        }
    }

    public void updateNullCheckMap(String fileName) {
        Map<String, Object> leftFlatMap = Util.flatten(filteredLeftMap);
        Map<String, Object> rightFlatMap = Util.flatten(filteredRightMap);
        for (String k : allfields) {
            if (rightFlatMap.get(k) == null || rightFlatMap.get(k).toString().equals("") || rightFlatMap.get(k).toString()
                                                                                                        .equals("null") || rightFlatMap
                    .containsKey(k) == false) {

                JSONArray tmpVarArr;
                if (mfields.containsKey(k)) {
                    tmpVarArr = mfields.get(k);
                } else {
                    tmpVarArr = new JSONArray();
                }

                Variance variance = new Variance(fileName, removeRedundantDifference(leftFlatMap.get(k)), "null");
                if (!variance.getCapturedValue().equals(variance.getExpectedValue()) && !fileName
                        .equals("91cc9348-520d-4389-bb92-5110e90c0773")) {//Uncomment to remove null-"" pairs//
                    tmpVarArr.add(variance);
                }
                if (tmpVarArr.size() > 0) {
                    mfields.put(k, tmpVarArr);
                }
            }
        }
    }

    public void updateMap(Map<String, Object> map, Map<String, JSONArray> map2, String fileName) {
        for (String k : map.keySet()) {
            JSONArray jsonArray;
            if (map2.containsKey(k)) {
                jsonArray = map2.get(k);
            } else {
                jsonArray = new JSONArray();
            }
            File file = new File(fileName, map.get(k) != null ? map.get(k) : "null");
            if (map.get(k) != null && !map.get(k).toString().equals("")) {

                if (!fileName.equals("91cc9348-520d-4389-bb92-5110e90c0773")) {
                    jsonArray.add(file);
                }
            }
            if (jsonArray.size() > 0) {
                map2.put(k, jsonArray);
            }
        }
    }
}


