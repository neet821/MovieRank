package com.neet821.movierank.client;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.neet821.movierank.model.MovieDetail;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

@Component
public class TmdbMovieInfoClient {
    private static final String SEARCH_URL = "https://api.themoviedb.org/3/search/movie";
    private static final String IMAGE_BASE_URL = "https://image.tmdb.org/t/p/w500";

    private final String apiKey;
    private final HttpClient httpClient;
    private final Gson gson;

    public TmdbMovieInfoClient(@Value("${tmdb.api-key:}") String apiKey) {
        this.apiKey = apiKey == null ? "" : apiKey.trim();
        this.httpClient = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NORMAL).build();
        this.gson = new Gson();
    }

    public Optional<MovieDetail> findMovie(String title, int year) throws IOException {
        if (apiKey.isBlank() || title == null || title.isBlank()) {
            return Optional.empty();
        }

        String query = URLEncoder.encode(title, StandardCharsets.UTF_8);
        String uri = SEARCH_URL + "?api_key=" + URLEncoder.encode(apiKey, StandardCharsets.UTF_8)
                + "&query=" + query + "&year=" + year + "&language=zh-CN&include_adult=false";
        HttpRequest request = HttpRequest.newBuilder(URI.create(uri))
                .GET()
                .header("Accept", "application/json")
                .build();

        HttpResponse<String> response;
        try {
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IOException("TMDB search interrupted", exception);
        }

        if (response.statusCode() / 100 != 2) {
            return Optional.empty();
        }
        SearchResponse searchResponse = gson.fromJson(response.body(), SearchResponse.class);
        if (searchResponse == null || searchResponse.results == null || searchResponse.results.isEmpty()) {
            return Optional.empty();
        }

        MovieResult first = searchResponse.results.get(0);
        MovieDetail detail = new MovieDetail();
        detail.setTitle(first.title);
        detail.setOverview(first.overview);
        detail.setReleaseDate(first.releaseDate);
        detail.setTmdbRating(first.voteAverage);
        if (first.posterPath != null && !first.posterPath.isBlank()) {
            detail.setPosterUrl(IMAGE_BASE_URL + first.posterPath);
        }
        return Optional.of(detail);
    }

    private static final class SearchResponse {
        private List<MovieResult> results;
    }

    private static final class MovieResult {
        private String title;
        private String overview;
        @SerializedName("poster_path")
        private String posterPath;
        @SerializedName("release_date")
        private String releaseDate;
        @SerializedName("vote_average")
        private double voteAverage;
    }
}
