[CmdletBinding()]
param(
    [Parameter(Mandatory)][string]$HostCentral,
    [string]$Truststore = (Join-Path $PSScriptRoot 'certificados/tomcat-truststore.p12'),
    [int]$Puerto = 9128,
    [string]$RutaPortal = '/portal',
    [string]$JavaHome
)
$ErrorActionPreference = 'Stop'
. (Join-Path $PSScriptRoot '../Requisitos-Comunes.ps1')
if (!(Test-Path -LiteralPath $Truststore)) { throw "Falta $Truststore" }
if ($HostCentral -match '[/?#@\s]' -or [Uri]::CheckHostName($HostCentral) -eq [UriHostNameType]::Unknown) { throw 'HostCentral inválido.' }
if ($Puerto -lt 1 -or $Puerto -gt 65535 -or $RutaPortal -notmatch '^/[A-Za-z0-9_/-]+$') { throw 'Puerto o ruta inválidos.' }
$java = if ($JavaHome) { Join-Path $JavaHome 'bin/java.exe' } else {
    $detectado = @(Get-RequisitosEstado -Rol Webapp | Where-Object { $_.Componente -eq 'Java' -and $_.Compatible })
    if (!$detectado.Count) { throw 'Java 25 no encontrado. Indicar -JavaHome.' }
    $detectado[0].Ruta
}
if ((Get-VersionJava $java) -notlike '25.*') { throw 'Se requiere Java 25.' }
$origen = Join-Path $PSScriptRoot 'DiagnosticoHttps.java'
if (!(Test-Path -LiteralPath $origen)) { throw "Falta $origen" }
$clave = Read-Host 'Contraseña del truststore PKCS12' -AsSecureString
$puntero = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($clave)
$anterior = $env:EVENTOS_TLS_PASSWORD
try {
    $env:EVENTOS_TLS_PASSWORD = [Runtime.InteropServices.Marshal]::PtrToStringBSTR($puntero)
    $url = "https://${HostCentral}:$Puerto${RutaPortal}?wsdl"
    $preferencia = $ErrorActionPreference
    $ErrorActionPreference = 'Continue'
    try { & $java $origen $url $Truststore; $codigo = $LASTEXITCODE }
    finally { $ErrorActionPreference = $preferencia }
    if ($codigo -ne 0) { throw "Falló la verificación HTTPS de Java en $url. Revisar PKIX, SAN y truststore." }
} finally {
    $env:EVENTOS_TLS_PASSWORD = $anterior
    [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($puntero)
}
