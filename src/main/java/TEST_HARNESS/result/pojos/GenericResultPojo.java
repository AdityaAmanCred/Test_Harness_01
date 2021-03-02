package TEST_HARNESS.result.pojos;

import java.util.ArrayList;
import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import lombok.Setter;

/**
 * @author Prithvi Patil
 * @version 1.0
 */
@Setter
public class GenericResultPojo<T> {
    @SerializedName ("Percentage")
    @Expose
    private Double percentage;

    @SerializedName ("ResultArray")
    @Expose
    private List<T> results;

    public void addToResultsList(T t) {
        this.results.add(t);
    }

    public void calculatePercentage(int totalNumberOfFiles) {
        this.percentage = (results.size() * 100.00) / totalNumberOfFiles;
    }

    public GenericResultPojo() {
        this.percentage = null;
        this.results = new ArrayList<>();
    }

    public boolean isEmpty() {
        return this.results.isEmpty();
    }
}
