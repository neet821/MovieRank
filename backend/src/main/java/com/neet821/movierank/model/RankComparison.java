package com.neet821.movierank.model;

import java.util.List;

public class RankComparison {
    private String sourceA;
    private String sourceB;
    private int sharedCount;
    private List<SharedRankGap> sharedMovies;
    private List<SourceMovieRank> onlyInSourceA;
    private List<SourceMovieRank> onlyInSourceB;
    private List<SharedRankGap> largestRankGaps;

    public String getSourceA() {
        return sourceA;
    }

    public void setSourceA(String sourceA) {
        this.sourceA = sourceA;
    }

    public String getSourceB() {
        return sourceB;
    }

    public void setSourceB(String sourceB) {
        this.sourceB = sourceB;
    }

    public int getSharedCount() {
        return sharedCount;
    }

    public void setSharedCount(int sharedCount) {
        this.sharedCount = sharedCount;
    }

    public List<SharedRankGap> getSharedMovies() {
        return sharedMovies;
    }

    public void setSharedMovies(List<SharedRankGap> sharedMovies) {
        this.sharedMovies = sharedMovies;
    }

    public List<SourceMovieRank> getOnlyInSourceA() {
        return onlyInSourceA;
    }

    public void setOnlyInSourceA(List<SourceMovieRank> onlyInSourceA) {
        this.onlyInSourceA = onlyInSourceA;
    }

    public List<SourceMovieRank> getOnlyInSourceB() {
        return onlyInSourceB;
    }

    public void setOnlyInSourceB(List<SourceMovieRank> onlyInSourceB) {
        this.onlyInSourceB = onlyInSourceB;
    }

    public List<SharedRankGap> getLargestRankGaps() {
        return largestRankGaps;
    }

    public void setLargestRankGaps(List<SharedRankGap> largestRankGaps) {
        this.largestRankGaps = largestRankGaps;
    }
}
