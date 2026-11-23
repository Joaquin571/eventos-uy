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
if ($NombreDb -notmatch '^[A-Za-z0-9_]+_demo$') { throw 'La base nueva debe terminar en _demo.' }
$psql = Join-Path $PostgresBin 'psql.exe'
if (!$Java) {
    $detectado = @(Get-RequisitosEstado -Rol Central | Where-Object { $_.Componente -eq 'Java' -and $_.Compatible })
    if ($detectado.Count) { $Java = $detectado[0].Ruta }
}
if (!$Java -or (Get-VersionJava $Java) -notlike '25.*') { throw 'Se requiere Java 25. Indicar -Java con la ruta a java.exe.' }
$jar = Join-Path $PSScriptRoot '../PC1/servidor.jar'
if (!(Test-Path -LiteralPath $psql) -or !(Test-Path -LiteralPath $jar)) { throw 'Faltan psql.exe o servidor.jar.' }
$credencial = Get-Credential -UserName $UsuarioDb -Message 'Usuario de la base nueva de demostración'
$anterior = @{}
foreach ($nombre in @('EVENTOS_DB_HOST','EVENTOS_DB_PORT','EVENTOS_DB_NAME','EVENTOS_DB_USER','EVENTOS_DB_PASSWORD','PGPASSWORD')) {
    $anterior[$nombre] = [Environment]::GetEnvironmentVariable($nombre, 'Process')
}
try {
    $env:EVENTOS_DB_HOST = $HostDb
    $env:EVENTOS_DB_PORT = [string]$PuertoDb
    $env:EVENTOS_DB_NAME = $NombreDb
    $env:EVENTOS_DB_USER = $credencial.UserName
    $env:EVENTOS_DB_PASSWORD = $credencial.GetNetworkCredential().Password
    $env:PGPASSWORD = $env:EVENTOS_DB_PASSWORD
    $opciones = @('-X','-w','-v','ON_ERROR_STOP=1','-h',$HostDb,'-p',[string]$PuertoDb,'-U',$credencial.UserName,'-d',$NombreDb)
    $identidad = & $psql @opciones -tAc 'SELECT current_database(), current_schema()'
    if ($LASTEXITCODE -ne 0 -or ($identidad -join '').Trim() -ne "$NombreDb|public") { throw 'La conexión no apunta a la base esperada y al esquema public.' }
    $tablas = & $psql @opciones -tAc "SELECT count(*) FROM information_schema.tables WHERE table_schema=current_schema() AND table_type='BASE TABLE'"
    if ($LASTEXITCODE -ne 0 -or ($tablas -join '').Trim() -ne '0') { throw 'La base no está vacía. No se modifica.' }
    Write-Host "Preparando esquema vacío en $NombreDb (public)."
    & $Java -cp $jar persistencia.PrepararEsquemaNuevo --base-vacia
    if ($LASTEXITCODE -ne 0) { throw 'Falló la preparación JPA. No continuar.' }
    foreach ($archivo in @('seguimiento-usuario.sql','V20261116_02__unicidad_ci.sql')) {
        & $psql @opciones -f (Join-Path $PSScriptRoot $archivo)
        if ($LASTEXITCODE -ne 0) { throw "Falló el SQL $archivo. Revisar la base antes de continuar." }
    }
    Write-Host 'Esquema e índices preparados. Comprobar tablas antes de cargar datos demo.'
} finally {
    foreach ($nombre in $anterior.Keys) { [Environment]::SetEnvironmentVariable($nombre, $anterior[$nombre], 'Process') }
}
