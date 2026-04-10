package com.example.clicker.server;

import java.time.LocalDateTime;

public class CommentEntry {
    private final String commentText;
    private final LocalDateTime createdAt;

    public CommentEntry(String commentText, LocalDateTime createdAt) {
        this.commentText = commentText;
        this.createdAt = createdAt;
    }

    public String getCommentText() {
        return commentText;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
