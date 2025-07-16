#!/bin/bash

# 打印彩色日志
function printInfo() {
  echo -e "\033[0;32m$1\033[0m"
}

function printWarning() {
  echo -e "\033[0;33m$1\033[0m"
}

function printError() {
  echo -e "\033[0;31m$1\033[0m"
}

# 检查命令是否存在
function checkPrereqs() {
  ## 检查是否安装了所需的工具
  if [ ! -d "bin" ]; then
    printError "bin目录不存在，请先运行download-fabric.sh下载Fabric二进制文件"
    exit 1
  fi
  
  which docker > /dev/null
  if [ $? -ne 0 ]; then
    printError "找不到docker命令，请安装Docker"
    exit 1
  fi
  
  which docker-compose > /dev/null
  if [ $? -ne 0 ]; then
    printError "找不到docker-compose命令，请安装Docker Compose"
    exit 1
  fi
  
  printInfo "所有前置条件已满足"
}

# 等待指定秒数
function sleep_seconds() {
  local seconds=$1
  printInfo "等待 ${seconds} 秒..."
  sleep $seconds
}

# 等待指定端口可用
function waitPort() {
  local server=$1
  local port=$2
  local timeout=$3
  
  local start_time=$(date +%s)
  local end_time=$((start_time + timeout))
  
  while true; do
    nc -z $server $port > /dev/null 2>&1
    if [ $? -eq 0 ]; then
      printInfo "${server}:${port} 已准备就绪"
      break
    fi
    
    local current_time=$(date +%s)
    if [ $current_time -gt $end_time ]; then
      printError "等待 ${server}:${port} 超时"
      return 1
    fi
    
    sleep 1
  done
  
  return 0
}

# 检查容器是否运行
function checkContainerRunning() {
  local container_name=$1
  
  if [ "$(docker ps -q -f name=$container_name)" ]; then
    return 0
  else
    return 1
  fi
}

# 执行命令并重试
function executeWithRetry() {
  local cmd=$1
  local retry_count=$2
  local retry_delay=$3
  
  local i=0
  while [ $i -lt $retry_count ]; do
    $cmd
    if [ $? -eq 0 ]; then
      return 0
    fi
    
    i=$((i+1))
    if [ $i -lt $retry_count ]; then
      printWarning "命令执行失败，${retry_delay}秒后重试 ($i/$retry_count)..."
      sleep $retry_delay
    fi
  done
  
  printError "命令执行失败，已达到最大重试次数"
  return 1
}

# 检查是否已经初始化
checkPrereqs 