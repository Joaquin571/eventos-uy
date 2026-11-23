Set-StrictMode -Version Latest
. (Join-Path $PSScriptRoot 'Requisitos-Comunes.ps1')

function Resolve-RutaArranque([string]$Ruta) {
    if ([IO.Path]::IsPathRooted($Ruta)) { return [IO.Path]::GetFullPath($Ruta) }
    return [IO.Path]::GetFullPath((Join-Path $PSScriptRoot $Ruta))
}

function Get-OpcionArranque([string]$Archivo, [string]$Nombre) {
    if (!(Test-Path -LiteralPath $Archivo -PathType Leaf)) { return $null }
    $datos = Get-Content -LiteralPath $Archivo -Raw | ConvertFrom-Json
    if ($datos.PSObject.Properties[$Nombre]) { return [string]$datos.$Nombre }
    return $null
}

function Save-OpcionesArranque([string]$Archivo, [hashtable]$Opciones) {
    $datos = @{}
    if (Test-Path -LiteralPath $Archivo -PathType Leaf) {
        $actual = Get-Content -LiteralPath $Archivo -Raw | ConvertFrom-Json
        foreach ($propiedad in $actual.PSObject.Properties) { $datos[$propiedad.Name] = $propiedad.Value }
    }
    foreach ($nombre in $Opciones.Keys) {
        if ($Opciones[$nombre]) { $datos[$nombre] = Resolve-RutaArranque $Opciones[$nombre] }
    }
    $directorio = Split-Path -Parent $Archivo
    if (!(Test-Path -LiteralPath $directorio)) { New-Item -ItemType Directory -Path $directorio -Force | Out-Null }
    [IO.File]::WriteAllText($Archivo, ($datos | ConvertTo-Json), (New-Object Text.UTF8Encoding($false)))
}

function Find-Java25([string]$Preferida) {
    $rutas = if ($Preferida) { @(Resolve-RutaArranque $Preferida) } else { @(Get-RutasJava '') }
    foreach ($ruta in $rutas) {
        if ((Get-VersionJava $ruta) -like '25.*') { return [IO.Path]::GetFullPath($ruta) }
    }
    throw 'No se encontro Java 25. Instalarlo o configurar la ruta a java.exe con -ConfigurarRutas.'
}

function Test-PuertoTcp([string]$HostDestino, [int]$Puerto) {
    $cliente = New-Object Net.Sockets.TcpClient
    try {
        $conexion = $cliente.BeginConnect($HostDestino, $Puerto, $null, $null)
        if (!$conexion.AsyncWaitHandle.WaitOne(1500)) { return $false }
        try { $cliente.EndConnect($conexion); return $true } catch { return $false }
    } catch { return $false }
    finally { $cliente.Close() }
}

function Assert-PostgresDisponible([string]$HostDestino, [int]$Puerto) {
    $candidatos = @(Get-RutasPostgres '' | Where-Object { (Get-VersionPostgres $_) -like '17.*' })
    if (!$candidatos.Count) { throw 'No se encontro PostgreSQL 17 (psql.exe y pg_isready.exe).' }
    $ready = Join-Path (Split-Path -Parent $candidatos[0]) 'pg_isready.exe'
    if (!(Test-Path -LiteralPath $ready -PathType Leaf)) { throw "Falta $ready" }
    & $ready -h $HostDestino -p $Puerto -t 2 2>&1 | Out-Null
    if ($LASTEXITCODE -ne 0) { throw "PostgreSQL no responde en ${HostDestino}:$Puerto." }
}
