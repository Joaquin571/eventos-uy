# eventos.uy – Laboratorio 2 (Grupo 04)

Programación de Aplicaciones · Tecnólogo en Informática · UTEC · 2026

## Requisitos

- JDK 25
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

Las tablas las crea Hibernate solo la primera vez que se usa el sistema.

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

En IntelliJ: panel **Maven → Reload All Maven Projects**, y luego
`eventos-uy → Lifecycle → clean` e `install`. El SDK del proyecto tiene que ser Java 25.

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

## 7. Recorrido sugerido

| Rol | Qué mostrar |
|---|---|
| Visitante | Inicio, categorías, lista de usuarios, perfiles, registrarse |
| Asistente (`juan23`) | Su perfil con sus registros; un asistente no entra a las páginas de organizador (error 403) |
| Organizador (`imm`) | Su perfil con las ediciones Aceptada, Ingresada y Rechazada; alta de institución |
| Administrador | Estación de trabajo: aceptar o rechazar una edición |

## Problemas frecuentes

| Síntoma | Causa |
|---|---|
| Error 404 en `/` | La aplicación no arrancó: mirar la consola de Tomcat |
| «No se pudo conectar a PostgreSQL» | El servicio no está iniciado, la base no existe o la contraseña no coincide |
| «No suitable driver found» | Reconstruir el proyecto (`install`) y reiniciar Tomcat |
| «Address already in use» | El puerto 8080 está ocupado: cambiarlo en la configuración |
| El seed omite todo | Los datos ya existían: recrear la base para empezar de cero |