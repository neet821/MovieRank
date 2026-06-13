package com.neet821.movierank.entity;

import lombok.Data;

@Data
public class MovieRank {
    private String title;
    private int year;
    private double doubanScore;
    private double imdbScore;
    private double maoyanScore;
    private double finalScore;
    private int finalRank;
    private String missingSources;
}
