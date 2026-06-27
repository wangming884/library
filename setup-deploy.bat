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
set "TOOLS_DIR=%PROJECT_DIR%tools"
set "TOOLS_CONFIG=%PROJECT_DIR%tools-path.txt"

rem 下载地址（Adoptium JDK 17 / Maven 3.9 / Node.js 22 LTS）
set "JDK_ZIP=OpenJDK17U-jdk_x64_windows_hotspot_17.0.15_6.zip"
set "JDK_URL=https://github.com/adoptium/temurin17-binaries/releases/download/jdk-17.0.15%2B6/%JDK_ZIP%"
set "JDK_DIR_NAME=jdk-17.0.15+6"

set "MVN_ZIP=apache-maven-3.9.9-bin.zip"
set "MVN_URL=https://dlcdn.apache.org/maven/maven-3/3.9.9/binaries/%MVN_ZIP%"
set "MVN_DIR_NAME=apache-maven-3.9.9"

set "NODE_ZIP=node-v22.16.0-win-x64.zip"
set "NODE_URL=https://nodejs.org/dist/v22.16.0/%NODE_ZIP%"
set "NODE_DIR_NAME=node-v22.16.0-win-x64"

title 图书馆管理系统 - 自动部署工具
echo.
echo  ╔══════════════════════════════════════════╗
echo  ║      图书馆管理系统  自动部署工具        ║
echo  ╚══════════════════════════════════════════╝
echo.

rem =====================================================
rem  第一步：环境检查（缺失则提示自动下载）
rem =====================================================
echo  [1/6] 检查运行环境...
echo.

rem 尝试读取上次保存的工具路径
if exist "%TOOLS_CONFIG%" (
    for /f "usebackq tokens=1,* delims==" %%a in ("%TOOLS_CONFIG%") do (
        if "%%a"=="JAVA_HOME"  set "JAVA_HOME=%%b"
        if "%%a"=="MAVEN_HOME" set "MAVEN_HOME=%%b"
        if "%%a"=="NODE_HOME"  set "NODE_HOME=%%b"
    )
    if defined JAVA_HOME  if exist "!JAVA_HOME!\bin\java.exe"  set "PATH=!JAVA_HOME!\bin;!PATH!"
    if defined MAVEN_HOME if exist "!MAVEN_HOME!\bin\mvn.cmd"  set "PATH=!MAVEN_HOME!\bin;!PATH!"
    if defined NODE_HOME  if exist "!NODE_HOME!\node.exe"      set "PATH=!NODE_HOME!\;!PATH!"
)

rem --- Java ---
set "NEED_JDK=0"
java -version >nul 2>&1
if %errorlevel% neq 0 set "NEED_JDK=1"

if "!NEED_JDK!"=="1" (
    echo  [未找到] Java / JDK
    echo.
    set /p "DL_JDK=  是否自动下载 JDK 17 到 tools\ 目录？(Y/N，默认Y): "
    if /i "!DL_JDK!"=="N" (
        echo  需要 JDK 17，请手动安装后重试。
        goto :fail
    )
    call :download_tool "JDK 17" "%JDK_URL%" "%JDK_ZIP%" "%JDK_DIR_NAME%" "JAVA_HOME"
    if errorlevel 1 goto :fail
    set "NEED_JDK=0"
)

for /f "tokens=3" %%v in ('java -version 2^>^&1 ^| findstr /i "version"') do set "JAVA_VER=%%~v"
echo         Java:    %JAVA_VER%
echo.

rem --- Maven ---
set "NEED_MVN=0"
mvn -version >nul 2>&1
if %errorlevel% neq 0 set "NEED_MVN=1"

if "!NEED_MVN!"=="1" (
    echo  [未找到] Maven
    echo.
    set /p "DL_MVN=  是否自动下载 Maven 3.9 到 tools\ 目录？(Y/N，默认Y): "
    if /i "!DL_MVN!"=="N" (
        echo  需要 Maven，请手动安装后重试。
        goto :fail
    )
    call :download_tool "Maven" "%MVN_URL%" "%MVN_ZIP%" "%MVN_DIR_NAME%" "MAVEN_HOME"
    if errorlevel 1 goto :fail
    set "NEED_MVN=0"
)

for /f "tokens=*" %%v in ('mvn -version 2^>^&1 ^| findstr /i "Apache Maven"') do set "MVN_VER=%%v"
echo         Maven:   %MVN_VER%
echo.

rem --- Node.js / npm ---
set "NEED_NODE=0"
npm -v >nul 2>&1
if %errorlevel% neq 0 set "NEED_NODE=1"

if "!NEED_NODE!"=="1" (
    echo  [未找到] Node.js / npm
    echo.
    set /p "DL_NODE=  是否自动下载 Node.js 22 LTS 到 tools\ 目录？(Y/N，默认Y): "
    if /i "!DL_NODE!"=="N" (
        echo  需要 Node.js，请手动安装后重试。
        goto :fail
    )
    call :download_tool "Node.js" "%NODE_URL%" "%NODE_ZIP%" "%NODE_DIR_NAME%" "NODE_HOME"
    if errorlevel 1 goto :fail
    set "NEED_NODE=0"
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

rem =====================================================
rem  工具下载子程序
rem  参数：%1=名称  %2=URL  %3=zip文件名  %4=解压后目录名  %5=环境变量名
rem =====================================================
:download_tool
set "_NAME=%~1"
set "_URL=%~2"
set "_ZIP=%~3"
set "_DIR=%~4"
set "_ENV=%~5"
set "_ZIP_PATH=%TOOLS_DIR%\%_ZIP%"
set "_EXTRACT_DIR=%TOOLS_DIR%\%_DIR%"

if not exist "%TOOLS_DIR%" mkdir "%TOOLS_DIR%"

rem 如果已下载过且解压完成，直接使用
if exist "%_EXTRACT_DIR%" (
    echo  %_NAME% 已存在，跳过下载。
    goto :_tool_set_path
)

echo.
echo  正在下载 %_NAME% ...
echo  %_URL%
echo.

curl --version >nul 2>&1
if %errorlevel% equ 0 (
    curl -L -# -o "%_ZIP_PATH%" "%_URL%"
) else (
    echo  [使用 PowerShell 下载]
    powershell -Command "[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; Invoke-WebRequest -Uri '%_URL%' -OutFile '%_ZIP_PATH%' -UseBasicParsing"
)

if not exist "%_ZIP_PATH%" (
    echo  [错误] %_NAME% 下载失败，请检查网络。
    exit /b 1
)

echo  正在解压...
powershell -Command "Expand-Archive -Path '%_ZIP_PATH%' -DestinationPath '%TOOLS_DIR%' -Force"
del /f /q "%_ZIP_PATH%" 2>nul

if not exist "%_EXTRACT_DIR%" (
    echo  [错误] %_NAME% 解压失败。
    exit /b 1
)

:_tool_set_path
rem 设置环境变量（当前会话 + 永久写入 tools-path.txt）
set "%_ENV%=%_EXTRACT_DIR%"
set "PATH=%_EXTRACT_DIR%\bin;%_EXTRACT_DIR%;!PATH!"

rem 保存到配置文件
if exist "%TOOLS_CONFIG%" (
    powershell -Command "(Get-Content '%TOOLS_CONFIG%') -replace '^%_ENV%=.*', '' | Set-Content '%TOOLS_CONFIG%'"
)
echo %_ENV%=%_EXTRACT_DIR%>>"%TOOLS_CONFIG%"

echo  [✓ %_NAME% 安装完成：%_EXTRACT_DIR%]
echo.
exit /b 0

rem =====================================================
rem  结束处理
rem =====================================================
:fail
echo.
echo  ╔══════════════════════════════════════════╗
echo  ║  部署失败，请检查上述错误信息            ║
echo  ╚══════════════════════════════════════════╝
echo.
pause
exit /b 1
