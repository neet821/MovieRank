package com.neet821.movierank.crawler;

import com.neet821.movierank.model.SourceMovieRank;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DoubanMovieRankCrawler implements MovieRankCrawler {
    @Override
    public String getSourceName() {
        return "Douban";
    }

    @Override
    public List<SourceMovieRank> crawl() {
        return List.of(
                new SourceMovieRank("Douban", "肖申克的救赎", 1994, 1, 9.7, "https://movie.douban.com/"),
                new SourceMovieRank("Douban", "霸王别姬", 1993, 2, 9.6, "https://movie.douban.com/")
        );
    }
}
