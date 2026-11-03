# eventos.uy – Laboratorio 2 (Grupo 04)

Programación de Aplicaciones · Tecnólogo en Informática · UTEC · 2026

## Requisitos

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
| `webapp` | Sitio web (servlets y JSP), que lleva el JAR de la lógica dentro |

La estación de trabajo y la web comparten la misma base PostgreSQL.

## 1. Crear la base de datos

Crear una base vacía llamada `eventosuy`. Con pgAdmin: *Databases → Create → Database*. O desde la terminal:

```
psql -U postgres -c "CREATE DATABASE eventosuy;"
```

Las tablas las crea Hibernate la primera vez que se inicializa JPA. Los índices
funcionales H13 **no** los crea Hibernate: antes de cargar datos o habilitar la
aplicación en una base nueva hay que preparar el esquema y aplicar el SQL según
[el procedimiento H13](docs/migraciones/H13-procedimiento.md). No repetirlo sobre
índices existentes. La aplicación en otra base requiere su revisión y autorización;
las pruebas automatizadas solo utilizan `eventosuy_test`.

### Credenciales

Por defecto el sistema se conecta con:

| Dato | Valor por defecto | Variable de entorno |
|---|---|---|
| Servidor | `localhost` | `EVENTOS_DB_HOST` |
| Puerto | `5432` | `EVENTOS_DB_PORT` |
| Base | `eventosuy` | `EVENTOS_DB_NAME` |
| Usuario | `postgres` | `EVENTOS_DB_USER` |
| Contraseña | `root` | `EVENTOS_DB_PASSWORD` |

Si la instalación de PostgreSQL usa otros valores, definir las variables de entorno en la
configuración de ejecución de IntelliJ (campo *Environment variables*).

## 2. Compilar

Verificar `java -version` y `mvn -version`: ambos deben utilizar JDK 25. En
IntelliJ, configurar Java 25 para el proyecto, Maven Runner y las aplicaciones,
y ejecutar **Maven → Reload All Maven Projects**.

En una instalación nueva, compilar primero **sin ejecutar pruebas**:

```powershell
mvn clean install -DskipTests
```

Esto genera `logica/target/logica-1.0-SNAPSHOT.jar`,
`estacion-trabajo/target/estacion-trabajo-1.0-SNAPSHOT.jar` y
`webapp/target/webapp-1.0-SNAPSHOT.war`. El WAR incluye la lógica y sus dependencias.
Todavía no inicializa una base ni ejecuta el seed.

Después crear/configurar `eventosuy_test`, inicializar su esquema con la protección
JDBC y aplicar H13 siguiendo [el procedimiento](docs/migraciones/H13-procedimiento.md).
**Solo después**, y con todas las variables de conexión de pruebas configuradas en
el mismo proceso Maven, ejecutar:

```powershell
$env:EVENTOS_DB_NAME = 'eventosuy_test'
mvn -Pcobertura clean install
```

Configurar también `EVENTOS_DB_HOST`, `EVENTOS_DB_PORT`, `EVENTOS_DB_USER` y
`EVENTOS_DB_PASSWORD` cuando difieran de los valores locales. No registrar ni
versionar contraseñas reales. Sin la base de pruebas verificada, H02 bloquea la
suite antes de JPA. No ejecutar `install` con pruebas contra la base habitual.

Node.js solo se necesita para las pruebas adicionales de JavaScript:
`node --test webapp/src/test/js/*.test.cjs`. La aplicación no lo necesita al ejecutarse.

## 3. Cargar los datos de prueba

Ejecutar `logica/src/main/java/datos/CargarDatosPrueba.java` (clic derecho → *Run*). Se puede
repetir: lo que ya existe se omite. Para partir de cero, borrar la base y volver a crearla.

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

Ejecutar `estacion-trabajo/src/main/java/swing/principal.java` (método `main`). El administrador
no tiene usuario ni contraseña: entra directamente. El menú **Eventos → Aceptar/Rechazar
Edición** pasa una edición de Ingresada a Aceptada o a Rechazada.

## 5. Sitio web

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
controlada según [el procedimiento H13](docs/migraciones/H13-procedimiento.md).
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
transitiva de Swing y del WAR; no basta copiar solo el JAR de lógica al ejecutar.
No se migran ni reescriben imágenes existentes. Si una imagen antigua no supera
la validación, Swing muestra «Vista previa no disponible»; su recuperación
mantiene los bytes almacenados. Una nueva asociación sí debe cumplir los límites.

| Síntoma | Causa |
|---|---|
| Error 404 en `/` | La aplicación no arrancó: mirar la consola de Tomcat |
| «No se pudo conectar a PostgreSQL» | El servicio no está iniciado, la base no existe o la contraseña no coincide |
| «No suitable driver found» | Reconstruir el proyecto (`install`) y reiniciar Tomcat |
| «Address already in use» | El puerto 8080 está ocupado: cambiarlo en la configuración |
| El seed omite todo | Los datos ya existían: recrear la base para empezar de cero |
