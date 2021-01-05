package TEST_HARNESS.download;

import static TEST_HARNESS.utils.Util.fetchProperty;
import static TEST_HARNESS.utils.Util.isValidParserSelection;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.BlockingQueue;
import TEST_HARNESS.Application;
import TEST_HARNESS.config.CompareAgainst;
import TEST_HARNESS.config.Config;
import lombok.Data;

@Data
abstract class Downloader implements Runnable {
    protected String objectId;

    protected String userId;

    protected BlockingQueue<String> pBlockingQueue;

    protected BlockingQueue<String> sBlockingQueue;

    public Downloader(String objectId, String userId, BlockingQueue<String> pBlockingQueue, BlockingQueue<String> sBlockingQueue) {
        this.objectId = objectId;
        this.userId = userId;
        this.pBlockingQueue = pBlockingQueue;
        this.sBlockingQueue = sBlockingQueue;
    }

    abstract void downloadPdf() throws IOException;

    public void savePDFFile(byte[] bytes, String fileName) {
        File file = new File(fetchProperty("PDF_DOWNLOAD_LOC") + fileName + ".pdf");
        try {
            OutputStream os = new FileOutputStream(file);
            os.write(bytes);
            System.out.println(
                    String.format("DownloadThread:[%s] downloaded %s. DownloadCount: %d", Thread.currentThread().getId(), fileName + ".pdf",
                            ++DownloadExecutor.downloadcount));
            os.close();
            DownloadExecutor.downloadedFiles.add(fileName);
            this.pBlockingQueue.put(fileName);
            if (Config.getCompareAgainst() == CompareAgainst.SECONDARY && isValidParserSelection() == true) {
                this.sBlockingQueue.put(fileName);
            }
        } catch (Exception e) {
            System.out.println("Error in downloading file: " + fileName + " :" + e);
        }
    }

}
