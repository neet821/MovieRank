package com.neet821.movierank.model;

import lombok.Data;

@Data
public class NowPlayingMovie {
    private String title;
    private String rating;
    private String posterUrl;
    private String posterSource;
    private String detailUrl;
    private String tmdbUrl;
    private String overview;
    private String releaseDate;
    private double tmdbRating;
    private double popularity;
    private String ticketUrl;
}
