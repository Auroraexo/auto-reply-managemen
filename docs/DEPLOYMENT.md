# 系统部署指南

## 一、生产环境部署

### 1.1 服务器要求

- **CPU**: 2 核及以上
- **内存**: 4GB 及以上
- **磁盘**: 50GB 及以上
- **操作系统**: Linux (CentOS 7+ / Ubuntu 18.04+)
- **域名**: 已备案域名（用于微信服务器配置）

### 1.2 软件要求

- Docker 20.10+
- Docker Compose 2.0+

## 二、部署步骤

### 2.1 安装 Docker

```bash
# CentOS
sudo yum install -y yum-utils
sudo yum-config-manager --add-repo https://download.docker.com/linux/centos/docker-ce.repo
sudo yum install docker-ce docker-ce-cli containerd.io
sudo systemctl start docker
sudo systemctl enable docker

# Ubuntu
sudo apt-get update
sudo apt-get install \
    ca-certificates \
    curl \
    gnupg \
    lsb-release
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /usr/share/keyrings/docker-archive-keyring.gpg
echo "deb [arch=$(dpkg --print-architecture) signed-by=/usr/share/keyrings/docker-archive-keyring.gpg] https://download.docker.com/linux/ubuntu $(lsb_release -cs) stable" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null
sudo apt-get update
sudo apt-get install docker-ce docker-ce-cli containerd.io
```

### 2.2 安装 Docker Compose

```bash
sudo curl -L "https://github.com/docker/compose/releases/download/v2.20.0/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose
docker-compose --version
```

### 2.3 部署项目

1. **上传项目到服务器**
```bash
# 方式 1: Git 克隆
git clone <项目地址>
cd auto-reply management

# 方式 2: 本地打包上传
# 在本地打包后通过 scp 上传
scp auto-reply-management.tar.gz root@your-server:/opt/
ssh root@your-server
cd /opt
tar -xzf auto-reply-management.tar.gz
cd auto-reply management
```

2. **配置环境变量**
创建 `.env` 文件：
```bash
cat > .env << EOF
WECHAT_APP_ID=wx1234567890abcdef
WECHAT_SECRET=your_secret_here
WECHAT_TOKEN=your_token_here
WECHAT_AES_KEY=your_aes_key_here
JWT_SECRET=your_jwt_secret_key_at_least_32_characters_long_for_security
EOF
```

3. **修改数据库密码**
编辑 `docker-compose.yml`，修改 MySQL 的 root 密码：
```yaml
environment:
  MYSQL_ROOT_PASSWORD: your_secure_password
```

4. **启动服务**
```bash
docker-compose up -d
```

5. **查看运行状态**
```bash
docker-compose ps
docker-compose logs -f
```

### 2.4 配置 Nginx SSL（可选但推荐）

1. **申请 SSL 证书**
   - 从 [Let's Encrypt](https://letsencrypt.org/) 免费申请
   - 或从云服务商购买

2. **配置 Nginx**
```nginx
server {
    listen 443 ssl http2;
    server_name your-domain.com;

    ssl_certificate /path/to/cert.pem;
    ssl_certificate_key /path/to/key.pem;

    location / {
        proxy_pass http://localhost:80;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    location /api/wechat {
        proxy_pass http://localhost:8080/api/wechat;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    }
}

server {
    listen 80;
    server_name your-domain.com;
    return 301 https://$server_name$request_uri;
}
```

3. **重启 Nginx**
```bash
sudo nginx -t
sudo systemctl reload nginx
```

### 2.5 配置微信公众号

1. 登录 [微信公众平台](https://mp.weixin.qq.com/)
2. 进入 开发 → 基本配置
3. 配置服务器：
   - **URL**: `https://your-domain.com/api/wechat`
   - **Token**: 与 `.env` 文件中配置一致
   - **EncodingAESKey**: 随机生成或自定义

4. 提交验证

## 三、日常运维

### 3.1 查看日志

```bash
# 查看所有服务日志
docker-compose logs -f

# 查看特定服务日志
docker-compose logs -f backend
docker-compose logs -f frontend
docker-compose logs -f mysql
docker-compose logs -f redis
```

### 3.2 重启服务

```bash
# 重启所有服务
docker-compose restart

# 重启单个服务
docker-compose restart backend
docker-compose restart frontend
```

### 3.3 停止服务

```bash
docker-compose down
```

### 3.4 数据备份

```bash
# 备份 MySQL 数据库
docker exec wechat-mysql mysqldump -u root -proot123456 wechat_auto_reply > backup_$(date +%Y%m%d_%H%M%S).sql

# 备份 Redis 数据
docker cp wechat-redis:/data/dump.rdb ./dump_$(date +%Y%m%d_%H%M%S).rdb
```

### 3.5 数据库恢复

```bash
# 恢复 MySQL 数据库
cat backup.sql | docker exec -i wechat-mysql mysql -u root -proot123456 wechat_auto_reply
```

### 3.6 扩容

```bash
# 增加后端实例数量
docker-compose up -d --scale backend=2

# 注意：多实例需要配置负载均衡和 Session 共享
```

## 四、监控告警

### 4.1 健康检查

```bash
# 检查服务状态
curl http://localhost:8080/api/actuator/health

# 检查前端
curl http://localhost/
```

### 4.2 资源监控

```bash
# 查看容器资源使用
docker stats

# 查看磁盘使用
df -h
du -sh ./data/*
```

### 4.3 日志收集（可选）

可以使用 ELK Stack 或 Loki 进行日志收集和展示。

## 五、常见问题排查

### 5.1 容器无法启动

```bash
# 查看详细错误
docker-compose logs backend

# 常见原因：
# 1. 端口被占用
# 2. 数据库连接失败
# 3. 环境变量配置错误
```

### 5.2 数据库连接失败

```bash
# 检查 MySQL 是否启动
docker-compose ps mysql

# 测试数据库连接
docker exec wechat-mysql mysql -u root -proot123456 -e "SHOW DATABASES;"
```

### 5.3 Redis 连接失败

```bash
# 检查 Redis 是否启动
docker-compose ps redis

# 测试 Redis 连接
docker exec wechat-redis redis-cli ping
```

### 5.4 微信服务器验证失败

检查项：
1. 域名是否可以公网访问
2. SSL 证书是否有效
3. Token 配置是否一致
4. 防火墙是否开放端口
5. Nginx 转发配置是否正确

```bash
# 测试外网访问
curl -I https://your-domain.com/api/wechat

# 检查防火墙
sudo firewall-cmd --list-all
```

## 六、性能优化

### 6.1 数据库优化

```sql
-- 添加索引
ALTER TABLE reply_rule ADD INDEX idx_type_mode (rule_type, match_mode);
ALTER TABLE message_record ADD INDEX idx_openid_time (open_id, create_time);

-- 定期清理历史数据
DELETE FROM message_record WHERE create_time < DATE_SUB(NOW(), INTERVAL 3 MONTH);
```

### 6.2 Redis 缓存优化

```yaml
# 配置 Redis 持久化
command: redis-server --appendonly yes --maxmemory 256mb --maxmemory-policy allkeys-lru
```

### 6.3 JVM 参数调优

编辑 `backend/Dockerfile`：
```dockerfile
ENTRYPOINT ["java", "-Xms512m", "-Xmx1g", "-jar", "app.jar"]
```

## 七、安全加固

### 7.1 修改默认密码

```sql
-- 修改数据库密码
ALTER USER 'root'@'localhost' IDENTIFIED BY 'new_secure_password';

-- 修改管理员密码（需要 BCrypt 加密）
UPDATE sys_user SET password = '$2a$10$new_hashed_password' WHERE username = 'admin';
```

### 7.2 防火墙配置

```bash
# 只开放必要端口
sudo firewall-cmd --permanent --add-service=ssh
sudo firewall-cmd --permanent --add-service=https
sudo firewall-cmd --reload
```

### 7.3 限制 API 访问频率

在 Nginx 中配置限流：
```nginx
limit_req_zone $binary_remote_addr zone=api_limit:10m rate=10r/s;

location /api/ {
    limit_req zone=api_limit burst=20 nodelay;
    proxy_pass http://localhost:8080;
}
```

## 八、版本更新

### 8.1 代码更新

```bash
# 拉取最新代码
git pull origin main

# 重新构建并启动
docker-compose build backend frontend
docker-compose up -d
```

### 8.2 数据库升级

如果有数据库变更：
```bash
# 执行升级脚本
docker exec -i wechat-mysql mysql -u root -proot123456 wechat_auto_reply < upgrade_v1.1.sql
```

## 九、回滚方案

### 9.1 应用回滚

```bash
# 切换到之前的版本
git checkout <previous-tag>

# 重新构建启动
docker-compose build
docker-compose up -d
```

### 9.2 数据库回滚

```bash
# 使用备份恢复
cat backup_20240101.sql | docker exec -i wechat-mysql mysql -u root -proot123456 wechat_auto_reply
```

## 十、联系支持

如遇到部署问题，请收集以下信息：

1. 服务器操作系统版本
2. Docker 和 Docker Compose 版本
3. 相关服务日志
4. 错误截图或错误信息

祝部署顺利！
