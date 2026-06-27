@echo off
setlocal EnableDelayedExpansion EnableExtensions
chcp 65001 >nul 2>&1

rem ===== 基本路径 =====
set "PROJECT_DIR=%~dp0"
set "APP_NAME=library"
set "FRONTEND_DIR=%PROJECT_DIR%frontend"
set "WAR_FILE=%PROJECT_DIR%target\%APP_NAME%.war"
set "TOMCAT_ZIP=apache-tomcat-9.0.102-windows-x64.zip"
set "TOMCAT_URL=https://dlcdn.apache.org/tomcat/tomcat-9/v9.0.102/bin/%TOMCAT_ZIP%"
set "TOMCAT_LOCAL=%PROJECT_DIR%tomcat"
set "CONFIG_FILE=%PROJECT_DIR%tomcat-path.txt"

title 图书馆管理系统 - 自动部署工具
echo.
echo  ╔══════════════════════════════════════════╗
echo  ║      图书馆管理系统  自动部署工具        ║
echo  ╚══════════════════════════════════════════╝
echo.

rem =====================================================
rem  第一步：环境检查
rem =====================================================
echo  [1/6] 检查运行环境...
echo.

rem --- Java ---
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo  [错误] 未找到 Java，请先安装 JDK 17
    echo         下载地址：https://adoptium.net/
    goto :fail
)

for /f "tokens=3" %%v in ('java -version 2^>^&1 ^| findstr /i "version"') do set "JAVA_VER=%%~v"
echo         Java:    %JAVA_VER%

rem --- Maven ---
mvn -version >nul 2>&1
if %errorlevel% neq 0 (
    echo  [错误] 未找到 Maven，请先安装 Maven 3.6+
    echo         下载地址：https://maven.apache.org/download.cgi
    goto :fail
)

for /f "tokens=*" %%v in ('mvn -version 2^>^&1 ^| findstr /i "Apache Maven"') do set "MVN_VER=%%v"
echo         Maven:   %MVN_VER%

rem --- Node.js / npm ---
npm -v >nul 2>&1
if %errorlevel% neq 0 (
    echo  [错误] 未找到 Node.js / npm，请先安装 Node.js 16+
    echo         下载地址：https://nodejs.org/
    goto :fail
)

for /f %%v in ('npm -v 2^>^&1') do set "NPM_VER=%%v"
echo         npm:     v%NPM_VER%

echo.
echo  [环境检查通过]
echo.

rem =====================================================
rem  第二步：Tomcat 配置
rem =====================================================
echo  [2/6] 配置 Tomcat...
echo.

set "CATALINA_HOME="

rem 读取上次保存的路径
if exist "%CONFIG_FILE%" (
    set /p SAVED_TOMCAT=<"%CONFIG_FILE%"
    if exist "!SAVED_TOMCAT!\bin\startup.bat" (
        echo  检测到上次使用的 Tomcat：!SAVED_TOMCAT!
        echo.
        set /p "USE_SAVED=  是否继续使用？(Y/N，默认Y): "
        if /i "!USE_SAVED!" neq "N" (
            set "CATALINA_HOME=!SAVED_TOMCAT!"
            goto :tomcat_ready
        )
    )
)

rem 搜索常见路径
echo  正在搜索已安装的 Tomcat...
set "FOUND_TOMCAT="
for %%P in (
    "%PROJECT_DIR%tomcat"
    "C:\Program Files\Apache Software Foundation\Tomcat 9.0"
    "E:\Apache Software Foundation\Tomcat 9.0"
    "D:\apache-tomcat-9.0"
    "C:\apache-tomcat-9.0"
) do (
    if exist "%%~P\bin\startup.bat" (
        set "FOUND_TOMCAT=%%~P"
        goto :found_tomcat
    )
)

:found_tomcat
if defined FOUND_TOMCAT (
    echo  找到已安装的 Tomcat：!FOUND_TOMCAT!
    echo.
    set /p "USE_FOUND=  是否使用此 Tomcat？(Y/N，默认Y): "
    if /i "!USE_FOUND!" neq "N" (
        set "CATALINA_HOME=!FOUND_TOMCAT!"
        goto :tomcat_ready
    )
)

:ask_tomcat
echo.
echo  ┌─────────────────────────────────────────┐
echo  │  Tomcat 安装选项：                      │
echo  │    1. 自动下载 Tomcat 9（推荐，约12MB）  │
echo  │    2. 手动输入已有 Tomcat 路径           │
echo  └─────────────────────────────────────────┘
echo.
set /p "TOMCAT_CHOICE=  请选择 [1/2，默认1]: "
if "!TOMCAT_CHOICE!"=="" set "TOMCAT_CHOICE=1"

if "!TOMCAT_CHOICE!"=="2" goto :input_tomcat
if "!TOMCAT_CHOICE!" neq "1" (
    echo  无效选择，请重新输入
    goto :ask_tomcat
)

rem --- 自动下载 Tomcat ---
:download_tomcat
echo.
echo  正在下载 Tomcat 9.0.102...
echo  下载地址：%TOMCAT_URL%
echo.

set "ZIP_PATH=%PROJECT_DIR%%TOMCAT_ZIP%"

rem 优先使用 curl（Win10+ 自带）
curl --version >nul 2>&1
if %errorlevel% equ 0 (
    curl -L -# -o "%ZIP_PATH%" "%TOMCAT_URL%"
) else (
    echo  [使用 PowerShell 下载]
    powershell -Command "[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; Invoke-WebRequest -Uri '%TOMCAT_URL%' -OutFile '%ZIP_PATH%' -UseBasicParsing"
)

if not exist "%ZIP_PATH%" (
    echo  [错误] 下载失败，请检查网络或手动下载：
    echo         %TOMCAT_URL%
    goto :fail
)

echo.
echo  正在解压到 %TOMCAT_LOCAL% ...

rem 使用 PowerShell 解压
powershell -Command "Expand-Archive -Path '%ZIP_PATH%' -DestinationPath '%PROJECT_DIR%' -Force"

rem 移动解压出来的目录到 tomcat/
if exist "%PROJECT_DIR%apache-tomcat-9.0.102" (
    if exist "%TOMCAT_LOCAL%" rd /s /q "%TOMCAT_LOCAL%"
    ren "%PROJECT_DIR%apache-tomcat-9.0.102" "tomcat"
)

rem 清理 zip
del /f /q "%ZIP_PATH%" 2>nul

if not exist "%TOMCAT_LOCAL%\bin\startup.bat" (
    echo  [错误] 解压失败，请手动下载 Tomcat
    goto :fail
)

set "CATALINA_HOME=%TOMCAT_LOCAL%"
echo  [Tomcat 下载完成]
goto :tomcat_ready

rem --- 手动输入路径 ---
:input_tomcat
echo.
set /p "CATALINA_HOME=  请输入 Tomcat 安装路径: "
set "CATALINA_HOME=!CATALINA_HOME:"=!"

if not exist "!CATALINA_HOME!\bin\startup.bat" (
    echo  [错误] 无效的 Tomcat 路径，未找到 bin\startup.bat
    goto :input_tomcat
)

:tomcat_ready
rem 保存路径到配置文件
echo !CATALINA_HOME!>"%CONFIG_FILE%"
echo.
echo  使用 Tomcat：!CATALINA_HOME!
echo  webapps：    !CATALINA_HOME!\webapps
echo.

rem =====================================================
rem  第三步：构建前端
rem =====================================================
echo  [3/6] 构建前端...
echo.

pushd "%FRONTEND_DIR%"
if %errorlevel% neq 0 (
    echo  [错误] 无法进入前端目录：%FRONTEND_DIR%
    goto :fail
)

call npm run build
if %errorlevel% neq 0 (
    popd
    echo  [错误] 前端构建失败
    goto :fail
)
popd
echo  [前端构建完成]

rem =====================================================
rem  第四步：Maven 打包
rem =====================================================
echo.
echo  [4/6] Maven 打包...
echo.

pushd "%PROJECT_DIR%"
call mvn clean package -DskipTests
if %errorlevel% neq 0 (
    popd
    echo  [错误] Maven 打包失败
    goto :fail
)
popd

if not exist "%WAR_FILE%" (
    echo  [错误] 未找到 WAR 文件：%WAR_FILE%
    goto :fail
)
echo  [Maven 打包完成]

rem =====================================================
rem  第五步：停止旧 Tomcat 并部署
rem =====================================================
echo.
echo  [5/6] 部署到 Tomcat...
echo.

set "WEBAPPS=!CATALINA_HOME!\webapps"
set "APP_DIR=!WEBAPPS!\%APP_NAME%"
set "APP_WAR=!WEBAPPS!\%APP_NAME%.war"

rem 尝试关闭 Tomcat
tasklist /fi "imagename eq java.exe" 2>nul | findstr /i "java.exe" >nul
if %errorlevel% equ 0 (
    echo  正在停止 Tomcat...
    call "!CATALINA_HOME!\bin\shutdown.bat" >nul 2>&1
    timeout /t 3 /nobreak >nul
)

rem 删除旧的部署文件
if exist "!APP_DIR!" (
    echo  删除旧目录：!APP_DIR!
    rd /s /q "!APP_DIR!" 2>nul
)

if exist "!APP_WAR!" (
    echo  删除旧 WAR：!APP_WAR!
    del /f /q "!APP_WAR!" 2>nul
)

rem 复制新 WAR
echo  复制 WAR 到 webapps...
copy /y "%WAR_FILE%" "!APP_WAR!" >nul
if %errorlevel% neq 0 (
    echo  [错误] 复制 WAR 失败，请检查权限
    goto :fail
)
echo  [部署完成]

rem =====================================================
rem  第六步：启动 Tomcat
rem =====================================================
echo.
echo  [6/6] 启动 Tomcat...
echo.

call "!CATALINA_HOME!\bin\startup.bat"
if %errorlevel% neq 0 (
    echo  [错误] Tomcat 启动失败
    goto :fail
)

echo.
echo  ╔══════════════════════════════════════════╗
echo  ║  部署完成！                              ║
echo  ║                                          ║
echo  ║  访问地址：http://localhost:8080/library  ║
echo  ║  如无法访问，请等待 10 秒后刷新          ║
echo  ╚══════════════════════════════════════════╝
echo.
pause
exit /b 0

:fail
echo.
echo  ╔══════════════════════════════════════════╗
echo  ║  部署失败，请检查上述错误信息            ║
echo  ╚══════════════════════════════════════════╝
echo.
pause
exit /b 1
