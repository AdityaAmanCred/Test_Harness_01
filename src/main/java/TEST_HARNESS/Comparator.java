package TEST_HARNESS;

import static TEST_HARNESS.Util.createJSONMap;
import static TEST_HARNESS.Util.getCommonFileNames;
import static TEST_HARNESS.Util.getPropertyFromFile;
import static TEST_HARNESS.Util.mapToJSONConverter;
import static TEST_HARNESS.Util.readCSVLineByLine;
import static TEST_HARNESS.Util.removeRedundantDifference;
import static TEST_HARNESS.Util.replaceNumbers;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.collections.map.HashedMap;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
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

    private Set<String> mandatoryFields;

    public Comparator() {
        keysToCompare = getPropertyFromFile("application.properties").getProperty("KEY_FILTER");
        mandatoryFields = readCSVLineByLine(getPropertyFromFile("application.properties").getProperty("MANDATORY_FIELDS_CSV")).stream().collect(
                Collectors.toSet());
    }

    private FileWiseResult compare(String fileName) throws JsonProcessingException, ParseException {

        System.out.println("Comparing responses for file: " + fileName);
        Map<String, Object> leftFlatMap = Util.flatten(filteredLeftMap);
        Map<String, Object> rightFlatMap = Util.flatten(filteredRightMap);

        MapDifference<String, Object> difference = Maps.difference(leftFlatMap, rightFlatMap);

        JSONObject leftOnlyJson = mapToJSONConverter(difference.entriesOnlyOnLeft());

        JSONObject rightOnlyJson = mapToJSONConverter(difference.entriesOnlyOnRight());

        JSONObject commonJson = mapToJSONConverter(difference.entriesInCommon());

        Map<String, DiffValues> diff = difference.entriesDiffering().entrySet().stream().collect(
                Collectors.toMap(Map.Entry::getKey, e -> new DiffValues(e.getValue().leftValue(), e.getValue().rightValue())));

        String differingEntries = new ObjectMapper().writeValueAsString(diff);
        JSONParser parser = new JSONParser();
        JSONObject diffJson = (JSONObject) parser.parse(differingEntries);
        FileWiseResult fileWiseResult = new FileWiseResult(fileName, leftOnlyJson, rightOnlyJson, new DummyPOJO(), new DummyPOJO());
        updateDiffKeys(diff, fileName);
        updateNullCheckMap(fileName);
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
            filteredLeftMap = createJSONMap(l_obj, keysToCompare);
            filteredRightMap = createJSONMap(r_obj, keysToCompare);
            jsonArray.add(this.compare(fileName));
        }
        generateAggregateMap();
        System.out.println("Results Generated for " + commonFileNames.size() + " files");
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
        for (String k : mandatoryFields) {
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
                //if (!variance.getCapturedValue().equals(variance.getExpectedValue())) {
                tmpVarArr.add(variance);
                // }
                if (tmpVarArr.size() > 0) {
                    mfields.put(k, tmpVarArr);
                }
            }
        }
        //System.out.println("h");
    }
}


