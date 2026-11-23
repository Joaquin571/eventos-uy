[CmdletBinding()]
param(
    [switch]$Actualizar,
    [switch]$VerificarGit
)
$ErrorActionPreference = 'Stop'
$repo = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot '..'))
$entrega = Join-Path $repo 'entrega-lab3'
$manifest = Join-Path $entrega 'documentacion/SHA256SUMS.txt'

function Get-BytesCanonicos([byte[]]$bytes, [string]$nombre) {
    if ($nombre -match '(?i)\.(jar|war)$') { return ,$bytes }
    # Git normaliza CRLF a LF; usar la misma secuencia tanto en Windows como en el blob.
    $salida = New-Object IO.MemoryStream
    try {
        for ($i = 0; $i -lt $bytes.Length; $i++) {
            if ($bytes[$i] -eq 13 -and $i + 1 -lt $bytes.Length -and $bytes[$i + 1] -eq 10) { continue }
            $salida.WriteByte($bytes[$i])
        }
        return ,$salida.ToArray()
    } finally { $salida.Dispose() }
}

function Get-Sha256([byte[]]$bytes) {
    $sha = [Security.Cryptography.SHA256]::Create()
    try { return [BitConverter]::ToString($sha.ComputeHash($bytes)).Replace('-', '').ToLowerInvariant() }
    finally { $sha.Dispose() }
}

function Get-BlobGit([string]$ruta) {
    $inicio = New-Object Diagnostics.ProcessStartInfo
    $inicio.FileName = 'git'
    $inicio.Arguments = 'cat-file blob "HEAD:entrega-lab3/' + $ruta + '"'
    $inicio.WorkingDirectory = $repo
    $inicio.UseShellExecute = $false
    $inicio.CreateNoWindow = $true
    $inicio.RedirectStandardOutput = $true
    $inicio.RedirectStandardError = $true
    $proceso = [Diagnostics.Process]::Start($inicio)
    $salida = New-Object IO.MemoryStream
    try {
        $proceso.StandardOutput.BaseStream.CopyTo($salida)
        $errorGit = $proceso.StandardError.ReadToEnd()
        $proceso.WaitForExit()
        if ($proceso.ExitCode -ne 0) { throw "No se pudo leer el blob Git de $ruta`: $errorGit" }
        return ,$salida.ToArray()
    } finally { $salida.Dispose(); $proceso.Dispose() }
}

$listado = @(& git -C $repo ls-files --cached -- 'entrega-lab3')
if ($LASTEXITCODE -ne 0) { throw 'No se pudo enumerar la entrega registrada en Git.' }
[string[]]$rutas = @($listado | Where-Object {
    $_ -ne 'entrega-lab3/documentacion/SHA256SUMS.txt' -and $_.StartsWith('entrega-lab3/')
} | ForEach-Object { $_.Substring('entrega-lab3/'.Length) })
[Array]::Sort($rutas, [StringComparer]::Ordinal)
if ($rutas.Count -eq 0) { throw 'No se encontraron archivos de entrega registrados en Git.' }

if ($Actualizar) {
    $lineas = @(
        foreach ($ruta in $rutas) {
            $archivo = Join-Path $entrega $ruta
            if (!(Test-Path -LiteralPath $archivo -PathType Leaf)) { throw "Falta $ruta" }
            $bytes = Get-BytesCanonicos ([IO.File]::ReadAllBytes($archivo)) $ruta
            (Get-Sha256 $bytes) + '  ' + $ruta
        }
    )
    [IO.File]::WriteAllLines($manifest, [string[]]$lineas, (New-Object Text.UTF8Encoding($false)))
}

if (!(Test-Path -LiteralPath $manifest -PathType Leaf)) { throw 'Falta SHA256SUMS.txt.' }
$esperados = @{}
foreach ($linea in [IO.File]::ReadAllLines($manifest, [Text.Encoding]::UTF8)) {
    if ($linea -notmatch '^([0-9a-f]{64})  (.+)$') { throw "Línea inválida en SHA256SUMS.txt: $linea" }
    if ($esperados.ContainsKey($Matches[2])) { throw "Archivo repetido en el manifiesto: $($Matches[2])" }
    $esperados[$Matches[2]] = $Matches[1]
}
if ($esperados.Count -ne $rutas.Count) { throw "Se esperaban $($rutas.Count) hashes y hay $($esperados.Count)." }
foreach ($ruta in $rutas) {
    if (!$esperados.ContainsKey($ruta)) { throw "Falta $ruta en el manifiesto." }
    $archivo = Join-Path $entrega $ruta
    if (!(Test-Path -LiteralPath $archivo -PathType Leaf)) { throw "Falta $ruta" }
    $actual = Get-Sha256 (Get-BytesCanonicos ([IO.File]::ReadAllBytes($archivo)) $ruta)
    if ($actual -ne $esperados[$ruta]) { throw "El archivo local no coincide: $ruta" }
    if ($VerificarGit) {
        $blob = Get-BlobGit $ruta
        $gitHash = Get-Sha256 (Get-BytesCanonicos $blob $ruta)
        if ($gitHash -ne $esperados[$ruta]) { throw "El blob publicado en Git no coincide: $ruta" }
    }
}
Write-Host "$($rutas.Count) archivos verificados (texto LF canónico; JAR/WAR binarios intactos)."
