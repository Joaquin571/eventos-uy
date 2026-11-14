param(
    [Parameter(Mandatory=$true)][string]$TomcatRuta,
    [Parameter(Mandatory=$true)][string]$JdkRuta
)
$ErrorActionPreference = 'Stop'
$raizProyecto = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot '../../../..'))
$baseAnterior = $env:EVENTOS_DB_NAME
Push-Location $raizProyecto
try {
    $env:EVENTOS_DB_NAME = 'eventosuy_test'
    $informe = [xml](Get-Content 'logica/target/surefire-reports/TEST-implementacion.LoginTest.xml' -Raw)
    $clasesLogica = ($informe.testsuite.properties.property | Where-Object name -eq 'java.class.path').value
    if (-not $clasesLogica) { throw 'Ejecutá primero las pruebas JUnit en eventosuy_test para obtener el classpath.' }
    $repositorioMaven = Join-Path $env:USERPROFILE '.m2/repository'
    $rutas = @($clasesLogica, (Join-Path $raizProyecto 'webapp/target/classes'),
        (Join-Path $TomcatRuta 'lib/*'), (Join-Path $TomcatRuta 'bin/tomcat-juli.jar'),
        (Join-Path $repositorioMaven 'jakarta/servlet/jsp/jstl/jakarta.servlet.jsp.jstl-api/3.0.2/jakarta.servlet.jsp.jstl-api-3.0.2.jar'),
        (Join-Path $repositorioMaven 'org/glassfish/web/jakarta.servlet.jsp.jstl/3.0.1/jakarta.servlet.jsp.jstl-3.0.1.jar'))
    $ErrorActionPreference = 'Continue'
    & (Join-Path $JdkRuta 'bin/java.exe') --class-path ($rutas -join [IO.Path]::PathSeparator) 'webapp/src/test/manual/VerificarPatrociniosWeb.java'
    if ($LASTEXITCODE -ne 0) { throw 'Falló la integración HTTP/JSP/PostgreSQL.' }
} finally {
    $env:EVENTOS_DB_NAME = $baseAnterior
    Pop-Location
}
