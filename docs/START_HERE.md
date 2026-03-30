# 🎉 本地运行完成指南

## ✅ 配置已完成

我已经为你完成了以下配置：

### 1. 数据库配置更新
- ✅ MySQL 密码已设置为：`123456`
- ✅ Redis 配置已优化
- ✅ 配置文件位置：`backend/src/main/resources/application.yml`

### 2. 一键启动脚本
创建了 3 个便捷的启动脚本：

#### 📋 check-env.bat - 环境检查工具
运行后会检查：
- ✅ Java 是否安装
- ✅ Maven 是否安装
- ✅ Node.js 是否安装
- ✅ npm 是否安装
- ✅ Docker 是否运行
- ⚠️ 端口是否被占用（3306, 6379, 8080, 3000）

#### 🚀 start-local.bat - 后端一键启动
自动完成：
- ✅ 启动 MySQL 容器（密码：123456）
- ✅ 启动 Redis 容器
- ✅ 初始化数据库表结构
- ✅ 编译 Maven 项目
- ✅ 启动 Spring Boot 应用

访问：http://localhost:8080/api/doc.html

#### 💻 start-frontend.bat - 前端一键启动
自动完成：
- ✅ 检查并安装 npm 依赖
- ✅ 启动 Vite 开发服务器

访问：http://localhost:3000

## 📖 使用步骤

### 第一次运行

#### Windows 用户（推荐脚本）

1. **检查环境**
   ```bash
   双击运行：check-env.bat
   ```
   确保所有✅都显示正常

2. **启动后端**
   ```bash
   双击运行：start-local.bat
   ```
   等待看到成功提示

3. **启动前端**（新开一个窗口）
   ```bash
   双击运行：start-frontend.bat
   ```
   等待看到成功提示

#### Linux/Mac 用户（Shell 脚本）

1. **启动后端**
   ```bash
   chmod +x start-local.sh
   ./start-local.sh
   ```

2. **启动前端**（新终端）
   ```bash
   chmod +x start-frontend.sh
   ./start-frontend.sh
   ```

#### 或使用纯命令（所有系统通用）

详见：[COMMANDS.md](COMMANDS.md) - 完整的命令速查表

4. **访问系统**
   - 浏览器打开：http://localhost:3000
   - 登录账户：admin / admin123

### 后续运行

只需要运行两个脚本：
1. `start-local.bat` （后端）
2. `start-frontend.bat` （前端）

## 🔧 技术栈说明

### 后端
- **框架**: Spring Boot 3.2.0
- **数据库**: MySQL 8.0 (Docker)
- **缓存**: Redis 7.x (Docker)
- **端口**: 8080

### 前端
- **框架**: Vue 3.4 + TypeScript
- **构建**: Vite 5.0
- **UI**: Element Plus 2.5
- **端口**: 3000

## 📝 重要信息

### 数据库连接
```yaml
主机：localhost
端口：3306
数据库：wechat_auto_reply
用户名：root
密码：123456
```

### Redis 连接
```yaml
主机：localhost
端口：6379
密码：无
数据库：0
```

### 默认账户
```
用户名：admin
密码：admin123
⚠️ 首次登录后请修改密码！
```

## 🐛 常见问题

### 问题 1: Docker 未启动
**现象**: 运行脚本时提示 Docker 错误
**解决**: 
- Windows: 启动 Docker Desktop
- 确保 Docker Desktop 完全启动后再运行脚本

### 问题 2: 端口被占用
**现象**: 提示端口 3306/6379/8080/3000 已被占用
**解决**:
```bash
# 查看占用进程
netstat -ano | findstr :端口号

# 杀死进程
taskkill /F /PID 进程号
```

### 问题 3: Maven 下载慢
**现象**: 编译时间很长
**解决**: 配置阿里云镜像
编辑 `backend/pom.xml`，添加：
```xml
<mirror>
    <id>aliyun</id>
    <mirrorOf>central</mirrorOf>
    <url>https://maven.aliyun.com/repository/public</url>
</mirror>
```

### 问题 4: npm install 失败
**现象**: 前端依赖安装失败
**解决**:
```bash
# 使用淘宝镜像
npm config set registry https://registry.npmmirror.com
npm cache clean --force
npm install
```

### 问题 5: 数据库初始化失败
**现象**: 表结构未创建
**解决**:
```bash
# 手动导入
docker exec -i wechat-mysql mysql -u root -p123456 wechat_auto_reply < backend\src\main\resources\db\schema.sql
```

## 📊 验证成功标志

### 后端启动成功
看到以下信息：
```
========================================
微信公众号自动回复管理系统启动成功！
API 文档地址：http://localhost:8080/api/doc.html
========================================
```

### 前端启动成功
看到以下信息：
```
VITE v5.0.x ready in xxx ms

➜  Local:   http://localhost:3000/
```

### Docker 容器运行中
```bash
docker ps
# 应该看到 wechat-mysql 和 wechat-redis
```

## 🎯 下一步操作

启动成功后：

1. **登录系统**
   - 访问 http://localhost:3000
   - 使用 admin/admin123 登录

2. **配置公众号**
   - 进入"公众号配置"页面
   - 填写你的微信公众号 AppID 和 Secret

3. **创建规则**
   - 进入"回复规则"页面
   - 点击"新增规则"
   - 创建你的第一个自动回复规则

4. **测试功能**
   - 使用微信测试号或真实公众号
   - 发送消息测试回复

## 📚 详细文档

- **LOCAL_SETUP.md** - 详细的本地运行指南
- **README.md** - 项目总览
- **DEPLOYMENT.md** - 部署指南
- **QUICK_REFERENCE.md** - 快速参考

## 🆘 获取帮助

如果遇到问题：

1. 查看详细文档：LOCAL_SETUP.md
2. 查看日志输出
3. 检查 Docker 容器状态
4. 查看常见问题部分

---

**祝你使用愉快！** 🎉

如有任何问题，请随时询问。
