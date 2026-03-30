# 🎉 命令启动完成指南

## ✅ 已创建的启动脚本

### Windows 脚本（.bat）

1. **check-env.bat** - 环境检查工具
   - 检查 Java、Maven、Node.js、npm、Docker
   - 检查端口占用情况
   - ✅/❌ 直观显示结果

2. **start-local.bat** - 后端一键启动
   - 自动启动 MySQL 容器
   - 自动启动 Redis 容器
   - 初始化数据库
   - 编译并启动 Spring Boot 应用

3. **start-frontend.bat** - 前端一键启动
   - 自动安装依赖（首次）
   - 启动 Vite 开发服务器

### Linux/Mac 脚本（.sh）

1. **start-local.sh** - 后端启动脚本
2. **start-frontend.sh** - 前端启动脚本

## 🚀 快速开始（3 步）

### Windows 用户

```bash
# 第 1 步：检查环境
双击运行：check-env.bat

# 第 2 步：启动后端
双击运行：start-local.bat
等待看到："微信公众号自动回复管理系统启动成功！"

# 第 3 步：启动前端（新开窗口）
双击运行：start-frontend.bat
等待看到："Local: http://localhost:3000/"
```

### Linux/Mac 用户

```bash
# 第 1 步：启动后端
chmod +x start-local.sh
./start-local.sh

# 第 2 步：启动前端（新终端）
chmod +x start-frontend.sh
./start-frontend.sh
```

## 📋 完整命令参考

所有可用命令请查看：**[COMMANDS.md](COMMANDS.md)**

包括：
- ✅ 手动启动命令（所有系统通用）
- ✅ Docker Compose 命令
- ✅ 常用运维命令
- ✅ 故障排查命令

## 🎯 访问地址

启动成功后：

| 服务 | 地址 | 说明 |
|------|------|------|
| **前端界面** | http://localhost:3000 | 管理后台 |
| **API 文档** | http://localhost:8080/api/doc.html | Swagger UI |
| **后端 API** | http://localhost:8080/api | RESTful 接口 |

登录账户：`admin / admin123`

## 💡 提示

### 首次运行
- ⏱️ 需要 5-10 分钟下载依赖
- 🐳 确保 Docker Desktop 已启动
- 📦 Maven 和 npm 会下载所有依赖

### 后续运行
- ⚡ 只需运行启动脚本
- 🔄 依赖已存在，启动更快
- 📊 数据持久化在 Docker 容器中

### 遇到问题
1. 查看脚本输出日志
2. 检查端口是否被占用
3. 确认 Docker 容器运行正常
4. 查看常见问题文档

## 📚 相关文档

- **START_HERE.md** - 快速开始指南
- **LOCAL_SETUP.md** - 详细配置说明
- **COMMANDS.md** - 完整命令速查表
- **README.md** - 项目总览

---

**一切准备就绪！现在可以用命令启动了！** 🎊
