package TEST_HARNESS;

import static TEST_HARNESS.Util.fetchProperty;
import static TEST_HARNESS.Util.getNames;
import static TEST_HARNESS.Util.readCSVLineByLine;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.stream.Collectors;
import com.google.common.util.concurrent.RateLimiter;
import lombok.Data;

@Data
abstract class Downloader implements Runnable {
    protected String objectId;

    protected String userId;

    protected Downloader(String objectId, String userId) {
        this.objectId = objectId;
        this.userId = userId;
    }
    //    protected int downloadcount;
    //
    //    protected Set<List<String>> csvRows = new HashSet<>();
    //
    //    protected Set<String> downloadedFiles = new HashSet<>();
    //
    //    protected RateLimiter rateLimiter;
    //
    //    protected int retryAttemptsLeft;
    //
    //    final protected int maxRetryAttempts = 3;

    //    protected Downloader(double rate) {
    //        this.setFileIds();
    //        this.downloadcount = 0;
    //        this.rateLimiter = RateLimiter.create(rate);
    //        this.retryAttemptsLeft = maxRetryAttempts;
    //        if (getNames("PDF_DOWNLOAD_LOC").size() > 0) {
    //            this.downloadedFiles = getNames("PDF_DOWNLOAD_LOC");
    //            this.downloadcount = downloadedFiles.size();
    //        }
    //    }

    abstract void downloadPdf() throws IOException;

    //    abstract void downloadPDFs();

    //    public void setFileIds() {
    //        csvRows = readCSVLineByLine(fetchProperty("FILE_IDS_CSV")).stream().collect(Collectors.toSet());
    //    }

    public void savePDFFile(byte[] bytes, String fileName) {
        File file = new File(fetchProperty("PDF_DOWNLOAD_LOC") + fileName + ".pdf");
        try {
            OutputStream os = new FileOutputStream(file);
            os.write(bytes);
            System.out.println("DownloadThread: [" + Thread.currentThread()
                                                           .getId() + "] downloaded: " + fileName + ".pdf downloadCount: " + ++DownloadExecutor.downloadcount);
            os.close();
            DownloadExecutor.downloadedFiles.add(fileName);
        } catch (Exception e) {
            System.out.println("Error in downloading file: " + fileName + " :" + e);
        }
    }

}
