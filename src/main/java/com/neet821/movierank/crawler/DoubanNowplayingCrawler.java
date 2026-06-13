package com.neet821.movierank.crawler;

import com.neet821.movierank.model.NowPlayingMovie;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class DoubanNowplayingCrawler {

    private static final String NOWPLAYING_URL = "https://movie.douban.com/cinema/nowplaying/wuhan/";
    private static final Logger log = LoggerFactory.getLogger(DoubanNowplayingCrawler.class);

    public List<NowPlayingMovie> crawl() throws IOException {
        Document doc = Jsoup.connect(NOWPLAYING_URL)
                .userAgent("MovieRank/1.0 (+https://localhost)")
                .timeout(15000)
                .get();
        log.info("Fetched page, #nowplaying exists={}, .list-item count={}",
                !doc.select("#nowplaying").isEmpty(),
                doc.select(".list-item").size());
        return parse(doc);
    }

    public List<NowPlayingMovie> parse(Document doc) {
        List<NowPlayingMovie> movies = new ArrayList<>();

        // 尝试多个选择器，按优先级排列
        Elements items = doc.select("#nowplaying .lists > li.list-item");
        log.info("Selector '#nowplaying .lists > li.list-item' matched {} items", items.size());
        if (items.isEmpty()) {
            items = doc.select("#nowplaying li.list-item");
            log.info("Selector '#nowplaying li.list-item' matched {} items", items.size());
        }
        if (items.isEmpty()) {
            log.info("All .list-item on page: {}", doc.select(".list-item").size());
        }

        for (Element item : items) {
            NowPlayingMovie movie = new NowPlayingMovie();

            // 电影名 — 优先用 data-title 属性（完整名），否则用 .stitle a 的 title 属性
            String title = item.attr("data-title");
            if (title.isBlank()) {
                Element titleLink = item.selectFirst(".stitle a");
                if (titleLink != null) {
                    title = titleLink.attr("title");
                    if (title.isBlank()) {
                        title = titleLink.text().trim();
                    }
                }
            }
            movie.setTitle(title);

            // 详情页链接 — 从 .poster a 或 data-subject 构建
            Element posterLink = item.selectFirst(".poster a");
            if (posterLink != null) {
                String href = posterLink.attr("href");
                if (!href.startsWith("http")) {
                    href = "https://movie.douban.com" + href;
                }
                movie.setDetailUrl(href);
            } else {
                String subjectId = item.attr("data-subject");
                if (!subjectId.isBlank()) {
                    movie.setDetailUrl("https://movie.douban.com/subject/" + subjectId + "/");
                }
            }

            // 海报图片
            Element posterImg = item.selectFirst(".poster img");
            if (posterImg != null) {
                String src = posterImg.attr("src");
                if (src.isBlank()) {
                    src = posterImg.attr("data-src");
                }
                movie.setPosterUrl(src);
            }

            // 评分 — .srating .subject-rate 或 .srating .text-tip 或 data-score 属性
            Element ratingEl = item.selectFirst(".srating .subject-rate");
            if (ratingEl != null) {
                movie.setRating(ratingEl.text().trim());
            } else {
                Element textTip = item.selectFirst(".srating .text-tip");
                if (textTip != null) {
                    movie.setRating(textTip.text().trim());
                } else {
                    String score = item.attr("data-score");
                    if (!score.isBlank()) {
                        movie.setRating(score);
                    }
                }
            }

            // 购票链接
            Element ticketLink = item.selectFirst(".sbtn a");
            if (ticketLink != null) {
                movie.setTicketUrl(ticketLink.attr("href"));
            }

            if (movie.getTitle() != null && !movie.getTitle().isBlank()) {
                movies.add(movie);
            }
        }

        return movies;
    }
}
