package com.neet821.movierank.client;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class TmdbPosterClient {

  private static final String SEARCH_URL =
      "https://api.themoviedb.org/3/search/movie";
  private static final String IMAGE_BASE_URL =
      "https://image.tmdb.org/t/p/w500";

  private final String apiKey;
  private final HttpClient httpClient;
  private final Gson gson;

  public TmdbPosterClient(@Value("${tmdb.api-key:}") String apiKey) {
    this.apiKey = apiKey == null ? "" : apiKey.trim();
    this.httpClient = HttpClient.newBuilder()
                          .followRedirects(HttpClient.Redirect.NORMAL)
                          .build();
    this.gson = new Gson();
  }

  public Optional<String> findPosterUrl(String title) throws IOException {
    if (apiKey.isBlank() || title == null || title.isBlank()) {
      return Optional.empty();
    }

    String query = URLEncoder.encode(title, StandardCharsets.UTF_8);
    String uri = SEARCH_URL + "?api_key=" +
                 URLEncoder.encode(apiKey, StandardCharsets.UTF_8) +
                 "&query=" + query + "&language=zh-CN&include_adult=false";

    HttpRequest request = HttpRequest.newBuilder(URI.create(uri))
                              .GET()
                              .header("Accept", "application/json")
                              .build();

    HttpResponse<String> response;
    try {
      response = httpClient.send(
          request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
    } catch (InterruptedException exception) {
      Thread.currentThread().interrupt();
      throw new IOException("TMDB search interrupted", exception);
    }

    if (response.statusCode() / 100 != 2) {
      return Optional.empty();
    }

    SearchResponse searchResponse =
        gson.fromJson(response.body(), SearchResponse.class);
    if (searchResponse == null || searchResponse.results == null) {
      return Optional.empty();
    }

    return searchResponse.results.stream()
        .map(result -> result.posterPath)
        .filter(path -> path != null && !path.isBlank())
        .findFirst()
        .map(path -> IMAGE_BASE_URL + path);
  }

  private static final class SearchResponse {
    private List<MovieResult> results;
  }

  private static final class MovieResult {
    @SerializedName("poster_path") private String posterPath;
  }
}
