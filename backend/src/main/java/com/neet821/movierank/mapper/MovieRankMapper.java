package com.neet821.movierank.mapper;

import com.neet821.movierank.entity.MovieRank;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface MovieRankMapper {
    List<MovieRank> findAllOrdered();
}
