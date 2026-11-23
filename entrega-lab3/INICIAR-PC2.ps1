[CmdletBinding()]
param(
    [string]$Tomcat,
    [string]$Java,
    [switch]$ConfigurarRutas,
    [switch]$SoloVerificar
)
$ErrorActionPreference = 'Stop'
. (Join-Path $PSScriptRoot 'Arranque-Comun.ps1')
$opciones = Join-Path $PSScriptRoot 'PC2/config/arranque.json'
if ($ConfigurarRutas) {
    $guardar = @{}
    if ($Java) { $guardar.Java = Find-Java25 $Java }
    if ($Tomcat) {
        $Tomcat = Resolve-RutaArranque $Tomcat
        if ((Get-VersionTomcat $Tomcat) -notmatch '^11\.0\.29(?:\.0)?$') { throw 'La ruta indicada no contiene Tomcat 11.0.29.' }
        $guardar.Tomcat = $Tomcat
    }
    if (!$guardar.Count) { throw 'Indicar -Tomcat y/o -Java para guardar las rutas.' }
    Save-OpcionesArranque $opciones $guardar
    Write-Host "Rutas guardadas en $opciones"
    return
}
if (!$Tomcat) { $Tomcat = Get-OpcionArranque $opciones 'Tomcat' }
if (!$Tomcat) { $Tomcat = Join-Path $env:LOCALAPPDATA 'EventosUyTools/apache-tomcat-11.0.29' }
$Tomcat = Resolve-RutaArranque $Tomcat
if ((Get-VersionTomcat $Tomcat) -notmatch '^11\.0\.29(?:\.0)?$') { throw "Tomcat 11.0.29 no encontrado en $Tomcat." }
if (!$Java) { $Java = Get-OpcionArranque $opciones 'Java' }
$Java = Find-Java25 $Java
$javaHome = Split-Path -Parent (Split-Path -Parent $Java)
$warPaquete = Join-Path $PSScriptRoot 'PC2/web.war'
$war = Join-Path $Tomcat 'webapps/web.war'
$config = Join-Path $PSScriptRoot 'PC2/config/web.properties'
$truststore = Join-Path $PSScriptRoot 'PC2/certificados/tomcat-truststore.p12'
foreach ($ruta in @($warPaquete, $war, $config, $truststore)) {
    if (!(Test-Path -LiteralPath $ruta -PathType Leaf)) { throw "Falta $ruta" }
}
if (Test-PuertoTcp '127.0.0.1' 8080) { throw 'El puerto 8080 ya esta ocupado; revisar Tomcat antes de iniciar otro proceso.' }
if ($SoloVerificar) { Write-Host 'PC2: requisitos y rutas verificados. Tomcat no se inicio.'; return }
$anteriorHome = $env:CATALINA_HOME
$anteriorBase = $env:CATALINA_BASE
try {
    $env:CATALINA_HOME = $Tomcat
    $env:CATALINA_BASE = $Tomcat
    & (Join-Path $PSScriptRoot 'PC2/Iniciar-Web.ps1') -Tomcat $Tomcat -JavaHome $javaHome -WebConfig $config -Truststore $truststore
} finally {
    $env:CATALINA_HOME = $anteriorHome
    $env:CATALINA_BASE = $anteriorBase
}
