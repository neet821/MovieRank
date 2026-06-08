package com.neet821.movierank;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("com.neet821.movierank.mapper")
@SpringBootApplication
public class MovieRankApplication {

    public static void main(String[] args) {
        SpringApplication.run(MovieRankApplication.class, args);
    }

}
