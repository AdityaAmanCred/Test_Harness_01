package TEST_HARNESS;

import static TEST_HARNESS.Util.getNames;
import static TEST_HARNESS.Util.getPropertyFromFile;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import org.json.simple.JSONArray;
import org.json.simple.parser.ParseException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Application {
    public static List<String> fileNames = new ArrayList<>();

    private static final ObjectMapper mapper = new ObjectMapper();

    private static CompareAgainst compareAgainst;

    private static ParserName parserName;

    public static void main(String[] args) throws IOException, ParseException {

        //Comment out these 3 lines if you don't have PCI access to download from Scraper.
        DownloadFiles downloadFiles = new DownloadFiles(5.0);
        downloadFiles.setFileIds();
        downloadFiles.downloadPDFs();

        //Fetches fileNames from PDF_DOWNLOAD_LOC
        fileNames = getNames("PDF_DOWNLOAD_LOC");

        if (getPropertyFromFile("application.properties").getProperty("PARSER_NAME").equals("PANDORA")) {
            parserName = ParserName.PANDORASTREET;
        } else {
            parserName = ParserName.BUMBLEBEE;
        }
        if (getPropertyFromFile("application.properties").getProperty("COMPARISON_MODE").equals("PROD")) {
            compareAgainst = CompareAgainst.PROD;
        } else {
            compareAgainst = CompareAgainst.MANUAL;
        }

        // Fetch Responses
        FetchResponses fetchResponses = new FetchResponses(getPropertyFromFile("application.properties").getProperty("STAGE_ID"),
                getPropertyFromFile("application.properties").getProperty("PROD_ID"),

                getPropertyFromFile("application.properties").getProperty("PDF_DOWNLOAD_LOC"), 5);
        if (parserName == ParserName.BUMBLEBEE) {
            fetchResponses.fetchBumblebeeResponses(Environment.STAGE);
        } else {
            fetchResponses.fetchPandoraResponses(Environment.STAGE);
        }

        if (compareAgainst == CompareAgainst.PROD) {
            if (parserName == ParserName.BUMBLEBEE) {
                fetchResponses.fetchBumblebeeResponses(Environment.PROD);
            } else {
                fetchResponses.fetchPandoraResponses(Environment.PROD);
            }
        } else {
            CreateSkeletalJsons createSkeletalJsons = new CreateSkeletalJsons();
            createSkeletalJsons.createJsonFiles();
            Scanner sc = new Scanner(System.in);
            int userInput = 0;
            while (userInput != 1) {
                System.out.println(
                        "Make manual changes to skeletal json files in 'ExpectedResponses' directory.To proceed to perform comparison press 1. \n");
                userInput = sc.nextInt();
            }
        }

        //Running Comparator
        Comparator comparator = new Comparator();
        JSONArray results = comparator.compareAll();
        ComparisonStats comparisonStats = new ComparisonStats(results);
        comparisonStats.GenerateStats();
        JSONArray fieldResults = comparator.generateFieldWiseResults();
        FieldWiseResults fieldWiseResults = new FieldWiseResults(fieldResults);
        FileWiseResults fileWiseResults = new FileWiseResults(comparisonStats.getDiffCount(), comparisonStats.getIdenticalCount(),
                comparisonStats.getLeftOnlyCount(), comparisonStats.getRightOnlyCount(), comparisonStats.getTotalFileCount(), results);
        GenerateResults generateResults = new GenerateResults();
        generateResults.writeTofile("field_wise", mapper.writeValueAsString(fieldWiseResults));
        generateResults.writeTofile("file_wise", mapper.writeValueAsString(fileWiseResults));
    }

    enum CompareAgainst {
        PROD,
        MANUAL
    }

    enum ParserName {
        BUMBLEBEE,
        PANDORASTREET
    }
}
