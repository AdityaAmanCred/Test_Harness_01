package TEST_HARNESS;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

@Data
@AllArgsConstructor
@Getter
public class DiffValues extends Object {
    private String expectedValue;

    private String capturedValue;
}
