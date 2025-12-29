#!/bin/bash

echo "============================================"
echo "   码客邦 - 开发环境启动脚本"
echo "============================================"
echo

cd "$(dirname "$0")/.."

echo "[1/3] 启动数据库和缓存服务..."
docker-compose -f docker-compose.dev.yml up -d

echo
echo "[2/3] 等待服务就绪..."
sleep 10

echo
echo "[3/3] 检查服务状态..."
docker-compose -f docker-compose.dev.yml ps

echo
echo "============================================"
echo "服务已启动!"
echo "============================================"
echo
echo "PostgreSQL: localhost:5432"
echo "  - 数据库: makebang"
echo "  - 用户名: makebang"
echo "  - 密码: makebang123"
echo
echo "Redis: localhost:6379"
echo "  - 密码: makebang123"
echo
echo "管理工具:"
echo "  - Adminer (数据库): http://localhost:8081"
echo "  - Redis Commander: http://localhost:8082"
echo
echo "启动后端: cd makebang-backend && mvn spring-boot:run"
echo "启动前端: cd makebang-frontend && npm run dev"
echo
