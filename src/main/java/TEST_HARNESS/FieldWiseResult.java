package TEST_HARNESS;

import java.io.Serializable;
import java.util.List;
import org.json.simple.JSONArray;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@AllArgsConstructor
@EqualsAndHashCode
public class FieldWiseResult implements Serializable {
    private String key;

    private Double variancePercentage;

    private JSONArray variance;
}
