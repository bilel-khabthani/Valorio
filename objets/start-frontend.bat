@echo off
title Lost & Found - Frontend Angular
color 0B

echo ============================================
echo   Lost ^& Found - Frontend Angular
echo   Port: 4200
echo   -> http://localhost:4200
echo ============================================
echo.

cd /d "%~dp0frontend"

echo [INFO] Verification de Node.js...
node --version
echo.

echo [INFO] Verification du backend (port 8081)...
powershell -Command "try { $t = New-Object Net.Sockets.TcpClient; $t.Connect('127.0.0.1',8081); $t.Close(); Write-Host '[OK] Backend accessible sur le port 8081' } catch { Write-Host '[AVERT] Backend non accessible - demarrez start-backend.bat d''abord' }"

echo.
echo [INFO] Demarrage d'Angular...
echo [INFO] Ouverture de http://localhost:4200
echo [INFO] Appuyez sur Ctrl+C pour arreter
echo.

npm start
pause
