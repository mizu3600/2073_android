package com.example.clicker;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

public class VoteApiClient {
    private final String serverBaseUrl;

    public VoteApiClient(String serverBaseUrl) {
        this.serverBaseUrl = serverBaseUrl;
    }

    public String submitChoice(int questionNo, String choice) {
        String requestUrl = serverBaseUrl + "?questionNo=" + questionNo + "&choice=" + choice;
        HttpURLConnection connection = null;

        try {
            URL url = new URL(requestUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            int responseCode = connection.getResponseCode();
            String body = readResponseBody(connection, responseCode);
            if (responseCode == HttpURLConnection.HTTP_OK) {
                return String.format(Locale.ENGLISH, "Vote submitted. HTTP %d\n%s", responseCode, body);
            }
            return String.format(Locale.ENGLISH, "Request failed. HTTP %d\n%s", responseCode, body);
        } catch (IOException e) {
            return "Network error: " + e.getMessage();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private String readResponseBody(HttpURLConnection connection, int responseCode) throws IOException {
        InputStream stream = responseCode >= HttpURLConnection.HTTP_BAD_REQUEST
                ? connection.getErrorStream()
                : connection.getInputStream();

        if (stream == null) {
            return "No response body returned.";
        }

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            StringBuilder builder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line).append('\n');
            }
            return builder.toString().trim();
        }
    }
}
