package com.example.clicker.server;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;

@WebServlet("/comment")
public class CommentServlet extends HttpServlet {
    private static final int MAX_COMMENT_LENGTH = 240;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("text/plain;charset=UTF-8");

        int questionNo = parseQuestionNo(request.getParameter("questionNo"));
        String comment = normaliseComment(request.getParameter("comment"));

        try (PrintWriter out = response.getWriter()) {
            if (comment == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.println("Comment cannot be empty.");
                return;
            }

            if (comment.length() > MAX_COMMENT_LENGTH) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.printf("Comment is too long. Maximum %d characters.%n", MAX_COMMENT_LENGTH);
                return;
            }

            try {
                DatabaseHelper.insertComment(questionNo, comment);
                out.printf("Comment submitted for question %d.%n", questionNo);
            } catch (SQLException e) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                out.println("Database error: " + e.getMessage());
            }
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/plain;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        response.getWriter().println("Use POST to submit comments.");
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

    private String normaliseComment(String comment) {
        if (comment == null) {
            return null;
        }

        String normalised = comment.trim();
        return normalised.isEmpty() ? null : normalised;
    }
}
