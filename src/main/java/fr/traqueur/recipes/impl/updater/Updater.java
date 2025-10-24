package fr.traqueur.recipes.impl.updater;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.Properties;
import java.util.Scanner;
import java.util.logging.Logger;

/**
 * This class is used to check if the plugin is up to date
 */
public class Updater {

    /**
     * Check updates the plugin
     * @param name The name of the plugin
     */
    public static void update(String name) {
        new Updater(name).checkUpdates();
    }

    /**
     * The URL of the GitHub API
     */
    private static final String API_URL = "https://api.github.com/repos/Traqueur-dev/{name}/releases/latest";
    /**
     * The name of the plugin
     */
    private final String name;

    /**
     * Create a new Updater
     * @param name The name of the plugin
     */
    private Updater(String name) {
        this.name = name;
    }

    /**
     * Check if the plugin is up to date and log a warning if it's not
     */
    private void checkUpdates() {
        if(!this.isUpToDate()) {
            Logger.getLogger(name)
                    .warning("The API is not up to date, " +
                            "the latest version is " + this.fetchLatestVersion());
        }
    }

    /**
     * Get the version of the plugin
     * @return The version of the plugin
     */
    private String getVersion() {
        Properties prop = new Properties();
        try {
            prop.load(Updater.class.getClassLoader().getResourceAsStream("recipeapi.properties"));
            return prop.getProperty("version");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Check if the plugin is up to date
     * @return True if the plugin is up to date, false otherwise
     */
    private boolean isUpToDate() {
        try {
            String latestVersion = fetchLatestVersion();
            return getVersion().equals(latestVersion);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Get the latest version of the plugin
     * @return The latest version of the plugin
     */
    private String fetchLatestVersion() {
        try {
            URL url = URI.create(API_URL.replace("{name}", this.name)).toURL();
            String responseString = getString(url);
            int tagNameIndex = responseString.indexOf("\"tag_name\"");
            int start = responseString.indexOf('\"', tagNameIndex + 10) + 1;
            int end = responseString.indexOf('\"', start);
            return responseString.substring(start, end);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Get the latest version of the plugin
     * @return The latest version of the plugin
     */
    private String getString(URL url) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        StringBuilder response = new StringBuilder();
        try (Scanner scanner = new Scanner(connection.getInputStream())) {
            while (scanner.hasNext()) {
                response.append(scanner.nextLine());
            }
        } finally {
            connection.disconnect();
        }

        return response.toString();
    }
}