package com.example.clicker.server.web;

import com.example.clicker.server.service.VoteSummary;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;

public class ResultsPageRenderer {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public String render(VoteSummary summary, String errorMessage) {
        StringBuilder html = new StringBuilder();
        int totalVotes = summary.getTotalVotes();

        html.append("<!DOCTYPE html>\n");
        html.append("<html lang=\"en\">\n");
        html.append("<head>\n");
        html.append("<meta charset=\"UTF-8\">\n");
        html.append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n");
        html.append("<title>Clicker Results</title>\n");
        html.append("<style>\n");
        html.append("body{font-family:Arial,sans-serif;margin:32px;background:#f5f7fb;color:#17202a;}\n");
        html.append(".card{max-width:720px;margin:0 auto;background:#fff;padding:24px;border-radius:16px;box-shadow:0 12px 30px rgba(0,0,0,0.08);}\n");
        html.append("table{width:100%;border-collapse:collapse;margin-top:16px;}\n");
        html.append("th,td{padding:12px;border-bottom:1px solid #e6ebf2;text-align:left;}\n");
        html.append(".bar-wrap{background:#e9eef5;border-radius:999px;height:16px;overflow:hidden;}\n");
        html.append(".bar{background:#0b5cad;height:100%;}\n");
        html.append(".meta{color:#5b6470;font-size:14px;}\n");
        html.append(".error{margin-top:16px;padding:12px;background:#fdecea;color:#a52714;border-radius:10px;}\n");
        html.append("a{color:#0b5cad;text-decoration:none;font-weight:600;}\n");
        html.append("</style>\n");
        html.append("</head>\n");
        html.append("<body>\n");
        html.append("<div class=\"card\">\n");
        html.append(String.format(Locale.ENGLISH, "<h2>Q%d. Who is the coolest Marvel Hero?</h2>%n", summary.getQuestionNo()));
        html.append(String.format(Locale.ENGLISH, "<p class=\"meta\">Last refreshed: %s</p>%n", FORMATTER.format(LocalDateTime.now())));
        html.append(String.format(Locale.ENGLISH, "<p class=\"meta\">Total votes: %d</p>%n", totalVotes));
        html.append("<table>\n");
        html.append("<thead><tr><th>Choice</th><th>Votes</th><th>Visual</th></tr></thead>\n");
        html.append("<tbody>\n");

        for (Map.Entry<String, Integer> entry : summary.getCounts().entrySet()) {
            int percent = totalVotes == 0 ? 0 : (entry.getValue() * 100 / totalVotes);
            html.append("<tr>\n");
            html.append(String.format(Locale.ENGLISH, "<td>%s</td>%n", entry.getKey().toUpperCase(Locale.ENGLISH)));
            html.append(String.format(Locale.ENGLISH, "<td>%d</td>%n", entry.getValue()));
            html.append(String.format(Locale.ENGLISH,
                    "<td><div class=\"bar-wrap\"><div class=\"bar\" style=\"width:%d%%\"></div></div></td>%n",
                    percent));
            html.append("</tr>\n");
        }

        html.append("</tbody>\n");
        html.append("</table>\n");
        html.append(String.format(Locale.ENGLISH,
                "<p class=\"meta\" style=\"margin-top:16px;\"><a href=\"display?questionNo=%d\">Refresh</a></p>%n",
                summary.getQuestionNo()));

        if (errorMessage != null) {
            html.append(String.format(Locale.ENGLISH,
                    "<div class=\"error\">Database error: %s</div>%n",
                    escapeHtml(errorMessage)));
        }

        html.append("</div>\n");
        html.append("</body>\n");
        html.append("</html>\n");
        return html.toString();
    }

    private String escapeHtml(String value) {
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
