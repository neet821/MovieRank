package com.neet821.movierank;

import com.neet821.movierank.crawler.TspdtTop1000Crawler;
import com.neet821.movierank.model.TspdtMovieRank;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class TspdtControllerTests {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TspdtTop1000Crawler tspdtTop1000Crawler;

    @Test
    void top1000EndpointReturnsCrawlerRows() throws Exception {
        TspdtMovieRank movieRank = new TspdtMovieRank();
        movieRank.setPosition(1);
        movieRank.setPreviousRank(1);
        movieRank.setTitle("Citizen Kane");
        movieRank.setDirector("Welles, Orson");
        movieRank.setYear("1941");
        movieRank.setCountry("USA");
        movieRank.setMinutes(119);

        when(tspdtTop1000Crawler.crawl()).thenReturn(List.of(movieRank));

        mockMvc.perform(get("/tspdt/top1000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].position").value(1))
                .andExpect(jsonPath("$[0].title").value("Citizen Kane"))
                .andExpect(jsonPath("$[0].director").value("Welles, Orson"))
                .andExpect(jsonPath("$[0].year").value("1941"))
                .andExpect(jsonPath("$[0].country").value("USA"))
                .andExpect(jsonPath("$[0].minutes").value(119));
    }
}
