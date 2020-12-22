package TEST_HARNESS.result.pojos;

import org.json.simple.JSONObject;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SSPOJO {
    private String id;

    private String object_type;

    private JSONObject json_object;
}
