param(
    [Parameter(Mandatory=$true)][string]$TomcatRuta,
    [Parameter(Mandatory=$true)][string]$JdkRuta
)
$ErrorActionPreference = 'Stop'
$raizProyecto = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot '../../../..'))
$javaAnterior = $env:JAVA_HOME
$pathAnterior = $env:PATH
$tomcatAnterior = $env:TOMCAT_HOME
$baseAnterior = $env:EVENTOS_DB_NAME
Push-Location $raizProyecto
try {
    if (-not (Test-Path -LiteralPath (Join-Path $TomcatRuta 'bin/bootstrap.jar'))) { throw 'Se requiere Tomcat 11 compilado.' }
    if (-not (Test-Path -LiteralPath (Join-Path $JdkRuta 'bin/java.exe'))) { throw 'La ruta del JDK no contiene java.exe.' }
    $env:JAVA_HOME = $JdkRuta
    $env:PATH = (Join-Path $JdkRuta 'bin') + [IO.Path]::PathSeparator + $pathAnterior
    $env:TOMCAT_HOME = [IO.Path]::GetFullPath($TomcatRuta)
    $env:EVENTOS_DB_NAME = 'eventosuy_test'
    $ErrorActionPreference = 'Continue'
    & mvn -DskipTests package
    $ErrorActionPreference = 'Stop'
    if ($LASTEXITCODE -ne 0) { throw 'Falló el empaquetado del reactor.' }
    # La IT comprueba current_database() por JDBC ANTES de inicializar JPA.
    # Usa un CATALINA_BASE temporal; nunca modifica la instalación indicada.
    $ErrorActionPreference = 'Continue'
    & mvn '-Dtest=TomcatPortalHttpsIT' '-Dsurefire.failIfNoSpecifiedTests=false' test
    $ErrorActionPreference = 'Stop'
    if ($LASTEXITCODE -ne 0) { throw 'Falló el recorrido HTTP -> SOAP/HTTPS -> Central.' }
} finally {
    $env:JAVA_HOME = $javaAnterior
    $env:PATH = $pathAnterior
    $env:TOMCAT_HOME = $tomcatAnterior
    $env:EVENTOS_DB_NAME = $baseAnterior
    Pop-Location
}
