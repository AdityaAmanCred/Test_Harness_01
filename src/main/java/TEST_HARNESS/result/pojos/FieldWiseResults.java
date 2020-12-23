package TEST_HARNESS.result.pojos;

import java.io.Serializable;
import org.json.simple.JSONArray;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@ToString
@EqualsAndHashCode
@AllArgsConstructor
public class FieldWiseResults implements Serializable {
    private JSONArray FieldWise;
}
