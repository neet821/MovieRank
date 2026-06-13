package com.neet821.movierank;

import com.neet821.movierank.model.MovieDetail;
import com.neet821.movierank.model.RankingSource;
import com.neet821.movierank.model.RankingSourceSummary;
import com.neet821.movierank.model.SourceMovieRank;
import com.neet821.movierank.service.MovieDetailService;
import com.neet821.movierank.service.SourceRankingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class RankingControllerTests {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SourceRankingService sourceRankingService;

    @MockitoBean
    private MovieDetailService movieDetailService;

    @Test
    void exposesRankingModesAndCombinedList() throws Exception {
        List<SourceMovieRank> ranks = sampleRanks();
        when(sourceRankingService.modeSummaries()).thenCallRealMethod();
        when(sourceRankingService.sourceSummaries()).thenReturn(sampleSummaries());
        when(sourceRankingService.findAll()).thenReturn(ranks);
        when(sourceRankingService.findBySource(RankingSource.IMDB)).thenReturn(List.of(ranks.get(0), ranks.get(2)));

        mockMvc.perform(get("/api/ranking-modes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(5)))
                .andExpect(jsonPath("$[0].name").value("均衡综合榜"));

        mockMvc.perform(get("/api/ranking-sources"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].expectedSize").value(250))
                .andExpect(jsonPath("$[0].loadedSize").value(greaterThan(0)))
                .andExpect(jsonPath("$[4].expectedSize").value(1000));

        mockMvc.perform(get("/api/rankings/combined?mode=BALANCED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$[0].finalRank").value(1))
                .andExpect(jsonPath("$[0].sourceWeights.IMDb").value(0.20));

        mockMvc.perform(get("/api/source-rankings?source=IMDB"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$[0].sourceName").value("IMDb"));
    }

    @Test
    void exposesComparisonAndMovieDetail() throws Exception {
        List<SourceMovieRank> ranks = sampleRanks();
        when(sourceRankingService.findAll()).thenReturn(ranks);
        when(movieDetailService.findById(eq("Shared Movie-1994"), any())).thenReturn(Optional.of(sampleDetail()));

        mockMvc.perform(get("/api/rankings/compare?sourceA=IMDB&sourceB=DOUBAN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sourceA").value("IMDb"))
                .andExpect(jsonPath("$.sourceB").value("豆瓣"))
                .andExpect(jsonPath("$.sharedCount").value(greaterThan(0)));

        mockMvc.perform(get("/api/movies/{id}", "Shared Movie-1994"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Shared Movie"))
                .andExpect(jsonPath("$.sourceRanks.IMDb").value(1));
    }

    private List<SourceMovieRank> sampleRanks() {
        return List.of(
                new SourceMovieRank(RankingSource.IMDB, "Shared Movie", 1994, 1, "https://example.com/imdb/shared"),
                new SourceMovieRank(RankingSource.DOUBAN, "Shared Movie", 1994, 2, "https://example.com/douban/shared"),
                new SourceMovieRank(RankingSource.IMDB, "Only IMDb", 1972, 2, "https://example.com/imdb/only"),
                new SourceMovieRank(RankingSource.DOUBAN, "Only Douban", 2001, 1, "https://example.com/douban/only"),
                new SourceMovieRank(RankingSource.LETTERBOXD, "Letterboxd Pick", 1962, 1, "https://example.com/letterboxd"),
                new SourceMovieRank(RankingSource.BFI, "BFI Pick", 1941, 1, "https://example.com/bfi"),
                new SourceMovieRank(RankingSource.TSPDT, "TSPDT Pick", 1941, 1, "https://example.com/tspdt"));
    }

    private List<RankingSourceSummary> sampleSummaries() {
        return List.of(
                new RankingSourceSummary("IMDB", "IMDb", 250, 2),
                new RankingSourceSummary("DOUBAN", "豆瓣", 250, 2),
                new RankingSourceSummary("LETTERBOXD", "Letterboxd", 500, 1),
                new RankingSourceSummary("BFI", "BFI Sight and Sound", 250, 1),
                new RankingSourceSummary("TSPDT", "TSPDT", 1000, 1));
    }

    private MovieDetail sampleDetail() {
        MovieDetail detail = new MovieDetail();
        detail.setId("Shared Movie-1994");
        detail.setTitle("Shared Movie");
        detail.setYear(1994);
        detail.setOverview("Sample overview");
        detail.setSourceRanks(Map.of("IMDb", 1, "豆瓣", 2));
        return detail;
    }
}
