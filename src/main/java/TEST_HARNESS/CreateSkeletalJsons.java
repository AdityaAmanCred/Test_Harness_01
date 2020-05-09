package TEST_HARNESS;

import static TEST_HARNESS.Util.fetchProperty;
import static TEST_HARNESS.Util.getNames;
import static TEST_HARNESS.Util.getPropertyFromFile;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class CreateSkeletalJsons {
    private ObjectMapper objMapper;

    public CreateSkeletalJsons() {
        this.objMapper = new ObjectMapper();
    }

    public void writeJsonFile(String fileName, JSONObject jsonObject) throws IOException, ParseException {

        try {

            FileWriter file = new FileWriter(fetchProperty("EXPECTED_DIR") + fileName + ".json");
            file.write(jsonObject.toJSONString());
            file.flush();
            file.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void createJsonFiles() {
        for (String fileName : getNames("StageResponses")) {
            try {
                Object obj = new JSONParser().parse(new FileReader(fetchProperty("STAGE_DIR") + fileName + ".json"));

                JSONObject nullified = nullifyFields((JSONObject) obj);
                writeJsonFile(fileName, nullified);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

    }

    public JSONObject nullifyFields(JSONObject jsonObject) throws IOException, ParseException {
        String jsonObjectString = JSONValue.toJSONString(jsonObject);
        TypeReference<Map<String, Object>> type = new TypeReference<Map<String, Object>>() {};
        Map<String, Object> jsonObjectMap = objMapper.readValue(jsonObjectString, type);
        Map<String, Object> flattened = Util.flatten(jsonObjectMap);
        flattened.replaceAll((k, v) -> v = null);
        Map<String, Object> unflattened = new HashMap<>();
        for (Map.Entry<String, Object> e : flattened.entrySet()) {
            String[] parts = StringUtils.split(e.getKey(), ".");
            Map<String, Object> dest = unflattened;
            for (int i = 0; i != parts.length - 1; i++) {
                Object tmp = dest.get(parts[i]);
                if (tmp == null) {
                    Map<String, Object> next = new HashMap<>();
                    dest.put(parts[i], next);
                    dest = next;
                    continue;
                }
                if (!(tmp instanceof Map)) {
                    throw new IllegalStateException();
                }
                dest = (Map<String, Object>) tmp;
            }
            dest.put(parts[parts.length - 1], e.getValue());
        }
        return (JSONObject) new JSONParser().parse(objMapper.writeValueAsString(unflattened));
    }
}
