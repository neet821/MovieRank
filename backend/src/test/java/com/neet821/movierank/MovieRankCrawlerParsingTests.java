package com.neet821.movierank;

import com.neet821.movierank.crawler.BfiSightAndSoundCrawler;
import com.neet821.movierank.crawler.DoubanMovieRankCrawler;
import com.neet821.movierank.crawler.ImdbMovieRankCrawler;
import com.neet821.movierank.crawler.LetterboxdMovieRankCrawler;
import com.neet821.movierank.model.SourceMovieRank;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MovieRankCrawlerParsingTests {
    @Test
    void doubanParserReadsRankTitleAndYear() {
        String html = """
                <ol class="grid_view">
                  <li><div class="item">
                    <em>1</em>
                    <a href="https://movie.douban.com/subject/1292052/">
                      <span class="title">肖申克的救赎</span>
                      <span class="title">&nbsp;/&nbsp;The Shawshank Redemption</span>
                    </a>
                    <p>1994&nbsp;/&nbsp;美国&nbsp;/&nbsp;犯罪 剧情</p>
                  </div></li>
                </ol>
                """;

        List<SourceMovieRank> rows = new DoubanMovieRankCrawler().parse(html, "https://movie.douban.com/top250");

        assertThat(rows).hasSize(1);
        assertThat(rows.get(0).getTitle()).isEqualTo("肖申克的救赎");
        assertThat(rows.get(0).getAliases()).containsExactly("The Shawshank Redemption");
        assertThat(rows.get(0).getYear()).isEqualTo(1994);
        assertThat(rows.get(0).getSourceRank()).isEqualTo(1);
    }

    @Test
    void letterboxdParserReadsOfficialTop500ListItems() {
        String html = """
                <ul class="poster-list">
                  <li class="posteritem numbered-list-item">
                    <div class="react-component" data-item-name="Harakiri (1962)" data-target-link="/film/harakiri/"></div>
                    <p class="list-number">1</p>
                  </li>
                </ul>
                """;

        List<SourceMovieRank> rows = new LetterboxdMovieRankCrawler().parse(html, "https://letterboxd.com/official/list/letterboxds-top-500-films/");

        assertThat(rows).hasSize(1);
        assertThat(rows.get(0).getTitle()).isEqualTo("Harakiri");
        assertThat(rows.get(0).getYear()).isEqualTo(1962);
        assertThat(rows.get(0).getSourceRank()).isEqualTo(1);
    }

    @Test
    void bfiParserReadsSightAndSoundCards() {
        String html = """
                <article>
                  <a href="/film/00638537/jeanne-dielman">
                    <h1>Jeanne Dielman, 23 Quai du Commerce, 1080 Bruxelles</h1>
                    <p class="PreviewCard__label">1</p>
                    <p>1975 Belgium, France</p>
                    <p>Directed by Chantal Akerman</p>
                  </a>
                </article>
                """;

        List<SourceMovieRank> rows = new BfiSightAndSoundCrawler().parse(html);

        assertThat(rows).hasSize(1);
        assertThat(rows.get(0).getTitle()).isEqualTo("Jeanne Dielman, 23 Quai du Commerce, 1080 Bruxelles");
        assertThat(rows.get(0).getYear()).isEqualTo(1975);
        assertThat(rows.get(0).getSourceRank()).isEqualTo(1);
    }

    @Test
    void imdbCsvFallbackParserReadsEnglishTable() {
        String csv = """
                ,Title,Year,Genre,Duration,Origin,Director,IMDB rating,Rating count,IMDB link
                1,The Shawshank Redemption,1994,Drama,2h 22min,USA,Frank Darabont,9.3,2030817,https://www.imdb.com/title/tt0111161
                9,"Il buono, il brutto, il cattivo",1966,Western,2h 41min,Italy,Sergio Leone,8.9,602707,https://www.imdb.com/title/tt0060196
                """;

        List<SourceMovieRank> rows = new ImdbMovieRankCrawler().parseCsvFallback(csv);

        assertThat(rows).hasSize(2);
        assertThat(rows.get(0).getTitle()).isEqualTo("The Shawshank Redemption");
        assertThat(rows.get(0).getYear()).isEqualTo(1994);
        assertThat(rows.get(0).getSourceRank()).isEqualTo(1);
        assertThat(rows.get(1).getTitle()).isEqualTo("Il buono, il brutto, il cattivo");
    }

    @Test
    void imdbFallbackParserReadsWikiTable() {
        String html = """
                <table class="wikitable">
                  <tr><th>Platz</th><th>Filmtitel</th><th>Jahr</th></tr>
                  <tr><td>1</td><td>Die Verurteilten</td><td>1994</td></tr>
                </table>
                """;

        List<SourceMovieRank> rows = new ImdbMovieRankCrawler().parseWikipediaFallback(html);

        assertThat(rows).hasSize(1);
        assertThat(rows.get(0).getTitle()).isEqualTo("Die Verurteilten");
        assertThat(rows.get(0).getYear()).isEqualTo(1994);
        assertThat(rows.get(0).getSourceRank()).isEqualTo(1);
    }
}
