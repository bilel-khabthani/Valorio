@echo off
title Installation PostgreSQL 17
color 0C

echo ============================================
echo   Installation de PostgreSQL 17
echo   (Necessite droits Administrateur)
echo ============================================
echo.

SET INSTALLER=C:\Users\suzan\AppData\Local\Temp\WinGet\PostgreSQL.PostgreSQL.17.17.11-5\postgresql-17.11-5-windows-x64.exe

if not exist "%INSTALLER%" (
    echo [ERREUR] Installeur non trouve: %INSTALLER%
    echo Telechargez PostgreSQL depuis: https://www.postgresql.org/download/windows/
    pause
    exit /b 1
)

echo [INFO] Lancement de l'installation PostgreSQL 17...
echo [INFO] MOT DE PASSE: postgres (a utiliser dans l'installeur)
echo.

"%INSTALLER%" --mode unattended --superpassword postgres --servicename postgresql-x64-17 --serviceport 5432 --prefix "C:\Program Files\PostgreSQL\17" --datadir "C:\Program Files\PostgreSQL\17\data"

echo.
echo [INFO] Verification du service...
sc query postgresql-x64-17

echo.
echo ============================================
echo  Si l'installation a reussi:
echo  1. Lancez setup-database.bat
echo  2. Puis start-backend.bat
echo  3. Puis start-frontend.bat
echo ============================================
pause
