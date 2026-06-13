package com.neet821.movierank.controller;

import com.neet821.movierank.crawler.TspdtTop1000Crawler;
import com.neet821.movierank.model.TspdtMovieRank;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@RestController
public class TspdtController {
    private final TspdtTop1000Crawler tspdtTop1000Crawler;

    public TspdtController(TspdtTop1000Crawler tspdtTop1000Crawler) {
        this.tspdtTop1000Crawler = tspdtTop1000Crawler;
    }

    @GetMapping("/tspdt/top1000")
    public List<TspdtMovieRank> getTop1000() throws IOException {
        return tspdtTop1000Crawler.crawl();
    }
}
