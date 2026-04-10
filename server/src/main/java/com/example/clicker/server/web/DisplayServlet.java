package com.example.clicker.server.web;

import com.example.clicker.server.service.VoteService;
import com.example.clicker.server.service.VoteSummary;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;

@WebServlet("/display")
public class DisplayServlet extends HttpServlet {
    private final VoteService voteService = new VoteService();
    private final ResultsPageRenderer resultsPageRenderer = new ResultsPageRenderer();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        int questionNo = parseQuestionNo(request.getParameter("questionNo"));
        VoteSummary summary = VoteSummary.empty(questionNo);
        String errorMessage = null;

        try {
            summary = voteService.getVoteSummary(questionNo);
        } catch (SQLException e) {
            errorMessage = e.getMessage();
        }

        try (PrintWriter out = response.getWriter()) {
            out.print(resultsPageRenderer.render(summary, errorMessage));
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
}
