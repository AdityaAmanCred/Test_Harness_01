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

    private void compare(String fileName) throws IOException, ParseException {

        Map<String, Object> rightFlatMap = Util.flatten(filteredRightMap);
        rightFlatMap.keySet().forEach(k -> allfields.add(k));
        String otherDir = Application.getCompareAgainst() == CompareAgainst.STANDALONE ? "STAGE_DIR" : "EXPECTED_DIR";
        if (otherDir.equals("EXPECTED_DIR")) {
            System.out.println("Comparing responses for file: " + fileName);
            Map<String, Object> leftFlatMap = Util.flatten(filteredLeftMap);
            leftFlatMap.keySet().forEach(k -> allfields.add(k));
            MapDifference<String, Object> difference = Maps.difference(leftFlatMap, rightFlatMap);

            updateMap(difference.entriesOnlyOnLeft(), keysOnLeft, fileName);
            updateMap(difference.entriesOnlyOnRight(), keysOnRight, fileName);

            Map<String, DiffValues> diff = difference.entriesDiffering().entrySet().stream().collect(
                    Collectors.toMap(Map.Entry::getKey, e -> new DiffValues(e.getValue().leftValue(), e.getValue().rightValue())));

            updateDiffKeys(diff, fileName);
        }

    }

    public void compareAll() throws IOException, ParseException {
        String otherDir = Application.getCompareAgainst() == CompareAgainst.STANDALONE ? "STAGE_DIR" : "EXPECTED_DIR";
        List<String> commonFileNames = getCommonFileNames(otherDir, "STAGE_DIR");
        for (String fileName : commonFileNames) {
            if (otherDir.equals("EXPECTED_DIR")) {
                Object l_obj = new JSONParser().parse(new FileReader(fetchProperty(otherDir) + fileName + ".json"));
                filteredLeftMap = createJSONMap(l_obj, keysToCompare);
            }
            Object r_obj = new JSONParser().parse(new FileReader(fetchProperty("STAGE_DIR") + fileName + ".json"));
            filteredRightMap = createJSONMap(r_obj, keysToCompare);
            compare(fileName);
        }
        if (otherDir.equals("EXPECTED_DIR")) {
            generateAggregateMap();
        }
        nullCheck();
    }

    public void nullCheck() throws IOException, ParseException {
        String otherDir = Application.getCompareAgainst() == CompareAgainst.STANDALONE ? "STAGE_DIR" : "EXPECTED_DIR";
        List<String> commonFileNames = getCommonFileNames(otherDir, "STAGE_DIR");
        for (String fileName : commonFileNames) {
            Object r_obj = new JSONParser().parse(new FileReader(fetchProperty("STAGE_DIR") + fileName + ".json"));
            filteredRightMap = createJSONMap(r_obj, keysToCompare);
            if (otherDir.equals("EXPECTED_DIR")) {
                Object l_obj = new JSONParser().parse(new FileReader(fetchProperty(otherDir) + fileName + ".json"));
                filteredLeftMap = createJSONMap(l_obj, keysToCompare);
            }

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
        Map<String, Object> rightFlatMap = Util.flatten(filteredRightMap);
        Map<String, Object> leftFlatMap = (Application.getCompareAgainst() != CompareAgainst.STANDALONE) ? Util.flatten(filteredLeftMap) : null;
        nullCheckFields.forEach(k -> allfields.add(k.trim()));
        for (String k : allfields) {
            if (rightFlatMap.get(k) == null || rightFlatMap.get(k).toString().equals("") || rightFlatMap.get(k).toString()
                                                                                                        .equals("null") || rightFlatMap
                    .containsKey(k) == false) {

                JSONArray tmpArr;
                if (nullfieldMap.containsKey(k)) {
                    tmpArr = nullfieldMap.get(k);
                } else {
                    tmpArr = new JSONArray();
                }

                Standalone obj = (Application.getCompareAgainst() != CompareAgainst.STANDALONE) ? new Variance(fileName,
                        removeRedundantDifference(leftFlatMap.get(k)), "null") : new Standalone(fileName, "null");
                // if (!variance.getCapturedValue().equals(variance
                // .getExpectedValue())) {//Uncomment if-condition to check null values for stage, even if prod values are null too//
                tmpArr.add(obj);
                // }
                if (tmpArr.size() > 0) {
                    nullfieldMap.put(k, tmpArr);
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
            Standalone file = new Standalone(fileName, map.get(k) != null ? map.get(k) : "null");
            if (map.get(k) != null && !map.get(k).toString().equals("")) {
                jsonArray.add(file);
            }
            if (jsonArray.size() > 0) {
                map2.put(k, jsonArray);
            }
        }
    }
}


