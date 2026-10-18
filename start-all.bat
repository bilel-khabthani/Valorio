@echo off
REM =========================================================================
REM  ValorIO - Demarrage complet de la plateforme
REM  7 microservices Spring Boot + moteur IA Python (ml-engine) + frontend
REM  Pre-requis : PostgreSQL 16 ecoute sur le port 5432 (base "valorio")
REM =========================================================================
setlocal EnableDelayedExpansion
set "ROOT=%~dp0"

REM --- Localisation de Maven (PATH d'abord, puis wrapper local) -----------
set "MVN=mvn"
where mvn >nul 2>nul
if errorlevel 1 (
  set "MVNW=%USERPROFILE%\.m2\wrapper\dists\apache-maven-3.9.12\59fe215c0ad6947fea90184bf7add084544567b927287592651fda3782e0e798\bin\mvn.cmd"
  if not exist "!MVNW!" (
    set "MVNW=%ROOT%.mvnw.cmd"
  )
  set "MVN=!MVNW!"
)

echo.
echo [1/10] Verification de PostgreSQL sur le port 5432...
powershell -NoProfile -Command "if (Get-NetTCPConnection -LocalPort 5432 -State Listen -ErrorAction SilentlyContinue) { exit 0 } else { exit 1 }" >nul 2>nul
if errorlevel 1 (
  echo   ERREUR : PostgreSQL n'ecoute pas sur 5432.
  echo   Demarrez PostgreSQL (service postgresql-x64-*) puis relancez ce script.
  pause
  exit /b 1
)
echo   OK.

echo [2/10] service-reparation     :8081
start "valorio-reparation-8081" cmd /k "cd /d "%ROOT%valorio-backend\service-reparation" && (if exist target\service-reparation-0.0.1-SNAPSHOT.jar (java -jar target\service-reparation-0.0.1-SNAPSHOT.jar) else ("!MVN!" spring-boot:run))"

echo [3/10] service-don            :8082
start "valorio-don-8082" cmd /k "cd /d "%ROOT%valorio-backend\service-don" && (if exist target\service-don-0.0.1-SNAPSHOT.jar (java -jar target\service-don-0.0.1-SNAPSHOT.jar) else ("!MVN!" spring-boot:run))"

echo [4/10] service-recyclage      :8083
start "valorio-recyclage-8083" cmd /k "cd /d "%ROOT%valorio-backend\service-recyclage" && (if exist target\service-recyclage-0.0.1-SNAPSHOT.jar (java -jar target\service-recyclage-0.0.1-SNAPSHOT.jar) else ("!MVN!" spring-boot:run))"

echo [5/10] service-auth           :8084
start "valorio-auth-8084" cmd /k "cd /d "%ROOT%valorio-backend\service-auth" && (if exist target\service-auth-0.0.1-SNAPSHOT.jar (java -jar target\service-auth-0.0.1-SNAPSHOT.jar) else ("!MVN!" spring-boot:run))"

echo [6/10] service-objets         :8085
start "valorio-objets-8085" cmd /k "cd /d "%ROOT%valorio-backend\service-objets" && (if exist target\service-objets-0.0.1-SNAPSHOT.jar (java -jar target\service-objets-0.0.1-SNAPSHOT.jar) else ("!MVN!" spring-boot:run))"

echo [7/10] service-marketplace    :8086
start "valorio-marketplace-8086" cmd /k "cd /d "%ROOT%valorio-backend\service-marketplace" && (if exist target\service-marketplace-0.0.1-SNAPSHOT.jar (java -jar target\service-marketplace-0.0.1-SNAPSHOT.jar) else ("!MVN!" spring-boot:run))"

echo [8/10] service-recommandation :8087
start "valorio-recommandation-8087" cmd /k "cd /d "%ROOT%valorio-backend\service-recommandation" && (if exist target\service-recommandation-0.0.1-SNAPSHOT.jar (java -jar target\service-recommandation-0.0.1-SNAPSHOT.jar) else ("!MVN!" spring-boot:run))"

echo [9/10] ml-engine (Python)     :8090
start "valorio-ml-engine-8090" cmd /k "cd /d "%ROOT%valorio-backend\ml-engine" && python -m uvicorn app.main:app --host 127.0.0.1 --port 8090"

echo [10/10] valorio-frontend      :4200
start "valorio-frontend-4200" cmd /k "cd /d "%ROOT%valorio-frontend" && npm start"

echo.
echo Demarrage lance. Attendez ~40s puis ouvrez http://localhost:4200
echo Santes : http://localhost:808X/actuator/health  |  ML : http://localhost:8090/health
echo Pour tout arreter : stop-all.bat
pause
