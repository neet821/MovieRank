package com.neet821.movierank;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class MovieRankControllerTests {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @AfterEach
    void restoreMovieRankRows() {
        jdbcTemplate.update("delete from movie_rank");
        jdbcTemplate.update("""
                insert into movie_rank
                    (title, year, douban_score, imdb_score, maoyan_score, final_score, final_rank, missing_sources)
                values
                    ('肖申克的救赎', 1994, 97.00, 93.00, 95.00, 95.10, 1, ''),
                    ('霸王别姬', 1993, 96.00, 81.00, 96.00, 90.75, 2, '')
                """);
    }

    @Test
    void movieRanksComeFromDatabaseRows() throws Exception {
        jdbcTemplate.execute("""
                create table if not exists movie_rank (
                    id bigint primary key auto_increment,
                    title varchar(100) not null,
                    year int not null,
                    douban_score decimal(5,2) not null,
                    imdb_score decimal(5,2) not null,
                    maoyan_score decimal(5,2) not null,
                    final_score decimal(5,2) not null,
                    final_rank int not null,
                    missing_sources varchar(200) not null default ''
                )
                """);
        jdbcTemplate.update("delete from movie_rank");
        jdbcTemplate.update("""
                insert into movie_rank
                    (title, year, douban_score, imdb_score, maoyan_score, final_score, final_rank, missing_sources)
                values
                    ('数据库电影', 2026, 88.00, 87.00, 86.00, 87.15, 1, '')
                """);

        mockMvc.perform(get("/movie-ranks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title").value("数据库电影"))
                .andExpect(jsonPath("$[0].finalRank").value(1))
                .andExpect(jsonPath("$[0].finalScore").value(87.15));
    }
}
