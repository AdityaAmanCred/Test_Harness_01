package TEST_HARNESS;

import static TEST_HARNESS.Util.createJSONMap;
import static TEST_HARNESS.Util.getCommonFileNames;
import static TEST_HARNESS.Util.getFileNamesFromArray;
import static TEST_HARNESS.Util.getNames;
import static TEST_HARNESS.Util.getPropertyFromFile;
import static TEST_HARNESS.Util.mapToJSONConverter;
import static TEST_HARNESS.Util.percentage;
import static TEST_HARNESS.Util.readCSVLineByLine;
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

        Map<String, DiffValues> diff = difference.entriesDiffering().entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey,
                e -> new DiffValues(e.getValue().leftValue() != null ? e.getValue().leftValue().toString() : "null",
                        e.getValue().rightValue() != null ? e.getValue().rightValue().toString() : "null")));

        String differingEntries = new ObjectMapper().writeValueAsString(diff);
        JSONParser parser = new JSONParser();
        JSONObject diffJson = (JSONObject) parser.parse(differingEntries);
        FileWiseResult fileWiseResult = new FileWiseResult(fileName, leftOnlyJson, rightOnlyJson, commonJson, diffJson);
        updateDiffKeys(diff, fileName);
        updateMFMap(fileName);
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

            if (diffKeys.containsKey(key)) {
                JSONArray varList = diffKeys.get(key);

                varList.add(new Variance(fileName, diff.get(key).getExpectedValue(), diff.get(key).getCapturedValue()));
                diffKeys.put(key, varList);
            } else {
                JSONArray newList = new JSONArray();
                newList.add(new Variance(fileName, diff.get(key).getExpectedValue(), diff.get(key).getCapturedValue()));
                diffKeys.put(key, newList);
            }
        }
    }

    public void updateMFMap(String fileName) {
        Map<String, Object> leftFlatMap = Util.flatten(filteredLeftMap);
        Map<String, Object> rightFlatMap = Util.flatten(filteredRightMap);
        Set<String> ValidMF = Sets.intersection(mandatoryFields, rightFlatMap.keySet());
        for (String k : ValidMF) {
            if (rightFlatMap.get(k) == null || rightFlatMap.get(k).toString().equals("")) {
                JSONArray tmpVarArr;
                if (mfields.containsKey(k)) {
                    tmpVarArr = mfields.get(k);
                } else {
                    tmpVarArr = new JSONArray();
                }

                Variance variance = new Variance(fileName, leftFlatMap.get(k) != null ? leftFlatMap.get(k).toString() : null,
                        rightFlatMap.get(k) != null ? rightFlatMap.get(k).toString() : null);
                tmpVarArr.add(variance);
                mfields.put(k, tmpVarArr);
            }
        }
    }
}


