@echo off
cd /d "%~dp0"
start "Indux Backend" powershell -NoExit -ExecutionPolicy Bypass -File "%~dp0start-server.ps1"
