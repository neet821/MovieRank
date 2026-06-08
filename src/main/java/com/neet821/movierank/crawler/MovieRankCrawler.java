package com.neet821.movierank.crawler;

import com.neet821.movierank.model.SourceMovieRank;
import java.util.List;

public interface MovieRankCrawler {
    String getSourceName();

    List<SourceMovieRank> crawl();
}
