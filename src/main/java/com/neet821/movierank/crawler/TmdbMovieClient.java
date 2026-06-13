package com.neet821.movierank.crawler;

import java.util.Optional;

@FunctionalInterface
public interface TmdbMovieClient {
    Optional<MovieSummary> findMovie(String title);

    default java.util.List<MovieSummary> trendingMovies() {
        return java.util.List.of();
    }

    record MovieSummary(
            int tmdbId,
            String title,
            String posterUrl,
            String detailUrl,
            String overview,
            String releaseDate,
            double voteAverage,
            double popularity
    ) {
    }
}
