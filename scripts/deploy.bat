@echo off
chcp 65001 >nul
echo ============================================
echo    码客邦 - 生产环境部署脚本
echo ============================================
echo.

cd /d %~dp0\..

echo [1/4] 拉取最新代码...
git pull origin main

echo.
echo [2/4] 构建Docker镜像...
docker-compose build --no-cache

echo.
echo [3/4] 启动所有服务...
docker-compose up -d

echo.
echo [4/4] 检查服务状态...
timeout /t 30 /nobreak >nul
docker-compose ps

echo.
echo ============================================
echo 部署完成!
echo ============================================
echo.
echo 前端: http://localhost
echo 后端API: http://localhost:8080/api
echo API文档: http://localhost:8080/api/doc.html
echo.
pause
