package com.neet821.movierank.service;

import com.neet821.movierank.crawler.MovieRankCrawler;
import com.neet821.movierank.model.RankingMode;
import com.neet821.movierank.model.RankingModeSummary;
import com.neet821.movierank.model.RankingSource;
import com.neet821.movierank.model.RankingSourceSummary;
import com.neet821.movierank.model.SourceMovieRank;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class SourceRankingService {
    private final List<MovieRankCrawler> crawlers;
    private final Map<RankingSource, List<SourceMovieRank>> cache = new EnumMap<>(RankingSource.class);

    public SourceRankingService(List<MovieRankCrawler> crawlers) {
        this.crawlers = crawlers;
    }

    public List<SourceMovieRank> findAll() {
        return Arrays.stream(RankingSource.values())
                .flatMap(source -> findBySource(source).stream())
                .sorted(Comparator.comparing(SourceMovieRank::getSourceName)
                        .thenComparingInt(SourceMovieRank::getSourceRank))
                .toList();
    }

    public List<SourceMovieRank> findBySource(RankingSource source) {
        return loadSource(source).stream()
                .sorted(Comparator.comparingInt(SourceMovieRank::getSourceRank))
                .toList();
    }

    public List<RankingSourceSummary> sourceSummaries() {
        return Arrays.stream(RankingSource.values())
                .map(source -> new RankingSourceSummary(
                        source.name(),
                        source.getDisplayName(),
                        source.getExpectedSize(),
                        safeLoadedSize(source)))
                .toList();
    }

    public List<RankingModeSummary> modeSummaries() {
        return Arrays.stream(RankingMode.values())
                .map(mode -> new RankingModeSummary(mode.name(), mode.getDisplayName(), toDisplayWeights(mode)))
                .toList();
    }

    private Map<String, Double> toDisplayWeights(RankingMode mode) {
        Map<String, Double> weights = new LinkedHashMap<>();
        for (RankingSource source : RankingSource.values()) {
            weights.put(source.getDisplayName(), mode.getWeights().get(source));
        }
        return weights;
    }

    private synchronized List<SourceMovieRank> loadSource(RankingSource source) {
        if (cache.containsKey(source)) {
            return cache.get(source);
        }
        MovieRankCrawler crawler = crawlers.stream()
                .filter(candidate -> source.getDisplayName().equals(candidate.getSourceName()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("缺少榜单爬虫: " + source.getDisplayName()));
        List<SourceMovieRank> ranks = List.copyOf(crawler.crawl());
        cache.put(source, ranks);
        return ranks;
    }

    private int safeLoadedSize(RankingSource source) {
        try {
            return findBySource(source).size();
        } catch (RuntimeException exception) {
            return 0;
        }
    }

}
