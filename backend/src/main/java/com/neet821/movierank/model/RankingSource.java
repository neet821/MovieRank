package com.neet821.movierank.model;

public enum RankingSource {
    IMDB("IMDb", 250),
    DOUBAN("豆瓣", 250),
    LETTERBOXD("Letterboxd", 500),
    BFI("BFI Sight and Sound", 264),
    TSPDT("TSPDT", 1000);

    private final String displayName;
    private final int expectedSize;

    RankingSource(String displayName, int expectedSize) {
        this.displayName = displayName;
        this.expectedSize = expectedSize;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getExpectedSize() {
        return expectedSize;
    }

    public static RankingSource fromValue(String value) {
        for (RankingSource source : values()) {
            if (source.name().equalsIgnoreCase(value) || source.displayName.equalsIgnoreCase(value)) {
                return source;
            }
        }
        throw new IllegalArgumentException("Unknown ranking source: " + value);
    }
}
