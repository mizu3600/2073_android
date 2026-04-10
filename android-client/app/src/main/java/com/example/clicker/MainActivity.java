package com.example.clicker;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    private static final int QUESTION_NO = 1;
    private static final int MAX_COMMENT_LENGTH = 240;

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private VoteApiClient voteApiClient;
    private CommentApiClient commentApiClient;
    private EditText edtComment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        voteApiClient = new VoteApiClient(BuildConfig.SERVER_BASE_URL);
        commentApiClient = new CommentApiClient(BuildConfig.SERVER_COMMENT_URL);
        edtComment = findViewById(R.id.edtComment);
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
        Toast.makeText(this, R.string.sending_vote, Toast.LENGTH_SHORT).show();
        executorService.execute(() -> {
            String result = voteApiClient.submitChoice(QUESTION_NO, choice);
            runOnUiThread(() -> Toast.makeText(this, result, Toast.LENGTH_SHORT).show());
        });
    }

    private boolean isSuccessfulResult(String result) {
        return !result.startsWith("Request failed") && !result.startsWith("Network error");
    }

    @Override
    protected void onDestroy() {
        executorService.shutdownNow();
        super.onDestroy();
    }
}
