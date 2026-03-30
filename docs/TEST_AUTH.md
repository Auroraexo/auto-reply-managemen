# 🧪 用户认证功能测试指南

## 快速测试（5 分钟）

### 第 1 步：启动后端

```bash
cd backend
mvn spring-boot:run
```

等待看到成功提示。

### 第 2 步：启动前端

```bash
cd frontend
npm run dev
```

访问 http://localhost:3000

### 第 3 步：测试注册

1. 打开浏览器，访问 http://localhost:3000
2. 自动跳转到登录页
3. 点击"立即注册"
4. 填写注册表单：
   - 用户名：`testuser`
   - 密码：`test123456`
   - 确认密码：`test123456`
   - 昵称：`测试用户`
5. 点击"注册"
6. ✅ 应该提示"注册成功，请登录"

### 第 4 步：测试登录

1. 自动跳转到登录页
2. 输入刚注册的账号：
   - 用户名：`testuser`
   - 密码：`test123456`
3. 点击"登录"
4. ✅ 应该跳转到首页（/rules）
5. ✅ 可以看到回复规则列表

### 第 5 步：测试退出登录

1. 点击右上角"管理员"下拉菜单
2. 点击"退出登录"
3. 确认退出
4. ✅ 应该清除 Token 并跳转到登录页

## API 接口测试

### 使用 Postman 或 curl 测试

#### 1. 测试注册接口

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "apiuser",
    "password": "api123456",
    "confirmPassword": "api123456",
    "nickname": "API 用户",
    "email": "api@test.com"
  }'
```

**预期响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": xxx,
    "username": "apiuser",
    "nickname": "API 用户"
  }
}
```

#### 2. 测试登录接口

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "apiuser",
    "password": "api123456"
  }'
```

**预期响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "accessToken": "eyJhbGc...",
    "tokenType": "Bearer",
    "userId": xxx,
    "username": "apiuser",
    "role": "USER"
  }
}
```

#### 3. 测试受保护接口（需要 Token）

```bash
# 复制上面返回的 accessToken
TOKEN="eyJhbGc..."  # 替换为实际的 token

curl -X GET http://localhost:8080/api/admin/rules/page \
  -H "Authorization: Bearer $TOKEN"
```

**预期响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [],
    "total": 0
  }
}
```

#### 4. 测试无效 Token

```bash
curl -X GET http://localhost:8080/api/admin/rules/page \
  -H "Authorization: Bearer invalid_token"
```

**预期**: 应该返回 401 或未认证错误

## 常见问题排查

### 问题 1: 注册失败 - 用户名已存在

**原因**: 数据库中已有相同用户名

**解决**: 
```sql
-- 查看已有用户
SELECT * FROM sys_user;

-- 删除测试用户（如果需要）
DELETE FROM sys_user WHERE username = 'testuser';
```

### 问题 2: 登录失败 - 用户名或密码错误

**检查**:
1. 用户名是否正确
2. 密码是否至少 6 位
3. 数据库中的密码是否是 BCrypt 加密

**验证**:
```sql
-- 查看用户（密码是加密的）
SELECT id, username, role, status FROM sys_user WHERE username = 'testuser';
```

### 问题 3: Token 无效

**可能原因**:
1. Token 格式错误
2. Token 过期（24 小时）
3. JWT_SECRET 配置变更

**解决**: 重新登录获取新 Token

### 问题 4: 前端无法跳转

**检查浏览器控制台**:
- 是否有 JavaScript 错误
- Network 面板查看请求状态
- localStorage 中是否有 token

**清理缓存**:
```javascript
// 在浏览器控制台执行
localStorage.clear()
location.reload()
```

## 数据库验证

### 查看注册用户

```sql
use wechat_auto_reply;

-- 查看所有用户
SELECT id, username, nickname, email, role, status, create_time 
FROM sys_user;

-- 查看特定用户
SELECT * FROM sys_user WHERE username = 'testuser';
```

### 验证密码加密

```sql
-- 密码应该是 BCrypt 加密后的字符串（以$2a$开头）
SELECT username, password FROM sys_user;

-- 示例：admin 用户的密码
-- $2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iDJd1X6YzKJvQlPJ9FKbxqQO7jKi
```

## 安全测试建议

### 1. 测试密码强度

尝试注册弱密码：
- `123456` ✅ (允许，但至少 6 位)
- `abc` ❌ (太短)
- 空密码 ❌ (不允许)

### 2. 测试 SQL 注入

尝试在用户名中使用 SQL 注入：
```
username: admin' OR '1'='1
username: '; DROP TABLE users; --
```
✅ 应该被拦截或转义

### 3. 测试 XSS 攻击

尝试在昵称中输入脚本：
```
nickname: <script>alert('xss')</script>
```
✅ 应该被转义或过滤

### 4. 测试 Token 篡改

修改 Token 的一部分：
```
原始：eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
修改：eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9XXX...
```
✅ 应该验证失败

## 性能测试（可选）

### 并发登录测试

使用 Apache Bench:
```bash
ab -n 100 -c 10 \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}' \
  http://localhost:8080/api/auth/login
```

### Token 验证性能

```bash
TOKEN="eyJhbGc..."  # 有效的 token

ab -n 1000 -c 50 \
  -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/api/admin/rules/page
```

## 测试完成清单

- [ ] 成功注册新用户
- [ ] 成功登录
- [ ] 成功访问受保护接口
- [ ] 成功退出登录
- [ ] 错误密码登录失败
- [ ] 不存在用户登录失败
- [ ] Token 验证正常
- [ ] 路由守卫正常工作
- [ ] 前端表单验证正常
- [ ] API 接口响应正常

---

**所有测试通过说明认证成功！** ✅
