package com.neet821.movierank;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class WebInterfaceTests {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void homePageShowsImplementedFeatures() throws Exception {
        MvcResult result = mockMvc.perform(get("/index.html"))
                .andExpect(status().isOk())
                .andReturn();

        String html = new String(result.getResponse().getContentAsByteArray(), StandardCharsets.UTF_8);

        assertThat(html).contains("MovieRank");
        assertThat(html).contains("Blue Album");
        assertThat(html).contains("电影综合排名");
        assertThat(html).contains("TSPDT Top 1000");
        assertThat(html).contains("tspdtBody");
        assertThat(html).contains("authGate");
        assertThat(html).contains("href=\"./styles.css?v=20260613-title-only\"");
        assertThat(html).contains("src=\"./app.js?v=20260613-title-only\"");
        assertThat(html).contains("id=\"loginForm\"");
        assertThat(html).contains("id=\"loginError\"");
        assertThat(html).contains("id=\"authGate\" class=\"auth-gate\" role=\"status\" aria-live=\"polite\" hidden");
        assertThat(html).contains("<h1>MovieRank</h1>");
        assertThat(html).doesNotContain("BLUE ALBUM ACCOUNT");
        assertThat(html).doesNotContain("使用 Blue Album 账号登录后");
        assertThat(html).contains("app.js?v=20260613-title-only");
    }

    @Test
    void loginTitleIsCentered() throws Exception {
        mockMvc.perform(get("/styles.css"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(".login-card h1")))
                .andExpect(content().string(containsString("text-align: center")));
    }

    @Test
    void appScriptIsAvailable() throws Exception {
        mockMvc.perform(get("/app.js"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("loadMovieRanks")));
        mockMvc.perform(get("/app.js"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("loadTspdtTop1000")));
        mockMvc.perform(get("/app.js"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("ensureBlueAlbumSession")));
        mockMvc.perform(get("/app.js"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("/movie-rank-api")));
        mockMvc.perform(get("/app.js"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("submitBlueAlbumLogin")));
        mockMvc.perform(get("/app.js"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("/api/auth/login")));
        mockMvc.perform(get("/app.js"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.not(containsString("window.location.href"))));
    }

    @Test
    void movieCardsStayInsidePrivateRoute() throws Exception {
        mockMvc.perform(get("/app.js"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("poster-empty")))
                .andExpect(content().string(containsString("movie-card")));
    }

    @Test
    void nowPlayingCardsShowTmdbDetails() throws Exception {
        mockMvc.perform(get("/app.js"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("tmdbRating")))
                .andExpect(content().string(containsString("overview")))
                .andExpect(content().string(containsString("releaseDate")));
    }
}
