package TEST_HARNESS;

import static TEST_HARNESS.Util.getPropertyFromFile;
import static TEST_HARNESS.Util.readCSVLineByLine;
import static TEST_HARNESS.Util.setParameters;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.json.simple.JSONArray;
import org.json.simple.parser.ParseException;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Application {
    private static List<String> fileNames = new ArrayList<>();

    private static CompareAgainst compareAgainst;

    private static ParserName parserName;

    public static void setFileNames(List<String> fileNames) {
        Application.fileNames = fileNames;
    }

    public static void setCompareAgainst(CompareAgainst compareAgainst) {
        Application.compareAgainst = compareAgainst;
    }

    public static void setParserName(ParserName parserName) {
        Application.parserName = parserName;
    }

    public static List<String> getFileNames() {
        return fileNames;
    }

    public static CompareAgainst getCompareAgainst() {
        return compareAgainst;
    }

    public static ParserName getParserName() {
        return parserName;
    }

    public static void main(String[] args) throws IOException, ParseException {
        //        //Download the PDF
        //        DownloadFiles downloadFiles = new DownloadFiles(5.0);
        //        downloadFiles.downloadPDFs();
        //
        //        //Set ParserName and Comparison Mode
        //        setParameters();
        //
        //        ////Fetch Responses
        //        FetchResponses fetchResponses = new FetchResponses(getPropertyFromFile("application.properties").getProperty("STAGE_ID"),
        //                getPropertyFromFile("application.properties").getProperty("PROD_ID"),
        //
        //                getPropertyFromFile("application.properties").getProperty("PDF_DOWNLOAD_LOC"), 5);
        //        fetchResponses.fetch();
        //
        //        //Running Comparator
        //        Comparator comparator = new Comparator();
        //        comparator.compareAll();
        //        comparator.nullCheck();
        //
        //        //Generate Results
        //        GenerateResults generateResults = new GenerateResults();
        //        generateResults.generate(comparator);
        FetchMorningStar fetchMorningStar = new FetchMorningStar(
                readCSVLineByLine(getPropertyFromFile("application.properties").getProperty("ISIN_CSV")));
        //fetchMorningStar.fetchAllOperationsData(Environment.STAGE);
        //fetchMorningStar.fetchAllPerfomanceData(Environment.STAGE);
        fetchMorningStar.fetchAllRatingsData(Environment.STAGE);
        //fetchMorningStar.fetchAllPortfolioData(Environment.STAGE);
        //        Comparator comparator = new Comparator();
        //        comparator.compareAll();
        //        comparator.nullCheck();
        //        GenerateResults generateResults = new GenerateResults();
        //        generateResults.generate(comparator);
    }

}
