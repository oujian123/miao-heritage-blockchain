#!/bin/bash

# 导入通用函数
. scripts/utils.sh

# 设置环境变量
export PATH=${PWD}/bin:$PATH
export FABRIC_CFG_PATH=${PWD}/config

CHANNEL_NAME=$1
DELAY=3
MAX_RETRY=5
VERBOSE=false

# 导入环境变量
. scripts/envVar.sh

createChannelTx() {
  printInfo "生成通道交易文件 ${CHANNEL_NAME}.tx"
  
  set -x
  configtxgen -profile TwoOrgsChannel -outputCreateChannelTx ./channel-artifacts/${CHANNEL_NAME}.tx -channelID $CHANNEL_NAME
  res=$?
  set +x
  
  if [ $res -ne 0 ]; then
    printError "生成通道交易文件失败"
    exit 1
  fi
}

createAnchorPeerTx() {
  printInfo "生成锚节点交易文件"
  
  for orgmsp in Org1MSP Org2MSP; do
    set -x
    configtxgen -profile TwoOrgsChannel -outputAnchorPeersUpdate ./channel-artifacts/${orgmsp}anchors.tx -channelID $CHANNEL_NAME -asOrg $orgmsp
    res=$?
    set +x
    
    if [ $res -ne 0 ]; then
      printError "生成${orgmsp}锚节点交易文件失败"
      exit 1
    fi
  done
}

createChannel() {
  # 创建channel-artifacts目录
  mkdir -p channel-artifacts
  
  # 生成通道交易文件
  createChannelTx
  
  # 生成锚节点交易文件
  createAnchorPeerTx
  
  printInfo "创建通道 ${CHANNEL_NAME}"
  
  # 使用org1创建通道
  setGlobals 1
  
  set -x
  peer channel create -o localhost:7050 -c $CHANNEL_NAME --ordererTLSHostnameOverride orderer.example.com -f ./channel-artifacts/${CHANNEL_NAME}.tx --outputBlock ./channel-artifacts/${CHANNEL_NAME}.block --tls --cafile $ORDERER_CA
  res=$?
  set +x
  
  if [ $res -ne 0 ]; then
    printError "创建通道失败"
    exit 1
  fi
  
  # 让org1加入通道
  printInfo "让Org1加入通道 ${CHANNEL_NAME}"
  setGlobals 1
  
  set -x
  peer channel join -b ./channel-artifacts/${CHANNEL_NAME}.block
  res=$?
  set +x
  
  if [ $res -ne 0 ]; then
    printError "Org1加入通道失败"
    exit 1
  fi
  
  # 让org2加入通道
  printInfo "让Org2加入通道 ${CHANNEL_NAME}"
  setGlobals 2
  
  set -x
  peer channel join -b ./channel-artifacts/${CHANNEL_NAME}.block
  res=$?
  set +x
  
  if [ $res -ne 0 ]; then
    printError "Org2加入通道失败"
    exit 1
  fi
  
  # 更新org1的锚节点
  printInfo "更新Org1的锚节点"
  setGlobals 1
  
  set -x
  peer channel update -o localhost:7050 --ordererTLSHostnameOverride orderer.example.com -c $CHANNEL_NAME -f ./channel-artifacts/Org1MSPanchors.tx --tls --cafile $ORDERER_CA
  res=$?
  set +x
  
  if [ $res -ne 0 ]; then
    printError "更新Org1锚节点失败"
    exit 1
  fi
  
  # 更新org2的锚节点
  printInfo "更新Org2的锚节点"
  setGlobals 2
  
  set -x
  peer channel update -o localhost:7050 --ordererTLSHostnameOverride orderer.example.com -c $CHANNEL_NAME -f ./channel-artifacts/Org2MSPanchors.tx --tls --cafile $ORDERER_CA
  res=$?
  set +x
  
  if [ $res -ne 0 ]; then
    printError "更新Org2锚节点失败"
    exit 1
  fi
  
  printInfo "通道 ${CHANNEL_NAME} 创建成功"
}

# 主函数
createChannel 