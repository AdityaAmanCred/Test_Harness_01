package TEST_HARNESS;

import org.json.simple.JSONArray;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@ToString
@EqualsAndHashCode
@AllArgsConstructor
public class CompleteResults {
    private int diffCount;

    private int IdenticalCount;

    private int LeftOnlyCount;

    private int rightOnlyCount;

    private int totalFileCount;

    private JSONArray results;
}
