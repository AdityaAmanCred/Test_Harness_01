package TEST_HARNESS.result.pojos;

import org.json.simple.JSONArray;
import com.google.gson.JsonArray;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RBLPojo {
    private String fileName;

    private Double total_amount_due;

    private Double new_debits;

    private Double previous_balance;

    private Double last_payment_received;

    private JsonArray domestic_transactions;
}
