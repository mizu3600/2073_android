package com.example.clicker.server;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Locale;
import java.util.Set;

@WebServlet("/select")
public class SelectServlet extends HttpServlet {
    private static final Set<String> VALID_CHOICES = Set.of("a", "b", "c", "d");

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/plain;charset=UTF-8");

        String choice = normaliseChoice(request.getParameter("choice"));
        int questionNo = parseQuestionNo(request.getParameter("questionNo"));

        try (PrintWriter out = response.getWriter()) {
            if (choice == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.println("Invalid choice. Use a, b, c, or d.");
                return;
            }

            String sql = "INSERT INTO responses (questionNo, choice) VALUES (?, ?)";
            try (Connection connection = DatabaseHelper.getConnection();
                 PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, questionNo);
                statement.setString(2, choice);
                statement.executeUpdate();
                out.printf("Vote submitted for question %d, choice %s.%n",
                        questionNo,
                        choice.toUpperCase(Locale.ENGLISH));
            } catch (SQLException e) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                out.println("Database error: " + e.getMessage());
            }
        }
    }

    private String normaliseChoice(String choice) {
        if (choice == null) {
            return null;
        }

        String normalised = choice.trim().toLowerCase(Locale.ENGLISH);
        return VALID_CHOICES.contains(normalised) ? normalised : null;
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
}
