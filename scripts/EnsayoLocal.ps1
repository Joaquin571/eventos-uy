[CmdletBinding()]
param(
    [ValidateSet('Iniciar', 'Detener', 'Estado')][string]$Accion = 'Iniciar',
    [string]$JdkRuta = 'C:\Program Files\JetBrains\IntelliJ IDEA 2026.2.0.1\jbr',
    [string]$PostgresRuta = 'C:\Program Files\PostgreSQL\17',
    [string]$TomcatRuta = "$env:LOCALAPPDATA\Programs\Apache Tomcat\apache-tomcat-11.0.29",
    [ValidatePattern('^(?:[0-9]{1,3}\.){3}[0-9]{1,3}$')][string]$DireccionWeb = '127.0.0.1',
    [switch]$SinSwing,
    [switch]$Esperar
)
$ErrorActionPreference = 'Stop'
$raizEnsayo = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot '..'))
$baseEnsayo = Join-Path $raizEnsayo '.build-cache/ensayo-local'
$estadoEnsayo = Join-Path $baseEnsayo 'estado.json'
$privadoEnsayo = Join-Path $baseEnsayo 'usuario/.eventosUy'
$dbEnsayo = Join-Path $baseEnsayo 'postgres'
$nombreDbEnsayo = 'eventosuy_ensayo_demo'

function Clave-Nueva {
    $bytesEnsayo = New-Object byte[] 24
    $generadorEnsayo = [Security.Cryptography.RandomNumberGenerator]::Create()
    try { $generadorEnsayo.GetBytes($bytesEnsayo) } finally { $generadorEnsayo.Dispose() }
    return [BitConverter]::ToString($bytesEnsayo).Replace('-', '')
}
function Escribir-Utf8([string]$Ruta, [string]$Texto) {
    [IO.File]::WriteAllText($Ruta, $Texto, (New-Object Text.UTF8Encoding($false)))
}
function Puerto-Libre {
    $sondaEnsayo = [Net.Sockets.TcpListener]::new([Net.IPAddress]::Loopback, 0)
    $sondaEnsayo.Start()
    try { return $sondaEnsayo.LocalEndpoint.Port } finally { $sondaEnsayo.Stop() }
}
function Proceso-Propio($Datos) {
    if (!$Datos) { return $null }
    $procesoEnsayo = Get-Process -Id $Datos.Id -ErrorAction SilentlyContinue
    if ($procesoEnsayo -and $procesoEnsayo.StartTime.ToUniversalTime().Ticks.ToString() -eq $Datos.Inicio) {
        return $procesoEnsayo
    }
    return $null
}
function Registrar-Proceso($Proceso) {
    return @{ Id = $Proceso.Id; Inicio = $Proceso.StartTime.ToUniversalTime().Ticks.ToString() }
}
function Guardar-Estado {
    Escribir-Utf8 $estadoEnsayo ($script:datosEnsayo | ConvertTo-Json -Depth 5)
}
function Iniciar-Proceso([string]$Nombre, [string]$Ejecutable, [string[]]$Argumentos) {
    $procesoEnsayo = Start-Process -FilePath $Ejecutable -ArgumentList $Argumentos -WorkingDirectory $raizEnsayo `
        -WindowStyle Hidden -PassThru -RedirectStandardOutput (Join-Path $baseEnsayo "$Nombre.log") `
        -RedirectStandardError (Join-Path $baseEnsayo "$Nombre-error.log")
    $script:datosEnsayo[$Nombre] = Registrar-Proceso $procesoEnsayo
    Guardar-Estado
    return $procesoEnsayo
}
function Comprobar-Salida([string]$Operacion) {
    if ($LASTEXITCODE -ne 0) { throw "Falló $Operacion. No se continuará con las operaciones siguientes." }
}
function Mostrar-Estado {
    if (!(Test-Path -LiteralPath $estadoEnsayo)) { Write-Output 'No hay ensayo local preparado.'; return }
    $actualEnsayo = Get-Content -Raw $estadoEnsayo | ConvertFrom-Json
    foreach ($servicioEnsayo in @('postgres', 'central', 'web')) {
        $vivoEnsayo = [bool](Proceso-Propio $actualEnsayo.$servicioEnsayo)
        Write-Output ($servicioEnsayo + ': ' + $(if ($vivoEnsayo) { 'activo' } else { 'detenido' }))
    }
    Write-Output ('Web: http://' + $actualEnsayo.HostWeb + ':' + $actualEnsayo.PuertoWeb + '/web/inicio')
    Write-Output ('Móvil: http://' + $actualEnsayo.HostWeb + ':' + $actualEnsayo.PuertoWeb + '/web/movil')
    Write-Output ('Usuarios y clave en el archivo privado: ' + (Join-Path $privadoEnsayo 'acceso-demo.properties'))
}

if ($Accion -eq 'Estado') { Mostrar-Estado; exit 0 }
if ($Accion -eq 'Detener') {
    if (!(Test-Path -LiteralPath $estadoEnsayo)) { Write-Output 'No hay procesos de ensayo registrados.'; exit 0 }
    $actualEnsayo = Get-Content -Raw $estadoEnsayo | ConvertFrom-Json
    foreach ($servicioEnsayo in @('web', 'central')) {
        $procesoEnsayo = Proceso-Propio $actualEnsayo.$servicioEnsayo
        if ($procesoEnsayo) { Stop-Process -Id $procesoEnsayo.Id; Write-Output "$servicioEnsayo detenido." }
    }
    if (Proceso-Propio $actualEnsayo.postgres) {
        & (Join-Path $PostgresRuta 'bin/pg_ctl.exe') -D $dbEnsayo -m fast -w stop
        Comprobar-Salida 'la parada del PostgreSQL de ensayo'
    }
    Write-Output 'Datos y configuración conservados. No se detuvieron servicios ajenos.'
    exit 0
}

foreach ($archivoEnsayo in @((Join-Path $JdkRuta 'bin/java.exe'), (Join-Path $JdkRuta 'bin/keytool.exe'),
        (Join-Path $PostgresRuta 'bin/initdb.exe'), (Join-Path $TomcatRuta 'bin/bootstrap.jar'),
        (Join-Path $raizEnsayo 'servidor-central/target/servidor.jar'), (Join-Path $raizEnsayo 'webapp/target/web.war'))) {
    if (!(Test-Path -LiteralPath $archivoEnsayo)) { throw "No se encontró $archivoEnsayo. Ajustar las rutas o construir los paquetes." }
}
$javaEnsayo = Join-Path $JdkRuta 'bin/java.exe'
$versionEnsayo = (& $javaEnsayo --version | Out-String)
if ($versionEnsayo -notmatch '(?m)^(?:openjdk|java) 25(?:[. ]|$)') { throw 'El ensayo requiere Java 25.' }
New-Item -ItemType Directory -Force $baseEnsayo, $privadoEnsayo | Out-Null

$script:datosEnsayo = @{}
if (Test-Path -LiteralPath $estadoEnsayo) {
    $anteriorEnsayo = Get-Content -Raw $estadoEnsayo | ConvertFrom-Json
    foreach ($propiedadEnsayo in $anteriorEnsayo.PSObject.Properties) { $script:datosEnsayo[$propiedadEnsayo.Name] = $propiedadEnsayo.Value }
    if ((Proceso-Propio $anteriorEnsayo.central) -or (Proceso-Propio $anteriorEnsayo.web)) {
        throw 'El ensayo ya está activo o quedó iniciado parcialmente. Consultar Estado o ejecutar Detener antes de iniciarlo otra vez.'
    }
}
if (!$script:datosEnsayo.PuertoDb) { $script:datosEnsayo.PuertoDb = Puerto-Libre }
$script:datosEnsayo.PuertoCentral = Puerto-Libre
$script:datosEnsayo.PuertoWeb = Puerto-Libre
$script:datosEnsayo.HostWeb = $DireccionWeb
Guardar-Estado

$archivoClavesEnsayo = Join-Path $privadoEnsayo 'claves.properties'
if (Test-Path -LiteralPath $archivoClavesEnsayo) {
    $clavesEnsayo = Get-Content -Raw $archivoClavesEnsayo | ConvertFrom-StringData
} else {
    $clavesEnsayo = @{ db = Clave-Nueva; tls = Clave-Nueva; demo = Clave-Nueva }
    Escribir-Utf8 $archivoClavesEnsayo ("db=" + $clavesEnsayo.db + "`ntls=" + $clavesEnsayo.tls + "`ndemo=" + $clavesEnsayo.demo + "`n")
}
$variablesEnsayo = @('EVENTOS_DB_HOST','EVENTOS_DB_PORT','EVENTOS_DB_NAME','EVENTOS_DB_USER','EVENTOS_DB_PASSWORD',
    'EVENTOS_DEMO_PASSWORD','PGPASSWORD','ENSAYO_TLS_PASSWORD')
$entornoAnteriorEnsayo = @{}
foreach ($variableEnsayo in $variablesEnsayo) { $entornoAnteriorEnsayo[$variableEnsayo] = [Environment]::GetEnvironmentVariable($variableEnsayo, 'Process') }
try {
    $env:EVENTOS_DB_HOST = '127.0.0.1'; $env:EVENTOS_DB_PORT = [string]$script:datosEnsayo.PuertoDb
    $env:EVENTOS_DB_NAME = $nombreDbEnsayo; $env:EVENTOS_DB_USER = 'ensayo'
    $env:EVENTOS_DB_PASSWORD = $clavesEnsayo.db; $env:PGPASSWORD = $clavesEnsayo.db
    $env:EVENTOS_DEMO_PASSWORD = $clavesEnsayo.demo; $env:ENSAYO_TLS_PASSWORD = $clavesEnsayo.tls
    if (!(Test-Path -LiteralPath (Join-Path $dbEnsayo 'PG_VERSION'))) {
        $claveInicialEnsayo = Join-Path $privadoEnsayo 'inicializacion-db.txt'
        Escribir-Utf8 $claveInicialEnsayo ($clavesEnsayo.db + "`n")
        try {
            $ErrorActionPreference = 'Continue'
            & (Join-Path $PostgresRuta 'bin/initdb.exe') -D $dbEnsayo -U ensayo --auth-host=scram-sha-256 --auth-local=trust `
                --encoding=UTF8 --locale=C ("--pwfile=" + $claveInicialEnsayo) *> (Join-Path $baseEnsayo 'initdb.log')
            $ErrorActionPreference = 'Stop'
            Comprobar-Salida 'la creación del clúster de ensayo'
        } finally { Remove-Item -LiteralPath $claveInicialEnsayo -ErrorAction SilentlyContinue }
    }
    if (!(Proceso-Propio $script:datosEnsayo.postgres)) {
        Iniciar-Proceso 'postgres' (Join-Path $PostgresRuta 'bin/postgres.exe') @('-D', ('"' + $dbEnsayo + '"'), '-p', [string]$script:datosEnsayo.PuertoDb, '-h', '127.0.0.1') | Out-Null
    }
    $psqlEnsayo = Join-Path $PostgresRuta 'bin/psql.exe'
    $opcionesDbEnsayo = @('-X','-w','-h','127.0.0.1','-p',[string]$script:datosEnsayo.PuertoDb,'-U','ensayo','-v','ON_ERROR_STOP=1')
    $listoEnsayo = $false
    for ($intentoEnsayo = 0; $intentoEnsayo -lt 40; $intentoEnsayo++) {
        & (Join-Path $PostgresRuta 'bin/pg_isready.exe') -h 127.0.0.1 -p $script:datosEnsayo.PuertoDb -U ensayo -d postgres *> $null
        if ($LASTEXITCODE -eq 0) { $listoEnsayo = $true; break }
        Start-Sleep -Milliseconds 250
    }
    if (!$listoEnsayo) { throw 'El PostgreSQL de ensayo no respondió. Revisar postgres-error.log.' }
    $existeDbEnsayo = & $psqlEnsayo @opcionesDbEnsayo -d postgres -tAc "SELECT 1 FROM pg_database WHERE datname='eventosuy_ensayo_demo'"
    Comprobar-Salida 'la comprobación de base demo'
    if ($existeDbEnsayo -ne '1') {
        & (Join-Path $PostgresRuta 'bin/createdb.exe') -w -h 127.0.0.1 -p $script:datosEnsayo.PuertoDb -U ensayo $nombreDbEnsayo
        Comprobar-Salida 'la creación de la base demo'
    }
    $tablasEnsayo = & $psqlEnsayo @opcionesDbEnsayo -d $nombreDbEnsayo -tAc "SELECT count(*) FROM information_schema.tables WHERE table_schema='public' AND table_type='BASE TABLE'"
    Comprobar-Salida 'la comprobación del esquema demo'
    $jarEnsayo = Join-Path $raizEnsayo 'servidor-central/target/servidor.jar'
    if ([int]$tablasEnsayo -eq 0) {
        $ErrorActionPreference = 'Continue'
        & $javaEnsayo -cp $jarEnsayo persistencia.PrepararEsquemaNuevo --base-vacia *> (Join-Path $baseEnsayo 'preparar.log')
        $ErrorActionPreference = 'Stop'
        Comprobar-Salida 'la preparación del esquema demo'
        $ErrorActionPreference = 'Continue'
        & $psqlEnsayo @opcionesDbEnsayo -d $nombreDbEnsayo -f (Join-Path $raizEnsayo 'logica/src/main/resources/db/migration/V20261116_02__unicidad_ci.sql') *> (Join-Path $baseEnsayo 'indices.log')
        $ErrorActionPreference = 'Stop'
        Comprobar-Salida 'los índices de unicidad'
    }
    $accesoEnsayo = Join-Path $privadoEnsayo 'acceso-demo.properties'
    if (!(Test-Path -LiteralPath $accesoEnsayo)) {
        $ErrorActionPreference = 'Continue'
        & $javaEnsayo -cp $jarEnsayo datos.CargarDatosLab3 --agregar-datos-demo *> (Join-Path $baseEnsayo 'datos-demo.log')
        $ErrorActionPreference = 'Stop'
        Comprobar-Salida 'la carga de datos demo'
        $textoDatosEnsayo = Get-Content -Raw (Join-Path $baseEnsayo 'datos-demo.log')
        $organizadorEnsayo = [regex]::Match($textoDatosEnsayo, '(?m)^Organizador: (\S+)').Groups[1].Value
        $asistentesEnsayo = [regex]::Match($textoDatosEnsayo, '(?m)^Asistentes: (\S+), (\S+)')
        if (!$organizadorEnsayo -or !$asistentesEnsayo.Success) { throw 'No se pudieron identificar los usuarios de demo.' }
        Escribir-Utf8 $accesoEnsayo ("organizador="+$organizadorEnsayo+"`nasistente="+$asistentesEnsayo.Groups[1].Value+
            "`notro.asistente="+$asistentesEnsayo.Groups[2].Value+"`npassword="+$clavesEnsayo.demo+"`n")
    }
    $keytoolEnsayo = Join-Path $JdkRuta 'bin/keytool.exe'
    $almacenEnsayo = Join-Path $privadoEnsayo 'central.p12'
    $publicoEnsayo = Join-Path $privadoEnsayo 'central-publico.cer'
    $confianzaEnsayo = Join-Path $privadoEnsayo 'confianza-web.p12'
    if (!(Test-Path -LiteralPath $almacenEnsayo)) {
        $ErrorActionPreference = 'Continue'
        & $keytoolEnsayo -genkeypair -alias central -keyalg RSA -keysize 2048 -validity 365 -dname 'CN=Eventos UY ensayo local' `
            -ext 'SAN=IP:127.0.0.1,DNS:localhost' -storetype PKCS12 -keystore $almacenEnsayo -storepass:env ENSAYO_TLS_PASSWORD *> (Join-Path $baseEnsayo 'certificado.log')
        $ErrorActionPreference = 'Stop'
        Comprobar-Salida 'el certificado local'
    }
    if (!(Test-Path -LiteralPath $confianzaEnsayo)) {
        $ErrorActionPreference = 'Continue'
        & $keytoolEnsayo -exportcert -rfc -alias central -keystore $almacenEnsayo -storepass:env ENSAYO_TLS_PASSWORD -file $publicoEnsayo *> (Join-Path $baseEnsayo 'exportar-certificado.log')
        $ErrorActionPreference = 'Stop'
        Comprobar-Salida 'la exportación del certificado público'
        $ErrorActionPreference = 'Continue'
        & $keytoolEnsayo -importcert -noprompt -alias central -storetype PKCS12 -keystore $confianzaEnsayo -storepass:env ENSAYO_TLS_PASSWORD -file $publicoEnsayo *> (Join-Path $baseEnsayo 'confianza.log')
        $ErrorActionPreference = 'Stop'
        Comprobar-Salida 'el truststore público de Web'
    }
    $puertoCentralEnsayo = $script:datosEnsayo.PuertoCentral
    Escribir-Utf8 (Join-Path $privadoEnsayo 'central.properties') ("central.host=127.0.0.1`ncentral.escucha=127.0.0.1`ncentral.puerto=$puertoCentralEnsayo`n"+
        "central.ruta=/eventos`ncentral.ruta.sesiones=/sesiones`ncentral.ruta.portal=/portal`nsesiones.duracion.segundos=3600`n"+
        "tls.almacen="+$almacenEnsayo.Replace('\','/')+"`ntls.almacen.password="+$clavesEnsayo.tls+"`ntls.clave.password="+$clavesEnsayo.tls+
        "`ndb.url=jdbc:postgresql://127.0.0.1:"+$script:datosEnsayo.PuertoDb+"/$nombreDbEnsayo`ndb.usuario=ensayo`ndb.password="+$clavesEnsayo.db+"`n")
    Escribir-Utf8 (Join-Path $privadoEnsayo 'web.properties') ("central.host=127.0.0.1`ncentral.puerto=$puertoCentralEnsayo`ncentral.ruta=/eventos`ncentral.ruta.sesiones=/sesiones`ncentral.ruta.portal=/portal`n")
    $usuarioEnsayo = Join-Path $baseEnsayo 'usuario'
    $argumentosCentralEnsayo = @(('"-Duser.home='+$usuarioEnsayo+'"'),'-jar',('"'+$jarEnsayo+'"'))
    if ($SinSwing) { $argumentosCentralEnsayo += '--sin-swing' }
    Iniciar-Proceso 'central' $javaEnsayo $argumentosCentralEnsayo | Out-Null
    $listoEnsayo = $false
    for ($intentoEnsayo = 0; $intentoEnsayo -lt 120; $intentoEnsayo++) {
        if (!(Proceso-Propio $script:datosEnsayo.central)) { throw 'Central terminó: revisar central-error.log.' }
        if (Select-String -LiteralPath (Join-Path $baseEnsayo 'central.log') -Pattern 'WSDL del portal:' -Quiet) { $listoEnsayo = $true; break }
        Start-Sleep -Milliseconds 250
    }
    if (!$listoEnsayo) { throw 'Central no publicó el WSDL en el plazo previsto.' }
    $baseTomcatEnsayo = Join-Path $baseEnsayo 'tomcat'
    foreach ($carpetaEnsayo in @('conf','logs','temp','work','webapps')) { New-Item -ItemType Directory -Force (Join-Path $baseTomcatEnsayo $carpetaEnsayo) | Out-Null }
    foreach ($archivoEnsayo in @('web.xml','catalina.properties','logging.properties')) {
        Copy-Item (Join-Path $TomcatRuta ('conf/'+$archivoEnsayo)) (Join-Path $baseTomcatEnsayo ('conf/'+$archivoEnsayo)) -Force
    }
    $puertoWebEnsayo = $script:datosEnsayo.PuertoWeb
    Escribir-Utf8 (Join-Path $baseTomcatEnsayo 'conf/server.xml') ("<Server port='-1'><Service name='Catalina'><Connector port='$puertoWebEnsayo' address='$DireccionWeb' protocol='HTTP/1.1'/><Engine name='Catalina' defaultHost='localhost'><Host name='localhost' appBase='webapps' unpackWARs='false' autoDeploy='false'/></Engine></Service></Server>")
    Copy-Item (Join-Path $raizEnsayo 'webapp/target/web.war') (Join-Path $baseTomcatEnsayo 'webapps/web.war') -Force
    $cpTomcatEnsayo = (Join-Path $TomcatRuta 'bin/bootstrap.jar')+';'+(Join-Path $TomcatRuta 'bin/tomcat-juli.jar')
    $argumentosWebEnsayo = @(('"-Dcatalina.home='+$TomcatRuta+'"'),('"-Dcatalina.base='+$baseTomcatEnsayo+'"'),
        ('"-Duser.home='+$usuarioEnsayo+'"'),('"-Djavax.net.ssl.trustStore='+$confianzaEnsayo+'"'),
        '-Djavax.net.ssl.trustStoreType=PKCS12',('"-Djavax.net.ssl.trustStorePassword='+$clavesEnsayo.tls+'"'),
        ('"-Djava.util.logging.config.file='+(Join-Path $baseTomcatEnsayo 'conf/logging.properties')+'"'),
        '-Djava.util.logging.manager=org.apache.juli.ClassLoaderLogManager','-cp',('"'+$cpTomcatEnsayo+'"'),'org.apache.catalina.startup.Bootstrap','start')
    # Tomcat no recibe credenciales JDBC del preparador.
    foreach ($variableEnsayo in @('EVENTOS_DB_HOST','EVENTOS_DB_PORT','EVENTOS_DB_NAME','EVENTOS_DB_USER','EVENTOS_DB_PASSWORD','EVENTOS_DEMO_PASSWORD','PGPASSWORD')) {
        [Environment]::SetEnvironmentVariable($variableEnsayo, $null, 'Process')
    }
    Iniciar-Proceso 'web' $javaEnsayo $argumentosWebEnsayo | Out-Null
    $listoEnsayo = $false
    for ($intentoEnsayo = 0; $intentoEnsayo -lt 120; $intentoEnsayo++) {
        if (!(Proceso-Propio $script:datosEnsayo.web)) { throw 'Tomcat terminó: revisar web-error.log.' }
        try {
            if ((Invoke-WebRequest -UseBasicParsing -Uri "http://${DireccionWeb}:$puertoWebEnsayo/web/inicio" -TimeoutSec 2).StatusCode -eq 200) { $listoEnsayo = $true; break }
        } catch { Start-Sleep -Milliseconds 500 }
    }
    if (!$listoEnsayo) { throw 'La Web no respondió con HTTP 200: revisar los logs del ensayo.' }
    Mostrar-Estado
    Write-Output 'SOAP y PostgreSQL siguen en loopback. El acceso remoto a Web requiere conectividad y permisos de red comprobados.'
} catch {
    Write-Warning 'El ensayo no terminó. Puede haber procesos propios iniciados; ejecutar este script con -Accion Detener.'
    throw
} finally {
    foreach ($variableEnsayo in $variablesEnsayo) { [Environment]::SetEnvironmentVariable($variableEnsayo, $entornoAnteriorEnsayo[$variableEnsayo], 'Process') }
}
if ($Esperar) {
    while ((Proceso-Propio $script:datosEnsayo.central) -or (Proceso-Propio $script:datosEnsayo.web)) { Start-Sleep -Seconds 1 }
}
