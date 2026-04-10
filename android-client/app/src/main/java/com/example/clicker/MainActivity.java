package com.example.clicker;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    private static final int QUESTION_NO = 1;
    private static final int MAX_COMMENT_LENGTH = 240;
    private static final long STATUS_REFRESH_INTERVAL_MS = 5000L;

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable statusRefreshRunnable = new Runnable() {
        @Override
        public void run() {
            fetchPollStatus();
            handler.postDelayed(this, STATUS_REFRESH_INTERVAL_MS);
        }
    };
    private VoteApiClient voteApiClient;
    private CommentApiClient commentApiClient;
    private StatusApiClient statusApiClient;
    private Button btnA;
    private Button btnB;
    private Button btnC;
    private Button btnD;
    private Button btnCommentSubmit;
    private EditText edtComment;
    private TextView txtQuestion;
    private TextView txtPollStatus;
    private boolean acceptingResponses = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        voteApiClient = new VoteApiClient(BuildConfig.SERVER_BASE_URL);
        commentApiClient = new CommentApiClient(BuildConfig.SERVER_COMMENT_URL);
        statusApiClient = new StatusApiClient(BuildConfig.SERVER_STATUS_URL);
        txtQuestion = findViewById(R.id.txtQuestion);
        txtPollStatus = findViewById(R.id.txtPollStatus);
        btnA = findViewById(R.id.btnA);
        btnB = findViewById(R.id.btnB);
        btnC = findViewById(R.id.btnC);
        btnD = findViewById(R.id.btnD);
        btnCommentSubmit = findViewById(R.id.btnCommentSubmit);
        edtComment = findViewById(R.id.edtComment);
        setInputsEnabled(false);
        fetchPollStatus();
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchPollStatus();
        handler.removeCallbacks(statusRefreshRunnable);
        handler.postDelayed(statusRefreshRunnable, STATUS_REFRESH_INTERVAL_MS);
    }

    @Override
    protected void onPause() {
        handler.removeCallbacks(statusRefreshRunnable);
        super.onPause();
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

    public void btnCommentHandler(android.view.View view) {
        if (!acceptingResponses) {
            Toast.makeText(this, txtPollStatus.getText(), Toast.LENGTH_SHORT).show();
            return;
        }

        String comment = edtComment.getText().toString().trim();
        if (comment.isEmpty()) {
            Toast.makeText(this, R.string.comment_empty, Toast.LENGTH_SHORT).show();
            return;
        }

        if (comment.length() > MAX_COMMENT_LENGTH) {
            Toast.makeText(this, getString(R.string.comment_too_long, MAX_COMMENT_LENGTH), Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this, R.string.sending_comment, Toast.LENGTH_SHORT).show();
        executorService.execute(() -> {
            String result = commentApiClient.submitComment(QUESTION_NO, comment);
            runOnUiThread(() -> {
                if (isSuccessfulResult(result)) {
                    edtComment.setText("");
                }
                Toast.makeText(this, result, Toast.LENGTH_SHORT).show();
            });
        });
    }

    private void handleChoice(String choice) {
        if (!acceptingResponses) {
            Toast.makeText(this, txtPollStatus.getText(), Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this, R.string.sending_vote, Toast.LENGTH_SHORT).show();
        executorService.execute(() -> {
            String result = voteApiClient.submitChoice(QUESTION_NO, choice);
            runOnUiThread(() -> Toast.makeText(this, result, Toast.LENGTH_SHORT).show());
        });
    }

    private void fetchPollStatus() {
        txtPollStatus.setText(R.string.status_loading);
        executorService.execute(() -> {
            PollStatusResponse statusResponse = statusApiClient.fetchStatus(QUESTION_NO);
            runOnUiThread(() -> applyPollStatus(statusResponse));
        });
    }

    private void applyPollStatus(PollStatusResponse statusResponse) {
        if (!statusResponse.questionText.isEmpty()) {
            txtQuestion.setText(getString(R.string.question_template, QUESTION_NO, statusResponse.questionText));
        }

        txtPollStatus.setText(statusResponse.message);

        acceptingResponses = statusResponse.successful
                ? statusResponse.acceptingResponses
                : true;
        setInputsEnabled(acceptingResponses);
    }

    private void setInputsEnabled(boolean enabled) {
        btnA.setEnabled(enabled);
        btnB.setEnabled(enabled);
        btnC.setEnabled(enabled);
        btnD.setEnabled(enabled);
        btnCommentSubmit.setEnabled(enabled);
        edtComment.setEnabled(enabled);

        float alpha = enabled ? 1.0f : 0.45f;
        btnA.setAlpha(alpha);
        btnB.setAlpha(alpha);
        btnC.setAlpha(alpha);
        btnD.setAlpha(alpha);
        btnCommentSubmit.setAlpha(alpha);
        edtComment.setAlpha(alpha);
    }

    private boolean isSuccessfulResult(String result) {
        return !result.startsWith("Request failed") && !result.startsWith("Network error");
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacks(statusRefreshRunnable);
        executorService.shutdownNow();
        super.onDestroy();
    }
}
