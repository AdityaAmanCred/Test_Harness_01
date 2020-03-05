package test;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.bohnman.squiggly.Squiggly;
import com.github.bohnman.squiggly.util.SquigglyUtils;
import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;

public class Comparator {
    private Map<String, Object> leftMap;

    private Map<String, Object> rightMap;

    private Map<String, Object> selectedLeftMap;

    private Map<String, Object> selectedRightMap;

    private String leftJson;

    private String rightJson;

    private ObjectMapper objMapper;

    private String keysToCompare = "**";

    public Comparator() {
        this.objMapper = new ObjectMapper();
    }

    private void setLeftAndRightJson(String leftJson, String rightJson) {
        this.leftJson = leftJson;
        this.rightJson = rightJson;
    }

    private Result compare(String fileName) throws JsonProcessingException, ParseException {
        Map<String, Object> leftFlatMap = CompareUtil.flatten(selectedLeftMap);
        Map<String, Object> rightFlatMap = CompareUtil.flatten(selectedRightMap);
        List<Pair<String, String>> leftOnly = new ArrayList<>();
        List<Pair<String, String>> rightOnly = new ArrayList<>();
        List<Pair<String, String>> identical_keys = new ArrayList<>();
        List<Pair<String, Pair<String, String>>> non_identical_keys = new ArrayList<>();

        MapDifference<String, Object> difference = Maps.difference(leftFlatMap, rightFlatMap);

        String entriesOnlyOnLeft = new ObjectMapper().writeValueAsString(difference.entriesOnlyOnLeft());
        JSONParser parser = new JSONParser();
        JSONObject leftOnlyJson = (JSONObject) parser.parse(entriesOnlyOnLeft);

        String entriesOnlyOnRight = new ObjectMapper().writeValueAsString(difference.entriesOnlyOnRight());
        JSONObject rightOnlyJson = (JSONObject) parser.parse(entriesOnlyOnRight);

        String identicalFields = new ObjectMapper().writeValueAsString(difference.entriesInCommon());
        JSONObject commonJson = (JSONObject) parser.parse(identicalFields);

        //        String differingFields =new ObjectMapper().writeValueAsString(difference.entriesDiffering());
        //        JSONObject DifferingJson = (JSONObject) parser.parse(differingFields);
        Map<String, Object> diff = difference.entriesDiffering().entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey,
                e -> new DiffValues(e.getValue().leftValue() != null ? e.getValue().leftValue().toString() : "null",
                        e.getValue().rightValue() != null ? e.getValue().rightValue().toString() : "null")));
        String differingEntries = new ObjectMapper().writeValueAsString(diff);
        JSONObject diffJson = (JSONObject) parser.parse(differingEntries);

        //System.out.println("Keys only on left:\n");
        //        difference.entriesOnlyOnLeft().forEach((key, value) -> System.out.println(key.getClass()));
        //
        //        System.out.println("\n\nKeys only on right:\n");
        //        difference.entriesOnlyOnRight().forEach((key, value) -> rightOnly
        //                .add(new ImmutablePair<String, String>(key!=null?key.toString():"null", value!=null?key.toString():"null")));//System.out.println(key + ": " + value));
        //
        //        System.out.println("\n\nDiffering key value pairs:\n");
        //        difference.entriesDiffering().forEach((key, value) -> non_identical_keys.add(new ImmutablePair<>(key!=null?key.toString(),
        //                new ImmutablePair<String, String>(value.leftValue()!=null?value.leftValue().toString():,
        //                        value.rightValue().toString()))));//System.out.println(key + ": " + value));
        //
        //        System.out.println("\n\nIdentical key value pairs:\n");
        //        difference.entriesInCommon().forEach((key, value) -> identical_keys
        //                .add(new ImmutablePair<String, String>(key.toString(), value.toString())));//System.out.println(key + ": " + value));

        Result result = new Result(fileName, leftOnlyJson, rightOnlyJson, commonJson, diffJson);
        System.out.println("inside Compare");
        return result;
    }

    public JSONArray compareAll() throws IOException, ParseException {
        JSONArray jsonArray = new JSONArray();
        for (String fileName : Main.fileNames) {
            Object l_obj = new JSONParser()
                    .parse(new FileReader("/Users/loaner/Desktop/Comparator/src/main/resources/ExpectedResponses/" + fileName + ".json"));
            Object r_obj = new JSONParser()
                    .parse(new FileReader("/Users/loaner/Desktop/Comparator/src/main/resources/StageResponses/" + fileName + ".json"));

            this.setLeftAndRightJson(JSONValue.toJSONString(l_obj), JSONValue.toJSONString(r_obj));
            this.createJsonMaps();
            jsonArray.add(this.compare(fileName));

        }
        return jsonArray;
    }

    public void createJsonMaps() throws IOException, ParseException {
        JSONParser parser = new JSONParser();
        ObjectMapper mapper = Squiggly.init(new ObjectMapper(), keysToCompare);

        TypeReference<Map<String, Object>> type = new TypeReference<Map<String, Object>>() {};
        leftMap = objMapper.readValue(leftJson, type);
        rightMap = objMapper.readValue(rightJson, type);
        selectedLeftMap = objMapper.readValue(SquigglyUtils.stringify(mapper, leftMap), type);
        selectedRightMap = objMapper.readValue(SquigglyUtils.stringify(mapper, rightMap), type);
    }
}


