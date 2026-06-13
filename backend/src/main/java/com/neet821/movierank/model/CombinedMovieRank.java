package com.neet821.movierank.model;

import java.util.List;
import java.util.Map;

public class CombinedMovieRank {
    private String id;
    private String title;
    private int year;
    private String modeName;
    private double finalScore;
    private int finalRank;
    private List<String> presentSources;
    private List<String> missingSources;
    private Map<String, Integer> sourceRanks;
    private Map<String, Double> sourceWeights;

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

    public String getModeName() {
        return modeName;
    }

    public void setModeName(String modeName) {
        this.modeName = modeName;
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

    public List<String> getPresentSources() {
        return presentSources;
    }

    public void setPresentSources(List<String> presentSources) {
        this.presentSources = presentSources;
    }

    public List<String> getMissingSources() {
        return missingSources;
    }

    public void setMissingSources(List<String> missingSources) {
        this.missingSources = missingSources;
    }

    public Map<String, Integer> getSourceRanks() {
        return sourceRanks;
    }

    public void setSourceRanks(Map<String, Integer> sourceRanks) {
        this.sourceRanks = sourceRanks;
    }

    public Map<String, Double> getSourceWeights() {
        return sourceWeights;
    }

    public void setSourceWeights(Map<String, Double> sourceWeights) {
        this.sourceWeights = sourceWeights;
    }
}
