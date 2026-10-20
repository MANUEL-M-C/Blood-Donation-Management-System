@echo off
setlocal enabledelayedexpansion

echo ============================================================
echo   BLOOD DONATION MANAGEMENT SYSTEM - BUILD SCRIPT
echo ============================================================

:: Locate javac
set JAVAC_CMD=javac
where javac >nul 2>nul
if %errorlevel% neq 0 (
    if exist "C:\Program Files\Java\jdk-26.0.1\bin\javac.exe" (
        set "JAVAC_CMD=C:\Program Files\Java\jdk-26.0.1\bin\javac.exe"
    ) else if exist "%JAVA_HOME%\bin\javac.exe" (
        set "JAVAC_CMD=%JAVA_HOME%\bin\javac.exe"
    ) else (
        echo [ERROR] javac could not be found. Please ensure Java 17+ JDK is installed and in your PATH.
        exit /b 1
    )
)

echo [INFO] Using Java Compiler: %JAVAC_CMD%

:: Create output directories
if not exist "target\classes" mkdir target\classes
if not exist "target\test-classes" mkdir target\test-classes

:: Generate relative file lists using PowerShell
powershell -NoProfile -Command "(Get-ChildItem -Path 'src/main/java' -Filter '*.java' -Recurse) | ForEach-Object { (Resolve-Path -Relative $_.FullName).Replace('\', '/') } | Set-Content -Path 'sources.txt' -Encoding ascii"
powershell -NoProfile -Command "(Get-ChildItem -Path 'src/test/java' -Filter '*.java' -Recurse) | ForEach-Object { (Resolve-Path -Relative $_.FullName).Replace('\', '/') } | Set-Content -Path 'test_sources.txt' -Encoding ascii"

echo [INFO] Compiling main application source code...
"%JAVAC_CMD%" -encoding UTF-8 -cp "lib/*" -d target\classes @sources.txt
if %errorlevel% neq 0 (
    echo [ERROR] Main compilation failed!
    if exist sources.txt del sources.txt
    if exist test_sources.txt del test_sources.txt
    exit /b 1
)

echo [INFO] Compiling automated test suite...
"%JAVAC_CMD%" -encoding UTF-8 -cp "target\classes;lib/*" -d target\test-classes @test_sources.txt
if %errorlevel% neq 0 (
    echo [ERROR] Test compilation failed!
    if exist sources.txt del sources.txt
    if exist test_sources.txt del test_sources.txt
    exit /b 1
)

if exist sources.txt del sources.txt
if exist test_sources.txt del test_sources.txt

:: Copy resources to target/classes
if exist "src\main\resources" (
    xcopy /s /y /q "src\main\resources\*" "target\classes\" >nul 2>nul
)

echo ============================================================
echo [SUCCESS] Build completed successfully!
echo Output classes: target\classes
echo You can now launch the application using: run.bat
echo ============================================================
exit /b 0
