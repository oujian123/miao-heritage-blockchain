#!/bin/bash

# 导入通用函数
. scripts/utils.sh

# 设置环境变量
export PATH=${PWD}/bin:$PATH
export FABRIC_CFG_PATH=${PWD}/config

CHANNEL_NAME=$1
CC_NAME=$2
CC_SRC_PATH=$3
CC_VERSION=$4
CC_SEQUENCE=$5
CC_INIT_FCN="InitLedger"
CC_END_POLICY="AND('Org1MSP.peer','Org2MSP.peer')"
CC_COLL_CONFIG=""
DELAY=3
MAX_RETRY=5
VERBOSE=false

# 导入环境变量
. scripts/envVar.sh

packageChaincode() {
  printInfo "打包链码 ${CC_NAME}"
  
  set -x
  peer lifecycle chaincode package ${CC_NAME}.tar.gz --path ${CC_SRC_PATH} --lang golang --label ${CC_NAME}_${CC_VERSION}
  res=$?
  set +x
  
  if [ $res -ne 0 ]; then
    printError "链码打包失败"
    exit 1
  fi
}

installChaincode() {
  ORG=$1
  setGlobals $ORG
  
  printInfo "安装链码到Org${ORG}"
  
  set -x
  peer lifecycle chaincode install ${CC_NAME}.tar.gz
  res=$?
  set +x
  
  if [ $res -ne 0 ]; then
    printError "链码安装到Org${ORG}失败"
    exit 1
  fi
}

queryInstalled() {
  ORG=$1
  setGlobals $ORG
  
  printInfo "查询Org${ORG}已安装的链码"
  
  set -x
  peer lifecycle chaincode queryinstalled >&log.txt
  res=$?
  set +x
  
  if [ $res -ne 0 ]; then
    cat log.txt
    printError "查询已安装链码失败"
    exit 1
  fi
  
  PACKAGE_ID=$(sed -n "/${CC_NAME}_${CC_VERSION}/{s/^Package ID: //; s/, Label:.*$//; p;}" log.txt)
  
  printInfo "链码包ID: ${PACKAGE_ID}"
}

approveForMyOrg() {
  ORG=$1
  setGlobals $ORG
  
  printInfo "Org${ORG}批准链码定义"
  
  if [ -z "$PACKAGE_ID" ]; then
    printError "找不到链码包ID"
    exit 1
  fi
  
  set -x
  peer lifecycle chaincode approveformyorg -o localhost:7050 --ordererTLSHostnameOverride orderer.example.com --channelID $CHANNEL_NAME --name ${CC_NAME} --version ${CC_VERSION} --package-id ${PACKAGE_ID} --sequence ${CC_SEQUENCE} --tls --cafile $ORDERER_CA --init-required
  res=$?
  set +x
  
  if [ $res -ne 0 ]; then
    printError "Org${ORG}批准链码定义失败"
    exit 1
  fi
}

checkCommitReadiness() {
  ORG=$1
  setGlobals $ORG
  
  printInfo "检查Org${ORG}链码提交就绪状态"
  
  set -x
  peer lifecycle chaincode checkcommitreadiness --channelID $CHANNEL_NAME --name ${CC_NAME} --version ${CC_VERSION} --sequence ${CC_SEQUENCE} --tls --cafile $ORDERER_CA --init-required --output json
  res=$?
  set +x
  
  if [ $res -ne 0 ]; then
    printError "检查链码提交就绪状态失败"
    exit 1
  fi
}

commitChaincodeDefinition() {
  printInfo "提交链码定义到通道"
  
  parsePeerConnectionParameters 1 2
  
  set -x
  peer lifecycle chaincode commit -o localhost:7050 --ordererTLSHostnameOverride orderer.example.com --channelID $CHANNEL_NAME --name ${CC_NAME} --version ${CC_VERSION} --sequence ${CC_SEQUENCE} --tls --cafile $ORDERER_CA --peerAddresses localhost:7051 --tlsRootCertFiles ${PEER0_ORG1_CA} --peerAddresses localhost:9051 --tlsRootCertFiles ${PEER0_ORG2_CA} --init-required
  res=$?
  set +x
  
  if [ $res -ne 0 ]; then
    printError "提交链码定义失败"
    exit 1
  fi
}

queryCommitted() {
  ORG=$1
  setGlobals $ORG
  
  printInfo "查询Org${ORG}已提交的链码"
  
  set -x
  peer lifecycle chaincode querycommitted --channelID $CHANNEL_NAME --name ${CC_NAME}
  res=$?
  set +x
  
  if [ $res -ne 0 ]; then
    printError "查询已提交链码失败"
    exit 1
  fi
}

chaincodeInvokeInit() {
  parsePeerConnectionParameters 1 2
  
  printInfo "初始化链码"
  
  set -x
  peer chaincode invoke -o localhost:7050 --ordererTLSHostnameOverride orderer.example.com --channelID $CHANNEL_NAME --name ${CC_NAME} --tls --cafile $ORDERER_CA --peerAddresses localhost:7051 --tlsRootCertFiles ${PEER0_ORG1_CA} --peerAddresses localhost:9051 --tlsRootCertFiles ${PEER0_ORG2_CA} --isInit -c '{"function":"'${CC_INIT_FCN}'","Args":[]}'
  res=$?
  set +x
  
  if [ $res -ne 0 ]; then
    printError "链码初始化失败"
    exit 1
  fi
}

parsePeerConnectionParameters() {
  PEER_CONN_PARMS=""
  PEERS=""
  
  while [ "$#" -gt 0 ]; do
    setGlobals $1
    PEER="peer0.org$1"
    PEERS="$PEERS $PEER"
    PEER_CONN_PARMS="$PEER_CONN_PARMS --peerAddresses $CORE_PEER_ADDRESS"
    if [ $1 -eq 1 ]; then
      PEER0_ORG1_CA=${CORE_PEER_TLS_ROOTCERT_FILE}
    else
      PEER0_ORG2_CA=${CORE_PEER_TLS_ROOTCERT_FILE}
    fi
    TLSINFO=(--tlsRootCertFiles "${CORE_PEER_TLS_ROOTCERT_FILE}")
    PEER_CONN_PARMS="$PEER_CONN_PARMS ${TLSINFO}"
    shift
  done
}

# 主函数
packageChaincode
installChaincode 1
installChaincode 2
queryInstalled 1
approveForMyOrg 1
checkCommitReadiness 1
checkCommitReadiness 2
approveForMyOrg 2
checkCommitReadiness 1
checkCommitReadiness 2
commitChaincodeDefinition
queryCommitted 1
chaincodeInvokeInit

printInfo "链码 ${CC_NAME} 部署成功" 