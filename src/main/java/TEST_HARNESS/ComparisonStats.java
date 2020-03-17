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
            FileWiseResult fileWiseResult = (FileWiseResult) itr.next();
            if (fileWiseResult.getKeysOnlyOnLeft().size() > 0) {
                LeftOnlyCount++;
            }
            if (fileWiseResult.getKeysOnlyOnRight().size() > 0) {
                rightOnlyCount++;
            }
            if (fileWiseResult.getDifferingKeys().size() > 0) {
                diffCount++;
            }
            if (fileWiseResult.getKeysOnlyOnLeft().size() == 0 && fileWiseResult.getKeysOnlyOnRight().size() == 0 && fileWiseResult.getDifferingKeys().size() == 0) {
                IdenticalCount++;
            }
        }

    }
}