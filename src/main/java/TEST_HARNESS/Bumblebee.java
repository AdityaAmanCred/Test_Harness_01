package TEST_HARNESS;

import static TEST_HARNESS.Util.getNames;
import java.io.File;
import java.io.IOException;
import java.sql.SQLOutput;
import java.util.concurrent.TimeUnit;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Bumblebee extends ParserResponses {
    public Bumblebee(double fetchRate) {
        super(fetchRate);
    }

    @Override
    public void fetchResponse(String fileName, Environment env) {
        String template_id = "";
        if (this.getParserType() == ParserType.PRIMARY) {
            template_id = this.getPrimaryParserTemplateId();
        } else {
            template_id = this.getSecondaryParserTemplateId();
        }
        if (env == Environment.PROD) {
            this.envName = "prod";
        } else {
            envName = "stg";
        }
        OkHttpClient client = new OkHttpClient().newBuilder().connectTimeout(15000, TimeUnit.MILLISECONDS).build();
        RequestBody body = new MultipartBody.Builder().setType(MultipartBody.FORM).addFormDataPart("template_id", template_id)
                                                      .addFormDataPart("pdf_to_transform", fileName, RequestBody
                                                              .create(MediaType.parse("application/octet-stream"), new File(pdfLocation + fileName)))
                                                      .build();
        Request request = new Request.Builder().url("http://bumblebee." + envName + ".dreamplug.net/xfmr/v1/pdf2data").method("POST", body)
                                               .addHeader("Accept", "*/*").addHeader("Accept-Encoding", "gzip, deflate")
                                               .addHeader("cache-control", "no-cache")
                                               .addHeader("content-type", "multipart/form-data; boundary=----WebKitFormBoundary7MA4YWxkTrZu0gW")
                                               .build();
        try {
            Response response = client.newCall(request).execute();
            if (response.code() >= 200 && response.code() < 300) {
                saveResponse(response.body().bytes(), fileName, ++fetchCounter);
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
        if (getNames(this.getParserType().toString().equalsIgnoreCase("PRIMARY") ? "PRIMARY_DIR" : "SECONDARY_DIR").size() > 0) {
            fetchedFileNames = getNames(this.getParserType().toString().equalsIgnoreCase("PRIMARY") ? "PRIMARY_DIR" : "SECONDARY_DIR");
            this.fetchCounter = fetchedFileNames.size();
        }
        for (String fileName : fileNames) {
            rateLimiter.acquire(1);
            if (!this.fetchedFileNames.contains(fileName)) {
                this.fetchResponse(fileName + ".pdf", env);
                this.fetchedFileNames.add(fileName);
            }else{
                System.out.println("Bumbleebee response for file "+ fileName+ " already fetched");
            }
        }
    }

}
