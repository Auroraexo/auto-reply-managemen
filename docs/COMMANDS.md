# 🚀 启动命令速查表

## Windows 系统

### 方式一：双击脚本（最简单）⭐

```bash
# 1. 检查环境
check-env.bat

# 2. 启动后端（等待成功提示）
start-local.bat

# 3. 新开窗口，启动前端
start-frontend.bat
```

### 方式二：命令行启动

#### PowerShell 或 CMD

**启动后端：**
```bash
cd E:\design\auto-reply management
.\start-local.bat
```

**启动前端（新窗口）：**
```bash
cd E:\design\auto-reply management
.\start-frontend.bat
```

---

## Linux / macOS 系统

### 终端命令

**启动后端：**
```bash
cd /path/to/auto-reply\ management
chmod +x start-local.sh
./start-local.sh
```

**启动前端（新终端窗口）：**
```bash
cd /path/to/auto-reply\ management
chmod +x start-frontend.sh
./start-frontend.sh
```

---

## 纯手动命令（不分系统）

### 1. 启动 MySQL 和 Redis

```bash
# 启动 MySQL
docker run -d --name wechat-mysql \
    -e MYSQL_ROOT_PASSWORD=123456 \
    -e MYSQL_DATABASE=wechat_auto_reply \
    -p 3306:3306 \
    mysql:8.0

# 等待启动
sleep 10

# 启动 Redis
docker run -d --name wechat-redis \
    -p 6379:6379 \
    redis:7-alpine \
    redis-server --appendonly yes
```

### 2. 初始化数据库

```bash
docker exec -i wechat-mysql mysql -u root -p123456 wechat_auto_reply < backend/src/main/resources/db/schema.sql
```

### 3. 启动后端（终端 1）

```bash
cd backend

# 首次编译
mvn clean install -DskipTests

# 启动应用
mvn spring-boot:run
```

### 4. 启动前端（终端 2）

```bash
cd frontend

# 首次安装依赖
npm install

# 启动开发服务器
npm run dev
```

---

## Docker Compose 启动（生产环境）

### 一键启动所有服务

```bash
# 配置环境变量
cp .env.example .env
# 编辑 .env 填入你的配置

# 启动所有服务
docker-compose up -d

# 查看日志
docker-compose logs -f

# 停止服务
docker-compose down
```

---

## 常用命令集合

### 环境检查

```bash
# Windows
check-env.bat

# Linux/Mac
./check-env.sh (需创建)
```

### 查看服务状态

```bash
# 查看 Docker 容器
docker ps

# 查看端口占用
netstat -ano | findstr :3306    # Windows
lsof -i :3306                   # Linux/Mac
```

### 查看日志

```bash
# MySQL 日志
docker logs wechat-mysql

# Redis 日志
docker logs wechat-redis

# 后端日志（在启动控制台查看）

# 前端日志（在启动控制台查看）
```

### 停止服务

```bash
# 停止 Docker 容器
docker stop wechat-mysql wechat-redis

# 停止后端（在运行界面按 Ctrl+C）

# 停止前端（在运行界面按 Ctrl+C）
```

### 重启服务

```bash
# 重启 Docker 容器
docker restart wechat-mysql wechat-redis

# 重启后端
cd backend
mvn spring-boot:run

# 重启前端
cd frontend
npm run dev
```

---

## 验证启动成功

### 后端成功标志

看到以下输出：
```
========================================
微信公众号自动回复管理系统启动成功！
API 文档地址：http://localhost:8080/api/doc.html
========================================
```

**测试：**
```bash
curl http://localhost:8080/api/doc.html
```

### 前端成功标志

看到以下输出：
```
VITE v5.0.x ready in xxx ms

➜  Local:   http://localhost:3000/
```

**测试：**
```bash
curl http://localhost:3000
```

### Docker 容器成功标志

```bash
docker ps
# 应该看到：
# wechat-mysql
# wechat-redis
```

---

## 快速故障排查

### 后端启动失败

```bash
# 1. 检查 Java 版本
java -version

# 2. 检查 Maven
mvn -version

# 3. 清理编译
cd backend
mvn clean

# 4. 重新编译
mvn install -DskipTests

# 5. 启动
mvn spring-boot:run
```

### 前端启动失败

```bash
# 1. 检查 Node 版本
node --version

# 2. 检查 npm
npm --version

# 3. 清理并重装
cd frontend
rm -rf node_modules package-lock.json
npm install

# 4. 启动
npm run dev
```

### Docker 容器无法启动

```bash
# 1. 查看错误日志
docker logs wechat-mysql
docker logs wechat-redis

# 2. 删除容器重建
docker rm -f wechat-mysql wechat-redis

# 3. 重新启动
docker run -d --name wechat-mysql ...
docker run -d --name wechat-redis ...
```

---

## 访问地址汇总

| 服务 | URL | 说明 |
|------|-----|------|
| 前端 | http://localhost:3000 | 管理后台 |
| API 文档 | http://localhost:8080/api/doc.html | Swagger UI |
| 后端 API | http://localhost:8080/api | RESTful 接口 |
| MySQL | localhost:3306 | 数据库 |
| Redis | localhost:6379 | 缓存 |

---

## 默认账户

```
用户名：admin
密码：admin123
⚠️ 首次登录后请修改密码！
```

---

## 提示

1. **首次运行**需要下载依赖，可能需要 5-10 分钟
2. **确保 Docker Desktop 已启动**（Windows/Mac）
3. **如果端口被占用**，先停止占用的程序
4. **建议先启动后端**，等后端成功后再启动前端
5. **遇到问题**查看日志输出

---

**祝你启动顺利！** 🎉
