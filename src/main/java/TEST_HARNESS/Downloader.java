package TEST_HARNESS;

import static TEST_HARNESS.Util.fetchProperty;
import static TEST_HARNESS.Util.getNames;
import static TEST_HARNESS.Util.readCSVLineByLine;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import com.google.common.util.concurrent.RateLimiter;
import lombok.Data;

@Data
abstract class Downloader {
    protected int downloadcount;

    protected List<List<String>> stringMatrix = new ArrayList<>();

    protected List<String> downloadedFiles = new ArrayList<>();

    protected RateLimiter rateLimiter;

    protected Downloader(double rate) {
        this.setFileIds();
        this.downloadcount = 0;
        this.rateLimiter = RateLimiter.create(rate);
        if (getNames("PDF_DOWNLOAD_LOC").size() > 0) {
            this.downloadedFiles = getNames("PDF_DOWNLOAD_LOC");
            this.downloadcount = downloadedFiles.size();
        }
    }

    abstract void downloadPdf(String... args) throws IOException;

    abstract void downloadPDFs();

    public void setFileIds() {
        stringMatrix = readCSVLineByLine(fetchProperty("FILE_IDS_CSV"));
    }

    public void savePDFFile(byte[] bytes, String fileName) {
        File file = new File(fetchProperty("PDF_DOWNLOAD_LOC") + fileName + ".pdf");
        try {
            OutputStream os = new FileOutputStream(file);
            os.write(bytes);
            System.out.println("Downloaded: " + fileName + ".pdf downloadCount: " + ++this.downloadcount);
            os.close();
        } catch (Exception e) {
            System.out.println("Error in downloading file: " + fileName + " :" + e);
        }
    }
}
