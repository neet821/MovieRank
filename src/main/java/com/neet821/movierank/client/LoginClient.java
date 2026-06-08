package com.neet821.movierank.client;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class LoginClient {
    public static void main(String[] args) throws Exception {
        User user = new User("admin", "123456");

        Gson gson = new Gson();
        String json = gson.toJson(user);

        HttpClient client = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(resolveLoginUrl(args)))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        String resultJson = response.body();

        System.out.println("服务端完整响应 JSON：" + resultJson);
        System.out.println(parseLoginResult(resultJson));
    }

    public static String parseLoginResult(String resultJson) {
        Gson gson = new Gson();
        JsonObject jsonObject = gson.fromJson(resultJson, JsonObject.class);
        int code = jsonObject.get("code").getAsInt();
        String msg = jsonObject.get("msg").getAsString();

        if (code == 200) {
            return "登录成功 | 业务状态码：" + code + "，提示：" + msg;
        }
        if (code == 500) {
            return "登录失败 | 业务状态码：" + code + "，提示：" + msg;
        }
        return "未知状态 | 业务状态码：" + code + "，提示：" + msg;
    }

    public static String resolveLoginUrl(String[] args) {
        if (args != null && args.length > 0 && args[0] != null && !args[0].isBlank()) {
            return args[0];
        }
        return "http://localhost:8080/login";
    }

    static class User {
        private String username;
        private String password;

        User(String username, String password) {
            this.username = username;
            this.password = password;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }
}
