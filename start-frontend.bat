@echo off
echo ========================================
echo 启动前端开发服务器
echo ========================================
echo.

cd frontend

echo 检查 node_modules...
if not exist "node_modules" (
    echo 正在安装依赖（首次运行可能需要几分钟）...
    call npm install
) else (
    echo 依赖已安装
)

echo.
echo ========================================
echo 启动 Vite 开发服务器...
echo 前端地址：http://localhost:3000
echo ========================================
echo.

call npm run dev

pause
