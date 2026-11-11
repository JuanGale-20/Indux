@echo off
set "JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-21.0.12.8-hotspot"
cd /d "%~dp0"
powershell -ExecutionPolicy Bypass -File .\start-server.ps1
