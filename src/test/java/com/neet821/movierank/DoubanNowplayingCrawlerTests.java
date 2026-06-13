package com.neet821.movierank;

import com.neet821.movierank.crawler.DoubanNowplayingCrawler;
import com.neet821.movierank.crawler.TmdbMovieClient;
import com.neet821.movierank.model.NowPlayingMovie;
import org.jsoup.Jsoup;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class DoubanNowplayingCrawlerTests {

    @Test
    void parseUsesTmdbPosterAndDetailsWhenTmdbHasMatch() {
        String html = """
                <div id="nowplaying">
                    <ul class="lists">
                        <li class="list-item" data-title="测试电影" data-subject="123456" data-score="8.6">
                            <ul class="poster">
                                <li><a href="/subject/123456/"><img src=""></a></li>
                            </ul>
                        </li>
                    </ul>
                </div>
                """;
        TmdbMovieClient tmdb = title -> Optional.of(new TmdbMovieClient.MovieSummary(
                99,
                "测试电影",
                "https://image.tmdb.org/t/p/w500/test.jpg",
                "https://www.themoviedb.org/movie/99",
                "这是一段 TMDB 简介",
                "2026-06-11",
                8.3,
                123.4
        ));

        List<NowPlayingMovie> movies = new DoubanNowplayingCrawler(tmdb).parse(Jsoup.parse(html));

        assertThat(movies).hasSize(1);
        assertThat(movies.get(0).getPosterUrl()).isEqualTo("https://image.tmdb.org/t/p/w500/test.jpg");
        assertThat(movies.get(0).getPosterSource()).isEqualTo("TMDB");
        assertThat(movies.get(0).getTmdbUrl()).isEqualTo("https://www.themoviedb.org/movie/99");
        assertThat(movies.get(0).getOverview()).isEqualTo("这是一段 TMDB 简介");
        assertThat(movies.get(0).getReleaseDate()).isEqualTo("2026-06-11");
        assertThat(movies.get(0).getTmdbRating()).isEqualTo(8.3);
        assertThat(movies.get(0).getPopularity()).isEqualTo(123.4);
        assertThat(movies.get(0).getDetailUrl()).isEqualTo("https://movie.douban.com/subject/123456/");
    }

    @Test
    void parseKeepsDoubanPosterWhenTmdbHasNoMatch() {
        String html = """
                <div id="nowplaying">
                    <ul class="lists">
                        <li class="list-item" data-title="测试电影" data-subject="123456">
                            <ul class="poster">
                                <li><a href="/subject/123456/"><img src="https://img.doubanio.com/view/photo/s_ratio_poster/public/p1.jpg"></a></li>
                            </ul>
                        </li>
                    </ul>
                </div>
                """;
        TmdbMovieClient tmdb = title -> Optional.empty();

        List<NowPlayingMovie> movies = new DoubanNowplayingCrawler(tmdb).parse(Jsoup.parse(html));

        assertThat(movies).hasSize(1);
        assertThat(movies.get(0).getPosterUrl()).contains("img.doubanio.com");
        assertThat(movies.get(0).getPosterSource()).isEqualTo("豆瓣");
    }
}
