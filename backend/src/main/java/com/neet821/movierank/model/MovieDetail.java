package com.neet821.movierank.model;

import java.util.Map;

public class MovieDetail {
    private String id;
    private String title;
    private int year;
    private String overview;
    private String posterUrl;
    private String releaseDate;
    private double tmdbRating;
    private Map<String, Integer> sourceRanks;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public String getOverview() {
        return overview;
    }

    public void setOverview(String overview) {
        this.overview = overview;
    }

    public String getPosterUrl() {
        return posterUrl;
    }

    public void setPosterUrl(String posterUrl) {
        this.posterUrl = posterUrl;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }

    public double getTmdbRating() {
        return tmdbRating;
    }

    public void setTmdbRating(double tmdbRating) {
        this.tmdbRating = tmdbRating;
    }

    public Map<String, Integer> getSourceRanks() {
        return sourceRanks;
    }

    public void setSourceRanks(Map<String, Integer> sourceRanks) {
        this.sourceRanks = sourceRanks;
    }
}
