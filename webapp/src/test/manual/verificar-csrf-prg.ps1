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
    $rutas = @('logica/target/test-classes', 'logica/target/classes',
        'webapp/target/webapp-1.0-SNAPSHOT/WEB-INF/lib/postgresql-42.7.7.jar',
        'webapp/target/webapp-1.0-SNAPSHOT/WEB-INF/lib/jakarta.persistence-api-3.2.0.jar',
        (Join-Path $TomcatRuta 'lib/*'), (Join-Path $TomcatRuta 'bin/tomcat-juli.jar'))
    $carpetaClases = Join-Path $raizProyecto 'webapp/target/manual-csrf-prg'
    New-Item -ItemType Directory -Force -Path $carpetaClases | Out-Null
    $cp = $rutas -join [IO.Path]::PathSeparator
    & (Join-Path $JdkRuta 'bin/javac.exe') -encoding UTF-8 -cp $cp -d $carpetaClases 'webapp/src/test/manual/VerificarCsrfPrgWeb.java'
    if ($LASTEXITCODE -ne 0) { throw 'Falló la compilación del verificador CSRF/PRG.' }
    $ErrorActionPreference = 'Continue' # Tomcat escribe sus mensajes normales en stderr.
    & (Join-Path $JdkRuta 'bin/java.exe') -cp ($carpetaClases + [IO.Path]::PathSeparator + $cp) VerificarCsrfPrgWeb
    if ($LASTEXITCODE -ne 0) { throw 'Falló la integración CSRF/PRG en Tomcat.' }
} finally {
    $env:EVENTOS_DB_NAME = $baseAnterior
    Pop-Location
}
