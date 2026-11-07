# Primer corte vertical SOAP — listado público de eventos

## Alcance implementado

Se expone únicamente `ISistema.listarEventos()` para la portada existente (`GET /` y `/inicio`, relativos al contexto web). Es una consulta pública de solo lectura, sin identidad ni parámetros sensibles. Permite comprobar una lista de DTO con categorías, texto Unicode y fechas, sin cambiar reglas ni incorporar casos funcionales del Lab 3.

```text
InicioServlet → ClienteEventosCentral → proxy wsimport → SOAP HTTP
  → EventosPublicador → Fábrica → ISistema.listarEventos → lógica/JPA
```

`EventoSoap` contiene nombre, sigla, descripción, fechaAlta ISO-8601 opcional y lista de categorías. Tiene constructor vacío y getters/setters; no referencia entidades JPA. No se modificó `ISistema` ni `DtEvento`. La portada consume directamente el DTO generado y conserva su JSP y filtro por categoría.

El publicador declara `ConsultaException`, que produce un fault de contrato con mensaje genérico ante un error interno. La web muestra una vista propia y HTTP 503; no muestra faultString, Hibernate ni JDBC y no intenta usar lógica local como respaldo de esta consulta. Cada pedido obtiene un puerto nuevo y configura endpoint y timeouts.

**Etapa transitoria:** `webapp` conserva su dependencia de `logica`. `ContextoListener` sigue inicializando JPA, `FiltroAcceso` sigue leyendo categorías de la lógica local y el resto de los servlets conserva acceso local, incluidas imágenes y detalle de evento. Por eso este corte **no cumple todavía la separación final Web/Central del laboratorio**, ni permite retirar la configuración PostgreSQL de Tomcat. Solo el listado de eventos de la portada fue migrado. La portada también puede generar peticiones de imágenes que todavía son locales.

## Configuración sin secretos versionados

No se crean ni sobrescriben archivos personales automáticamente. Crear manualmente `~/.eventosUy` para el usuario del Central y para el usuario que ejecuta Tomcat. En Windows, `~` suele ser `C:\Users\<usuario>`; si Tomcat es un servicio puede corresponder a otra cuenta.

1. Copiar `servidor-central/src/main/resources/central.properties.example` como `~/.eventosUy/central.properties`.
2. Completar `central.host`, `central.puerto`, `central.ruta`, `db.url`, `db.usuario` y `db.password` con los valores del entorno autorizado. No versionar ese archivo con credenciales reales.
3. Copiar `webapp/src/main/resources/web.properties.example` como `~/.eventosUy/web.properties` en la cuenta de Tomcat. Host, puerto y ruta deben apuntar al Central.

Ejemplo del endpoint local de las plantillas: host `127.0.0.1`, puerto `9128`, ruta `/eventos`; URL `http://127.0.0.1:9128/eventos`. Son valores de ejemplo editables en archivos, no direcciones fijadas en el código Java. No usar `127.0.0.1` como destino remoto.

La plantilla del Central apunta a **eventosuy_test**, con credenciales sin completar. **No apuntarla a la base habitual para estas verificaciones.** El esquema debe existir: el nuevo arranque pasa conexión externa a EMF y fuerza `hibernate.hbm2ddl.auto=validate`; no crea ni migra tablas. Las operaciones administrativas existentes de Swing sí pueden escribir si el usuario las ejecuta: para comprobar este corte limitarse al listado y consultas.

La sobrecarga `BaseDeDatos.inicializar(Map)` es el único cambio de infraestructura de lógica. No usa credenciales de respaldo, no sustituye una conexión ya inicializada y valida el esquema. El arranque antiguo sin parámetros permanece para los clientes todavía no migrados; su configuración y comportamiento del Lab 2 no se cambiaron. Las propiedades externas de este corte no son todavía la migración global de configuración del laboratorio.

## Compilar y ejecutar

Usar JDK **25**, verificando `java -version` y `mvn -version`. En PowerShell, si JAVA_HOME apunta a otra versión, seleccionar la instalación real de Java 25 antes de ejecutar Maven.

Para compilar y empaquetar sin ejecutar las pruebas históricas que escriben en PostgreSQL:

```powershell
mvn -DskipTests package
```

Produce `servidor-central/target/servidor.jar` y el WAR de `webapp/target/` con el nombre que conserva su POM actual (`webapp-1.0-SNAPSHOT.war`). El nombre final `web.war` queda para el cierre del empaquetado distribuido; este corte no renombra el despliegue existente. `servidor.jar` incluye lógica, Swing, Metro y sus dependencias.

Iniciar el Central después de completar la configuración y comprobar que la base de pruebas tiene el esquema existente:

```powershell
java -jar servidor-central/target/servidor.jar
```

El proceso inicializa JPA desde propiedades, publica SOAP mediante `Endpoint.publish` y **después** invoca el arranque Swing existente. No necesita Tomcat. Cerrar la ventana principal termina el proceso existente; el shutdown hook detiene endpoint y JPA.

Desplegar el WAR en Tomcat 11 como se hacía previamente, o copiarlo a `webapps` sin IDE. Conservar la conexión de pruebas requerida por los casos web aún locales. El contexto depende del nombre/configuración de despliegue: abrir `<base-real-del-sitio>/inicio`, no asumir que es `/webapp`. La URL SOAP y la URL del navegador web pertenecen a procesos diferentes.

## Comprobación manual local y en otra computadora

1. Iniciar Central. Con las plantillas locales, abrir `http://127.0.0.1:9128/eventos?wsdl`; debe aparecer `EventosService`, operación `listarEventos` y fault declarado. El XSD referenciado muestra exclusivamente datos de transporte.
2. Iniciar Tomcat y abrir la portada real `/inicio`. El listado y el filtro `?categoria=<nombre-codificado>` deben conservar su contenido. Una lista vacía es válida si la base no contiene eventos; no cargar seed automáticamente.
3. Detener Central, manteniendo PostgreSQL y Tomcat disponibles. Al recargar `/inicio`, la consulta debe mostrar página propia de indisponibilidad con HTTP 503. La lectura local de categorías del filtro sigue siendo un prerrequisito de esta etapa.
4. Reiniciar Central y recargar: vuelve a consultar por SOAP, sin reiniciar Tomcat.
5. Para otra computadora, cambiar `central.host` del Central por su **IP LAN real**, reiniciarlo y abrir desde ese equipo `http://<IP-CENTRAL>:<puerto><ruta>?wsdl`. Si no responde, comprobar red y firewall; no modificar el firewall automáticamente.
6. En `~/.eventosUy/web.properties` del equipo Web indicar esa misma IP y puerto/ruta; reiniciar Tomcat y recorrer `/inicio`. No reconstruir JAR/WAR ni regenerar cliente por cambiar IP.
7. Verificar la petición SOAP en el servidor o con herramientas de red de forma controlada. No habilitar volcados globales de mensajes: otros casos posteriores pueden incluir datos sensibles.

No se declara ejecutado el ensayo con dos computadoras ni una inspección visual real por incluir estas instrucciones.

## Contrato y generación: sin editar fuentes generadas

- Central: `jaxws-maven-plugin:4.0.5` ejecuta `wsgen` desde `EventosPublicador`; salida `servidor-central/target/wsdl/`.
- Web: se versiona una copia de `EventosService.wsdl` y `EventosService_schema1.xsd` en `webapp/src/main/resources/wsdl/`.
- `wsimport` genera `web.soap.generado` durante Maven en `target/generated-sources/wsimport`. Esas clases no se editan ni versionan.
- El WSDL generado por wsgen contiene dirección de marcador `REPLACE_WITH_ACTUAL_URL`. El cliente la sustituye por properties antes de llamar; no obtiene el destino del marcador ni necesita descargar WSDL remoto para compilar.

Tras cambiar el contrato, regenerar Central y actualizar la copia de WSDL/XSD antes de recompilar consumidores:

```powershell
mvn -pl servidor-central -am -DskipTests package
Copy-Item servidor-central/target/wsdl/EventosService.wsdl webapp/src/main/resources/wsdl/EventosService.wsdl
Copy-Item servidor-central/target/wsdl/EventosService_schema1.xsd webapp/src/main/resources/wsdl/EventosService_schema1.xsd
mvn -DskipTests package
```

Revisar el diff del contrato antes de aceptar el cambio. No es necesario actualizarlo por cada despliegue.

## Pruebas de este corte

Las pruebas nuevas levantan un endpoint HTTP real en un puerto local efímero y usan un doble de `ISistema`; **no inicializan JPA ni abren conexiones PostgreSQL**. La integración web usa el cliente generado real, pero request/response del Servlet son dobles: no equivale a una prueba visual ni a un Tomcat desplegado.

Para repetir exclusivamente las nuevas pruebas, desde PowerShell:

```powershell
mvn -pl webapp -am '-Dtest=ConfiguracionCentralTest,EventosPublicadorTest,InicioSoapTest' '-Dsurefire.failIfNoSpecifiedTests=false' test
```

No ejecutar indiscriminadamente `mvn test` sobre el reactor: incluiría pruebas anteriores de persistencia. Su ejecución requiere autorización, aislamiento y la protección de base de pruebas existente. No se desactiva esa protección en este corte.

La elección de Metro aporta JAXB al nodo SOAP. Para evitar implementaciones superpuestas al combinarlo con Hibernate, los POM del Central y Web excluyen el `org.glassfish.jaxb:jaxb-runtime` transitivo de lógica; conservan la implementación JAXB suministrada por Metro. No se alteraron las dependencias internas de `logica` ni las del Swing independiente.

## Pendientes de la siguiente etapa

Migrar resto de casos web, categorías e imágenes; retirar lógica/JPA del WAR; completar configuración global y empaquetado final `web.war`; probar persistencia real y despliegue en dos equipos; verificar visualmente Swing/Tomcat. No se implementaron móvil, seguidores, videos ni otros casos nuevos del Lab 3 en esta fase.

## Evidencia de esta ejecución — 06/11/2026

- JDK Temurin 25.0.5; `mvn clean package` con selección explícita de las pruebas web y SOAP sin persistencia: **BUILD SUCCESS** en los cuatro módulos.
- Central: **6 pruebas**, web: **79 pruebas** (73 existentes y 6 nuevas). Total **85**, ninguna fallida, con error u omitida. No se ejecutó la suite histórica de persistencia ni se midió cobertura nueva de lógica.
- Se comprobó SOAP/JAXB desde el propio `servidor.jar` con un doble de lógica, más descubrimiento del proveedor JPA y lector WEBP, sin abrir una base de datos. Esto no equivale a inicializar JPA contra PostgreSQL ni abrir Swing.
- Se crearon las dos plantillas en `C:\Users\Joaco\.eventosUy`, sin sobrescribir archivos existentes. `central.properties` todavía requiere completar credenciales autorizadas antes de arrancar; no se conectó a PostgreSQL.
- Shade conserva advertencias de recursos repetidos (licencias, manifiestos e índice HK2); ya no hay la superposición de clases JAXB detectada inicialmente. Metro informa que el monitoreo no está disponible; las consultas probadas funcionan sin ese monitoreo.
- No se ejecutó Tomcat ni una comprobación visual en navegador/Swing, y no se probó otro equipo LAN. No se modificaron datos ni esquemas PostgreSQL, ni se realizaron commit o push.
