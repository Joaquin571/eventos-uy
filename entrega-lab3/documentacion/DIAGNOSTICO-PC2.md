# PC2: diagnóstico de `/web/eventos`

## Qué se sabe del código

- `ConfiguracionWeb.archivoConfiguracion()` usa `-Deventos.web.config=RUTA` si existe.
  `Iniciar-Web.ps1` apunta explícitamente a `PC2/config/web.properties`. El fallback
  del código a `user.home/.eventosUy/web.properties` es sólo compatibilidad anterior;
  esta instalación no depende de él.
- Son obligatorios `central.host` y `central.puerto`. El portal usa
  `central.ruta.portal`, con `/portal` como valor predeterminado; las sesiones
  usan `central.ruta.sesiones`, con `/sesiones` predeterminado. El cliente histórico
  de eventos lee `central.ruta`. La plantilla define explícitamente las tres rutas.
- Con `central.host=100.115.156.116`, `central.puerto=9128` y
  `central.ruta.portal=/portal`, la URL efectiva de Portal es
  `https://100.115.156.116:9128/portal`. `ContextoListener` la registra al iniciar.
  Los WSDL del WAR son recursos locales de los proxies; el destino de la llamada
  se sustituye mediante `BindingProvider.ENDPOINT_ADDRESS_PROPERTY`. No hay
  fallback a `localhost` para el host.
- El listado usa `ContextoListener.central(req)`, que crea `ClienteCentral` para
  Portal. `FiltroAcceso` consulta `listarCategorias()` por SOAP para construir el
  cabezal antes de entrar al servlet. Por eso incluso una URL sin servlet podía
  mostrar la página 503. El nuevo WAR agrega `/eventos` como alias de `/inicio`.
- `ClienteCentral` capturaba `WebServiceException` y perdía la causa. El nuevo WAR
  registra clase, mensaje saneado y causa raíz en el log de Tomcat; el navegador
  sigue recibiendo la página genérica sin detalles internos.

`Test-NetConnection=True` sólo confirma TCP. `curl -k` obtiene el WSDL **sin validar**
la cadena ni el nombre del certificado. Ninguna de esas pruebas demuestra que la JVM
de Tomcat pueda completar TLS o invocar una operación SOAP.

## Preparar configuración y diagnosticar desde PC2

Usar la carpeta completa `C:\entrega-lab3` con el WAR nuevo y los scripts de la raíz.
Desde `C:\entrega-lab3` en PowerShell 5.1:

```powershell
$tomcat = 'C:\RUTA_REAL_TOMCAT_11'
$javaHome = 'C:\RUTA_REAL_JDK25'
$trust = 'C:\entrega-lab3\PC2\certificados\tomcat-truststore.p12'
$config = 'C:\entrega-lab3\PC2\config\web.properties'

.\Verificar-Requisitos.ps1 -Rol Webapp -TomcatRuta $tomcat -JavaRuta "$javaHome\bin\java.exe"
.\PC2\Preparar-Web.ps1 -HostCentral 100.115.156.116 -Destino $config
```

Si la configuración existente tiene valores diferentes, `Preparar-Web.ps1` se
detiene. Revisarla y ejecutar de nuevo con `-Actualizar` para guardarle un respaldo
privado y reemplazarla. No copiar contraseñas al archivo Web: contiene sólo host,
puerto y rutas. Para probar la cadena y el SAN **sin** `-k`, usar el certificado
público de Central en formato PEM:

```powershell
curl.exe --fail --show-error --cacert 'C:\RUTA_CERT_PUBLICO\central-publico.cer' 'https://100.115.156.116:9128/portal?wsdl'
```

La sonda siguiente usa Java 25, el **mismo truststore** y la verificación de nombre/IP
que necesita Tomcat. Pide la clave del truststore y no la coloca en la línea de
comandos ni en el paquete:

```powershell
.\PC2\Verificar-JavaTls.ps1 -HostCentral 100.115.156.116 -Truststore $trust -JavaHome $javaHome
```

Si falla, revisar `PKIX path building failed` (certificado o CA ausente del
truststore), `No subject alternative names matching IP address` (falta
`IP:100.115.156.116` en SAN), errores de contraseña/tipo PKCS#12, puerto y ruta.
Un `WSDL: OK` confirma TLS Java y el documento WSDL, **no** la operación SOAP.

## Reemplazar WAR y reiniciar Tomcat de consola

Detener Tomcat desde otra consola; esperar que termine antes de reemplazar:

```powershell
& "$tomcat\bin\shutdown.bat"
.\PC2\Instalar-Web.ps1 -Tomcat $tomcat -Reemplazar
.\PC2\Iniciar-Web.ps1 -Tomcat $tomcat -Truststore $trust -JavaHome $javaHome -WebConfig $config
```

`Instalar-Web.ps1 -Reemplazar` conserva el WAR y la aplicación expandida anteriores
en `webapps-backup/`. `Iniciar-Web.ps1` valida Java 25, Tomcat 11 y que el PKCS#12 se
puede abrir; pasa explícitamente `-Deventos.web.config`,
`-Djavax.net.ssl.trustStore` y `-Djavax.net.ssl.trustStoreType=PKCS12` a Tomcat.
Mantener esa consola abierta. Si Tomcat corre como **servicio Windows**, detener e
iniciar el servicio correspondiente y configurar esas mismas propiedades en su JVM;
`catalina.bat run` no altera el servicio.

Al arrancar, buscar `Configuración SOAP` en la consola o los logs de Tomcat y
confirmar `portal=https://100.115.156.116:9128/portal`. Abrir
`http://IP_PC2:8080/web/eventos`. Ante un 503, buscar `Fallo SOAP Portal` y leer
`clase`, `mensaje` y `causaRaiz` en la consola o `logs/` de Tomcat. Esos datos
permiten diferenciar PKIX/SAN, conexión, timeout y falla de transporte. Si no
aparece ese log, confirmar que Tomcat desplegó el WAR nuevo y que se está mirando
el log del proceso correcto.

La causa concreta del fallo observado en PC2 **no está demostrada** con las pruebas
facilitadas. Se necesita la salida de `Verificar-JavaTls.ps1` y el log `Fallo SOAP
Portal` del despliegue actualizado para cerrarla.
