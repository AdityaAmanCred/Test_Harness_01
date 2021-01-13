package TEST_HARNESS;

import static TEST_HARNESS.utils.Util.setParserNames;
import java.io.IOException;
import org.json.simple.parser.ParseException;
import TEST_HARNESS.comparator.Comparator;
import TEST_HARNESS.download.DownloadExecutor;
import TEST_HARNESS.download.DownloaderName;
import TEST_HARNESS.parse.ParsePdf;
import TEST_HARNESS.result.GenerateResults;

public class Application {
    private static Thread downloadThread;

    private static Thread parsingThread;

    private static void initializeDownloadThread() {
        downloadThread = new Thread(new DownloadExecutor(DownloaderName.PORTKEY));
    }

    private static void initializeparsingThread() {
        parsingThread = new Thread(new ParsePdf());
    }

    static {
        setParserNames();
        initializeDownloadThread();
        initializeparsingThread();
    }

    private static void waitForDownloadAndParsingThreadsToTerminate() {
        try {
            downloadThread.join();
            parsingThread.join();
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    public static void main(String[] args) throws IOException, ParseException, InterruptedException {
        //Download the PDFs
        downloadThread.start();

        //Fetch Transformed JSONs from Parsers
        parsingThread.start();

        //Running Comparator
        waitForDownloadAndParsingThreadsToTerminate();
        Comparator comparator = new Comparator();
        comparator.compareAll();

        //Generate Results
        GenerateResults generateResults = new GenerateResults();
        generateResults.generate(comparator);
    }
}
