# 苗族文化遗产区块链溯源电商平台

基于区块链技术的苗族银饰/服饰溯源电商平台，实现文化遗产的全生命周期追溯、防伪验证与价值传承。

## 项目架构

本项目采用Spring Boot + Hyperledger Fabric + Vue 3的技术栈，构建完整的溯源电商解决方案。

### 核心功能

1. **区块链溯源系统**
   - 资产上链：将苗族银饰/服饰的关键信息记录到区块链
   - 数字身份：为每件作品创建不可篡改的唯一身份
   - 二维码生成：基于资产ID生成溯源二维码
   - 所有权管理：记录资产从创建到销售的所有权变更

2. **电商平台** (后续开发)
   - 用户管理：匠人、平台、消费者多角色账户体系
   - 商品展示：链下存储详细商品信息和高清图片
   - 交易支付：支持在线支付，并自动触发区块链资产转移

3. **AI鉴别功能** (后续开发)
   - 银饰/服饰真伪鉴别：基于计算机视觉的特征识别
   - 文化背景解读：结合大语言模型提供文化知识讲解

## 技术选型

- **后端框架**: Spring Boot 3.x (Java 17+)
- **区块链**: Hyperledger Fabric 2.x
- **智能合约**: Go语言
- **数据库**: MySQL 8.x
- **前端框架**: Vue 3 (计划中)
- **AI**: TensorFlow/PyTorch + DeepSeek API (计划中)

## 项目结构

```
miao-heritage/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── miaoheritage/
│   │   │           ├── api/              # REST API层
│   │   │           │   ├── controller/   # 控制器
│   │   │           │   └── dto/          # 数据传输对象
│   │   │           ├── blockchain/       # 区块链接口层
│   │   │           │   ├── config/       # 区块链配置
│   │   │           │   ├── model/        # 区块链数据模型
│   │   │           │   └── service/      # 区块链服务
│   │   │           ├── service/          # 业务服务层
│   │   │           └── MiaoHeritageApplication.java
│   │   └── resources/
│   │       ├── application.yml           # 应用配置
│   │       └── hyperledger-fabric-network.json  # Fabric网络配置
│   └── test/
└── chaincode/
    └── miaoasset/
        └── go/                           # Go语言智能合约
```

## 快速开始

### 前提条件

- Java 17+
- Docker & Docker Compose
- Hyperledger Fabric 2.x
- Maven 3.6+

### 编译与运行

1. 编译项目
   ```bash
   mvn clean package
   ```

2. 启动Hyperledger Fabric网络 (需要提前准备好Fabric环境)
   ```bash
   cd fabric-network
   ./startFabric.sh
   ```

3. 部署智能合约
   ```bash
   cd fabric-network
   ./deployChaincode.sh miaoasset
   ```

4. 运行Spring Boot应用
   ```bash
   java -jar target/miao-heritage-platform-0.1.0-SNAPSHOT.jar
   ```

## 开发路线图

- [x] 第一阶段: 区块链溯源核心系统 (当前)
- [ ] 第二阶段: 完整电商平台
- [ ] 第三阶段: AI鉴别功能集成

## 贡献指南

欢迎通过Pull Request方式贡献代码。请确保代码风格一致并通过单元测试。

## 许可证

Apache License 2.0 