# 👥 角色权限设计文档

## 📋 角色体系

本系统采用 **三级角色体系**，确保权限管理的安全性和灵活性。

### 角色层级结构

```
┌─────────────────┐
│  SUPER_ADMIN    │ ← 超级管理员（最高权限）
│  (超级管理员)    │
└────────┬────────┘
         │ implies
         ▼
┌─────────────────┐
│     ADMIN       │ ← 管理员（次级权限）
│   (管理员)       │
└────────┬────────┘
         │ implies
         ▼
┌─────────────────┐
│      USER       │ ← 普通用户（基础权限）
│   (普通用户)     │
└─────────────────┘
```

---

## 🎯 角色详细定义

### 1. SUPER_ADMIN（超级管理员）

**角色说明**: 系统最高管理者，拥有所有权限

**默认账户**:
- 用户名：`admin`
- 密码：`admin123`

**主要职责**:
- 系统整体管理
- 用户账号管理
- 公众号配置管理
- 角色权限分配
- 系统参数配置

**权限列表**:
| 模块 | 权限项 | 权限 |
|------|--------|------|
| 回复规则 | 查看、新增、编辑、删除、启用/禁用 | ✅ 全部 |
| 消息记录 | 查看、导出、统计 | ✅ 全部 |
| 微信用户 | 查看、标签、备注 | ✅ 全部 |
| 素材管理 | 上传、查看、删除 | ✅ 全部 |
| 公众号配置 | 查看、修改 | ✅ 全部 |
| 系统用户 | 查看、新增、编辑、删除、角色分配 | ✅ 全部 |
| 操作日志 | 查看所有日志 | ✅ 全部 |
| 数据统计 | 查看所有统计 | ✅ 全部 |

---

### 2. ADMIN（管理员）

**角色说明**: 日常运营管理者，拥有业务管理权限

**主要职责**:
- 回复规则配置
- 消息记录查看
- 微信粉丝管理
- 素材管理
- 数据统计查看

**权限列表**:
| 模块 | 权限项 | 权限 |
|------|--------|------|
| 回复规则 | 查看、新增、编辑、删除、启用/禁用 | ✅ 全部 |
| 消息记录 | 查看、导出、统计 | ✅ 全部 |
| 微信用户 | 查看、标签、备注 | ✅ 全部 |
| 素材管理 | 上传、查看、删除 | ✅ 全部 |
| 公众号配置 | 仅查看 | ❌ 不能修改 |
| 系统用户 | 无权限 | ❌ 不能管理 |
| 操作日志 | 仅查看自己的 | ⚠️ 受限 |
| 数据统计 | 查看统计 | ✅ 全部 |

---

### 3. USER（普通用户）

**角色说明**: 基础使用者，只有查看权限

**主要职责**:
- 查看回复规则
- 查看消息记录
- 查看统计数据

**权限列表**:
| 模块 | 权限项 | 权限 |
|------|--------|------|
| 回复规则 | 仅查看 | ⚠️ 只读 |
| 消息记录 | 仅查看 | ⚠️ 只读 |
| 微信用户 | 仅查看 | ⚠️ 只读 |
| 素材管理 | 仅查看 | ⚠️ 只读 |
| 公众号配置 | 无权限 | ❌ 不可见 |
| 系统用户 | 无权限 | ❌ 不可见 |
| 操作日志 | 无权限 | ❌ 不可见 |
| 数据统计 | 查看统计 | ✅ 全部 |

---

## 🔐 权限控制实现

### 后端权限控制

#### 1. Spring Security 配置

```java
// SecurityConfig.java
.authorizeHttpRequests(auth -> auth
    // 放行登录注册接口
    .requestMatchers("/auth/**").permitAll()
    // 放行微信接口
    .requestMatchers("/wechat/**").permitAll()
    // 需要特定角色的接口
    .requestMatchers("/admin/users/**").hasRole("SUPER_ADMIN")
    .requestMatchers("/admin/config/**").hasRole("SUPER_ADMIN")
    .requestMatchers("/admin/rules/**").hasAnyRole("SUPER_ADMIN", "ADMIN")
    // 其他请求需要认证
    .anyRequest().authenticated()
)
```

#### 2. 方法级权限注解

```java
// 需要超级管理员权限
@RequireRole({RoleConstants.SUPER_ADMIN})
@DeleteMapping("/{id}")
public Result<Void> deleteUser(@PathVariable Long id) {
    userService.deleteUser(id);
    return Result.success();
}

// 需要管理员或以上权限
@RequireRole({RoleConstants.SUPER_ADMIN, RoleConstants.ADMIN})
@PostMapping
public Result<ReplyRule> createRule(@RequestBody ReplyRuleDTO ruleDTO) {
    return Result.success(replyRuleService.createRule(ruleDTO));
}
```

### 前端权限控制

#### 1. 路由守卫

```javascript
// router/index.ts
router.beforeEach((to, from, next) => {
  const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
  const role = userInfo.role
  
  // 根据角色判断是否有权限访问
  if (to.meta.roles && !to.meta.roles.includes(role)) {
    next('/403') // 无权限跳转
  } else {
    next()
  }
})
```

#### 2. 按钮级权限

```vue
<template>
  <!-- 只有超级管理员可见 -->
  <el-button 
    v-permission="['SUPER_ADMIN']"
    @click="handleDelete"
  >
    删除
  </el-button>
  
  <!-- 管理员和超级管理员可见 -->
  <el-button 
    v-permission="['ADMIN', 'SUPER_ADMIN']"
    @click="handleEdit"
  >
    编辑
  </el-button>
</template>
```

---

## 📊 角色使用场景

### 场景 1: 企业管理

**公司架构**:
- 老板 → SUPER_ADMIN（查看所有数据）
- 运营经理 → ADMIN（管理公众号）
- 客服专员 → USER（查看消息和规则）

### 场景 2: 多公众号代运营

**运营模式**:
- 平台管理员 → SUPER_ADMIN（管理所有客户）
- 客户 A 运营 → ADMIN（只管理客户 A 的公众号）
- 实习生 → USER（只能查看学习）

### 场景 3: 个人使用

**个人场景**:
- 自己 → SUPER_ADMIN（完全控制）
- 朋友帮忙 → USER（临时查看）

---

## 🔄 角色扩展性

### 未来可扩展的角色

1. **GUEST（访客）**
   - 临时访问权限
   - 时效性限制
   - 只读权限

2. **OPERATOR（操作员）**
   - 介于 ADMIN 和 USER 之间
   - 可以执行简单操作
   - 不能修改配置

3. **AUDITOR（审计员）**
   - 查看所有数据和日志
   - 不能执行修改操作
   - 独立于管理体系

---

## 📝 角色管理最佳实践

### 1. 最小权限原则

- 默认授予最低必要权限
- 根据需要逐步提升
- 定期审查权限分配

### 2. 职责分离

- 超级管理员和管理员分离
- 操作和审计分离
- 避免权限过度集中

### 3. 安全建议

⚠️ **重要提示**:

1. **修改默认密码**
   - admin 账户首次登录后立即修改密码
   - 使用强密码（至少 8 位，包含大小写字母+数字）

2. **定期审查**
   - 每月检查用户权限
   - 及时回收离职人员权限
   - 审计敏感操作日志

3. **权限分配**
   - 谨慎授予 SUPER_ADMIN 角色
   - ADMIN 角色不超过 3 人
   - USER 角色按需分配

4. **监控告警**
   - 敏感操作短信通知
   - 异常登录告警
   - 权限变更审批

---

## 🎯 推荐配置

### 初创团队（1-3 人）

```
SUPER_ADMIN: 1 人（创始人/技术负责人）
ADMIN: 1-2 人（运营人员）
USER: 0-1 人（实习生/兼职）
```

### 中型企业（5-10 人）

```
SUPER_ADMIN: 1-2 人（CTO/技术总监）
ADMIN: 2-3 人（运营主管/客服主管）
USER: 3-5 人（客服人员/数据分析）
```

### 大型企业（20+ 人）

```
SUPER_ADMIN: 2 人（互相备份）
ADMIN: 5-8 人（各部门负责人）
USER: 15+ 人（一线员工）
```

---

## 📖 相关文档

- **AUTH_FEATURE.md** - 用户认证功能说明
- **TEST_AUTH.md** - 测试指南
- **ARCHITECTURE.md** - 系统架构设计

---

**合理的角色权限设计是系统安全的基石！** 🔒
