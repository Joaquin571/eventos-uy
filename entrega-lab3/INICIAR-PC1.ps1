[CmdletBinding()]
param(
    [string]$Java,
    [switch]$ConfigurarRutas,
    [switch]$SoloVerificar,
    [switch]$SinSwing
)
$ErrorActionPreference = 'Stop'
. (Join-Path $PSScriptRoot 'Arranque-Comun.ps1')
$opciones = Join-Path $PSScriptRoot 'PC1/config/arranque.json'
if ($ConfigurarRutas) {
    if (!$Java) { throw 'Indicar -Java con la ruta a java.exe para guardar la configuracion.' }
    $Java = Find-Java25 $Java
    Save-OpcionesArranque $opciones @{ Java = $Java }
    Write-Host "Ruta de Java 25 guardada en $opciones"
    return
}
$config = Join-Path $PSScriptRoot 'PC1/config/central.properties'
$jar = Join-Path $PSScriptRoot 'PC1/servidor.jar'
if (!(Test-Path -LiteralPath $config -PathType Leaf)) { throw "Falta $config" }
if (!(Test-Path -LiteralPath $jar -PathType Leaf)) { throw "Falta $jar" }
if (!$Java) { $Java = Get-OpcionArranque $opciones 'Java' }
$Java = Find-Java25 $Java
$propiedades = Get-Content -LiteralPath $config | Where-Object { $_ -match '^(db\.url|central\.escucha|central\.puerto)=' }
$url = @($propiedades | Where-Object { $_ -match '^db\.url=' } | Select-Object -Last 1)
if (!$url.Count -or $url[0] -notmatch '^db\.url=jdbc:postgresql://([^/:]+):(\d+)/') { throw 'db.url de PostgreSQL no es valido.' }
$dbHost = $Matches[1]
$dbPuerto = [int]$Matches[2]
Assert-PostgresDisponible $dbHost $dbPuerto
$escucha = @($propiedades | Where-Object { $_ -match '^central\.escucha=' } | Select-Object -Last 1)
$puerto = @($propiedades | Where-Object { $_ -match '^central\.puerto=' } | Select-Object -Last 1)
if (!$escucha.Count -or !$puerto.Count -or $puerto[0] -notmatch '^central\.puerto=(\d+)$') { throw 'Puerto o interfaz de Central no validos.' }
$puertoCentral = [int]$Matches[1]
$hostCentral = $escucha[0].Substring('central.escucha='.Length)
if ($hostCentral -eq '0.0.0.0') { $hostCentral = '127.0.0.1' }
if (Test-PuertoTcp $hostCentral $puertoCentral) { throw "El puerto de Central ya esta ocupado: ${hostCentral}:$puertoCentral." }
if ($SoloVerificar) { Write-Host 'PC1: requisitos y rutas verificados. Central no se inicio.'; return }
$argumentos = @{ Java = $Java; Config = $config }
if ($SinSwing) { $argumentos.SinSwing = $true }
& (Join-Path $PSScriptRoot 'PC1/Iniciar-Central.ps1') @argumentos
