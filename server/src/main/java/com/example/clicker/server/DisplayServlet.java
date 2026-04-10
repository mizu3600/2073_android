package com.example.clicker.server;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Comparator;

@WebServlet("/display")
public class DisplayServlet extends HttpServlet {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final List<String> CHOICE_ORDER = List.of("a", "b", "c", "d");
    private static final String QUESTION_TEXT = "Who is the coolest Marvel Hero?";
    private static final Map<String, String> CHOICE_COLORS = Map.of(
            "a", "#0b5cad",
            "b", "#f08c2e",
            "c", "#18a999",
            "d", "#c44569"
    );

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        int questionNo = parseQuestionNo(request.getParameter("questionNo"));
        Map<String, Integer> counts = defaultCounts();
        List<CommentEntry> comments = List.of();
        List<String> errors = new ArrayList<>();

        try {
            counts.putAll(DatabaseHelper.countResponsesByChoice(questionNo));
        } catch (SQLException e) {
            errors.add("Vote counts unavailable: " + e.getMessage());
        }

        try {
            comments = DatabaseHelper.listRecentComments(questionNo, 10);
        } catch (SQLException e) {
            errors.add("Comments unavailable: " + e.getMessage());
        }

        try (PrintWriter out = response.getWriter()) {
            out.print(renderPage(
                    questionNo,
                    counts,
                    comments,
                    errors.isEmpty() ? null : String.join(" ", errors)));
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

    private Map<String, Integer> defaultCounts() {
        LinkedHashMap<String, Integer> counts = new LinkedHashMap<>();
        for (String choice : CHOICE_ORDER) {
            counts.put(choice, 0);
        }
        return counts;
    }

    private String renderPage(int questionNo, Map<String, Integer> counts, List<CommentEntry> comments, String errorMessage) {
        StringBuilder html = new StringBuilder();
        int totalVotes = counts.values().stream().mapToInt(Integer::intValue).sum();
        String leadingChoice = counts.entrySet().stream()
                .max(Comparator.comparingInt(Map.Entry::getValue))
                .filter(entry -> entry.getValue() > 0)
                .map(entry -> entry.getKey().toUpperCase(Locale.ENGLISH))
                .orElse("None");
        String leadingText = "None".equals(leadingChoice)
                ? "Waiting for the first vote"
                : "Choice " + leadingChoice + " is leading";

        html.append("<!DOCTYPE html>\n");
        html.append("<html lang=\"en\">\n");
        html.append("<head>\n");
        html.append("<meta charset=\"UTF-8\">\n");
        html.append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n");
        html.append("<title>Clicker Results</title>\n");
        html.append("<style>\n");
        html.append(":root{color-scheme:light;--bg:#f5f5f3;--panel:#ffffff;--ink:#111111;--muted:#666666;--line:#dddddd;--accent:#111111;--shadow:none;}\n");
        html.append("*{box-sizing:border-box;}\n");
        html.append("body{margin:0;font-family:Arial,sans-serif;background:var(--bg);color:var(--ink);}\n");
        html.append(".shell{max-width:960px;margin:0 auto;padding:32px 20px 48px;}\n");
        html.append(".hero{background:var(--panel);border:1px solid var(--line);border-radius:16px;padding:24px;position:relative;overflow:hidden;}\n");
        html.append(".hero::after{display:none;}\n");
        html.append(".eyebrow{margin:0 0 8px;font-size:12px;letter-spacing:0.08em;text-transform:uppercase;color:var(--muted);}\n");
        html.append(".hero h1{margin:0 0 10px;font-size:clamp(28px,4vw,36px);line-height:1.1;max-width:640px;font-weight:700;}\n");
        html.append(".hero p{margin:0;color:var(--muted);font-size:15px;max-width:560px;}\n");
        html.append(".stats{display:grid;grid-template-columns:repeat(auto-fit,minmax(180px,1fr));gap:12px;margin-top:20px;}\n");
        html.append(".stat-card{background:var(--panel);border:1px solid var(--line);border-radius:14px;padding:16px;}\n");
        html.append(".stat-label{display:block;font-size:12px;color:var(--muted);margin-bottom:8px;text-transform:uppercase;letter-spacing:0.08em;}\n");
        html.append(".stat-value{display:block;font-size:28px;font-weight:700;}\n");
        html.append(".stat-note{display:block;margin-top:8px;font-size:14px;color:var(--muted);}\n");
        html.append(".panel{margin-top:16px;background:var(--panel);border:1px solid var(--line);border-radius:16px;padding:24px;}\n");
        html.append(".panel-head{display:flex;justify-content:space-between;gap:16px;align-items:flex-start;flex-wrap:wrap;margin-bottom:20px;}\n");
        html.append(".panel-head h2{margin:0;font-size:24px;}\n");
        html.append(".meta{color:var(--muted);font-size:14px;}\n");
        html.append(".toggle-row{display:flex;gap:10px;flex-wrap:wrap;}\n");
        html.append(".toggle-btn{border:1px solid var(--line);border-radius:999px;padding:10px 16px;font-size:14px;font-weight:600;background:#ffffff;color:var(--muted);cursor:pointer;transition:all 0.2s ease;}\n");
        html.append(".toggle-btn.active{background:var(--accent);color:#ffffff;border-color:var(--accent);}\n");
        html.append(".chart-view{display:none;}\n");
        html.append(".chart-view.active{display:block;}\n");
        html.append(".summary-grid{display:grid;grid-template-columns:minmax(0,2fr) minmax(280px,1fr);gap:20px;align-items:start;}\n");
        html.append(".chart-card,.table-card{border:1px solid var(--line);border-radius:14px;padding:20px;background:#ffffff;}\n");
        html.append(".chart-title{margin:0 0 18px;font-size:18px;}\n");
        html.append(".column-chart{display:grid;grid-template-columns:repeat(4,minmax(0,1fr));gap:18px;align-items:end;min-height:340px;padding:10px 0 4px;}\n");
        html.append(".column-item{text-align:center;}\n");
        html.append(".column-track{height:260px;border-radius:12px;background:#f0f0ee;display:flex;align-items:flex-end;padding:10px;}\n");
        html.append(".column-bar{width:100%;border-radius:8px 8px 4px 4px;min-height:14px;}\n");
        html.append(".column-value{display:block;margin-bottom:10px;font-size:22px;font-weight:700;}\n");
        html.append(".column-label{display:block;margin-top:12px;font-size:14px;font-weight:700;letter-spacing:0.08em;color:var(--muted);}\n");
        html.append(".column-percent{display:block;margin-top:6px;font-size:13px;color:var(--muted);}\n");
        html.append(".bar-list{display:grid;gap:16px;}\n");
        html.append(".bar-item{display:grid;grid-template-columns:60px minmax(0,1fr) 72px;gap:14px;align-items:center;}\n");
        html.append(".bar-item strong{font-size:16px;letter-spacing:0.08em;}\n");
        html.append(".bar-track{height:20px;border-radius:999px;background:#f0f0ee;padding:3px;}\n");
        html.append(".bar-fill{height:100%;border-radius:999px;min-width:14px;}\n");
        html.append(".bar-count{text-align:right;font-size:14px;color:var(--muted);font-weight:700;}\n");
        html.append(".pie-layout{display:grid;grid-template-columns:minmax(280px,360px) minmax(0,1fr);gap:24px;align-items:center;}\n");
        html.append(".pie-chart{width:min(100%,320px);aspect-ratio:1;border-radius:50%;margin:0 auto;position:relative;border:1px solid var(--line);}\n");
        html.append(".pie-chart::after{content:'';position:absolute;inset:24%;background:#fff;border-radius:50%;border:1px solid var(--line);}\n");
        html.append(".pie-center{position:absolute;inset:0;display:flex;flex-direction:column;align-items:center;justify-content:center;z-index:1;}\n");
        html.append(".pie-total{font-size:34px;font-weight:700;line-height:1;}\n");
        html.append(".pie-caption{margin-top:8px;font-size:13px;letter-spacing:0.08em;color:var(--muted);text-transform:uppercase;}\n");
        html.append(".legend{display:grid;gap:12px;}\n");
        html.append(".legend-item{display:flex;justify-content:space-between;align-items:center;padding:12px 14px;border-radius:12px;background:#ffffff;border:1px solid var(--line);gap:12px;}\n");
        html.append(".legend-left{display:flex;align-items:center;gap:10px;font-weight:700;}\n");
        html.append(".swatch{width:14px;height:14px;border-radius:50%;display:inline-block;}\n");
        html.append(".legend-meta{font-size:14px;color:var(--muted);}\n");
        html.append("table{width:100%;border-collapse:collapse;}\n");
        html.append("th,td{padding:12px 0;border-bottom:1px solid var(--line);text-align:left;}\n");
        html.append("th{font-size:13px;letter-spacing:0.08em;text-transform:uppercase;color:var(--muted);}\n");
        html.append("td:last-child,th:last-child{text-align:right;}\n");
        html.append(".choice-pill{display:inline-flex;align-items:center;gap:8px;font-weight:700;}\n");
        html.append(".choice-dot{width:10px;height:10px;border-radius:50%;display:inline-block;}\n");
        html.append(".footer-meta{display:flex;justify-content:space-between;gap:12px;flex-wrap:wrap;margin-top:18px;font-size:14px;color:var(--muted);padding-top:16px;border-top:1px solid var(--line);}\n");
        html.append(".footer-meta a{color:var(--accent);text-decoration:none;font-weight:700;}\n");
        html.append(".comment-list{display:grid;gap:12px;}\n");
        html.append(".comment-item{border:1px solid var(--line);border-radius:14px;padding:16px;background:#ffffff;}\n");
        html.append(".comment-time{display:block;margin-bottom:8px;font-size:12px;letter-spacing:0.06em;text-transform:uppercase;color:var(--muted);}\n");
        html.append(".comment-text{margin:0;font-size:15px;line-height:1.55;white-space:pre-wrap;word-break:break-word;}\n");
        html.append(".empty-state{border:1px dashed var(--line);border-radius:14px;padding:18px;color:var(--muted);background:#ffffff;}\n");
        html.append(".error{margin-top:16px;padding:14px 16px;background:#faf4f4;color:#8a2d2d;border:1px solid #e5caca;border-radius:12px;}\n");
        html.append("@media (max-width:860px){.summary-grid,.pie-layout{grid-template-columns:1fr;}.panel{padding:18px;}.hero{padding:20px;}.column-chart{min-height:300px;gap:12px;}.column-track{height:220px;}}\n");
        html.append("@media (max-width:560px){.shell{padding:22px 14px 36px;}.bar-item{grid-template-columns:48px minmax(0,1fr);}.bar-count{grid-column:1 / -1;text-align:left;}.toggle-btn{flex:1 1 100%;}}\n");
        html.append("</style>\n");
        html.append("</head>\n");
        html.append("<body>\n");
        html.append("<div class=\"shell\">\n");
        html.append("<section class=\"hero\">\n");
        html.append("<p class=\"eyebrow\">IM2073 Live Results</p>\n");
        html.append(String.format(Locale.ENGLISH, "<h1>Q%d. %s</h1>%n", questionNo, QUESTION_TEXT));
        html.append("<p>Clean live results view with switchable chart styles.</p>\n");
        html.append("<div class=\"stats\">\n");
        html.append("<div class=\"stat-card\"><span class=\"stat-label\">Total Votes</span>");
        html.append(String.format(Locale.ENGLISH, "<span class=\"stat-value\">%d</span>", totalVotes));
        html.append("<span class=\"stat-note\">Responses collected so far</span></div>\n");
        html.append("<div class=\"stat-card\"><span class=\"stat-label\">Leading Choice</span>");
        html.append(String.format(Locale.ENGLISH, "<span class=\"stat-value\">%s</span>", escapeHtml(leadingChoice)));
        html.append(String.format(Locale.ENGLISH, "<span class=\"stat-note\">%s</span></div>%n", escapeHtml(leadingText)));
        html.append("<div class=\"stat-card\"><span class=\"stat-label\">Last Refreshed</span>");
        html.append(String.format(Locale.ENGLISH, "<span class=\"stat-value\">%s</span>", FORMATTER.format(LocalDateTime.now())));
        html.append("<span class=\"stat-note\">Local server time</span></div>\n");
        html.append("</div>\n");
        html.append("</section>\n");
        html.append("<section class=\"panel\">\n");
        html.append("<div class=\"panel-head\">\n");
        html.append("<div><h2>Visualization</h2><p class=\"meta\">Switch between horizontal bar, vertical column, and pie chart views.</p></div>\n");
        html.append("<div class=\"toggle-row\">\n");
        html.append("<button class=\"toggle-btn\" type=\"button\" data-chart=\"bar\">Bar Chart</button>\n");
        html.append("<button class=\"toggle-btn active\" type=\"button\" data-chart=\"column\">Column Chart</button>\n");
        html.append("<button class=\"toggle-btn\" type=\"button\" data-chart=\"pie\">Pie Chart</button>\n");
        html.append("</div>\n");
        html.append("</div>\n");
        html.append("<div class=\"summary-grid\">\n");
        html.append("<div class=\"chart-card\">\n");
        html.append("<div class=\"chart-view\" data-chart-view=\"bar\">\n");
        html.append("<h3 class=\"chart-title\">Bar Chart</h3>\n");
        html.append("<div class=\"bar-list\">\n");
        appendBarChart(html, counts, totalVotes);
        html.append("</div>\n");
        html.append("</div>\n");
        html.append("<div class=\"chart-view active\" data-chart-view=\"column\">\n");
        html.append("<h3 class=\"chart-title\">Column Chart</h3>\n");
        html.append("<div class=\"column-chart\">\n");
        appendColumnChart(html, counts, totalVotes);
        html.append("</div>\n");
        html.append("</div>\n");
        html.append("<div class=\"chart-view\" data-chart-view=\"pie\">\n");
        html.append("<h3 class=\"chart-title\">Pie Chart</h3>\n");
        appendPieChart(html, counts, totalVotes);
        html.append("</div>\n");
        html.append("</div>\n");
        html.append("<div class=\"table-card\">\n");
        html.append("<h3 class=\"chart-title\">Vote Breakdown</h3>\n");
        html.append("<table>\n");
        html.append("<thead><tr><th>Choice</th><th>Votes</th><th>Share</th></tr></thead>\n");
        html.append("<tbody>\n");
        appendVoteRows(html, counts, totalVotes);
        html.append("</tbody>\n");
        html.append("</table>\n");
        html.append("</div>\n");
        html.append("</div>\n");
        html.append(String.format(Locale.ENGLISH,
                "<div class=\"footer-meta\"><span>Question %d results</span><a href=\"display?questionNo=%d\">Refresh Data</a></div>%n",
                questionNo,
                questionNo));

        if (errorMessage != null) {
            html.append(String.format(Locale.ENGLISH,
                    "<div class=\"error\">Database error: %s</div>%n",
                    escapeHtml(errorMessage)));
        }

        html.append("</section>\n");

        html.append("<section class=\"panel\">\n");
        html.append("<div class=\"panel-head\">\n");
        html.append("<div><h2>Recent Comments</h2><p class=\"meta\">Latest anonymous comments from students.</p></div>\n");
        html.append("</div>\n");
        appendComments(html, comments);
        html.append("</section>\n");

        html.append("</div>\n");
        html.append("<script>\n");
        html.append("const toggleButtons=document.querySelectorAll('[data-chart]');\n");
        html.append("const chartViews=document.querySelectorAll('[data-chart-view]');\n");
        html.append("toggleButtons.forEach((button)=>{button.addEventListener('click',()=>{const target=button.dataset.chart;toggleButtons.forEach((item)=>item.classList.toggle('active',item===button));chartViews.forEach((view)=>view.classList.toggle('active',view.dataset.chartView===target));});});\n");
        html.append("</script>\n");
        html.append("</body>\n");
        html.append("</html>\n");
        return html.toString();
    }

    private void appendBarChart(StringBuilder html, Map<String, Integer> counts, int totalVotes) {
        for (Map.Entry<String, Integer> entry : counts.entrySet()) {
            int percent = percentage(entry.getValue(), totalVotes);
            String choice = entry.getKey();
            html.append("<div class=\"bar-item\">\n");
            html.append(String.format(Locale.ENGLISH, "<strong>%s</strong>%n", choice.toUpperCase(Locale.ENGLISH)));
            html.append("<div class=\"bar-track\">");
            html.append(String.format(Locale.ENGLISH,
                    "<div class=\"bar-fill\" style=\"width:%d%%;background:%s;\"></div>",
                    Math.max(percent, entry.getValue() > 0 ? 8 : 0),
                    colorForChoice(choice)));
            html.append("</div>\n");
            html.append(String.format(Locale.ENGLISH, "<div class=\"bar-count\">%d votes · %d%%</div>%n", entry.getValue(), percent));
            html.append("</div>\n");
        }
    }

    private void appendColumnChart(StringBuilder html, Map<String, Integer> counts, int totalVotes) {
        for (Map.Entry<String, Integer> entry : counts.entrySet()) {
            int percent = percentage(entry.getValue(), totalVotes);
            String choice = entry.getKey();
            html.append("<div class=\"column-item\">\n");
            html.append(String.format(Locale.ENGLISH, "<span class=\"column-value\">%d</span>%n", entry.getValue()));
            html.append("<div class=\"column-track\">");
            html.append(String.format(Locale.ENGLISH,
                    "<div class=\"column-bar\" style=\"height:%d%%;background:%s;\"></div>",
                    Math.max(percent, entry.getValue() > 0 ? 8 : 0),
                    colorForChoice(choice)));
            html.append("</div>\n");
            html.append(String.format(Locale.ENGLISH, "<span class=\"column-label\">%s</span>%n", choice.toUpperCase(Locale.ENGLISH)));
            html.append(String.format(Locale.ENGLISH, "<span class=\"column-percent\">%d%% share</span>%n", percent));
            html.append("</div>\n");
        }
    }

    private void appendPieChart(StringBuilder html, Map<String, Integer> counts, int totalVotes) {
        html.append("<div class=\"pie-layout\">\n");
        html.append(String.format(Locale.ENGLISH,
                "<div class=\"pie-chart\" style=\"background:%s;\">%n",
                buildPieGradient(counts, totalVotes)));
        html.append("<div class=\"pie-center\">");
        html.append(String.format(Locale.ENGLISH, "<span class=\"pie-total\">%d</span>", totalVotes));
        html.append("<span class=\"pie-caption\">Total Votes</span>");
        html.append("</div>\n");
        html.append("</div>\n");
        html.append("<div class=\"legend\">\n");
        for (Map.Entry<String, Integer> entry : counts.entrySet()) {
            int percent = percentage(entry.getValue(), totalVotes);
            String choice = entry.getKey();
            html.append("<div class=\"legend-item\">\n");
            html.append("<div class=\"legend-left\">");
            html.append(String.format(Locale.ENGLISH,
                    "<span class=\"swatch\" style=\"background:%s;\"></span><span>Choice %s</span>",
                    colorForChoice(choice),
                    choice.toUpperCase(Locale.ENGLISH)));
            html.append("</div>\n");
            html.append(String.format(Locale.ENGLISH, "<div class=\"legend-meta\">%d votes · %d%%</div>%n", entry.getValue(), percent));
            html.append("</div>\n");
        }
        html.append("</div>\n");
        html.append("</div>\n");
    }

    private void appendVoteRows(StringBuilder html, Map<String, Integer> counts, int totalVotes) {
        for (Map.Entry<String, Integer> entry : counts.entrySet()) {
            int percent = percentage(entry.getValue(), totalVotes);
            String choice = entry.getKey();
            html.append("<tr>\n");
            html.append("<td>");
            html.append(String.format(Locale.ENGLISH,
                    "<span class=\"choice-pill\"><span class=\"choice-dot\" style=\"background:%s;\"></span>%s</span>",
                    colorForChoice(choice),
                    choice.toUpperCase(Locale.ENGLISH)));
            html.append("</td>\n");
            html.append(String.format(Locale.ENGLISH, "<td>%d</td>%n", entry.getValue()));
            html.append(String.format(Locale.ENGLISH, "<td>%d%%</td>%n", percent));
            html.append("</tr>\n");
        }
    }

    private void appendComments(StringBuilder html, List<CommentEntry> comments) {
        if (comments.isEmpty()) {
            html.append("<div class=\"empty-state\">No comments yet. Students can submit anonymous notes from the Android app.</div>\n");
            return;
        }

        html.append("<div class=\"comment-list\">\n");
        for (CommentEntry comment : comments) {
            html.append("<article class=\"comment-item\">\n");
            html.append(String.format(Locale.ENGLISH,
                    "<span class=\"comment-time\">%s</span>%n",
                    FORMATTER.format(comment.getCreatedAt())));
            html.append(String.format(Locale.ENGLISH,
                    "<p class=\"comment-text\">%s</p>%n",
                    escapeHtml(comment.getCommentText())));
            html.append("</article>\n");
        }
        html.append("</div>\n");
    }

    private int percentage(int value, int totalVotes) {
        return totalVotes == 0 ? 0 : (int) Math.round(value * 100.0 / totalVotes);
    }

    private String colorForChoice(String choice) {
        return CHOICE_COLORS.getOrDefault(choice, "#0b5cad");
    }

    private String buildPieGradient(Map<String, Integer> counts, int totalVotes) {
        if (totalVotes == 0) {
            return "conic-gradient(#dbe5f1 0deg 360deg)";
        }

        StringBuilder gradient = new StringBuilder("conic-gradient(");
        double currentAngle = 0.0;

        for (String choice : CHOICE_ORDER) {
            int value = counts.getOrDefault(choice, 0);
            double angle = value * 360.0 / totalVotes;
            double nextAngle = currentAngle + angle;
            gradient.append(String.format(Locale.ENGLISH,
                    "%s %.2fdeg %.2fdeg,",
                    colorForChoice(choice),
                    currentAngle,
                    nextAngle));
            currentAngle = nextAngle;
        }

        if (gradient.charAt(gradient.length() - 1) == ',') {
            gradient.deleteCharAt(gradient.length() - 1);
        }
        gradient.append(")");
        return gradient.toString();
    }

    private String escapeHtml(String value) {
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
