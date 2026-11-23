Set-StrictMode -Version Latest

function Get-RequisitosManifiesto {
    $archivo = Join-Path $PSScriptRoot 'requirements.psd1'
    $datos = Import-PowerShellDataFile -LiteralPath $archivo
    if ($datos.SchemaVersion -ne 1) { throw 'Versión de requirements.psd1 no admitida.' }
    return $datos
}

function Get-RutasJava([string]$Preferida) {
    $rutas = New-Object 'System.Collections.Generic.List[string]'
    if ($Preferida) { $rutas.Add($Preferida) }
    if ($env:JAVA_HOME) { $rutas.Add((Join-Path $env:JAVA_HOME 'bin/java.exe')) }
    $comando = Get-Command java.exe -ErrorAction SilentlyContinue
    if ($comando) { $rutas.Add($comando.Source) }
    foreach ($raiz in @((Join-Path $env:ProgramFiles 'Eclipse Adoptium'), (Join-Path $env:ProgramFiles 'Java'))) {
        if (Test-Path -LiteralPath $raiz) {
            Get-ChildItem -LiteralPath $raiz -Directory -ErrorAction SilentlyContinue | ForEach-Object {
                $rutas.Add((Join-Path $_.FullName 'bin/java.exe'))
            }
        }
    }
    return @($rutas | Select-Object -Unique)
}

function Get-VersionJava([string]$Ruta) {
    if (!(Test-Path -LiteralPath $Ruta -PathType Leaf)) { return $null }
    $preferencia = $ErrorActionPreference
    $ErrorActionPreference = 'Continue'
    try { $salida = (& $Ruta -version 2>&1 | Out-String); $codigo = $LASTEXITCODE }
    finally { $ErrorActionPreference = $preferencia }
    if ($codigo -ne 0) { return $null }
    if ($salida -match '(?:openjdk|java)(?: version)?\s+"?(\d+(?:\.\d+)*)') { return $Matches[1] }
    return $null
}

function Get-RutasPostgres([string]$Preferida) {
    $rutas = New-Object 'System.Collections.Generic.List[string]'
    if ($Preferida) { $rutas.Add($Preferida) }
    $comando = Get-Command psql.exe -ErrorAction SilentlyContinue
    if ($comando) { $rutas.Add($comando.Source) }
    if ($env:ProgramFiles) {
        $raiz = Join-Path $env:ProgramFiles 'PostgreSQL'
        if (Test-Path -LiteralPath $raiz) {
            Get-ChildItem -LiteralPath $raiz -Directory -ErrorAction SilentlyContinue | ForEach-Object {
                $rutas.Add((Join-Path $_.FullName 'bin/psql.exe'))
            }
        }
    }
    $registro = 'HKLM:\SOFTWARE\PostgreSQL\Installations'
    if (Test-Path $registro) {
        Get-ChildItem $registro -ErrorAction SilentlyContinue | ForEach-Object {
            $item = Get-ItemProperty $_.PSPath
            if ($item.'Base Directory') { $rutas.Add((Join-Path $item.'Base Directory' 'bin/psql.exe')) }
        }
    }
    return @($rutas | Select-Object -Unique)
}

function Get-VersionPostgres([string]$Ruta) {
    if (!(Test-Path -LiteralPath $Ruta -PathType Leaf)) { return $null }
    $salida = (& $Ruta --version 2>&1 | Out-String)
    if ($LASTEXITCODE -ne 0) { return $null }
    if ($salida -match 'PostgreSQL\)\s+(\d+(?:\.\d+)*)') { return $Matches[1] }
    return $null
}

function Get-RutasTomcat([string]$Preferida, [string]$Destino) {
    $rutas = New-Object 'System.Collections.Generic.List[string]'
    if ($Preferida) { $rutas.Add($Preferida) }
    if ($env:CATALINA_HOME) { $rutas.Add($env:CATALINA_HOME) }
    if ($Destino) { $rutas.Add($Destino) }
    return @($rutas | Select-Object -Unique)
}

function Get-VersionTomcat([string]$Ruta) {
    $jar = Join-Path $Ruta 'lib/catalina.jar'
    $bat = Join-Path $Ruta 'bin/catalina.bat'
    if (!(Test-Path -LiteralPath $jar -PathType Leaf) -or !(Test-Path -LiteralPath $bat -PathType Leaf)) { return $null }
    Add-Type -AssemblyName System.IO.Compression.FileSystem
    $archivo = [IO.Compression.ZipFile]::OpenRead($jar)
    try {
        $entrada = $archivo.GetEntry('org/apache/catalina/util/ServerInfo.properties')
        if (!$entrada) { return $null }
        $lector = New-Object IO.StreamReader($entrada.Open())
        try { $texto = $lector.ReadToEnd() } finally { $lector.Dispose() }
        if ($texto -match '(?m)^server\.number=(\d+(?:\.\d+)+)') { return $Matches[1] }
    } finally { $archivo.Dispose() }
    return $null
}

function Get-RequisitosEstado {
    param(
        [ValidateSet('Central','Webapp')][string]$Rol,
        [string]$JavaRuta,
        [string]$PostgresRuta,
        [string]$TomcatRuta,
        [string]$TomcatDestino
    )
    $datos = Get-RequisitosManifiesto
    foreach ($nombre in $datos.Roles[$Rol]) {
        $version = $null
        $ruta = $null
        $candidatos = switch ($nombre) {
            'Java' { Get-RutasJava $JavaRuta }
            'PostgreSQL' { Get-RutasPostgres $PostgresRuta }
            'Tomcat' { Get-RutasTomcat $TomcatRuta $TomcatDestino }
        }
        foreach ($candidato in $candidatos) {
            $actual = switch ($nombre) {
                'Java' { Get-VersionJava $candidato }
                'PostgreSQL' { Get-VersionPostgres $candidato }
                'Tomcat' { Get-VersionTomcat $candidato }
            }
            if (!$actual) { continue }
            if (!$version) { $version = $actual; $ruta = $candidato }
            if ([int]($actual.Split('.')[0]) -eq [int]$datos[$nombre].Major) {
                $version = $actual; $ruta = $candidato; break
            }
        }
        [pscustomobject]@{
            Rol = $Rol
            Componente = $nombre
            Requerido = if ($nombre -eq 'Tomcat') { '11.x' } else { "$($datos[$nombre].Major).x" }
            Version = if ($version) { $version } else { 'No detectado' }
            Ruta = if ($ruta) { $ruta } else { '' }
            Compatible = [bool]($version -and [int]($version.Split('.')[0]) -eq [int]$datos[$nombre].Major)
        }
    }
}

function Test-ReinicioPendiente {
    foreach ($ruta in @('HKLM:\SOFTWARE\Microsoft\Windows\CurrentVersion\WindowsUpdate\Auto Update\RebootRequired',
                       'HKLM:\SOFTWARE\Microsoft\Windows\CurrentVersion\Component Based Servicing\RebootPending')) {
        if (Test-Path $ruta) { return $true }
    }
    $session = Get-ItemProperty 'HKLM:\SYSTEM\CurrentControlSet\Control\Session Manager' -ErrorAction SilentlyContinue
    return [bool]($session -and $session.PSObject.Properties['PendingFileRenameOperations'])
}
