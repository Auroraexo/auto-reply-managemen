@echo off
chcp 65001 >nul
echo ========================================
echo 环境检查工具
echo ========================================
echo.

echo [1/6] 检查 Java 版本...
java -version >nul 2>&1
if errorlevel 1 (
    echo ❌ 未检测到 Java，请先安装 JDK 17+
) else (
    java -version 2>&1 | findstr "version"
    echo ✅ Java 已安装
)
echo.

echo [2/6] 检查 Maven 版本...
mvn -version >nul 2>&1
if errorlevel 1 (
    echo ❌ 未检测到 Maven，请先安装 Maven 3.6+
) else (
    mvn -version 2>&1 | findstr "version"
    echo ✅ Maven 已安装
)
echo.

echo [3/6] 检查 Node.js 版本...
node --version >nul 2>&1
if errorlevel 1 (
    echo ❌ 未检测到 Node.js，请先安装 Node.js 18+
) else (
    node --version
    echo ✅ Node.js 已安装
)
echo.

echo [4/6] 检查 npm 版本...
npm --version >nul 2>&1
if errorlevel 1 (
    echo ❌ 未检测到 npm
) else (
    npm --version
    echo ✅ npm 已安装
)
echo.

echo [5/6] 检查 Docker 是否运行...
docker ps >nul 2>&1
if errorlevel 1 (
    echo ❌ Docker 未运行或未安装，请先启动 Docker Desktop
) else (
    docker --version
    echo ✅ Docker 正在运行
)
echo.

echo [6/6] 检查端口占用情况...
echo 检查端口 3306 (MySQL)...
netstat -ano | findstr ":3306" >nul 2>&1
if errorlevel 1 (
    echo   端口 3306 可用
) else (
    echo   ⚠️  端口 3306 已被占用
)

echo 检查端口 6379 (Redis)...
netstat -ano | findstr ":6379" >nul 2>&1
if errorlevel 1 (
    echo   端口 6379 可用
) else (
    echo   ⚠️  端口 6379 已被占用
)

echo 检查端口 8080 (后端 API)...
netstat -ano | findstr ":8080" >nul 2>&1
if errorlevel 1 (
    echo   端口 8080 可用
) else (
    echo   ⚠️  端口 8080 已被占用
)

echo 检查端口 3000 (前端)...
netstat -ano | findstr ":3000" >nul 2>&1
if errorlevel 1 (
    echo   端口 3000 可用
) else (
    echo   ⚠️  端口 3000 已被占用
)

echo.
echo ========================================
echo 检查完成！
echo ========================================
echo.
echo 如果看到 ❌ 标记，请先安装相应软件
echo 如果看到 ⚠️  标记，请关闭占用端口的程序
echo.

pause
