package TEST_HARNESS.result.pojos;

import com.google.gson.JsonArray;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author Prithvi Patil
 * @version 1.0
 */
@Data
@AllArgsConstructor
public class TransactionsRelativeComplimentPojo extends Object {
    @SerializedName ("File_Name")
    @Expose
    private String fileName;

    @SerializedName ("Primary_Minus_Secondary_Transactions")
    @Expose
    private JsonArray primaryMinusSecondaryTransactions;

    @SerializedName ("Secondary_Minus_Primary_Transactions")
    @Expose
    private JsonArray secondaryMinusPrimaryTransactions;

}