[CmdletBinding(DefaultParameterSetName='Validar')]
param(
    [Parameter(ParameterSetName='Cargar',Mandatory=$true)][switch]$Cargar,
    [Parameter(ParameterSetName='Verificar',Mandatory=$true)][switch]$Verificar,
    [string]$PostgresBin,
    [string]$Java
)
$ErrorActionPreference = 'Stop'
. (Join-Path $PSScriptRoot '../Requisitos-Comunes.ps1')
$config = Join-Path $PSScriptRoot '../PC1/config/central.properties'
$jar = Join-Path $PSScriptRoot '../PC1/servidor.jar'
$fuente = Join-Path $PSScriptRoot 'CargarDatosDefensa.java'
if (!(Test-Path -LiteralPath $config -PathType Leaf)) { throw 'Falta PC1/config/central.properties.' }
if (!(Test-Path -LiteralPath $jar -PathType Leaf) -or !(Test-Path -LiteralPath $fuente -PathType Leaf)) {
    throw 'Faltan servidor.jar o CargarDatosDefensa.java.'
}
if (!$PostgresBin) {
    $PostgresBin = Join-Path $env:ProgramFiles 'PostgreSQL/17/bin'
}
$psql = Join-Path $PostgresBin 'psql.exe'
$dump = Join-Path $PostgresBin 'pg_dump.exe'
$restore = Join-Path $PostgresBin 'pg_restore.exe'
foreach ($programa in @($psql,$dump,$restore)) {
    if (!(Test-Path -LiteralPath $programa -PathType Leaf)) { throw "No se encontró $programa. Indicar -PostgresBin." }
    $version = (& $programa --version 2>&1) -join ' '
    if ($LASTEXITCODE -ne 0 -or $version -notmatch '\b17\.\d+') { throw "Se requiere PostgreSQL 17: $programa" }
}
if (!$Java) {
    $detectado = @(Get-RequisitosEstado -Rol Central | Where-Object { $_.Componente -eq 'Java' -and $_.Compatible })
    if ($detectado.Count) { $Java = $detectado[0].Ruta }
}
if (!$Java -or (Get-VersionJava $Java) -notlike '25.*') { throw 'Se requiere Java 25. Indicar -Java con la ruta a java.exe.' }
$lineas = Get-Content -LiteralPath $config -Encoding UTF8
$valores = @{}
foreach ($linea in $lineas) {
    if ($linea -match '^\s*(db\.url|db\.usuario|db\.password)=(.*)$') { $valores[$Matches[1]] = $Matches[2] }
}
foreach ($clave in @('db.url','db.usuario','db.password')) {
    if (!$valores.ContainsKey($clave) -or [string]::IsNullOrWhiteSpace($valores[$clave])) { throw "Falta $clave en la configuración de Central." }
}
if ($valores['db.url'] -notmatch '^jdbc:postgresql://([^/:]+):(\d+)/([A-Za-z0-9_]+)$') {
    throw 'db.url no tiene un formato PostgreSQL compatible.'
}
$hostDb = $Matches[1]; $puerto = $Matches[2]; $nombreDb = $Matches[3]
if ($nombreDb -ne 'eventosuy_demo') { throw 'La configuración no apunta a eventosuy_demo.' }
$usuario = $valores['db.usuario']
$anterior = @{}
foreach ($nombre in @('EVENTOS_DB_HOST','EVENTOS_DB_PORT','EVENTOS_DB_NAME','EVENTOS_DB_USER','EVENTOS_DB_PASSWORD','EVENTOS_DEMO_PASSWORD','PGPASSWORD')) {
    $anterior[$nombre] = [Environment]::GetEnvironmentVariable($nombre,'Process')
}
try {
    $env:EVENTOS_DB_HOST = $hostDb
    $env:EVENTOS_DB_PORT = $puerto
    $env:EVENTOS_DB_NAME = $nombreDb
    $env:EVENTOS_DB_USER = $usuario
    $env:EVENTOS_DB_PASSWORD = $valores['db.password']
    $env:PGPASSWORD = $valores['db.password']
    $conexion = @('-X','-w','-v','ON_ERROR_STOP=1','-h',$hostDb,'-p',$puerto,'-U',$usuario,'-d',$nombreDb)
    $identidad = & $psql @conexion -tAc 'select current_database()'
    if ($LASTEXITCODE -ne 0 -or ($identidad -join '').Trim() -ne $nombreDb) { throw 'No se pudo verificar la base efectiva.' }
    $modo = '--validar'
    if ($Verificar) { $modo = '--verificar' }
    if ($Cargar) {
        $respaldoDir = Join-Path $PSScriptRoot 'respaldos'
        New-Item -ItemType Directory -Path $respaldoDir -Force | Out-Null
        $respaldo = Join-Path $respaldoDir ("eventosuy_demo_{0}.dump" -f (Get-Date -Format 'yyyyMMdd_HHmmss_fff'))
        if (Test-Path -LiteralPath $respaldo) { throw 'Ya existe un respaldo con ese nombre. Reintentar más tarde.' }
        & $dump -Fc -w -h $hostDb -p $puerto -U $usuario -f $respaldo $nombreDb
        if ($LASTEXITCODE -ne 0 -or !(Test-Path -LiteralPath $respaldo) -or (Get-Item -LiteralPath $respaldo).Length -lt 1024) {
            throw 'Falló el respaldo. No se cargaron datos.'
        }
        $catalogo = & $restore -l $respaldo
        if ($LASTEXITCODE -ne 0 -or ($catalogo -join "`n") -notmatch 'TABLE DATA') { throw 'El respaldo no pasó la verificación de catálogo. No se cargaron datos.' }
        $hash = (Get-FileHash -LiteralPath $respaldo -Algorithm SHA256).Hash
        Write-Host "Respaldo verificado: $respaldo"
        Write-Host "SHA-256: $hash"
        $segura = Read-Host 'Clave nueva para las cuentas ficticias' -AsSecureString
        $puntero = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($segura)
        try { $env:EVENTOS_DEMO_PASSWORD = [Runtime.InteropServices.Marshal]::PtrToStringBSTR($puntero) }
        finally { [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($puntero) }
        if ([string]::IsNullOrWhiteSpace($env:EVENTOS_DEMO_PASSWORD)) { throw 'La clave demo no puede estar vacía.' }
        $modo = '--cargar'
    }
    & $Java --class-path $jar $fuente $modo
    if ($LASTEXITCODE -ne 0) { throw "Falló $modo. Conservar el respaldo y revisar la base antes de repetir." }
} finally {
    foreach ($nombre in $anterior.Keys) { [Environment]::SetEnvironmentVariable($nombre,$anterior[$nombre],'Process') }
}
