package com.neet821.movierank.model;

import java.util.List;

public class WeightedRankResult {
    private String title;
    private int year;
    private double doubanScore;
    private double imdbScore;
    private double maoyanScore;
    private double finalScore;
    private int finalRank;
    private List<String> missingSources;

    public WeightedRankResult() {
    }

    public WeightedRankResult(String title, int year, double doubanScore, double imdbScore, double maoyanScore,
                              double finalScore, int finalRank, List<String> missingSources) {
        this.title = title;
        this.year = year;
        this.doubanScore = doubanScore;
        this.imdbScore = imdbScore;
        this.maoyanScore = maoyanScore;
        this.finalScore = finalScore;
        this.finalRank = finalRank;
        this.missingSources = missingSources;
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

    public double getDoubanScore() {
        return doubanScore;
    }

    public void setDoubanScore(double doubanScore) {
        this.doubanScore = doubanScore;
    }

    public double getImdbScore() {
        return imdbScore;
    }

    public void setImdbScore(double imdbScore) {
        this.imdbScore = imdbScore;
    }

    public double getMaoyanScore() {
        return maoyanScore;
    }

    public void setMaoyanScore(double maoyanScore) {
        this.maoyanScore = maoyanScore;
    }

    public double getFinalScore() {
        return finalScore;
    }

    public void setFinalScore(double finalScore) {
        this.finalScore = finalScore;
    }

    public int getFinalRank() {
        return finalRank;
    }

    public void setFinalRank(int finalRank) {
        this.finalRank = finalRank;
    }

    public List<String> getMissingSources() {
        return missingSources;
    }

    public void setMissingSources(List<String> missingSources) {
        this.missingSources = missingSources;
    }
}
