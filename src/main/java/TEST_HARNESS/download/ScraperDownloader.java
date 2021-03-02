package TEST_HARNESS.download;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

@Slf4j
public class ScraperDownloader extends Downloader {
    public ScraperDownloader(String objectId, String userId, BlockingQueue<String> pBlockingQueue, BlockingQueue<String> sBlockingQueue) {
        super(objectId, userId, pBlockingQueue, sBlockingQueue);
    }

    public void downloadPdf() throws IOException {
        OkHttpClient client = new OkHttpClient().newBuilder().connectTimeout(15000, TimeUnit.MILLISECONDS).build();
        MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
        RequestBody body = RequestBody.create(mediaType, "id=" + objectId + "&unlock=true");
        Request request = new Request.Builder().url("http://scraper.pci.dreamplug.net/scraper/v1/pdf2data/download/pdf").method("POST", body)
                                               .addHeader("Content-Type", "application/x-www-form-urlencoded").build();
        Response response = client.newCall(request).execute();
        if (response.code() >= 200 && response.code() < 300) {
            savePDFFile(response.body().bytes(), objectId);

        } else {
            log.warn("For downloading file: " + objectId + ", got response code " + response.code());

        }
    }

    @Override
    public void run() {
        try {
            downloadPdf();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

