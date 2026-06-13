package com.neet821.movierank.service;

import com.neet821.movierank.client.TmdbMovieInfoClient;
import com.neet821.movierank.model.CombinedMovieRank;
import com.neet821.movierank.model.MovieDetail;
import com.neet821.movierank.model.RankingMode;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
public class MovieDetailService {
    private final SourceRankingService sourceRankingService;
    private final RankingEngine rankingEngine;
    private final TmdbMovieInfoClient tmdbMovieInfoClient;

    public MovieDetailService(SourceRankingService sourceRankingService,
                              RankingEngine rankingEngine,
                              TmdbMovieInfoClient tmdbMovieInfoClient) {
        this.sourceRankingService = sourceRankingService;
        this.rankingEngine = rankingEngine;
        this.tmdbMovieInfoClient = tmdbMovieInfoClient;
    }

    public Optional<MovieDetail> findById(String id, RankingMode mode) throws IOException {
        List<CombinedMovieRank> ranks = rankingEngine.rank(sourceRankingService.findAll(), mode);
        Optional<CombinedMovieRank> rank = ranks.stream()
                .filter(item -> item.getId().equals(id))
                .findFirst();
        if (rank.isEmpty()) {
            return Optional.empty();
        }

        CombinedMovieRank combined = rank.get();
        MovieDetail detail = tmdbMovieInfoClient.findMovie(combined.getTitle(), combined.getYear())
                .orElseGet(MovieDetail::new);
        detail.setId(combined.getId());
        if (detail.getTitle() == null || detail.getTitle().isBlank()) {
            detail.setTitle(combined.getTitle());
        }
        detail.setYear(combined.getYear());
        detail.setSourceRanks(combined.getSourceRanks());
        if (detail.getOverview() == null || detail.getOverview().isBlank()) {
            detail.setOverview("暂无 TMDB 简介。配置 TMDB_API_KEY 后会自动补充影片简介、海报和评分。");
        }
        return Optional.of(detail);
    }
}
