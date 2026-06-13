package com.neet821.movierank.model;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SourceMovieRank {
    private RankingSource source;
    private String title;
    private int year;
    private int sourceRank;
    private String detailUrl;
    private List<String> aliases = new ArrayList<>();

    public SourceMovieRank() {
    }

    public SourceMovieRank(RankingSource source, String title, int year, int sourceRank, String detailUrl) {
        this.source = source;
        this.title = title;
        this.year = year;
        this.sourceRank = sourceRank;
        this.detailUrl = detailUrl;
    }

    public SourceMovieRank(RankingSource source, String title, int year, int sourceRank, String detailUrl, List<String> aliases) {
        this(source, title, year, sourceRank, detailUrl);
        setAliases(aliases);
    }

    public RankingSource getSource() {
        return source;
    }

    public void setSource(RankingSource source) {
        this.source = source;
    }

    public String getSourceName() {
        return source == null ? "" : source.getDisplayName();
    }

    public String getId() {
        String keyTitle = aliases == null || aliases.isEmpty() ? title : aliases.get(0);
        return normalize(keyTitle).replace(' ', '-') + "-" + year;
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

    public String getDetailUrl() {
        return detailUrl;
    }

    public void setDetailUrl(String detailUrl) {
        this.detailUrl = detailUrl;
    }

    public List<String> getAliases() {
        return aliases;
    }

    public void setAliases(List<String> aliases) {
        this.aliases = aliases == null ? new ArrayList<>() : new ArrayList<>(aliases);
    }

    private String normalize(String value) {
        String titleValue = value == null ? "" : value;
        return Normalizer.normalize(titleValue, Normalizer.Form.NFKD)
                .replaceAll("\\p{M}", "")
                .replaceAll("[^\\p{L}\\p{N}]+", " ")
                .trim()
                .toLowerCase(Locale.ROOT);
    }
}
