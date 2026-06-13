package com.neet821.movierank.crawler;

import com.neet821.movierank.model.SourceMovieRank;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MaoyanMovieRankCrawler implements MovieRankCrawler {
    @Override
    public String getSourceName() {
        return "Maoyan";
    }

    @Override
    public List<SourceMovieRank> crawl() {
        return List.of(
                new SourceMovieRank("Maoyan", "肖申克的救赎", 1994, 3, 9.5, "https://www.maoyan.com/"),
                new SourceMovieRank("Maoyan", "霸王别姬", 1993, 1, 9.6, "https://www.maoyan.com/")
        );
    }
}
