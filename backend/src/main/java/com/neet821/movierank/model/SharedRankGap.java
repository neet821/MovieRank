package com.neet821.movierank.model;

public class SharedRankGap {
    private String title;
    private int year;
    private int sourceARank;
    private int sourceBRank;
    private int rankGap;

    public SharedRankGap(String title, int year, int sourceARank, int sourceBRank) {
        this.title = title;
        this.year = year;
        this.sourceARank = sourceARank;
        this.sourceBRank = sourceBRank;
        this.rankGap = Math.abs(sourceARank - sourceBRank);
    }

    public String getTitle() {
        return title;
    }

    public int getYear() {
        return year;
    }

    public int getSourceARank() {
        return sourceARank;
    }

    public int getSourceBRank() {
        return sourceBRank;
    }

    public int getRankGap() {
        return rankGap;
    }
}
