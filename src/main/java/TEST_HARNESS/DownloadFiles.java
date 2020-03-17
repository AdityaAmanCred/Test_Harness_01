package TEST_HARNESS;

import static TEST_HARNESS.Util.getPropertyFromFile;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
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

    public DownloadFiles(double rate) {
        rateLimiter = RateLimiter.create(rate);

    }

    public void downloadPDFs() throws IOException {
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
        saveFile(response.body().bytes(), id);
    }

    public void saveFile(byte[] bytes, String fileName) {
        File file = new File(getPropertyFromFile("application.properties").getProperty("PDF_DOWNLOAD_LOC") + fileName + ".pdf");
        try {

            OutputStream os = new FileOutputStream(file);

            os.write(bytes);
            System.out.println("Downloaded: " + fileName + ".pdf");
            os.close();
        } catch (Exception e) {
            System.out.println("Error in downloading file: " + fileName + " :" + e);
        }
    }

    public void setFileIds() {

        try {
            Scanner scanner = new Scanner(new File(getPropertyFromFile("application.properties").getProperty("FILE_IDS_CSV")));
            while (scanner.hasNextLine()) {
                this.fileIds.add(scanner.nextLine());
            }
            scanner.close();
        } catch (FileNotFoundException e) {
            System.out.println("FILE_ID file NOT FOUND");
            e.printStackTrace();
        }
    }
}
