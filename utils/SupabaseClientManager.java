package utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class SupabaseClientManager {

    private static SupabaseClient instance;

    private SupabaseClientManager() {
        // Private constructor to prevent instantiation from outside
    }

    public static synchronized SupabaseClient getInstance() {
        if (instance == null) {
            Properties properties = new Properties();
            try (InputStream input = SupabaseClientManager.class.getResourceAsStream("/config.properties")) {
                if (input == null) {
                    System.out.println("Sorry, unable to find config.properties");
                    return null; // Handle the error as needed
                }
                properties.load(input);
                String projectRef = properties.getProperty("supabase.projectRef");
                String apiKey = properties.getProperty("supabase.apiKey");
                String url = "https://" + projectRef + ".supabase.co";

                instance = new SupabaseClient(url, apiKey);
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException("Failed to load configuration properties.");
            }
        }
        return instance;
    }
}
