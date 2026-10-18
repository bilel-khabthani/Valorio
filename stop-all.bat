@echo off
REM =========================================================================
REM  ValorIO - Arret complet de la plateforme
REM  Tue les processus lies aux ports de chaque module, puis les fenetres.
REM =========================================================================
echo.
echo Arret de ValorIO (ports 8081-8087, 8090, 4200)...
for %%p in (8081 8082 8083 8084 8085 8086 8087 8090 4200) do (
  powershell -NoProfile -Command "$c = Get-NetTCPConnection -LocalPort %%p -State Listen -ErrorAction SilentlyContinue; if ($c) { Stop-Process -Id $c.OwningProcess -Force -ErrorAction SilentlyContinue; Write-Host '  port %%p : arrete' } else { Write-Host '  port %%p : deja libre' }"
)

REM Fermeture des fenetres de service ouvertes par start-all.bat
for %%t in (valorio-reparation-8081 valorio-don-8082 valorio-recyclage-8083 valorio-auth-8084 valorio-objets-8085 valorio-marketplace-8086 valorio-recommandation-8087 valorio-ml-engine-8090 valorio-frontend-4200) do (
  taskkill /FI "WINDOWTITLE eq %%t*" /F >nul 2>nul
)

echo Termine.
pause
