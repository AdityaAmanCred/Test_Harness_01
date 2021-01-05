package TEST_HARNESS.result.pojos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@AllArgsConstructor
@EqualsAndHashCode
public class ValidationFailure {
    private String fileName;

    private Double diff;
}
