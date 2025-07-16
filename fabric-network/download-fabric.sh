#!/bin/bash

# 设置Fabric版本
export FABRIC_VERSION=2.2.0
export CA_VERSION=1.4.9

# 创建bin和config目录
mkdir -p bin config

# 下载Fabric二进制文件和配置文件
curl -sSL https://bit.ly/2ysbOFE | bash -s -- ${FABRIC_VERSION} ${CA_VERSION} -d -s

echo "Hyperledger Fabric二进制文件和Docker镜像已下载完成" 