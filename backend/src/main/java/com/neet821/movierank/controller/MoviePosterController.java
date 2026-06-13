package com.neet821.movierank.controller;

import com.neet821.movierank.client.TmdbPosterClient;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MoviePosterController {

  private final HttpClient httpClient;
  private final TmdbPosterClient tmdbPosterClient;

  public MoviePosterController(TmdbPosterClient tmdbPosterClient) {
    this.tmdbPosterClient = tmdbPosterClient;
    this.httpClient = HttpClient.newBuilder()
                          .followRedirects(HttpClient.Redirect.NORMAL)
                          .build();
  }

  @GetMapping("/posters/nowplaying")
  public ResponseEntity<byte[]> getPoster(@RequestParam String title,
                                          @RequestParam(required = false)
                                          String doubanUrl) throws IOException {
    ResponseEntity<byte[]> doubanPoster = fetchImage(doubanUrl);
    if (doubanPoster != null) {
      return doubanPoster;
    }

    String tmdbPosterUrl = tmdbPosterClient.findPosterUrl(title).orElse(null);
    ResponseEntity<byte[]> tmdbPoster = fetchImage(tmdbPosterUrl);
    if (tmdbPoster != null) {
      return tmdbPoster;
    }

    return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
  }

  private ResponseEntity<byte[]> fetchImage(String imageUrl)
      throws IOException {
    if (imageUrl == null || imageUrl.isBlank()) {
      return null;
    }

    String encodedUrl = imageUrl;
    if (imageUrl.startsWith("http")) {
      encodedUrl = imageUrl;
    }

    HttpRequest request =
        HttpRequest.newBuilder(URI.create(encodedUrl))
            .GET()
            .header("User-Agent", "MovieRank/1.0 (+https://localhost)")
            .header("Referer", "https://movie.douban.com/")
            .build();

    HttpResponse<byte[]> response;
    try {
      response =
          httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
    } catch (InterruptedException exception) {
      Thread.currentThread().interrupt();
      throw new IOException("Image fetch interrupted", exception);
    }

    if (response.statusCode() / 100 != 2) {
      return null;
    }

    MediaType contentType = resolveContentType(
        response.headers().firstValue("content-type").orElse(""));
    if (contentType == null ||
        !contentType.getType().equalsIgnoreCase("image")) {
      return null;
    }

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(contentType);
    headers.setContentLength(response.body().length);
    return new ResponseEntity<>(response.body(), headers, HttpStatus.OK);
  }

  private MediaType resolveContentType(String contentType) {
    if (contentType == null || contentType.isBlank()) {
      return MediaType.IMAGE_JPEG;
    }
    try {
      return MediaType.parseMediaType(contentType);
    } catch (IllegalArgumentException exception) {
      return MediaType.IMAGE_JPEG;
    }
  }
}
