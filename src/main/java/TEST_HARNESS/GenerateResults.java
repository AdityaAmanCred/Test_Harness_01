package TEST_HARNESS;

import static TEST_HARNESS.Util.getCommonFileNames;
import static TEST_HARNESS.Util.getFileNamesFromArray;
import static TEST_HARNESS.Util.getPropertyFromFile;
import static TEST_HARNESS.Util.percentage;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.json.simple.JSONArray;

public class GenerateResults {
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

    public JSONArray getVarianceArray(Map<String, JSONArray> diffKeys2, String key) {
        return diffKeys2.get(key);
    }
}
