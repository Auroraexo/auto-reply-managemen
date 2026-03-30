# 系统架构设计文档

## 一、系统概述

微信公众号消息自动回复与后台管理系统是一个完整的 SaaS 平台，用于管理微信公众号的自动回复功能。系统采用前后端分离架构，支持 Docker 容器化部署。

## 二、技术架构图

```
┌─────────────────────────────────────────────────────────────┐
│                         用户层                                │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐          │
│  │ 微信用户    │  │ 管理员      │  │ 微信服务器  │          │
│  └──────┬──────┘  └──────┬──────┘  └──────┬──────┘          │
└─────────┼────────────────┼────────────────┼──────────────────┘
          │                │                │
          │ HTTPS          │ HTTPS          │ HTTPS
          │                │                │
┌─────────▼────────────────▼────────────────▼──────────────────┐
│                      Nginx (反向代理/负载均衡)                  │
│                    SSL 终端 / 静态资源服务                       │
└────────────────────────────┬─────────────────────────────────┘
                             │
              ┌──────────────┴──────────────┐
              │                             │
       ┌──────▼──────┐              ┌──────▼──────┐
       │   前端应用   │              │  后端 API    │
       │  Vue 3 SPA  │              │ Spring Boot │
       │  Port: 80   │              │  Port: 8080 │
       └─────────────┘              └──────┬──────┘
                                           │
                              ┌────────────┴────────────┐
                              │                         │
                       ┌──────▼──────┐          ┌──────▼──────┐
                       │    MySQL    │          │    Redis    │
                       │  数据持久化  │          │   缓存      │
                       └─────────────┘          └─────────────┘
```

## 三、核心模块设计

### 3.1 模块划分

```
auto-reply-system/
├── 微信消息处理模块 (WeChat Message Handler)
│   ├── 服务器验证
│   ├── 消息接收
│   ├── 消息解析
│   └── 回复构建
│
├── 规则引擎模块 (Rule Engine)
│   ├── 规则匹配
│   ├── 优先级排序
│   ├── 命中统计
│   └── 规则测试
│
├── 后台管理模块 (Admin Management)
│   ├── 规则管理
│   ├── 用户管理
│   ├── 消息记录
│   └── 公众号配置
│
├── 数据存储模块 (Data Storage)
│   ├── MySQL (主数据库)
│   └── Redis (缓存)
│
└── 基础设施模块 (Infrastructure)
    ├── 安全认证 (JWT)
    ├── 异常处理
    ├── 日志记录
    └── API 文档
```

### 3.2 核心流程

#### 微信消息处理流程

```
微信服务器发送消息
    ↓
Nginx 接收请求并转发
    ↓
后端 /api/wechat 接口
    ↓
验证签名 (verifyServer)
    ↓
解析 XML 消息 (parseXml)
    ↓
保存消息记录 (saveMessageRecord)
    ↓
同步用户信息 (syncAndSaveUser)
    ↓
匹配回复规则 (matchReplyRule)
    ↓
更新规则命中次数 (updateHitCount)
    ↓
构建回复消息 (buildReplyMessage)
    ↓
返回 XML 响应给微信服务器
```

#### 规则匹配算法

```
获取所有启用的规则（按优先级降序）
    ↓
遍历规则列表
    ↓
判断规则类型：
    ├─ SUBSCRIBE: 检查是否为关注事件
    ├─ DEFAULT: 直接匹配（兜底）
    ├─ MESSAGE_TYPE: 检查消息类型
    └─ KEYWORD: 
        ├─ EXACT: 完全匹配
        ├─ CONTAIN: 包含匹配
        └─ REGEX: 正则匹配
    ↓
返回第一个匹配的规则
    ↓
如果没有匹配，返回 null
```

## 四、数据库设计

### 4.1 ER 图

```
┌─────────────┐      ┌──────────────┐      ┌─────────────┐
│  sys_user   │      │ wechat_config│      │ reply_rule  │
├─────────────┤      ├──────────────┤      ├─────────────┤
│ id          │      │ id           │      │ id          │
│ username    │      │ app_id       │      │ rule_name   │
│ password    │      │ secret       │      │ rule_type   │
│ role        │      │ token        │      │ priority    │
└─────────────┘      │ is_default   │      │ match_mode  │
                     └──────────────┘      │ match_content│
                            │              │ reply_type  │
                            │              │ reply_content│
                            │              │ is_enabled  │
                            │              │ hit_count   │
                            │              └─────────────┘
                            │                     │
                            │                     │
                     ┌──────▼──────┐      ┌──────▼──────┐
                     │ wechat_user │      │message_record│
                     ├─────────────┤      ├─────────────┤
                     │ id          │      │ id          │
                     │ open_id     │      │ open_id     │
                     │ nickname    │      │ msg_type    │
                     │ gender      │      │ content     │
                     │ subscribe   │      │ reply_rule_id│
                     │ city        │      │ reply_status│
                     └─────────────┘      └─────────────┘
```

### 4.2 表结构说明

详见 `backend/src/main/resources/db/schema.sql`

核心表：
- **sys_user**: 系统管理员账户
- **wechat_config**: 微信公众号配置
- **reply_rule**: 自动回复规则（核心业务表）
- **message_record**: 消息收发记录
- **wechat_user**: 微信粉丝信息
- **material**: 素材管理
- **operation_log**: 操作日志

## 五、API 接口设计

### 5.1 接口规范

所有接口遵循 RESTful 风格，统一返回格式：

```json
{
  "code": 200,
  "message": "success",
  "data": {},
  "timestamp": 1234567890
}
```

### 5.2 接口分类

#### 微信相关接口
- `GET /api/wechat` - 微信服务器验证
- `POST /api/wechat` - 接收和處理微信消息

#### 回复规则接口
- `GET /api/admin/rules/page` - 分页查询
- `GET /api/admin/rules/{id}` - 查询详情
- `POST /api/admin/rules` - 创建规则
- `PUT /api/admin/rules/{id}` - 更新规则
- `DELETE /api/admin/rules/{id}` - 删除规则
- `PUT /api/admin/rules/{id}/enabled` - 启用/禁用
- `POST /api/admin/rules/test-match` - 测试匹配

#### 用户管理接口
- `GET /api/admin/users/page` - 用户列表
- `POST /api/admin/users` - 创建用户
- `PUT /api/admin/users/{id}` - 更新用户
- `DELETE /api/admin/users/{id}` - 删除用户

#### 消息记录接口
- `GET /api/admin/messages/page` - 消息列表
- `GET /api/admin/messages/{id}` - 消息详情

完整 API 文档通过 Swagger 自动生成，访问：`/api/doc.html`

## 六、安全设计

### 6.1 认证机制

- **后台管理**: JWT Token 认证
  - Token 有效期：24 小时
  - 存储在 localStorage
  - 每次请求携带在 Header 中

- **微信服务器**: Signature 签名验证
  - Token 验证
  - Timestamp 时间戳
  - Nonce 随机数
  - SHA1 加密算法

### 6.2 权限控制

基于角色的访问控制（RBAC）：
- **ADMIN**: 管理员，拥有所有权限
- **USER**: 普通用户，只有查看权限

使用 Spring Security 进行权限拦截。

### 6.3 数据安全

- 密码 BCrypt 加密存储
- SQL 注入防护（MyBatis 预编译）
- XSS 攻击防护
- CORS 跨域配置
- HTTPS 传输加密

## 七、性能优化

### 7.1 缓存策略

```java
// Access Token 缓存（Redis）
Key: wechat:access_token
TTL: 7000 秒（预留 200 秒缓冲）

// 热点规则缓存（可扩展）
Key: rule:hot:{ruleId}
TTL: 根据业务需求配置
```

### 7.2 数据库优化

- 索引设计：
  - `reply_rule`: idx_rule_type, idx_match_mode, idx_is_enabled, idx_priority
  - `message_record`: idx_open_id, idx_msg_type, idx_create_time
  - `wechat_user`: uk_open_id, idx_subscribe

- 分表策略（可选）：
  - message_record 按月分表
  - operation_log 按月分表

### 7.3 异步处理

```java
// 用户信息同步（@Async）
@Async
public void syncAndSaveUser(String openId) {
    // 异步同步用户信息
}

// 消息记录保存（可优化为异步）
```

### 7.4 定时任务

```java
// Access Token 刷新（每天凌晨 2 点）
@Scheduled(cron = "0 0 2 * * ?")
public boolean refreshAccessToken() {
    // 刷新逻辑
}
```

## 八、扩展性设计

### 8.1 插件化规则引擎

可以扩展更多规则类型：
```java
public interface RuleMatcher {
    boolean matches(ReplyRule rule, WechatMessage message);
}

// 实现类：
// - KeywordRuleMatcher
// - EventTypeRuleMatcher
// - UserTagRuleMatcher
// - TimeRangeRuleMatcher
```

### 8.2 多公众号支持

当前支持单公众号，可扩展为：
- 一个系统管理多个公众号
- 每个公众号独立的配置和规则
- 规则跨号复用

### 8.3 客服消息扩展

可扩展客服消息功能：
- 主动推送消息
- 批量群发
- 模板消息

## 九、监控与日志

### 9.1 日志级别

```yaml
logging:
  level:
    root: INFO
    com.wechat.autoreply: DEBUG
```

### 9.2 关键日志点

- 微信消息接收
- 规则匹配过程
- API 请求响应
- 异常错误信息
- 用户操作记录

### 9.3 监控指标

- API 响应时间
- 规则匹配成功率
- 消息处理延迟
- 数据库连接池状态
- Redis 缓存命中率

## 十、部署架构

### 10.1 开发环境

```
本地运行：
- Backend: localhost:8080
- Frontend: localhost:3000
- MySQL: localhost:3306
- Redis: localhost:6379
```

### 10.2 生产环境

```
Docker Compose 部署：
- frontend (Nginx): Port 80
- backend (Spring Boot): Port 8080
- mysql: Port 3306
- redis: Port 6379
```

### 10.3 高可用方案（可选）

```
         ┌──────────┐
         │  SLB     │
         └────┬─────┘
              │
    ┌─────────┴─────────┐
    │                   │
┌───▼────┐       ┌─────▼───┐
│ Nginx1 │       │ Nginx2  │
└───┬────┘       └─────┬───┘
    │                   │
    └─────────┬─────────┘
              │
    ┌─────────┴─────────┐
    │                   │
┌───▼────┐       ┌─────▼───┐
│Backend1│       │Backend2 │
└───┬────┘       └─────┬───┘
    │                   │
    └─────────┬─────────┘
              │
    ┌─────────┴─────────┐
    │                   │
┌───▼────┐       ┌─────▼───┐
│ MySQL  │       │  Redis  │
│ Master │       │ Cluster │
└────────┘       └─────────┘
```

## 十一、技术选型理由

### 11.1 为什么选择 Spring Boot？

- 快速开发，约定优于配置
- 丰富的生态系统
- 完善的监控和管理功能
- 易于测试和部署

### 11.2 为什么选择 Vue 3？

- 轻量级，性能好
- 组合式 API，代码组织更灵活
- TypeScript 支持好
- 学习曲线平缓

### 11.3 为什么选择 MyBatis-Plus？

- 简化 CRUD 操作
- 支持 Lambda 查询
- 内置分页插件
- 支持代码生成

### 11.4 为什么使用 Redis？

- 高性能缓存
- 支持多种数据结构
- 分布式锁
- 会话管理

## 十二、未来规划

### 短期目标
- [ ] 完善单元测试
- [ ] 添加数据统计分析功能
- [ ] 优化前端 UI/UX
- [ ] 支持图文消息编辑器

### 中期目标
- [ ] 多公众号管理
- [ ] 客服消息推送
- [ ] 用户标签分组
- [ ] 定时任务管理

### 长期目标
- [ ] AI 智能回复（接入 ChatGPT 等）
- [ ] 数据分析报表
- [ ] 移动端管理 APP
- [ ] 开放平台（第三方开发者）

---

**文档版本**: v1.0  
**最后更新**: 2026-04-01  
**维护者**: 系统架构团队
