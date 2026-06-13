package com.neet821.movierank;

import com.neet821.movierank.model.RankComparison;
import com.neet821.movierank.model.RankingSource;
import com.neet821.movierank.model.SourceMovieRank;
import com.neet821.movierank.service.RankingComparisonService;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RankingComparisonServiceTests {
    private final RankingComparisonService comparisonService = new RankingComparisonService();

    @Test
    void comparesSharedAndUniqueMoviesBetweenTwoSources() {
        RankComparison comparison = comparisonService.compare(
                RankingSource.IMDB,
                RankingSource.DOUBAN,
                List.of(
                        new SourceMovieRank(RankingSource.IMDB, "Shared One", 1994, 1, "url"),
                        new SourceMovieRank(RankingSource.DOUBAN, "Shared One", 1994, 7, "url"),
                        new SourceMovieRank(RankingSource.IMDB, "IMDb Only", 1995, 2, "url"),
                        new SourceMovieRank(RankingSource.DOUBAN, "Douban Only", 1996, 2, "url"),
                        new SourceMovieRank(RankingSource.IMDB, "Shared Two", 1997, 100, "url"),
                        new SourceMovieRank(RankingSource.DOUBAN, "Shared Two", 1997, 3, "url")
                )
        );

        assertThat(comparison.getSharedCount()).isEqualTo(2);
        assertThat(comparison.getSharedMovies()).extracting("title")
                .containsExactly("Shared One", "Shared Two");
        assertThat(comparison.getOnlyInSourceA()).extracting(SourceMovieRank::getTitle)
                .containsExactly("IMDb Only");
        assertThat(comparison.getOnlyInSourceB()).extracting(SourceMovieRank::getTitle)
                .containsExactly("Douban Only");
        assertThat(comparison.getLargestRankGaps()).extracting("title")
                .containsExactly("Shared Two", "Shared One");
        assertThat(comparison.getLargestRankGaps().get(0).getRankGap()).isEqualTo(97);
    }

    @Test
    void comparesMoviesByAliasesAcrossLanguages() {
        RankComparison comparison = comparisonService.compare(
                RankingSource.IMDB,
                RankingSource.DOUBAN,
                List.of(
                        new SourceMovieRank(RankingSource.IMDB, "The Shawshank Redemption", 1994, 1, "url-a"),
                        new SourceMovieRank(RankingSource.DOUBAN, "肖申克的救赎", 1994, 1, "url-b", List.of("The Shawshank Redemption"))
                )
        );

        assertThat(comparison.getSharedCount()).isEqualTo(1);
        assertThat(comparison.getOnlyInSourceA()).isEmpty();
        assertThat(comparison.getOnlyInSourceB()).isEmpty();
    }
}
