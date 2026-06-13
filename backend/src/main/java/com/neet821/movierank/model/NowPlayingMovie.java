package com.neet821.movierank.model;

import lombok.Data;

@Data
public class NowPlayingMovie {
    private String title;
    private String rating;
    private String posterUrl;
    private String detailUrl;
    private String ticketUrl;
}
