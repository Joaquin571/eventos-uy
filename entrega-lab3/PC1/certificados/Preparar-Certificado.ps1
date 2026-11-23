[CmdletBinding()]
param(
    [Parameter(Mandatory)][string]$HostCentral,
    [string[]]$OtrosSan = @(),
    [string]$Java,
    [int]$VigenciaDias = 365
)
$ErrorActionPreference = 'Stop'
. (Join-Path $PSScriptRoot '../../Requisitos-Comunes.ps1')
if (!$Java) {
    $detectado = @(Get-RequisitosEstado -Rol Central | Where-Object { $_.Componente -eq 'Java' -and $_.Compatible })
    if ($detectado.Count) { $Java = $detectado[0].Ruta }
}
if (!$Java -or (Get-VersionJava $Java) -notlike '25.*') { throw 'Se requiere Java 25. Indicar -Java con la ruta a java.exe.' }
$keytool = Join-Path (Split-Path -Parent (Split-Path -Parent $Java)) 'bin/keytool.exe'
if (!(Test-Path -LiteralPath $keytool)) { throw "Falta $keytool" }
$tipoHost = [Uri]::CheckHostName($HostCentral)
if ($tipoHost -eq [UriHostNameType]::Unknown) { throw 'HostCentral inválido.' }
$san = @($(if ($tipoHost -eq [UriHostNameType]::IPv4) { "IP:$HostCentral" } else { "DNS:$HostCentral" }))
foreach ($otro in $OtrosSan) {
    if ($otro -notmatch '^(IP|DNS):[A-Za-z0-9.-]+$') { throw "SAN inválido: $otro" }
    $san += $otro
}
if ($VigenciaDias -lt 1) { throw 'VigenciaDias debe ser positivo.' }
$almacen = Join-Path $PSScriptRoot 'central.p12'
$publico = Join-Path $PSScriptRoot 'central-publico.cer'
if ((Test-Path -LiteralPath $almacen) -or (Test-Path -LiteralPath $publico)) { throw 'Ya existe TLS de Central. No se sobrescribe.' }
$segura = Read-Host 'Contraseña nueva del PKCS12 de Central' -AsSecureString
$puntero = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($segura)
$anterior = $env:EVENTOS_TLS_PASSWORD
try {
    $env:EVENTOS_TLS_PASSWORD = [Runtime.InteropServices.Marshal]::PtrToStringBSTR($puntero)
    $preferencia = $ErrorActionPreference
    $ErrorActionPreference = 'Continue'
    try {
        & $keytool -genkeypair -alias central -keyalg RSA -keysize 3072 -sigalg SHA256withRSA -validity $VigenciaDias -storetype PKCS12 -keystore $almacen -dname 'CN=Eventos UY Central, O=Equipo, C=UY' -ext ('SAN=' + ($san -join ',')) -storepass:env EVENTOS_TLS_PASSWORD -keypass:env EVENTOS_TLS_PASSWORD
        $codigo = $LASTEXITCODE
        if ($codigo -eq 0) {
            & $keytool -exportcert -rfc -alias central -keystore $almacen -file $publico -storepass:env EVENTOS_TLS_PASSWORD
            $codigo = $LASTEXITCODE
        }
    } finally { $ErrorActionPreference = $preferencia }
    if ($codigo -ne 0) { throw 'Falló keytool. Revisar los archivos parciales antes de repetir.' }
} finally {
    $env:EVENTOS_TLS_PASSWORD = $anterior
    [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($puntero)
}
Write-Host "PKCS12 privado: $almacen"
Write-Host "Certificado público para PC2: $publico"
Write-Host "SHA256 del archivo público: $((Get-FileHash -Algorithm SHA256 -LiteralPath $publico).Hash)"
& $keytool -printcert -file $publico
