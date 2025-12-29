@echo off
chcp 65001 >nul
echo ============================================
echo    码客邦 - 停止开发环境
echo ============================================
echo.

cd /d %~dp0\..

echo 停止所有服务...
docker-compose -f docker-compose.dev.yml down

echo.
echo 服务已停止!
echo.
pause
