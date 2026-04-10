package com.example.clicker.ui;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.clicker.R;
import com.example.clicker.config.AppConfig;
import com.example.clicker.network.VoteApiClient;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    private static final int QUESTION_NO = 1;

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private TextView txtResponse;
    private VoteApiClient voteApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtResponse = findViewById(R.id.txtResponseId);
        voteApiClient = new VoteApiClient(AppConfig.getServerBaseUrl());
    }

    public void btnAHandler(android.view.View view) {
        handleChoice("a");
    }

    public void btnBHandler(android.view.View view) {
        handleChoice("b");
    }

    public void btnCHandler(android.view.View view) {
        handleChoice("c");
    }

    public void btnDHandler(android.view.View view) {
        handleChoice("d");
    }

    private void handleChoice(String choice) {
        txtResponse.setText(getString(R.string.sending_vote));
        executorService.execute(() -> {
            String result = voteApiClient.submitChoice(QUESTION_NO, choice);
            runOnUiThread(() -> txtResponse.setText(result));
        });
    }

    @Override
    protected void onDestroy() {
        executorService.shutdownNow();
        super.onDestroy();
    }
}
