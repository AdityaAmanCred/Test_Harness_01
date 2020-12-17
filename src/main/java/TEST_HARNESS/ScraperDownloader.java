package TEST_HARNESS;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

//public class ScraperDownloader extends Downloader {
//    public ScraperDownloader(double rate) {
//        super(rate);
//    }
//
//    public boolean downloadPdf(String... args) throws IOException {
//        OkHttpClient client = new OkHttpClient().newBuilder().connectTimeout(15000, TimeUnit.MILLISECONDS).build();
//        MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
//        RequestBody body = RequestBody.create(mediaType, "id=" + args[0] + "&unlock=true");
//        Request request = new Request.Builder().url("http://scraper.pci.dreamplug.net/scraper/v1/pdf2data/download/pdf").method("POST", body)
//                                               .addHeader("Content-Type", "application/x-www-form-urlencoded").build();
//        Response response = client.newCall(request).execute();
//        if (response.code() >= 200 && response.code() < 300) {
//            savePDFFile(response.body().bytes(), args[0]);
//            return true;
//        } else {
//            System.out.println("For downloading file: " + args[0] + ", got response code " + response.code());
//
//        }
//        return false;
//    }
//
//    public void downloadPDFs() {
//        while (this.retryAttemptsLeft-- > 0 && csvRows.size() > 0) {
//            Iterator<List<String>> iterator = this.csvRows.iterator();
//            while (iterator.hasNext()) {
//                try {
//                    rateLimiter.acquire(1);
//                    List<String> line = iterator.next();
//                    if (!downloadedFiles.contains(line.get(0))) {
//                        if (this.downloadPdf(line.get(0))) {
//                            downloadedFiles.add(line.get(0));
//                            iterator.remove();
//                        }
//
//                    } else {
//                        System.out.println("File " + line.get(0) + " already downloaded.");
//                    }
//                } catch (SocketTimeoutException e) {
//                    e.printStackTrace();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//    }
//
//    @Override
//    public void run() {
//
//    }
//}

