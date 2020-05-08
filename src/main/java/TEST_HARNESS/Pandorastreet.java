package TEST_HARNESS;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Pandorastreet extends ParserResponses {
    public Pandorastreet(double fetchRate) {
        super(fetchRate);
    }

    @Override
    public void fetchResponse(String fileName, Environment env) {
        String feed_id = "";
        if (env == Environment.PROD) {
            envName = "prod";
            feed_id = prodId;
        } else {
            envName = "stg";
            feed_id = stageId;
        }
        OkHttpClient client = new OkHttpClient().newBuilder().connectTimeout(15000, TimeUnit.MILLISECONDS).build();
        RequestBody body = new MultipartBody.Builder().setType(MultipartBody.FORM).addFormDataPart("feed_id", feed_id)
                                                      .addFormDataPart("file", fileName, RequestBody
                                                              .create(MediaType.parse("application/octet-stream"), new File(pdfLocation + fileName)))
                                                      .build();
        Request request = new Request.Builder().url("http://pandorastreet." + this.envName + ".dreamplug.net/rocketpdf/api/test/pdf")
                                               .method("POST", body).addHeader("Content-Type",
                        "multipart/form-data; boundary=--------------------------204397559572218327330332").build();
        try {
            Response response = client.newCall(request).execute();
            if (response.code() >= 200 && response.code() < 300) {
                saveResponse(response.body().bytes(), fileName, env, ++fetchCounter);
            } else {
                System.out.println("On " + envName + " ResponseCode: " + response.code() + " for " + fileName.split("\\.")[0]);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void fetchAllResponses(Environment env) {
        setFetchCounter(0);
        for (String fileName : fileNames) {
            rateLimiter.acquire(1);
            this.fetchResponse(fileName + ".pdf", env);
        }
    }
}
