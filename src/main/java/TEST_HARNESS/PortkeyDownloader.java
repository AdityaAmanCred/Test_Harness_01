package TEST_HARNESS;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class PortkeyDownloader extends Downloader {
    public PortkeyDownloader(String objectId, String userId, BlockingQueue<String> pBlockingQueue,BlockingQueue<String> sBlockingQueue) {
        super(objectId, userId,pBlockingQueue,sBlockingQueue);
    }

    public void downloadPdf() throws IOException {
        OkHttpClient client = new OkHttpClient().newBuilder().build();
        Request request = new Request.Builder().url("http://portkey.prod.dreamplug.net/etl/v1/source/data/" + objectId + "?unlock=false")
                                               .method("GET", null).addHeader("X-USER-ID", userId).build();
        Response response = client.newCall(request).execute();
        if (response.code() >= 200 && response.code() < 300) {
            savePDFFile(response.body().bytes(), objectId);
        } else {
            System.out.println("For downloading object_id: " + objectId + ", user_id: " + userId + ", got response code " + response.code());
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
