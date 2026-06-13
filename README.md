# MovieRank

项目现在分成三部分：

- `backend`：Spring Boot 后端，提供榜单、综合排序、横向对比和电影详情接口。
- `frontend`：Vue 前端，展示五种综合榜模式、两榜横向对比和电影详情页。
- `database`：数据库初始化脚本。

## 启动

后端：

```bash
cd backend
./mvnw spring-boot:run
```

前端：

```bash
cd frontend
npm install
npm run dev
```

前端默认通过 Vite 把 `/api` 请求转发到 `http://localhost:8080`。

## TMDB

如果需要自动补充电影简介、海报和 TMDB 评分，启动后端前设置：

```bash
export TMDB_API_KEY=你的密钥
```

没有密钥时，电影详情页仍会显示本地榜单信息。
