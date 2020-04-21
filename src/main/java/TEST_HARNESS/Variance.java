package TEST_HARNESS;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class Variance implements Serializable {
    private String fileName;

    private Object expectedValue;

    private Object capturedValue;
}
