@echo off
setlocal EnableExtensions
chcp 65001 >nul

rem ===== Basic config =====
rem If Tomcat is installed elsewhere, set CATALINA_HOME before running this file,
rem or change the default path below.
if not defined CATALINA_HOME set "CATALINA_HOME=E:\Apache Software Foundation\Tomcat 9.0"

echo.
echo  当前 Tomcat 路径：%CATALINA_HOME%
echo  如需更改请输入新路径，直接回车则使用当前路径。
echo.
set /p "TOMCAT_INPUT=  Tomcat 路径: "
if defined TOMCAT_INPUT (
    set "CATALINA_HOME=%TOMCAT_INPUT:"=%"
    echo  已切换到：%CATALINA_HOME%
)
echo.

set "APP_NAME=library"
set "PROJECT_DIR=%~dp0"
set "FRONTEND_DIR=%PROJECT_DIR%frontend"
set "DEPLOY_ROOT=%PROJECT_DIR%target-deploy"
set "BUILD_DIR=%DEPLOY_ROOT%\build-%RANDOM%-%RANDOM%"
set "WAR_FILE=%BUILD_DIR%\%APP_NAME%.war"
set "WEBAPPS_DIR=%CATALINA_HOME%\webapps"
set "APP_DIR=%WEBAPPS_DIR%\%APP_NAME%"
set "APP_WAR=%WEBAPPS_DIR%\%APP_NAME%.war"

echo.
echo ===== Tomcat auto deploy =====
echo Project:  %PROJECT_DIR%
echo Tomcat:   %CATALINA_HOME%
echo App name: %APP_NAME%
echo Build dir:%BUILD_DIR%
echo.

call :check_env || goto :failed
if /i "%~1"=="--check" (
    echo Environment check passed.
    exit /b 0
)

echo ===== 1. Build frontend =====
pushd "%FRONTEND_DIR%" || goto :frontend_dir_failed
call npm run build
if errorlevel 1 (
    popd
    echo Frontend build failed.
    goto :failed
)
popd

echo ===== 2. Package WAR =====
pushd "%PROJECT_DIR%" || goto :project_dir_failed
call mvn "-Ddeploy.build.directory=%BUILD_DIR%" clean package -DskipTests
if errorlevel 1 (
    popd
    echo Maven package failed.
    echo Check the Maven error above. This script uses target-deploy to avoid locked target files.
    goto :failed
)
popd

if not exist "%WAR_FILE%" (
    echo WAR file not found: "%WAR_FILE%"
    goto :failed
)

call :cleanup_old_builds

if /i "%~1"=="--build-only" (
    echo.
    echo ===== Build completed =====
    echo WAR: "%WAR_FILE%"
    echo Tomcat deploy was skipped because --build-only was used.
    echo.
    exit /b 0
)

echo ===== 3. Stop Tomcat =====
call "%CATALINA_HOME%\bin\shutdown.bat"
call :wait_seconds 8

echo ===== 4. Replace WAR =====
if exist "%APP_DIR%" (
    echo Removing old app directory: "%APP_DIR%"
    rd /s /q "%APP_DIR%" 2>nul
    if exist "%APP_DIR%" (
        echo Failed to remove old app directory. Tomcat may still be running or files are locked.
        echo Stop java.exe/Tomcat processes that use "%APP_DIR%", then run this script again.
        goto :failed
    )
)

if exist "%APP_WAR%" (
    echo Removing old WAR: "%APP_WAR%"
    del /f /q "%APP_WAR%" 2>nul
    if exist "%APP_WAR%" (
        echo Failed to remove old WAR. Tomcat may still be running or files are locked.
        goto :failed
    )
)

echo Copying new WAR to Tomcat webapps.
copy /y "%WAR_FILE%" "%APP_WAR%" >nul
if errorlevel 1 (
    echo Failed to copy WAR. Check Tomcat webapps permissions.
    goto :failed
)

echo ===== 5. Start Tomcat =====
call "%CATALINA_HOME%\bin\startup.bat"
if errorlevel 1 (
    echo Tomcat startup script failed.
    goto :failed
)

echo.
echo ===== Deploy completed =====
echo URL: http://localhost:8080/%APP_NAME%/
echo If it does not open, check "%CATALINA_HOME%\logs\catalina.*.log"
echo.
exit /b 0

:check_env
if not exist "%CATALINA_HOME%\bin\startup.bat" (
    echo Invalid CATALINA_HOME. startup.bat not found:
    echo "%CATALINA_HOME%\bin\startup.bat"
    exit /b 1
)
if not exist "%CATALINA_HOME%\bin\shutdown.bat" (
    echo Invalid CATALINA_HOME. shutdown.bat not found:
    echo "%CATALINA_HOME%\bin\shutdown.bat"
    exit /b 1
)
if not exist "%WEBAPPS_DIR%" (
    echo Tomcat webapps directory not found:
    echo "%WEBAPPS_DIR%"
    exit /b 1
)
if not exist "%FRONTEND_DIR%\package.json" (
    echo Invalid frontend directory. package.json not found:
    echo "%FRONTEND_DIR%\package.json"
    exit /b 1
)
if not exist "%PROJECT_DIR%pom.xml" (
    echo Invalid project directory. pom.xml not found:
    echo "%PROJECT_DIR%pom.xml"
    exit /b 1
)
where npm >nul 2>&1
if errorlevel 1 (
    echo npm not found. Install Node.js or add npm to PATH.
    exit /b 1
)
where mvn >nul 2>&1
if errorlevel 1 (
    echo mvn not found. Install Maven or add mvn to PATH.
    exit /b 1
)
exit /b 0

:wait_seconds
timeout /t %1 /nobreak >nul
exit /b 0

:cleanup_old_builds
echo ===== Clean old Maven build artifacts =====
if not exist "%DEPLOY_ROOT%" (
    exit /b 0
)
for /d %%D in ("%DEPLOY_ROOT%\build-*") do (
    if /i not "%%~fD"=="%BUILD_DIR%" (
        echo Removing old build directory: "%%~fD"
        rd /s /q "%%~fD" 2>nul
        if exist "%%~fD" (
            echo Warning: failed to remove old build directory: "%%~fD"
        )
    )
)
exit /b 0

:frontend_dir_failed
echo Cannot enter frontend directory: "%FRONTEND_DIR%"
goto :failed

:project_dir_failed
echo Cannot enter project directory: "%PROJECT_DIR%"
goto :failed

:failed
echo.
echo ===== Deploy failed =====
echo The last error above is the current failure reason.
echo.
exit /b 1
