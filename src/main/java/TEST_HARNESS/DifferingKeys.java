package TEST_HARNESS;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@AllArgsConstructor
@EqualsAndHashCode
public class DifferingKeys implements Serializable {
    private String key;

    private List<String> fileNames;

    private Double failurePercentage;
}
