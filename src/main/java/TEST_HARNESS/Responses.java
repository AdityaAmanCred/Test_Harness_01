package TEST_HARNESS;

import static TEST_HARNESS.ParserResponses.*;
import static TEST_HARNESS.Util.fetchProperty;
import static TEST_HARNESS.Util.setComparisonParameter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import com.google.common.util.concurrent.RateLimiter;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
public abstract class Responses {
    protected RateLimiter rateLimiter;

    protected int fetchCounter;

    @Getter
    @Setter
    protected static ParserType parserType;

    public Responses(double fetchRate) {
        this.rateLimiter = RateLimiter.create(fetchRate);
        this.fetchCounter = 0;
        setComparisonParameter();
    }

    public void saveResponse(byte[] bytes, String fileName, int fetchCounter) {
        java.io.File file;
        if (this.parserType == ParserType.SECONDARY) {
            file = new java.io.File(fetchProperty("SECONDARY_DIR") + fileName.split("\\.")[0] + ".json");
        } else {
            file = new File(fetchProperty("PRIMARY_DIR") + fileName.split("\\.")[0] + ".json");
        }

        try {

            OutputStream os = new FileOutputStream(file);

            os.write(bytes);
            if (this.parserType == ParserType.SECONDARY) {
                System.out.println("Secondary Parser response fetched for: " + fileName + " fetchedCount = " + fetchCounter);
            } else {
                System.out.println("Primary Parser response fetched for: " + fileName + " fetchedCount = " + fetchCounter);
            }

            os.close();
        } catch (Exception e) {
            System.out.println(fileName + ".pdf: Exception: " + e);
        }
    }
}
