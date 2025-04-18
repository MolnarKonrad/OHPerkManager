package utils;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import org.json.JSONObject;

public class SupabaseClient {

    private String url;
    private String apiKey;
    private HttpClient httpClient;

   public SupabaseClient(String url, String apiKey) {
        this.url = url;
        this.apiKey = apiKey;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    public String getUrl() {
        return url;
    }

    public String getApiKey() {
        return apiKey;
    }

   public String get(String endpoint) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url + endpoint))
                .header("apikey", apiKey)
                .header("Authorization", "Bearer " + apiKey)
                .header("Accept", "application/json")
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }

    public String from(String tableName) throws IOException, InterruptedException {
        String endpoint = "/rest/v1/" + tableName;
        return get(endpoint);
    }

    public CompletableFuture<Boolean> insertUser(Map<String, Object> data) {
        JSONObject json = new JSONObject(data);
        String endpoint = "/rest/v1/users";
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url + endpoint))
                .header("apikey", apiKey)
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(10))
                .POST(HttpRequest.BodyPublishers.ofString(json.toString()))
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> response.statusCode() == 201);
    }
}
