package TEST_HARNESS;

import static TEST_HARNESS.Util.fetchProperty;
import static TEST_HARNESS.Util.getCommonFileNames;
import static TEST_HARNESS.Util.readGsonFile;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.json.simple.parser.ParseException;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.Data;

@Data
public class TemplateValidation {
    private Map<TemplateFAILURETYPE, ArrayList<Pair<String, Double>>> validationMap = new HashMap<>();

    public void validateAll() throws IOException, ParseException {
        List<String> FileNames = getCommonFileNames(Application.getCompareAgainst() == CompareAgainst.SECONDARY ? "SECONDARY_DIR" : "PRIMARY_DIR",
                "PRIMARY_DIR");
        for (String filename : FileNames) {
            System.out.println("Performing Template Validation for file: " + filename);
            JsonObject parserResponse = readGsonFile(fetchProperty("PRIMARY_DIR") + filename + ".json").getAsJsonObject();
            RBLPojo rblPojo = generateRBLPojoObject(parserResponse, filename);
            if (!rblPojoNullcheck(rblPojo)) {
                ArrayList<Pair<String, Double>> temp;
                if (validationMap.containsKey(TemplateFAILURETYPE.NULLValue)) {
                    temp = validationMap.get(TemplateFAILURETYPE.NULLValue);
                } else {
                    temp = new ArrayList<>();
                }
                temp.add(new ImmutablePair<>(filename, null));
                validationMap.put(TemplateFAILURETYPE.NULLValue, temp);
            } else {
                validateOverall(rblPojo);
                if (rblPojo.getDomestic_transactions() != null) {
                    validateDebits(rblPojo);
                    validateCredits(rblPojo);
                }
            }
        }
    }

    public void validateOverall(RBLPojo rblPojo) {
        Double diff = rblPojo.getTotal_amount_due() - (rblPojo.getPrevious_balance() - rblPojo.getLast_payment_received()) - rblPojo.getNew_debits();
        if (Math.abs(diff) > 5.0) {
            ArrayList<Pair<String, Double>> temp;
            if (validationMap.containsKey(TemplateFAILURETYPE.OVERALL)) {
                temp = validationMap.get(TemplateFAILURETYPE.OVERALL);
            } else {
                temp = new ArrayList<>();
            }
            temp.add(new ImmutablePair<>(rblPojo.getFileName(), diff));
            validationMap.put(TemplateFAILURETYPE.OVERALL, temp);
        }
    }

    public void validateDebits(RBLPojo rblPojo) {
        Double debits = 0.0;
        JsonArray domesticTransactions = rblPojo.getDomestic_transactions();
        for (JsonElement t : domesticTransactions) {
            Double tA = t.getAsJsonObject().get("txn_amount").getAsDouble();
            if (tA > 0) {
                debits += tA;
            }
        }
        Double diff = rblPojo.getNew_debits() - debits;
        if (Math.abs(diff) > 5.0) {
            ArrayList<Pair<String, Double>> temp;
            if (validationMap.containsKey(TemplateFAILURETYPE.DEBIT_TRANSACTIONS)) {
                temp = validationMap.get(TemplateFAILURETYPE.DEBIT_TRANSACTIONS);
            } else {
                temp = new ArrayList<>();
            }
            temp.add(new ImmutablePair<>(rblPojo.getFileName(), diff));
            validationMap.put(TemplateFAILURETYPE.DEBIT_TRANSACTIONS, temp);
        }
    }

    public void validateCredits(RBLPojo rblPojo) {
        Double credits = 0.0;
        JsonArray domesticTransactions = rblPojo.getDomestic_transactions();
        for (JsonElement t : domesticTransactions) {
            Double tA = t.getAsJsonObject().get("txn_amount").getAsDouble();
            if (tA < 0) {
                credits += Math.abs(tA);
            }
        }
        Double diff = rblPojo.getLast_payment_received() - credits;
        if (Math.abs(diff) > 5.0) {
            ArrayList<Pair<String, Double>> temp;
            if (validationMap.containsKey(TemplateFAILURETYPE.CREDIT_TRANSACTIONS)) {
                temp = validationMap.get(TemplateFAILURETYPE.CREDIT_TRANSACTIONS);
            } else {
                temp = new ArrayList<>();
            }
            temp.add(new ImmutablePair<>(rblPojo.getFileName(), diff));
            validationMap.put(TemplateFAILURETYPE.CREDIT_TRANSACTIONS, temp);
        }
    }

    private RBLPojo generateRBLPojoObject(JsonObject parserResponse, String fileName) {
        Double total_amt_due = JsonUtils
                .hasKeyWithNonNullValue(parserResponse, "transformed_data.statement_details.dues.total_amount_due") ? JsonUtils
                .getJsonElement(parserResponse, "transformed_data.statement_details.dues.total_amount_due").getAsDouble() : null;

        Double new_debits = JsonUtils
                .hasKeyWithNonNullValue(parserResponse, "transformed_data.statement_details.account_summary.new_debits") ? JsonUtils
                .getJsonElement(parserResponse, "transformed_data.statement_details.account_summary.new_debits").getAsDouble() : null;
        Double previous_balance = JsonUtils
                .hasKeyWithNonNullValue(parserResponse, "transformed_data.statement_details.account_summary.previous_balance") ? JsonUtils
                .getJsonElement(parserResponse, "transformed_data.statement_details.account_summary.previous_balance").getAsDouble() : null;

        Double last_payment_received = JsonUtils
                .hasKeyWithNonNullValue(parserResponse, "transformed_data.statement_details.account_summary.last_payment_received") ? JsonUtils
                .getJsonElement(parserResponse, "transformed_data.statement_details.account_summary.last_payment_received").getAsDouble() : null;
        JsonArray domestic_transactions = JsonUtils
                .hasKeyWithNonNullValue(parserResponse, "transformed_data.statement_details.transactions.domestic_transactions") ? JsonUtils
                .getJsonElement(parserResponse, "transformed_data.statement_details.transactions.domestic_transactions").getAsJsonArray() : null;
        return new RBLPojo(fileName, total_amt_due, new_debits, previous_balance, last_payment_received, domestic_transactions);
    }

    private boolean rblPojoNullcheck(RBLPojo rblPojoObj) {
        if (rblPojoObj.getTotal_amount_due() == null || rblPojoObj.getLast_payment_received() == null || rblPojoObj
                .getNew_debits() == null || rblPojoObj.getPrevious_balance() == null) {
            return false;
        } else {
            return true;
        }
    }
}
