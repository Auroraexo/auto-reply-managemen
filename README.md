# 微信公众号消息自动回复与后台管理系统

基于 Spring Boot + Vue3 的微信公众号自动回复管理平台，支持关键词回复、用户管理、数据统计等功能。

## 技术栈

- 前端：Vue3 + TypeScript + Element Plus + ECharts
- 后端：Spring Boot 3 + MyBatis Plus + Spring Security + JWT
- 数据库：MySQL 8.0
- 构建：Maven + Vite

## 功能模块

- 自动回复：关键词匹配（精确/模糊/正则）、关注回复、默认回复
- 用户管理：粉丝列表、标签管理、备注
- 消息记录：消息查询、主动发送
- 数据统计：粉丝趋势、消息统计、规则命中 TOP10
- 数据备份：手动备份、备份文件下载
- 公众号配置：AppID / Token 配置
- 权限控制：ADMIN / USER 角色，JWT 认证

## 快速启动

### 环境要求

- JDK 17+
- Node.js 18+
- MySQL 8.0
- Maven 3.6+

### 1. 数据库初始化

```sql
CREATE DATABASE wechat_auto_reply DEFAULT CHARACTER SET utf8mb4;
```

执行 `backend/src/main/resources/db/schema.sql`

### 2. 启动后端

```bash
cd backend
mvn spring-boot:run
```

后端默认运行在 `http://localhost:8080`

### 3. 启动前端

```bash
cd frontend
npm install
npm run dev
```

前端默认运行在 `http://localhost:3000`

### 4. 默认账号

| 用户名 | 密码 | 角色 |
|--------|------|------|
| admin | admin123 | 管理员 |

## 项目结构

```
├── backend/          # Spring Boot 后端
│   ├── src/main/java/com/wechat/autoreply/
│   │   ├── controller/   # 接口层
│   │   ├── service/      # 业务层
│   │   ├── mapper/       # 数据层
│   │   ├── entity/       # 实体类
│   │   ├── config/       # 配置类
│   │   └── filter/       # JWT 过滤器
│   └── src/main/resources/
│       ├── application.yml
│       └── db/schema.sql
└── frontend/         # Vue3 前端
    └── src/
        ├── views/    # 页面组件
        ├── api/      # 接口封装
        ├── router/   # 路由配置
        └── utils/    # 工具函数
```

## 分支说明

| 分支 | 内容 |
|------|------|
| aurora | 前端代码 |
| lime | 后端代码 |
