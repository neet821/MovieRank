package com.neet821.movierank.crawler;

import com.neet821.movierank.model.TspdtMovieRank;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Component
public class TspdtTop1000Crawler {
    public static final String SOURCE_URL = "https://theyshootpictures.com/gf1000_all1000films_table.php";

    public List<TspdtMovieRank> crawl() throws IOException {
        String html = Jsoup.connect(SOURCE_URL)
                .userAgent("MovieRank/1.0 (+https://localhost)")
                .timeout(15000)
                .get()
                .html();

        return parse(html);
    }

    public List<TspdtMovieRank> parse(String html) {
        return Jsoup.parse(html)
                .select("table tbody tr")
                .stream()
                .map(this::toMovieRank)
                .filter(movie -> movie.getPosition() > 0)
                .limit(1000)
                .toList();
    }

    private TspdtMovieRank toMovieRank(Element row) {
        List<String> cells = row.select("td")
                .stream()
                .map(Element::text)
                .map(String::trim)
                .toList();

        if (cells.size() < 7) {
            return new TspdtMovieRank();
        }

        TspdtMovieRank movieRank = new TspdtMovieRank();
        movieRank.setPosition(parseNumber(cells.get(0)));
        movieRank.setPreviousRank(parseNumber(cells.get(1)));
        movieRank.setTitle(cells.get(2));
        movieRank.setDirector(cells.get(3));
        movieRank.setYear(cells.get(4));
        movieRank.setCountry(cells.get(5));
        movieRank.setMinutes(parseNumber(cells.get(6)));
        return movieRank;
    }

    private int parseNumber(String value) {
        String digits = value.replaceAll("[^0-9]", "");
        if (digits.isBlank()) {
            return 0;
        }
        return Integer.parseInt(digits);
    }
}
