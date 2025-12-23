@echo off
chcp 65001 >nul
echo ========================================
echo 启动 SmartHRM 前端服务
echo ========================================
echo.

REM 检查Node.js是否安装
node --version >nul 2>&1
if errorlevel 1 (
    echo [错误] 未检测到Node.js，请先安装Node.js
    echo [下载] https://nodejs.org/
    pause
    exit /b 1
)

echo [信息] 检测到Node.js环境
node --version
npm --version

echo.
cd frontend

REM 检查node_modules是否存在
if not exist "node_modules" (
    echo [信息] 首次运行，正在安装依赖...
    call npm install
    if errorlevel 1 (
        echo [错误] 依赖安装失败
        pause
        exit /b 1
    )
)

echo.
echo [信息] 正在启动前端开发服务器...
echo [提示] 前端服务将在 http://localhost:3000 启动
echo [提示] 按 Ctrl+C 停止服务
echo.

call npm run dev

pause


