package com.neet821.movierank.controller;

import com.neet821.movierank.crawler.DoubanNowplayingCrawler;
import com.neet821.movierank.model.NowPlayingMovie;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@RestController
public class NowPlayingController {
    private final DoubanNowplayingCrawler crawler;

    public NowPlayingController(DoubanNowplayingCrawler crawler) {
        this.crawler = crawler;
    }

    @GetMapping("/nowplaying")
    public List<NowPlayingMovie> getNowPlaying() throws IOException {
        return crawler.crawl();
    }
}
