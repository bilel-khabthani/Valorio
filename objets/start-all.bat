@echo off
title Lost & Found - Demarrage complet
color 0E

echo ============================================
echo   Lost ^& Found - Demarrage de l'application
echo ============================================
echo.
echo  Cette fenetre va ouvrir 2 terminaux:
echo  1. Backend  -> http://localhost:8081
echo  2. Frontend -> http://localhost:4200
echo.
echo  Attendez que les 2 soient demarres avant
echo  d'ouvrir http://localhost:4200
echo.
pause

echo [1/2] Demarrage du backend...
start "Backend - Spring Boot" cmd /k "cd /d "%~dp0" && start-backend.bat"

echo Attente 15 secondes pour que le backend demarre...
timeout /t 15 /nobreak

echo [2/2] Demarrage du frontend...
start "Frontend - Angular" cmd /k "cd /d "%~dp0" && start-frontend.bat"

echo.
echo ============================================
echo  Les deux serveurs sont en cours de demarrage
echo  Ouvrez http://localhost:4200 dans 30 secondes
echo ============================================
pause
