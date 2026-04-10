package com.example.clicker.server;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@WebServlet("/status")
public class StatusServlet extends HttpServlet {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");

        int questionNo = parseQuestionNo(request.getParameter("questionNo"));
        QuestionSchedule schedule = DatabaseHelper.findQuestionSchedule(questionNo);

        try (PrintWriter out = response.getWriter()) {
            if (schedule == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.printf(Locale.ENGLISH,
                        "{\"success\":false,\"message\":\"Question %d not found.\"}%n",
                        questionNo);
                return;
            }

            LocalDateTime now = LocalDateTime.now();
            PollStatus status = schedule.getStatus(now);
            out.print(buildStatusJson(schedule, now, status));
        }
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

    private String buildStatusJson(QuestionSchedule schedule, LocalDateTime now, PollStatus status) {
        return String.format(Locale.ENGLISH,
                "{\"success\":true,\"questionNo\":%d,\"questionText\":\"%s\",\"status\":\"%s\",\"acceptingResponses\":%s,"
                        + "\"message\":\"%s\",\"startTime\":\"%s\",\"endTime\":\"%s\",\"serverTime\":\"%s\"}",
                schedule.getQuestionNo(),
                escapeJson(schedule.getQuestionText()),
                status.name().toLowerCase(Locale.ENGLISH),
                schedule.isOpen(now),
                escapeJson(buildStatusMessage(schedule, status)),
                FORMATTER.format(schedule.getStartTime()),
                FORMATTER.format(schedule.getEndTime()),
                FORMATTER.format(now));
    }

    private String buildStatusMessage(QuestionSchedule schedule, PollStatus status) {
        return switch (status) {
            case OPEN -> "Voting is open now.";
            case NOT_STARTED -> "Voting opens at " + FORMATTER.format(schedule.getStartTime()) + ".";
            case CLOSED -> "Voting closed at " + FORMATTER.format(schedule.getEndTime()) + ".";
        };
    }

    private String escapeJson(String value) {
        return value.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n");
    }
}
