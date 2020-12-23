package TEST_HARNESS.result.pojos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

@Data
@AllArgsConstructor
@Getter
public class DiffValues extends Object {
    private Object expectedValue;

    private Object capturedValue;
}
