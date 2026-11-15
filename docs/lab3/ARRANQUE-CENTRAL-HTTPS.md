# Arranque del Central con SOAP sobre HTTPS

> Actualización 15/11: Web ya consume exclusivamente HTTPS mediante Portal y
> P03. Los ejemplos de certificados y configuración externa siguen vigentes.
> Ver [integración, pruebas reales de Tomcat e instrucciones actuales](INTEGRACION-2026-11-15.md).
> Las cifras y limitaciones del corte original se conservan como historia.


## Alcance

`central.Main` publica eventos y sesiones en un único `HttpsServer` del JDK con Metro 4.0.5. `ServidorHttpsCentral` recibe un `ISistema`, crea un único `SesionesRemotas` y publica las instancias mediante `Endpoint.create(instancia).publish(HttpContext)`. Eventos y sesiones comparten ese controlador; no hay un registro por petición. Las futuras fachadas protegidas deberán recibir ese mismo registro, sin crear otro.

TLS se valida antes de inicializar JPA. Luego se obtiene `ISistema` mediante `Fabrica`, se publican las fachadas y se conserva el arranque existente de Swing dentro del proceso. No se modifica el mecanismo interno de obtención de controladores de Swing. El cierre del proceso detiene endpoints, listener y ejecutor; después cierra JPA. El cierre del componente es idempotente y también se ejecuta si falla la publicación.

No hay listener HTTP ni fallback sin cifrar. PortalService publica los casos
autorizados, incluido `altaTipoRegistro`, en `/portal`, y comparte el registro
P03 de `/sesiones`. Las sesiones son volátiles: reiniciar Central invalida
todas las referencias. Login y logout Web utilizan P03.

## Preparar TLS fuera del repositorio

Ejemplo PowerShell para laboratorio, ejecutado con Java 25. Sustituir `central-lab` y `192.168.1.50` por el DNS e IP reales y estables del Central **antes** de generar el certificado. La IP de ejemplo no configura la red. No ejecutar estos pasos para desplegar en LAN hasta acordarlo con el equipo.

```powershell
New-Item -ItemType Directory -Force "$HOME/.eventosUy"
keytool -genkeypair -alias central -keyalg RSA -keysize 3072 -sigalg SHA256withRSA -validity 90 -storetype PKCS12 -keystore "$HOME/.eventosUy/central.p12" -dname "CN=central-lab" -ext "SAN=dns:central-lab,ip:192.168.1.50,ip:127.0.0.1"
keytool -exportcert -rfc -alias central -keystore "$HOME/.eventosUy/central.p12" -file "$HOME/.eventosUy/central-publico.pem"
```

`keytool` solicita las contraseñas sin incluirlas en los comandos. Para PKCS#12 usar la misma contraseña de almacén y clave. Conservar la clave privada solamente en el Central. El almacén debe contener exactamente una entrada de clave privada y su cadena vigente. El nombre/IP usado por cada cliente debe figurar en SAN; CN por sí solo no basta. No usar verificadores de hostname permisivos.

Compartir exclusivamente `central-publico.pem`, comprobar su huella SHA-256 por un canal confiable y crear un truststore externo en la computadora cliente:

```powershell
keytool -printcert -file central-publico.pem
keytool -importcert -alias central -file central-publico.pem -storetype PKCS12 -keystore "$HOME/.eventosUy/central-confianza.p12"
```

En una defensa con certificado autofirmado, confiar únicamente en el certificado público acordado. Renovarlo exige actualizar la confianza del cliente. Nunca copiar `central.p12` al cliente ni agregarlo a Git.

## Configuración y ejecución del Central

Copiar `servidor-central/src/main/resources/central.properties.example` a `~/.eventosUy/central.properties`. El archivo se lee en UTF-8. Completar externamente:

| Propiedad | Significado |
|---|---|
| `central.host` | DNS o IP con SAN válido que usarán los clientes; sin esquema ni puerto |
| `central.escucha` | Interfaz de escucha: `127.0.0.1` local; IP LAN o `0.0.0.0` para LAN |
| `central.puerto` | Puerto TCP fijo, por ejemplo `9128`; `0` se reserva para pruebas efímeras |
| `central.ruta` | Ruta independiente de eventos, por ejemplo `/eventos` |
| `central.ruta.sesiones` | Ruta independiente de sesiones, por ejemplo `/sesiones` |
| `central.ruta.portal` | Ruta de los casos del portal, por defecto `/portal` |
| `sesiones.duracion.segundos` | Duración fija positiva, por ejemplo `1800` |
| `tls.almacen` | Ruta absoluta al PKCS#12 externo, con `/` en Windows |
| `tls.almacen.password`, `tls.clave.password` | Contraseñas externas, nunca versionadas |
| `db.url`, `db.usuario`, `db.password` | Configuración PostgreSQL existente y autorizada |

Las rutas no pueden coincidir ni anidarse. No usar `0.0.0.0` como destino del cliente. Java Properties interpreta escapes: usar `/` en rutas y escapar los caracteres de contraseña cuando lo requiera ese formato. Restringir los permisos del archivo al usuario que ejecuta el Central; la configuración contiene secretos en texto y no debe compartirse ni imprimirse.

```powershell
mvn -pl servidor-central -am -DskipTests package
java -jar servidor-central/target/servidor.jar
```

`Main` conserva la ubicación de configuración basada en `user.home`; no lee la plantilla automáticamente. El arranque real requiere PostgreSQL y un esquema ya existente: se usa `hibernate.hbm2ddl.auto=validate`, sin crear ni migrar tablas. No ejecutar contra una base no autorizada para esta comprobación. Las pruebas de este corte no ejecutan `Main` ni inicializan JPA: ejercen el componente de publicación real con un doble.

Al arrancar se imprimen solamente las URL públicas de los tres WSDL. Ejemplos:

```text
https://central-lab:9128/eventos?wsdl
https://central-lab:9128/sesiones?wsdl
https://central-lab:9128/portal?wsdl
```

Si falta TLS, el almacén no es válido o sus claves son incorrectas, el arranque falla sin intentar HTTP. El cliente detecta un SAN incorrecto durante el handshake; cargar el almacén no demuestra por sí solo que su SAN coincide con todos los nombres de conexión.

## Consumidor y procedimiento LAN pendiente

1. Elegir una IP estable y DNS resoluble desde ambas computadoras (o conectar por IP incluida en SAN).
2. Configurar el Central para escuchar en su interfaz LAN. Autorizar en el firewall del equipo solamente TCP al puerto elegido, perfil privado y, si es posible, IP del Servidor Web. No abrir PostgreSQL para consumir SOAP.
3. Distribuir únicamente el certificado público y configurar el truststore en la JVM cliente. Comprobar conectividad con `Test-NetConnection central-lab -Port 9128`; esto comprueba TCP, no TLS ni SOAP.
4. Comprobar ambos WSDL por HTTPS con un cliente que confíe en ese certificado. No utilizar `curl -k`, `TrustAll` ni desactivar hostname verification.
5. Usar los proxies ya generados por `wsimport`, sustituyendo por configuración externa `BindingProvider.ENDPOINT_ADDRESS_PROPERTY`. Crear un puerto por invocación. Configurar la confianza antes de abrir conexiones mediante `javax.net.ssl.trustStore`, `javax.net.ssl.trustStoreType=PKCS12` y `javax.net.ssl.trustStorePassword` en una configuración de JVM privada, o mediante el `SSLContext` específico del cliente ya demostrado en las pruebas. No versionar contraseñas ni pasarlas en comandos compartidos.
6. Ejecutar login y logout con un usuario de laboratorio autorizado, sin imprimir contraseña, respuesta de encabezados ni credencial. Comprobar rechazo con truststore ajeno y con un nombre no incluido en SAN.

La adaptación Java del puerto usa estas APIs, sin modificar las clases generadas:

```java
var puerto = new SesionesService().getSesionesPort();
var transporte = (BindingProvider) puerto;
transporte.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, urlHttpsExterna);
var usuario = puerto.iniciarSesion(identificador, contrasenia);
// Leer X-EventosUy-Session de HTTP_RESPONSE_HEADERS, sin imprimirlo.
// Con otro puerto nuevo, enviar la referencia únicamente por HTTP:
transporteLogout.getRequestContext().put(MessageContext.HTTP_REQUEST_HEADERS,
        Map.of("Authorization", List.of("Bearer " + referencia)));
puertoLogout.cerrarSesion();
```

La credencial no está en el DTO, WSDL, URL ni logs. El logout de formato válido es idempotente. La revocación efectiva se comprueba en la suite contra el registro compartido; repetir logout exitosamente no demuestra por sí mismo esa revocación.

**Integración Web completada el 15/11:** todos los clientes exigen HTTPS.
PortalService utiliza `/portal` y el registro P03 de `/sesiones`. Tomcat
configura confianza externa y no recibe acceso a PostgreSQL. La integración
real con Tomcat verifica estos recorridos y el rechazo sin Central.

## Verificación automatizada sin PostgreSQL

```powershell
mvn -pl webapp -am '-Dtest=ArranqueHttpsCentralTest,SesionesSoapHttpsTest,SesionesRemotasTest,EncabezadosSoapHttpsTest,InicioSoapTest,EventosPublicadorTest,ConfiguracionCentralTest' '-Dsurefire.failIfNoSpecifiedTests=false' test
```

`ArranqueHttpsCentralTest` instancia el componente productivo con propiedades temporales sin JDBC, un certificado temporal y un doble de `ISistema`. Comprueba ambos WSDL, consulta mediante proxy generado, login, logout repetido, rechazo TLS por falta de confianza y hostname incorrecto, claves/almacén ausentes o inválidos, rechazo de rutas superpuestas, inicio único, cierre repetido y liberación del puerto. Las otras suites cubren revocación, concurrencia, expiración y regresiones anteriores. Los certificados y secretos de pruebas quedan en directorios temporales; no se agregan al repositorio.

Resultado local: 38 pruebas seleccionadas aprobadas (4 de arranque, 8 de sesiones SOAP HTTPS, 10 del registro, 4 de encabezados, 6 de portada SOAP, 2 del publicador de eventos y 4 de configuración). No se ejecutó el arranque productivo con JPA ni una conexión entre dos computadoras. No se modificó el firewall ni se desplegó en LAN. El cierre validado es el del componente; el hook de `Main` se revisó estáticamente, sin arrancar Swing ni PostgreSQL. Metro mantiene su advertencia conocida de Java 25 sobre `sun.misc.Unsafe`.
