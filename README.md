# eventos.uy – Laboratorio 3 (Grupo 04)

Estado verificado y límites: [auditoría Lab 3](AUDITORIA_LAB3.md). Preparación adicional:
[ensayo local con un comando](docs/ENSAYO_LOCAL.md),
[datos reproducibles](docs/DATOS_DEMO_LAB3.md),
[pruebas LAN/Tailscale y fallos](docs/PRUEBAS_RED_LAB3.md) y
[modelo y secuencias actuales](docs/lab3/ANALISIS_DISENO_ACTUAL.md).

## Instalación vigente del Laboratorio 3

Requiere JDK 25, Maven 3, PostgreSQL y Tomcat 11. Para el preparador de esquema,
configurar `EVENTOS_DB_HOST`, `EVENTOS_DB_PORT`, `EVENTOS_DB_NAME`,
`EVENTOS_DB_USER` y `EVENTOS_DB_PASSWORD` sin valores predeterminados.
Central usa `~/.eventosUy/central.properties` (plantilla en
`servidor-central/src/main/resources/central.properties.example`); allí se
configuran `db.url`, usuario, clave, host de publicación, interfaz de escucha,
puerto y PKCS#12. La dirección de PostgreSQL puede ser remota. La plantilla
contiene `eventosuy_test` solo como ejemplo: sustituirla por la base prevista.

Para una **base nueva y vacía**, compilar y ejecutar el preparador explícito
con las variables de esa base configuradas:

```powershell
mvn -DskipTests package
java -cp servidor-central/target/servidor.jar persistencia.PrepararEsquemaNuevo --base-vacia
```

El preparador se niega a actuar si el esquema ya contiene tablas. Aplicar con
`psql -v ON_ERROR_STOP=1 -f` los archivos
`logica/src/main/resources/db/migration/V20261116_01__visitas_evento.sql` y
`V20261116_02__unicidad_ci.sql`, en ese orden, sobre la base prevista. El segundo
requiere resolver antes cualquier correo o nombre de tipo duplicado sin distinguir
mayúsculas. En una base **existente**, conservar sus tablas y aplicar solo las
migraciones pendientes después de revisar respaldo, esquema y duplicados.
`Main` usa `validate` y nunca crea tablas en el arranque.

Configurar TLS y confianza según
[arranque HTTPS](docs/lab3/ARRANQUE-CENTRAL-HTTPS.md). Ejecutar
`java -jar servidor-central/target/servidor.jar` para Central con Swing, o
agregar `--sin-swing` para ejecutarlo sin ventana. Swing comparte proceso y
`ISistema` con Central; una máquina distinta ejecuta Tomcat y `web.war`, no
Swing separado. En Tomcat, crear `~/.eventosUy/web.properties` desde
`webapp/src/main/resources/web.properties.example`, apuntar al host y puerto
HTTPS del Central y configurar el truststore público en su JVM. Desplegar
`webapp/target/web.war`; no requiere acceso JDBC en Tomcat.

Las pruebas persistentes solo se ejecutan con `EVENTOS_DB_NAME=eventosuy_test`
y verifican `current_database()` por JDBC. La validación física entre máquinas
requiere probar SAN, firewall, SOAP, Tomcat y navegador en la LAN de entrega.

Las secciones de Laboratorio 2 que siguen son históricas y no describen el
procedimiento de instalación del Laboratorio 3.

Para ensayar en un esquema aislado de `eventosuy_test`, crear primero un esquema
vacío y ejecutar el preparador con `--base-vacia --schema nombre_del_esquema`.
Configurar `db.url` de Central con `?currentSchema=nombre_del_esquema,public`.
Aplicar los SQL en una sesión `psql` con `SET search_path TO nombre_del_esquema`
y comprobar `SELECT current_database()` antes de escribir. Al terminar el ensayo,
retirar únicamente ese esquema temporal.

Programación de Aplicaciones · Tecnólogo en Informática · UTEC · 2026

## Arquitectura vigente

En `lab3-desarrollo`, todos los Servlet consumen Central mediante SOAP/HTTPS.
Tomcat no ejecuta lógica ni persistencia; Swing llama a ISistema en el proceso
de Central. Se conservan Actor, sesiones P03, Java 25 y Metro 4.0.5.

Ver [resultado, pruebas, commits e instrucciones vigentes](docs/lab3/INTEGRACION-2026-11-15.md)
y [configuración TLS externa](docs/lab3/ARRANQUE-CENTRAL-HTTPS.md).
El WAR se genera como `webapp/target/web.war` y Central como
`servidor-central/target/servidor.jar`.

Las secciones siguientes conservan la referencia histórica del Laboratorio 2.
Para desplegar Laboratorio 3, usar las instrucciones del informe vigente;
PostgreSQL y los lectores de imágenes pertenecen exclusivamente a Central.

## Referencia histórica del Laboratorio 2

Los pasos de esta sección documentan el proyecto anterior. Para instalar el
Laboratorio 3 se utiliza el procedimiento al comienzo de este README.

### Requisitos históricos

- JDK 25
- Apache Maven 3 (en el PATH o el Maven integrado de IntelliJ)
- PostgreSQL (servicio iniciado, puerto 5432)
- Apache Tomcat 11, en su versión compilada (zip *Core*, no el *source*)
- IntelliJ IDEA (Ultimate, o Community con el plugin Smart Tomcat)

## Estructura

| Módulo | Qué es |
|---|---|
| `logica` | JAR con las reglas de negocio, los tipos de datos y el acceso a PostgreSQL (JPA/Hibernate) |
| `estacion-trabajo` | Aplicación Swing del administrador |
| `webapp` | Sitio web (servlets y JSP); en Laboratorio 3 consume Central por SOAP/HTTPS |

En Laboratorio 3, solamente Central accede a PostgreSQL; Swing está en su proceso.

## 1. Crear la base de datos

Crear una base vacía llamada `eventosuy`. Con pgAdmin: *Databases → Create → Database*. O desde la terminal:

```
psql -U postgres -c "CREATE DATABASE eventosuy;"
```

Para Laboratorio 3, `Main` valida un esquema existente. El preparador y los dos
SQL versionados se describen al comienzo de este README.

### Credenciales

La tabla siguiente documenta valores utilizados en la versión anterior;
**no son valores predeterminados del Laboratorio 3**:

| Dato | Valor por defecto | Variable de entorno |
|---|---|---|
| Servidor | `localhost` | `EVENTOS_DB_HOST` |
| Puerto | `5432` | `EVENTOS_DB_PORT` |
| Base | `eventosuy` | `EVENTOS_DB_NAME` |
| Usuario | `postgres` | `EVENTOS_DB_USER` |
| Contraseña | Sin valor vigente | `EVENTOS_DB_PASSWORD` |

El preparador actual exige las cinco variables; Central toma sus credenciales
de `central.properties`.

## 2. Compilar

Verificar `java -version` y `mvn -version`: ambos deben utilizar JDK 25. En
IntelliJ, configurar Java 25 para el proyecto, Maven Runner y las aplicaciones,
y ejecutar **Maven → Reload All Maven Projects**.

En una instalación nueva, compilar primero **sin ejecutar pruebas**:

```powershell
mvn clean install -DskipTests
```

En Laboratorio 3 los artefactos para distribuir son
`servidor-central/target/servidor.jar` y `webapp/target/web.war`.
El WAR no incluye lógica ni persistencia.

Después crear/configurar `eventosuy_test`, inicializar su esquema con la protección
JDBC y aplicar los dos SQL indicados al comienzo de este README.
**Solo después**, y con todas las variables de conexión de pruebas configuradas en
el mismo proceso Maven, ejecutar:

```powershell
$env:EVENTOS_DB_NAME = 'eventosuy_test'
mvn -Pcobertura clean install
```

Configurar también `EVENTOS_DB_HOST`, `EVENTOS_DB_PORT`, `EVENTOS_DB_USER` y
`EVENTOS_DB_PASSWORD`. No registrar ni
versionar contraseñas reales. Sin la base de pruebas verificada, H02 bloquea la
suite antes de JPA. No ejecutar `install` con pruebas contra la base habitual.

Node.js solo se necesita para las pruebas adicionales de JavaScript:
`node --test webapp/src/test/js/*.test.cjs`. La aplicación no lo necesita al ejecutarse.

## 3. Cargar los datos de prueba

El seed histórico está en `logica/src/main/java/datos/CargarDatosPrueba.java`.
No cubre todos los casos nuevos del Laboratorio 3 y no debe utilizarse para
reinicializar una base con datos existentes.

| Usuario | Rol | Contraseña |
|---|---|---|
| `imm` | Organizador | `1234` |
| `mvdcomics` | Organizador | `1234` |
| `juan23` | Asistente (UTEC) | `1234` |
| `maria88` | Asistente (sin institución) | `1234` |
| `ana_utec` | Asistente (UTEC) | `1234` |

Los datos cubren: los dos roles, ediciones en los tres estados (Aceptada, Ingresada y Rechazada),
tipos de registro, instituciones, un patrocinio (código `UTEC2026`), un registro general y un
registro con código de patrocinio.

## 4. Estación de trabajo

En Laboratorio 3, `java -jar servidor-central/target/servidor.jar` abre Swing. El administrador
no tiene usuario ni contraseña: entra directamente. El menú **Eventos → Aceptar/Rechazar
Edición** pasa una edición de Ingresada a Aceptada o a Rechazada.

## 5. Sitio web

La configuración siguiente corresponde al desarrollo del Laboratorio 2; para
Laboratorio 3 desplegar `webapp/target/web.war` en Tomcat 11.

1. **Run → Edit Configurations → + → Smart Tomcat**.
2. *Tomcat server*: la carpeta de Tomcat que contiene `bin` y `lib` (con `lib/catalina.jar`).
3. *Deployment directory*: `webapp/src/main/webapp`.
4. *Use classpath of module*: `webapp`. *Context path*: `/`. *Server port*: `8080`. JRE: Java 25.
5. Ejecutar y abrir <http://localhost:8080/>.

El filtro de acceso aplica el rol: visitante, asistente u organizador.

## 6. Pruebas

Las pruebas JUnit están en `logica/src/test/java` y usan una base aparte:

1. Crear la base `eventosuy_test` (vacía).
2. En la configuración de JUnit (o su plantilla), definir `EVENTOS_DB_NAME=eventosuy_test`.
3. Ejecutar con *Run Tests with Coverage*.

Los índices funcionales de H13 deben existir antes de ejecutar las nuevas pruebas
de concurrencia. En una base `eventosuy_test` nueva, primero compilar sin ejecutar
pruebas, inicializar el esquema con la protección JDBC y aplicar la migración
controlada mediante los SQL versionados indicados arriba.
JUnit no ejecuta migraciones. En la base de pruebas local ya se aplicó el 03/11/2026;
no repetir el script sobre índices existentes. No se aplicó en otra base.

Las clases JUnit de persistencia heredan una protección común que comprueba por JDBC
`SELECT current_database()` antes de inicializar las pruebas o JPA. Sin
`EVENTOS_DB_NAME=eventosuy_test`, si la URL no coincide o si la base efectiva no puede
verificarse, la ejecución falla; no se omite silenciosamente. También se aplica al
ejecutar una clase individual desde IntelliJ. Las pruebas puras no necesitan base.

Los usuarios nuevos guardan su fecha de alta automáticamente. Los anteriores sin
fecha persistida muestran «No registrada»: no se asigna una fecha histórica ficticia.

## 7. Recorrido sugerido

| Rol | Qué mostrar |
|---|---|
| Visitante | Inicio, categorías, lista de usuarios, perfiles, registrarse |
| Asistente (`juan23`) | Su perfil con sus registros; un asistente no entra a las páginas de organizador (error 403) |
| Organizador (`imm`) | Su perfil con las ediciones Aceptada, Ingresada y Rechazada; alta de institución |
| Administrador | Estación de trabajo: aceptar o rechazar una edición |

## Problemas frecuentes

### Validación de imágenes

Se conservan los bytes originales de PNG, JPEG, GIF y WEBP. Las nuevas cargas
se decodifican completas y se rechazan archivos corruptos o incompletos. Límites:
2 MiB comprimidos, 4096 píxeles por lado, 8 millones de píxeles por imagen,
100 fotogramas y 32 millones de píxeles acumulados por animación.
El lector WEBP es `com.twelvemonkeys.imageio:imageio-webp:3.15.3`, dependencia
de Central y Swing. No se incluye en el WAR de Laboratorio 3.
No se migran ni reescriben imágenes existentes. Si una imagen antigua no supera
la validación, Swing muestra «Vista previa no disponible»; su recuperación
mantiene los bytes almacenados. Una nueva asociación sí debe cumplir los límites.

| Síntoma | Causa |
|---|---|
| Error 404 en `/` | La aplicación no arrancó: mirar la consola de Tomcat |
| «No se pudo conectar a PostgreSQL» | El servicio no está iniciado, la base no existe o la contraseña no coincide |
| «No suitable driver found» | En Laboratorio 3, revisar el JAR de Central y su configuración JDBC; Tomcat no usa driver PostgreSQL |
| «Address already in use» | El puerto 8080 está ocupado: cambiarlo en la configuración |
| El seed omite todo | Los datos ya existían; revisar la base elegida sin borrar datos |
