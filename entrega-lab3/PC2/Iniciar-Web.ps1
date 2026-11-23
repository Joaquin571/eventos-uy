[CmdletBinding()]
param(
    [string]$Tomcat,
    [string]$Truststore = (Join-Path $PSScriptRoot 'certificados/tomcat-truststore.p12'),
    [string]$JavaHome,
    [string]$WebConfig = (Join-Path $PSScriptRoot 'config/web.properties')
)
$ErrorActionPreference = 'Stop'
. (Join-Path $PSScriptRoot '../Requisitos-Comunes.ps1')
if (!$Tomcat) {
    if ($env:CATALINA_HOME) { $Tomcat = $env:CATALINA_HOME }
    else { $Tomcat = Join-Path $env:LOCALAPPDATA 'EventosUyTools/apache-tomcat-11.0.29' }
}
$catalina = Join-Path $Tomcat 'bin/catalina.bat'
$web = Join-Path $Tomcat 'webapps/web.war'
$config = [IO.Path]::GetFullPath($WebConfig)
if (![IO.Path]::IsPathRooted($Truststore)) { throw 'Truststore debe ser una ruta absoluta.' }
foreach ($ruta in @($catalina,$web,$config,$Truststore)) {
    if (!(Test-Path -LiteralPath $ruta)) { throw "Falta $ruta" }
}
$tomcatVersion = Get-VersionTomcat $Tomcat
if ($tomcatVersion -notmatch '^11\.0\.29(?:\.0)?$') { throw "Tomcat 11.0.29 no verificado en $Tomcat" }
$javaHomeAnterior = $env:JAVA_HOME
$jreHomeAnterior = $env:JRE_HOME
if (!$JavaHome) {
    $detectado = @(Get-RequisitosEstado -Rol Webapp | Where-Object { $_.Componente -eq 'Java' -and $_.Compatible })
    if (!$detectado.Count) { throw 'Java 25 no encontrado. Indicar -JavaHome.' }
    $JavaHome = Split-Path -Parent (Split-Path -Parent $detectado[0].Ruta)
}
$java = Join-Path $JavaHome 'bin/java.exe'
$version = Get-VersionJava $java
if (!$version -or !$version.StartsWith('25.')) {
    throw 'Tomcat debe ejecutarse con Java 25. Indicar -JavaHome o configurar JAVA_HOME.'
}
$clave = Read-Host 'Contraseña del truststore de PC2' -AsSecureString
$puntero = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($clave)
$anterior = $env:CATALINA_OPTS
$env:JAVA_HOME = $JavaHome
$env:JRE_HOME = $JavaHome
try {
    $texto = [Runtime.InteropServices.Marshal]::PtrToStringBSTR($puntero)
    if ($texto -match '["%!\r\n]') {
        throw 'La clave del truststore contiene caracteres que catalina.bat no puede recibir de forma segura. Configurar la JVM del servicio Windows o usar una clave sin comillas, porcentaje ni exclamación.'
    }
    $keytool = Join-Path (Split-Path -Parent (Split-Path -Parent $java)) 'bin/keytool.exe'
    if (!(Test-Path -LiteralPath $keytool)) { throw 'keytool.exe no encontrado junto a Java 25.' }
    $claveAnterior = $env:EVENTOS_TRUSTSTORE_PASSWORD
    try {
        $env:EVENTOS_TRUSTSTORE_PASSWORD = $texto
        $preferencia = $ErrorActionPreference
        $ErrorActionPreference = 'Continue'
        try { $null = & $keytool -list -storetype PKCS12 -keystore $Truststore -storepass:env EVENTOS_TRUSTSTORE_PASSWORD 2>&1; $codigoTls = $LASTEXITCODE }
        finally { $ErrorActionPreference = $preferencia }
        if ($codigoTls -ne 0) { throw 'El truststore PKCS12 no puede abrirse con la contraseña indicada.' }
    } finally { $env:EVENTOS_TRUSTSTORE_PASSWORD = $claveAnterior }
    $env:CATALINA_OPTS = "$anterior `"-Deventos.web.config=$config`" `"-Djavax.net.ssl.trustStore=$Truststore`" -Djavax.net.ssl.trustStoreType=PKCS12 `"-Djavax.net.ssl.trustStorePassword=$texto`""
    Write-Host "Iniciando Tomcat $tomcatVersion con Java $version y configuración $config. Esta consola debe permanecer abierta."
    $preferencia = $ErrorActionPreference
    $ErrorActionPreference = 'Continue'
    try {
        & $catalina run 2>&1 | ForEach-Object {
            $linea = [string]$_
            if ($texto.Length -gt 0) { $linea = $linea.Replace($texto, '[CLAVE_OCULTA]') }
            Write-Host $linea
        }
        $codigo = $LASTEXITCODE
    }
    finally { $ErrorActionPreference = $preferencia }
    if ($codigo -ne 0) { throw "Tomcat terminó con código $codigo. Revisar logs y configuración TLS." }
} finally {
    $texto = $null
    $env:CATALINA_OPTS = $anterior
    $env:JAVA_HOME = $javaHomeAnterior
    $env:JRE_HOME = $jreHomeAnterior
    [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($puntero)
}
