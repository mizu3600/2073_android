package com.example.clicker;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

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
        voteApiClient = new VoteApiClient(BuildConfig.SERVER_BASE_URL);
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
