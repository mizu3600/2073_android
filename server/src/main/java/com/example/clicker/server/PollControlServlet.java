package com.example.clicker.server;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

@WebServlet("/control")
public class PollControlServlet extends HttpServlet {
    private static final int DEFAULT_DURATION_SECONDS = 180;
    private static final int MAX_DURATION_SECONDS = 3600;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        int questionNo = parseQuestionNo(request.getParameter("questionNo"));
        String action = request.getParameter("action");

        QuestionSchedule schedule;
        try {
            schedule = DatabaseHelper.findQuestionSchedule(questionNo);
        } catch (IllegalStateException e) {
            redirect(response, questionNo, e.getMessage());
            return;
        }

        if (schedule == null) {
            redirect(response, questionNo, "Question not found.");
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        PollStatus status = schedule.getStatus(now);

        try {
            if ("start".equals(action)) {
                handleStart(request, response, questionNo, status, now);
                return;
            }

            if ("end".equals(action)) {
                handleEnd(response, questionNo, status, now);
                return;
            }

            redirect(response, questionNo, "Unknown action.");
        } catch (IllegalStateException e) {
            redirect(response, questionNo, e.getMessage());
        }
    }

    private void handleStart(
            HttpServletRequest request,
            HttpServletResponse response,
            int questionNo,
            PollStatus status,
            LocalDateTime now) throws IOException {
        if (status == PollStatus.OPEN) {
            redirect(response, questionNo, "Poll is already open.");
            return;
        }

        int durationSeconds = parseDurationSeconds(request.getParameter("durationSeconds"));
        LocalDateTime endTime = now.plusSeconds(durationSeconds);
        DatabaseHelper.startPoll(questionNo, now, endTime);
        redirect(response, questionNo, "Poll started.");
    }

    private void handleEnd(
            HttpServletResponse response,
            int questionNo,
            PollStatus status,
            LocalDateTime now) throws IOException {
        if (status != PollStatus.OPEN) {
            redirect(response, questionNo, "Poll is not open.");
            return;
        }

        DatabaseHelper.endPoll(questionNo, now);
        redirect(response, questionNo, "Poll ended.");
    }

    private int parseQuestionNo(String questionNoParam) {
        if (questionNoParam == null || questionNoParam.isBlank()) {
            return 1;
        }

        try {
            return Integer.parseInt(questionNoParam);
        } catch (NumberFormatException ignored) {
            return 1;
        }
    }

    private int parseDurationSeconds(String durationParam) {
        if (durationParam == null || durationParam.isBlank()) {
            return DEFAULT_DURATION_SECONDS;
        }

        try {
            int parsed = Integer.parseInt(durationParam);
            if (parsed <= 0) {
                return DEFAULT_DURATION_SECONDS;
            }
            return Math.min(parsed, MAX_DURATION_SECONDS);
        } catch (NumberFormatException ignored) {
            return DEFAULT_DURATION_SECONDS;
        }
    }

    private void redirect(HttpServletResponse response, int questionNo, String message) throws IOException {
        String encoded = URLEncoder.encode(message, StandardCharsets.UTF_8);
        response.sendRedirect("display?questionNo=" + questionNo + "&notice=" + encoded);
    }
}
