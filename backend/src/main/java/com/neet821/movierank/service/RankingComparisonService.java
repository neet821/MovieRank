package com.neet821.movierank.service;

import com.neet821.movierank.model.RankComparison;
import com.neet821.movierank.model.RankingSource;
import com.neet821.movierank.model.SharedRankGap;
import com.neet821.movierank.model.SourceMovieRank;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class RankingComparisonService {
    public RankComparison compare(RankingSource sourceA, RankingSource sourceB, List<SourceMovieRank> allRanks) {
        Map<MovieKey, SourceMovieRank> aMovies = byKey(sourceA, allRanks);
        Map<MovieKey, SourceMovieRank> bMovies = byKey(sourceB, allRanks);
        Map<SourceMovieRank, SourceMovieRank> sharedPairs = new LinkedHashMap<>();
        aMovies.forEach((key, value) -> {
            if (bMovies.containsKey(key)) {
                sharedPairs.putIfAbsent(value, bMovies.get(key));
            }
        });
        Set<SourceMovieRank> sharedA = sharedPairs.keySet();
        Set<SourceMovieRank> sharedB = new LinkedHashSet<>(sharedPairs.values());

        List<SharedRankGap> shared = sharedPairs.entrySet()
                .stream()
                .map(entry -> toGap(entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparing(SharedRankGap::getTitle))
                .toList();

        List<SourceMovieRank> onlyA = ranksForSource(sourceA, allRanks).stream()
                .filter(rank -> !sharedA.contains(rank))
                .sorted(Comparator.comparingInt(SourceMovieRank::getSourceRank))
                .toList();
        List<SourceMovieRank> onlyB = ranksForSource(sourceB, allRanks).stream()
                .filter(rank -> !sharedB.contains(rank))
                .sorted(Comparator.comparingInt(SourceMovieRank::getSourceRank))
                .toList();

        RankComparison comparison = new RankComparison();
        comparison.setSourceA(sourceA.getDisplayName());
        comparison.setSourceB(sourceB.getDisplayName());
        comparison.setSharedCount(shared.size());
        comparison.setSharedMovies(shared);
        comparison.setOnlyInSourceA(onlyA);
        comparison.setOnlyInSourceB(onlyB);
        comparison.setLargestRankGaps(shared.stream()
                .sorted(Comparator.comparingInt(SharedRankGap::getRankGap).reversed())
                .limit(20)
                .toList());
        return comparison;
    }

    private Map<MovieKey, SourceMovieRank> byKey(RankingSource source, List<SourceMovieRank> ranks) {
        Map<MovieKey, SourceMovieRank> movies = new LinkedHashMap<>();
        ranks.stream()
                .filter(rank -> rank.getSource() == source)
                .forEach(rank -> MovieKey.fromAllNames(rank).forEach(key -> movies.putIfAbsent(key, rank)));
        return movies;
    }

    private List<SourceMovieRank> ranksForSource(RankingSource source, List<SourceMovieRank> ranks) {
        return ranks.stream()
                .filter(rank -> rank.getSource() == source)
                .toList();
    }

    private SharedRankGap toGap(SourceMovieRank a, SourceMovieRank b) {
        return new SharedRankGap(a.getTitle(), a.getYear(), a.getSourceRank(), b.getSourceRank());
    }
}
