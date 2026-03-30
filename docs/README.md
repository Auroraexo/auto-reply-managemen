# 微信公众号消息自动回复与后台管理系统

## 系统简介

本系统是一个功能完善的微信公众号消息自动回复管理平台，提供可视化的规则配置、消息记录管理、用户管理等功能。

### 核心功能

1. **微信消息接收与处理**
   - 支持文本、图片、语音、视频、位置、链接等多种消息类型
   - 支持关注、取消关注、菜单点击等事件处理
   - 自动验证微信服务器

2. **自动回复规则管理**
   - 关键词回复（支持完全匹配、包含匹配、正则匹配）
   - 关注自动回复
   - 默认回复
   - 按消息类型回复
   - 支持多种回复类型：文本、图片、语音、视频、音乐、图文
   - 规则优先级设置
   - 规则启用/禁用控制

3. **后台管理界面**
   - 回复规则管理（增删改查）
   - 消息记录查看
   - 微信用户管理
   - 公众号配置管理

4. **数据存储方案**
   - MySQL 8.0 存储核心数据
   - Redis 缓存 Access Token 等临时数据
   - 完整的数据库表设计

## 技术架构

### 后端技术栈
- **框架**: Spring Boot 3.2.0
- **语言**: Java 17
- **ORM**: MyBatis-Plus 3.5.4
- **数据库**: MySQL 8.0+
- **缓存**: Redis 7.x
- **安全**: Spring Security + JWT
- **API 文档**: Swagger/OpenAPI 3.0
- **HTTP 客户端**: RestTemplate

### 前端技术栈
- **框架**: Vue 3.4 + TypeScript
- **构建工具**: Vite 5.0
- **UI 组件**: Element Plus 2.5
- **状态管理**: Pinia 2.1
- **路由**: Vue Router 4.2
- **HTTP 客户端**: Axios 1.6

### 部署方案
- **容器化**: Docker + Docker Compose
- **Web 服务器**: Nginx

## 项目结构

```
auto-reply management/
├── backend/                          # 后端项目
│   ├── src/main/java/com/wechat/autoreply/
│   │   ├── config/                   # 配置类
│   │   ├── controller/               # 控制器
│   │   ├── dto/                      # 数据传输对象
│   │   ├── entity/                   # 实体类
│   │   ├── mapper/                   # Mapper 接口
│   │   ├── service/                  # 服务层
│   │   └── vo/                       # 视图对象
│   ├── src/main/resources/
│   │   ├── db/
│   │   │   └── schema.sql           # 数据库脚本
│   │   └── application.yml          # 配置文件
│   ├── pom.xml                      # Maven 配置
│   └── Dockerfile                   # Docker 镜像
├── frontend/                         # 前端项目
│   ├── src/
│   │   ├── api/                     # API 接口
│   │   ├── router/                  # 路由配置
│   │   ├── utils/                   # 工具函数
│   │   ├── views/                   # 页面组件
│   │   ├── App.vue                  # 根组件
│   │   └── main.ts                  # 入口文件
│   ├── package.json                 # 依赖配置
│   ├── vite.config.ts              # Vite 配置
│   ├── Dockerfile                   # Docker 镜像
│   └── nginx.conf                   # Nginx 配置
├── docker-compose.yml               # Docker Compose 配置
└── README.md                        # 说明文档
```

## 快速开始

### 🚀 本地运行（Windows）

#### 方式一：一键启动脚本（最简单）

1. **检查环境**
   ```bash
   # 双击运行
   check-env.bat
   ```

2. **启动后端**
   ```bash
   # 双击运行
   start-local.bat
   ```
   等待看到：`微信公众号自动回复管理系统启动成功！`

3. **启动前端**（新窗口）
   ```bash
   # 双击运行
   start-frontend.bat
   ```
   等待看到：`Local: http://localhost:3000/`

4. **访问系统**
   - 前端界面：http://localhost:3000
   - API 文档：http://localhost:8080/api/doc.html
   - 默认账户：admin / admin123

详细说明请查看：[LOCAL_SETUP.md](LOCAL_SETUP.md)

### 方式二：Docker 部署（推荐生产环境）

1. **克隆项目**
```bash
git clone <项目地址>
cd auto-reply management
```

2. **配置环境变量**
在项目根目录创建 `.env` 文件：
```bash
WECHAT_APP_ID=你的公众号 AppID
WECHAT_SECRET=你的公众号 Secret
WECHAT_TOKEN=你的微信服务器 Token
WECHAT_AES_KEY=你的消息加解密密钥
JWT_SECRET=自定义 JWT 密钥（至少 32 个字符）
```

3. **启动服务**
```bash
docker-compose up -d
```

4. **访问系统**
- 前端界面：http://localhost
- 后端 API: http://localhost:8080/api
- API 文档：http://localhost:8080/api/doc.html

### 方式二：本地开发

#### 后端启动

1. **环境要求**
   - JDK 17+
   - MySQL 8.0+
   - Redis 7.x
   - Maven 3.6+

2. **初始化数据库**
```bash
mysql -u root -p < backend/src/main/resources/db/schema.sql
```

3. **修改配置**
编辑 `backend/src/main/resources/application.yml`：
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/wechat_auto_reply?...
    username: root
    password: your_password
  
  data:
    redis:
      host: localhost
      port: 6379

wechat:
  official-account:
    app-id: your_app_id
    secret: your_secret
    token: your_token
    aes-key: your_aes_key
```

4. **启动后端**
```bash
cd backend
mvn clean install
mvn spring-boot:run
```

#### 前端启动

1. **环境要求**
   - Node.js 18+
   - npm 9+

2. **安装依赖**
```bash
cd frontend
npm install
```

3. **启动前端**
```bash
npm run dev
```

4. **访问系统**
- 前端界面：http://localhost:3000
- 后端 API: http://localhost:8080/api

## 数据库设计

### 核心数据表

1. **sys_user** - 系统用户表（管理员）
2. **wechat_config** - 微信公众号配置表
3. **reply_rule** - 自动回复规则表
4. **message_record** - 消息记录表
5. **wechat_user** - 微信用户表（粉丝）
6. **material** - 素材管理表
7. **operation_log** - 操作日志表

详细表结构请查看 `backend/src/main/resources/db/schema.sql`

## 核心功能说明

### 1. 微信消息处理流程

```
微信服务器 → 验证签名 → 接收 XML 消息 → 解析消息 → 
匹配回复规则 → 构建回复 XML → 返回给微信服务器
```

### 2. 回复规则匹配逻辑

规则按优先级排序，依次匹配：
1. **关注事件** (SUBSCRIBE): 用户关注公众号时触发
2. **关键词** (KEYWORD): 根据匹配模式（完全/包含/正则）匹配
3. **消息类型** (MESSAGE_TYPE): 根据消息类型匹配
4. **默认回复** (DEFAULT): 以上都不匹配时的兜底回复

### 3. 支持的回复类型

- **TEXT**: 文本消息
- **IMAGE**: 图片消息（需要 media_id）
- **VOICE**: 语音消息（需要 media_id）
- **VIDEO**: 视频消息（需要 media_id）
- **MUSIC**: 音乐消息（需要 JSON 配置）
- **NEWS**: 图文消息（需要 JSON 配置）

## API 接口说明

### 微信消息接口

- **GET /api/wechat** - 微信服务器验证
- **POST /api/wechat** - 接收微信消息

### 回复规则管理

- **GET /api/admin/rules/page** - 分页查询规则
- **GET /api/admin/rules/{id}** - 查询规则详情
- **POST /api/admin/rules** - 创建规则
- **PUT /api/admin/rules/{id}** - 更新规则
- **DELETE /api/admin/rules/{id}** - 删除规则
- **PUT /api/admin/rules/{id}/enabled** - 启用/禁用规则
- **POST /api/admin/rules/test-match** - 测试规则匹配

完整 API 文档请访问：http://localhost:8080/api/doc.html

## 配置说明

### 微信公众号配置

1. 登录 [微信公众平台](https://mp.weixin.qq.com/)
2. 进入 开发 → 基本配置
3. 获取 AppID 和 Secret
4. 配置服务器地址：`http://你的域名/api/wechat`
5. 配置 Token 和 AES Key

### 环境变量

| 变量名 | 说明 | 是否必填 |
|--------|------|----------|
| WECHAT_APP_ID | 公众号 AppID | 是 |
| WECHAT_SECRET | 公众号 Secret | 是 |
| WECHAT_TOKEN | 微信服务器 Token | 是 |
| WECHAT_AES_KEY | 消息加解密密钥 | 否 |
| JWT_SECRET | JWT 加密密钥 | 是 |

## 安全建议

1. **生产环境必须修改默认密码**
   - 默认管理员账号：admin
   - 默认密码：admin123

2. **使用 HTTPS**
   - 微信服务器回调必须使用 HTTPS
   - 建议使用 Nginx 反向代理配置 SSL

3. **保护敏感信息**
   - 不要将 Secret、Token 等敏感信息提交到代码仓库
   - 使用环境变量或配置中心管理敏感信息

4. **定期更新 Access Token**
   - 系统已配置定时任务每天凌晨 2 点自动刷新
   - Access Token 有效期为 2 小时

## 常见问题

### 1. 微信服务器验证失败

**原因**：
- Token 配置不一致
- 签名算法错误

**解决方法**：
- 确保微信公众平台配置的 Token 与系统配置一致
- 检查服务器时间是否准确

### 2. Access Token 获取失败

**原因**：
- AppID 或 Secret 错误
- 公众号未认证

**解决方法**：
- 检查 AppID 和 Secret 配置是否正确
- 确保公众号已通过微信认证

### 3. 消息无法回复

**原因**：
- 超过 5 秒回复时限
- 回复格式错误

**解决方法**：
- 优化规则匹配逻辑，提高响应速度
- 检查 XML 格式是否符合微信规范

## 扩展开发

### 添加新的回复类型

1. 在 `ReplyRule` 实体中添加新类型
2. 在 `WechatMessageServiceImpl.buildReplyMessage()` 中实现构建逻辑
3. 在前端添加对应的选项

### 添加新的消息处理逻辑

1. 在 `WechatMessageServiceImpl.processMessage()` 中添加处理逻辑
2. 创建对应的服务和 Mapper
3. 添加 API 接口和前端页面

## 性能优化建议

1. **Redis 缓存**
   - Access Token 使用 Redis 缓存
   - 热点规则可以缓存到 Redis

2. **数据库优化**
   - 为常用查询字段添加索引
   - 定期清理历史消息记录

3. **异步处理**
   - 用户信息同步使用异步处理
   - 消息记录保存使用异步

## License

Apache License 2.0

## 联系方式

如有问题请联系系统管理员
