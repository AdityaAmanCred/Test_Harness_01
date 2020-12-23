package TEST_HARNESS.parse;

import static TEST_HARNESS.utils.Util.getNames;
import static TEST_HARNESS.utils.Util.getParserNameForParserType;
import static TEST_HARNESS.utils.Util.presentInDIROneAndNotInDirTwo;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import com.google.common.util.concurrent.RateLimiter;
import TEST_HARNESS.config.Config;

/**
 * @author Prithvi Patil
 * @version 1.0
 */
public class ParsingExecutor implements Runnable {
    private int numberOfThreads;

    private RateLimiter rateLimiter;

    private BlockingQueue<String> blockingQueue;

    private Set<String> fetchedFileNames;

    public Integer fetchCounter;

    private int retryAttemptsLeft;

    private int maxretryAttempts = 3;

    private ExecutorService parsingThreadPool;

    private ParserType parserType;

    public ParsingExecutor(int numberOfThreads, double rateLimit, BlockingQueue<String> blockingQueue, ParserType parserType) {
        this.numberOfThreads = numberOfThreads;
        this.rateLimiter = RateLimiter.create(rateLimit);
        this.blockingQueue = blockingQueue;
        this.retryAttemptsLeft = maxretryAttempts;
        this.parserType = parserType;
        fetchedFileNames = new HashSet<>(getNames(this.parserType == ParserType.PRIMARY ? "PRIMARY_DIR" : "SECONDARY_DIR"));
        this.fetchCounter = fetchedFileNames.size();
        parsingThreadPool = Executors.newFixedThreadPool(numberOfThreads);
        loadBlockingQueue();
    }

    private void parsePdfs() throws InterruptedException {
        while (true) {
            if (Config.IsDownloadThreadIsTerminated() == true && blockingQueue.isEmpty()) {
                break;
            }
            String fileName = blockingQueue.take();
            parsingThreadPool.submit(getParser(parserType, fileName + ".pdf"));
        }
    }

    public Parser getParser(ParserType parserType, String fileName) {
        Parser parser;
        ParserName parserName = getParserNameForParserType(parserType);
        if (parserName == ParserName.BUMBLEBEE) {
            parser = getBumbleBee(parserType, fileName);
        } else if (parserName == ParserName.OPTIMUS) {
            parser = getOptimus(parserType, fileName);
        } else {
            parser = getPandorastreet(parserType, fileName);
        }
        return parser;
    }

    private Parser getBumbleBee(ParserType parserType, String fileName) {
        return new Bumblebee(parserType, fileName);
    }

    private Parser getOptimus(ParserType parserType, String fileName) {
        return new Optimus(parserType, fileName);
    }

    private Parser getPandorastreet(ParserType parserType, String fileName) {
        return new Pandorastreet(parserType, fileName);
    }

    private void loadBlockingQueue() {
        Set<String> fileNames = presentInDIROneAndNotInDirTwo("PDF_DOWNLOAD_LOC", parserType == ParserType.PRIMARY ? "PRIMARY_DIR" : "SECONDARY_DIR");
        blockingQueue.addAll(fileNames);
    }

    public void run() {
        try {
            parsePdfs();
            parsingThreadPool.shutdown();
            parsingThreadPool.awaitTermination(1, TimeUnit.DAYS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

