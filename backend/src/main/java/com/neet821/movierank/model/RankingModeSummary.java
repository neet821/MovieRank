package com.neet821.movierank.model;

import java.util.Map;

public class RankingModeSummary {
    private String code;
    private String name;
    private Map<String, Double> weights;

    public RankingModeSummary(String code, String name, Map<String, Double> weights) {
        this.code = code;
        this.name = name;
        this.weights = weights;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public Map<String, Double> getWeights() {
        return weights;
    }
}
