package TEST_HARNESS;

import java.util.Iterator;
import org.json.simple.JSONArray;
import lombok.Getter;

@Getter
public class ComparisonStats {
    private JSONArray results;

    private int diffCount;

    private int IdenticalCount;

    private int LeftOnlyCount;

    private int rightOnlyCount;

    private int totalFileCount;

    public ComparisonStats(JSONArray results) {
        this.results = results;
    }

    public void GenerateStats() {
        totalFileCount = results.size();
        Iterator itr = results.iterator();
        while (itr.hasNext()) {
            Result result = (Result) itr.next();
            if (result.getKeysOnlyOnLeft().size() > 0) {
                LeftOnlyCount++;
            }
            if (result.getKeysOnlyOnRight().size() > 0) {
                rightOnlyCount++;
            }
            if (result.getDifferingKeys().size() > 0) {
                diffCount++;
            }
            if (result.getKeysOnlyOnLeft().size() == 0 && result.getKeysOnlyOnRight().size() == 0 && result.getDifferingKeys().size() == 0) {
                IdenticalCount++;
            }
        }

    }
}