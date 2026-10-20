@echo off
setlocal enabledelayedexpansion

echo ============================================================
echo   BLOOD DONATION MANAGEMENT SYSTEM - LAUNCH SCRIPT
echo ============================================================

:: Locate java
set JAVA_CMD=java
where java >nul 2>nul
if %errorlevel% neq 0 (
    if exist "C:\Program Files\Java\jdk-26.0.1\bin\java.exe" (
        set "JAVA_CMD=C:\Program Files\Java\jdk-26.0.1\bin\java.exe"
    ) else if exist "%JAVA_HOME%\bin\java.exe" (
        set "JAVA_CMD=%JAVA_HOME%\bin\java.exe"
    ) else (
        echo [ERROR] java runtime could not be found. Please ensure Java 17+ is installed.
        exit /b 1
    )
)

:: If target\classes does not exist, trigger build first
if not exist "target\classes\com\blooddonation\Main.class" (
    echo [INFO] Classes not compiled yet. Running build.bat first...
    call build.bat
    if %errorlevel% neq 0 (
        echo [ERROR] Build step failed. Cannot start application.
        pause
        exit /b 1
    )
)

echo [INFO] Launching Blood Donation Management System...
"%JAVA_CMD%" --enable-native-access=ALL-UNNAMED -cp "target\classes;lib\*" com.blooddonation.Main

if %errorlevel% neq 0 (
    echo [ERROR] Application exited with error code %errorlevel%.
    pause
)
exit /b 0
