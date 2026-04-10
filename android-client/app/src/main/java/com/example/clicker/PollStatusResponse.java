package com.example.clicker;

public class PollStatusResponse {
    public final boolean successful;
    public final boolean acceptingResponses;
    public final String questionText;
    public final String status;
    public final String message;
    public final String startTime;
    public final String endTime;
    public final String serverTime;

    public PollStatusResponse(
            boolean successful,
            boolean acceptingResponses,
            String questionText,
            String status,
            String message,
            String startTime,
            String endTime,
            String serverTime) {
        this.successful = successful;
        this.acceptingResponses = acceptingResponses;
        this.questionText = questionText;
        this.status = status;
        this.message = message;
        this.startTime = startTime;
        this.endTime = endTime;
        this.serverTime = serverTime;
    }
}
