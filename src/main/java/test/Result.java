package test;

import java.io.Serializable;
import org.json.simple.JSONObject;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class Result implements Serializable {
    private String fileName;

    private JSONObject keysOnlyOnLeft;

    private JSONObject keysOnlyOnRight;

    private JSONObject IdenticalKeys;

    private JSONObject DifferingKeys;
}
