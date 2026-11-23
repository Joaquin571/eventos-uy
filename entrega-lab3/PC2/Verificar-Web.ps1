[CmdletBinding()]
param(
    [Parameter(Mandatory)][string]$HostCentral,
    [int]$PuertoCentral = 9128,
    [Parameter(Mandatory)][string]$CertificadoPublico,
    [string[]]$Rutas = @('/eventos','/sesiones','/portal'),
    [string]$HostWeb = '127.0.0.1',
    [int]$PuertoWeb = 8080
)
$ErrorActionPreference = 'Stop'
if (!(Test-Path -LiteralPath $CertificadoPublico)) { throw 'Falta el certificado público de Central.' }
$tcp = Test-NetConnection -ComputerName $HostCentral -Port $PuertoCentral -WarningAction SilentlyContinue
if (!$tcp.TcpTestSucceeded) { throw "No hay conexión TCP a ${HostCentral}:$PuertoCentral. Revisar red y firewall." }
foreach ($ruta in $Rutas) {
    if ($ruta -notmatch '^/[A-Za-z0-9_/-]+$') { throw "Ruta inválida: $ruta" }
    $wsdl = "https://${HostCentral}:$PuertoCentral${ruta}?wsdl"
    $contenido = & curl.exe --fail --silent --show-error --cacert $CertificadoPublico $wsdl 2>&1
    if ($LASTEXITCODE -ne 0 -or ($contenido -join "`n") -notmatch 'definitions') { throw "Falló TLS/WSDL en $wsdl : $contenido" }
    Write-Host "OK: $wsdl"
}
$url = "http://${HostWeb}:$PuertoWeb/web/inicio"
try { $respuesta = Invoke-WebRequest -UseBasicParsing -Uri $url -TimeoutSec 15 }
catch { throw "No responde $url. Revisar Tomcat, web.properties y logs: $_" }
if ($respuesta.StatusCode -ne 200) { throw "HTTP $($respuesta.StatusCode) en $url" }
Write-Host "OK: $url (HTTP 200)"
Write-Host "Comprobar también en navegador: http://${HostWeb}:$PuertoWeb/web/movil"
