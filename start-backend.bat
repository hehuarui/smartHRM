@echo off
chcp 65001 >nul
echo ========================================
echo 启动 SmartHRM 后端服务
echo ========================================
echo.

REM 检查Java是否安装
java -version >nul 2>&1
if errorlevel 1 (
    echo [错误] 未检测到Java，请先安装Java 17
    pause
    exit /b 1
)

echo [信息] 检测到Java环境
java -version

echo.
echo [信息] 正在启动Spring Boot应用...
echo [提示] 后端服务将在 http://localhost:8080 启动
echo [提示] 按 Ctrl+C 停止服务
echo.

REM 使用Maven启动
if exist "mvnw.cmd" (
    echo [信息] 使用Maven Wrapper启动...
    call mvnw.cmd spring-boot:run
) else (
    echo [信息] 使用Maven启动...
    mvn spring-boot:run
    if errorlevel 1 (
        echo.
        echo [错误] Maven未安装或不在PATH中
        echo [提示] 请使用IDE（如IntelliJ IDEA）直接运行 SmartHrmApplication.java
        echo [提示] 或安装Maven后重试
        pause
        exit /b 1
    )
)

pause


