package com.neet821.movierank.crawler;

import com.neet821.movierank.model.RankingSource;
import com.neet821.movierank.model.SourceMovieRank;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Component
public class LetterboxdMovieRankCrawler implements MovieRankCrawler {
    private static final String BASE_URL = "https://letterboxd.com/official/list/letterboxds-top-500-films/";

    @Override
    public String getSourceName() {
        return RankingSource.LETTERBOXD.getDisplayName();
    }

    @Override
    public List<SourceMovieRank> crawl() {
        List<SourceMovieRank> rows = new ArrayList<>();
        for (int page = 1; page <= 5; page++) {
            String url = page == 1 ? BASE_URL : BASE_URL + "page/" + page + "/";
            try {
                rows.addAll(parse(fetch(url), url));
            } catch (IOException exception) {
                throw new IllegalStateException("Letterboxd Top 500 爬取失败: " + url, exception);
            }
        }
        return rows.stream()
                .sorted(Comparator.comparingInt(SourceMovieRank::getSourceRank))
                .limit(RankingSource.LETTERBOXD.getExpectedSize())
                .toList();
    }

    public List<SourceMovieRank> parse(String html, String pageUrl) {
        Document document = Jsoup.parse(html, pageUrl);
        return document.select("ul.poster-list li")
                .stream()
                .map(item -> toMovieRank(item, pageUrl))
                .filter(rank -> rank.getSourceRank() > 0 && !rank.getTitle().isBlank())
                .toList();
    }

    private SourceMovieRank toMovieRank(Element item, String pageUrl) {
        Element component = item.selectFirst(".react-component[data-item-name]");
        String name = component == null ? "" : component.attr("data-item-name");
        int year = parseYear(name);
        String title = name.replaceAll("\\s*\\((19|20)\\d{2}\\)\\s*$", "").trim();
        int rank = parseNumber(text(item, ".list-number"));
        String detailUrl = component == null ? pageUrl : component.absUrl("data-target-link");
        return new SourceMovieRank(RankingSource.LETTERBOXD, title, year, rank, detailUrl);
    }

    private String fetch(String url) throws IOException {
        return Jsoup.connect(url)
                .userAgent("Mozilla/5.0 MovieRank")
                .timeout(15000)
                .get()
                .html();
    }

    private String text(Element root, String selector) {
        Element element = root.selectFirst(selector);
        return element == null ? "" : element.text();
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
