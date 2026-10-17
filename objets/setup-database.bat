@echo off
title Lost & Found - Setup Base de Donnees
color 0A

echo ============================================
echo   Lost ^& Found - Configuration PostgreSQL
echo ============================================
echo.

SET PG_BIN=

:: Chercher psql.exe dans les emplacements courants
if exist "C:\Program Files\PostgreSQL\17\bin\psql.exe" SET PG_BIN=C:\Program Files\PostgreSQL\17\bin
if exist "C:\Program Files\PostgreSQL\16\bin\psql.exe" SET PG_BIN=C:\Program Files\PostgreSQL\16\bin
if exist "C:\Program Files\PostgreSQL\15\bin\psql.exe" SET PG_BIN=C:\Program Files\PostgreSQL\15\bin
if exist "C:\PostgreSQL\17\bin\psql.exe" SET PG_BIN=C:\PostgreSQL\17\bin

if "%PG_BIN%"=="" (
    echo [ERREUR] PostgreSQL n'est pas installe ou introuvable.
    echo.
    echo Solutions:
    echo  1. Lancez INSTALLER-POSTGRESQL.bat
    echo  2. Ou telechargez depuis https://www.postgresql.org/download/windows/
    echo.
    pause
    exit /b 1
)

echo [OK] PostgreSQL trouve: %PG_BIN%
echo.

SET PGPASSWORD=postgres

echo [INFO] Creation de la base 'lostandfound'...
"%PG_BIN%\psql.exe" -U postgres -c "CREATE DATABASE lostandfound;" 2>&1
echo.

echo [INFO] Test de connexion...
"%PG_BIN%\psql.exe" -U postgres -d lostandfound -c "SELECT 'Connexion OK' as status;" 2>&1

echo.
echo ============================================
echo  Base de donnees configuree !
echo  Lancez maintenant: start-backend.bat
echo ============================================
pause
