package TEST_HARNESS;

import static TEST_HARNESS.Util.createJSONMap;
import static TEST_HARNESS.Util.fetchProperty;
import static TEST_HARNESS.Util.getCommonFileNames;
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

@Getter
public class Comparator {
    private Map<String, Object> filteredLeftMap;

    private Map<String, Object> filteredRightMap;

    private String keysToCompare;

    private Map<String, JSONArray> diffKeys = new HashedMap();

    private Map<String, JSONArray> nullfieldMap = new HashedMap();

    private Map<String, List<String>> aggregateMap = new HashMap<>();

    private Map<String, JSONArray> keysOnLeft = new HashMap<>();

    private Map<String, JSONArray> keysOnRight = new HashMap<>();

    private Set<String> nullCheckFields;

    private Set<String> allfields = new HashSet<>();

    public Comparator() {
        keysToCompare = fetchProperty("KEY_FILTER");
        nullCheckFields = readCSVLineByLine(fetchProperty("NULLCHECK_FIELDS_CSV")).stream().collect(Collectors.toSet());
    }

    private void compare(String fileName) throws JsonProcessingException, ParseException {

        System.out.println("Comparing responses for file: " + fileName);
        Map<String, Object> leftFlatMap = Util.flatten(filteredLeftMap);
        Map<String, Object> rightFlatMap = Util.flatten(filteredRightMap);
        leftFlatMap.keySet().forEach(k -> allfields.add(k));
        rightFlatMap.keySet().forEach(k -> allfields.add(k));
        MapDifference<String, Object> difference = Maps.difference(leftFlatMap, rightFlatMap);

        updateMap(difference.entriesOnlyOnLeft(), keysOnLeft, fileName);
        updateMap(difference.entriesOnlyOnRight(), keysOnRight, fileName);

        Map<String, DiffValues> diff = difference.entriesDiffering().entrySet().stream().collect(
                Collectors.toMap(Map.Entry::getKey, e -> new DiffValues(e.getValue().leftValue(), e.getValue().rightValue())));

        updateDiffKeys(diff, fileName);
    }

    public void compareAll() throws IOException, ParseException {
        List<String> commonFileNames = getCommonFileNames("EXPECTED_DIR", "STAGE_DIR");
        for (String fileName : commonFileNames) {
            Object l_obj = new JSONParser().parse(new FileReader(fetchProperty("EXPECTED_DIR") + fileName + ".json"));
            Object r_obj = new JSONParser().parse(new FileReader(fetchProperty("STAGE_DIR") + fileName + ".json"));
            filteredLeftMap = createJSONMap(l_obj, keysToCompare);
            filteredRightMap = createJSONMap(r_obj, keysToCompare);
            compare(fileName);
        }
        generateAggregateMap();
    }

    public void nullCheck() throws IOException, ParseException {
        List<String> commonFileNames = getCommonFileNames("EXPECTED_DIR", "STAGE_DIR");
        for (String fileName : commonFileNames) {
            Object l_obj = new JSONParser().parse(new FileReader(fetchProperty("EXPECTED_DIR") + fileName + ".json"));
            Object r_obj = new JSONParser().parse(new FileReader(fetchProperty("STAGE_DIR") + fileName + ".json"));
            filteredLeftMap = createJSONMap(l_obj, keysToCompare);
            filteredRightMap = createJSONMap(r_obj, keysToCompare);
            updateNullCheckMap(fileName);
        }
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

    private void updateNullCheckMap(String fileName) {
        Map<String, Object> leftFlatMap = Util.flatten(filteredLeftMap);
        Map<String, Object> rightFlatMap = Util.flatten(filteredRightMap);
        nullCheckFields.forEach(k -> allfields.add(k.trim()));
        for (String k : allfields) {
            if (rightFlatMap.get(k) == null || rightFlatMap.get(k).toString().equals("") || rightFlatMap.get(k).toString()
                                                                                                        .equals("null") || rightFlatMap
                    .containsKey(k) == false) {

                JSONArray tmpVarArr;
                if (nullfieldMap.containsKey(k)) {
                    tmpVarArr = nullfieldMap.get(k);
                } else {
                    tmpVarArr = new JSONArray();
                }

                Variance variance = new Variance(fileName, removeRedundantDifference(leftFlatMap.get(k)), "null");
                // if (!variance.getCapturedValue().equals(variance
                // .getExpectedValue())) {//Uncomment if-condition to check null values for stage, even if prod values are null too//
                tmpVarArr.add(variance);
                // }
                if (tmpVarArr.size() > 0) {
                    nullfieldMap.put(k, tmpVarArr);
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
                jsonArray.add(file);
            }
            if (jsonArray.size() > 0) {
                map2.put(k, jsonArray);
            }
        }
    }
}


