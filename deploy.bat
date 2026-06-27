@echo off
chcp 65001 >nul 2>&1

set CATALINA_HOME=E:\Apache Software Foundation\Tomcat 9.0

echo ===== 1. 构建前端 =====
cd /d "%~dp0frontend"
call npm run build
if %errorlevel% neq 0 (
    echo 前端构建失败！
    pause
    exit /b 1
)

echo ===== 2. Maven 打包 =====
cd /d "%~dp0"
call mvn clean package -DskipTests
if %errorlevel% neq 0 (
    echo Maven 打包失败！
    pause
    exit /b 1
)

echo ===== 3. 停止 Tomcat =====
call "%CATALINA_HOME%\bin\shutdown.bat"
timeout /t 3 /nobreak >nul

echo ===== 4. 替换 WAR =====
rd /s /q "%CATALINA_HOME%\webapps\library"
del /f /q "%CATALINA_HOME%\webapps\library.war"
copy /y "%~dp0target\library.war" "%CATALINA_HOME%\webapps\"

echo ===== 5. 启动 Tomcat =====
call "%CATALINA_HOME%\bin\startup.bat"

echo ===== 部署完成！请访问 http://localhost:8080/library =====
pause
