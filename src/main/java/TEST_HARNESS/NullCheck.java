package TEST_HARNESS;

import org.json.simple.JSONArray;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@AllArgsConstructor
@EqualsAndHashCode
public class NullCheck extends Object {
    private String key;

    private Double NullPercentage;

    private JSONArray Entities;
}


