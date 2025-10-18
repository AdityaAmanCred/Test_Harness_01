package TEST_HARNESS.parse;

import static TEST_HARNESS.utils.Util.fetchProperty;
import java.io.File;
import java.io.IOException;
import java.util.Objects;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

@Slf4j
public class Optimus extends Parser {
    @Setter
    private String apiUrlSuffix;

    public Optimus(ParserType parserType, String fileName) {
        super(parserType, fileName);
        apiUrlSuffix = "json_object";
    }

    public boolean fetchResponse() {
        String otherParser = (fetchProperty("PRIMARY_PARSER_NAME").equalsIgnoreCase("OPTIMUS")) ? fetchProperty(
                "SECONDARY_PARSER_NAME") : fetchProperty("PRIMARY_PARSER_NAME");
        if (otherParser.equalsIgnoreCase("BUMBLEBEE")) {
            apiUrlSuffix = "transformed_data";
        }
        OkHttpClient client = new OkHttpClient().newBuilder()
                                                .build();
        MediaType mediaType = MediaType.parse("multipart/form-data; boundary=----WebKitFormBoundary7MA4YWxkTrZu0gW");
        RequestBody body = new MultipartBody.Builder().setType(MultipartBody.FORM)
                                                      .addFormDataPart("pdf_to_transform", fileName,
                                                              RequestBody.create(MediaType.parse("application/octet-stream"),
                                                                      new File(pdfLocation + fileName)))
                                                      .addFormDataPart("config_id", this.templateId)
                                                      .build();
        //--make change--//
        Request request;
        if (Objects.equals(this.envName, "local")) {
            request = new Request.Builder().url(String.format(fetchProperty("OPTIMUS_LOCAL"), this.apiUrlSuffix))
                                           .method("POST", body)
                                           .addHeader("Accept", "*/*")
                                           .addHeader("Accept-Encoding", "gzip, deflate")
                                           .addHeader("cache-control", "no-cache")
                                           .addHeader("content-type", "multipart/form-data; boundary=----WebKitFormBoundary7MA4YWxkTrZu0gW")
                                           .build();
        } else {
            request = new Request.Builder().url(String.format(fetchProperty("OPTIMUS_FQDN"), this.envName, this.apiUrlSuffix))
                                           .method("POST", body)
                                           .addHeader("Accept", "*/*")
                                           .addHeader("Accept-Encoding", "gzip, deflate")
                                           .addHeader("cache-control", "no-cache")
                                           .addHeader("content-type", "multipart/form-data; boundary=----WebKitFormBoundary7MA4YWxkTrZu0gW")
                                           .build();
        }
        try {
            Response response = client.newCall(request)
                                      .execute();
            if (response.code() >= 200 && response.code() < 300) {
                saveResponse(response.body()
                                     .bytes(), fileName);
                return true;
            } else {
                log.warn("On " + this.envName + " ResponseCode: " + response.code() + " for " + fileName.split("\\.")[0]);
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
