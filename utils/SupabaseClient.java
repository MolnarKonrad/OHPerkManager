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

    /**
     * Konstruktor, amely beállítja a Supabase URL-t, API kulcsot és
     * inicializálja a HttpClient-et.
     *
     * @param url A Supabase projekt URL-je, pl.
     * "https://your-project.supabase.co"
     * @param apiKey Az anon vagy service API kulcs
     */
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

    /**
     * Végrehajt egy GET kérést a Supabase REST API felé az adott endpoint-tal.
     * A endpoint a Supabase URL-hez viszonyított útvonal, pl.
     * "/rest/v1/users?username=eq.someUser&select=*"
     *
     * @param endpoint A lekérdezendő endpoint
     * @return A válasz JSON szövegként
     * @throws IOException Ha hálózati hiba történik
     * @throws InterruptedException Ha a kérés megszakad
     */
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

    // Construct the endpoint for the specified table
        String endpoint = "/rest/v1/" + tableName;

    // Return the response from the GET request to the specified table
        return get(endpoint);
    }

    /**
     * Beszúr egy rekordot a megadott táblába a Supabase REST API segítségével.
     * A data map-et JSON objektummá alakítja, és POST kérést küld a Supabase
     * endpointjára.
     *
     * @param table A beszúrandó tábla neve, pl. "users"
     * @param data A beszúrandó adatokat tartalmazó Map (kulcs: oszlopnév,
     * érték: érték)
     * @return Egy CompletableFuture, amely true értéket ad vissza, ha a
     * beszúrás sikeres (HTTP státuszkód 201), különben false.
     */
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
