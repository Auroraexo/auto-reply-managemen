#!/bin/bash

echo "========================================"
echo "微信公众号自动回复管理系统 - 本地启动"
echo "========================================"
echo ""

# 检查 Docker 是否运行
if ! docker ps > /dev/null 2>&1; then
    echo "❌ Docker 未运行，请先启动 Docker Desktop"
    exit 1
fi

echo "[1/4] 检查 MySQL 是否运行..."
if ! docker ps | grep -q wechat-mysql; then
    echo "MySQL 未运行，正在启动 MySQL..."
    docker run -d --name wechat-mysql \
        -e MYSQL_ROOT_PASSWORD=123456 \
        -e MYSQL_DATABASE=wechat_auto_reply \
        -p 3306:3306 \
        mysql:8.0
    sleep 10
else
    echo "✅ MySQL 已在运行"
fi

echo ""
echo "[2/4] 检查 Redis 是否运行..."
if ! docker ps | grep -q wechat-redis; then
    echo "Redis 未运行，正在启动 Redis..."
    docker run -d --name wechat-redis \
        -p 6379:6379 \
        redis:7-alpine \
        redis-server --appendonly yes
    sleep 3
else
    echo "✅ Redis 已在运行"
fi

echo ""
echo "[3/4] 初始化数据库..."
if ! docker exec wechat-mysql mysql -u root -p123456 wechat_auto_reply -e "SHOW TABLES;" > /dev/null 2>&1; then
    echo "正在导入数据库脚本..."
    docker exec -i wechat-mysql mysql -u root -p123456 wechat_auto_reply < backend/src/main/resources/db/schema.sql
    echo "✅ 数据库初始化完成！"
else
    echo "✅ 数据库已初始化"
fi

echo ""
echo "[4/4] 启动后端服务..."
cd backend
echo "正在编译项目（首次运行可能需要几分钟）..."
mvn clean install -DskipTests

echo ""
echo "========================================"
echo "启动 Spring Boot 应用..."
echo "API 文档地址：http://localhost:8080/api/doc.html"
echo "========================================"
echo ""

mvn spring-boot:run
