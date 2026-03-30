#!/bin/bash

echo "========================================"
echo "启动前端开发服务器"
echo "========================================"
echo ""

cd frontend

# 检查 node_modules
if [ ! -d "node_modules" ]; then
    echo "正在安装依赖（首次运行可能需要几分钟）..."
    npm install
else
    echo "✅ 依赖已安装"
fi

echo ""
echo "========================================"
echo "启动 Vite 开发服务器..."
echo "前端地址：http://localhost:3000"
echo "========================================"
echo ""

npm run dev
