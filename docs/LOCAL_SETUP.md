# 本地运行快速指南 🚀

## 环境要求

### 必需软件
- ✅ **JDK 17+** - [下载地址](https://adoptium.net/)
- ✅ **Maven 3.6+** - [下载地址](https://maven.apache.org/)
- ✅ **Node.js 18+** - [下载地址](https://nodejs.org/)
- ✅ **Docker** - [下载地址](https://www.docker.com/)

### 可选工具
- IntelliJ IDEA（Java 开发）
- VS Code（前端开发）
- Navicat（数据库管理）

## 方式一：一键启动脚本（推荐）

### 1. 启动后端
双击运行：
```
start-local.bat
```

这个脚本会自动：
- ✅ 启动 MySQL 容器（密码：123456）
- ✅ 启动 Redis 容器
- ✅ 初始化数据库
- ✅ 编译并启动 Spring Boot 应用

访问 API 文档：http://localhost:8080/api/doc.html

### 2. 启动前端（新窗口）
双击运行：
```
start-frontend.bat
```

访问前端界面：http://localhost:3000

## 方式二：手动启动

### 步骤 1：启动 MySQL 和 Redis

```bash
# 启动 MySQL
docker run -d --name wechat-mysql ^
    -e MYSQL_ROOT_PASSWORD=123456 ^
    -e MYSQL_DATABASE=wechat_auto_reply ^
    -p 3306:3306 ^
    mysql:8.0

# 等待 MySQL 启动
timeout /t 10

# 启动 Redis
docker run -d --name wechat-redis ^
    -p 6379:6379 ^
    redis:7-alpine
```

### 步骤 2：初始化数据库

```bash
# 导入表结构
docker exec -i wechat-mysql mysql -u root -p123456 wechat_auto_reply < backend\src\main\resources\db\schema.sql

# 验证（应该看到 7 张表）
docker exec -it wechat-mysql mysql -u root -p123456 wechat_auto_reply -e "SHOW TABLES;"
```

### 步骤 3：启动后端

打开终端 1：
```bash
cd backend

# 首次运行需要编译
mvn clean install -DskipTests

# 启动应用
mvn spring-boot:run
```

看到以下信息表示启动成功：
```
========================================
微信公众号自动回复管理系统启动成功！
API 文档地址：http://localhost:8080/api/doc.html
========================================
```

### 步骤 4：启动前端

打开终端 2：
```bash
cd frontend

# 首次运行需要安装依赖
npm install

# 启动开发服务器
npm run dev
```

看到以下信息表示启动成功：
```
  VITE v5.0.x  ready in xxx ms

  ➜  Local:   http://localhost:3000/
  ➜  Network: use --host to expose
```

## 方式三：使用 IDEA 启动

### 配置 IDEA

1. **导入项目**
   - File → Open → 选择 backend 文件夹
   - 等待 Maven 导入完成

2. **配置运行**
   - 找到 `AutoReplyApplication.java`
   - 右键 → Run 'AutoReplyApplication'

3. **配置前端**
   - File → Open → 选择 frontend 文件夹
   - 打开 Terminal，运行 `npm run dev`

## 验证安装

### 1. 检查服务状态

```bash
# 查看 Docker 容器
docker ps

# 应该看到：
# wechat-mysql
# wechat-redis
```

### 2. 测试后端接口

浏览器访问：
- http://localhost:8080/api/wechat (应该返回错误，因为需要参数)
- http://localhost:8080/api/doc.html (Swagger 文档)

### 3. 测试前端

浏览器访问：http://localhost:3000

默认登录账户：
- 用户名：admin
- 密码：admin123

## 常见问题

### ❌ MySQL 启动失败

**问题**: 端口 3306 被占用

**解决**:
```bash
# 停止占用端口的服务
netstat -ano | findstr :3306
taskkill /F /PID <进程 ID>

# 或者修改映射端口
docker run -d ... -p 3307:3306 ...
```

### ❌ Maven 编译失败

**问题**: 依赖下载失败

**解决**:
```bash
# 使用国内镜像
# 编辑 backend/pom.xml，添加阿里云镜像
<mirror>
    <id>aliyun</id>
    <mirrorOf>central</mirrorOf>
    <name>Aliyun Maven</name>
    <url>https://maven.aliyun.com/repository/public</url>
</mirror>
```

### ❌ npm install 失败

**问题**: 网络问题或 Node 版本不兼容

**解决**:
```bash
# 使用淘宝镜像
npm config set registry https://registry.npmmirror.com

# 清理缓存
npm cache clean --force

# 重新安装
npm install
```

### ❌ 数据库连接失败

**检查**:
```bash
# 测试 MySQL 连接
docker exec -it wechat-mysql mysql -u root -p123456

# 如果无法连接，重启容器
docker restart wechat-mysql
```

### ❌ 端口已被占用

**解决**:
```bash
# 查看端口占用
netstat -ano | findstr :8080
netstat -ano | findstr :3000

# 杀死进程
taskkill /F /PID <进程 ID>

# 或者修改配置
# 后端：编辑 application.yml 的 server.port
# 前端：编辑 vite.config.ts 的 server.port
```

## 数据库信息

```yaml
主机：localhost
端口：3306
数据库：wechat_auto_reply
用户名：root
密码：123456
```

### 查看数据

```bash
# 进入 MySQL
docker exec -it wechat-mysql mysql -u root -p123456

# 使用数据库
use wechat_auto_reply;

# 查看所有表
show tables;

# 查看管理员账户
select id, username, nickname from sys_user;

# 退出
exit;
```

## Redis 信息

```yaml
主机：localhost
端口：6379
数据库：0
密码：无
```

### 查看缓存

```bash
# 进入 Redis
docker exec -it wechat-redis redis-cli

# 查看所有键
keys *

# 查看 Access Token
get wechat:access_token

# 退出
exit
```

## 日志查看

### 后端日志
直接在启动控制台查看，或重定向到文件：
```bash
mvn spring-boot:run > backend.log 2>&1
```

### Docker 日志
```bash
# MySQL 日志
docker logs wechat-mysql

# Redis 日志
docker logs wechat-redis

# 实时查看
docker logs -f wechat-mysql
```

## 停止服务

### 停止 Docker 容器
```bash
docker stop wechat-mysql wechat-redis
```

### 删除 Docker 容器（可选）
```bash
docker rm wechat-mysql wechat-redis
```

### 停止应用
在运行的控制台按 `Ctrl + C`

## 清理数据（谨慎使用）

```bash
# 重置数据库（会删除所有数据）
docker exec -i wechat-mysql mysql -u root -p123456 -e "DROP DATABASE wechat_auto_reply; CREATE DATABASE wechat_auto_reply CHARACTER SET utf8mb4;"
docker exec -i wechat-mysql mysql -u root -p123456 wechat_auto_reply < schema.sql

# 完全清理
docker rm -f wechat-mysql wechat-redis
```

## 下一步

启动成功后，你可以：

1. 登录管理后台：http://localhost:3000
   - 用户名：admin
   - 密码：admin123

2. 配置微信公众号信息

3. 创建回复规则

4. 使用微信测试号进行测试

---

**提示**: 
- 首次运行需要下载依赖，可能需要几分钟
- 建议先运行后端，等后端启动完成后再运行前端
- 遇到问题请查看日志输出

祝你运行顺利！🎉
