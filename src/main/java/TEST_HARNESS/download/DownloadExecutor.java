package TEST_HARNESS.download;

import static TEST_HARNESS.utils.Util.fetchProperty;
import static TEST_HARNESS.utils.Util.getNames;
import static TEST_HARNESS.utils.Util.readCSVLineByLine;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import com.google.common.util.concurrent.RateLimiter;
import TEST_HARNESS.config.Config;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Prithvi Patil
 * @version 1.0
 */
@Slf4j
public class DownloadExecutor implements Runnable {
    private int numberOfDownloadThreads;

    private BlockingQueue<String> pBlockingQueue;

    private BlockingQueue<String> sBlockingQueue;

    private ExecutorService downloadThreadPool;

    public static int downloadcount;

    private Set<List<String>> csvRows = new HashSet<>();

    public static Set<String> downloadedFiles = new HashSet<>();

    private RateLimiter rateLimiter;

    private int retryAttemptsLeft;

    final private int maxRetryAttempts = 3;

    private DownloaderName downloaderName;

    private int maxNumberOfDownloadThreads = 5;

    private int maxDownloadRate = 2;

    public DownloadExecutor(DownloaderName downloaderName) {
        this.numberOfDownloadThreads = Math.min(maxNumberOfDownloadThreads, Integer.parseInt(fetchProperty("NUM_DOWNLOAD_THREADS")));
        this.pBlockingQueue = Config.getPrimaryParserBlockingQueue();
        this.sBlockingQueue = Config.getSecondaryParserBlockingQueue();
        downloadThreadPool = Executors.newFixedThreadPool(numberOfDownloadThreads);
        this.setFileIds();
        this.downloadcount = 0;
        this.rateLimiter = RateLimiter.create(Math.min(maxDownloadRate, Double.parseDouble(fetchProperty("DOWNLOAD_RATELIMIT"))));
        this.retryAttemptsLeft = maxRetryAttempts;
        if (getNames("PDF_DOWNLOAD_LOC").size() > 0) {
            this.downloadedFiles = getNames("PDF_DOWNLOAD_LOC");
            this.downloadcount = downloadedFiles.size();
        }
        this.downloaderName = downloaderName;
        resetCSVRows();
    }

    public void downloadPDFs() throws InterruptedException {
        if (this.csvRows.size() == 0) {
            log.info("All PDFs already downloaded");
        }
        while (this.retryAttemptsLeft-- > 0 && csvRows.size() > 0) {
            if (this.retryAttemptsLeft != this.maxRetryAttempts - 1) {
                log.info("Retrying Download for failed ones..");
            }
            Iterator<List<String>> iterator = this.csvRows.iterator();
            while (iterator.hasNext()) {
                try {
                    rateLimiter.acquire();
                    List<String> line = iterator.next();
                    if (!downloadedFiles.contains(line.get(0))) {
                        downloadThreadPool.submit(getDownloader(downloaderName, line.get(0), line.get(1)));
                        iterator.remove();
                    } else {
                        log.info("File " + line.get(0) + " already downloaded.");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void resetCSVRows() {
        csvRows = csvRows.stream().filter(r -> !this.downloadedFiles.contains(r.get(0))).collect(Collectors.toSet());
        if (this.csvRows.size() == 0) {
            Config.setDownloadThreadIsTerminated(true);
        }
    }

    public void setFileIds() {
        csvRows = readCSVLineByLine(fetchProperty("FILE_IDS_CSV")).stream().collect(Collectors.toSet());
    }

    private Downloader getDownloader(DownloaderName downloaderName, String objectId, String userId) {
        return (downloaderName == DownloaderName.PORTKEY) ? new PortkeyDownloader(objectId, userId, this.pBlockingQueue,
                this.sBlockingQueue) : new ScraperDownloader(objectId, userId, this.pBlockingQueue, this.sBlockingQueue);
    }

    public void run() {
        try {
            downloadPDFs();
            downloadThreadPool.shutdown();
            downloadThreadPool.awaitTermination(1, TimeUnit.DAYS);
            Config.setDownloadThreadIsTerminated(true);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
