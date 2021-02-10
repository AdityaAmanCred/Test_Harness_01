package TEST_HARNESS.comparator;

import static TEST_HARNESS.utils.Util.fetchProperty;
import static TEST_HARNESS.utils.Util.getCommonFileNames;
import static TEST_HARNESS.utils.Util.readGsonFile;
import static TEST_HARNESS.utils.Util.writeTofile;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.json.simple.parser.ParseException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import TEST_HARNESS.config.CompareAgainst;
import TEST_HARNESS.config.Config;
import TEST_HARNESS.result.pojos.DomesticTransaction;
import TEST_HARNESS.result.pojos.NegativeTransactionAmountPojo;
import TEST_HARNESS.result.pojos.TransactionsRelativeComplimentPojo;
import TEST_HARNESS.result.pojos.TransactionAnalysisResults;
import TEST_HARNESS.result.pojos.GenericResultPojo;
import TEST_HARNESS.utils.JsonUtils;
import TEST_HARNESS.utils.Util;

/**
 * @author Prithvi Patil
 * @version 1.0
 */
public class TransactionsInspector {
    private TransactionAnalysisResults transactionAnalysisResults;

    private GenericResultPojo negativeTransactionValidationResult;

    private List<String> commonFileNames = getCommonFileNames(
            Config.getCompareAgainst() == CompareAgainst.SECONDARY ? "SECONDARY_DIR" : "PRIMARY_DIR", "PRIMARY_DIR");

    private void inspectParserDifferences() throws IOException, ParseException, java.text.ParseException {
        System.out.println("Inspecting differences between the two Parsers in domestic-transactions");
        GenericResultPojo primaryParserTransactionCaptureFailure = new GenericResultPojo();
        GenericResultPojo secondaryParserTransactionCaptureFailure = new GenericResultPojo();
        GenericResultPojo otherGenericResultPojo = new GenericResultPojo();

        for (String fileName : this.commonFileNames) {
            System.out.println("Inspecting transactions for file: " + fileName);
            JsonObject primaryParserJson = readGsonFile(fetchProperty("PRIMARY_DIR") + fileName + ".json").getAsJsonObject();
            JsonObject secondatyParserJson = readGsonFile(fetchProperty("SECONDARY_DIR") + fileName + ".json").getAsJsonObject();
            List<DomesticTransaction> pDomesticTransactions = getDomesticTransactions(primaryParserJson, fileName);
            List<DomesticTransaction> sDomesticTransactions = getDomesticTransactions(secondatyParserJson, fileName);
            List<DomesticTransaction> secondaryMinusPrimary = new ArrayList<>(sDomesticTransactions);
            List<DomesticTransaction> primaryMinusSecondary = new ArrayList<>(pDomesticTransactions);
            secondaryMinusPrimary.removeAll(pDomesticTransactions);
            primaryMinusSecondary.removeAll(sDomesticTransactions);
            JsonArray pMSJsonArray = (new Gson().toJsonTree(primaryMinusSecondary, new TypeToken<List<DomesticTransaction>>() {}.getType()))
                    .getAsJsonArray();
            JsonArray sMPJsonArray = (new Gson().toJsonTree(secondaryMinusPrimary, new TypeToken<List<DomesticTransaction>>() {}.getType()))
                    .getAsJsonArray();
            TransactionsRelativeComplimentPojo transactionsRelativeComplimentPojo = new TransactionsRelativeComplimentPojo(fileName, pMSJsonArray,
                    sMPJsonArray);
            if (pMSJsonArray.size() > 0 && sMPJsonArray.size() == 0) {
                secondaryParserTransactionCaptureFailure.addToResultsList(transactionsRelativeComplimentPojo);
            } else if (sMPJsonArray.size() > 0 && pMSJsonArray.size() == 0) {
                primaryParserTransactionCaptureFailure.addToResultsList(transactionsRelativeComplimentPojo);
            } else if (sMPJsonArray.size() != 0 && pMSJsonArray.size() != 0) {
                otherGenericResultPojo.addToResultsList(transactionsRelativeComplimentPojo);
            }
        }
        primaryParserTransactionCaptureFailure.calculatePercentage(commonFileNames.size());
        secondaryParserTransactionCaptureFailure.calculatePercentage(commonFileNames.size());
        otherGenericResultPojo.calculatePercentage(commonFileNames.size());
        this.transactionAnalysisResults = new TransactionAnalysisResults(primaryParserTransactionCaptureFailure,
                secondaryParserTransactionCaptureFailure, otherGenericResultPojo);
        generateTransactionAnalysisResults("TransactionsInspectionResults", this.transactionAnalysisResults);
    }

    private List<DomesticTransaction> getDomesticTransactions(JsonObject parserResponse, String fileName) throws java.text.ParseException {
        List<DomesticTransaction> domesticTransactionsList = new ArrayList<>();
        JsonArray domesticTransactionsJsonArray = JsonUtils.hasKeyWithNonNullValue(parserResponse,
                Util.getMainJsonFieldName() + ".statement_details.transactions.domestic_transactions") ? JsonUtils
                .getJsonElement(parserResponse, Util.getMainJsonFieldName() + ".statement_details.transactions.domestic_transactions")
                .getAsJsonArray() : null;
        if (domesticTransactionsJsonArray != null) {
            for (JsonElement dT : domesticTransactionsJsonArray) {
                String txnDescStr = JsonUtils.hasKeyWithNonNullValue(dT.getAsJsonObject(), "txn_description") ? JsonUtils
                        .getJsonElement(dT.getAsJsonObject(), "txn_description").getAsString() : null;
                String txnTypeStr = JsonUtils.hasKeyWithNonNullValue(dT.getAsJsonObject(), "txn_type") ? JsonUtils
                        .getJsonElement(dT.getAsJsonObject(), "txn_type").getAsString() : null;
                String txnDateStr = JsonUtils.hasKeyWithNonNullValue(dT.getAsJsonObject(), "txn_date") ? JsonUtils
                        .getJsonElement(dT.getAsJsonObject(), "txn_date").getAsString() : null;
                String txnBilledDateStr = JsonUtils.hasKeyWithNonNullValue(dT.getAsJsonObject(), "billed_date") ? JsonUtils
                        .getJsonElement(dT.getAsJsonObject(), "billed_date").getAsString() : null;
                Double txnAmount = JsonUtils.hasKeyWithNonNullValue(dT.getAsJsonObject(), "txn_amount") ? JsonUtils
                        .getJsonElement(dT.getAsJsonObject(), "txn_amount").getAsDouble() : null;
                Date txnDate = DomesticTransaction.parseDate("yyyy-MM-dd'T'HH:mm:ss'Z'", txnDateStr);
                Date billedDate = DomesticTransaction.parseDate("yyyy-MM-dd'T'HH:mm:ss'Z'", txnBilledDateStr);
                DomesticTransaction domesticTransaction = new DomesticTransaction(txnDate, txnTypeStr, txnDescStr, billedDate, txnAmount);
                domesticTransactionsList.add(domesticTransaction);
            }
            return domesticTransactionsList;
        }
        return null;
    }

    private void generateTransactionAnalysisResults(String fileName, Object obj) {
        try {
            writeTofile(fileName, new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create().toJson(obj));
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println(String.format("Generated result file: %s.json", fileName));

    }

    private void searchForNegativeTransactionsAllFiles() throws ParseException, java.text.ParseException, IOException {
        System.out.println("Searching for negative transaction amount in all files");
        this.negativeTransactionValidationResult = new GenericResultPojo();
        Set<String> primaryResponsesFileNames = Util.getNames("PRIMARY_DIR");
        Iterator<String> fileNamesiterator = primaryResponsesFileNames.iterator();
        while (fileNamesiterator.hasNext()) {
            String fileName = fileNamesiterator.next();
            NegativeTransactionAmountPojo negativeTransactionAmountPojo = new NegativeTransactionAmountPojo();
            negativeTransactionAmountPojo.setFileName(fileName);
            negativeTransactionAmountPojo.setTransactions(searchNegativeTransactionsForFile(fileName));
            if (negativeTransactionAmountPojo.getTransactionsSize() > 0) {
                this.negativeTransactionValidationResult.addToResultsList(negativeTransactionAmountPojo);
            }
        }
        this.negativeTransactionValidationResult.calculatePercentage(primaryResponsesFileNames.size());
        generateTransactionAnalysisResults("NegativeTransactions", this.negativeTransactionValidationResult);
    }

    private List<DomesticTransaction> searchNegativeTransactionsForFile(String fileName)
            throws IOException, ParseException, java.text.ParseException {
        System.out.println("Searching for negative transaction amount for file: " + fileName);
        List<DomesticTransaction> negativeTransactions = new ArrayList<>();
        JsonObject primaryParserJson = readGsonFile(fetchProperty("PRIMARY_DIR") + fileName + ".json").getAsJsonObject();
        List<DomesticTransaction> allDomesticTransactions = getDomesticTransactions(primaryParserJson, fileName);
        for (DomesticTransaction t : allDomesticTransactions) {
            if (t.getTxnAmount() < 0) {
                negativeTransactions.add(t);
            }
        }
        return negativeTransactions;
    }

    public void inspectTransactions() throws ParseException, java.text.ParseException, IOException {
        inspectParserDifferences();
        searchForNegativeTransactionsAllFiles();
    }
}
