$ErrorActionPreference = "Stop"
$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $scriptDir

$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-21.0.12.8-hotspot"

if (Test-Path ".env") {
    Get-Content ".env" | ForEach-Object {
        $line = $_.Trim()
        if ($line -and -not $line.StartsWith("#") -and $line.Contains("=")) {
            $parts = $line.Split("=", 2)
            $key = $parts[0].Trim()
            $val = $parts[1].Trim()
            Set-Item -Path "env:$key" -Value $val
        }
    }
}

Write-Host "Variables cargadas desde .env:"
Write-Host "  DB: $env:INDUX_DB_URL ($env:INDUX_DB_USERNAME)"
Write-Host "  Admin: $env:INDUX_ADMIN_EMAIL"
Write-Host "Iniciando Indux Backend en http://localhost:8080 ..."

& ".\mvnw.cmd" spring-boot:run
