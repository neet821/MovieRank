package com.neet821.movierank.crawler;

import com.neet821.movierank.model.RankingSource;
import com.neet821.movierank.model.SourceMovieRank;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;

@Component
public class BfiSightAndSoundCrawler implements MovieRankCrawler {
    private static final String SOURCE_URL = "https://www.bfi.org.uk/sight-and-sound/greatest-films-all-time";

    @Override
    public String getSourceName() {
        return RankingSource.BFI.getDisplayName();
    }

    @Override
    public List<SourceMovieRank> crawl() {
        try {
            return parse(Jsoup.connect(SOURCE_URL)
                    .userAgent("Mozilla/5.0 MovieRank")
                    .timeout(15000)
                    .get()
                    .html());
        } catch (IOException exception) {
            throw new IllegalStateException("BFI Sight and Sound 爬取失败", exception);
        }
    }

    public List<SourceMovieRank> parse(String html) {
        Document document = Jsoup.parse(html, SOURCE_URL);
        return document.select("article")
                .stream()
                .map(this::toMovieRank)
                .filter(rank -> rank.getSourceRank() > 0 && !rank.getTitle().isBlank())
                .sorted(Comparator.comparingInt(SourceMovieRank::getSourceRank))
                .toList();
    }

    private SourceMovieRank toMovieRank(Element article) {
        String title = text(article, "h1");
        int rank = parseNumber(article.select("p").stream()
                .map(Element::text)
                .filter(value -> value.matches("=?\\s*\\d+"))
                .findFirst()
                .orElse(""));
        int year = parseYear(article.select("p").stream()
                .map(Element::text)
                .filter(value -> value.matches(".*(19|20)\\d{2}.*"))
                .findFirst()
                .orElse(""));
        Element link = article.selectFirst("a[href]");
        String detailUrl = link == null ? SOURCE_URL : link.absUrl("href");
        return new SourceMovieRank(RankingSource.BFI, title, year, rank, detailUrl);
    }

    private String text(Element root, String selector) {
        Element element = root.selectFirst(selector);
        return element == null ? "" : element.text().trim();
    }

    private int parseYear(String value) {
        java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("(19|20)\\d{2}").matcher(value);
        return matcher.find() ? Integer.parseInt(matcher.group()) : 0;
    }

    private int parseNumber(String value) {
        String digits = value == null ? "" : value.replaceAll("[^0-9]", "");
        return digits.isBlank() ? 0 : Integer.parseInt(digits);
    }
}
