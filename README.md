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

2. **电商平台**
   - 用户管理：匠人、平台、消费者多角色账户体系
   - 商品展示：链下存储详细商品信息和高清图片
   - 交易支付：支持在线支付，并自动触发区块链资产转移

3. **AI鉴别功能**
   - 银饰/服饰真伪鉴别：基于计算机视觉的特征识别
   - 文化背景解读：结合大语言模型提供文化知识讲解

## 技术选型

- **后端框架**: Spring Boot 3.x (Java 17+)
- **区块链**: 2
- **智能合约**: Go语言
- **数据库**: MySQL 8.x
- **前端框架**: Vue 3 + Element Plus
- **AI**: DeepSeek API

## 快速开始

### 环境要求

- JDK 17+
- Maven 3.8+
- Node.js 16+
- Docker & Docker Compose
- MySQL 8.x

### 启动步骤

1. 克隆项目
```bash
git clone https://github.com/your-username/miao-heritage.git
cd miao-heritage
```

2. 一键启动（区块链网络 + 后端 + 前端）
```bash
chmod +x start-all.sh
./start-all.sh
```

3. 分步启动

启动区块链网络：
```bash
cd fabric-network
./start-network.sh
```

启动后端服务：
```bash
cd miao-heritage
mvn spring-boot:run
```

启动前端服务：
```bash
cd miao-heritage-frontend
npm install
npm run serve
```

4. 访问应用
- 前端界面: http://localhost:8080
- API文档: http://localhost:8080/api/swagger-ui.html

## 项目结构

```
miao-heritage/
├── chaincode/                  # 区块链智能合约
│   └── miaoasset/             # 资产管理合约
├── fabric-network/            # Fabric网络配置
├── miao-heritage/             # 后端项目
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   │   └── com/miaoheritage/
│   │   │   │       ├── api/              # REST API层
│   │   │   │       ├── blockchain/       # 区块链接口层
│   │   │   │       ├── config/          # 配置类
│   │   │   │       ├── entity/          # 实体类
│   │   │   │       ├── repository/      # 数据访问层
│   │   │   │       ├── security/        # 安全配置
│   │   │   │       └── service/         # 业务逻辑层
│   │   │   └── resources/               # 配置文件
│   │   └── test/                        # 测试代码
└── miao-heritage-frontend/    # 前端项目
    ├── public/
    └── src/
        ├── assets/            # 静态资源
        ├── components/        # 组件
        ├── router/            # 路由配置
        ├── store/             # 状态管理
        └── views/             # 页面
```

## 许可证

MIT 