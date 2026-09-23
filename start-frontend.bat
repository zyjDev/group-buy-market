@echo off
setlocal

rem ============================================================
rem  Group-Buy-Market  -  Frontend static page launcher
rem  Web root : docs\dev-ops\nginx\html
rem  Backend  : http://127.0.0.1:8091  (start it separately)
rem  Run this script from the repository root.
rem ============================================================

set "PORT=8080"
set "BACKEND_PORT=8091"
set "ROOT=%~dp0"
set "WEBROOT=%ROOT%docs\dev-ops\nginx\html"

cls
echo.
echo ============================================================
echo   Group-Buy-Market  -  Frontend
echo ============================================================
echo.

if not exist "%WEBROOT%\login.html" goto :no_webroot

rem ---------- check whether the static port is already in use ----------
netstat -ano | findstr /c:":%PORT% " | findstr /i "LISTENING" >nul 2>&1
if not errorlevel 1 goto :port_busy

rem ---------- check whether the backend is up ----------
netstat -ano | findstr /c:":%BACKEND_PORT% " | findstr /i "LISTENING" >nul 2>&1
if errorlevel 1 goto :backend_down
echo   [OK]   Backend is listening on port %BACKEND_PORT%
goto :pick_server

:backend_down
echo   [WARN] Backend is NOT listening on port %BACKEND_PORT%
echo          The page will open, but every API call will fail.
echo          Start group-buy-market-app first, then reload the page.

:pick_server
echo.
echo   Web root : docs\dev-ops\nginx\html
echo   Open     : http://127.0.0.1:%PORT%/login.html
echo   Backend  : http://127.0.0.1:%BACKEND_PORT%
echo.

set "SRV="
where py >nul 2>&1
if not errorlevel 1 set "SRV=py -3 -m http.server %PORT% --bind 127.0.0.1"
if defined SRV goto :run

where python >nul 2>&1
if not errorlevel 1 set "SRV=python -m http.server %PORT% --bind 127.0.0.1"
if defined SRV goto :run

where npx >nul 2>&1
if not errorlevel 1 set "SRV=npx --yes http-server -p %PORT% -a 127.0.0.1 -c-1"
if defined SRV goto :run

goto :no_runtime

:run
echo   [RUN] %SRV%
echo.
start "Frontend static server - close this window to stop" /d "%WEBROOT%" cmd /k "%SRV%"
timeout /t 2 /nobreak >nul
start "" "http://127.0.0.1:%PORT%/login.html"
echo   [DONE] Browser opened.
echo          The server runs in the other window; close it to stop.
echo.
timeout /t 5 /nobreak >nul
exit /b 0

rem ============================================================

:no_webroot
echo   [ERROR] Web root not found:
echo           %WEBROOT%
echo.
echo           Put this script in the repository root and run it there.
echo.
pause
exit /b 1

:port_busy
echo   [ERROR] Port %PORT% is already in use.
echo.
echo           Edit this script and change PORT=8080 to another port.
echo.
pause
exit /b 1

:no_runtime
echo   [ERROR] No static file server runtime found.
echo.
echo           Need one of:
echo             - Python  (py  or  python  on PATH)
echo             - Node.js (npx on PATH)
echo.
echo           Install one, open a NEW terminal, then run this script again.
echo.
pause
exit /b 1
