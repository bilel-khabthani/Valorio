@echo off
title Lost & Found - Backend Spring Boot
color 0A

echo ============================================
echo   Lost ^& Found - Backend Spring Boot
echo   Port: 8081
echo ============================================
echo.

SET JAVA_HOME=C:\Program Files\Java\jdk-22
SET MAVEN_HOME=C:\maven
SET PATH=%JAVA_HOME%\bin;%MAVEN_HOME%\bin;%PATH%

cd /d "%~dp0backend"

echo [1/3] Verification de Java...
java -version 2>&1 | findstr "version"
if errorlevel 1 (
    echo [ERREUR] Java introuvable dans %JAVA_HOME%
    pause & exit /b 1
)

echo.
echo [2/3] Verification de Maven...
mvn.cmd -version 2>&1 | findstr "Maven"
if errorlevel 1 (
    echo [ERREUR] Maven introuvable dans %MAVEN_HOME%
    pause & exit /b 1
)

echo.
echo [3/3] Verification de PostgreSQL (port 5432)...
powershell -NoProfile -Command "try { $t = New-Object Net.Sockets.TcpClient('127.0.0.1',5432); $t.Close(); exit 0 } catch { exit 1 }"
if errorlevel 1 (
    echo [ERREUR] PostgreSQL n'est pas accessible sur le port 5432.
    echo.
    echo Solutions:
    echo   1. Ouvrez services.msc et demarrez 'postgresql-x64-17'
    echo   2. Ou lancez INSTALLER-POSTGRESQL.bat puis setup-database.bat
    echo.
    pause
    exit /b 1
)
echo [OK] PostgreSQL accessible

echo.
echo ============================================
echo  Demarrage du backend Spring Boot...
echo  -> http://localhost:8081/api/stats
echo  Appuyez sur Ctrl+C pour arreter
echo ============================================
echo.

mvn.cmd spring-boot:run -Dspring-boot.run.jvmArguments="-Xms256m -Xmx512m"

pause
