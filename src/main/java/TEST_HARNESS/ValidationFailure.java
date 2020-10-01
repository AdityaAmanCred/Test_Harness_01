package TEST_HARNESS;

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
