package TEST_HARNESS;

import static TEST_HARNESS.Util.fetchProperty;
import static TEST_HARNESS.Util.getNames;
import static TEST_HARNESS.Util.readCSVLineByLine;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import com.google.common.util.concurrent.RateLimiter;

/**
 * @author Prithvi Patil
 * @version 1.0
 */
public class DownloadExecutor {
    private int numberOfDownloadThreads;

    private BlockingQueue<String> blockingQueue;

    private ExecutorService downloadExecutor;

    public static int downloadcount;

    private Set<List<String>> csvRows = new HashSet<>();

    public static Set<String> downloadedFiles = new HashSet<>();

    private RateLimiter rateLimiter;

    private int retryAttemptsLeft;

    final private int maxRetryAttempts = 3;

    public DownloadExecutor(int numberOfDownloadThreads, BlockingQueue<String> blockingQueue, int rate) {
        this.numberOfDownloadThreads = numberOfDownloadThreads;
        this.blockingQueue = blockingQueue;
        downloadExecutor = Executors.newFixedThreadPool(numberOfDownloadThreads);
        this.setFileIds();
        this.downloadcount = 0;
        this.rateLimiter = RateLimiter.create(rate);
        this.retryAttemptsLeft = maxRetryAttempts;
        if (getNames("PDF_DOWNLOAD_LOC").size() > 0) {
            this.downloadedFiles = getNames("PDF_DOWNLOAD_LOC");
            this.downloadcount = downloadedFiles.size();
        }
    }

    public void downloadPDFs() {
        resetCSVRows();
        if (this.csvRows.size() == 0) {
            System.out.println("All PDFs already downloaded");
        }
        while (this.retryAttemptsLeft-- > 0 && csvRows.size() > 0) {
            if (this.retryAttemptsLeft != this.maxRetryAttempts - 1) {
                System.out.println("Retrying Download for failed ones..");
            }
            Iterator<List<String>> iterator = this.csvRows.iterator();
            while (iterator.hasNext()) {
                try {
                    rateLimiter.acquire();
                    List<String> line = iterator.next();
                    if (!downloadedFiles.contains(line.get(0))) {
                        downloadExecutor.submit(new PortkeyDownloader(line.get(0), line.get(1)));
                        if (downloadedFiles.contains(line.get(0))) {
                            iterator.remove();
                        }

                    } else {
                        System.out.println("File " + line.get(0) + " already downloaded.");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void resetCSVRows() {
        csvRows = csvRows.stream().filter(r -> !this.downloadedFiles.contains(r.get(0))).collect(Collectors.toSet());
    }

    public void setFileIds() {
        csvRows = readCSVLineByLine(fetchProperty("FILE_IDS_CSV")).stream().collect(Collectors.toSet());
    }
}
