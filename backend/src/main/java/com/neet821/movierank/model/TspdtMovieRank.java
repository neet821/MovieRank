package com.neet821.movierank.model;

import lombok.Data;

@Data
public class TspdtMovieRank {
    private int position;
    private int previousRank;
    private String title;
    private String director;
    private String year;
    private String country;
    private int minutes;
}
