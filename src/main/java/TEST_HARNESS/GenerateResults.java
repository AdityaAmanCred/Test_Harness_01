package TEST_HARNESS;

import static TEST_HARNESS.Util.fetchProperty;
import static TEST_HARNESS.Util.generateAggregateMap;
import static TEST_HARNESS.Util.getCommonFileNames;
import static TEST_HARNESS.Util.getFileNamesFromArray;
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
    private final ObjectMapper mapper = new ObjectMapper();

    public void writeTofile(String fileName, String results) {
        //Write JSON file
        try (FileWriter file = new FileWriter(fetchProperty("RESULT_DIR") + fileName + ".json")) {
            file.write(results);
            file.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public JSONArray generateFieldWiseDifferencesResults(Map<String, JSONArray> map) {
        Map<String, List<String>> aggregatedMap = generateAggregateMap(map);
        JSONArray jsonArray = new JSONArray();
        for (String aggKey : aggregatedMap.keySet()) {
            JSONArray varianceArr = new JSONArray();
            Set<String> fileSet = new HashSet<>();
            List<String> keys = aggregatedMap.get(aggKey);
            for (String key : keys) {
                JSONArray tmpVarArr = getJsonArrayForKey(map, key);
                varianceArr.addAll(tmpVarArr);
                fileSet.addAll(getFileNamesFromArray(tmpVarArr));
            }
            Double varPercentage = percentage(fileSet.size(), getCommonFileNames("SECONDARY_DIR", "PRIMARY_DIR").size());
            FieldWiseResult fieldResult = new FieldWiseResult(aggKey, varPercentage, varianceArr);
            jsonArray.add(fieldResult);
        }
        return jsonArray;
    }

    public JSONArray generateExclusiveFieldsResults(Comparator comparator, ParserType parserType) {
        Map<String, List<String>> aggregatedMap = generateAggregateMap(
                parserType == ParserType.SECONDARY ? comparator.getKeysOnLeft() : comparator.getKeysOnRight());
        JSONArray resultArr = new JSONArray();

        for (String aggKey : aggregatedMap.keySet()) {
            JSONArray tempArr1 = new JSONArray();
            List<String> keys = aggregatedMap.get(aggKey);
            Set<String> fileSet = new HashSet<>();
            for (String key : keys) {
                JSONArray tempArr2 = getJsonArrayForKey(parserType == ParserType.SECONDARY ? comparator.getKeysOnLeft() : comparator.getKeysOnRight(),
                        key);
                tempArr1.addAll(tempArr2);
                fileSet.addAll(getFileNamesFromArray(tempArr2));
            }
            Double varPercentage = percentage(fileSet.size(), getCommonFileNames("SECONDARY_DIR", "PRIMARY_DIR").size());
            FieldWiseResult fieldWiseResult = new FieldWiseResult(aggKey, varPercentage, tempArr1);
            resultArr.add(fieldWiseResult);
        }
        return resultArr;
    }

    public JSONArray getJsonArrayForKey(Map<String, JSONArray> map, String key) {
        return map.get(key);
    }

    public void generate(Comparator comparator) throws JsonProcessingException {
        //ComparisonStats comparisonStats = new ComparisonStats();
        //comparisonStats.GenerateStats();;
        FieldWiseResults primaryNullCheck = new FieldWiseResults(generateNullFieldsResults(comparator.getPrimaryNullFieldMap()));
        FieldWiseResults secondaryNullCheck = new FieldWiseResults(generateNullFieldsResults(comparator.getSecondaryNullFieldMap()));
        writeTofile("PrimaryParserNullFields", mapper.writeValueAsString(primaryNullCheck));
        writeTofile("SecondaryParserNullFields", mapper.writeValueAsString(secondaryNullCheck));
        if (Application.getCompareAgainst() != CompareAgainst.STANDALONE) {
            FieldWiseResults fieldWise = new FieldWiseResults(generateFieldWiseDifferencesResults(comparator.getDiffKeys()));
            FieldWiseResults leftOnly = new FieldWiseResults(generateExclusiveFieldsResults(comparator, ParserType.SECONDARY));
            FieldWiseResults rightOnly = new FieldWiseResults(generateExclusiveFieldsResults(comparator, ParserType.PRIMARY));
            writeTofile("FieldWiseDifferences", mapper.writeValueAsString(fieldWise));
            writeTofile("SecondaryParserExclusiveFields", mapper.writeValueAsString(leftOnly));
            writeTofile("PrimaryParserExclusiveFields", mapper.writeValueAsString(rightOnly));
        }
        System.out.printf("Results generated for %d files\n", getCommonFileNames("PRIMARY_DIR", "SECONDARY_DIR").size());
    }

    public JSONArray generateNullFieldsResults(Map<String, JSONArray> map) {
        Map<String, List<String>> aggregatedMap = generateAggregateMap(map);
        JSONArray jsonArray = new JSONArray();
        for (String aggKey : aggregatedMap.keySet()) {
            JSONArray varianceArr = new JSONArray();
            Set<String> fileSet = new HashSet<>();
            List<String> keys = aggregatedMap.get(aggKey);
            for (String key : keys) {
                JSONArray tmpVarArr = getJsonArrayForKey(map, key);
                varianceArr.addAll(tmpVarArr);
                fileSet.addAll(getFileNamesFromArray(tmpVarArr));
            }
            Double varPercentage = percentage(fileSet.size(), getCommonFileNames("SECONDARY_DIR", "PRIMARY_DIR").size());
            NullCheck fieldResult = new NullCheck(aggKey, varPercentage, varianceArr);
            jsonArray.add(fieldResult);
        }
        return jsonArray;
    }
}
