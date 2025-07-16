#!/bin/bash

# 设置颜色
GREEN='\033[0;32m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${GREEN}苗族文化遗产区块链溯源电商平台启动脚本${NC}"
echo "========================================"

# 检查Docker是否运行
if ! docker info > /dev/null 2>&1; then
  echo -e "${BLUE}[错误] Docker未运行，请先启动Docker${NC}"
  exit 1
fi

# 启动区块链网络
echo -e "${BLUE}[1/3] 启动Hyperledger Fabric网络...${NC}"
cd fabric-network && ./start-network.sh
cd ..

# 启动后端服务
echo -e "${BLUE}[2/3] 启动后端服务...${NC}"
./start-app.sh &
BACKEND_PID=$!

# 等待后端启动
echo "等待后端服务启动..."
sleep 10

# 启动前端服务
echo -e "${BLUE}[3/3] 启动前端服务...${NC}"
./start-frontend.sh &
FRONTEND_PID=$!

echo -e "${GREEN}所有服务已启动！${NC}"
echo "========================================"
echo "区块链网络: 运行在Docker中"
echo "后端服务: http://localhost:8080/api"
echo "前端界面: http://localhost:8080"
echo "========================================"
echo "按 Ctrl+C 停止所有服务"

# 等待用户中断
wait $BACKEND_PID $FRONTEND_PID 