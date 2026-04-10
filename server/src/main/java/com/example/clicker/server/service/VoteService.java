package com.example.clicker.server.service;

import com.example.clicker.server.repository.ResponseRepository;

import java.sql.SQLException;
import java.util.Map;

public class VoteService {
    private final ResponseRepository responseRepository;

    public VoteService() {
        this(new ResponseRepository());
    }

    VoteService(ResponseRepository responseRepository) {
        this.responseRepository = responseRepository;
    }

    public void submitVote(int questionNo, String choice) throws SQLException {
        responseRepository.insertResponse(questionNo, choice);
    }

    public VoteSummary getVoteSummary(int questionNo) throws SQLException {
        Map<String, Integer> counts = VoteSummary.defaultCounts();
        counts.putAll(responseRepository.countResponsesByChoice(questionNo));
        return new VoteSummary(questionNo, counts);
    }
}
