#!/bin/bash

# 设置环境变量
export PATH=${PWD}/bin:$PATH
export FABRIC_CFG_PATH=${PWD}/config

# 检查是否已下载Fabric二进制文件
if [ ! -d "bin" ] || [ ! -d "config" ]; then
  echo "未检测到Fabric二进制文件，正在下载..."
  ./download-fabric.sh
fi

# 停止可能正在运行的网络
./network.sh down

# 启动网络，创建通道
./network.sh up

# 创建通道
./network.sh createChannel

# 部署链码
./network.sh deployCC -ccn miaoasset -ccp ../chaincode/miaoasset/go -ccl go -ccv 1.0 -ccs 1

echo "Hyperledger Fabric网络已启动，链码已部署" 