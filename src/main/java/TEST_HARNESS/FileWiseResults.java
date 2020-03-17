package TEST_HARNESS;

import java.io.Serializable;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class FileWiseResults implements Serializable {
    private int DiffCount;

    private int IdenticalCount;

    private int LeftOnlyCount;

    private int RightOnlyCount;

    private int TotalFileCount;

    private JSONArray FileWise;
}
