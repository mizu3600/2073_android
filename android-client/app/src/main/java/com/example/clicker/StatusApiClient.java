package com.example.clicker;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class StatusApiClient {
    private final String statusUrl;

    public StatusApiClient(String statusUrl) {
        this.statusUrl = statusUrl;
    }

    public PollStatusResponse fetchStatus(int questionNo) {
        HttpURLConnection connection = null;

        try {
            URL url = new URL(statusUrl + "?questionNo=" + questionNo);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            int responseCode = connection.getResponseCode();
            String body = readResponseBody(connection, responseCode);
            JSONObject jsonObject = new JSONObject(body);
            return new PollStatusResponse(
                    jsonObject.optBoolean("success", responseCode == HttpURLConnection.HTTP_OK),
                    jsonObject.optBoolean("acceptingResponses", false),
                    jsonObject.optString("questionText", ""),
                    jsonObject.optString("status", "unknown"),
                    jsonObject.optString("message", body),
                    jsonObject.optString("startTime", ""),
                    jsonObject.optString("endTime", ""),
                    jsonObject.optString("serverTime", ""));
        } catch (Exception e) {
            return new PollStatusResponse(
                    false,
                    true,
                    "",
                    "unknown",
                    "Status unavailable: " + e.getMessage(),
                    "",
                    "",
                    "");
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
            return "{}";
        }

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            StringBuilder builder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
            return builder.toString().trim();
        }
    }
}
