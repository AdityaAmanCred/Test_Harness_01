package TEST_HARNESS;

import static TEST_HARNESS.Util.getPropertyFromFile;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.TimeUnit;
import com.google.common.util.concurrent.RateLimiter;
import lombok.Data;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

@Data
public class FetchResponses {
    private String stageId;

    private String prodId;

    private String pdfLocation;

    private String environment;

    private static RateLimiter rateLimiter;

    public FetchResponses(String stageId, String prodId, String pdfLocation, double rate) {
        this.stageId = stageId;
        this.prodId = prodId;
        this.pdfLocation = pdfLocation;
        rateLimiter = RateLimiter.create(rate);
    }

    public void fetchBumbleBeeResponse(String pdfFileName, Environment env) throws IOException {
        String template_id = "";
        if (env == Environment.PROD) {
            environment = "prod";
            template_id = prodId;
        } else {
            environment = "stg";
            template_id = stageId;
        }
        OkHttpClient client = new OkHttpClient().newBuilder().connectTimeout(15000, TimeUnit.MILLISECONDS).build();
        RequestBody body = new MultipartBody.Builder().setType(MultipartBody.FORM).addFormDataPart("template_id", template_id)
                                                      .addFormDataPart("pdf_to_transform", pdfFileName, RequestBody
                                                              .create(MediaType.parse("application/octet-stream"),
                                                                      new File(pdfLocation + pdfFileName))).build();
        Request request = new Request.Builder().url("http://bumblebee." + environment + ".dreamplug.net/xfmr/v1/pdf2data").method("POST", body)
                                               .addHeader("Accept", "*/*").addHeader("Accept-Encoding", "gzip, deflate")
                                               .addHeader("cache-control", "no-cache")
                                               .addHeader("content-type", "multipart/form-data; boundary=----WebKitFormBoundary7MA4YWxkTrZu0gW")
                                               .build();
        try {
            Response response = client.newCall(request).execute();
            if (response.code() >= 200 && response.code() < 300) {
                this.saveResponse(response.body().bytes(), pdfFileName, env);
            } else {
                System.out.println("On " + environment + " ResponseCode: " + response.code() + " for " + pdfFileName.split("\\.")[0]);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void fetchPandoraResponse(String pdfFileName, Environment env) throws IOException {
        String feed_id = "";
        if (env == Environment.PROD) {
            environment = "prod";
            feed_id = prodId;
        } else {
            environment = "stg";
            feed_id = stageId;
        }
        OkHttpClient client = new OkHttpClient().newBuilder().connectTimeout(15000, TimeUnit.MILLISECONDS).build();
        RequestBody body = new MultipartBody.Builder().setType(MultipartBody.FORM).addFormDataPart("feed_id", feed_id)
                                                      .addFormDataPart("file", pdfFileName, RequestBody
                                                              .create(MediaType.parse("application/octet-stream"),
                                                                      new File(pdfLocation + pdfFileName))).build();
        Request request = new Request.Builder().url("http://pandorastreet." + this.environment + ".dreamplug.net/rocketpdf/api/test/pdf")
                                               .method("POST", body).addHeader("Content-Type",
                        "multipart/form-data; boundary=--------------------------204397559572218327330332").build();
        try {
            Response response = client.newCall(request).execute();
            if (response.code() >= 200 && response.code() < 300) {
                this.saveResponse(response.body().bytes(), pdfFileName, env);
            } else {
                System.out.println("On " + environment + " ResponseCode: " + response.code() + " for " + pdfFileName.split("\\.")[0]);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void fetchOptimusResponse(String pdfFileName, Environment env, Application.ParserName otherParser) throws IOException {

        String feed_id = "";
        String suffix = "";
        if (env == Environment.PROD) {
            environment = "prod";
            feed_id = prodId;
        } else {
            environment = "stg";
            feed_id = stageId;
        }
        if (otherParser == Application.ParserName.BUMBLEBEE) {
            suffix = "transformed_data";
        } else {
            suffix = "json_object";
        }
        OkHttpClient client = new OkHttpClient().newBuilder().build();
        MediaType mediaType = MediaType.parse("multipart/form-data; boundary=----WebKitFormBoundary7MA4YWxkTrZu0gW");
        RequestBody body = new MultipartBody.Builder().setType(MultipartBody.FORM).addFormDataPart("pdf_to_transform", pdfFileName,
                RequestBody.create(MediaType.parse("application/octet-stream"), new File(pdfLocation + pdfFileName)))
                                                      .addFormDataPart("config_id", feed_id).build();
        Request request = new Request.Builder().url("http://optimus." + this.environment + ".dreamplug.net/pdf_to_json/file_stream/test/" + suffix)
                                               .method("POST", body).addHeader("Accept", "*/*").addHeader("Accept-Encoding", "gzip, deflate")
                                               .addHeader("cache-control", "no-cache")
                                               .addHeader("content-type", "multipart/form-data; boundary=----WebKitFormBoundary7MA4YWxkTrZu0gW")
                                               .build();
        try {
            Response response = client.newCall(request).execute();
            if (response.code() >= 200 && response.code() < 300) {
                this.saveResponse(response.body().bytes(), pdfFileName, env);
            } else {
                System.out.println("On " + environment + " ResponseCode: " + response.code() + " for " + pdfFileName.split("\\.")[0]);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void saveResponse(byte[] bytes, String fileName, Environment env) {
        File file;
        if (env == Environment.PROD) {
            file = new File(getPropertyFromFile("application.properties").getProperty("EXPECTED_DIR") + fileName.split("\\.")[0] + ".json");
        } else {
            file = new File(getPropertyFromFile("application.properties").getProperty("STAGE_DIR") + fileName.split("\\.")[0] + ".json");
        }

        try {

            OutputStream os = new FileOutputStream(file);

            os.write(bytes);
            if (env == Environment.PROD) {
                System.out.println("Prod response fetched for: " + fileName);
            } else {
                System.out.println("Stage response fetched for: " + fileName);
            }

            os.close();
        } catch (Exception e) {
            System.out.println(fileName + ".pdf: Exception: " + e);
        }
    }

    public void fetchBumblebeeResponses(Environment env) throws IOException {
        for (String fileName : Application.fileNames) {
            rateLimiter.acquire(1);
            this.fetchBumbleBeeResponse(fileName + ".pdf", env);
        }
    }

    public void fetchPandoraResponses(Environment env) throws IOException {
        for (String fileName : Application.fileNames) {
            rateLimiter.acquire(1);
            this.fetchPandoraResponse(fileName + ".pdf", env);
        }
    }

    public void fetchOptimusResponses(Environment env, Application.ParserName otherParser) throws IOException {
        for (String fileName : Application.fileNames) {
            rateLimiter.acquire(1);
            this.fetchOptimusResponse(fileName + ".pdf", env, otherParser);
        }
    }
}



