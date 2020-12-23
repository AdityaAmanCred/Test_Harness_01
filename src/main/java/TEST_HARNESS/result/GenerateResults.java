package TEST_HARNESS.result;

import static TEST_HARNESS.utils.Util.fetchProperty;
import static TEST_HARNESS.utils.Util.generateAggregateMap;
import static TEST_HARNESS.utils.Util.getCommonFileNames;
import static TEST_HARNESS.utils.Util.getFileNamesFromArray;
import static TEST_HARNESS.utils.Util.percentage;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.tuple.Pair;
import org.json.simple.JSONArray;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import TEST_HARNESS.Application;
import TEST_HARNESS.config.CompareAgainst;
import TEST_HARNESS.config.Config;
import TEST_HARNESS.result.pojos.ValidationFailure;
import TEST_HARNESS.result.pojos.ValidationFailureResults;
import TEST_HARNESS.comparator.Comparator;
import TEST_HARNESS.parse.ParserType;
import TEST_HARNESS.result.pojos.FieldWiseResult;
import TEST_HARNESS.result.pojos.FieldWiseResults;
import TEST_HARNESS.result.pojos.NullCheck;
import TEST_HARNESS.result.pojos.StandaloneResult;
import TEST_HARNESS.result.pojos.StandaloneResults;
import TEST_HARNESS.result.pojos.TemplateFAILURETYPE;

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
        FieldWiseResults primaryNullCheck = new FieldWiseResults(generateNullFieldsResults(comparator.getPrimaryNullFieldMap()));
        writeTofile("PrimaryParserNullFields", mapper.writeValueAsString(primaryNullCheck));
        if (fetchProperty("SECONDARY_MODE_STANDALONE_ANALYSIS_DISABLED").toLowerCase().contains("false") || Config
                .getCompareAgainst() == CompareAgainst.STANDALONE) {
            long failureCount = comparator.getStandaloneMap().entrySet().stream().filter(stringIntegerEntry -> stringIntegerEntry.getValue() != 200)
                                          .count();
            Double failurePercentage = Double.valueOf(failureCount * 1.0 / comparator.getStandaloneMap().size()) * 100;
            JSONArray jsonArray = new JSONArray();
            for (String filename : comparator.getStandaloneMap().keySet()) {
                Integer statusCode = comparator.getStandaloneMap().get(filename);
                if (statusCode != 200) {
                    jsonArray.add(new StandaloneResult(filename, statusCode));
                }
            }
            StandaloneResults standaloneResults = new StandaloneResults(failurePercentage, jsonArray);
            writeTofile("StandAloneAnalysis", mapper.writeValueAsString(standaloneResults));
        }
        if (fetchProperty("ISSUER").toLowerCase().contains("rbl")) {
            FieldWiseResults templateValidationFailureResults = new FieldWiseResults(
                    generateTemplateValidationResults(comparator.getTemplateValidation().getValidationMap()));
            writeTofile("TemplateValidationResults", mapper.writeValueAsString(templateValidationFailureResults));
        }
        if (Config.getCompareAgainst() == CompareAgainst.SECONDARY) {
            FieldWiseResults secondaryNullCheck = new FieldWiseResults(generateNullFieldsResults(comparator.getSecondaryNullFieldMap()));
            FieldWiseResults fieldWise = new FieldWiseResults(generateFieldWiseDifferencesResults(comparator.getDiffKeys()));
            FieldWiseResults leftOnly = new FieldWiseResults(generateExclusiveFieldsResults(comparator, ParserType.SECONDARY));
            FieldWiseResults rightOnly = new FieldWiseResults(generateExclusiveFieldsResults(comparator, ParserType.PRIMARY));
            writeTofile("SecondaryParserNullFields", mapper.writeValueAsString(secondaryNullCheck));
            writeTofile("FieldWiseDifferences", mapper.writeValueAsString(fieldWise));
            writeTofile("SecondaryParserExclusiveFields", mapper.writeValueAsString(leftOnly));
            writeTofile("PrimaryParserExclusiveFields", mapper.writeValueAsString(rightOnly));
        }
        System.out.printf("Results generated for %d files\n",
                getCommonFileNames(Config.getCompareAgainst() == CompareAgainst.SECONDARY ? "SECONDARY_DIR" : "PRIMARY_DIR", "PRIMARY_DIR").size());
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
            Double varPercentage = percentage(fileSet.size(),
                    getCommonFileNames(Config.getCompareAgainst() == CompareAgainst.SECONDARY ? "SECONDARY_DIR" : "PRIMARY_DIR", "PRIMARY_DIR")
                            .size());
            NullCheck fieldResult = new NullCheck(aggKey, varPercentage, varianceArr);
            jsonArray.add(fieldResult);
        }
        return jsonArray;
    }

    public JSONArray generateTemplateValidationResults(Map<TemplateFAILURETYPE, ArrayList<Pair<String, Double>>> validationMap) {
        JSONArray results = new JSONArray();
        for (TemplateFAILURETYPE templateFAILURETYPE : validationMap.keySet()) {
            ArrayList<Pair<String, Double>> arrayList = validationMap.get(templateFAILURETYPE);
            Set<String> failedFileNames = new HashSet<>();
            JSONArray validationFailures = new JSONArray();
            for (Pair<String, Double> p : arrayList) {
                failedFileNames.add(p.getKey());
                validationFailures.add(new ValidationFailure(p.getKey(), p.getValue()));
            }

            results.add(new ValidationFailureResults(percentage(failedFileNames.size(),
                    getCommonFileNames(Config.getCompareAgainst() == CompareAgainst.SECONDARY ? "SECONDARY_DIR" : "PRIMARY_DIR", "PRIMARY_DIR")
                            .size()), templateFAILURETYPE.toString(), validationFailures));
        }
        return results;
    }
}
