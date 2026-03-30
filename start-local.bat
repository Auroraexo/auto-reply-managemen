@echo off
echo ========================================
echo 微信公众号自动回复管理系统 - 本地启动
echo ========================================
echo.

echo [1/4] 检查 MySQL 是否运行...
docker ps | findstr mysql
if errorlevel 1 (
    echo MySQL 未运行，正在启动 MySQL...
    docker run -d --name wechat-mysql ^
        -e MYSQL_ROOT_PASSWORD=123456 ^
        -e MYSQL_DATABASE=wechat_auto_reply ^
        -p 3306:3306 ^
        mysql:8.0
    timeout /t 10 /nobreak >nul
) else (
    echo MySQL 已在运行
)

echo.
echo [2/4] 检查 Redis 是否运行...
docker ps | findstr redis
if errorlevel 1 (
    echo Redis 未运行，正在启动 Redis...
    docker run -d --name wechat-redis ^
        -p 6379:6379 ^
        redis:7-alpine ^
        redis-server --appendonly yes
    timeout /t 3 /nobreak >nul
) else (
    echo Redis 已在运行
)

echo.
echo [3/4] 初始化数据库...
docker exec wechat-mysql mysql -u root -p123456 wechat_auto_reply -e "SHOW TABLES;" >nul 2>&1
if errorlevel 1 (
    echo 正在导入数据库脚本...
    docker exec -i wechat-mysql mysql -u root -p123456 wechat_auto_reply < backend\src\main\resources\db\schema.sql
    echo 数据库初始化完成！
) else (
    echo 数据库已初始化
)

echo.
echo [4/4] 启动后端服务...
cd backend
echo 正在编译项目（首次运行可能需要几分钟）...
call mvn clean install -DskipTests
echo.
echo ========================================
echo 启动 Spring Boot 应用...
echo API 文档地址：http://localhost:8080/api/doc.html
echo ========================================
echo.
call mvn spring-boot:run

pause
