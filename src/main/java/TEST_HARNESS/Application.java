package TEST_HARNESS;

import static TEST_HARNESS.Util.setParserNames;
import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
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

    public static CountDownLatch pdfCountDownLatch;

    public static BlockingQueue<String> primaryParserBlockingQueue = new ArrayBlockingQueue(100);

    public static BlockingQueue<String> secondaryParserBlockingQueue = new ArrayBlockingQueue(100);

    public static boolean downLoadThreadIsTerminated = false;

    public static CountDownLatch countDownLatch;

    public static void main(String[] args) throws IOException, ParseException, InterruptedException {
        //Download the PDFs
        setParserNames();
        Thread downloadThread = new Thread(new DownloadExecutor(DownloaderName.PORTKEY));
        downloadThread.start();

        //Fetch Responses
        Thread parsingThread = new Thread(new ParsePdf());
        parsingThread.start();

        //Running Comparator
        downloadThread.join();
        parsingThread.join();
        Comparator comparator = new Comparator();
        comparator.compareAll();

        //Generate Results
        GenerateResults generateResults = new GenerateResults();
        generateResults.generate(comparator);
    }
}
