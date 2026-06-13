package com.neet821.movierank.controller;

import com.neet821.movierank.model.CombinedMovieRank;
import com.neet821.movierank.model.MovieDetail;
import com.neet821.movierank.model.RankComparison;
import com.neet821.movierank.model.RankingMode;
import com.neet821.movierank.model.RankingModeSummary;
import com.neet821.movierank.model.RankingSource;
import com.neet821.movierank.model.RankingSourceSummary;
import com.neet821.movierank.model.SourceMovieRank;
import com.neet821.movierank.service.MovieDetailService;
import com.neet821.movierank.service.RankingComparisonService;
import com.neet821.movierank.service.RankingEngine;
import com.neet821.movierank.service.SourceRankingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api")
public class RankingController {
    private final SourceRankingService sourceRankingService;
    private final RankingEngine rankingEngine;
    private final RankingComparisonService rankingComparisonService;
    private final MovieDetailService movieDetailService;

    public RankingController(SourceRankingService sourceRankingService,
                             RankingEngine rankingEngine,
                             RankingComparisonService rankingComparisonService,
                             MovieDetailService movieDetailService) {
        this.sourceRankingService = sourceRankingService;
        this.rankingEngine = rankingEngine;
        this.rankingComparisonService = rankingComparisonService;
        this.movieDetailService = movieDetailService;
    }

    @GetMapping("/ranking-modes")
    public List<RankingModeSummary> modes() {
        return sourceRankingService.modeSummaries();
    }

    @GetMapping("/ranking-sources")
    public List<RankingSourceSummary> sources() {
        return sourceRankingService.sourceSummaries();
    }

    @GetMapping("/source-rankings")
    public List<SourceMovieRank> sourceRankings(@RequestParam(defaultValue = "IMDB") String source) {
        return sourceRankingService.findBySource(RankingSource.fromValue(source));
    }

    @GetMapping("/rankings/combined")
    public List<CombinedMovieRank> combinedRankings(@RequestParam(defaultValue = "BALANCED") String mode) {
        return rankingEngine.rank(sourceRankingService.findAll(), RankingMode.fromValue(mode));
    }

    @GetMapping("/rankings/compare")
    public RankComparison compare(@RequestParam(defaultValue = "IMDB") String sourceA,
                                  @RequestParam(defaultValue = "DOUBAN") String sourceB) {
        return rankingComparisonService.compare(
                RankingSource.fromValue(sourceA),
                RankingSource.fromValue(sourceB),
                sourceRankingService.findAll());
    }

    @GetMapping("/movies/{id}")
    public ResponseEntity<MovieDetail> movieDetail(@PathVariable String id,
                                                   @RequestParam(defaultValue = "BALANCED") String mode)
            throws IOException {
        return movieDetailService.findById(id, RankingMode.fromValue(mode))
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
