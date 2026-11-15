# Estado actual del Laboratorio 3 — Eventos UY

Actualizado el **15/11/2026**, rama `lab3-desarrollo`.

La etapa 2 está corregida y confirmada. Los aportes compatibles de usuarios e
instituciones de Bentancor están integrados selectivamente. Los 21 Servlet,
el filtro y el listener usan clientes remotos; Tomcat no inicia JPA ni incluye
lógica o PostgreSQL en el WAR. Central publica eventos, Portal y P03 sobre HTTPS.
Swing continúa usando ISistema dentro del proceso Central.

Validación: **436 JUnit + 1 integración Tomcat/HTTPS**, 61 JavaScript y
5 comprobaciones Swing, sin fallos. La IT verificó 49 solicitudes HTTP y el
comportamiento sin Central. Antes de las pruebas persistentes se confirmó
`eventosuy_test` por JDBC. No se hizo push ni integración destructiva.

Ver [el informe de integración](INTEGRACION-2026-11-15.md) para funcionalidades,
seguridad, commits, límites, evidencia y comandos de ejecución. Los dominios
ajenos a esta integración mantienen el alcance del roadmap del equipo.

El cierre anterior se conserva abajo como historia; sus pendientes Web/P03 no
describen la versión actual.

---

# Cierre histórico del 11/11/2026

Cierre del **11/11/2026**, rama `lab3-desarrollo`, HEAD `a1a15e7`. Este documento describe el árbol de trabajo **sin confirmar**, no una entrega publicada. Los cambios de sesiones, HTTPS y refactorizaciones aprobadas todavía están modificados o sin seguimiento en Git. No se hicieron commits, push ni merges durante este cierre.

Fuentes de referencia: [requisitos](REQUISITOS.md), [contratos del equipo](CONTRATOS-EQUIPO.md), [auditoría actualizada](AUDITORIA-SERVLETS-CAPAS.md), [arranque HTTPS](ARRANQUE-CENTRAL-HTTPS.md) y [sesiones SOAP](P03-SESIONES-SOAP.md). La implementación actual prevalece ante estados antiguos de esos documentos; este cierre no modifica los contratos acordados ni implementa nuevos casos.

## 1. Arquitectura implementada

| Módulo | Estado real |
|---|---|
| `logica` | `ISistema` define casos de uso con DTO; `Sistema` aplica validaciones y las políticas ya centralizadas. Manejadores gestionan acceso y transacciones JPA/Hibernate/PostgreSQL. Persisten reglas pendientes fuera de Sistema en los casos enumerados abajo |
| `estacion-trabajo` | Swing accede directamente a `ISistema` dentro del proceso del Central, como exige la letra. No debe migrarse a SOAP. Los paneles protegidos corregidos reciben el actor de `principal` |
| `servidor-central` | `Main` carga configuración externa, prepara TLS antes de JPA, obtiene `ISistema` por la Fábrica, publica ambos servicios y abre Swing. Un `HttpsServer`, rutas independientes y un registro `SesionesRemotas` por proceso; cierre de endpoints/listener/ejecutor y JPA |
| `webapp` | Tomcat todavía ejecuta lógica local: 17 Servlet llaman `ContextoListener.sistema`. El listener inicia JPA y el filtro consulta categorías localmente. Solo `InicioServlet` consume SOAP, con incompatibilidad HTTP/HTTPS pendiente |

En el Central, `ServidorHttpsCentral` comparte el controlador recibido entre eventos y el registro de sesiones. No significa que exista un único `Sistema` global: `Fabrica` es singleton, pero `getISistema()` construye un controlador; los paneles Swing mantienen ese patrón local existente. La identidad se pasa como argumento y no se guarda en un actor global mutable de Sistema.

La arquitectura objetivo Web → SOAP HTTPS → Central → Sistema → manejadores → PostgreSQL **todavía no se cumple de extremo a extremo**. Los tres casos refactorizados siguen siendo llamadas locales desde Web. El WAR aún depende del módulo de implementación `logica` y sus dependencias de persistencia.

## 2. Componentes funcionales y evidencia de pruebas

| Componente | Evidencia | Límite de lo comprobado |
|---|---|---|
| Alta de tipo y alta de patrocinio | Regresiones de Sistema y persistencia real repetidas al cierre, incluyendo denegaciones y unicidad concurrente | No hay operación SOAP protegida publicada para ellos |
| Consulta de patrocinios | Matriz real de actores/estados, consultas por código y clave compuesta, listados y DTO sin códigos para terceros | No implica que toda consulta de edición o perfil ya esté autorizada en Sistema |
| Adaptación HTTP y CSRF | Pruebas de Servlet/filtro con dobles; 29 comprobaciones HTTP/JSP/PostgreSQL reales del bloque previo | El verificador HTTP no se repitió al cierre; la prueba no equivale a la defensa LAN |
| Propagación de actor en Swing | Compilación del reactor y verificador headless repetido: administrador, organizador, asistente, actor obligatorio y campo final | El verificador usa doble de ISistema y captura el guardado antes del diálogo; no reemplaza las pruebas de autorización real ni una comprobación visual |
| HTTPS y sesiones SOAP | Pruebas previas: 38 JUnit seleccionadas aprobadas, incluyendo 4 del componente productivo de arranque; proxy generado, confianza/SAN, login/logout y concurrencia | No se repitieron al cierre. Usan doble de ISistema; no se probó Main con persistencia/Swing reales ni dos computadoras |

**Ejecución repetida en este cierre:** 119 pruebas JUnit, 0 fallos, 0 errores, 0 omitidas; `BUILD SUCCESS`. Distribución:

| Suite | Cantidad |
|---|---:|
| `ConsultaTipoRegistroTest` | 6 |
| `PatrocinioTest` | 13 |
| `ConsultaPatrociniosVisibilidadTest` | 8 |
| `UnicidadConcurrenciaTest` | 21 |
| `manejadores.ConflictosPersistenciaTest` | 3 |
| `AltasPrgTest` | 9 |
| `ConsultasEdicionTest` | 17 |
| `PatrociniosAutorizacionTest` | 15 |
| `PatrociniosFlujoTest` | 16 |
| `CsrfTest` | 11 |

Las primeras cuatro suites usan Sistema/persistencia real y la barrera de base segura; la quinta es unitaria. Las cinco web usan dobles de interfaces HTTP/lógica. Las 5 comprobaciones Swing se ejecutaron aparte y no se suman como JUnit. Los avisos de Hibernate y las violaciones de unicidad inducidas en concurrencia no hicieron fallar las pruebas. No se ejecutó la suite completa ni se midió cobertura general.

Antes de iniciar persistencia se ejecutó por JDBC `SELECT current_database()` y se confirmó exactamente **`eventosuy_test`**. Las suites verificaron también la base mediante `PruebaConBaseSegura`. No se usó la base productiva.

## 3. Refactorizaciones terminadas en lógica, pendientes de transporte

### Alta de tipo de registro

- Única firma `altaTipoRegistro(DtTipoRegistro, String nombreEdicion, Actor)`; no existe la anterior sin actor.
- Sistema valida rol, propiedad, datos, costo finito/no negativo y cupo positivo. Administrador local exceptúa propiedad, no invariantes.
- `ManejadorEventos` conserva `PESSIMISTIC_WRITE`, unicidad, persistencia y rollback. El callback interno `Consumer<String>` definido en Sistema comprueba propiedad antes de consultar duplicados.
- Web obtiene actor de sesión y conserva parseo/mensajes específicos por costo/cupo vacíos. Swing, cargador y pruebas usan la firma nueva.

### Alta de patrocinio

- Única escritura `altaPatrocinio(DtPatrocinio, String nombreEvento, Actor)`; relación evento-edición opcional cuando el consumidor no aporta evento.
- Sistema contiene propiedad, referencias, importes, duplicados y límite del 20 %. El formulario usa `obtenerOpcionesAltaPatrocinio` con actor y DTO de opciones, sin reglas de propiedad en web.
- `ManejadorPatrocinios` resuelve entidades gestionadas y bloquea edición/tipo; ejecuta el callback de Sistema dentro de la transacción, antes de persistir. Denegación hace rollback y conserva `AccesoDenegadoException`.
- `AltaPatrocinioPanel` recibe el actor, lo conserva privado/final y transmite la misma instancia. No crea administradores. La creación administrativa del Swing permanece en `principal.main`.

### Consulta y visibilidad de patrocinios

- Firmas con actor en todas las variantes: `consultarPatrocinio(codigo, actor)`, `consultarPatrocinio(edicion, institucion, actor)` y `obtenerPatrociniosEdicion(edicion, actor)`. No quedan las variantes sin actor.
- `consultarPatrocinios(evento, edicion, institucion, actor)` compone la selección autorizada mediante `DtConsultaPatrocinios`; el evento real de la edición prevalece sobre el parámetro de URL.
- `obtenerInstitucionesPatrocinadoras(edicion, actor)` devuelve solo nombres, incluso si el actor podría conocer códigos. Es lo único cambiado en el recorrido de patrocinadoras de `ConsultaEdicionServlet`.
- `AccesoPatrocinios` solo lee la sesión. Web ya no recibe DTO completos para redactarlos después. `null` representa explícitamente un visitante anónimo, no un administrador.
- Swing conserva acceso administrativo local mediante el actor de `principal`; los paneles de consulta no fabrican uno nuevo.

| Actor de consulta | Ediciones visibles | Código |
|---|---|---|
| Visitante, asistente u organizador ajeno | Aceptadas | Ausente del DTO |
| Organizador propietario | Sus ediciones en los tres estados existentes | Disponible |
| Administrador local | Todas | Disponible |

Los demás campos de patrocinio siguen siendo públicos en ediciones aceptadas según el comportamiento previo. Recurso oculto/inexistente no devuelve DTO; las colecciones quedan vacías y la consulta de página responde 404 cuando corresponde. El selector lista aceptadas y conserva solo la privada seleccionada y autorizada, no todas las privadas del propietario.

## 4. Servlet todavía pendientes

Inventario actual: **19 Servlet; 12 con reglas funcionales pendientes; 17 con llamadas locales propias; 1 consumidor SOAP; 0 recorridos completos desacoplados de JPA local**. Las categorías se superponen.

| Grupo | Servlet | Pendiente |
|---|---|---|
| Cortes centralizados | `AltaTipoRegistroServlet`, `AltaPatrocinioServlet`, `ConsultaPatrocinioServlet` | SOAP HTTPS, identidad verificada en Central, DTO/errores de transporte y pruebas remotas. Alta de tipo aún necesita acordar consulta propia segura para el selector remoto |
| Reglas de edición/evento/imagen | `AltaEdicionServlet`, `AltaEventoServlet`, `ConsultaEdicionServlet`, `ConsultaTipoRegistroServlet`, `EventoServlet`, `ImagenServlet` | Propiedad/visibilidad/estados, composición dirigida y, en altas, atomicidad de imagen. En ConsultaEdicion solo está cerrado el resumen de patrocinadoras |
| Usuarios/instituciones | `AltaInstitucionServlet`, `AltaUsuarioServlet`, `ModificarUsuarioServlet`, `UsuarioServlet` | Validaciones repetidas o divergentes, identidad de modificaciones, proyección pública/propia y atomicidad de imágenes |
| Registros | `RegistroEdicionServlet`, `ConsultaRegistroServlet` | Actor, permisos de lectura, invariantes y estado consistente con la escritura |
| Infraestructura/presentación sin regla funcional propia detectada | `InicioServlet`, `LoginServlet`, `LogoutServlet`, `UsuariosServlet` | TLS/categorías, servicios de sesiones, revocación y listado SOAP; no requieren copiar negocio en fachadas |

Los 12 con reglas pendientes son los tres grupos intermedios de la tabla. `FiltroAcceso`, `ContextoListener` y `AccesoEdiciones` no son Servlet adicionales: son dependencias transversales pendientes. No se encontraron nuevos accesos directos a EM/JDBC en Servlet; el acceso JPA local del listener sigue abierto.

## 5. Operaciones SOAP existentes y faltantes

**Existen tres operaciones en dos servicios:**

| Servicio | Operación | Consumidor actual |
|---|---|---|
| `EventosService` | `listarEventos()` | `InicioServlet` mediante cliente generado; adaptación HTTP/HTTPS normal pendiente |
| `SesionesService` | `iniciarSesion(identificador, contrasenia)` | Pruebas reales HTTPS; login web aún no integrado |
| `SesionesService` | `cerrarSesion()` | Pruebas reales HTTPS; logout web aún no integrado |

No están publicadas alta de tipo, alta de patrocinio ni consultas de patrocinios. Tampoco están migradas categorías, usuarios/perfiles, instituciones, detalle/alta de evento/edición, consultas de tipos, inscripciones/registros ni imágenes. Los DTO locales nuevos son records/DTO de lógica: **no asumir que pueden exponerse directamente como contratos JAXB**; falta su adaptación de transporte y errores controlados.

Las funciones nuevas del Lab 3 (visitas/ranking, seguimiento, asistencia/archivo, finalización, búsqueda/disponibilidad y móvil) siguen requiriendo sus cortes y verificación contra REQUISITOS. No se acreditan como implementadas por existir documentación o consumidores anteriores. Este cierre no es una auditoría funcional completa de cada requisito nuevo.

## 6. Autenticación, autorización y revisión final

- Login web actual: `LoginServlet` delega a `Sistema.autenticar`, renueva sesión HTTP y guarda su resultado; todavía no obtiene una sesión remota. Logout invalida HTTP sin llamar a Central.
- Central: `SesionesRemotas` autentica mediante ISistema, emite referencias de 32 bytes aleatorios con `SecureRandom`/Base64 URL sin relleno, conserva identidad/vencimiento fijo en memoria y permite sesiones independientes y revocación. Sesión desconocida, malformada, vencida o revocada no permite obtener actor.
- `SesionesPublicador` entrega la referencia solo en `X-EventosUy-Session`; el logout lee un único encabezado Bearer válido y es idempotente. La contraseña de entrada viaja en la solicitud SOAP bajo HTTPS; la referencia de sesión no se coloca en DTO de negocio, URL ni logs.
- En los casos corregidos, Web construye actor desde el usuario de sesión, no desde nickname/rol enviados como parámetros. El actor administrativo solo pertenece a entradas locales confiables. `Actor.usuarioAutenticado` no autentica un DTO por sí mismo.
- El futuro publicador protegido deberá resolver actor con `SesionesRemotas.requerirActor` y delegar en Sistema. No deserializar Actor/rol administrativo desde el cliente. Ese recorrido protegido todavía no está publicado.
- Revisión de fuentes y compilación: no se encontraron firmas productivas antiguas de los tres cortes, reglas duplicadas de patrocinio en web, administradores creados dentro de los paneles corregidos ni nuevas llamadas JPA web. `ConsultaEdicionServlet` mantiene otras reglas pendientes y no se declara cerrado por completo.
- `addPatrocinio` tiene una sola invocación productiva desde Sistema. Los manejadores públicos/callbacks y las fábricas locales de Actor no son una frontera frente a código Java arbitrario dentro del proceso; no se encontró una ruta productiva HTTP/SOAP que omita la autorización.

## 7. Riesgos abiertos

| Prioridad | Riesgo comprobado o condición pendiente |
|---|---|
| P0 | Consultas de registros/perfiles y otras operaciones lógicas sin actor dependen de controles web. Publicarlas sin corregir permitiría omitir la política; no es una vulnerabilidad demostrada de un endpoint protegido actual |
| P0 | Altas de usuario, institución, evento y edición, y modificación de perfil, confirman datos e imagen en transacciones separadas. Puede persistir la primera parte si falla la segunda |
| P0 | La comprobación de estado al registrar ocurre en Sistema antes de la transacción del manejador; revisar consistencia ante cambio concurrente de estado |
| P0 | Cliente normal de portada fabrica HTTP, mientras Central solo publica HTTPS. No integrar operaciones protegidas hasta configurar confianza/TLS e identidad Central |
| P1 | Tomcat conserva JPA, Fábrica, dependencia de lógica y categorías locales; no cumple separación física de persistencia. La inicialización local aún tiene defaults de conexión que no deben confundirse con configuración segura del Central |
| P1 | Visibilidad de edición/tipo/imagen y campos de perfiles aún depende de helpers/Servlet. Falta la misma política para todos los clientes |
| P1 | URL institucional y validaciones de formulario tienen divergencias entre clientes; acordar política antes de trasladarlas |
| P1 | Empaquetado, arranque completo con base real autorizada y defensa entre computadoras no se han demostrado en conjunto. El procedimiento LAN está preparado, no desplegado |

No se encontró una nueva fuga de códigos de patrocinio en las consultas corregidas. La integridad y autorización de los otros dominios siguen abiertas; aprobar patrocinios no las corrige indirectamente.

## 8. Próximos bloques recomendados

1. **Consulta de registros**: centralizar registros propios y consulta del organizador propietario, con actor y DTO acotados. Es el próximo caso recomendado por confidencialidad de datos personales y porque permite repetir el patrón aprendido sin una reescritura. Coordinar las consultas relacionadas de edición/perfil antes de cambiar firmas compartidas.
2. **Modificación de perfil con imagen**: primer corte atómico de escritura, incluyendo perfil, contraseña opcional e imagen en una unidad. Después aplicar el criterio a las cuatro altas con imagen. No basta la validación previa ni agregar una anotación transaccional sin infraestructura.
3. **Visibilidad restante**: edición/tipo/imagen/perfil; retirar reglas de `AccesoEdiciones` gradualmente sin afectar recorridos ya aprobados.
4. **Frontera remota**: corregir HTTPS del cliente público e integrar login/logout con el servicio de sesiones existente. Es requisito previo a publicar casos protegidos; no requiere hacer Swing remoto.
5. **SOAP de los casos corregidos y catálogos**, seguido de las demás operaciones corregidas. Contratos, JAXB, errores y consumidores deben acordarse antes de generar WSDL.
6. **Retirar persistencia del WAR** y completar integración/empaquetado y defensa LAN; no borrar la dependencia local mientras queden consumidores.

La atomicidad sigue siendo bloqueante para las escrituras con imagen. El orden anterior no autoriza implementar nada durante el cierre y no reemplaza la distribución vigente del equipo.

## 9. Comandos para compilar, ejecutar y verificar

PowerShell desde la raíz, con Java 25 y Maven configurados. La compilación sola no debe ejecutar pruebas persistentes:

```powershell
mvn -pl webapp -am -DskipTests package
```

El Central se ejecuta con `java -jar servidor-central/target/servidor.jar`, **solo después** de completar `~/.eventosUy/central.properties` con TLS externo y una base autorizada cuyo esquema ya exista. Se conserva validación de esquema en ese arranque. Para certificados, SAN, truststore, firewall y LAN seguir [ARRANQUE-CENTRAL-HTTPS](ARRANQUE-CENTRAL-HTTPS.md). No se ejecutó Main productivo en el cierre.

Para preparar y repetir las regresiones de cierre con barrera previa:

```powershell
mvn -pl logica -am -DskipTests test-compile
if ($LASTEXITCODE -ne 0) { throw 'No se compiló la barrera de pruebas.' }
$baseAnterior = $env:EVENTOS_DB_NAME
try {
    $env:EVENTOS_DB_NAME = 'eventosuy_test'
    $pg = Join-Path $env:USERPROFILE '.m2/repository/org/postgresql/postgresql/42.7.7/postgresql-42.7.7.jar'
    & "$env:JAVA_HOME/bin/java.exe" -cp "logica/target/classes;logica/target/test-classes;$pg" soporte.ProteccionBasePruebas
    if ($LASTEXITCODE -ne 0) { throw 'No se confirmó eventosuy_test por JDBC. No ejecutar pruebas persistentes.' }
    mvn -pl webapp -am '-Dtest=ConsultaTipoRegistroTest,PatrocinioTest,ConsultaPatrociniosVisibilidadTest,UnicidadConcurrenciaTest,ConflictosPersistenciaTest,AltasPrgTest,ConsultasEdicionTest,PatrociniosAutorizacionTest,PatrociniosFlujoTest,CsrfTest' '-Dsurefire.failIfNoSpecifiedTests=false' test
    if ($LASTEXITCODE -ne 0) { throw 'Fallaron las regresiones.' }
} finally {
    $env:EVENTOS_DB_NAME = $baseAnterior
}
```

`JAVA_HOME` debe señalar el JDK 25; en el cierre se usó Adoptium 25.0.5. Maven se ejecutó con `-o` porque las dependencias ya estaban en caché. Omitir `-o` permite resolverlas en un entorno nuevo. No introducir contraseñas en comandos compartidos ni copiar la configuración real al repositorio.

Verificador Swing sin PostgreSQL, después de compilar sus tests:

```powershell
mvn -pl estacion-trabajo -am -DskipTests test-compile
java '-Djava.awt.headless=true' -cp 'estacion-trabajo/target/test-classes;estacion-trabajo/target/classes;logica/target/classes' swing.AltaPatrocinioPanelActorTest
```

Es un ejecutable JDK, no una suite JUnit que Surefire ejecute automáticamente. Para las pruebas aisladas de sesiones/HTTPS sin base, el comando específico está en [P03-SESIONES-SOAP](P03-SESIONES-SOAP.md) y [ARRANQUE-CENTRAL-HTTPS](ARRANQUE-CENTRAL-HTTPS.md). No usar `mvn test` indiscriminadamente contra la configuración de desarrollo.

## 10. Limitaciones conocidas y estado de Git

- **HTTP/HTTPS:** `webapp/src/main/java/web/soap/ClienteEventosCentral.java:50` construye `new URI("http", ...)`; `ServidorHttpsCentral` no ofrece HTTP. Las pruebas del proxy generado sobre HTTPS no prueban que el cliente normal de portada esté integrado. No resolverlo degradando TLS.
- Registro de sesiones en memoria: reiniciar Central invalida referencias; no hay persistencia de sesiones ni coordinación entre varios procesos Central. No se introduce otro mecanismo de autenticación.
- Los DTO nuevos de composición no acreditan compatibilidad JAXB ni contratos SOAP completos. Las entidades siguen dentro de lógica/persistencia; el transporte protegido está pendiente.
- La selección de regresiones no demuestra todo el Lab 3, toda la interfaz Swing ni un despliegue real entre computadoras. Quedan comprobaciones visuales y el ensayo de defensa.
- El diff contra HEAD incluye muchos cambios de bloques anteriores. La revisión de cierre no permite afirmar que todo ese árbol esté preparado para un único commit: no se hizo staging ni se seleccionaron archivos para confirmar.
- En esta actividad solo se actualizó `AUDITORIA-SERVLETS-CAPAS.md` y se creó este documento. Los fuentes, pruebas, POM y contratos conservan su contenido previo al cierre. Los artefactos de compilación y reportes quedan bajo `target/`, fuera de los archivos que deben versionarse.

Estado final: `lab3-desarrollo`, índice vacío, cambios previos preservados; sin commit, push ni merge. El siguiente paso requiere revisar y aprobar el diseño del caso de consulta de registros, no implementarlo automáticamente.
