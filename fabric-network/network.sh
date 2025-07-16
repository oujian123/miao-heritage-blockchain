#!/bin/bash

# 导入通用函数
. scripts/utils.sh

# 设置环境变量
export PATH=${PWD}/bin:$PATH
export FABRIC_CFG_PATH=${PWD}/config

# 默认参数
CHANNEL_NAME="miaoassetchannel"
CC_NAME="miaoasset"
CC_SRC_PATH="../chaincode/miaoasset/go"
CC_VERSION="1.0"
CC_SEQUENCE="1"
CC_INIT_FCN="InitLedger"
CC_END_POLICY="AND('Org1MSP.peer','Org2MSP.peer')"
CC_COLL_CONFIG=""
COMPOSE_FILE_BASE=docker-compose.yaml
COMPOSE_FILE_COUCH=${COMPOSE_FILE_BASE}
COMPOSE_FILE_CA=${COMPOSE_FILE_BASE}
CRYPTO="crypto-config"
MAX_RETRY=5
CLI_DELAY=3
CHANNEL_NAME="miaoassetchannel"

# 解析命令行参数
function parseArgs() {
  while [[ $# -ge 1 ]] ; do
    key="$1"
    case $key in
    up )
      UP_DOWN="up"
      shift
      ;;
    down )
      UP_DOWN="down"
      shift
      ;;
    createChannel )
      CREATE_CHANNEL="createChannel"
      shift
      ;;
    deployCC )
      DEPLOY_CC="deployCC"
      shift
      ;;
    -c )
      shift
      CHANNEL_NAME="$1"
      shift
      ;;
    -ca )
      CRYPTO="Certificate Authorities"
      CA_CRYPTO="true"
      shift
      ;;
    -s )
      DATABASE="couchdb"
      shift
      ;;
    -ccn )
      shift
      CC_NAME="$1"
      shift
      ;;
    -ccp )
      shift
      CC_SRC_PATH="$1"
      shift
      ;;
    -ccl )
      shift
      CC_LANGUAGE="$1"
      shift
      ;;
    -ccv )
      shift
      CC_VERSION="$1"
      shift
      ;;
    -ccs )
      shift
      CC_SEQUENCE="$1"
      shift
      ;;
    * )
      echo "未知参数 $1"
      exit 1
      ;;
    esac
  done
}

# 启动网络
function networkUp() {
  if [ ! -d "organizations/peerOrganizations" ]; then
    echo "生成证书和密钥..."
    if [ "$CA_CRYPTO" == "true" ]; then
      echo "使用证书授权机构..."
      docker-compose -f $COMPOSE_FILE_CA up -d 2>&1
      sleep 2
      scripts/registerEnroll.sh
    else
      echo "使用cryptogen生成证书和密钥..."
      mkdir -p organizations/peerOrganizations organizations/ordererOrganizations
      cryptogen generate --config=./organizations/cryptogen/crypto-config-org1.yaml --output="organizations"
      cryptogen generate --config=./organizations/cryptogen/crypto-config-org2.yaml --output="organizations"
      cryptogen generate --config=./organizations/cryptogen/crypto-config-orderer.yaml --output="organizations"
    fi
  fi

  echo "生成通道配置交易..."
  mkdir -p system-genesis-block
  configtxgen -profile TwoOrgsOrdererGenesis -channelID system-channel -outputBlock ./system-genesis-block/genesis.block
  
  echo "启动Fabric网络..."
  docker-compose -f ${COMPOSE_FILE_BASE} up -d
}

# 创建通道
function createChannel() {
  echo "创建通道 ${CHANNEL_NAME}..."
  scripts/createChannel.sh $CHANNEL_NAME
}

# 部署链码
function deployCC() {
  echo "部署链码 ${CC_NAME}..."
  scripts/deployCC.sh $CHANNEL_NAME $CC_NAME $CC_SRC_PATH $CC_VERSION $CC_SEQUENCE
}

# 关闭网络
function networkDown() {
  echo "关闭Fabric网络..."
  docker-compose -f ${COMPOSE_FILE_BASE} down --volumes --remove-orphans
  if [ -d "organizations/peerOrganizations" ]; then
    docker run --rm -v $(pwd):/data busybox sh -c 'cd /data && rm -rf system-genesis-block/*.block organizations/peerOrganizations organizations/ordererOrganizations'
    docker run --rm -v $(pwd):/data busybox sh -c 'cd /data && rm -rf channel-artifacts log.txt *.tar.gz'
  fi
}

# 主函数
parseArgs $@

if [ "${UP_DOWN}" == "up" ]; then
  networkUp
elif [ "${UP_DOWN}" == "down" ]; then
  networkDown
fi

if [ "${CREATE_CHANNEL}" == "createChannel" ]; then
  createChannel
fi

if [ "${DEPLOY_CC}" == "deployCC" ]; then
  deployCC
fi 