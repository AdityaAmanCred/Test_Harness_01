package TEST_HARNESS.result.pojos;

/**
 *
 */
import java.util.Date;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class DomesticTransaction {
    @SerializedName ("txn_date")
    @Expose
    private Date txnDate;

    @SerializedName ("txn_type")
    @Expose
    private String txnType;

    @SerializedName ("txn_description")
    @Expose
    private String txnDescription;

    @SerializedName ("billed_date")
    @Expose
    private Date billedDate;

    @SerializedName ("txn_amount")
    @Expose
    private Double txnAmount;

}