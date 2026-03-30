# 🎯 角色权限快速参考

## 三级角色体系

### 👑 SUPER_ADMIN（超级管理员）
- **权限**: 全部权限 ✅
- **职责**: 系统管理、用户管理、公众号配置
- **默认账户**: admin / admin123

### ⚙️ ADMIN（管理员）  
- **权限**: 业务管理权限（继承 USER 权限）
- **职责**: 回复规则、消息记录、粉丝管理
- **不能**: 管理系统用户、修改公众号配置

### 👤 USER（普通用户）
- **权限**: 只读权限 🔍
- **职责**: 查看规则、消息、统计
- **不能**: 修改、删除任何数据

---

## 权限对比表

| 功能 | SUPER_ADMIN | ADMIN | USER |
|------|:-----------:|:-----:|:----:|
| 回复规则管理 | ✅ | ✅ | 🔍 |
| 消息记录查看 | ✅ | ✅ | 🔍 |
| 微信粉丝管理 | ✅ | ✅ | 🔍 |
| 素材管理 | ✅ | ✅ | 🔍 |
| 公众号配置 | ✅ | ❌ | ❌ |
| 系统用户管理 | ✅ | ❌ | ❌ |
| 操作日志 | ✅ | ⚠️ | ❌ |

✅ = 完全权限 | 🔍 = 只读 | ❌ = 无权限 | ⚠️ = 受限

---

## 角色层级关系

```
SUPER_ADMIN (最高权限)
    ↓ implies
ADMIN (次级权限)
    ↓ implies  
USER (基础权限)
```

**特点**: 
- 上级自动拥有下级所有权限
- 权限逐级递减
- 职责明确分离

---

## 使用建议

### 推荐配置

**小团队** (1-3 人):
- 1 × SUPER_ADMIN
- 1-2 × ADMIN  
- 0-1 × USER

**中型企业** (5-10 人):
- 1-2 × SUPER_ADMIN
- 2-3 × ADMIN
- 3-5 × USER

**大型企业** (20+ 人):
- 2 × SUPER_ADMIN
- 5-8 × ADMIN
- 15+ × USER

---

## 安全提醒

⚠️ **重要**:
1. 首次登录后**立即修改** admin 密码
2. **谨慎授予** SUPER_ADMIN 角色
3. 定期**审查**用户权限
4. 敏感操作**审批流程**

---

## API 测试

### 创建不同角色的用户

```bash
# 创建管理员
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "manager",
    "password": "manager123",
    "nickname": "运营经理"
  }'

# 手动在数据库修改角色
UPDATE sys_user SET role = 'ADMIN' WHERE username = 'manager';
```

### 验证权限

```bash
# 用 ADMIN 账户登录获取 Token
TOKEN="eyJhbGc..."

# 尝试访问用户管理接口（应该被拒绝）
curl -X GET http://localhost:8080/api/admin/users/page \
  -H "Authorization: Bearer $TOKEN"
# 预期：403 Forbidden

# 尝试访问规则列表（应该成功）
curl -X GET http://localhost:8080/api/admin/rules/page \
  -H "Authorization: Bearer $TOKEN"
# 预期：200 OK
```

---

## 数据库操作

### 查看所有用户角色

```sql
SELECT id, username, nickname, role, status 
FROM sys_user;
```

### 修改用户角色

```sql
-- 提升为管理员
UPDATE sys_user 
SET role = 'ADMIN' 
WHERE username = 'testuser';

-- 降级为普通用户
UPDATE sys_user 
SET role = 'USER' 
WHERE username = 'testuser';

-- 禁用用户
UPDATE sys_user 
SET status = 0 
WHERE username = 'baduser';
```

### 查看角色分布

```sql
SELECT role, COUNT(*) as count 
FROM sys_user 
GROUP BY role;
```

---

## 前端权限控制

### 路由配置示例

```javascript
{
  path: '/users',
  name: 'Users',
  component: () => import('@/views/Users.vue'),
  meta: { 
    title: '用户管理',
    roles: ['SUPER_ADMIN'] // 只有超级管理员可访问
  }
}
```

### 按钮权限示例

```vue
<!-- 自定义指令 -->
<el-button v-permission="'SUPER_ADMIN'">
  删除用户
</el-button>

<el-button v-permission="['ADMIN', 'SUPER_ADMIN']">
  编辑规则
</el-button>
```

---

## 常见问题

**Q: 可以自定义角色吗？**  
A: 当前版本支持 3 个预定义角色，未来可扩展。

**Q: 如何添加新角色？**  
A: 修改 RoleConstants.java 并更新权限配置。

**Q: 角色可以动态调整吗？**  
A: 可以，通过数据库直接修改或管理界面（待实现）。

**Q: 权限如何继承？**  
A: 通过 Spring Security 角色层级自动继承。

---

**选择合适的角色，确保系统安全！** 🔐
