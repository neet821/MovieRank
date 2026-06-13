package com.neet821.movierank.service;

import com.neet821.movierank.entity.MovieRank;
import com.neet821.movierank.mapper.MovieRankMapper;
import com.neet821.movierank.model.WeightedRankResult;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class MovieRankService {
    private final MovieRankMapper movieRankMapper;

    public MovieRankService(MovieRankMapper movieRankMapper) {
        this.movieRankMapper = movieRankMapper;
    }

    public List<WeightedRankResult> getWeightedRanks() {
        return movieRankMapper.findAllOrdered()
                .stream()
                .map(this::toWeightedRankResult)
                .toList();
    }

    private WeightedRankResult toWeightedRankResult(MovieRank movieRank) {
        return new WeightedRankResult(
                movieRank.getTitle(),
                movieRank.getYear(),
                movieRank.getDoubanScore(),
                movieRank.getImdbScore(),
                movieRank.getMaoyanScore(),
                movieRank.getFinalScore(),
                movieRank.getFinalRank(),
                parseMissingSources(movieRank.getMissingSources())
        );
    }

    private List<String> parseMissingSources(String missingSources) {
        if (missingSources == null || missingSources.isBlank()) {
            return List.of();
        }
        return Arrays.stream(missingSources.split(","))
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .toList();
    }
}
