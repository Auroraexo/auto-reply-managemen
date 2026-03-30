# 快速参考卡片 ⚡

## 🚀 启动命令

### Docker 部署
```bash
# 配置环境变量
echo "WECHAT_APP_ID=你的 AppID" > .env
echo "WECHAT_SECRET=你的 Secret" >> .env
echo "WECHAT_TOKEN=你的 Token" >> .env
echo "WECHAT_AES_KEY=你的 AES Key" >> .env
echo "JWT_SECRET=你的 JWT 密钥" >> .env

# 一键启动
docker-compose up -d

# 查看日志
docker-compose logs -f

# 停止服务
docker-compose down
```

### 本地开发
```bash
# 后端（终端 1）
cd backend
mvn spring-boot:run

# 前端（终端 2）
cd frontend
npm install
npm run dev
```

## 🌐 访问地址

| 服务 | 地址 | 说明 |
|------|------|------|
| 前端界面 | http://localhost | 管理后台 |
| 后端 API | http://localhost:8080/api | RESTful 接口 |
| API 文档 | http://localhost:8080/api/doc.html | Swagger UI |
| MySQL | localhost:3306 | 数据库 |
| Redis | localhost:6379 | 缓存 |

## 🔑 默认账户

```
用户名：admin
密码：admin123
⚠️ 首次登录请修改密码！
```

## 📋 核心 API

### 微信接口
```http
GET  /api/wechat?signature=xxx&timestamp=xxx&nonce=xxx&echostr=xxx
POST /api/wechat
```

### 规则管理
```http
GET    /api/admin/rules/page?page=1&size=10
GET    /api/admin/rules/{id}
POST   /api/admin/rules
PUT    /api/admin/rules/{id}
DELETE /api/admin/rules/{id}
PUT    /api/admin/rules/{id}/enabled?enabled=true
POST   /api/admin/rules/test-match?msgType=text&content=你好
```

## 💾 数据库信息

```yaml
数据库：wechat_auto_reply
字符集：utf8mb4
时区：Asia/Shanghai

表：
- sys_user         # 管理员
- wechat_config    # 公众号配置
- reply_rule       # 回复规则（核心）
- message_record   # 消息记录
- wechat_user      # 粉丝
- material         # 素材
- operation_log    # 日志
```

## 🎯 规则类型说明

| 类型 | 说明 | 匹配条件 |
|------|------|----------|
| KEYWORD | 关键词 | matchContent + matchMode |
| SUBSCRIBE | 关注 | Event = subscribe |
| DEFAULT | 默认 | 无条件匹配（兜底） |
| MESSAGE_TYPE | 消息类型 | MsgType 匹配 |

## 🔧 匹配模式

| 模式 | 说明 | 示例 |
|------|------|------|
| EXACT | 完全匹配 | "你好" = "你好" |
| CONTAIN | 包含匹配 | "你好吗" 包含 "你好" |
| REGEX | 正则 | "^你好.*" |

## 📦 回复类型

| 类型 | 说明 | 必填字段 |
|------|------|----------|
| TEXT | 文本 | replyContent |
| IMAGE | 图片 | mediaId |
| VOICE | 语音 | mediaId |
| VIDEO | 视频 | mediaId, replyContent |
| MUSIC | 音乐 | musicData (JSON) |
| NEWS | 图文 | articleData (JSON) |

## 🛠️ 常用运维命令

### 查看日志
```bash
# 所有服务
docker-compose logs -f

# 单个服务
docker-compose logs -f backend
docker-compose logs -f mysql
```

### 备份数据库
```bash
docker exec wechat-mysql mysqldump -u root -proot123456 wechat_auto_reply > backup.sql
```

### 恢复数据库
```bash
cat backup.sql | docker exec -i wechat-mysql mysql -u root -proot123456 wechat_auto_reply
```

### 重启服务
```bash
docker-compose restart backend
```

## 🔍 故障排查

### 容器状态检查
```bash
docker-compose ps
```

### 端口占用检查
```bash
netstat -tunlp | grep :8080
netstat -tunlp | grep :3306
```

### 数据库连接测试
```bash
docker exec wechat-mysql mysql -u root -proot123456 -e "SHOW DATABASES;"
```

### Redis 连接测试
```bash
docker exec wechat-redis redis-cli ping
```

## 📝 配置检查清单

部署前确认：
- [ ] `.env` 文件已创建并填写完整
- [ ] 微信公众号配置正确
- [ ] 域名和 SSL 证书已准备
- [ ] 数据库密码已修改
- [ ] JWT 密钥已更改（至少 32 字符）

## 🎨 开发技巧

### 后端热重载
```xml
<!-- pom.xml 添加 -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-devtools</artifactId>
    <scope>runtime</scope>
</dependency>
```

### 前端调试
打开浏览器开发者工具：
- Console: 查看日志
- Network: 查看请求
- Application: 查看 localStorage

### API 测试
使用 Postman 或 curl 测试接口：
```bash
curl -X GET http://localhost:8080/api/admin/rules/page \
  -H "Authorization: Bearer {token}"
```

## 📊 性能参数

### JVM 推荐配置
```bash
-Xms512m -Xmx1g -XX:+UseG1GC
```

### Redis 配置
```yaml
maxmemory: 256mb
maxmemory-policy: allkeys-lru
```

### MySQL 优化
```sql
-- 添加索引
ALTER TABLE reply_rule ADD INDEX idx_type (rule_type);
ALTER TABLE message_record ADD INDEX idx_time (create_time);
```

## 🆘 常见错误代码

| 错误码 | 说明 | 解决方法 |
|--------|------|----------|
| 40002 | 无效凭证 | 检查 Access Token |
| 40013 | 无效 AppID | 检查配置 |
| 40164 | IP 不在白名单 | 添加服务器 IP |
| 500 | 系统错误 | 查看日志 |

## 📞 获取帮助

遇到问题时的排查顺序：
1. 查看应用日志：`docker-compose logs backend`
2. 检查数据库连接
3. 验证微信配置
4. 查看网络请求
5. 重启服务

---

**提示**: 将此文件打印或保存为书签，方便随时查阅！ 📌
