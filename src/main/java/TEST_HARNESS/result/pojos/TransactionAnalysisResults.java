package TEST_HARNESS.result.pojos;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;

/**
 * @author Prithvi Patil
 * @version 1.0
 */
@AllArgsConstructor
public class TransactionAnalysisResults {
    @SerializedName ("Primary_Parser_Transaction_Capture_Failures")
    @Expose
    private final GenericResultPojo primaryParserTransactionCaptureFailure;

    @SerializedName ("Secondary_Parser_Transaction_Capture_Failures")
    @Expose
    private final GenericResultPojo secondaryParserTransactionCaptureFailure;

    @SerializedName ("Other_Transaction_Differences")
    @Expose
    private final GenericResultPojo otherGenericResultPojo;
}
