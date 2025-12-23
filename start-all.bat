@echo off
chcp 65001 >nul
echo ========================================
echo 启动 SmartHRM 完整服务
echo ========================================
echo.
echo [提示] 此脚本将启动后端和前端服务
echo [提示] 需要两个命令行窗口分别运行
echo.

REM 启动后端
start "SmartHRM Backend" cmd /k start-backend.bat

REM 等待3秒
timeout /t 3 /nobreak >nul

REM 启动前端
start "SmartHRM Frontend" cmd /k start-frontend.bat

echo.
echo [信息] 已启动后端和前端服务窗口
echo [提示] 后端: http://localhost:8080
echo [提示] 前端: http://localhost:3000
echo.
echo [重要] 请确保MongoDB服务已启动！
echo [提示] MongoDB默认运行在 localhost:27017
echo.

pause

