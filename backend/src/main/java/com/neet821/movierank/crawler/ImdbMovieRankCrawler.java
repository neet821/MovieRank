package com.neet821.movierank.crawler;

import com.neet821.movierank.model.SourceMovieRank;
import com.neet821.movierank.model.RankingSource;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;

@Component
public class ImdbMovieRankCrawler implements MovieRankCrawler {
    private static final String IMDB_URL = "https://www.imdb.com/chart/top/";
    private static final String CSV_FALLBACK_URL = "https://raw.githubusercontent.com/itiievskyi/IMDB-Top-250/master/imdb_top_250.csv";
    private static final String FALLBACK_URL = "https://de.wikipedia.org/wiki/IMDb_Top_250_Movies";

    @Override
    public String getSourceName() {
        return RankingSource.IMDB.getDisplayName();
    }

    @Override
    public List<SourceMovieRank> crawl() {
        try {
            List<SourceMovieRank> officialRows = parseOfficial(fetchHtml(IMDB_URL), IMDB_URL);
            if (!officialRows.isEmpty()) {
                return officialRows;
            }
        } catch (IOException ignored) {
            // IMDb often returns a WAF challenge to non-browser clients. Use public mirror tables below.
        }
        try {
            List<SourceMovieRank> csvRows = parseCsvFallback(fetchText(CSV_FALLBACK_URL));
            if (!csvRows.isEmpty()) {
                return csvRows;
            }
        } catch (IOException ignored) {
            // Keep the older public table as a final fallback.
        }
        try {
            return parseWikipediaFallback(fetchHtml(FALLBACK_URL));
        } catch (IOException exception) {
            throw new IllegalStateException("IMDb Top 250 爬取失败", exception);
        }
    }

    public List<SourceMovieRank> parseOfficial(String html, String pageUrl) {
        Document document = Jsoup.parse(html, pageUrl);
        return document.select("li.ipc-metadata-list-summary-item")
                .stream()
                .map(item -> {
                    String text = text(item, "h3");
                    int rank = parseLeadingNumber(text);
                    String title = text.replaceFirst("^\\s*\\d+\\.\\s*", "").trim();
                    int year = parseYear(item.text());
                    Element link = item.selectFirst("a[href]");
                    String detailUrl = link == null ? pageUrl : link.absUrl("href");
                    return new SourceMovieRank(RankingSource.IMDB, title, year, rank, detailUrl);
                })
                .filter(rank -> rank.getSourceRank() > 0 && !rank.getTitle().isBlank())
                .sorted(Comparator.comparingInt(SourceMovieRank::getSourceRank))
                .limit(RankingSource.IMDB.getExpectedSize())
                .toList();
    }

    public List<SourceMovieRank> parseWikipediaFallback(String html) {
        Document document = Jsoup.parse(html, FALLBACK_URL);
        return document.select("table.wikitable tr")
                .stream()
                .skip(1)
                .map(this::fromWikiRow)
                .filter(rank -> rank.getSourceRank() > 0 && !rank.getTitle().isBlank())
                .sorted(Comparator.comparingInt(SourceMovieRank::getSourceRank))
                .limit(RankingSource.IMDB.getExpectedSize())
                .toList();
    }

    public List<SourceMovieRank> parseCsvFallback(String csv) {
        return csv.lines()
                .skip(1)
                .map(this::fromCsvRow)
                .filter(rank -> rank.getSourceRank() > 0 && !rank.getTitle().isBlank())
                .sorted(Comparator.comparingInt(SourceMovieRank::getSourceRank))
                .limit(RankingSource.IMDB.getExpectedSize())
                .toList();
    }

    private SourceMovieRank fromWikiRow(Element row) {
        List<Element> cells = row.select("td");
        if (cells.size() < 3) {
            return new SourceMovieRank(RankingSource.IMDB, "", 0, 0, FALLBACK_URL);
        }
        int rank = parseNumber(cells.get(0).text());
        String title = cells.get(1).text().trim();
        int year = parseYear(cells.get(2).text());
        return new SourceMovieRank(RankingSource.IMDB, title, year, rank, FALLBACK_URL);
    }

    private SourceMovieRank fromCsvRow(String row) {
        List<String> cells = parseCsvRow(row);
        if (cells.size() < 10) {
            return new SourceMovieRank(RankingSource.IMDB, "", 0, 0, CSV_FALLBACK_URL);
        }
        int rank = parseNumber(cells.get(0));
        String title = cells.get(1).trim();
        int year = parseYear(cells.get(2));
        String detailUrl = cells.get(9).trim().isBlank() ? CSV_FALLBACK_URL : cells.get(9).trim();
        return new SourceMovieRank(RankingSource.IMDB, title, year, rank, detailUrl);
    }

    private List<String> parseCsvRow(String row) {
        java.util.ArrayList<String> cells = new java.util.ArrayList<>();
        StringBuilder cell = new StringBuilder();
        boolean quoted = false;
        for (int index = 0; index < row.length(); index++) {
            char current = row.charAt(index);
            if (current == '"') {
                if (quoted && index + 1 < row.length() && row.charAt(index + 1) == '"') {
                    cell.append('"');
                    index++;
                } else {
                    quoted = !quoted;
                }
            } else if (current == ',' && !quoted) {
                cells.add(cell.toString());
                cell.setLength(0);
            } else {
                cell.append(current);
            }
        }
        cells.add(cell.toString());
        return cells;
    }

    private String fetchHtml(String url) throws IOException {
        return Jsoup.connect(url)
                .userAgent("Mozilla/5.0 MovieRank")
                .timeout(15000)
                .get()
                .html();
    }

    private String fetchText(String url) throws IOException {
        return Jsoup.connect(url)
                .userAgent("Mozilla/5.0 MovieRank")
                .ignoreContentType(true)
                .timeout(15000)
                .execute()
                .body();
    }

    private String text(Element root, String selector) {
        Element element = root.selectFirst(selector);
        return element == null ? "" : element.text().trim();
    }

    private int parseLeadingNumber(String value) {
        java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("^\\s*(\\d+)").matcher(value);
        return matcher.find() ? Integer.parseInt(matcher.group(1)) : 0;
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
