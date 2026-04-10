package com.example.clicker.server;

import java.time.LocalDateTime;

public class QuestionSchedule {
    private final int questionNo;
    private final String questionText;
    private final LocalDateTime startTime;
    private final LocalDateTime endTime;

    public QuestionSchedule(int questionNo, String questionText, LocalDateTime startTime, LocalDateTime endTime) {
        this.questionNo = questionNo;
        this.questionText = questionText;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public int getQuestionNo() {
        return questionNo;
    }

    public String getQuestionText() {
        return questionText;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public PollStatus getStatus(LocalDateTime now) {
        if (now.isBefore(startTime)) {
            return PollStatus.NOT_STARTED;
        }
        if (now.isAfter(endTime)) {
            return PollStatus.CLOSED;
        }
        return PollStatus.OPEN;
    }

    public boolean isOpen(LocalDateTime now) {
        return getStatus(now) == PollStatus.OPEN;
    }
}
