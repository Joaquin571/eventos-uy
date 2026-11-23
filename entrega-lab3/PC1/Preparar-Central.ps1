[CmdletBinding()]
param(
    [Parameter(Mandatory)][string]$HostCentral,
    [string]$Escucha,
    [int]$PuertoCentral = 9128,
    [string]$DbHost = '127.0.0.1',
    [int]$DbPuerto = 5432,
    [string]$DbNombre = 'eventosuy_defensa_demo',
    [Parameter(Mandatory)][string]$DbUsuario,
    [string]$TlsAlmacen = (Join-Path $PSScriptRoot 'certificados/central.p12'),
    [string]$Destino = (Join-Path $PSScriptRoot 'config/central.properties'),
    [switch]$Actualizar,
    [switch]$TlsClaveDistinta
)
$ErrorActionPreference = 'Stop'
if (!$Escucha) { $Escucha = $HostCentral }
if ($HostCentral -match '[/?#@\s]' -or [Uri]::CheckHostName($HostCentral) -eq [UriHostNameType]::Unknown) { throw 'HostCentral inválido.' }
if ($Escucha -ne '0.0.0.0' -and [Uri]::CheckHostName($Escucha) -eq [UriHostNameType]::Unknown) { throw 'Escucha inválida.' }
if ($PuertoCentral -lt 1 -or $PuertoCentral -gt 65535 -or $DbPuerto -lt 1 -or $DbPuerto -gt 65535) { throw 'Puerto inválido.' }
if ($DbNombre -notmatch '^[A-Za-z0-9_]+_demo$') { throw 'La base debe terminar en _demo.' }
$almacen = [IO.Path]::GetFullPath($TlsAlmacen)
if (!(Test-Path -LiteralPath $almacen)) { throw "Falta $almacen. Ejecutar PC1/certificados/Preparar-Certificado.ps1." }
$destinoReal = [IO.Path]::GetFullPath($Destino)
if (Test-Path -LiteralPath $destinoReal) {
    if (!$Actualizar) { Write-Host "Configuración existente conservada: $destinoReal. Usar -Actualizar para respaldar y reemplazar."; return }
    $respaldo = $destinoReal + '.bak-' + (Get-Date -Format 'yyyyMMdd-HHmmss')
    if (Test-Path -LiteralPath $respaldo) { throw "Ya existe $respaldo" }
    Copy-Item -LiteralPath $destinoReal -Destination $respaldo
    Write-Host "Respaldo privado: $respaldo"
}
$plantilla = Join-Path $PSScriptRoot 'config/central.properties.example'
if (!(Test-Path -LiteralPath $plantilla)) { throw "Falta $plantilla" }
function Valor-Propiedad([string]$valor) {
    return $valor.Replace('\','\\').Replace("`r",'\r').Replace("`n",'\n')
}
$claveTls = Read-Host 'Contraseña del PKCS12 de Central' -AsSecureString
$punteroTls = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($claveTls)
$punteroClave = [IntPtr]::Zero
$punteroDb = [IntPtr]::Zero
try {
    if ($TlsClaveDistinta) {
        $secretaClave = Read-Host 'Contraseña de la clave privada TLS' -AsSecureString
        $punteroClave = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($secretaClave)
    }
    $secretaDb = Read-Host 'Contraseña del usuario PostgreSQL de demostración' -AsSecureString
    $punteroDb = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($secretaDb)
    $tlsTexto = [Runtime.InteropServices.Marshal]::PtrToStringBSTR($punteroTls)
    $claveTexto = if ($TlsClaveDistinta) { [Runtime.InteropServices.Marshal]::PtrToStringBSTR($punteroClave) } else { $tlsTexto }
    $dbTexto = [Runtime.InteropServices.Marshal]::PtrToStringBSTR($punteroDb)
    $valores = [ordered]@{
        'central.host' = $HostCentral
        'central.escucha' = $Escucha
        'central.puerto' = [string]$PuertoCentral
        'central.ruta' = '/eventos'
        'central.ruta.sesiones' = '/sesiones'
        'central.ruta.portal' = '/portal'
        'sesiones.duracion.segundos' = '1800'
        'tls.almacen' = $almacen.Replace('\','/')
        'tls.almacen.password' = Valor-Propiedad $tlsTexto
        'tls.clave.password' = Valor-Propiedad $claveTexto
        'db.url' = "jdbc:postgresql://${DbHost}:$DbPuerto/$DbNombre"
        'db.usuario' = Valor-Propiedad $DbUsuario
        'db.password' = Valor-Propiedad $dbTexto
    }
    $texto = Get-Content -LiteralPath $plantilla -Raw
    foreach ($clave in $valores.Keys) {
        $patron = '(?m)^' + [regex]::Escape($clave) + '=.*$'
        if ($texto -notmatch $patron) { throw "La plantilla no contiene $clave" }
        $texto = [regex]::Replace($texto, $patron, { param($coincidencia) $clave + '=' + $valores[$clave] })
    }
    New-Item -ItemType Directory -Force -Path (Split-Path -Parent $destinoReal) | Out-Null
    [IO.File]::WriteAllText($destinoReal, $texto, (New-Object Text.UTF8Encoding($false)))
    Write-Host "Configuración privada creada en $destinoReal; Portal: https://${HostCentral}:$PuertoCentral/portal"
} finally {
    [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($punteroTls)
    if ($punteroClave -ne [IntPtr]::Zero) { [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($punteroClave) }
    if ($punteroDb -ne [IntPtr]::Zero) { [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($punteroDb) }
}
