[CmdletBinding()]
param(
    [Parameter(Mandatory)][string]$HostCentral,
    [int]$PuertoCentral = 9128,
    [string]$RutaEventos = '/eventos',
    [string]$RutaPortal = '/portal',
    [string]$RutaSesiones = '/sesiones',
    [string]$Destino = (Join-Path $PSScriptRoot 'config/web.properties'),
    [switch]$Actualizar
)
$ErrorActionPreference = 'Stop'
$plantilla = Join-Path $PSScriptRoot 'config/web.properties.example'
if (!(Test-Path -LiteralPath $plantilla)) { throw "Falta $plantilla" }
if ($PuertoCentral -lt 1 -or $PuertoCentral -gt 65535) { throw 'PuertoCentral inválido.' }
if ($HostCentral -match '[/?#@\s]' -or [Uri]::CheckHostName($HostCentral) -eq [UriHostNameType]::Unknown) {
    throw 'HostCentral debe ser una IP o un nombre DNS, sin esquema ni ruta.'
}
foreach ($ruta in @($RutaEventos,$RutaPortal,$RutaSesiones)) {
    if ($ruta -notmatch '^/[A-Za-z0-9_-]+(?:/[A-Za-z0-9_-]+)*$') { throw "Ruta SOAP inválida: $ruta" }
}
$rutas = @($RutaEventos,$RutaPortal,$RutaSesiones)
if (@($rutas | Select-Object -Unique).Count -ne 3) {
    throw 'Las tres rutas SOAP deben ser diferentes.'
}
$valores = [ordered]@{
    'central.host' = $HostCentral
    'central.puerto' = [string]$PuertoCentral
    'central.ruta' = $RutaEventos
    'central.ruta.portal' = $RutaPortal
    'central.ruta.sesiones' = $RutaSesiones
}
$texto = Get-Content -LiteralPath $plantilla -Raw
foreach ($clave in $valores.Keys) {
    $patron = '(?m)^' + [regex]::Escape($clave) + '=.*$'
    if ($texto -notmatch $patron) { throw "La plantilla no contiene $clave" }
    $texto = [regex]::Replace($texto, $patron, $clave + '=' + $valores[$clave])
}
$rutaDestino = [IO.Path]::GetFullPath($Destino)
if (Test-Path -LiteralPath $rutaDestino) {
    $actual = Get-Content -LiteralPath $rutaDestino -Raw
    $coincide = $true
    foreach ($clave in $valores.Keys) {
        $patron = '(?m)^' + [regex]::Escape($clave) + '=' + [regex]::Escape($valores[$clave]) + '\s*$'
        if ($actual -notmatch $patron) { $coincide = $false; break }
    }
    if ($coincide) { Write-Host "Configuración ya vigente: $rutaDestino"; return }
    if (!$Actualizar) { throw "Ya existe $rutaDestino con valores distintos. Revisar y usar -Actualizar para guardar respaldo y reemplazarlo." }
    $respaldo = $rutaDestino + '.bak-' + (Get-Date -Format 'yyyyMMdd-HHmmss')
    if (Test-Path -LiteralPath $respaldo) { throw "Ya existe $respaldo" }
    Copy-Item -LiteralPath $rutaDestino -Destination $respaldo
    Write-Host "Respaldo creado: $respaldo"
}
New-Item -ItemType Directory -Force -Path (Split-Path -Parent $rutaDestino) | Out-Null
[IO.File]::WriteAllText($rutaDestino, $texto, (New-Object Text.UTF8Encoding($false)))
Write-Host "Configuración SOAP guardada en $rutaDestino; Portal: https://${HostCentral}:$PuertoCentral$RutaPortal"
