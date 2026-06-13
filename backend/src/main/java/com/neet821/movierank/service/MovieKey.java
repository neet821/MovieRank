package com.neet821.movierank.service;

import com.neet821.movierank.model.SourceMovieRank;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

record MovieKey(String title, int year) {
    static MovieKey from(SourceMovieRank rank) {
        List<MovieKey> keys = fromAllNames(rank);
        return keys.isEmpty() ? new MovieKey("", rank.getYear()) : keys.get(0);
    }

    static List<MovieKey> fromAllNames(SourceMovieRank rank) {
        List<MovieKey> keys = new ArrayList<>();
        for (String alias : rank.getAliases()) {
            addKey(keys, alias, rank.getYear());
        }
        addKey(keys, rank.getTitle(), rank.getYear());
        return keys;
    }

    static String slug(String title, int year) {
        return normalize(title).replace(' ', '-') + "-" + year;
    }

    static String slug(SourceMovieRank rank) {
        String value = rank.getAliases().isEmpty() ? rank.getTitle() : rank.getAliases().get(0);
        return slug(value, rank.getYear());
    }

    private static void addKey(List<MovieKey> keys, String title, int year) {
        String normalized = normalize(title);
        if (normalized.isBlank()) {
            return;
        }
        MovieKey key = new MovieKey(normalized, year);
        if (!keys.contains(key)) {
            keys.add(key);
        }
    }

    private static String normalize(String title) {
        String value = title == null ? "" : title;
        return Normalizer.normalize(value, Normalizer.Form.NFKD)
                .replaceAll("\\p{M}", "")
                .replaceAll("[^\\p{L}\\p{N}]+", " ")
                .trim()
                .toLowerCase(Locale.ROOT);
    }
}
