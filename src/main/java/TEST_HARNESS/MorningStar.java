package TEST_HARNESS;

import static TEST_HARNESS.Util.fetchProperty;
import static TEST_HARNESS.Util.readCSVLineByLine;
import java.io.IOException;
import java.util.List;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MorningStar extends Responses {
    private List<List<String>> isinList;

    public MorningStar(double fetchRate) {
        super(fetchRate);
        isinList = readCSVLineByLine(fetchProperty("ISIN_CSV"));
    }

    private void fetchOperationsData(String isin, Environment env) throws IOException {
        String environment = "";
        if (env == Environment.PROD) {
            environment = "prod";

        } else {
            environment = "stg";
        }
        OkHttpClient client = new OkHttpClient().newBuilder().build();
        Request request = new Request.Builder()
                .url("http://wallstreet." + environment + ".dreamplug.net/wallstreet/v1/morningstar/isin/" + isin + "/operations").method("GET", null)
                .build();
        try {
            Response response = client.newCall(request).execute();
            if (response.code() >= 200 && response.code() < 300) {
                saveResponse(response.body().bytes(), "operations_" + isin, ++primaryParserFetchCounter);
                Environment env_prod = Environment.PROD;
            } else {
                System.out.println("On " + env.toString() + " ResponseCode: " + response.code() + " for isin: " + isin);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void fetchPerfomanceData(String isin, Environment env) throws IOException {
        String environment = "";
        if (env == Environment.PROD) {
            environment = "prod";

        } else {
            environment = "stg";
        }
        OkHttpClient client = new OkHttpClient().newBuilder().build();
        Request request = new Request.Builder()
                .url("http://wallstreet." + environment + ".dreamplug.net/wallstreet/v1/morningstar/isin/" + isin + "/performance")
                .method("GET", null).build();
        try {
            Response response = client.newCall(request).execute();
            if (response.code() >= 200 && response.code() < 300) {
                saveResponse(response.body().bytes(), "perfomance_" + isin, ++primaryParserFetchCounter);
            } else {
                System.out.println("On " + env.toString() + " ResponseCode: " + response.code() + " for isin: " + isin);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void fetchRatingsData(String isin, Environment env) throws IOException {
        String environment = "";
        if (env == Environment.PROD) {
            environment = "prod";

        } else {
            environment = "stg";
        }
        OkHttpClient client = new OkHttpClient().newBuilder().build();
        Request request = new Request.Builder()
                .url("http://wallstreet." + environment + ".dreamplug.net/wallstreet/v1/morningstar/isin/" + isin + "/ratings").method("GET", null)
                .build();
        try {
            Response response = client.newCall(request).execute();
            if (response.code() >= 200 && response.code() < 300) {
                saveResponse(response.body().bytes(), "ratings_" + isin, ++primaryParserFetchCounter);
            } else {
                System.out.println("On " + env.toString() + " ResponseCode: " + response.code() + " for isin: " + isin);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void fetchPortfolioData(String isin, Environment env) throws IOException {
        String environment = "";
        if (env == Environment.PROD) {
            environment = "prod";

        } else {
            environment = "stg";
        }
        OkHttpClient client = new OkHttpClient().newBuilder().build();
        Request request = new Request.Builder()
                .url("http://wallstreet." + environment + ".dreamplug.net/wallstreet/v1/morningstar/isin/" + isin + "/portfolio").method("GET", null)
                .build();
        try {
            Response response = client.newCall(request).execute();
            if (response.code() >= 200 && response.code() < 300) {
                saveResponse(response.body().bytes(), "portfolio_" + isin, ++primaryParserFetchCounter);
            } else {
                System.out.println("On " + env.toString() + " ResponseCode: " + response.code() + " for isin: " + isin);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void fetchAllOperationsData(Environment env) throws IOException {
        setPrimaryParserFetchCounter(0);
        for (String isin : isinList.get(0)) {
            rateLimiter.acquire(1);
            fetchOperationsData(isin, env);
        }
    }

    public void fetchAllPerfomanceData(Environment env) throws IOException {
        setPrimaryParserFetchCounter(0);
        for (String isin : isinList.get(0)) {
            rateLimiter.acquire(1);
            fetchPerfomanceData(isin, env);
        }

    }

    public void fetchAllRatingsData(Environment env) throws IOException {
        setPrimaryParserFetchCounter(0);
        for (String isin : isinList.get(0)) {
            rateLimiter.acquire(1);
            fetchRatingsData(isin, env);
        }
    }

    public void fetchAllPortfolioData(Environment env) throws IOException {
        setPrimaryParserFetchCounter(0);
        for (String isin : isinList.get(0)) {
            rateLimiter.acquire(1);
            fetchPortfolioData(isin, env);
        }
    }
}
