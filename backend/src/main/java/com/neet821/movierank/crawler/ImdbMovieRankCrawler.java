package com.neet821.movierank.crawler;

import com.neet821.movierank.model.SourceMovieRank;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ImdbMovieRankCrawler implements MovieRankCrawler {
    @Override
    public String getSourceName() {
        return "IMDb";
    }

    @Override
    public List<SourceMovieRank> crawl() {
        return List.of(
                new SourceMovieRank("IMDb", "肖申克的救赎", 1994, 1, 9.3, "https://www.imdb.com/"),
                new SourceMovieRank("IMDb", "霸王别姬", 1993, 80, 8.1, "https://www.imdb.com/")
        );
    }
}
