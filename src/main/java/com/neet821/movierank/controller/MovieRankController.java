package com.neet821.movierank.controller;

import com.neet821.movierank.model.WeightedRankResult;
import com.neet821.movierank.service.MovieRankService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class MovieRankController {
    private final MovieRankService movieRankService;

    public MovieRankController(MovieRankService movieRankService) {
        this.movieRankService = movieRankService;
    }

    @GetMapping("/movie-ranks")
    public List<WeightedRankResult> getMovieRanks() {
        return movieRankService.getWeightedRanks();
    }
}
