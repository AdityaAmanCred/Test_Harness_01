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
    private int DiffCount;

    private int IdenticalCount;

    private int LeftOnlyCount;

    private int RightOnlyCount;

    private int TotalFileCount;

    private JSONArray FileWise;

    private JSONArray FieldWise;
}
