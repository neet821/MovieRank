package com.neet821.movierank.model;

public class SourceMovieRank {
    private String sourceName;
    private String title;
    private int year;
    private int sourceRank;
    private double sourceScore;
    private String detailUrl;

    public SourceMovieRank() {
    }

    public SourceMovieRank(String sourceName, String title, int year, int sourceRank, double sourceScore, String detailUrl) {
        this.sourceName = sourceName;
        this.title = title;
        this.year = year;
        this.sourceRank = sourceRank;
        this.sourceScore = sourceScore;
        this.detailUrl = detailUrl;
    }

    public String getSourceName() {
        return sourceName;
    }

    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
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

    public int getSourceRank() {
        return sourceRank;
    }

    public void setSourceRank(int sourceRank) {
        this.sourceRank = sourceRank;
    }

    public double getSourceScore() {
        return sourceScore;
    }

    public void setSourceScore(double sourceScore) {
        this.sourceScore = sourceScore;
    }

    public String getDetailUrl() {
        return detailUrl;
    }

    public void setDetailUrl(String detailUrl) {
        this.detailUrl = detailUrl;
    }
}
