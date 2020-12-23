package TEST_HARNESS.result.pojos;

import org.json.simple.JSONArray;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@AllArgsConstructor
@EqualsAndHashCode
public class ValidationFailureResults {
    private Double failurePercentage;

    private String failureType;

    private JSONArray failedFiles;
}
