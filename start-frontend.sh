#!/bin/bash

# 进入前端项目目录
cd miao-heritage-frontend

# 安装依赖
echo "安装依赖..."
npm install

# 启动开发服务器
echo "启动前端开发服务器..."
npm run serve

echo "前端已启动，请访问 http://localhost:8080" 