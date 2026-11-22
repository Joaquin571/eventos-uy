# Eventos UY Laboratorio 3: despliegue y defensa

Guía para preparar **otra computadora** a partir del código de `lab3-integracion-equipo`. Completá los marcadores `IP_CENTRAL`, `IP_WEB`, `TU_USUARIO` y `TU_PASSWORD` en configuraciones privadas; ninguno es un valor operativo. Ver [configuraciones de ejemplo](docs/CONFIGURACION_EJEMPLOS.md), [checklist de defensa](docs/DEFENSA_CHECKLIST.md) y [arquitectura](docs/lab3/DEFENSA_ARQUITECTURA.md). La [letra oficial](Laboratorio%203.pdf), §§3, 7.9 y 8, exige cuatro dispositivos en la misma red y una defensa sin IntelliJ.

## 1. Topología y requisitos

| Equipo | Función | Conexión |
| --- | --- | --- |
| PC 1 | PostgreSQL, `servidor.jar`, SOAP HTTPS y Swing en una JVM | Central conecta a PostgreSQL por `localhost`; recibe SOAP de PC 2. |
| PC 2 | Tomcat 11 con `web.war` | Webapp llama a Central por SOAP HTTPS; no necesita JPA ni acceso JDBC. |
| PC 3 | Navegador de escritorio | Abre la Webapp por la IP de PC 2. |
| Teléfono | Navegador para `/web/movil` | Abre la misma Webapp por la IP de PC 2. |

Se necesita **JDK 25** para compilar y ejecutar las clases de este proyecto: `pom.xml` fija `maven.compiler.release=25`. Usá Maven compatible con el JDK instalado y comprobá `java -version` y `mvn -version`. El [POM de Webapp](webapp/pom.xml) declara Servlet 6.1 y JSP 4.0 como `provided`; **Tomcat 11** implementa esas API ([guía oficial](https://tomcat.apache.org/migration-11.0.html)). El despliegue documentado usa PostgreSQL 17; el [driver JDBC](logica/pom.xml) y el código exigen PostgreSQL, sin fijar una versión exacta del servidor. Verificá la versión real de cada equipo antes del ensayo.

El código usa un único listener HTTPS de Central para `/eventos`, `/sesiones` y `/portal`, configurables mediante propiedades. La Webapp usa principalmente `/portal` y `/sesiones`; `ClienteEventosCentral` aún puede usar `/eventos` para la portada. Swing llama a `ISistema` directamente dentro del proceso Central. [Detalle por método](docs/lab3/DEFENSA_ARQUITECTURA.md).

La red local física es la opción que cumple literalmente la letra. Una VPN de Tailscale puede servir para un ensayo, pero **su aceptación como «misma red» debe confirmarla el docente**. Si se elige LAN, el certificado de Central debe contener la IP o DNS LAN real en SAN. Si se elige VPN, cada dispositivo que acceda por ella debe estar conectado y autorizado. El video de YouTube requiere acceso al proveedor; una LAN aislada puede demostrar el resto de los casos sin reproducirlo.

## 2. Construir y distribuir artefactos

Desde la raíz del repositorio, con JDK 25 y dependencias Maven disponibles:

```powershell
java -version
mvn -version
mvn '-DskipTests' clean package
```

El [POM de Central](servidor-central/pom.xml) configura Shade con `Main-Class: central.Main` y produce `servidor-central/target/servidor.jar`. El [POM de Webapp](webapp/pom.xml) produce `webapp/target/web.war`; las dependencias a `logica` y `servidor-central` son **solo de prueba**. Si sólo se necesita reconstruir un módulo con sus dependencias, usar `mvn -pl servidor-central -am '-DskipTests' package` o `mvn -pl webapp -am '-DskipTests' package`. `-DskipTests` evita ejecutar JUnit, pero Maven puede compilar las pruebas.

Copiar `servidor.jar` a PC 1 y `web.war` a PC 2. Si PC 1 preparará una base nueva, transferirle también los **SQL versionados que se hayan determinado necesarios**, conservando sus nombres y verificando sus hashes. No copiar a PC 2 la base, el archivo de configuración de Central ni su clave privada. Comprobar nombre, fecha y hash de ambos artefactos en el equipo de destino. La compilación de la Webapp usa WSDL/XSD versionados en `webapp/src/main/resources/wsdl`; no requiere que Central esté encendido para `wsimport`.

## 3. Configuración privada y arranque de Central

En **la cuenta que ejecutará el JAR** en PC 1, crear `%USERPROFILE%\.eventosUy\central.properties` a partir de [central.properties.example](servidor-central/src/main/resources/central.properties.example). `central.Main.main` lo carga desde `user.home/.eventosUy/central.properties`. Configurar:

- `central.host`: host/IP que se publica en las URL WSDL y que usará PC 2.
- `central.escucha`: interfaz local donde abre el socket HTTPS; usar la IP real de PC 1 para esa red. `0.0.0.0` escucha en todas las interfaces y exige reglas de firewall apropiadas.
- `central.puerto`, `central.ruta`, `central.ruta.sesiones` y, de forma explícita, `central.ruta.portal`: puerto y rutas diferentes, no anidadas.
- `sesiones.duracion.segundos`: entero positivo.
- `tls.almacen`: ruta **absoluta** a un PKCS#12 con una sola clave privada y cadena vigente; `tls.almacen.password` y `tls.clave.password` quedan sólo en el archivo privado.
- `db.url`, `db.usuario`, `db.password`: conexión PostgreSQL de PC 1. Una instalación local puede usar `jdbc:postgresql://localhost:5432/eventosuy_final`.

El certificado debe incluir en SAN **el mismo host/IP que figura en `central.host` y en `web.properties`**. Distribuir a PC 2 únicamente el certificado público y comprobar su huella antes de importarlo en un truststore privado de Tomcat. No copiar el PKCS#12 con la clave privada al equipo Web. [Comandos de ejemplo](docs/CONFIGURACION_EJEMPLOS.md#2-certificado-y-confianza-tls).

`ConfiguracionCentral.propiedadesJpa` construye las propiedades JDBC y fija `hibernate.hbm2ddl.auto=validate`. Por eso Central **no prepara ni migra** la base: el esquema debe existir antes del arranque. Con la base y TLS ya preparados, en la carpeta de PC 1 donde se copió el JAR:

```powershell
java -jar .\servidor.jar
```

`central.Main.main` inicia JPA, publica los tres endpoints HTTPS y abre Swing en la misma JVM. `--sin-swing` es una opción de diagnóstico, no sirve para demostrar la Estación de Trabajo. El programa imprime las tres URL WSDL. En PC 2, comprobar conectividad TCP y después abrir `https://IP_CENTRAL:9128/portal?wsdl` con confianza TLS y SAN correctos. Un puerto abierto por sí solo no prueba que SOAP funcione. Si se cambia IP, certificado, rutas o truststore, reiniciar los procesos afectados.

## 4. PostgreSQL en una PC nueva

Instalar PostgreSQL en PC 1 y mantener 5432 accesible sólo localmente si Central corre en esa PC. Crear una base de demostración **nueva**, por ejemplo `eventosuy_final`, con usuario autorizado. Antes de cualquier preparación o SQL, verificar en `psql` el host, puerto, usuario, `current_database()` y `current_schema()`; hacer un respaldo si ya hay datos. No ejecutar pruebas automatizadas sobre esa base: reservar `eventosuy_test` para JUnit.

`persistencia.PrepararEsquemaNuevo --base-vacia`, dentro de `servidor.jar`, lee `EVENTOS_DB_HOST`, `EVENTOS_DB_PORT`, `EVENTOS_DB_NAME`, `EVENTOS_DB_USER` y `EVENTOS_DB_PASSWORD` del **proceso preparador**. Rechaza un esquema que ya tenga tablas y crea las tablas JPA del modelo actual. No volver a ejecutarlo sobre una instalación existente. El servidor normal usa `db.*` de `central.properties`, no esas variables.

Después de preparar una base vacía, inspeccionar columnas, restricciones, tablas e índices **antes** de decidir qué SQL falta. En particular, verificar `registro.asistenciaconfirmada`, `edicion.fechaarchivo`, `edicion.videoyoutube`, `edicion.descripcion`, `evento.visitas` y `evento.finalizado`. La tabla de seguidores se define aparte en `logica/src/main/resources/db/seguimiento-usuario.sql`; los índices de unicidad sin distinguir mayúsculas están en `logica/src/main/resources/db/migration/V20261116_02__unicidad_ci.sql`. Aplicar esos scripts en la base verificada, revisando primero duplicados para los índices. `V20261116_01__visitas_evento.sql`, `sql/lab3-adrian-asistencia.sql`, `sql/lab3-emmanuel-video.sql` y `sql/lab3-emmanuel-busqueda.sql` sirven para **esquemas previos** si sus columnas o restricciones faltan; no ejecutar todos los scripts a ciegas sobre la base recién creada. `sql/lab3-emmanuel-finalizacion.sql` repite la columna `finalizado` del script de visitas. [Procedimiento y comandos](docs/CONFIGURACION_EJEMPLOS.md#4-postgresql-base-nueva-y-respaldo).

Arrancar Central sólo cuando la validación JPA del esquema pueda completarse. Preparar datos de demostración para asistentes, organizadores, seguidores, video, evento finalizado, edición archivable/archivada y registros con y sin asistencia; **la cobertura de datos debe comprobarse manualmente**. Hacer `pg_dump -Fc` antes de operar sobre la base de defensa y ensayar restauración en otra base, sin `--clean` sobre la original.

## 5. Tomcat en PC 2

En la cuenta que ejecuta Tomcat, crear `%USERPROFILE%\.eventosUy\web.properties` desde [web.properties.example](webapp/src/main/resources/web.properties.example). Definir `central.host=IP_CENTRAL`, `central.puerto=9128` y las rutas del servicio. Todos los clientes SOAP (Eventos, Portal y Sesiones) admiten el mismo archivo alternativo con `-Deventos.web.config=...`. Los clientes rechazan destinos que no sean HTTPS.

Crear un truststore PKCS#12 nuevo en PC 2 con el certificado **público** de Central y configurar `javax.net.ssl.trustStore`, `javax.net.ssl.trustStoreType=PKCS12` y `javax.net.ssl.trustStorePassword` en la JVM de Tomcat, fuera del repositorio. Para Tomcat iniciado mediante scripts, `bin/setenv.bat` puede definir `CATALINA_OPTS`; un servicio Windows requiere configurar su JVM por separado. No desactivar la validación TLS. Consultar [ejemplos](docs/CONFIGURACION_EJEMPLOS.md#3-tomcat-11-independiente).

Con Tomcat detenido, copiar `web.war` al directorio `webapps` de la instalación elegida e iniciarlo con `bin/catalina.bat run` o `bin/startup.bat`. El contexto normal del archivo `web.war` es `/web`: desde PC 3 y el teléfono abrir `http://IP_WEB:8080/web/inicio` y `http://IP_WEB:8080/web/movil`. Si se despliega como `ROOT.war`, las rutas empiezan en `/`. Confirmar el puerto HTTP real de Tomcat: 8080 es el valor de ejemplo, no una constante del proyecto. Revisar `logs` si hay 404, TLS o errores SOAP.

## 6. Red, comprobación y pendientes de defensa

| Puerto de ejemplo | Servicio | Regla recomendada |
| --- | --- | --- |
| 5432 TCP | PostgreSQL en PC 1 | Sólo loopback si Central comparte equipo. |
| 9128 TCP | SOAP HTTPS en PC 1 | Permitir desde PC 2 por la red de defensa. |
| 8080 TCP | Tomcat en PC 2 | Permitir desde PC 3 y teléfono por la red de defensa. |

Comprobar en orden: dirección efectiva de PC 1 y PC 2 → listener y firewall → `?wsdl` con certificado confiable → portada Web → login → consulta y una operación real → Swing → persistencia tras reiniciar Central. `Test-NetConnection IP_CENTRAL -Port 9128` comprueba TCP; `curl.exe --cacert CERT_PUBLICO "https://IP_CENTRAL:9128/portal?wsdl"` comprueba HTTPS y la respuesta WSDL. `CERT_PUBLICO` es la ruta al certificado exportado; no usar `-k`. Guardar evidencia sin capturar contraseñas ni encabezados de sesión.

Antes de declarar lista la defensa quedan **pendientes de verificar en los equipos reales**: aceptación docente de VPN si se usa; comunicación de los cuatro dispositivos sin IDE; certificado con SAN de la red elegida; truststore y handshake de PC 2; reglas de firewall; WSDL y casos Web/Swing; datos completos de la base de demostración y respaldo restaurable. El código ofrece `/movil` mediante `MovilInicioServlet`; su navegación real debe ensayarse en el teléfono. Esta guía no acredita por sí sola que el despliegue físico ya funcione.
