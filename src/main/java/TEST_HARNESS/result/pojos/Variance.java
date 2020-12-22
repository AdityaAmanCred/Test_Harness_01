package TEST_HARNESS.result.pojos;

import java.io.Serializable;
import TEST_HARNESS.result.pojos.Standalone;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@EqualsAndHashCode
@ToString
@Getter
public class Variance extends Standalone implements Serializable {
    private Object expectedValue;

    public Variance(String fileName, Object capturedValue, Object expectedValue) {
        super(fileName, capturedValue);
        this.expectedValue = expectedValue;
    }
}
