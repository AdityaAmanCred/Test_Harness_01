package TEST_HARNESS;

import static TEST_HARNESS.Util.getCommonFileNames;
import static TEST_HARNESS.Util.getFileNamesFromArray;
import static TEST_HARNESS.Util.getNames;
import static TEST_HARNESS.Util.getPropertyFromFile;
import static TEST_HARNESS.Util.percentage;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.json.simple.JSONArray;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class GenerateResults {
    private ObjectMapper mapper = new ObjectMapper();

    public void writeTofile(String fileName, String results) {
        //Write JSON file
        try (FileWriter file = new FileWriter(getPropertyFromFile("application.properties").getProperty("RESULT_DIR") + fileName + ".json")) {
            file.write(results);
            file.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public JSONArray generateFieldWiseResults(Map<String, List<String>> aggregateMap, Map<String, JSONArray> diffKeys) {
        JSONArray jsonArray = new JSONArray();
        for (String aggKey : aggregateMap.keySet()) {
            JSONArray varianceArr = new JSONArray();
            Set<String> fileSet = new HashSet<>();
            List<String> keys = aggregateMap.get(aggKey);
            for (String key : keys) {
                JSONArray tmpVarArr = getVarianceArray(diffKeys, key);
                varianceArr.addAll(tmpVarArr);
                fileSet.addAll(getFileNamesFromArray(tmpVarArr));
            }
            Double varPercentage = percentage(fileSet.size(), getCommonFileNames("EXPECTED_DIR", "STAGE_DIR").size());
            FieldWiseResult fieldResult = new FieldWiseResult(aggKey, varPercentage, varianceArr);
            jsonArray.add(fieldResult);
        }
        return jsonArray;
    }

    public JSONArray generateMissingKeyResults(Comparator comparator, Environment env) {
        JSONArray jsonArray = new JSONArray();
        Set<String> nonIntersectionKeys = (env == Environment.PROD) ? comparator.getKeysOnLeft().keySet() : comparator.getKeysOnRight().keySet();
        for (String k : nonIntersectionKeys) {
            JSONArray fileArr = (env == Environment.PROD) ? comparator.getKeysOnLeft().get(k) : comparator.getKeysOnRight().get(k);
            jsonArray.add(new MissingKeys(k, fileArr));
        }
        return jsonArray;
    }

    public JSONArray getVarianceArray(Map<String, JSONArray> diffKeys2, String key) {
        return diffKeys2.get(key);
    }

    public void generate(Comparator comparator) throws JsonProcessingException {
        //        ComparisonStats comparisonStats = new ComparisonStats();
        //        comparisonStats.GenerateStats();
        JSONArray fieldResults = generateFieldWiseResults(comparator.getAggregateMap(), comparator.getDiffKeys());
        FieldWiseResults fieldWise = new FieldWiseResults(fieldResults);
        FieldWiseResults leftOnly = new FieldWiseResults(generateMissingKeyResults(comparator, Environment.PROD));
        FieldWiseResults rightOnly = new FieldWiseResults(generateMissingKeyResults(comparator, Environment.STAGE));
        FieldWiseResults nullCheck = new FieldWiseResults(generateMFResults(comparator.getNullfieldMap()));
        writeTofile("field_wise", mapper.writeValueAsString(fieldWise));
        writeTofile("nullcheck", mapper.writeValueAsString(nullCheck));
        writeTofile("leftOnly", mapper.writeValueAsString(leftOnly));
        writeTofile("rightOnly", mapper.writeValueAsString(rightOnly));
        System.out.printf("Results generated for %d files", getCommonFileNames("EXPECTED_DIR", "STAGE_DIR").size());
    }

    public JSONArray generateMFResults(Map<String, JSONArray> mfMap) {
        JSONArray jsonArray = new JSONArray();
        for (String k : mfMap.keySet()) {
            Double varPercentage = percentage(mfMap.get(k).size(), getNames("STAGE_DIR").size());
            NullCheck fieldWiseResult = new NullCheck(k, varPercentage, getVarianceArray(mfMap, k));
            jsonArray.add(fieldWiseResult);
        }
        return jsonArray;
    }
}
