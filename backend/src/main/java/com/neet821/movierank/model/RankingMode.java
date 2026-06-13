package com.neet821.movierank.model;

import java.util.LinkedHashMap;
import java.util.Map;

public enum RankingMode {
    BALANCED("均衡综合榜", weights(0.20, 0.20, 0.20, 0.20, 0.20)),
    AUDIENCE("观众口碑榜", weights(0.35, 0.30, 0.25, 0.05, 0.05)),
    AUTHORITY("影史权威榜", weights(0.10, 0.10, 0.15, 0.30, 0.35)),
    CHINESE("中文用户偏好榜", weights(0.15, 0.50, 0.15, 0.10, 0.10)),
    COMMUNITY("影迷社区偏好榜", weights(0.15, 0.15, 0.45, 0.10, 0.15));

    private final String displayName;
    private final Map<RankingSource, Double> weights;

    RankingMode(String displayName, Map<RankingSource, Double> weights) {
        this.displayName = displayName;
        this.weights = weights;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Map<RankingSource, Double> getWeights() {
        return weights;
    }

    public static RankingMode fromValue(String value) {
        if (value == null || value.isBlank()) {
            return BALANCED;
        }
        for (RankingMode mode : values()) {
            if (mode.name().equalsIgnoreCase(value) || mode.displayName.equalsIgnoreCase(value)) {
                return mode;
            }
        }
        throw new IllegalArgumentException("Unknown ranking mode: " + value);
    }

    private static Map<RankingSource, Double> weights(double imdb, double douban, double letterboxd,
                                                      double bfi, double tspdt) {
        Map<RankingSource, Double> result = new LinkedHashMap<>();
        result.put(RankingSource.IMDB, imdb);
        result.put(RankingSource.DOUBAN, douban);
        result.put(RankingSource.LETTERBOXD, letterboxd);
        result.put(RankingSource.BFI, bfi);
        result.put(RankingSource.TSPDT, tspdt);
        return Map.copyOf(result);
    }
}
