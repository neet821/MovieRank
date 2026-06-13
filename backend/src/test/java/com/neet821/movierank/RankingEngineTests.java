package com.neet821.movierank;

import com.neet821.movierank.model.CombinedMovieRank;
import com.neet821.movierank.model.RankingMode;
import com.neet821.movierank.model.RankingSource;
import com.neet821.movierank.model.SourceMovieRank;
import com.neet821.movierank.service.RankingEngine;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RankingEngineTests {
    private final RankingEngine rankingEngine = new RankingEngine();

    @Test
    void balancedModeUsesEqualWeightsAcrossFiveSources() {
        List<CombinedMovieRank> ranks = rankingEngine.rank(List.of(
                new SourceMovieRank(RankingSource.IMDB, "Movie A", 2000, 1, "url-a"),
                new SourceMovieRank(RankingSource.DOUBAN, "Movie A", 2000, 1, "url-a"),
                new SourceMovieRank(RankingSource.LETTERBOXD, "Movie A", 2000, 1, "url-a"),
                new SourceMovieRank(RankingSource.BFI, "Movie A", 2000, 10, "url-a"),
                new SourceMovieRank(RankingSource.TSPDT, "Movie A", 2000, 10, "url-a"),
                new SourceMovieRank(RankingSource.IMDB, "Movie B", 2001, 4, "url-b"),
                new SourceMovieRank(RankingSource.DOUBAN, "Movie B", 2001, 4, "url-b"),
                new SourceMovieRank(RankingSource.LETTERBOXD, "Movie B", 2001, 4, "url-b"),
                new SourceMovieRank(RankingSource.BFI, "Movie B", 2001, 1, "url-b"),
                new SourceMovieRank(RankingSource.TSPDT, "Movie B", 2001, 1, "url-b")
        ), RankingMode.BALANCED);

        assertThat(ranks).extracting(CombinedMovieRank::getTitle)
                .containsExactly("Movie A", "Movie B");
        assertThat(ranks.get(0).getSourceWeights()).containsEntry("IMDb", 0.20);
        assertThat(ranks.get(0).getFinalRank()).isEqualTo(1);
    }

    @Test
    void authorityModeGivesBfiAndTspdtMoreInfluence() {
        List<CombinedMovieRank> ranks = rankingEngine.rank(List.of(
                new SourceMovieRank(RankingSource.IMDB, "Crowd Favorite", 2000, 1, "url-a"),
                new SourceMovieRank(RankingSource.DOUBAN, "Crowd Favorite", 2000, 1, "url-a"),
                new SourceMovieRank(RankingSource.LETTERBOXD, "Crowd Favorite", 2000, 1, "url-a"),
                new SourceMovieRank(RankingSource.BFI, "Crowd Favorite", 2000, 80, "url-a"),
                new SourceMovieRank(RankingSource.TSPDT, "Crowd Favorite", 2000, 900, "url-a"),
                new SourceMovieRank(RankingSource.IMDB, "Canon Favorite", 1960, 80, "url-b"),
                new SourceMovieRank(RankingSource.DOUBAN, "Canon Favorite", 1960, 80, "url-b"),
                new SourceMovieRank(RankingSource.LETTERBOXD, "Canon Favorite", 1960, 80, "url-b"),
                new SourceMovieRank(RankingSource.BFI, "Canon Favorite", 1960, 1, "url-b"),
                new SourceMovieRank(RankingSource.TSPDT, "Canon Favorite", 1960, 1, "url-b")
        ), RankingMode.AUTHORITY);

        assertThat(ranks).extracting(CombinedMovieRank::getTitle)
                .containsExactly("Canon Favorite", "Crowd Favorite");
        assertThat(ranks.get(0).getModeName()).isEqualTo("影史权威榜");
    }

    @Test
    void missingSourcesAreRecordedButMovieCanStillRank() {
        List<CombinedMovieRank> ranks = rankingEngine.rank(List.of(
                new SourceMovieRank(RankingSource.IMDB, "Single Source", 1999, 1, "url")
        ), RankingMode.BALANCED);

        assertThat(ranks).hasSize(1);
        assertThat(ranks.get(0).getPresentSources()).containsExactly("IMDb");
        assertThat(ranks.get(0).getMissingSources())
                .containsExactly("豆瓣", "Letterboxd", "BFI Sight and Sound", "TSPDT");
    }

    @Test
    void aliasesMergeLocalizedMovieTitles() {
        List<CombinedMovieRank> ranks = rankingEngine.rank(List.of(
                new SourceMovieRank(RankingSource.IMDB, "The Shawshank Redemption", 1994, 1, "url-a"),
                new SourceMovieRank(RankingSource.DOUBAN, "肖申克的救赎", 1994, 1, "url-b", List.of("The Shawshank Redemption"))
        ), RankingMode.BALANCED);

        assertThat(ranks).hasSize(1);
        assertThat(ranks.get(0).getPresentSources()).containsExactly("IMDb", "豆瓣");
        assertThat(ranks.get(0).getId()).isEqualTo("the-shawshank-redemption-1994");
    }
}
