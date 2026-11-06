# ProyectoWebServices — Referencia técnica para eventos.uy

## 1. Alcance de la revisión

Se estudiaron el README completo, POM raíz y de ambos módulos, arranque y configuración del Central, publicador y lógica de ejemplo, datatypes/enumerados/excepciones, cliente web y servlets, `web.xml`, contrato WSDL/XSD, plantillas `.properties`, scripts y configuraciones `.run`.

Los archivos citados son rutas reales relativas a `ProyectoWebServices/`, referencia local excluida de Git. **Se analizó su código; no se ejecutaron sus scripts, servicios ni compilación.** No se modificó ni copió la demo al proyecto. Esta documentación permite comprender los patrones sin exigir la demo para construir eventos.uy.

## 2. Estructura Maven encontrada

| Archivo | Hallazgo |
| --- | --- |
| `pom.xml` | Agregador `uy.edu.curso:cartelera:1.0-SNAPSHOT`, módulos `central` y `web`; no es el padre declarado por sus POM. |
| `central/pom.xml` | JAR `cartelera-central`, Java release 25, Metro `com.sun.xml.ws:jaxws-rt:4.0.5`, finalName `central`. |
| `web/pom.xml` | WAR `cartelera-web`, release 25, Servlet 6.1 provided y Metro 4.0.5 empaquetado; finalName `cartelera`; sin dependencia del Central. |
| `.mvn/wrapper/maven-wrapper.properties` | Wrapper 3.3.4, distribución Maven 3.9.9. |

El único contrato compartido entre aplicaciones es WSDL/XSD; las clases del cliente se generan. El WAR no necesita importar clases del módulo Central. Tomcat no provee Metro: la dependencia runtime del cliente va en `WEB-INF/lib`.

## 3. Publicación SOAP y fachada

Las clases Java siguientes están bajo `central/src/main/java/uy/edu/curso/cartelera/central/`:

- `Main.main`: crea `Configuracion`, calcula URL y ejecuta `Endpoint.publish(url, new Publicador())`; registra `endpoint.stop()` en un shutdown hook e informa direcciones LAN. No levanta Tomcat ni tiene ventana Swing real.
- `servicio/Publicador`: clase concreta `@WebService`, con `@WebMethod` y parámetros nombrados por `@WebParam`. No declara una SEI separada escrita a mano; el contrato generado sí expone la interfaz al consumidor.
- Operaciones reales: `listarPeliculas()`, `consultarPelicula(String)`, `listarFunciones(String)`, `comprarEntradas(int,int,String)` y `obtenerPoster(String)`.
- Cada operación delega en `Cartelera.getInstance()`. En eventos.uy se deberá usar la Fábrica/controladores existentes, como pide §7.1; no copiar ese singleton de cine como un componente nuevo del dominio.

`logica/Cartelera.java` contiene mapas en memoria y métodos `synchronized`. `logica/Funcion.vender` descuenta lugares disponibles. Esto sirve para ilustrar delegación y errores, **no** garantiza transacciones PostgreSQL ni sustituye JPA/bloqueos del proyecto. La demo no persiste al reiniciar ni implementa la distribución completa con Swing.

## 4. Datatypes, enumerados, listas y fechas

Archivos de referencia bajo el mismo paquete Central:

| Archivo o método | Técnica observada | Aplicación prevista |
| --- | --- | --- |
| `datatypes/DataPelicula.java` | Constructor público vacío y getters/setters; usa `Clasificacion`. | DTO de transporte aptos para JAXB; no entidades JPA. |
| `datatypes/DataFuncion.java` | `fechaHora` es String ISO-8601; constructor convierte `LocalDateTime.toString()`. | Elegir conversión explícita para fechas del contrato, sin serializar directamente `LocalDate` sin adaptación. |
| `datatypes/DataCompra.java` | Contiene otro datatype `DataFuncion`. | Composición de respuestas sin filtrar entidades. |
| `logica/Clasificacion.java` | Enum con TODO_PUBLICO, MAYORES_13, MAYORES_15 y MAYORES_18. | Estados/niveles como enumerados del contrato cuando corresponda. |
| `Publicador.listarPeliculas/listarFunciones` | Devuelven `List<...>` de datatypes. | Listas de datos; los arreglos son una alternativa admitida, no una operación adicional de la demo. |

En `web/src/main/resources/wsdl/cartelera.xsd` las listas tienen `maxOccurs="unbounded"`, el enum restricciones `xs:enumeration` y las imágenes `xs:base64Binary`. Los DTO del consumidor son clases nuevas generadas desde XML, no las mismas clases Java del Central. La tabla del README advierte que una colección como atributo en una clase generada puede ofrecer solo getter con lista mutable: no agregar setters a fuentes generadas para imitar los DTO escritos a mano.

El README compara `java.util.Date` con `XMLGregorianCalendar` y aconseja String ISO para `LocalDate/LocalDateTime`. Se tomará esa técnica como candidata; la representación definitiva de eventos.uy se decidirá con pruebas de ida y vuelta, campos opcionales y fechas, no mediante sustituciones masivas.

## 5. Imágenes

- `Cartelera.obtenerPoster(String)` lee un recurso del JAR y devuelve sus bytes; `Publicador.obtenerPoster` los transmite como `byte[]` (Base64 en XML).
- `web/src/main/java/uy/edu/curso/cartelera/web/VerPoster.java` pide bytes al proxy y escribe respuesta `image/jpeg` con longitud y cuerpo.
- Los pósters no están embebidos en `DataPelicula`; su consulta separada evita enviar todas las imágenes en cada listado.

En eventos.uy las imágenes están en PostgreSQL, con controles de formato/tamaño/privacidad que deben conservarse. No copiar el MIME JPEG fijo ni el almacenamiento en recursos: habrá que transportar bytes y la información de tipo necesaria, manteniendo validación y autorización. No se propone MTOM como requisito: byte[] satisface la letra.

## 6. Excepciones y fallas de comunicación

`logica/NoEncontradoException.java` y `SinLugaresException.java` extienden `Exception`. Al figurar en el `throws` del publicador, producen faults declarados en WSDL. En el consumidor aparecen `NoEncontradoException_Exception` y `SinLugaresException_Exception`.

`web/.../ComprarEntradas.doPost`:

1. Lee/convierte parámetros; errores numéricos producen HTTP 400.
2. Invoca `comprarEntradas` por el proxy.
3. Captura excepción de cupo y vuelve a mostrar la ficha; inexistencia produce 404.
4. También captura un `SOAPFaultException` genérico para el ejemplo de `IllegalArgumentException` no declarada.
5. Con éxito guarda comprobante en sesión y redirige a GET `/compra` (PRG).

`web/src/main/webapp/WEB-INF/web.xml` configura página para `jakarta.xml.ws.WebServiceException` y páginas 400/404. El README distingue falla técnica de error de negocio.

**Reutilizar:** faults declarados, tratamiento centralizado de indisponibilidad y mensajes útiles. **No copiar:** mostrar indiscriminadamente `faultString` de errores inesperados. La lógica de eventos.uy puede conservar sus excepciones locales; la fachada debe traducir errores de negocio conocidos a un contrato remoto explícito, sin filtrar JDBC/Hibernate ni credenciales.

## 7. wsimport y ciclo del contrato

En `web/pom.xml`, `com.sun.xml.ws:jaxws-maven-plugin:4.0.5` ejecuta `wsimport` sobre `src/main/resources/wsdl/cartelera.wsdl`. Genera el paquete `uy.edu.curso.cartelera.web.servicio`, usa `wsdlLocation=/wsdl/cartelera.wsdl` y deja fuentes en `target/generated-sources/wsimport`. Ese paquete no se escribe ni edita manualmente.

`scripts/actualizar-wsdl.ps1` necesita Central encendido: descarga `?wsdl` y `?xsd=1`, cambia la importación a `cartelera.xsd` local y guarda ambos. El esquema local evita que una compilación normal requiera Central encendido. La URL del script tiene un valor por defecto de desarrollo y parámetro `-Url`; no es la configuración final que debe copiarse literalmente al Lab 3.

Secuencia reutilizable: modificar contrato → publicar Central actualizado → actualizar copia WSDL/XSD → generar cliente con Maven → adaptar consumidores → reconstruir y desplegar. Una compilación de contrato ya actualizado debe usar los archivos locales; un cambio de IP no requiere volver a generar cliente.

## 8. Uso del cliente y configuración

En `web/src/main/java/uy/edu/curso/cartelera/web/ClienteCentral.java`:

- Una instancia estática de `PublicadorService` lee el WSDL.
- `publicador()` obtiene un puerto nuevo mediante `getPublicadorPort()`; no comparte un proxy mutable entre hilos de Tomcat.
- Configura `BindingProvider.ENDPOINT_ADDRESS_PROPERTY` con URL externa.
- Configura timeouts Metro: conexión 3000 ms y respuesta 10000 ms. Son valores de la demo, no mínimos exigidos por la letra.
- Lee `~/.cartelera/web.properties`; si no existe lo copia desde recursos del WAR. El usuario relevante es quien ejecuta Tomcat.

`ListarPeliculas.doGet` usa el proxy, agrega lista a request y hace forward a JSP. `ConsultarPelicula.mostrar` reutiliza un puerto para dos llamadas consecutivas (película y funciones); son dos intercambios de red, no una consulta local atómica.

En Central, `Configuracion.java` lee `~/.cartelera/central.properties`, exige claves no vacías y construye la URL. Las dos plantillas definen `central.host`, `central.puerto`, `central.ruta`; la Central además `central.mostrarMensajes`.

La demo habilita impresión de mensajes SOAP por `com.sun.xml.ws.transport.http.HttpAdapter.dump` según configuración. **No activarla por defecto en eventos.uy**: mensajes de autenticación e imágenes pueden contener información sensible. Ese diagnóstico es una herramienta opcional, no un requisito.

Para eventos.uy el directorio debe ser **`~/.eventosUy`**, no `.cartelera`; Central agregará configuración PostgreSQL externa pasada a EMF. Proponer `central.properties` y `web.properties` conserva la separación, pero esos nombres concretos y sus claves deberán documentarse al implementarlos: aún no existen como nueva configuración del Lab 3.

## 9. Empaquetado, scripts y red

`central/pom.xml` usa Shade 3.6.0 en `package`: manifiesto con clase Main, `ServicesResourceTransformer` para unir proveedores SPI y filtros de firmas/module-info; no crea dependency-reduced-pom. Genera `central/target/central.jar`. El transformador de servicios resulta especialmente relevante para Metro/JAXB y deberá comprobarse también para JPA e ImageIO al integrar las dependencias de eventos.uy.

| Archivo real | Función y cautela |
| --- | --- |
| `scripts/setup.ps1` | Detecta/instala JDK 25, configura entorno, opcionalmente IntelliJ y compila con wrapper y `-DskipTests`. Ejecutarlo puede modificar la computadora: no se ejecutó ni se propone copiarlo sin revisar. |
| `scripts/central-up.ps1` | Compila `central` con wrapper y `-DskipTests`, luego `java -jar`; opción `-SinCompilar`. No reemplaza una suite de verificación. |
| `scripts/web-up.ps1` | Descarga Tomcat 11.0.26 en tools, construye WAR, **lo copia como `ROOT.war`** a su Tomcat, configura entorno, inicia/detiene y abre navegador. |
| `scripts/actualizar-wsdl.ps1` | Actualiza contrato local, no es un paso que deba repetirse para cada cambio de IP. |
| `scripts/descargar-posters.ps1` | Descarga recursos del dominio cine; no tiene utilidad funcional para eventos.uy y no se reutilizará. |
| `.run/*.run.xml` | Facilita arranque/actualización desde IntelliJ; no se requiere para ejecutar los artefactos. |

Los scripts requieren PowerShell 7+, pero la letra no obliga a ese shell. Atención al contexto: aunque el artefacto se llama `cartelera.war`, el script lo despliega como ROOT y `/cartelera` es el mapping de `ListarPeliculas`. No deducir el context path solamente del nombre del WAR ni copiar URLs de la demo a eventos.uy.

El README explica cambiar `central.host` a IP LAN, configurar esa misma IP en el Web, reiniciar y comprobar WSDL desde otro equipo; también documenta firewall, loopback y puertos ocupados. Un bind `0.0.0.0` no es una dirección de destino para clientes: en eventos.uy se deberá verificar que URL publicada/WSDL y configuración del Web sean accesibles desde la LAN.

## 10. Qué reutilizar y qué cambiar

| Patrón de la demo | Decisión propuesta para eventos.uy |
| --- | --- |
| Fachada SOAP delgada y `Endpoint.publish` | Reutilizar técnica; delegar a Fábrica/ISistema, publicar antes de ventana Swing. |
| DTO JAXB y faults declarados | Adaptar contrato propio, fechas y permisos; no copiar clases del cine. |
| Cliente generado, WSDL/XSD local y endpoint sustituible | Adoptar patrón para desacoplar web del JAR de lógica y permitir build reproducible. |
| Proxy por pedido y timeouts | Adoptar diseño; definir valores y manejo de indisponibilidad propios. |
| byte[] y consulta separada de imágenes | Adaptar a tipos y privacidad existentes; conservar bytes originales y límites. |
| Properties externas y plantillas | Usar `~/.eventosUy`; sumar conexión JPA; nunca publicar secretos reales. |
| JAR autocontenido y WAR independiente | Producir **`servidor.jar` y `web.war`**, no nombres de la demo; integrar Swing en el JAR. |
| Datos en memoria / synchronized global | No trasladar: mantener JPA/PostgreSQL y garantías transaccionales del proyecto. |
| Sitio de cine, pósters y scripts de instalación | No incorporar al producto; móvil responsive y casos nuevos se desarrollarán según la letra. |

**Resultado:** ANALIZADO y DOCUMENTADO. Compatibilidad real de la integración Metro/Java 25/paquetes, nuevo contrato y despliegue LAN: PENDIENTES de implementación y pruebas autorizadas. No se declara que la demo haya sido ejecutada ni que SOAP ya exista en eventos.uy.
