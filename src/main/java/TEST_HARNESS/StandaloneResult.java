package TEST_HARNESS;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@AllArgsConstructor
@EqualsAndHashCode
public class StandaloneResult extends Object {
    private String filename;

    private int statuscode;
}
