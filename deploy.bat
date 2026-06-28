@echo off
setlocal EnableExtensions EnableDelayedExpansion
chcp 65001 >nul 2>&1

set "PROJECT_DIR=%~dp0"
set "APP_NAME=library"
set "FRONTEND_DIR=%PROJECT_DIR%frontend"
set "DEPLOY_ROOT=%PROJECT_DIR%target-deploy"
set "BUILD_DIR=%DEPLOY_ROOT%\build-%RANDOM%-%RANDOM%"
set "WAR_FILE=%BUILD_DIR%\%APP_NAME%.war"

set "CATALINA_HOME="
for %%P in (
    "%PROJECT_DIR%tomcat"
    "C:\Program Files\Apache Software Foundation\Tomcat 9.0"
    "E:\Apache Software Foundation\Tomcat 9.0"
    "D:\apache-tomcat-9.0"
    "C:\apache-tomcat-9.0"
) do (
    if exist "%%~P\bin\startup.bat" (
        set "CATALINA_HOME=%%~P"
        goto :tomcat_selected
    )
)

:tomcat_selected
if not defined CATALINA_HOME (
    echo.
    echo Please enter the Tomcat installation directory.
    set /p "CATALINA_HOME=Tomcat path: "
    set "CATALINA_HOME=!CATALINA_HOME:~0,-0!"
)

set "CATALINA_HOME=%CATALINA_HOME:"=%"
set "WEBAPPS_DIR=%CATALINA_HOME%\webapps"
set "APP_DIR=%WEBAPPS_DIR%\%APP_NAME%"
set "APP_WAR=%WEBAPPS_DIR%\%APP_NAME%.war"

echo.
echo ===== Tomcat auto deploy =====
echo Project: %PROJECT_DIR%
echo Tomcat:  %CATALINA_HOME%
echo App name: %APP_NAME%
echo Build dir: %BUILD_DIR%
echo.

call :check_env
if errorlevel 1 (
    echo.
    echo ===== Deploy failed =====
    echo Please fix the above errors and run again.
    echo.
    pause
    exit /b 1
)

if /i "%~1"=="--check" (
    echo Environment check passed.
    echo.
    pause
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
    echo Tomcat deploy skipped because --build-only was used.
    echo.
    pause
    exit /b 0
)

echo ===== 3. Stop Tomcat =====
call "%CATALINA_HOME%\bin\shutdown.bat"
call :wait_seconds 8

echo ===== 4. Replace WAR =====
if exist "%APP_DIR%" (
    echo Removing old app directory: "%APP_DIR%"
    rd /s /q "%APP_DIR%" 2>nul
)

if exist "%APP_WAR%" (
    echo Removing old WAR: "%APP_WAR%"
    del /f /q "%APP_WAR%" 2>nul
)

copy /y "%WAR_FILE%" "%APP_WAR%" >nul
if errorlevel 1 (
    echo Failed to copy WAR to Tomcat webapps.
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
echo.
pause
exit /b 0

:check_env
if not exist "%CATALINA_HOME%\bin\startup.bat" (
    echo Invalid Tomcat path. startup.bat not found:
    echo "%CATALINA_HOME%\bin\startup.bat"
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
if not exist "%DEPLOY_ROOT%" exit /b 0
for /d %%D in ("%DEPLOY_ROOT%\build-*") do (
    if /i not "%%~fD"=="%BUILD_DIR%" (
        rd /s /q "%%~fD" 2>nul
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
pause
exit /b 1
