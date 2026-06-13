package com.neet821.movierank.crawler;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class TmdbApiMovieClient implements TmdbMovieClient {
    private static final String SEARCH_URL = "https://api.themoviedb.org/3/search/movie";
    private static final String TRENDING_URL = "https://api.themoviedb.org/3/trending/movie/week";
    private static final String POSTER_BASE_URL = "https://image.tmdb.org/t/p/w500";
    private static final String MOVIE_BASE_URL = "https://www.themoviedb.org/movie/";

    private final String readToken;
    private final String apiKey;

    public TmdbApiMovieClient(
            @Value("${tmdb.api.read-token:}") String readToken,
            @Value("${tmdb.api.key:}") String apiKey
    ) {
        this.readToken = readToken == null ? "" : readToken.trim();
        this.apiKey = apiKey == null ? "" : apiKey.trim();
    }

    @Override
    public Optional<MovieSummary> findMovie(String title) {
        if (title == null || title.isBlank() || (readToken.isBlank() && apiKey.isBlank())) {
            return Optional.empty();
        }

        try {
            String url = SEARCH_URL
                    + "?query=" + URLEncoder.encode(title.trim(), StandardCharsets.UTF_8)
                    + "&language=zh-CN&include_adult=false&page=1";
            if (readToken.isBlank()) {
                url += "&api_key=" + URLEncoder.encode(apiKey, StandardCharsets.UTF_8);
            }

            JsonArray results = fetchResults(url);
            if (results == null || results.isEmpty()) {
                return Optional.empty();
            }

            for (int i = 0; i < results.size(); i++) {
                JsonObject movie = results.get(i).getAsJsonObject();
                Optional<MovieSummary> summary = toMovieSummary(movie);
                if (summary.isEmpty()) {
                    continue;
                }
                return summary;
            }
        } catch (IOException | IllegalStateException | UnsupportedOperationException ignored) {
            return Optional.empty();
        }

        return Optional.empty();
    }

    @Override
    public List<MovieSummary> trendingMovies() {
        if (readToken.isBlank() && apiKey.isBlank()) {
            return List.of();
        }

        try {
            String url = TRENDING_URL + "?language=zh-CN";
            if (readToken.isBlank()) {
                url += "&api_key=" + URLEncoder.encode(apiKey, StandardCharsets.UTF_8);
            }
            JsonArray results = fetchResults(url);
            if (results == null || results.isEmpty()) {
                return List.of();
            }

            List<MovieSummary> movies = new ArrayList<>();
            for (int i = 0; i < results.size() && movies.size() < 12; i++) {
                toMovieSummary(results.get(i).getAsJsonObject()).ifPresent(movies::add);
            }
            return movies;
        } catch (IOException | IllegalStateException | UnsupportedOperationException ignored) {
            return List.of();
        }
    }

    private JsonArray fetchResults(String url) throws IOException {
        Connection connection = Jsoup.connect(url)
                .ignoreContentType(true)
                .userAgent("MovieRank/1.0")
                .timeout(10000);
        if (!readToken.isBlank()) {
            connection.header("Authorization", "Bearer " + readToken);
        }

        JsonObject root = JsonParser.parseString(connection.execute().body()).getAsJsonObject();
        return root.getAsJsonArray("results");
    }

    private Optional<MovieSummary> toMovieSummary(JsonObject movie) {
        if (!hasText(movie, "poster_path") || !movie.has("id")) {
            return Optional.empty();
        }
        int movieId = movie.get("id").getAsInt();
        String title = firstText(movie, "title", "name", "original_title");
        String posterPath = movie.get("poster_path").getAsString();
        return Optional.of(new MovieSummary(
                movieId,
                title,
                POSTER_BASE_URL + posterPath,
                MOVIE_BASE_URL + movieId,
                text(movie, "overview"),
                text(movie, "release_date"),
                number(movie, "vote_average"),
                number(movie, "popularity")
        ));
    }

    private boolean hasText(JsonObject object, String memberName) {
        return object.has(memberName)
                && !object.get(memberName).isJsonNull()
                && !object.get(memberName).getAsString().isBlank();
    }

    private String firstText(JsonObject object, String... memberNames) {
        for (String memberName : memberNames) {
            String value = text(object, memberName);
            if (!value.isBlank()) {
                return value;
            }
        }
        return "";
    }

    private String text(JsonObject object, String memberName) {
        if (!hasText(object, memberName)) {
            return "";
        }
        return object.get(memberName).getAsString();
    }

    private double number(JsonObject object, String memberName) {
        if (!object.has(memberName) || object.get(memberName).isJsonNull()) {
            return 0;
        }
        return object.get(memberName).getAsDouble();
    }
}
