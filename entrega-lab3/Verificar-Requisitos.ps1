[CmdletBinding()]
param(
    [Parameter(Mandatory)][ValidateSet('Central','Webapp')][string]$Rol,
    [string]$JavaRuta,
    [string]$PostgresRuta,
    [string]$TomcatRuta
)
$ErrorActionPreference = 'Stop'
. (Join-Path $PSScriptRoot 'Requisitos-Comunes.ps1')
$datos = Get-RequisitosManifiesto
$destino = Join-Path $env:LOCALAPPDATA ("EventosUyTools/" + $datos.Tomcat.DirectoryName)
$estado = @(Get-RequisitosEstado -Rol $Rol -JavaRuta $JavaRuta -PostgresRuta $PostgresRuta -TomcatRuta $TomcatRuta -TomcatDestino $destino)
$estado | Format-Table Componente, Requerido, Version, Compatible, Ruta -AutoSize
Write-Host "Reinicio de Windows pendiente: $(Test-ReinicioPendiente)"
Write-Host "WinGet disponible: $([bool](Get-Command winget.exe -ErrorAction SilentlyContinue))"
Write-Host 'Maven no se necesita en los equipos de ejecución.'
if (@($estado | Where-Object { !$_.Compatible }).Count) { exit 1 }
