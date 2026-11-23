[CmdletBinding()]
param(
    [Parameter(Mandatory)][string]$HostCentral,
    [int]$Puerto = 9128,
    [Parameter(Mandatory)][string]$CertificadoPublico,
    [string[]]$Rutas = @('/eventos','/sesiones','/portal')
)
$ErrorActionPreference = 'Stop'
if (!(Test-Path -LiteralPath $CertificadoPublico)) { throw 'Falta el certificado público de Central.' }
$tcp = Test-NetConnection -ComputerName $HostCentral -Port $Puerto -WarningAction SilentlyContinue
if (!$tcp.TcpTestSucceeded) { throw "No hay conexión TCP a ${HostCentral}:$Puerto. Revisar escucha y firewall." }
foreach ($ruta in $Rutas) {
    if ($ruta -notmatch '^/[A-Za-z0-9_/-]+$') { throw "Ruta inválida: $ruta" }
    $url = "https://${HostCentral}:$Puerto${ruta}?wsdl"
    $respuesta = & curl.exe --fail --silent --show-error --cacert $CertificadoPublico $url 2>&1
    if ($LASTEXITCODE -ne 0) { throw "Falló TLS/WSDL en $url : $respuesta" }
    if (($respuesta -join "`n") -notmatch 'definitions') { throw "La respuesta de $url no parece ser un WSDL." }
    Write-Host "OK: $url"
}
