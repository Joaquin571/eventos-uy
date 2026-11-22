# Eventos UY: configuraciones y comandos de ejemplo

Complemento de [README_DESPLIEGUE_LAB3.md](../README_DESPLIEGUE_LAB3.md). **Todos los valores `IP_CENTRAL`, `IP_WEB`, `TU_USUARIO`, `TU_PASSWORD` y `RUTA_*` son marcadores**: sustituirlos por valores reales sólo en archivos privados fuera de Git. Los comandos se ofrecen para ejecutarlos manualmente después de comprobar equipo, base y rutas; este documento no registra credenciales ni el estado operativo de una máquina concreta.

## 1. Propiedades por nodo

En la cuenta que ejecuta `servidor.jar` de PC 1, copiar [la plantilla](../servidor-central/src/main/resources/central.properties.example) a `%USERPROFILE%\.eventosUy\central.properties` y completarla:

```properties
central.host=IP_CENTRAL
central.escucha=IP_CENTRAL
central.puerto=9128
central.ruta=/eventos
central.ruta.portal=/portal
central.ruta.sesiones=/sesiones
sesiones.duracion.segundos=1800
db.url=jdbc:postgresql://localhost:5432/eventosuy_final
db.usuario=TU_USUARIO
db.password=TU_PASSWORD
tls.almacen=RUTA_ABSOLUTA_AL_PKCS12
tls.almacen.password=TU_PASSWORD
tls.clave.password=TU_PASSWORD
```

`central.host` es la dirección publicada en los WSDL; `central.escucha` es una dirección local donde Central puede abrir el listener. Elegir la IP de PC 1 en la red de defensa; `0.0.0.0` sólo si se justifica escuchar en todas las interfaces y el firewall limita el acceso. El certificado debe incluir en SAN el host/IP publicado. El código exige las rutas de eventos y sesiones y permite `/portal` por defecto; fijar las tres evita ambigüedad. `tls.almacen` debe ser absoluto y el PKCS#12 debe contener una sola clave privada y cadena vigente. **No versionar este archivo ni su respaldo.**

En la cuenta de Tomcat de PC 2, copiar [la plantilla Web](../webapp/src/main/resources/web.properties.example) a `%USERPROFILE%\.eventosUy\web.properties`:

```properties
central.host=IP_CENTRAL
central.puerto=9128
central.ruta=/eventos
central.ruta.portal=/portal
central.ruta.sesiones=/sesiones
```

Todos los clientes SOAP (Eventos, Portal y Sesiones) admiten `-Deventos.web.config=RUTA_PRIVADA`. Si no se define esa propiedad, usan `%USERPROFILE%\.eventosUy\web.properties`. La Webapp no necesita `EVENTOS_DB_*`, `db.*` ni acceso PostgreSQL.

## 2. Certificado y confianza TLS

En PC 1, crear un **PKCS#12 nuevo** en una ruta privada. Antes de generarlo, definir la IP o DNS real de Central y comprobar que los nombres de destino están libres. Estos comandos piden las contraseñas de forma interactiva; no usar `-storepass` ni `-keypass` con valores reales en la línea de comandos:

```powershell
$ipCentral = Read-Host 'IP real de Central en la red de defensa'
$tls = Join-Path $env:USERPROFILE '.eventosUy\tls'
New-Item -ItemType Directory -Path $tls -Force | Out-Null
$p12 = Join-Path $tls 'central-defensa.p12'
$publico = Join-Path $tls 'central-defensa-publico.cer'
if ((Test-Path -LiteralPath $p12) -or (Test-Path -LiteralPath $publico)) { throw 'Elegir nombres nuevos; no sobrescribir certificados' }
keytool -genkeypair -alias central -keyalg RSA -keysize 3072 -sigalg SHA256withRSA -validity 365 -storetype PKCS12 -keystore $p12 -dname 'CN=EventosUY Central, O=Equipo, C=UY' -ext "SAN=IP:$ipCentral,IP:127.0.0.1,DNS:localhost"
keytool -exportcert -rfc -alias central -keystore $p12 -file $publico
keytool -printcert -file $publico
```

Si `central.host` es un DNS en vez de una IP, emitir el certificado con `DNS:nombre.real` en SAN. Comprobar que la clave y el certificado son válidos antes de cambiar `tls.almacen`. Nunca copiar `central-defensa.p12` a PC 2. Transferir sólo `central-defensa-publico.cer` por un medio confiable y comparar la huella SHA-256 de ambos lados.

En PC 2, con el certificado público ya transferido a una ruta privada, crear un truststore **nuevo**:

```powershell
$tls = Join-Path $env:USERPROFILE '.eventosUy\tls'
$publico = Join-Path $tls 'central-defensa-publico.cer'
$truststore = Join-Path $tls 'tomcat-defensa-truststore.p12'
if (Test-Path -LiteralPath $truststore) { throw 'Elegir otro nombre; no sobrescribir truststore' }
keytool -printcert -file $publico
keytool -importcert -alias central -file $publico -storetype PKCS12 -keystore $truststore
keytool -list -v -storetype PKCS12 -keystore $truststore
```

Tomcat debe recibir las propiedades JVM `javax.net.ssl.trustStore=RUTA_TRUSTSTORE`, `javax.net.ssl.trustStoreType=PKCS12` y `javax.net.ssl.trustStorePassword=TU_PASSWORD`. Si se inicia con scripts, definirlas en un `bin/setenv.bat` privado mediante `CATALINA_OPTS`; si se ejecuta como servicio Windows, configurar la JVM del servicio. **No versionar `setenv.bat` con contraseña.** Comprobar el WSDL con validación de certificado y host, sin `-k`:

```powershell
curl.exe --cacert 'RUTA_CERTIFICADO_PUBLICO' 'https://IP_CENTRAL:9128/portal?wsdl'
```

El comando anterior sólo funcionará si Central ya arrancó, `central.host` coincide con el SAN, el certificado público corresponde al que presenta el servidor y PC 2 llega al puerto configurado. `Test-NetConnection IP_CENTRAL -Port 9128` confirma TCP, no confianza TLS ni SOAP.

### Respaldo de configuración de Central

Antes de cambiar un `central.properties` existente, detener Central, guardar una copia **privada** con nombre y ubicación elegidos por el operador y verificarla. No se publica el nombre de respaldo de una instalación concreta. Para restaurar, detener Central y conservar también la versión nueva antes de copiar el respaldo; comprobar después que `central.host`, `central.escucha`, certificado y SAN correspondan a la red efectiva. Nunca imprimir las propiedades con contraseñas en una captura o log.

## 3. Tomcat 11 independiente

Instalar Tomcat 11 en PC 2 y confirmar `java -version`, el puerto HTTP y las rutas reales. El proyecto se compila para Java 25, por lo que la JVM que ejecuta Tomcat también debe poder cargar clases Java 25. Con Tomcat detenido y tras revisar que el destino no contiene otra aplicación que deba conservarse:

```powershell
$tomcat = 'RUTA_TOMCAT_11'
$war = 'RUTA_AL_ARTEFACTO\web.war'
Test-Path -LiteralPath (Join-Path $tomcat 'bin\catalina.bat')
Test-Path -LiteralPath $war
Copy-Item -LiteralPath $war -Destination (Join-Path $tomcat 'webapps\web.war')
& (Join-Path $tomcat 'bin\catalina.bat') run
```

`web.war` da el contexto `/web`, por ejemplo `http://IP_WEB:8080/web/inicio` y `/web/movil`. Para contexto `/` se necesita desplegar como `ROOT.war` sólo tras comprobar que no se reemplaza una aplicación existente. Para detener Tomcat desde otra consola usar `bin/shutdown.bat`; revisar el directorio `logs` de `CATALINA_BASE` o `CATALINA_HOME`. La JVM debe leer `web.properties` bajo el perfil de la cuenta que ejecuta Tomcat. Consultar [la guía oficial de Tomcat](https://tomcat.apache.org/tomcat-11.0-doc/RUNNING.txt) para detalles de `CATALINA_OPTS` y servicios Windows.

## 4. PostgreSQL: base nueva y respaldo

Usar en PC 1 PostgreSQL local y una base de demostración distinta de `eventosuy_test`. Los comandos siguientes **no se ejecutaron para preparar esta documentación**. Sustituir `TU_USUARIO_ADMIN` por un rol autorizado y revisar la salida de cada consulta antes del paso siguiente. Si la base ya existe, detener el procedimiento de creación y trabajar con un plan de migración y respaldo específico.

```powershell
$psql = 'C:\Program Files\PostgreSQL\17\bin\psql.exe'
& $psql -h localhost -p 5432 -U TU_USUARIO_ADMIN -d postgres -tAc "SELECT datname FROM pg_database WHERE datname='eventosuy_final'"
# Sólo si la consulta anterior no devolvió filas y el nombre está autorizado:
& $psql -h localhost -p 5432 -U TU_USUARIO_ADMIN -d postgres -c 'CREATE DATABASE eventosuy_final'
& $psql -h localhost -p 5432 -U TU_USUARIO_ADMIN -d eventosuy_final -c 'SELECT current_database(), current_schema()'
```

Para ejecutar el preparador sobre esa base **vacía**, proporcionar las cinco variables al proceso Java. Introducir la contraseña por diálogo, no en el historial. Comprobar antes que la consulta `current_database()` devuelve `eventosuy_final` para el mismo host, puerto y usuario. `PrepararEsquemaNuevo` aborta si el esquema ya contiene tablas:

```powershell
$cred = Get-Credential -UserName 'TU_USUARIO'
$env:EVENTOS_DB_HOST = 'localhost'
$env:EVENTOS_DB_PORT = '5432'
$env:EVENTOS_DB_NAME = 'eventosuy_final'
$env:EVENTOS_DB_USER = $cred.UserName
$env:EVENTOS_DB_PASSWORD = $cred.GetNetworkCredential().Password
try {
    java -cp .\servidor.jar persistencia.PrepararEsquemaNuevo --base-vacia
} finally {
    Remove-Item Env:EVENTOS_DB_PASSWORD -ErrorAction SilentlyContinue
}
```

El comando anterior supone que `servidor.jar` ya está en la carpeta actual de PC 1. Verificar luego columnas, restricciones e índices del esquema creado. La tabla de seguimiento y los índices de unicidad sin distinguir mayúsculas se definen mediante SQL versionado; transferir a PC 1 los scripts necesarios desde el repositorio, verificar sus nombres y hashes y señalar la carpeta que los contiene. Aplicar, tras inspección y respaldo, **sólo** lo necesario a la base comprobada, con `ON_ERROR_STOP`:

```powershell
$sql = 'RUTA_A_SQL_VERSIONADOS'
& $psql -v ON_ERROR_STOP=1 -h localhost -p 5432 -U TU_USUARIO -d eventosuy_final -f (Join-Path $sql 'seguimiento-usuario.sql')
& $psql -v ON_ERROR_STOP=1 -h localhost -p 5432 -U TU_USUARIO -d eventosuy_final -f (Join-Path $sql 'V20261116_02__unicidad_ci.sql')
& $psql -h localhost -p 5432 -U TU_USUARIO -d eventosuy_final -c 'SELECT current_database(), current_schema()'
```

Antes del índice único, buscar duplicados de correo y de nombre de tipo de registro dentro de una edición. `V20261116_01__visitas_evento.sql`, `sql/lab3-adrian-asistencia.sql`, `sql/lab3-emmanuel-video.sql` y `sql/lab3-emmanuel-busqueda.sql` son migraciones para esquemas **anteriores**: verificar si realmente falta cada columna o restricción antes de aplicarlas. `sql/lab3-emmanuel-finalizacion.sql` duplica la columna `finalizado` del script de visitas. El preparador actual usa `hibernate.hbm2ddl.auto=update` sólo en este paso explícito sobre una base vacía; `central.Main` usa `validate` al arrancar y no migra.

Si la base tiene datos, crear un respaldo privado **antes** de cualquier DDL y comprobar que `pg_dump` terminó sin error. Restaurar para ensayo sólo en una base diferente y vacía; nunca usar `pg_restore --clean` sobre la demostración:

```powershell
$pgDump = 'C:\Program Files\PostgreSQL\17\bin\pg_dump.exe'
& $pgDump -h localhost -p 5432 -U TU_USUARIO -d eventosuy_final -Fc -f 'RUTA_PRIVADA\eventosuy_final.dump'
if ($LASTEXITCODE -ne 0) { throw 'Falló el respaldo; no continuar con SQL' }
```

Mantener los `.dump` fuera de Git. Verificar manualmente los datos que exige la defensa; las tablas creadas no aportan por sí solas asistentes, ediciones ni registros.

## 5. Comprobaciones antes del ensayo

```powershell
java -version
mvn -version
Test-NetConnection IP_CENTRAL -Port 9128
curl.exe --cacert 'RUTA_CERTIFICADO_PUBLICO' 'https://IP_CENTRAL:9128/portal?wsdl'
Test-NetConnection IP_WEB -Port 8080
```

Desde PC 3 y el teléfono, abrir en navegador `http://IP_WEB:8080/web/inicio` y `http://IP_WEB:8080/web/movil`. Si se usa VPN, confirmar que los nodos están conectados y autorizados; si no se acepta como «misma red», usar LAN. Mantener 5432 sin acceso remoto cuando Central y PostgreSQL comparten PC. Toda IP, puerto, certificado y regla de firewall de la instalación final **queda pendiente de verificar en los equipos reales**.
