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

    private static ParserName primaryParserName;

    private static ParserName secondaryParserName;

    private static Environment primaryParserEnv;

    private static Environment secondaryParserEnv;

    public static Environment getPrimaryParserEnv() {
        return primaryParserEnv;
    }

    public static Environment getSecondaryParserEnv() {
        return secondaryParserEnv;
    }

    public static void setPrimaryParserEnv(Environment primaryParserEnv) {
        Application.primaryParserEnv = primaryParserEnv;
    }

    public static void setSecondaryParserEnv(Environment secondaryParserEnv) {
        Application.secondaryParserEnv = secondaryParserEnv;
    }

    public static void setCompareAgainst(CompareAgainst compareAgainst) {
        Application.compareAgainst = compareAgainst;
    }

    public static CompareAgainst getCompareAgainst() {
        return compareAgainst;
    }

    public static ParserName getPrimaryParserName() {
        return primaryParserName;
    }

    public static void setPrimaryParserName(ParserName parserName) {
        Application.primaryParserName = parserName;
    }

    public static ParserName getSecondaryParserName() {
        return secondaryParserName;
    }

    public static void setSecondaryParserName(ParserName parserName) {
        Application.secondaryParserName = parserName;
    }

    public static void main(String[] args) throws IOException, ParseException {
        //Downloader the PDF
        Downloader downloader = new PortkeyDownloader(50.0);
        downloader.downloadPDFs();

        //Fetch Responses
        // For fetching all Parser's data(Optimus,Bumblebee, Pandora)
        fetchParserResponses();

        //For fetching MorningStar data
        //        MorningStar morningStar = new MorningStar(20.0);
        //        morningStar.setParserType(Responses.ParserType.PRIMARY);
        //        morningStar.fetchAllOperationsData(Environment.STAGE);

        //Running Comparator
        Comparator comparator = new Comparator();
        comparator.compareAll();

        //Generate Results
        GenerateResults generateResults = new GenerateResults();
        generateResults.generate(comparator);
    }
}
