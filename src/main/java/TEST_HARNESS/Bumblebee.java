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

public class Bumblebee extends ParserResponses {
    public Bumblebee(String fileName, Environment environment) {
        super(fileName, environment);
        this.parserName = ParserName.BUMBLEBEE;
    }

    @Override
    public void fetchResponse() {
        String template_id = "";
        if (this.getParserType() == ParserType.PRIMARY) {
            template_id = this.getPrimaryParserTemplateId();
        } else {
            template_id = this.getSecondaryParserTemplateId();
        }
        if (environment == Environment.PROD) {
            envName = "prod";
        } else {
            envName = "stg";
        }
        OkHttpClient client = new OkHttpClient().newBuilder().connectTimeout(60, TimeUnit.SECONDS).readTimeout(60, TimeUnit.SECONDS)
                                                .writeTimeout(60, TimeUnit.SECONDS).build();
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
                saveResponse(response.body().bytes(), fileName, ++primaryParserFetchCounter);
                ParsingExecutor.fetchedFileNamesPrimaryParser.add(fileName);
            } else {
                System.out.println("On " + envName + " ResponseCode: " + response.code() + " for " + fileName.split("\\.")[0]);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        fetchResponse();
    }
}
