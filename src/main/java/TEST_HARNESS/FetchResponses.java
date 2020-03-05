package TEST_HARNESS;

import static TEST_HARNESS.Util.getPropertyFromFile;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.TimeUnit;
import lombok.AllArgsConstructor;
import lombok.Data;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

@Data
@AllArgsConstructor
public class FetchResponses {
    private String stageId;

    private String prodId;

    private String pdfLocation;

    public void fetchBumbleBeeStageResponse(String pdfFileName) throws IOException {
        OkHttpClient client = new OkHttpClient().newBuilder().connectTimeout(15000, TimeUnit.MILLISECONDS).build();
        RequestBody body = new MultipartBody.Builder().setType(MultipartBody.FORM).addFormDataPart("template_id", stageId)
                                                      .addFormDataPart("pdf_to_transform", pdfFileName, RequestBody
                                                              .create(MediaType.parse("application/octet-stream"),
                                                                      new File(pdfLocation + pdfFileName))).build();
        Request request = new Request.Builder().url("http://bumblebee.stg.dreamplug.net/xfmr/v1/pdf2data").method("POST", body)
                                               .addHeader("Accept", "*/*").addHeader("Accept-Encoding", "gzip, deflate")
                                               .addHeader("cache-control", "no-cache")
                                               .addHeader("content-type", "multipart/form-data; boundary=----WebKitFormBoundary7MA4YWxkTrZu0gW")
                                               .build();
        try {
            Response response = client.newCall(request).execute();
            this.writeStage(response.body().bytes(), pdfFileName);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void fetchBumbleBeeProdResponse(String pdfFileName) throws IOException {
        OkHttpClient client = new OkHttpClient().newBuilder().connectTimeout(15000, TimeUnit.MILLISECONDS).build();
        RequestBody body = new MultipartBody.Builder().setType(MultipartBody.FORM).addFormDataPart("template_id", prodId)
                                                      .addFormDataPart("pdf_to_transform", pdfFileName, RequestBody
                                                              .create(MediaType.parse("application/octet-stream"),
                                                                      new File(pdfLocation + pdfFileName))).build();
        Request request = new Request.Builder().url("http://bumblebee.prod.dreamplug.net/xfmr/v1/pdf2data").method("POST", body)
                                               .addHeader("Accept", "*/*").addHeader("Accept-Encoding", "gzip, deflate")
                                               .addHeader("cache-control", "no-cache")
                                               .addHeader("content-type", "multipart/form-data; boundary=----WebKitFormBoundary7MA4YWxkTrZu0gW")
                                               .build();
        try {
            Response response = client.newCall(request).execute();
            this.writeProd(response.body().bytes(), pdfFileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void fetchPandoraStageResponse(String pdfFileName) throws IOException {
        OkHttpClient client = new OkHttpClient().newBuilder().connectTimeout(15000, TimeUnit.MILLISECONDS).build();
        RequestBody body = new MultipartBody.Builder().setType(MultipartBody.FORM).addFormDataPart("feed_id", stageId)
                                                      .addFormDataPart("file", pdfFileName, RequestBody
                                                              .create(MediaType.parse("application/octet-stream"),
                                                                      new File(pdfLocation + pdfFileName))).build();
        Request request = new Request.Builder().url("http://pandorastreet.stg.dreamplug.net/rocketpdf/api/test/pdf").method("POST", body)
                                               .addHeader("Content-Type",
                                                       "multipart/form-data; boundary=--------------------------204397559572218327330332").build();
        try {
            Response response = client.newCall(request).execute();
            this.writeStage(response.body().bytes(), pdfFileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void fetchPandoraProdResponse(String pdfFileName) throws IOException {
        OkHttpClient client = new OkHttpClient().newBuilder().connectTimeout(15000, TimeUnit.MILLISECONDS).build();
        RequestBody body = new MultipartBody.Builder().setType(MultipartBody.FORM).addFormDataPart("feed_id", prodId)
                                                      .addFormDataPart("file", pdfFileName, RequestBody
                                                              .create(MediaType.parse("application/octet-stream"),
                                                                      new File(pdfLocation + pdfFileName))).build();
        Request request = new Request.Builder().url("http://pandorastreet.prod.dreamplug.net/rocketpdf/api/test/pdf").method("POST", body)
                                               .addHeader("Content-Type",
                                                       "multipart/form-data; boundary=--------------------------089951034329116001461389").build();
        try {
            Response response = client.newCall(request).execute();
            this.writeProd(response.body().bytes(), pdfFileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeStage(byte[] bytes, String fileName) {
        File file = new File(getPropertyFromFile("application.properties").getProperty("STAGE_DIR") + fileName.split("\\.")[0] + ".json");
        try {

            OutputStream os = new FileOutputStream(file);

            os.write(bytes);
            System.out.println("Stage response fetched for: " + fileName);

            os.close();
        } catch (Exception e) {
            System.out.println(fileName + ".pdf: Exception: " + e);
        }
    }

    private void writeProd(byte[] bytes, String fileName) {
        File file = new File(getPropertyFromFile("application.properties").getProperty("EXPECTED_DIR") + fileName.split("\\.")[0] + ".json");
        try {

            OutputStream os = new FileOutputStream(file);

            os.write(bytes);
            System.out.println("Prod response fetched for: " + fileName);
            os.close();
        } catch (Exception e) {
            System.out.println(fileName + ".pdf: Exception: " + e);
        }
    }

    public void fetchBumblebeeStageResponses() throws IOException {
        for (String fileName : Application.fileNames) {
            this.fetchBumbleBeeStageResponse(fileName + ".pdf");
        }
    }

    public void fetchBumblebeeProdResponses() throws IOException {
        for (String fileName : Application.fileNames) {
            this.fetchBumbleBeeProdResponse(fileName + ".pdf");
        }
    }

    public void fetchPandoraStageResponses() throws IOException {
        for (String fileName : Application.fileNames) {
            this.fetchPandoraStageResponse(fileName + ".pdf");
        }
    }

    public void fetchPandoraProdResponses() throws IOException {
        for (String fileName : Application.fileNames) {
            this.fetchPandoraProdResponse(fileName + ".pdf");
        }
    }
}



