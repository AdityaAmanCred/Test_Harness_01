package TEST_HARNESS.result.pojos;

/**
 *
 */
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.google.gson.annotations.Expose;
import lombok.AllArgsConstructor;

@JsonInclude (JsonInclude.Include.NON_NULL)
@JsonPropertyOrder ({ "txn_date", "txn_type", "txn_description", "billed_date", "txn_amount" })
@AllArgsConstructor
public class DomesticTransaction {
    @JsonProperty ("txn_date")
    @Expose
    private Date txnDate;

    @JsonProperty ("txn_type")

    @Expose
    private String txnType;

    @JsonProperty ("txn_description")

    @Expose
    private String txnDescription;

    @JsonProperty ("billed_date")

    @Expose
    private Date billedDate;

    @JsonProperty ("txn_amount")

    @Expose
    private Double txnAmount;

    @JsonProperty ("txn_date")
    public Date getTxnDate() {
        return txnDate;
    }

    @JsonProperty ("txn_date")
    public void setTxnDate(Date txnDate) {
        this.txnDate = txnDate;
    }

    @JsonProperty ("txn_type")
    public String getTxnType() {
        return txnType;
    }

    @JsonProperty ("txn_type")
    public void setTxnType(String txnType) {
        this.txnType = txnType;
    }

    @JsonProperty ("txn_description")
    public String getTxnDescription() {
        return txnDescription;
    }

    @JsonProperty ("txn_description")
    public void setTxnDescription(String txnDescription) {
        this.txnDescription = txnDescription;
    }

    @JsonProperty ("billed_date")
    public Date getBilledDate() {
        return billedDate;
    }

    @JsonProperty ("billed_date")
    public void setBilledDate(Date billedDate) {
        this.billedDate = billedDate;
    }

    @JsonProperty ("txn_amount")
    public Double getTxnAmount() {
        return txnAmount;
    }

    @JsonProperty ("txn_amount")
    public void setTxnAmount(Double txnAmount) {
        this.txnAmount = txnAmount;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(DomesticTransaction.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("txnDate");
        sb.append('=');
        sb.append(((this.txnDate == null) ? "<null>" : this.txnDate));
        sb.append(',');
        sb.append("txnType");
        sb.append('=');
        sb.append(((this.txnType == null) ? "<null>" : this.txnType));
        sb.append(',');
        sb.append("txnDescription");
        sb.append('=');
        sb.append(((this.txnDescription == null) ? "<null>" : this.txnDescription));
        sb.append(',');
        sb.append("billedDate");
        sb.append('=');
        sb.append(((this.billedDate == null) ? "<null>" : this.billedDate));
        sb.append(',');
        sb.append("txnAmount");
        sb.append('=');
        sb.append(((this.txnAmount == null) ? "<null>" : this.txnAmount));
        sb.append(',');
        sb.append("additionalProperties");
        sb.append('=');
        sb.append(',');
        if (sb.charAt((sb.length() - 1)) == ',') {
            sb.setCharAt((sb.length() - 1), ']');
        } else {
            sb.append(']');
        }
        return sb.toString();
    }

    @Override
    public int hashCode() {
        int result = 1;
        result = ((result * 31) + ((this.txnType == null) ? 0 : this.txnType.hashCode()));
        result = ((result * 31) + ((this.txnDescription == null) ? 0 : this.txnDescription.hashCode()));
        result = ((result * 31) + ((this.billedDate == null) ? 0 : this.billedDate.hashCode()));
        result = ((result * 31) + ((this.txnDate == null) ? 0 : this.txnDate.hashCode()));
        result = ((result * 31) + ((this.txnAmount == null) ? 0 : this.txnAmount.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof DomesticTransaction) == false) {
            return false;
        }
        DomesticTransaction rhs = ((DomesticTransaction) other);
        return (((((((this.txnType == rhs.txnType) || ((this.txnType != null) && this.txnType
                .equals(rhs.txnType))) && ((this.txnDescription == rhs.txnDescription) || ((this.txnDescription != null) && this.txnDescription
                .equals(rhs.txnDescription)))) && ((this.billedDate == rhs.billedDate) || ((this.billedDate != null) && this.billedDate
                .equals(rhs.billedDate)))) && ((this.txnDate == rhs.txnDate) || ((this.txnDate != null) && this.txnDate
                .equals(rhs.txnDate)))) && ((this.txnAmount == rhs.txnAmount) || ((this.txnAmount != null) && this.txnAmount
                .equals(rhs.txnAmount)))));
    }

    public static Date parseDate(String pattern, String dateString) throws ParseException {
        DateFormat dateformat = new SimpleDateFormat(pattern);
        Date date = dateformat.parse(dateString);
        return date;
    }

}