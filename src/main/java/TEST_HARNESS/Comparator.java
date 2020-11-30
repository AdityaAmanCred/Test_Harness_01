package TEST_HARNESS;

import static TEST_HARNESS.Util.createJSONMap;
import static TEST_HARNESS.Util.fetchProperty;
import static TEST_HARNESS.Util.getCommonFileNames;
import static TEST_HARNESS.Util.getIssuerDetails;
import static TEST_HARNESS.Util.readJsonFile;
import static TEST_HARNESS.Util.removeRedundantDifference;
import static TEST_HARNESS.Util.setComparisonParameter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.collections.map.HashedMap;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import lombok.Getter;

@Getter
public class Comparator {
    private Map<String, Object> filteredLeftMap;

    private Map<String, Object> filteredRightMap;

    private String keysToCompare;

    private Map<String, JSONArray> diffKeys = new HashedMap();

    private Map<String, JSONArray> primaryNullFieldMap = new HashedMap();

    private Map<String, JSONArray> secondaryNullFieldMap = new HashedMap();

    private Map<String, JSONArray> keysOnLeft = new HashMap<>();

    private Map<String, JSONArray> keysOnRight = new HashMap<>();

    private Set<String> allfields = new HashSet<>();

    private Map<String, Integer> standaloneMap = new HashMap<>();

    private TemplateValidation templateValidation = new TemplateValidation();

    public Comparator() {
        keysToCompare = fetchProperty("KEY_FILTER");
        setComparisonParameter();
    }

    private void compare(String fileName) throws IOException, ParseException {

        Map<String, Object> rightFlatMap = Util.flatten(filteredRightMap);
        rightFlatMap.keySet().forEach(k -> allfields.add(k));
        String otherDir = Application.getCompareAgainst() == CompareAgainst.SECONDARY ? "SECONDARY_DIR" : "PRIMARY_DIR";
        if (otherDir.equals("SECONDARY_DIR")) {
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
        if (fetchProperty("SECONDARY_MODE_STANDALONE_ANALYSIS_DISABLED").toLowerCase().contains("false") || Application
                .getCompareAgainst() == CompareAgainst.STANDALONE) {
            standaloneAnalysis();
        }

        if (fetchProperty("ISSUER").toLowerCase().contains("rbl")) {
            templateValidation.validateAll();
        }
        String otherDir = Application.getCompareAgainst() == CompareAgainst.SECONDARY ? "SECONDARY_DIR" : "PRIMARY_DIR";
        List<String> commonFileNames = getCommonFileNames(otherDir, "PRIMARY_DIR");
        for (String fileName : commonFileNames) {
            if (otherDir.equals("SECONDARY_DIR")) {
                Object l_obj = readJsonFile(fetchProperty(otherDir) + fileName + ".json");
                filteredLeftMap = createJSONMap(l_obj, keysToCompare);
            }
            Object r_obj = readJsonFile(fetchProperty("PRIMARY_DIR") + fileName + ".json");
            filteredRightMap = createJSONMap(r_obj, keysToCompare);
            compare(fileName);
        }
        nullCheck();
    }

    public void standaloneAnalysis() throws IOException, ParseException {
        ObjectMapper objectMapper = new ObjectMapper();
        List<String> FileNames = getCommonFileNames(Application.getCompareAgainst() == CompareAgainst.SECONDARY ? "SECONDARY_DIR" : "PRIMARY_DIR",
                "PRIMARY_DIR");
        for (String filename : FileNames) {
            System.out.println("Performing Standalone analysis for file: " + filename);
            Object parserResponse = readJsonFile(fetchProperty("PRIMARY_DIR") + filename + ".json");
            SSPOJO requestBody = generateSSPojoRequestBody(parserResponse);
            Integer statusCode = StandaloneMode.generateStatement(objectMapper.writeValueAsString(requestBody));
            standaloneMap.put(filename, statusCode);
        }

    }

    public SSPOJO generateSSPojoRequestBody(Object parserResponse) throws IOException, ParseException {
        preprocess((JSONObject) ((JSONObject) parserResponse).get(Util.getMainJsonFieldName()));
        SSPOJO reqBody = new SSPOJO("002ed4e7-5f13-4a17-adaa-0c413d28f9d1", "CREDIT_CARD_STATEMENT",
                (JSONObject) ((JSONObject) parserResponse).get(Util.getMainJsonFieldName()));
        return reqBody;
    }

    public void preprocess(JSONObject requestBody) throws IOException, ParseException {
        try {
            JSONObject standaloneAnalysisJsonObject = (JSONObject) readJsonFile("StandaloneAnalysisResource.json");
            JSONObject issuerJson = getIssuerDetails(standaloneAnalysisJsonObject, fetchProperty("ISSUER"));
            JSONObject cardDetails = (JSONObject) requestBody.get("card_details");
            if (cardDetails.get("card_number") == null) {
                cardDetails.put("card_number", issuerJson.get("card_number"));
            }
            cardDetails.put("instrument_id", issuerJson.get("instrument_id"));
            JSONObject userDetails = (JSONObject) requestBody.get("user_details");
            userDetails.put("user_id", standaloneAnalysisJsonObject.get("user_id"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void nullCheck() throws IOException, ParseException {
        String otherDir = Application.getCompareAgainst() == CompareAgainst.SECONDARY ? "SECONDARY_DIR" : "PRIMARY_DIR";
        List<String> commonFileNames = getCommonFileNames(otherDir, "PRIMARY_DIR");
        for (String fileName : commonFileNames) {
            Object r_obj = readJsonFile(fetchProperty("PRIMARY_DIR") + fileName + ".json");
            filteredRightMap = createJSONMap(r_obj, keysToCompare);
            if (otherDir.equals("SECONDARY_DIR")) {
                Object l_obj = readJsonFile(fetchProperty(otherDir) + fileName + ".json");
                filteredLeftMap = createJSONMap(l_obj, keysToCompare);
            }

            updateNullCheckMap(fileName, ParserType.PRIMARY);
            if (Application.getCompareAgainst() == CompareAgainst.SECONDARY) {
                updateNullCheckMap(fileName, ParserType.SECONDARY);
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
            Variance variance = new Variance(fileName, removeRedundantDifference(diff.get(key).getCapturedValue()),
                    removeRedundantDifference(diff.get(key).getExpectedValue()));
            if (!variance.getCapturedValue().equals(variance.getExpectedValue())) {
                varList.add(variance);
            }
            if (varList.size() > 0) {
                diffKeys.put(key, varList);
            }
        }
    }

    private void updateNullCheckMap(String fileName, ParserType parserType) {
        Map<String, Object> rightFlatMap = Util.flatten(filteredRightMap);
        Map<String, Object> leftFlatMap = (Application.getCompareAgainst() != CompareAgainst.STANDALONE) ? Util.flatten(filteredLeftMap) : null;
        if (parserType == ParserType.SECONDARY) {
            Map<String, Object> temp = leftFlatMap;
            leftFlatMap = rightFlatMap;
            rightFlatMap = temp;
        }

        for (String k : allfields) {
            if (rightFlatMap.containsKey(k) == true && (rightFlatMap.get(k) == null || rightFlatMap.get(k).toString().equals(""))) {
                JSONArray tmpArr;
                if ((parserType == ParserType.PRIMARY ? primaryNullFieldMap : secondaryNullFieldMap).containsKey(k)) {
                    tmpArr = (parserType == ParserType.PRIMARY ? primaryNullFieldMap : secondaryNullFieldMap).get(k);
                } else {
                    tmpArr = new JSONArray();
                }
                Standalone obj = (Application.getCompareAgainst() == CompareAgainst.SECONDARY) ? new Variance(fileName, "null",
                        removeRedundantDifference(leftFlatMap.get(k))) : new Standalone(fileName, "null");
                if (((obj instanceof Variance) && !obj.getCapturedValue().equals(((Variance) obj)
                        .getExpectedValue())) || !(obj instanceof Variance)) {//Uncomment the if-condition to flag null values for primary/secondary parser, even if other parser values are null too//
                    tmpArr.add(obj);
                }
                if (tmpArr.size() > 0) {
                    (parserType == ParserType.PRIMARY ? primaryNullFieldMap : secondaryNullFieldMap).put(k, tmpArr);
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


