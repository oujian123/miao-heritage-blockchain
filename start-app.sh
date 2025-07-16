#!/bin/bash

# 进入项目目录
cd miao-heritage

# 使用Maven构建项目
echo "构建项目..."
mvn clean package -DskipTests

# 启动应用
echo "启动应用..."
java -jar target/miao-heritage-0.0.1-SNAPSHOT.jar

echo "应用已启动，请访问 http://localhost:8080/api" 