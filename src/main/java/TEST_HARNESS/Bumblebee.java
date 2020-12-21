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

public class Bumblebee extends Parser {
    public Bumblebee(ParserType parserType, String fileName) {
        super(parserType, fileName);
    }

    public boolean fetchResponse() {
        OkHttpClient client = new OkHttpClient().newBuilder().connectTimeout(60, TimeUnit.SECONDS).readTimeout(180, TimeUnit.SECONDS)
                                                .writeTimeout(180, TimeUnit.SECONDS).build();
        RequestBody body = new MultipartBody.Builder().setType(MultipartBody.FORM).addFormDataPart("template_id", this.templateId)
                                                      .addFormDataPart("pdf_to_transform", this.fileName, RequestBody
                                                              .create(MediaType.parse("application/octet-stream"),
                                                                      new File(this.pdfLocation + this.fileName))).build();
        Request request = new Request.Builder().url("http://bumblebee." + this.envName + ".dreamplug.net/xfmr/v1/pdf2data").method("POST", body)
                                               .addHeader("Accept", "*/*").addHeader("Accept-Encoding", "gzip, deflate")
                                               .addHeader("cache-control", "no-cache")
                                               .addHeader("content-type", "multipart/form-data; boundary=----WebKitFormBoundary7MA4YWxkTrZu0gW")
                                               .build();
        try {
            Response response = client.newCall(request).execute();
            if (response.code() >= 200 && response.code() < 300) {
                saveResponse(response.body().bytes(), this.fileName);
                return true;
            } else {
                System.out.println("On " + this.envName + " ResponseCode: " + response.code() + " for " + this.fileName.split("\\.")[0]);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void run() {
        fetchResponse();
    }
}
