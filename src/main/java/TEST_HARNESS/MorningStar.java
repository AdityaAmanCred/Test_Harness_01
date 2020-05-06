package TEST_HARNESS;

import static TEST_HARNESS.Util.fetchProperty;
import static TEST_HARNESS.Util.getPropertyFromFile;
import static TEST_HARNESS.Util.readCSVLineByLine;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MorningStar extends Responses {
    private List<String> isinList;

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
                saveAPIResponse(response.body().bytes(), "operations_" + isin, env, ++fetchCounter);
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
                saveAPIResponse(response.body().bytes(), "perfomance_" + isin, env, ++fetchCounter);
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
                saveAPIResponse(response.body().bytes(), "ratings_" + isin, env, ++fetchCounter);
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
                saveAPIResponse(response.body().bytes(), "portfolio_" + isin, env, ++fetchCounter);
            } else {
                System.out.println("On " + env.toString() + " ResponseCode: " + response.code() + " for isin: " + isin);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void fetchAllOperationsData(Environment env) throws IOException {
        for (String isin : isinList) {
            rateLimiter.acquire(1);
            fetchOperationsData(isin, env);
        }
    }

    public void fetchAllPerfomanceData(Environment env) throws IOException {
        for (String isin : isinList) {
            rateLimiter.acquire(1);
            fetchPerfomanceData(isin, env);
        }

    }

    public void fetchAllRatingsData(Environment env) throws IOException {
        for (String isin : isinList) {
            rateLimiter.acquire(1);
            fetchRatingsData(isin, env);
        }
    }

    public void fetchAllPortfolioData(Environment env) throws IOException {
        for (String isin : isinList) {
            rateLimiter.acquire(1);
            fetchPortfolioData(isin, env);
        }
    }

    public static void saveAPIResponse(byte[] bytes, String isin, Environment env, int fetchCounter) {
        java.io.File file;
        if (env == Environment.PROD) {
            file = new java.io.File(fetchProperty("EXPECTED_DIR") + isin + ".json");
        } else {
            file = new File(fetchProperty("STAGE_DIR") + isin + ".json");
        }

        try {
            OutputStream os = new FileOutputStream(file);

            os.write(bytes);
            if (env == Environment.PROD) {
                System.out.println("Prod response fetched for: " + isin + " fetchedCount = " + fetchCounter);
            } else {
                System.out.println("Stage response fetched for: " + isin + " fetchedCount = " + fetchCounter);
            }

            os.close();
        } catch (Exception e) {
            System.out.println(isin + " : Exception: " + e);
        }
    }
}
