package TEST_HARNESS.result.pojos;

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
