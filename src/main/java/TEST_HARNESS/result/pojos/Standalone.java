package TEST_HARNESS.result.pojos;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class Standalone implements Serializable {
    private String fileName;

    private Object capturedValue;
}
