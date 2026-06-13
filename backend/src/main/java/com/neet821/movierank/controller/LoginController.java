package com.neet821.movierank.controller;

import com.neet821.movierank.entity.User;
import com.neet821.movierank.service.UserService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class LoginController {
    private final UserService userService;

    public LoginController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody User user) {
        User result = userService.login(user);
        Map<String, Object> resultMap = new HashMap<>();

        if (result == null) {
            resultMap.put("code", 500);
            resultMap.put("msg", "账号或密码错误");
        } else {
            resultMap.put("code", 200);
            resultMap.put("msg", "登录成功");
        }

        return resultMap;
    }
}
