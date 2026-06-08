package com.neet821.movierank.service;

import com.neet821.movierank.entity.User;
import com.neet821.movierank.mapper.UserMapper;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final UserMapper userMapper;

    public UserService(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    public User login(User user) {
        return userMapper.login(user);
    }
}
