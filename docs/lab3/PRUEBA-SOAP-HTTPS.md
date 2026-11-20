# Prueba aislada de SOAP sobre HTTPS

> Este procedimiento conserva la sonda de transporte original. La regresi?n
> vigente a?ade Sistema/PostgreSQL y Tomcat reales: ver
> [validación y comandos del 15/11](INTEGRACION-2026-11-15.md).


Los ejecutables viven en las fuentes de prueba de `webapp`. El servidor publica `EventosPublicador` con un doble de `ISistema` que devuelve un evento conocido. El cliente usa `ClienteEventosCentral(URI)` y el proxy generado con `wsimport`. La prueba prescinde de PostgreSQL, Tomcat y Swing.

## Compilar en cada computadora

Desde la raíz del repositorio, con Java 25 seleccionado en `JAVA_HOME` y `PATH`:

```powershell
mvn -pl webapp -am '-Dtest=InicioSoapTest,EventosPublicadorTest' '-Dsurefire.failIfNoSpecifiedTests=false' test
```

En cada terminal que ejecute una JVM, obtener el classpath local:

```powershell
[xml]$reporte = Get-Content -Raw webapp/target/surefire-reports/TEST-web.InicioSoapTest.xml
$cp = ($reporte.testsuite.properties.property | Where-Object name -eq 'java.class.path').value
```

## Preparar certificados de laboratorio

Usar un directorio privado fuera del repositorio. Ingresar contraseñas de al menos seis caracteres. Las variables se definen por terminal y deben limpiarse al terminar.

```powershell
$tlsDir = Join-Path ([System.IO.Path]::GetTempPath()) ("eventos-soap-https-" + [guid]::NewGuid())
New-Item -ItemType Directory -Path $tlsDir | Out-Null
$env:SOAP_HTTPS_KEYSTORE_PASSWORD = [pscredential]::new('laboratorio', (Read-Host 'Clave del almacén servidor' -AsSecureString)).GetNetworkCredential().Password
$env:SOAP_HTTPS_TRUSTSTORE_PASSWORD = [pscredential]::new('laboratorio', (Read-Host 'Clave del almacén de confianza' -AsSecureString)).GetNetworkCredential().Password
$serverP12 = Join-Path $tlsDir 'servidor.p12'
$serverCer = Join-Path $tlsDir 'servidor.cer'
$trustP12 = Join-Path $tlsDir 'confianza.p12'

keytool -genkeypair -alias servidor -keyalg RSA -keysize 2048 -validity 2 -dname 'CN=Laboratorio HTTPS' -ext 'SAN=IP:127.0.0.1' -storetype PKCS12 -keystore $serverP12 -storepass:env SOAP_HTTPS_KEYSTORE_PASSWORD
keytool -exportcert -rfc -alias servidor -keystore $serverP12 -storepass:env SOAP_HTTPS_KEYSTORE_PASSWORD -file $serverCer
keytool -printcert -file $serverCer
keytool -importcert -alias servidor -file $serverCer -storetype PKCS12 -keystore $trustP12 -storepass:env SOAP_HTTPS_TRUSTSTORE_PASSWORD
```

Verificar la huella antes de confirmar la importación. `servidor.p12` contiene la clave privada y se queda en el equipo servidor. Para reproducir el caso sin confianza, repetir la generación, exportación e importación con otro par de claves y archivos distintos. Usar el mismo SAN de loopback y guardar ese certificado público en `confianza-ajena.p12`. El almacén del cliente contiene exclusivamente certificados públicos.

## Ejecutar en JVM locales separadas

Terminal del servidor, con `$cp`, `$serverP12` y su contraseña configurados:

```powershell
$env:SOAP_HTTPS_BIND = '127.0.0.1'
$env:SOAP_HTTPS_PORT = '0'
$env:SOAP_HTTPS_PATH = '/eventos-prueba'
$env:SOAP_HTTPS_KEYSTORE = $serverP12
java -cp $cp web.soap.PruebaHttpsServidor
```

El puerto `0` pide un puerto libre. Copiar la URL de `SOAP_HTTPS_LISTO`. Enter detiene el endpoint y el listener HTTPS. El ejecutable también registra el cierre para la salida de la JVM.

En la terminal del cliente, volver a definir `$cp`, `$tlsDir`, `$trustP12` y `SOAP_HTTPS_TRUSTSTORE_PASSWORD`:

```powershell
$env:SOAP_HTTPS_URL = Read-Host 'URL anunciada por el servidor'
$env:SOAP_HTTPS_TRUSTSTORE = $trustP12
java -cp $cp web.soap.PruebaHttpsCliente exito

$env:SOAP_HTTPS_TRUSTSTORE = Join-Path $tlsDir 'confianza-ajena.p12'
java -cp $cp web.soap.PruebaHttpsCliente sin-confianza

$env:SOAP_HTTPS_TRUSTSTORE = $trustP12
$env:SOAP_HTTPS_URL = $env:SOAP_HTTPS_URL.Replace('127.0.0.1', 'localhost')
java '-Djava.net.preferIPv4Stack=true' -cp $cp web.soap.PruebaHttpsCliente nombre-incorrecto
```

Cada comando debe salir con código `0` e imprimir `OK`. Los casos negativos requieren un fallo de handshake con el diagnóstico esperado de JSSE. Una caída del servidor o un timeout no cuenta como rechazo TLS. El caso de nombre incorrecto usa el almacén correcto y solo cambia el nombre de la URL. El certificado contiene el SAN IP `127.0.0.1` y carece del SAN DNS `localhost`. La configuración de confianza afecta solo a la JVM de prueba y conserva los controles de certificado y nombre del JDK.

## Repetir en dos computadoras

1. Emitir otro certificado en el servidor con el SAN de la URL que usará el cliente. Si la URL usa una IP, especificar `SAN=IP:<IP-LAN>`. Si usa un nombre DNS, especificar `SAN=DNS:<nombre>` y verificar que resuelva al equipo.
2. Configurar `SOAP_HTTPS_BIND` con la IP LAN del servidor y elegir un puerto disponible y accesible desde el cliente. Para una prueba DNS, el cliente usa el nombre DNS en la URL aunque el servidor anuncie su IP de escucha.
3. Transferir únicamente el certificado público al cliente. Confirmar su huella por un canal confiable e importarlo a su propio PKCS#12 de confianza. Configurar `SOAP_HTTPS_URL` con la URL LAN y ejecutar `exito`. El resultado esperado es el evento conocido devuelto por `listarEventos()`.
4. Cerrar con Enter. Borrar los almacenes temporales al terminar y limpiar las variables de contraseña de las terminales usadas.

## Evidencia local

El 10/11/2026 se ejecutó el servidor con Java 25 y Metro 4.0.5 por loopback. Cada escenario usó una JVM cliente separada. La llamada SOAP devolvió los cinco campos esperados. El almacén ajeno produjo un fallo de validación PKIX. La URL con `localhost` produjo rechazo por identidad del certificado. El servidor terminó con código 0 al recibir Enter. Pasaron también las ocho regresiones SOAP existentes seleccionadas arriba. Quedan por verificar la invocación entre dos computadoras y el despliegue HTTPS productivo con Tomcat.

## Encabezados HTTP en Metro sobre HTTPS

La sonda automatizada vive en [EncabezadosSoapHttpsTest](../../webapp/src/test/java/web/soap/EncabezadosSoapHttpsTest.java). Publica una fachada de prueba en loopback y reutiliza la firma de `listarEventos()` para usar el proxy generado desde el WSDL vigente. La operación devuelve una lista vacía. No abre JPA, no autentica usuarios y no publica escrituras. JUnit elimina el directorio temporal que contiene el PKCS#12. El cliente confía únicamente en el certificado público de prueba, mediante un `SSLSocketFactory` configurado por puerto. Se mantiene la verificación del nombre.

```powershell
mvn -pl webapp -am '-Dtest=EncabezadosSoapHttpsTest,InicioSoapTest,EventosPublicadorTest' '-Dsurefire.failIfNoSpecifiedTests=false' test
```

- El cliente pone `Authorization: Bearer` en `MessageContext.HTTP_REQUEST_HEADERS` mediante el `BindingProvider` del puerto generado. El servidor lee el encabezado con `WebServiceContext.getMessageContext()`.
- Se comprobaron encabezados ausentes, dos valores separados de `Authorization`, valores unidos con coma, esquema incorrecto, Bearer sin referencia y dos referencias sintéticas diferentes. Metro entregó los dos valores separados a la sonda. El valor agrupado llegó como un único encabezado malformado.
- El servidor escribe `X-Prueba-Resultado` en `HTTP_RESPONSE_HEADERS`. El cliente lee la categoría desde `BindingProvider.getResponseContext()`. El encabezado contiene una etiqueta de prueba y no devuelve la referencia.
- Dos invocaciones usan puertos independientes y esperan en una barrera dentro de la misma instancia de la fachada. Después de coincidir, cada una lee su referencia y recibe su propia categoría en el encabezado de respuesta. La sonda no imprime los valores recibidos.

El 10/11/2026 pasaron los cuatro tests de encabezados y las ocho regresiones SOAP seleccionadas con Java 25 y Metro 4.0.5. La prueba de encabezados se ejecutó en una JVM por loopback, con tráfico TLS real. No verifica normalización por intermediarios, el despliegue entre equipos, ni emisión o revocación de credenciales por SOAP. Java 25 informó una advertencia de Metro por uso de `sun.misc.Unsafe`. La suite terminó correctamente.

### Instancia compartida para P03

Cuando se conecten los publicadores definitivos, el punto de arranque del Central debe crear **una instancia** de `SesionesRemotas` por proceso e inyectar esa misma referencia por constructor en los publicadores de login, logout y operaciones protegidas. Así, una sesión emitida por uno puede ser consultada o revocada por otro. El registro compartido contiene el estado de las sesiones. Cada fachada sigue leyendo los encabezados desde el contexto de su invocación y el cliente crea un puerto por llamada. La sonda no conecta todavía ese registro ni cambia el arranque productivo.
