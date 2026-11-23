[CmdletBinding()]
param(
    [string]$PostgresBin = (Join-Path $env:ProgramFiles 'PostgreSQL/17/bin'),
    [string]$HostDb = '127.0.0.1',
    [int]$PuertoDb = 5432,
    [string]$NombreDb = 'eventosuy_defensa_demo',
    [string]$UsuarioDb,
    [string]$Java
)
$ErrorActionPreference = 'Stop'
. (Join-Path $PSScriptRoot '../Requisitos-Comunes.ps1')
if ($NombreDb -notmatch '^[A-Za-z0-9_]+_demo$') { throw 'La base de demostración debe terminar en _demo.' }
$psql = Join-Path $PostgresBin 'psql.exe'
$jar = Join-Path $PSScriptRoot '../PC1/servidor.jar'
if (!(Test-Path -LiteralPath $psql) -or !(Test-Path -LiteralPath $jar)) { throw 'Faltan psql.exe o servidor.jar.' }
if (!$Java) {
    $detectado = @(Get-RequisitosEstado -Rol Central | Where-Object { $_.Componente -eq 'Java' -and $_.Compatible })
    if ($detectado.Count) { $Java = $detectado[0].Ruta }
}
if (!$Java -or (Get-VersionJava $Java) -notlike '25.*') { throw 'Se requiere Java 25. Indicar -Java con la ruta a java.exe.' }
$credencial = Get-Credential -UserName $UsuarioDb -Message 'Usuario de PostgreSQL para datos demo'
$segura = Read-Host 'Contraseña nueva para los usuarios de demostración' -AsSecureString
$puntero = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($segura)
$anterior = @{}
foreach ($nombre in @('EVENTOS_DB_HOST','EVENTOS_DB_PORT','EVENTOS_DB_NAME','EVENTOS_DB_USER','EVENTOS_DB_PASSWORD','EVENTOS_DEMO_PASSWORD','PGPASSWORD')) {
    $anterior[$nombre] = [Environment]::GetEnvironmentVariable($nombre, 'Process')
}
try {
    $env:EVENTOS_DB_HOST = $HostDb
    $env:EVENTOS_DB_PORT = [string]$PuertoDb
    $env:EVENTOS_DB_NAME = $NombreDb
    $env:EVENTOS_DB_USER = $credencial.UserName
    $env:EVENTOS_DB_PASSWORD = $credencial.GetNetworkCredential().Password
    $env:EVENTOS_DEMO_PASSWORD = [Runtime.InteropServices.Marshal]::PtrToStringBSTR($puntero)
    $env:PGPASSWORD = $env:EVENTOS_DB_PASSWORD
    $identidad = & $psql -X -w -v ON_ERROR_STOP=1 -h $HostDb -p $PuertoDb -U $credencial.UserName -d $NombreDb -tAc 'SELECT current_database()'
    if ($LASTEXITCODE -ne 0 -or ($identidad -join '').Trim() -ne $NombreDb) { throw 'La conexión no apunta a la base prevista.' }
    & $Java -cp $jar datos.CargarDatosLab3 --agregar-datos-demo
    if ($LASTEXITCODE -ne 0) { throw 'Falló la carga demo. Revisar el estado de la base antes de repetir.' }
} finally {
    foreach ($nombre in $anterior.Keys) { [Environment]::SetEnvironmentVariable($nombre, $anterior[$nombre], 'Process') }
    [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($puntero)
}
