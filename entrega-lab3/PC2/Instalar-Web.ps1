[CmdletBinding()]
param(
    [string]$Tomcat,
    [switch]$Reemplazar
)
$ErrorActionPreference = 'Stop'
. (Join-Path $PSScriptRoot '../Requisitos-Comunes.ps1')
if (!$Tomcat) {
    if ($env:CATALINA_HOME) { $Tomcat = $env:CATALINA_HOME }
    else { $Tomcat = Join-Path $env:LOCALAPPDATA 'EventosUyTools/apache-tomcat-11.0.29' }
}
$war = Join-Path $PSScriptRoot 'web.war'
$catalina = Join-Path $Tomcat 'bin/catalina.bat'
$destino = Join-Path $Tomcat 'webapps/web.war'
if (!(Test-Path -LiteralPath $war)) { throw "Falta $war" }
if (!(Test-Path -LiteralPath $catalina)) { throw "Falta Tomcat 11 en $Tomcat" }
$version = Get-VersionTomcat $Tomcat
if (!$version -or !$version.StartsWith('11.')) { throw "Tomcat 11 no verificado en $Tomcat" }
if (Test-Path -LiteralPath $destino) {
    if (!$Reemplazar) { throw "Ya existe $destino. Detener Tomcat y usar -Reemplazar para guardar respaldo y actualizarlo." }
    $marca = Get-Date -Format 'yyyyMMdd-HHmmss'
    $respaldo = Join-Path $Tomcat ('webapps-backup/web-' + $marca + '.war')
    New-Item -ItemType Directory -Force -Path (Split-Path -Parent $respaldo) | Out-Null
    if (Test-Path -LiteralPath $respaldo) { throw "Ya existe $respaldo" }
    Copy-Item -LiteralPath $destino -Destination $respaldo
    Write-Host "WAR anterior respaldado en $respaldo"
    $expandido = Join-Path $Tomcat 'webapps/web'
    if (Test-Path -LiteralPath $expandido) {
        $raizTomcat = [IO.Path]::GetFullPath($Tomcat).TrimEnd('\')
        $rutaExpandida = [IO.Path]::GetFullPath($expandido)
        $respaldoExpandido = [IO.Path]::GetFullPath((Join-Path $Tomcat ('webapps-backup/web-' + $marca + '-expandido')))
        if (!$rutaExpandida.StartsWith($raizTomcat + '\', [StringComparison]::OrdinalIgnoreCase) -or
            !$respaldoExpandido.StartsWith($raizTomcat + '\', [StringComparison]::OrdinalIgnoreCase) -or
            (Test-Path -LiteralPath $respaldoExpandido)) { throw 'Rutas de respaldo inesperadas. No se mueve la aplicación expandida.' }
        Move-Item -LiteralPath $rutaExpandida -Destination $respaldoExpandido
        Write-Host "Aplicación expandida anterior respaldada en $respaldoExpandido"
    }
}
Copy-Item -LiteralPath $war -Destination $destino -Force
if ((Get-FileHash -Algorithm SHA256 -LiteralPath $war).Hash -ne (Get-FileHash -Algorithm SHA256 -LiteralPath $destino).Hash) {
    throw 'La copia del WAR no coincide con el original.'
}
Write-Host "WAR copiado en $destino. Contexto esperado: /web"
