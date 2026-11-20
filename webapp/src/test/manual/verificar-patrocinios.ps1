param(
    [Parameter(Mandatory=$true)][string]$TomcatRuta,
    [Parameter(Mandatory=$true)][string]$JdkRuta
)
# El recorrido vigente incluye Central HTTPS; no inicia lógica dentro de Tomcat.
& (Join-Path $PSScriptRoot 'verificar-portal-https.ps1') @PSBoundParameters
