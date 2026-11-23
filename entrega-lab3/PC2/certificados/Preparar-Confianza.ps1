[CmdletBinding()]
param(
    [string]$CertificadoPublico = (Join-Path $PSScriptRoot 'central-publico.cer'),
    [Parameter(Mandatory)][ValidatePattern('^[0-9A-Fa-f]{64}$')][string]$Sha256Esperado,
    [string]$Java,
    [string]$Destino = (Join-Path $PSScriptRoot 'tomcat-truststore.p12')
)
$ErrorActionPreference = 'Stop'
. (Join-Path $PSScriptRoot '../../Requisitos-Comunes.ps1')
if (!$Java) {
    $detectado = @(Get-RequisitosEstado -Rol Webapp | Where-Object { $_.Componente -eq 'Java' -and $_.Compatible })
    if ($detectado.Count) { $Java = $detectado[0].Ruta }
}
if (!$Java -or (Get-VersionJava $Java) -notlike '25.*') { throw 'Se requiere Java 25. Indicar -Java con la ruta a java.exe.' }
$keytool = Join-Path (Split-Path -Parent (Split-Path -Parent $Java)) 'bin/keytool.exe'
if (!(Test-Path -LiteralPath $keytool)) { throw "Falta $keytool" }
if (!(Test-Path -LiteralPath $CertificadoPublico)) { throw "Falta $CertificadoPublico" }
$real = (Get-FileHash -Algorithm SHA256 -LiteralPath $CertificadoPublico).Hash
if ($real -ine $Sha256Esperado) { throw 'SHA256 del certificado público no coincide con el informado por PC1.' }
$destinoReal = [IO.Path]::GetFullPath($Destino)
if (Test-Path -LiteralPath $destinoReal) { throw "Ya existe $destinoReal. No se sobrescribe el truststore." }
Write-Host "Certificado público verificado: $real"
& $keytool -printcert -file $CertificadoPublico
$segura = Read-Host 'Contraseña nueva del truststore de PC2 (sin comillas, % ni !)' -AsSecureString
$puntero = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($segura)
$anterior = $env:EVENTOS_TRUSTSTORE_PASSWORD
try {
    $texto = [Runtime.InteropServices.Marshal]::PtrToStringBSTR($puntero)
    if ($texto -match '["%!\r\n]') { throw 'La contraseña contiene caracteres incompatibles con catalina.bat.' }
    $env:EVENTOS_TRUSTSTORE_PASSWORD = $texto
    $preferencia = $ErrorActionPreference
    $ErrorActionPreference = 'Continue'
    try {
        & $keytool -importcert -noprompt -alias central -file $CertificadoPublico -storetype PKCS12 -keystore $destinoReal -storepass:env EVENTOS_TRUSTSTORE_PASSWORD
        $codigo = $LASTEXITCODE
        if ($codigo -eq 0) {
            & $keytool -list -alias central -storetype PKCS12 -keystore $destinoReal -storepass:env EVENTOS_TRUSTSTORE_PASSWORD
            $codigo = $LASTEXITCODE
        }
    } finally { $ErrorActionPreference = $preferencia }
    if ($codigo -ne 0) { throw 'No se pudo crear/verificar el truststore. Revisar archivos parciales.' }
} finally {
    $env:EVENTOS_TRUSTSTORE_PASSWORD = $anterior
    [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($puntero)
}
Write-Host "Truststore privado listo: $destinoReal"
