package TEST_HARNESS;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class PortkeyDownloader extends Downloader {
    public PortkeyDownloader(double rate) {
        super(rate);
    }

    public boolean downloadPdf(String... args) throws IOException {
        OkHttpClient client = new OkHttpClient().newBuilder().build();
        Request request = new Request.Builder().url("http://portkey.prod.dreamplug.net/etl/v1/source/data/" + args[0] + "?unlock=false")
                                               .method("GET", null).addHeader("X-USER-ID", args[1]).build();
        Response response = client.newCall(request).execute();
        if (response.code() >= 200 && response.code() < 300) {
            savePDFFile(response.body().bytes(), args[0]);
            return true;
        } else {
            System.out.println("For downloading object_id: " + args[0] + ", user_id: " + args[1] + ", got response code " + response.code());

        }
        return false;
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
                    rateLimiter.acquire(1);
                    List<String> line = iterator.next();
                    if (!downloadedFiles.contains(line.get(0))) {
                        if (this.downloadPdf(line.get(0), line.get(1))) {
                            downloadedFiles.add(line.get(0));
                            iterator.remove();
                        }

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

    private void resetCSVRows() {
        csvRows = csvRows.stream().filter(r -> !this.downloadedFiles.contains(r.get(0))).collect(Collectors.toSet());
    }

}
