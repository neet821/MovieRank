package com.neet821.movierank;

import com.neet821.movierank.crawler.TspdtTop1000Crawler;
import com.neet821.movierank.crawler.TspdtMovieRankCrawler;
import com.neet821.movierank.model.SourceMovieRank;
import com.neet821.movierank.model.TspdtMovieRank;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TspdtTop1000CrawlerTests {
    @Test
    void parseReadsMovieRowsFromTspdtTable() {
        String html = """
                <table>
                    <thead>
                    <tr>
                        <th>Pos</th><th>2025</th><th>Title</th><th>Director</th><th>Year</th><th>Country</th><th>Mins</th>
                    </tr>
                    </thead>
                    <tbody>
                    <tr>
                        <td>1</td><td>1</td><td>Citizen Kane</td><td>Welles, Orson</td><td>1941</td><td>USA</td><td>119</td>
                    </tr>
                    <tr>
                        <td>2</td><td>2</td><td>Vertigo</td><td>Hitchcock, Alfred</td><td>1958</td><td>USA</td><td>128</td>
                    </tr>
                    </tbody>
                </table>
                """;

        List<TspdtMovieRank> movies = new TspdtTop1000Crawler().parse(html);

        assertThat(movies).hasSize(2);
        assertThat(movies.get(0).getPosition()).isEqualTo(1);
        assertThat(movies.get(0).getPreviousRank()).isEqualTo(1);
        assertThat(movies.get(0).getTitle()).isEqualTo("Citizen Kane");
        assertThat(movies.get(0).getDirector()).isEqualTo("Welles, Orson");
        assertThat(movies.get(0).getYear()).isEqualTo("1941");
        assertThat(movies.get(0).getCountry()).isEqualTo("USA");
        assertThat(movies.get(0).getMinutes()).isEqualTo(119);
    }

    @Test
    void parseKeepsOnlyFirstThousandRows() {
        StringBuilder html = new StringBuilder("<table><tbody>");
        for (int i = 1; i <= 1001; i++) {
            html.append("""
                    <tr>
                        <td>%d</td><td>%d</td><td>Movie %d</td><td>Director</td><td>2026</td><td>USA</td><td>100</td>
                    </tr>
                    """.formatted(i, i, i));
        }
        html.append("</tbody></table>");

        List<TspdtMovieRank> movies = new TspdtTop1000Crawler().parse(html.toString());

        assertThat(movies).hasSize(1000);
        assertThat(movies.get(movies.size() - 1).getPosition()).isEqualTo(1000);
    }

    @Test
    void adapterKeepsParsedTspdtMovieTitlesAndRanks() {
        TspdtMovieRank citizenKane = new TspdtMovieRank();
        citizenKane.setPosition(1);
        citizenKane.setTitle("Citizen Kane");
        citizenKane.setYear("1941");

        List<SourceMovieRank> rows = new TspdtMovieRankCrawler(new TspdtTop1000Crawler())
                .toSourceRanks(List.of(citizenKane));

        assertThat(rows).hasSize(1);
        assertThat(rows.get(0).getTitle()).isEqualTo("Citizen Kane");
        assertThat(rows.get(0).getYear()).isEqualTo(1941);
        assertThat(rows.get(0).getSourceRank()).isEqualTo(1);
        assertThat(rows.get(0).getSourceName()).isEqualTo("TSPDT");
    }
}
