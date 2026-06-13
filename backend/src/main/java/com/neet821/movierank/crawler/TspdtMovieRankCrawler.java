package com.neet821.movierank.crawler;

import com.neet821.movierank.model.RankingSource;
import com.neet821.movierank.model.SourceMovieRank;
import com.neet821.movierank.model.TspdtMovieRank;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Component
public class TspdtMovieRankCrawler implements MovieRankCrawler {
    private final TspdtTop1000Crawler tspdtTop1000Crawler;

    public TspdtMovieRankCrawler(TspdtTop1000Crawler tspdtTop1000Crawler) {
        this.tspdtTop1000Crawler = tspdtTop1000Crawler;
    }

    @Override
    public String getSourceName() {
        return RankingSource.TSPDT.getDisplayName();
    }

    @Override
    public List<SourceMovieRank> crawl() {
        try {
            return toSourceRanks(tspdtTop1000Crawler.crawl());
        } catch (IOException exception) {
            throw new IllegalStateException("TSPDT Top 1000 爬取失败", exception);
        }
    }

    public List<SourceMovieRank> toSourceRanks(List<TspdtMovieRank> movies) {
        return movies.stream()
                .map(movie -> new SourceMovieRank(
                        RankingSource.TSPDT,
                        movie.getTitle(),
                        parseYear(movie.getYear()),
                        movie.getPosition(),
                        TspdtTop1000Crawler.SOURCE_URL))
                .toList();
    }

    private int parseYear(String year) {
        if (year == null || year.isBlank()) {
            return 0;
        }
        return Integer.parseInt(year.replaceAll("[^0-9]", ""));
    }
}
