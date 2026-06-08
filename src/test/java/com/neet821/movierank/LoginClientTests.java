package com.neet821.movierank;

import com.neet821.movierank.client.LoginClient;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LoginClientTests {
    @Test
    void parseLoginResultReadsSuccessJson() {
        String message = LoginClient.parseLoginResult("""
                {"code":200,"msg":"登录成功"}
                """);

        assertThat(message).isEqualTo("登录成功 | 业务状态码：200，提示：登录成功");
    }

    @Test
    void parseLoginResultReadsFailureJson() {
        String message = LoginClient.parseLoginResult("""
                {"code":500,"msg":"账号或密码错误"}
                """);

        assertThat(message).isEqualTo("登录失败 | 业务状态码：500，提示：账号或密码错误");
    }

    @Test
    void resolveLoginUrlUsesDefaultWhenNoArgumentProvided() {
        assertThat(LoginClient.resolveLoginUrl(new String[]{})).isEqualTo("http://localhost:8080/login");
    }

    @Test
    void resolveLoginUrlUsesFirstArgumentWhenProvided() {
        assertThat(LoginClient.resolveLoginUrl(new String[]{"http://localhost:18080/login"}))
                .isEqualTo("http://localhost:18080/login");
    }
}
