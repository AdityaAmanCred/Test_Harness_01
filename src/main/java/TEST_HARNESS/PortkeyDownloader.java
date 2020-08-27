package TEST_HARNESS;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.List;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class PortkeyDownloader extends Downloader {
    public PortkeyDownloader(double rate) {
        super(rate);
    }

    public void downloadPdf(String... args) throws IOException {
        OkHttpClient client = new OkHttpClient().newBuilder().build();
        Request request = new Request.Builder().url("http://portkey.stg.dreamplug.net/etl/v1/source/data/" + args[0] + "?unlock=false")
                                               .method("GET", null).addHeader("X-USER-ID", args[1]).build();
        Response response = client.newCall(request).execute();
        if (response.code() >= 200 && response.code() < 300) {
            savePDFFile(response.body().bytes(), args[0]);
        } else {
            System.out.println("For downloading file: " + args[0] + ", got response code " + response.code());
        }
    }

    public void downloadPDFs() {
        for (List<String> line : stringMatrix) {
            try {
                rateLimiter.acquire(1);
                if (!downloadedFiles.contains(line.get(0))) {
                    this.downloadPdf(line.get(0), line.get(1));
                    downloadedFiles.add(line.get(0));
                } else {
                    System.out.println("File " + line.get(0) + " already downloaded.");
                }
            } catch (SocketTimeoutException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
