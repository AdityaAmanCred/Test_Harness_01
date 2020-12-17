package TEST_HARNESS;

import static TEST_HARNESS.Util.getNames;
import static TEST_HARNESS.Util.isValidParserSelection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import com.google.common.util.concurrent.RateLimiter;

/**
 * @author Prithvi Patil
 * @version 1.0
 */
public class ParsingExecutor {
    protected Set<String> fileNames;

    public static Set<String> fetchedFileNamesPrimaryParser;

    public static Set<String> fetchedFileNamesSecondaryParser;

    private RateLimiter primaryParserRateLimiter;

    private RateLimiter secondaryParserRateLimiter;

    private ExecutorService primaryParsingExecutor;

    private ExecutorService secondaryParsingExecutor;

    private int primaryParserFetchCounter;

    private int secondaryParserFetchCounter;

    private int primaryParserRetryAttemptsLeft;

    private int secondaryParserRetryAttemptsLeft;

    final protected int maxretryAttempts = 3;

    public ParsingExecutor(int numberOfPrimaryParsingThreads, int numberOfSecondaryParsingThreads, int primaryParserFetchRate,
            int secondaryParserFetchRate) {
        this.primaryParsingExecutor = Executors.newFixedThreadPool(numberOfPrimaryParsingThreads);
        if (Application.getCompareAgainst() == CompareAgainst.SECONDARY) {
            this.secondaryParsingExecutor = Executors.newFixedThreadPool(numberOfSecondaryParsingThreads);
        }
        this.primaryParserRateLimiter = RateLimiter.create(primaryParserFetchRate);
        this.secondaryParserRateLimiter = RateLimiter.create(secondaryParserFetchRate);
        this.fileNames = getNames("PDF_DOWNLOAD_LOC").stream().collect(Collectors.toSet());
        fetchedFileNamesPrimaryParser = new HashSet<>();
        fetchedFileNamesSecondaryParser = new HashSet<>();
        primaryParserFetchCounter = 0;
        secondaryParserFetchCounter = 0;
        primaryParserRetryAttemptsLeft = maxretryAttempts;
        secondaryParserRetryAttemptsLeft = maxretryAttempts;
    }

    public void fetchAllResponses() {
        fetchedFileNamesPrimaryParser = new HashSet<>(getNames("PRIMARY_DIR"));
        fetchedFileNamesSecondaryParser = new HashSet<>(getNames("SECONDARY_DIR"));
        this.primaryParserFetchCounter = fetchedFileNamesPrimaryParser.size();
        this.secondaryParserFetchCounter = fetchedFileNamesPrimaryParser.size();
        if (fileNames.equals(fetchedFileNamesPrimaryParser)) {
            System.out.println(String.format("All PDFs files already transformed for %s", Application.getPrimaryParserName()));
        }
        while (this.primaryParserRetryAttemptsLeft-- > 0) {
            if (this.primaryParserRetryAttemptsLeft != this.maxretryAttempts - 1) {
                System.out.println(String.format("Retrying to fetch %s responses for failed PDFs", Application.getPrimaryParserName()));
            }
            Iterator<String> iterator = fileNames.iterator();
            while (iterator.hasNext()) {
                primaryParserRateLimiter.acquire();
                String fileName = iterator.next();
                if (!this.fetchedFileNamesPrimaryParser.contains(fileName)) {
                    primaryParsingExecutor.submit(new Bumblebee(fileName + ".pdf", Application.getPrimaryParserEnv()));

                } else {
                    System.out
                            .println(String.format("%s Parser response for file " + fileName + " already fetched", this.getParserType().toString()));
                }
                if (Application.getCompareAgainst() == CompareAgainst.SECONDARY) {
                    if (!this.fetchedFileNamesSecondaryParser.contains(fileName)) {
                        secondaryParsingExecutor.submit(new Bumblebee(fileName + ".pdf", Application.getSecondaryParserEnv()));
                    } else {
                        System.out.println(
                                String.format("%s Parser response for file " + fileName + " already fetched", this.getParserType().toString()));
                    }
                }

            }
        }
    }

}
