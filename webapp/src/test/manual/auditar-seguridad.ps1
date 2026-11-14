param(
    [Parameter(Mandatory=$true)][string]$TomcatRuta,
    [Parameter(Mandatory=$true)][string]$JdkRuta
)
$ErrorActionPreference='Stop'
$raizProyecto=[IO.Path]::GetFullPath((Join-Path $PSScriptRoot '../../../..'))
$baseAnterior=$env:EVENTOS_DB_NAME
Push-Location $raizProyecto
try {
    $env:EVENTOS_DB_NAME='eventosuy_test'
    $cp=@('logica/target/test-classes','logica/target/classes',
        'webapp/target/webapp-1.0-SNAPSHOT/WEB-INF/lib/postgresql-42.7.7.jar',
        'webapp/target/webapp-1.0-SNAPSHOT/WEB-INF/lib/jakarta.persistence-api-3.2.0.jar',
        (Join-Path $TomcatRuta 'lib/*'),(Join-Path $TomcatRuta 'bin/tomcat-juli.jar')) -join [IO.Path]::PathSeparator
    $clases='webapp/target/auditoria-seguridad'
    New-Item -ItemType Directory -Force -Path $clases | Out-Null
    & (Join-Path $JdkRuta 'bin/javac.exe') -encoding UTF-8 -cp $cp -d $clases webapp/src/test/manual/VerificarCsrfPrgWeb.java webapp/src/test/manual/AuditarSeguridadWeb.java
    if($LASTEXITCODE -ne 0){throw 'Falló compilación de auditoría.'}
    $ErrorActionPreference='Continue'
    & (Join-Path $JdkRuta 'bin/java.exe') -cp ($clases+[IO.Path]::PathSeparator+$cp) AuditarSeguridadWeb
    if($LASTEXITCODE -ne 0){throw 'Auditoría con expectativas fallidas; revisar el informe antes de cambiar producción.'}
} finally {$env:EVENTOS_DB_NAME=$baseAnterior;Pop-Location}
