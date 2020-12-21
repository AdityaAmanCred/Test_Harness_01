package TEST_HARNESS;

import java.io.IOException;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class StandaloneMode {
    public static Integer generateStatement(String content) throws IOException {
        OkHttpClient client = new OkHttpClient().newBuilder().build();
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, content);
        Request request = new Request.Builder().url("http://statement-service.stg.dreamplug.net/statement/consumer/consolidate").method("POST", body)
                                               .addHeader("Content-Type", "application/json").build();
        Response response = client.newCall(request).execute();
        response.close();
        return response.code();
    }
}
