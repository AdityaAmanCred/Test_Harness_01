package TEST_HARNESS;

import static TEST_HARNESS.Util.fetchProperty;
import static TEST_HARNESS.Util.readCSVLineByLine;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import com.google.common.util.concurrent.RateLimiter;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class DownloadFiles {
    private List<String> fileIds = new ArrayList<>();

    private static RateLimiter rateLimiter;

    private int downloadcount;

    public DownloadFiles(double rate) {
        rateLimiter = RateLimiter.create(rate);
        this.setFileIds();
        downloadcount = 0;
    }

    public void downloadPDFs() throws IOException {
        downloadcount = 0;
        for (String id : fileIds) {
            try {
                rateLimiter.acquire(1);
                this.downloadPDF(id);
            } catch (SocketTimeoutException e) {
                e.printStackTrace();
            }

        }
    }

    public void downloadPDF(String id) throws IOException {
        OkHttpClient client = new OkHttpClient().newBuilder().connectTimeout(15000, TimeUnit.MILLISECONDS).build();
        MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
        RequestBody body = RequestBody.create(mediaType, "id=" + id + "&unlock=true");
        Request request = new Request.Builder().url("http://scraper.pci.dreamplug.net/scraper/v1/pdf2data/download/pdf").method("POST", body)
                                               .addHeader("Content-Type", "application/x-www-form-urlencoded").build();
        Response response = client.newCall(request).execute();
        if (response.code() >= 200 && response.code() < 300) {
            savePDFFile(response.body().bytes(), id);
        }

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

    public void setFileIds() {
        fileIds = readCSVLineByLine(fetchProperty("FILE_IDS_CSV"));
    }
}
