# SpringBoot PDF 落实对照说明

本文档对应 `/home/neet821/Downloads/java/exe/SpringBoot完整开发文档.pdf`，说明 MovieRank 项目中每个已落实文件参考了 PDF 的哪一部分。

## 已落实内容

| PDF 内容 | 项目文件 | 落实情况 |
| --- | --- | --- |
| 第 4.2 节：pom.xml 依赖配置 | `pom.xml` | 已加入 Web、MyBatis、MySQL、Lombok、Gson、测试依赖 |
| 第 5.2、5.3 节：YAML 全局配置 | `src/main/resources/application.yml` | 已配置 8080 端口、MySQL 连接、MyBatis XML 路径 |
| 第 7 章：数据库初始化 | `src/main/resources/sql/init.sql` | 已创建 `movierank` 数据库、`user` 表和 `admin/123456` 测试账号 |
| 第 8.1 节：实体类 User | `src/main/java/com/neet821/movierank/entity/User.java` | 已用 Lombok `@Data` 简化实体类 |
| 第 8.2 节：Mapper 数据访问层 | `src/main/java/com/neet821/movierank/mapper/UserMapper.java` | 已定义 `login(User user)` |
| 第 8.3 节：Mapper XML | `src/main/resources/mapper/UserMapper.xml` | 已写登录查询 SQL，并绑定 Mapper 接口 |
| 第 8.4 节：Service 业务层 | `src/main/java/com/neet821/movierank/service/UserService.java` | 已调用 Mapper 查询用户 |
| 第 8.5 节：Controller 接口层 | `src/main/java/com/neet821/movierank/controller/LoginController.java` | 已提供 `POST /login`，返回 `code` 和 `msg` |
| 第 8.6 节：项目启动类 | `src/main/java/com/neet821/movierank/MovieRankApplication.java` | 已保留启动入口，并增加 Mapper 扫描 |
| 第 9 章：JDK HttpClient + Gson 客户端 | `src/main/java/com/neet821/movierank/client/LoginClient.java` | 已实现客户端请求、JSON 序列化、响应解析 |
| 第 11 章：标准项目目录结构 | `controller`、`service`、`mapper`、`entity`、`client`、`resources/mapper` | 已按文档分层整理 |
| 第 12 章：项目启动运行步骤 | `init.sql`、`application.yml`、`LoginClient.java` | 已能初始化数据库、启动服务、运行客户端 |

## 本项目做出的适配

- PDF 使用 Spring Boot `2.7.18` 和 JDK 11；当前项目原本是 Spring Boot `4.0.6`，所以 MyBatis 启动器使用了适配当前版本的 `4.0.0`。
- PDF 示例包名是 `com.example.demo`；本项目包名是 `com.neet821.movierank`，所有路径已按本项目包名调整。
- PDF 示例数据库名是 `testdb`；本项目使用更贴合项目名称的 `movierank`。
- PDF 中客户端默认请求 `http://localhost:8080/login`；本项目保留默认地址，同时允许临时传入其他地址，方便端口被占用时测试。

## 验证方式

初始化数据库：

```bash
mysql -uroot -p0852 < src/main/resources/sql/init.sql
```

运行测试：

```bash
./mvnw clean test
```

启动项目：

```bash
./mvnw spring-boot:run
```

登录成功测试：

```bash
curl -X POST http://localhost:8080/login \
  -H 'Content-Type: application/json' \
  -d '{"username":"admin","password":"123456"}'
```

预期返回：

```json
{"msg":"登录成功","code":200}
```

登录失败测试：

```bash
curl -X POST http://localhost:8080/login \
  -H 'Content-Type: application/json' \
  -d '{"username":"admin","password":"wrong"}'
```

预期返回：

```json
{"msg":"账号或密码错误","code":500}
```
