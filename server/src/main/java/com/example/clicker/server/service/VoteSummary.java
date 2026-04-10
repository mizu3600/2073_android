package com.example.clicker.server.service;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class VoteSummary {
    private final int questionNo;
    private final Map<String, Integer> counts;

    public VoteSummary(int questionNo, Map<String, Integer> counts) {
        this.questionNo = questionNo;
        this.counts = Collections.unmodifiableMap(new LinkedHashMap<>(counts));
    }

    public static VoteSummary empty(int questionNo) {
        return new VoteSummary(questionNo, defaultCounts());
    }

    public static LinkedHashMap<String, Integer> defaultCounts() {
        LinkedHashMap<String, Integer> counts = new LinkedHashMap<>();
        counts.put("a", 0);
        counts.put("b", 0);
        counts.put("c", 0);
        counts.put("d", 0);
        return counts;
    }

    public int getQuestionNo() {
        return questionNo;
    }

    public Map<String, Integer> getCounts() {
        return counts;
    }

    public int getTotalVotes() {
        return counts.values().stream().mapToInt(Integer::intValue).sum();
    }
}
