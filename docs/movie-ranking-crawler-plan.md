# 电影排名爬取与加权综合排名实现方案

## 1. 项目目标

本项目希望从多个电影网站获取电影榜单数据，将不同网站上的电影排名、评分和热度信息整理到统一格式中，再按照固定权重计算出一个综合排名。

默认选择三个数据来源：

| 来源 | 主要价值 | 默认权重 |
| --- | --- | --- |
| 豆瓣 | 国内用户口碑和评分 | 40% |
| IMDb | 国际用户口碑和评分 | 35% |
| 猫眼 | 国内市场热度和观众反馈 | 25% |

最终效果是：系统可以定期抓取榜单，合并同一部电影在不同网站的数据，生成一份综合电影排行榜，并在页面或接口中展示。

## 2. 整体流程

建议按下面的顺序实现：

1. 分别从豆瓣、IMDb、猫眼获取电影榜单数据。
2. 从每条榜单记录中提取电影名称、年份、原始排名、原始评分、来源网站等信息。
3. 清洗电影名称，例如去掉多余空格、统一中英文标点、保留上映年份。
4. 判断不同来源中的记录是否属于同一部电影。
5. 将每个来源的排名或评分转换成 0 到 100 的统一分数。
6. 按照豆瓣 40%、IMDb 35%、猫眼 25% 的权重计算综合分。
7. 按综合分从高到低排序，生成最终综合排名。
8. 保存抓取结果、合并结果和最终排名，方便页面展示和后续排查。

## 3. 数据来源设计

### 3.1 豆瓣

豆瓣适合作为国内口碑来源。可以优先考虑电影榜单、电影 Top 列表或公开可访问的电影页面。

建议抓取字段：

- 电影名称
- 上映年份
- 豆瓣评分
- 豆瓣排名
- 详情页地址

### 3.2 IMDb

IMDb 适合作为国际口碑来源。可以优先考虑 IMDb Top 250 等公开榜单。

建议抓取字段：

- 英文电影名称
- 上映年份
- IMDb 评分
- IMDb 排名
- 详情页地址

### 3.3 猫眼

猫眼更适合补充国内热度和市场反馈。可以关注热门榜、评分榜或公开可访问的榜单页面。

建议抓取字段：

- 电影名称
- 上映年份
- 猫眼评分
- 猫眼排名
- 详情页地址

网站页面结构可能会变化，所以代码中不要把解析规则写得过于死板。每个网站应单独写一个抓取和解析模块，某个网站失效时不会影响其他网站。

## 4. 核心数据结构建议

### 4.1 SourceMovieRank

表示从单个网站抓到的一条电影排名数据。

建议字段：

| 字段 | 含义 |
| --- | --- |
| sourceName | 来源网站，例如 Douban、IMDb、Maoyan |
| title | 电影名称 |
| originalTitle | 原始名称，保留网站上的写法 |
| year | 上映年份 |
| sourceRank | 该网站中的排名 |
| sourceScore | 该网站中的原始评分 |
| detailUrl | 电影详情页地址 |
| crawledAt | 抓取时间 |

### 4.2 UnifiedMovie

表示合并后的电影信息。

建议字段：

| 字段 | 含义 |
| --- | --- |
| id | 系统内部电影编号 |
| title | 统一后的电影名称 |
| year | 上映年份 |
| sourceRanks | 不同来源的排名数据 |
| needManualCheck | 是否需要人工确认 |
| manualCheckReason | 需要人工确认的原因 |

### 4.3 WeightedRankResult

表示最终综合排名结果。

建议字段：

| 字段 | 含义 |
| --- | --- |
| movieId | 对应的电影编号 |
| title | 电影名称 |
| year | 上映年份 |
| doubanScore | 豆瓣转换后的分数 |
| imdbScore | IMDb 转换后的分数 |
| maoyanScore | 猫眼转换后的分数 |
| finalScore | 综合分 |
| finalRank | 综合排名 |
| missingSources | 缺失的数据来源 |

## 5. 模块划分建议

### 5.1 MovieRankCrawler

每个网站实现一个独立爬取器：

- `DoubanMovieRankCrawler`
- `ImdbMovieRankCrawler`
- `MaoyanMovieRankCrawler`

每个爬取器只负责一件事：从对应网站获取原始榜单，并返回 `SourceMovieRank` 列表。

### 5.2 解析与清洗模块

解析模块负责从网页或接口结果中提取需要的字段。清洗模块负责统一电影名称格式。

建议清洗规则：

- 去掉电影名前后的空格。
- 将连续多个空格合并成一个空格。
- 统一中文和英文括号。
- 保留上映年份，作为匹配同一电影的重要依据。
- 不直接删除英文名或别名，必要时作为辅助匹配信息保存。

### 5.3 电影合并模块

合并时优先使用“电影名称 + 上映年份”判断是否为同一部电影。

建议规则：

1. 名称完全一致且年份一致，直接合并。
2. 名称相近且年份一致，可以合并，但标记为需要人工确认。
3. 年份不一致时，不自动合并。
4. 同一网站中出现重复电影时，保留排名更靠前的一条，并记录重复情况。

### 5.4 RankScoreCalculator

该模块负责把不同网站的原始分数转换成统一分数，并计算综合分。

默认权重：

```text
豆瓣：40%
IMDb：35%
猫眼：25%
```

权重总和：

```text
40% + 35% + 25% = 100%
```

综合分计算方式：

```text
综合分 = 豆瓣统一分 * 0.40 + IMDb统一分 * 0.35 + 猫眼统一分 * 0.25
```

如果某个来源缺失，不建议简单按 0 分计算。更合理的方式是只使用已有来源，并按已有来源的权重重新分配。

例如一部电影只有豆瓣和 IMDb 数据：

```text
综合分 = (豆瓣统一分 * 0.40 + IMDb统一分 * 0.35) / (0.40 + 0.35)
```

这样可以避免某部电影只是因为某个网站没有收录就被严重拉低。

## 6. 分数转换规则

不同网站的评分范围不一定完全相同，所以需要先转换成 0 到 100 的统一分。

建议规则：

| 来源 | 原始评分示例 | 转换方式 |
| --- | --- | --- |
| 豆瓣 | 0 到 10 分 | 原始评分 * 10 |
| IMDb | 0 到 10 分 | 原始评分 * 10 |
| 猫眼 | 0 到 10 分 | 原始评分 * 10 |

如果某些榜单只有排名，没有评分，可以用排名转换为分数：

```text
排名分 = 100 - ((当前排名 - 1) / (榜单总数量 - 1)) * 100
```

例如一个 100 部电影的榜单中，第 1 名为 100 分，第 100 名为 0 分。

如果同一来源同时有评分和排名，建议优先使用评分；排名可以作为辅助参考。

## 7. 异常处理

实际运行时，某些网站可能出现访问失败、页面结构变化、数据缺失等情况。

建议处理方式：

- 单个网站抓取失败时，不中断整个任务。
- 抓取失败的网站记录错误原因和失败时间。
- 继续使用其他网站的数据生成综合排名。
- 最终结果中记录缺失来源，例如 `missingSources = ["IMDb"]`。
- 页面展示时可以提示“部分来源暂时缺失”。

常见异常包括：

| 情况 | 处理方式 |
| --- | --- |
| 网站无法访问 | 跳过该来源，记录失败原因 |
| 页面结构变化 | 返回空结果并记录解析失败 |
| 某条电影缺少年份 | 保留数据，但合并时标记为需要人工确认 |
| 评分为空 | 尝试使用排名换算分数 |
| 同名电影较多 | 不自动合并，交给人工确认 |

## 8. 数据保存建议

建议至少保存三类数据：

1. 原始抓取结果：方便以后检查某个网站当时抓到了什么。
2. 合并后的电影数据：方便处理同一电影来自多个网站的情况。
3. 最终综合排名：方便页面快速展示。

如果使用数据库，可以设计三张主要表：

| 表 | 作用 |
| --- | --- |
| source_movie_rank | 保存单个网站的原始榜单数据 |
| unified_movie | 保存合并后的电影信息 |
| weighted_rank_result | 保存最终综合排名 |

## 9. 页面与接口展示建议

后续可以提供一个综合排名页面，展示：

- 综合排名
- 电影名称
- 上映年份
- 综合分
- 豆瓣分
- IMDb 分
- 猫眼分
- 缺失来源
- 是否需要人工确认

也可以提供接口，例如：

```text
GET /movie-ranks
```

返回最终综合排名列表。

```text
POST /movie-ranks/refresh
```

手动触发一次榜单更新。

## 10. 合规与稳定性提醒

爬取网站数据时要注意合规和稳定性：

- 优先使用公开页面、公开榜单或官方允许的接口。
- 不绕过登录、验证码、付费限制或访问限制。
- 控制访问频率，避免短时间大量请求。
- 设置清晰的请求间隔，例如每次请求间隔 2 到 5 秒。
- 保存抓取时间，避免频繁重复抓取。
- 如果网站明确禁止抓取，应停止抓取该来源，改为人工导入或使用允许的数据来源。

## 11. 推荐实现顺序

建议按下面顺序开发，降低一次性开发难度：

1. 先写固定的模拟数据，验证综合分计算是否正确。
2. 实现 `RankScoreCalculator`，完成权重计算。
3. 实现电影名称清洗和合并逻辑。
4. 先接入一个来源，例如豆瓣。
5. 再接入 IMDb。
6. 最后接入猫眼。
7. 加入数据库保存。
8. 加入页面或接口展示。
9. 加入定时更新或手动刷新功能。

## 12. 验收标准

文档对应的后续功能完成后，应满足这些标准：

- 至少能处理豆瓣、IMDb、猫眼三个来源中的两个来源。
- 单个来源失败时，系统仍然能生成综合排名。
- 权重计算正确，默认权重总和为 100%。
- 同一部电影不会因为不同网站名称格式不同而明显重复出现。
- 缺失来源和需要人工确认的数据能被明确标记。
- 页面或接口能展示最终综合排名。

## 13. 从创建文件开始的基础代码步骤

当前项目是 Spring Boot 项目，基础包名是：

```text
com.neet821.movierank
```

建议先不要直接写真实爬虫。第一步先用模拟数据把“数据结构、综合分计算、接口展示”跑通。等基础流程能正常运行后，再把模拟数据替换成真实网站抓取。

### 13.1 创建目录

在 `src/main/java/com/neet821/movierank` 下创建这些目录：

```text
model
crawler
service
controller
```

最终结构建议如下：

```text
src/main/java/com/neet821/movierank
├── MovieRankApplication.java
├── controller
│   └── MovieRankController.java
├── crawler
│   ├── DoubanMovieRankCrawler.java
│   ├── ImdbMovieRankCrawler.java
│   ├── MaoyanMovieRankCrawler.java
│   └── MovieRankCrawler.java
├── model
│   ├── SourceMovieRank.java
│   └── WeightedRankResult.java
└── service
    ├── MovieRankService.java
    └── RankScoreCalculator.java
```

### 13.2 创建 SourceMovieRank

文件路径：

```text
src/main/java/com/neet821/movierank/model/SourceMovieRank.java
```

基础代码：

```java
package com.neet821.movierank.model;

public class SourceMovieRank {
    private String sourceName;
    private String title;
    private int year;
    private int sourceRank;
    private double sourceScore;
    private String detailUrl;

    public SourceMovieRank() {
    }

    public SourceMovieRank(String sourceName, String title, int year, int sourceRank, double sourceScore, String detailUrl) {
        this.sourceName = sourceName;
        this.title = title;
        this.year = year;
        this.sourceRank = sourceRank;
        this.sourceScore = sourceScore;
        this.detailUrl = detailUrl;
    }

    public String getSourceName() {
        return sourceName;
    }

    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getSourceRank() {
        return sourceRank;
    }

    public void setSourceRank(int sourceRank) {
        this.sourceRank = sourceRank;
    }

    public double getSourceScore() {
        return sourceScore;
    }

    public void setSourceScore(double sourceScore) {
        this.sourceScore = sourceScore;
    }

    public String getDetailUrl() {
        return detailUrl;
    }

    public void setDetailUrl(String detailUrl) {
        this.detailUrl = detailUrl;
    }
}
```

这个类用来保存“某个网站上的一条电影排名”。

### 13.3 创建 WeightedRankResult

文件路径：

```text
src/main/java/com/neet821/movierank/model/WeightedRankResult.java
```

基础代码：

```java
package com.neet821.movierank.model;

import java.util.List;

public class WeightedRankResult {
    private String title;
    private int year;
    private double doubanScore;
    private double imdbScore;
    private double maoyanScore;
    private double finalScore;
    private int finalRank;
    private List<String> missingSources;

    public WeightedRankResult() {
    }

    public WeightedRankResult(String title, int year, double doubanScore, double imdbScore, double maoyanScore,
                              double finalScore, int finalRank, List<String> missingSources) {
        this.title = title;
        this.year = year;
        this.doubanScore = doubanScore;
        this.imdbScore = imdbScore;
        this.maoyanScore = maoyanScore;
        this.finalScore = finalScore;
        this.finalRank = finalRank;
        this.missingSources = missingSources;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public double getDoubanScore() {
        return doubanScore;
    }

    public void setDoubanScore(double doubanScore) {
        this.doubanScore = doubanScore;
    }

    public double getImdbScore() {
        return imdbScore;
    }

    public void setImdbScore(double imdbScore) {
        this.imdbScore = imdbScore;
    }

    public double getMaoyanScore() {
        return maoyanScore;
    }

    public void setMaoyanScore(double maoyanScore) {
        this.maoyanScore = maoyanScore;
    }

    public double getFinalScore() {
        return finalScore;
    }

    public void setFinalScore(double finalScore) {
        this.finalScore = finalScore;
    }

    public int getFinalRank() {
        return finalRank;
    }

    public void setFinalRank(int finalRank) {
        this.finalRank = finalRank;
    }

    public List<String> getMissingSources() {
        return missingSources;
    }

    public void setMissingSources(List<String> missingSources) {
        this.missingSources = missingSources;
    }
}
```

这个类用来保存最终展示给用户看的综合排名。

### 13.4 创建 MovieRankCrawler 接口

文件路径：

```text
src/main/java/com/neet821/movierank/crawler/MovieRankCrawler.java
```

基础代码：

```java
package com.neet821.movierank.crawler;

import com.neet821.movierank.model.SourceMovieRank;
import java.util.List;

public interface MovieRankCrawler {
    String getSourceName();

    List<SourceMovieRank> crawl();
}
```

这个接口规定：每个网站爬取器都必须告诉系统“自己是哪一个网站”，并返回这个网站的电影榜单。

### 13.5 创建三个模拟爬取器

第一阶段先返回固定数据，不访问真实网站。这样可以先确认项目流程能跑通。

文件路径：

```text
src/main/java/com/neet821/movierank/crawler/DoubanMovieRankCrawler.java
```

基础代码：

```java
package com.neet821.movierank.crawler;

import com.neet821.movierank.model.SourceMovieRank;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DoubanMovieRankCrawler implements MovieRankCrawler {
    @Override
    public String getSourceName() {
        return "Douban";
    }

    @Override
    public List<SourceMovieRank> crawl() {
        return List.of(
                new SourceMovieRank("Douban", "肖申克的救赎", 1994, 1, 9.7, "https://movie.douban.com/"),
                new SourceMovieRank("Douban", "霸王别姬", 1993, 2, 9.6, "https://movie.douban.com/")
        );
    }
}
```

文件路径：

```text
src/main/java/com/neet821/movierank/crawler/ImdbMovieRankCrawler.java
```

基础代码：

```java
package com.neet821.movierank.crawler;

import com.neet821.movierank.model.SourceMovieRank;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ImdbMovieRankCrawler implements MovieRankCrawler {
    @Override
    public String getSourceName() {
        return "IMDb";
    }

    @Override
    public List<SourceMovieRank> crawl() {
        return List.of(
                new SourceMovieRank("IMDb", "肖申克的救赎", 1994, 1, 9.3, "https://www.imdb.com/"),
                new SourceMovieRank("IMDb", "霸王别姬", 1993, 80, 8.1, "https://www.imdb.com/")
        );
    }
}
```

文件路径：

```text
src/main/java/com/neet821/movierank/crawler/MaoyanMovieRankCrawler.java
```

基础代码：

```java
package com.neet821.movierank.crawler;

import com.neet821.movierank.model.SourceMovieRank;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MaoyanMovieRankCrawler implements MovieRankCrawler {
    @Override
    public String getSourceName() {
        return "Maoyan";
    }

    @Override
    public List<SourceMovieRank> crawl() {
        return List.of(
                new SourceMovieRank("Maoyan", "肖申克的救赎", 1994, 3, 9.5, "https://www.maoyan.com/"),
                new SourceMovieRank("Maoyan", "霸王别姬", 1993, 1, 9.6, "https://www.maoyan.com/")
        );
    }
}
```

### 13.6 创建 RankScoreCalculator

文件路径：

```text
src/main/java/com/neet821/movierank/service/RankScoreCalculator.java
```

基础代码：

```java
package com.neet821.movierank.service;

import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class RankScoreCalculator {
    private static final Map<String, Double> WEIGHTS = Map.of(
            "Douban", 0.40,
            "IMDb", 0.35,
            "Maoyan", 0.25
    );

    public double toHundredPointScore(double sourceScore) {
        return sourceScore * 10;
    }

    public double calculateFinalScore(Map<String, Double> scores) {
        double totalScore = 0;
        double totalWeight = 0;

        for (Map.Entry<String, Double> entry : scores.entrySet()) {
            Double weight = WEIGHTS.get(entry.getKey());
            if (weight != null) {
                totalScore += entry.getValue() * weight;
                totalWeight += weight;
            }
        }

        if (totalWeight == 0) {
            return 0;
        }

        return totalScore / totalWeight;
    }
}
```

这个类先做两件事：把 10 分制转换成 100 分制，再按权重计算综合分。

### 13.7 创建 MovieRankService

文件路径：

```text
src/main/java/com/neet821/movierank/service/MovieRankService.java
```

基础代码：

```java
package com.neet821.movierank.service;

import com.neet821.movierank.crawler.MovieRankCrawler;
import com.neet821.movierank.model.SourceMovieRank;
import com.neet821.movierank.model.WeightedRankResult;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MovieRankService {
    private final List<MovieRankCrawler> crawlers;
    private final RankScoreCalculator calculator;

    public MovieRankService(List<MovieRankCrawler> crawlers, RankScoreCalculator calculator) {
        this.crawlers = crawlers;
        this.calculator = calculator;
    }

    public List<WeightedRankResult> getWeightedRanks() {
        List<SourceMovieRank> sourceRanks = new ArrayList<>();

        for (MovieRankCrawler crawler : crawlers) {
            sourceRanks.addAll(crawler.crawl());
        }

        Map<String, List<SourceMovieRank>> groupedMovies = new HashMap<>();
        for (SourceMovieRank sourceRank : sourceRanks) {
            String key = sourceRank.getTitle() + "-" + sourceRank.getYear();
            groupedMovies.computeIfAbsent(key, value -> new ArrayList<>()).add(sourceRank);
        }

        List<WeightedRankResult> results = new ArrayList<>();
        for (List<SourceMovieRank> movieSources : groupedMovies.values()) {
            SourceMovieRank first = movieSources.get(0);
            Map<String, Double> scores = new HashMap<>();

            for (SourceMovieRank movieSource : movieSources) {
                scores.put(movieSource.getSourceName(), calculator.toHundredPointScore(movieSource.getSourceScore()));
            }

            double doubanScore = scores.getOrDefault("Douban", 0.0);
            double imdbScore = scores.getOrDefault("IMDb", 0.0);
            double maoyanScore = scores.getOrDefault("Maoyan", 0.0);
            double finalScore = calculator.calculateFinalScore(scores);

            List<String> missingSources = new ArrayList<>();
            if (!scores.containsKey("Douban")) {
                missingSources.add("Douban");
            }
            if (!scores.containsKey("IMDb")) {
                missingSources.add("IMDb");
            }
            if (!scores.containsKey("Maoyan")) {
                missingSources.add("Maoyan");
            }

            results.add(new WeightedRankResult(
                    first.getTitle(),
                    first.getYear(),
                    doubanScore,
                    imdbScore,
                    maoyanScore,
                    finalScore,
                    0,
                    missingSources
            ));
        }

        results.sort(Comparator.comparingDouble(WeightedRankResult::getFinalScore).reversed());

        for (int i = 0; i < results.size(); i++) {
            results.get(i).setFinalRank(i + 1);
        }

        return results;
    }
}
```

这个类负责把三个来源的数据合并起来，并算出最终排名。

### 13.8 创建 MovieRankController

文件路径：

```text
src/main/java/com/neet821/movierank/controller/MovieRankController.java
```

基础代码：

```java
package com.neet821.movierank.controller;

import com.neet821.movierank.model.WeightedRankResult;
import com.neet821.movierank.service.MovieRankService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class MovieRankController {
    private final MovieRankService movieRankService;

    public MovieRankController(MovieRankService movieRankService) {
        this.movieRankService = movieRankService;
    }

    @GetMapping("/movie-ranks")
    public List<WeightedRankResult> getMovieRanks() {
        return movieRankService.getWeightedRanks();
    }
}
```

这个类提供一个访问入口。项目启动后，在浏览器访问：

```text
http://localhost:8080/movie-ranks
```

应该能看到综合排名结果。

### 13.9 第一次运行检查

在项目根目录运行：

```bash
./mvnw spring-boot:run
```

启动成功后访问：

```text
http://localhost:8080/movie-ranks
```

预期结果是返回一个列表，里面至少包含：

- 肖申克的救赎
- 霸王别姬
- 综合分
- 综合排名
- 各来源分数

### 13.10 后续再接真实网站

基础代码跑通后，再逐个替换三个模拟爬取器中的 `crawl()` 方法。

例如 `DoubanMovieRankCrawler` 后续要做的事情是：

1. 请求豆瓣榜单页面。
2. 读取页面内容。
3. 从页面中解析电影名称、年份、评分、排名。
4. 把解析结果组装成 `SourceMovieRank` 列表。
5. 返回给 `MovieRankService`。

接入真实网站时，一次只改一个爬取器。先让豆瓣跑通，再做 IMDb，最后做猫眼。这样出问题时更容易定位。
