package com.neet821.movierank.mapper;

import com.neet821.movierank.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper {
    User login(User user);
}
