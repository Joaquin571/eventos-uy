[CmdletBinding()]
param(
    [string]$Java,
    [string]$Config = (Join-Path $PSScriptRoot 'config/central.properties'),
    [switch]$SinSwing
)
$ErrorActionPreference = 'Stop'
. (Join-Path $PSScriptRoot '../Requisitos-Comunes.ps1')
$jar = Join-Path $PSScriptRoot 'servidor.jar'
$config = [IO.Path]::GetFullPath($Config)
if (!(Test-Path -LiteralPath $jar)) { throw "Falta $jar" }
if (!(Test-Path -LiteralPath $config)) { throw "Falta $config. Ejecutar PC1/Preparar-Central.ps1." }
if (!$Java) {
    $detectado = @(Get-RequisitosEstado -Rol Central | Where-Object { $_.Componente -eq 'Java' -and $_.Compatible })
    if ($detectado.Count) { $Java = $detectado[0].Ruta }
}
if (!$Java -or (Get-VersionJava $Java) -notlike '25.*') {
    throw 'Se requiere Java 25. Indicar -Java con la ruta a java.exe del JDK 25.'
}
$argumentos = @("-Deventos.central.config=$config", '-jar', $jar)
if ($SinSwing) { $argumentos += '--sin-swing' }
Write-Host 'Iniciando Central; esta consola debe permanecer abierta. Configuración:' $config
$preferencia = $ErrorActionPreference
$ErrorActionPreference = 'Continue'
try { & $Java @argumentos; $codigo = $LASTEXITCODE }
finally { $ErrorActionPreference = $preferencia }
if ($codigo -ne 0) { throw "Central terminó con código $codigo. Revisar configuración, TLS y PostgreSQL." }
