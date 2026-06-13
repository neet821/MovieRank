package com.neet821.movierank.service;

import com.neet821.movierank.model.CombinedMovieRank;
import com.neet821.movierank.model.RankingMode;
import com.neet821.movierank.model.RankingSource;
import com.neet821.movierank.model.SourceMovieRank;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class RankingEngine {
    public List<CombinedMovieRank> rank(List<SourceMovieRank> sourceRanks, RankingMode mode) {
        Map<MovieKey, List<SourceMovieRank>> grouped = sourceRanks.stream()
                .collect(Collectors.groupingBy(MovieKey::from));

        List<CombinedMovieRank> combined = grouped.values()
                .stream()
                .map(ranks -> combine(ranks, mode))
                .sorted(Comparator.comparingDouble(CombinedMovieRank::getFinalScore).reversed()
                        .thenComparing(CombinedMovieRank::getTitle))
                .toList();

        for (int index = 0; index < combined.size(); index++) {
            combined.get(index).setFinalRank(index + 1);
        }
        return combined;
    }

    private CombinedMovieRank combine(List<SourceMovieRank> ranks, RankingMode mode) {
        SourceMovieRank first = ranks.get(0);
        Map<RankingSource, SourceMovieRank> bySource = new EnumMap<>(RankingSource.class);
        for (SourceMovieRank rank : ranks) {
            bySource.put(rank.getSource(), rank);
        }

        double score = 0;
        Map<String, Integer> sourceRanks = new LinkedHashMap<>();
        Map<String, Double> sourceWeights = new LinkedHashMap<>();
        List<String> presentSources = new ArrayList<>();
        List<String> missingSources = new ArrayList<>();

        for (RankingSource source : RankingSource.values()) {
            double weight = mode.getWeights().getOrDefault(source, 0.0);
            sourceWeights.put(source.getDisplayName(), weight);
            SourceMovieRank sourceRank = bySource.get(source);
            if (sourceRank == null) {
                missingSources.add(source.getDisplayName());
                continue;
            }
            presentSources.add(source.getDisplayName());
            sourceRanks.put(source.getDisplayName(), sourceRank.getSourceRank());
            score += scoreForRank(sourceRank.getSourceRank()) * weight;
        }

        CombinedMovieRank result = new CombinedMovieRank();
        result.setId(MovieKey.slug(first));
        result.setTitle(first.getTitle());
        result.setYear(first.getYear());
        result.setModeName(mode.getDisplayName());
        result.setFinalScore(round(score));
        result.setPresentSources(presentSources);
        result.setMissingSources(missingSources);
        result.setSourceRanks(sourceRanks);
        result.setSourceWeights(sourceWeights);
        return result;
    }

    private double scoreForRank(int rank) {
        if (rank <= 0) {
            return 0;
        }
        return 100.0 / rank;
    }

    private double round(double value) {
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }
}
