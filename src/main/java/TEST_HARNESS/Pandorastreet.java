package TEST_HARNESS;

import static TEST_HARNESS.Util.getNames;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Pandorastreet extends Parser {
    public Pandorastreet(ParserType parserType, String fileName) {
        super(parserType, fileName);
    }

    public boolean fetchResponse() {
        OkHttpClient client = new OkHttpClient().newBuilder().connectTimeout(15000, TimeUnit.MILLISECONDS).build();
        RequestBody body = new MultipartBody.Builder().setType(MultipartBody.FORM).addFormDataPart("feed_id", this.templateId)
                                                      .addFormDataPart("file", fileName, RequestBody
                                                              .create(MediaType.parse("application/octet-stream"), new File(pdfLocation + fileName)))
                                                      .build();
        Request request = new Request.Builder().url("http://pandorastreet." + this.envName + ".dreamplug.net/rocketpdf/api/test/pdf")
                                               .method("POST", body).addHeader("Content-Type",
                        "multipart/form-data; boundary=--------------------------204397559572218327330332").build();
        try {
            Response response = client.newCall(request).execute();
            if (response.code() >= 200 && response.code() < 300) {
                saveResponse(response.body().bytes(), fileName);
                return true;
            } else {
                System.out.println("On " + this.envName + " ResponseCode: " + response.code() + " for " + fileName.split("\\.")[0]);
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
