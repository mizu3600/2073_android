package com.example.clicker;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

public class CommentApiClient {
    private final String commentUrl;

    public CommentApiClient(String commentUrl) {
        this.commentUrl = commentUrl;
    }

    public String submitComment(int questionNo, String comment) {
        HttpURLConnection connection = null;

        try {
            URL url = new URL(commentUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");

            String payload = "questionNo=" + questionNo
                    + "&comment=" + URLEncoder.encode(comment, StandardCharsets.UTF_8.name());

            try (OutputStream outputStream = connection.getOutputStream()) {
                outputStream.write(payload.getBytes(StandardCharsets.UTF_8));
            }

            int responseCode = connection.getResponseCode();
            String body = readResponseBody(connection, responseCode);
            if (responseCode == HttpURLConnection.HTTP_OK) {
                return String.format(Locale.ENGLISH, "Comment submitted. HTTP %d\n%s", responseCode, body);
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
