package TEST_HARNESS;

import static TEST_HARNESS.Util.fetchParserResponses;
import java.io.IOException;
import org.json.simple.parser.ParseException;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Application {
    private static CompareAgainst compareAgainst;

    private static ParserName stageParserName;

    private static ParserName prodParserName;

    public static void setCompareAgainst(CompareAgainst compareAgainst) {
        Application.compareAgainst = compareAgainst;
    }

    public static CompareAgainst getCompareAgainst() {
        return compareAgainst;
    }

    public static ParserName getStageParserName() {
        return stageParserName;
    }

    public static void setStageParserName(ParserName parserName) {
        Application.stageParserName = parserName;
    }

    public static ParserName getProdParserName() {
        return prodParserName;
    }

    public static void setProdParserName(ParserName parserName) {
        Application.prodParserName = parserName;
    }

    public static void main(String[] args) throws IOException, ParseException {
        //Download the PDF
        DownloadFiles downloadFiles = new DownloadFiles(10.0);
        downloadFiles.downloadPDFs();

        ////Fetch Responses
        //For fetching all Parser's data(Optimus,Bumblebee, Pandora)
        fetchParserResponses();
        //For fetching MorningStar data
        //        MorningStar morningStar = new MorningStar(20.0);
        //        morningStar.fetchAllPortfolioData(Environment.STAGE);

        //Running Comparator
        Comparator comparator = new Comparator();
        comparator.compareAll();

        //Generate Results
        GenerateResults generateResults = new GenerateResults();
        generateResults.generate(comparator);
    }
}
