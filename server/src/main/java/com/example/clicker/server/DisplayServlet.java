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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

@WebServlet("/display")
public class DisplayServlet extends HttpServlet {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        int questionNo = parseQuestionNo(request.getParameter("questionNo"));

        Map<String, Integer> counts = new LinkedHashMap<>();
        counts.put("a", 0);
        counts.put("b", 0);
        counts.put("c", 0);
        counts.put("d", 0);

        String errorMessage = null;
        String sql = "SELECT choice, COUNT(*) AS total FROM responses WHERE questionNo = ? GROUP BY choice";

        try (Connection connection = DatabaseHelper.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, questionNo);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    String choice = resultSet.getString("choice");
                    int total = resultSet.getInt("total");
                    if (choice != null && counts.containsKey(choice.toLowerCase(Locale.ENGLISH))) {
                        counts.put(choice.toLowerCase(Locale.ENGLISH), total);
                    }
                }
            }
        } catch (SQLException e) {
            errorMessage = e.getMessage();
        }

        int totalVotes = counts.values().stream().mapToInt(Integer::intValue).sum();

        try (PrintWriter out = response.getWriter()) {
            out.println("<!DOCTYPE html>");
            out.println("<html lang=\"en\">");
            out.println("<head>");
            out.println("<meta charset=\"UTF-8\">");
            out.println("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">");
            out.println("<title>Clicker Results</title>");
            out.println("<style>");
            out.println("body{font-family:Arial,sans-serif;margin:32px;background:#f5f7fb;color:#17202a;}");
            out.println(".card{max-width:720px;margin:0 auto;background:#fff;padding:24px;border-radius:16px;box-shadow:0 12px 30px rgba(0,0,0,0.08);}");
            out.println("table{width:100%;border-collapse:collapse;margin-top:16px;}");
            out.println("th,td{padding:12px;border-bottom:1px solid #e6ebf2;text-align:left;}");
            out.println(".bar-wrap{background:#e9eef5;border-radius:999px;height:16px;overflow:hidden;}");
            out.println(".bar{background:#0b5cad;height:100%;}");
            out.println(".meta{color:#5b6470;font-size:14px;}");
            out.println(".error{margin-top:16px;padding:12px;background:#fdecea;color:#a52714;border-radius:10px;}");
            out.println("a{color:#0b5cad;text-decoration:none;font-weight:600;}");
            out.println("</style>");
            out.println("</head>");
            out.println("<body>");
            out.println("<div class=\"card\">");
            out.printf("<h2>Q%d. Who is the coolest Marvel Hero?</h2>%n", questionNo);
            out.printf("<p class=\"meta\">Last refreshed: %s</p>%n", FORMATTER.format(LocalDateTime.now()));
            out.printf("<p class=\"meta\">Total votes: %d</p>%n", totalVotes);
            out.println("<table>");
            out.println("<thead><tr><th>Choice</th><th>Votes</th><th>Visual</th></tr></thead>");
            out.println("<tbody>");

            for (Map.Entry<String, Integer> entry : counts.entrySet()) {
                int percent = totalVotes == 0 ? 0 : (entry.getValue() * 100 / totalVotes);
                out.println("<tr>");
                out.printf("<td>%s</td>%n", entry.getKey().toUpperCase(Locale.ENGLISH));
                out.printf("<td>%d</td>%n", entry.getValue());
                out.printf("<td><div class=\"bar-wrap\"><div class=\"bar\" style=\"width:%d%%\"></div></div></td>%n", percent);
                out.println("</tr>");
            }

            out.println("</tbody>");
            out.println("</table>");
            out.printf("<p class=\"meta\" style=\"margin-top:16px;\"><a href=\"display?questionNo=%d\">Refresh</a></p>%n", questionNo);

            if (errorMessage != null) {
                out.printf("<div class=\"error\">Database error: %s</div>%n", escapeHtml(errorMessage));
            }

            out.println("</div>");
            out.println("</body>");
            out.println("</html>");
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

    private String escapeHtml(String value) {
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
