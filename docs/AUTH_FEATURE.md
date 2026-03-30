# 🔐 用户认证功能说明

## ✅ 已完成的功能

### 后端功能

#### 1. 用户实体和数据库表
- ✅ `SysUser` 实体类
- ✅ `sys_user` 数据库表（已存在）
- ✅ BCrypt 密码加密

#### 2. JWT 认证
- ✅ `JwtUtil` 工具类
- ✅ Token 生成和验证
- ✅ Token 中解析用户信息

#### 3. Spring Security 配置
- ✅ `SecurityConfig` 安全配置
- ✅ JWT 过滤器 `JwtAuthenticationFilter`
- ✅ 免认证接口配置（登录、注册、微信接口、Swagger）

#### 4. 用户服务
- ✅ `UserService` 接口
- ✅ `UserServiceImpl` 实现
- ✅ 登录功能（验证用户名密码，生成 Token）
- ✅ 注册功能（检查用户名，BCrypt 加密密码）
- ✅ 用户管理（查询、更新、删除）

#### 5. 认证控制器
- ✅ `AuthController` 
- ✅ POST `/auth/login` - 用户登录
- ✅ POST `/auth/register` - 用户注册
- ✅ POST `/auth/logout` - 退出登录
- ✅ GET `/auth/current` - 获取当前用户信息

### 前端功能

#### 1. 登录页面
- ✅ `/login` 路由
- ✅ `Login.vue` 组件
- ✅ 表单验证（用户名、密码）
- ✅ 调用登录 API
- ✅ 保存 Token 到 localStorage
- ✅ 登录成功跳转到首页

#### 2. 注册页面
- ✅ `/register` 路由
- ✅ `Register.vue` 组件
- ✅ 表单验证（用户名、密码、确认密码、邮箱）
- ✅ 调用注册 API
- ✅ 注册成功跳转到登录页

#### 3. 路由守卫
- ✅ 全局前置守卫 `beforeEach`
- ✅ 未登录访问需要认证的页面跳转到登录页
- ✅ 已登录访问登录/注册页跳转到首页
- ✅ 页面标题设置

#### 4. 布局优化
- ✅ 退出登录功能
- ✅ 清除本地存储
- ✅ 确认对话框
- ✅ 跳转到登录页

#### 5. API 封装
- ✅ `user.ts` API 接口
- ✅ `login()` - 登录
- ✅ `register()` - 注册
- ✅ `logout()` - 退出
- ✅ `getCurrentUser()` - 获取当前用户

## 📋 API 接口文档

### 1. 用户登录

**接口**: `POST /api/auth/login`

**请求参数**:
```json
{
  "username": "admin",
  "password": "admin123"
}
```

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "tokenType": "Bearer",
    "expiresIn": 86400000,
    "userId": 1,
    "username": "admin",
    "role": "ADMIN"
  }
}
```

### 2. 用户注册

**接口**: `POST /api/auth/register`

**请求参数**:
```json
{
  "username": "testuser",
  "password": "test123456",
  "confirmPassword": "test123456",
  "nickname": "测试用户",
  "email": "test@example.com"
}
```

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 2,
    "username": "testuser",
    "nickname": "测试用户",
    "email": "test@example.com",
    "role": "USER",
    "status": 1
  }
}
```

### 3. 退出登录

**接口**: `POST /api/auth/logout`

**响应**:
```json
{
  "code": 200,
  "message": "success"
}
```

### 4. 获取当前用户信息

**接口**: `GET /api/auth/current`

**请求头**:
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "username": "admin",
    "nickname": "系统管理员",
    "email": "admin@example.com",
    "role": "ADMIN",
    "status": 1
  }
}
```

## 🔒 安全机制

### 1. 密码加密
- 使用 BCrypt 强哈希算法
- 自动加盐
- 不可逆加密

### 2. Token 认证
- JWT Token 有效期 24 小时
- 基于 HMACSHA256 签名
- 无状态认证

### 3. 请求保护
- 受保护接口需要 Bearer Token
- Token 在 Header 中传递
- 格式：`Authorization: Bearer <token>`

### 4. 路由保护
- 前端路由守卫
- 未登录自动跳转
- Token 失效处理

## 🚀 使用流程

### 注册新用户

1. 访问 `/register` 页面
2. 填写用户名、密码、确认密码
3. 可选填写昵称、邮箱
4. 点击"注册"按钮
5. 注册成功跳转到登录页

### 登录

1. 访问 `/login` 页面
2. 输入用户名和密码
3. 点击"登录"按钮
4. 登录成功跳转到首页
5. Token 保存到 localStorage

### 访问受保护接口

前端自动在请求头添加 Token：
```javascript
// request.ts 中已配置
request.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})
```

### 退出登录

1. 点击右上角用户菜单
2. 选择"退出登录"
3. 确认后清除 Token
4. 跳转到登录页

## 📝 测试步骤

### 1. 测试注册功能

```bash
# 使用 curl 测试
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "test123456",
    "confirmPassword": "test123456",
    "nickname": "测试用户"
  }'
```

### 2. 测试登录功能

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "test123456"
  }'
```

### 3. 测试受保护接口

```bash
# 使用登录返回的 Token
curl -X GET http://localhost:8080/api/admin/rules/page \
  -H "Authorization: Bearer <your_token>"
```

## ⚠️ 注意事项

### 1. 默认管理员账户

数据库初始化时会自动创建管理员账户：
- **用户名**: admin
- **密码**: admin123
- **角色**: ADMIN

⚠️ **首次登录后请立即修改密码！**

### 2. Token 管理

- Token 存储在 localStorage 中
- 有效期 24 小时
- 刷新页面不会丢失
- 退出登录时清除

### 3. 密码要求

- 至少 6 位字符
- 建议使用大小写字母 + 数字组合
- 不要使用简单密码

### 4. 用户名要求

- 3-20 位字符
- 不能与已有用户重复
- 不能使用特殊字符

## 🔧 扩展功能（可选）

### 1. 密码找回
- [ ] 邮箱验证
- [ ] 发送验证码
- [ ] 重置密码

### 2. 个人信息
- [ ] 修改个人资料
- [ ] 上传头像
- [ ] 修改密码

### 3. 权限管理
- [ ] 角色管理
- [ ] 权限控制
- [ ] 菜单权限

### 4. Token 刷新
- [ ] Refresh Token 机制
- [ ] 自动刷新 Token
- [ ] Token 黑名单

---

**用户认证功能已完成！现在可以注册登录了！** 🎉
