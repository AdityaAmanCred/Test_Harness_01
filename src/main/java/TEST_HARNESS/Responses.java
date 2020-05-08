package TEST_HARNESS;

import static TEST_HARNESS.Util.fetchProperty;
import static TEST_HARNESS.Util.setComparisonParameter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import com.google.common.util.concurrent.RateLimiter;
import lombok.Data;

@Data
public abstract class Responses {
    protected RateLimiter rateLimiter;

    protected int fetchCounter;

    public Responses(double fetchRate) {
        this.rateLimiter = RateLimiter.create(fetchRate);
        this.fetchCounter = 0;
        setComparisonParameter();
    }

    public void saveResponse(byte[] bytes, String fileName, Environment env, int fetchCounter) {
        java.io.File file;
        if (env == Environment.PROD) {
            file = new java.io.File(fetchProperty("EXPECTED_DIR") + fileName.split("\\.")[0] + ".json");
        } else {
            file = new File(fetchProperty("STAGE_DIR") + fileName.split("\\.")[0] + ".json");
        }

        try {

            OutputStream os = new FileOutputStream(file);

            os.write(bytes);
            if (env == Environment.PROD) {
                System.out.println("Prod response fetched for: " + fileName + " fetchedCount = " + fetchCounter);
            } else {
                System.out.println("Stage response fetched for: " + fileName + " fetchedCount = " + fetchCounter);
            }

            os.close();
        } catch (Exception e) {
            System.out.println(fileName + ".pdf: Exception: " + e);
        }
    }
}
