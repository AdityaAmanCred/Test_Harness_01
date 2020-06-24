package TEST_HARNESS;

import static TEST_HARNESS.Util.fetchProperty;
import static TEST_HARNESS.Util.getNames;
import java.io.File;
import java.io.IOException;
import lombok.Setter;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Optimus extends ParserResponses {
    @Setter
    private String apiUrlSuffix;

    public Optimus(double fetchRate) {
        super(fetchRate);//change this to "transformed_data" if comparing against pandora.
        apiUrlSuffix = "json_object";
    }

    @Override
    public void fetchResponse(String fileName, Environment env) {
        String feed_id = "";
        if (this.getParserType() == ParserType.PRIMARY) {
            feed_id = this.getPrimaryParserTemplateId();
        } else {
            feed_id = this.getSecondaryParserTemplateId();
        }
        if (env == Environment.PROD) {
            envName = "prod";

        } else {
            envName = "stg";
        }

        Environment otherEnv = (Application.getPrimaryParserName() == ParserName.OPTIMUS) ? Application.getSecondaryParserEnv() : Application
                .getPrimaryParserEnv();
        String otherParser = (fetchProperty("PRIMARY_PARSER_NAME").equalsIgnoreCase("OPTIMUS")) ? fetchProperty(
                "SECONDARY_PARSER_NAME") : fetchProperty("PRIMARY_PARSER_NAME");
        if (otherParser.equalsIgnoreCase("BUMBLEBEE")) {
            apiUrlSuffix = "transformed_data";
        }
        OkHttpClient client = new OkHttpClient().newBuilder().build();
        MediaType mediaType = MediaType.parse("multipart/form-data; boundary=----WebKitFormBoundary7MA4YWxkTrZu0gW");
        RequestBody body = new MultipartBody.Builder().setType(MultipartBody.FORM).addFormDataPart("pdf_to_transform", fileName,
                RequestBody.create(MediaType.parse("application/octet-stream"), new File(pdfLocation + fileName)))
                                                      .addFormDataPart("config_id", feed_id).build();
        Request request = new Request.Builder().url("http://optimus." + this.envName + ".dreamplug.net/pdf_to_json/file_stream/test/" + apiUrlSuffix)
                                               .method("POST", body).addHeader("Accept", "*/*").addHeader("Accept-Encoding", "gzip, deflate")
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
            }
        }
    }
}
