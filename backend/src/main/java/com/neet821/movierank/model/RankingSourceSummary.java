package com.neet821.movierank.model;

public class RankingSourceSummary {
    private String code;
    private String name;
    private int expectedSize;
    private int loadedSize;

    public RankingSourceSummary(String code, String name, int expectedSize, int loadedSize) {
        this.code = code;
        this.name = name;
        this.expectedSize = expectedSize;
        this.loadedSize = loadedSize;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public int getExpectedSize() {
        return expectedSize;
    }

    public int getLoadedSize() {
        return loadedSize;
    }
}
