package TEST_HARNESS;

import com.google.common.util.concurrent.RateLimiter;
import lombok.Data;

@Data
public abstract class Responses {
    protected RateLimiter rateLimiter;

    protected int fetchCounter;

    public Responses(double fetchRate) {
        this.rateLimiter = RateLimiter.create(fetchRate);
        this.fetchCounter = 0;
    }

}
