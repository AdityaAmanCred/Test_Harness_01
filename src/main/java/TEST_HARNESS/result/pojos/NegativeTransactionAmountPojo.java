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
public class NegativeTransactionAmountPojo {
    @SerializedName ("File_Name")
    @Expose
    private String fileName;

    @SerializedName ("Transactions")
    @Expose
    private List<DomesticTransaction> transactions;

    public int getTransactionsSize() {
        return this.transactions.size();
}
}
