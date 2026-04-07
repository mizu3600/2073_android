package com.example.clicker;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private TextView txtResponse;
    private String serverBaseUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtResponse = findViewById(R.id.txtResponseId);
        serverBaseUrl = getString(R.string.server_base_url);
    }

    public void btnAHandler(android.view.View view) {
        submitChoice("a");
    }

    public void btnBHandler(android.view.View view) {
        submitChoice("b");
    }

    public void btnCHandler(android.view.View view) {
        submitChoice("c");
    }

    public void btnDHandler(android.view.View view) {
        submitChoice("d");
    }

    private void submitChoice(String choice) {
        txtResponse.setText(getString(R.string.sending_vote));
        executorService.execute(() -> {
            String requestUrl = serverBaseUrl + "?questionNo=1&choice=" + choice;
            String result = performGet(requestUrl);
            runOnUiThread(() -> txtResponse.setText(result));
        });
    }

    private String performGet(String requestUrl) {
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

    @Override
    protected void onDestroy() {
        executorService.shutdownNow();
        super.onDestroy();
    }
}
