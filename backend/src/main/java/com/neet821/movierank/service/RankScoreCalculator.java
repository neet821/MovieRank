package com.neet821.movierank.service;

import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class RankScoreCalculator {
    private static final Map<String, Double> WEIGHTS = Map.of(
            "Douban", 0.40,
            "IMDb", 0.35,
            "Maoyan", 0.25
    );

    public double toHundredPointScore(double sourceScore) {
        return sourceScore * 10;
    }

    public double calculateFinalScore(Map<String, Double> scores) {
        double totalScore = 0;
        double totalWeight = 0;

        for (Map.Entry<String, Double> entry : scores.entrySet()) {
            Double weight = WEIGHTS.get(entry.getKey());
            if (weight != null) {
                totalScore += entry.getValue() * weight;
                totalWeight += weight;
            }
        }

        if (totalWeight == 0) {
            return 0;
        }

        return totalScore / totalWeight;
    }
}
