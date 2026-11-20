# Auditoria del Laboratorio 3 y preparacion de dev

Revision iniciada el 15/11/2026 y cerrada el 16/11/2026. Fuente normativa: `Laboratorio 3.pdf`, 14 paginas leidas completas, contrastadas con `REQUISITOS.md`. Los planes no se consideran evidencia de implementacion.

## Dictamen

El Laboratorio 3 NO esta completo. La arquitectura distribuida base y los casos anteriores pasan las regresiones, pero faltan casos expresamente exigidos por la letra. No recomiendo publicar como entrega final. No se ejecuto la fase de integracion condicionada a una auditoria satisfactoria: no hay rama temporal preparada ni merge realizado.

## Matriz de cumplimiento

Cada fila indica implementacion y prueba encontrada o la ausencia de ambas. Completo se refiere al requisito de esa fila, no al laboratorio entero. Las pruebas automatizadas locales no sustituyen la defensa en cuatro equipos.

parcial: 18, completo: 18, no verificable: 3, faltante: 17. Total: 56.

| Requisito / caso | Modulo responsable | Implementacion, prueba y observaciones | Estado | Fuente |
| --- | --- | --- | --- | --- |
| A01: Separar Servidor Central y Servidor Web; exclusivamente el Central mantiene lógica y persistencia. | Central/Web/Swing | Central y Tomcat separados y WAR sin JPA probados localmente; falta segundo equipo. | parcial | §3, pp. 1–2; §8.3, p. 13 |
| A02: Swing usa llamadas directas a la lógica y comparte proceso con el Central. | Central/Web/Swing | ISistema directo, 15 comprobaciones headless; ventana real pendiente. | completo | §3, pp. 1–2; §7.9, p. 12 |
| A03: Defensa en cuatro dispositivos de la misma red: Central/Swing/PostgreSQL, Web/Tomcat, navegador de escritorio y celular. | Central/Web/Swing | No se dispuso de cuatro dispositivos fisicos. | no verificable | §3, p. 2; §7.9, p. 12; §8, p. 13 |
| A04: SOAP mediante Jakarta XML Web Services 4, Eclipse Metro, dependencia `com.sun.xml.ws:jaxws-rt`. | Central/Web/Swing | Metro 4.0.5, Jakarta XML WS 4, Java 25; Portal HTTPS y Tomcat real. | completo | §7.1, pp. 6–7; §7.11, p. 13 |
| A05: Publicar mediante `Endpoint.publish`, al iniciar el Central antes de mostrar la ventana principal; Central sin Tomcat. | Central/Web/Swing | Main publica Endpoint antes de abrir Swing; CentralJarArranqueIT. | completo | §7.1, p. 6 |
| A06: Publicadores delgados: obtener controladores por la Fábrica y delegar; no implementar negocio ni acceder a JPA en ellos. | Central/Web/Swing | Publicadores delegan en Sistema; Actor se deriva de P03, no del formulario. | completo | §7.1, p. 6 |
| A07: Transferir tipos simples, enumerados, datatypes, listas/arreglos y `byte[]`; no objetos del dominio. | Central/Web/Swing | Modulo contratos sin entidades; pruebas SOAP y regeneracion wsimport. | completo | §7.1, pp. 6–7 |
| A08: Datatypes aptos para JAXB: constructor público vacío y getters/setters públicos por atributo. | Central/Web/Swing | Beans JAXB y conversion de fechas; pruebas de contratos y SOAP. | completo | §7.1, p. 6 |
| A09: Excepciones de negocio declaradas, convertidas en SOAP Fault, con mensajes comprensibles en web. | Central/Web/Swing | Faults y errores sanitizados; pruebas Portal HTTPS. | completo | §7.1, p. 6 |
| A10: Cliente generado por `wsimport` usando `jaxws-maven-plugin`; no editar clases generadas. | Central/Web/Swing | Reactor aislado regenera fuentes desde WSDL/XSD versionados. | completo | §7.1, p. 7; §7.11, p. 13 |
| A11: Imágenes por SOAP como `byte[]`; la web no accede a su almacenamiento. | Central/Web/Swing | Imagen byte[] por Portal; PortalPersistenciaHttpsTest. | completo | §7.1, p. 7 |
| A12: Si el Central no responde, página de error propia sin trazas. | Central/Web/Swing | TomcatPortalHttpsIT verifica respuesta 503 con Central apagado. | completo | §7.1, p. 7 |
| A13: Configuración externa en `~/.eventosUy`, sin direcciones, puertos ni credenciales incrustados en código. | Central/Web/Swing | Se retiraron credenciales JDBC por defecto; ConfiguracionBaseTest y JAR real. | completo | §7.2, p. 7 |
| A14: Central configura host/puerto/ruta SOAP y URL/usuario/contraseña PostgreSQL; valores de conexión se pasan al crear EMF. | Central/Web/Swing | XML sin URL/usuario/password; Main pasa configuracion a EMF; JAR sin variables DB. | completo | §7.2, p. 7 |
| A15: Web configura host/puerto/ruta Central y reemplaza la dirección del WSDL. | Central/Web/Swing | Endpoint y confianza externos; pruebas configuracion y Tomcat HTTPS. | completo | §7.2, p. 7 |
| A16: Directorio externo solo para configuración; recursos y bibliotecas dentro de JAR/WAR. | Central/Web/Swing | JAR ejecutado desde directorio temporal sin fuentes; WAR real en Tomcat. | completo | §7.2, p. 7 |
| A17: Publicación con IP LAN del Central y acceso al sitio con IP del Web. | Central/Web/Swing | HTTPS local probado; falta LAN y certificados para direcciones reales. | no verificable | §7.2, p. 7 |
| A18: Java SE 25, IntelliJ para desarrollo, Maven, Tomcat 11, PostgreSQL mediante JPA solo en Central y Git. | Central/Web/Swing | Java 25, Maven, Metro 4.0.5 y Tomcat 11 comprobados; IntelliJ visual pendiente. | parcial | §7.11, p. 13 |
| F01: Persistir todos los nuevos datos: relaciones de seguimiento, video, asistencia, finalización, archivo y visitas. | Sistema/JPA/Web | No existen seguimiento, video, asistencia, finalizacion, archivo ni visitas persistentes. | faltante | §4, pp. 2–3 |
| F02: Conservar actores y casos anteriores no modificados, ahora con web consumiendo SOAP. Fechas nuevas por defecto: día actual, salvo indicación distinta. | Sistema/JPA/Web | Casos anteriores conservados; 464 JUnit, 2 IT, 62 JavaScript y 15 comprobaciones Swing. | completo | §5, p. 3; §6, p. 3 |
| F03: Consultas Swing incluyen eventos finalizados y ediciones archivadas, indicando su estado. | Sistema/JPA/Web | Estados finalizado/archivado no modelados; sin prueba. | faltante | §6.1, p. 3 |
| F04: Swing: cinco eventos más visitados, orden descendente, posición, nombre y cantidad de visitas. | Sistema/JPA/Web | Top cinco y contador persistente ausentes; sin prueba. | faltante | §6.1, p. 3; §7.7, p. 10 |
| F05: Alta de edición: solo eventos no finalizados; video YouTube opcional, además de datos anteriores; nombre único, organizador autenticado y estado inicial Ingresada. | Sistema/JPA/Web | Alta atomica con imagen y propietario; falta video y rechazo por finalizacion. DAO rollback y Swing. | parcial | §6.2, p. 4 |
| F06: Consulta de edición: video en página si existe; conservar tipos, patrocinios y registro propio; dueño ve registros con indicador de asistencia. | Sistema/JPA/Web | Detalle base con tipos/patrocinios/registro; falta video e indicador de asistencia. | parcial | §6.2, p. 4 |
| F07: Asistentes y organizadores pueden seguir y dejar de seguir desde listado/perfil; no a sí mismos ni duplicar relación. | Sistema/JPA/Web | Seguimiento no implementado; sin pruebas del caso. | faltante | §6.2, p. 4 |
| F08: Organizador finaliza evento no finalizado; se oculta de listas/búsquedas y no admite nuevas ediciones. | Sistema/JPA/Web | Finalizacion no implementada; sin pruebas del caso. | faltante | §6.2, p. 4 |
| F09: Organizador archiva una edición propia Aceptada cuyo fin es anterior al día actual; registrar fecha de archivo. | Sistema/JPA/Web | Archivo no implementado; sin pruebas del caso. | faltante | §4, p. 3; §6.2, pp. 4–5 |
| F10: Archivadas no aparecen en listas/búsquedas generales; se consultan en perfil de su organizador; registros se conservan y asistente los ve en su perfil. | Sistema/JPA/Web | Visibilidad de archivadas no implementada; sin pruebas del caso. | faltante | §6.2, p. 5 |
| F11: Asistente descarga constancia PDF desde su registro solo si confirmó asistencia. | Sistema/JPA/Web | Asistencia y constancia PDF ausentes; sin pruebas del caso. | faltante | §6.2, p. 5 |
| F12: Perfil consultado muestra seguidores/seguidos y acciones de seguir/dejar de seguir cuando correspondan. | Sistema/JPA/Web | Perfil existente sin seguidores/seguidos; pruebas de perfiles previos solamente. | parcial | §6.2, p. 5 |
| F13: Alta de usuario informa disponibilidad de nickname y correo mientras se escribe. | Sistema/JPA/Web | Disponibilidad mediante Portal HTTPS; pruebas Emmanuel adaptadas y JavaScript. | completo | §6.2, p. 5; §7.6, pp. 9–10 |
| F14: Móvil: inicio por nickname/correo y contraseña; solo asistentes; rechazar organizadores y credenciales inválidas. | Sistema/JPA/Web | P03/login existente; falta entrada movil que admita solo asistentes. | parcial | §5, p. 3; §6.3, p. 5 |
| F15: Móvil: cierre de sesión vuelve al inicio de sesión. | Sistema/JPA/Web | Logout existente probado en Tomcat; falta recorrido movil requerido. | parcial | §6.3, p. 5 |
| F16: Móvil: evento → ediciones Aceptadas → detalle completo con organizador, tipos, patrocinios, imagen/video y registro propio si existe. | Sistema/JPA/Web | Consulta escritorio existente; falta sitio movil y video. | parcial | §6.3, pp. 5–6 |
| F17: Móvil: consultar registros propios y detalle de tipo, fecha, costo, código usado y asistencia. | Sistema/JPA/Web | Registro propio existente y autorizado; falta asistencia y sitio movil. | parcial | §6.3, p. 6 |
| F18: Móvil: registrar asistencia a edición en que está registrado; persistir confirmación; si ya estaba confirmada, informarlo y no volver a ofrecerla. | Sistema/JPA/Web | Confirmacion de asistencia no implementada; sin pruebas del caso. | faltante | §6.3, p. 6 |
| W01: Móvil dentro del mismo `web.war`, ruta propia y diseño para pantallas pequeñas, reutilizando servlets y acceso SOAP. | Web/Central | No existe sitio movil con ruta propia ni ensayo fisico. | faltante | §7.3, pp. 7–8 |
| W02: Pantalla inicial de login; navbar con Consulta Edición, Consulta Registro, Registrar Asistencia y Logout; hamburguesa en ancho reducido. | Web/Central | Menu movil y asistencia ausentes; sin prueba. | faltante | §7.3.1–3, pp. 7–8 |
| W03: Bootstrap 5 o equivalente incluido localmente; alcance responsive obligatorio: casos de §6.3. | Web/Central | Recursos locales presentes; falta responsive de los casos moviles y ensayo fisico. | parcial | §7.3, p. 7; §7.11, p. 13 |
| W04: Video YouTube mediante iframe en escritorio y móvil; convertir URL normal a embed, móvil 100 % ancho y relación 16:9. | Web/Central | Video e iframe no implementados; sin prueba. | faltante | §7.4, p. 9 |
| W05: Constancia PDF generada a demanda, sin almacenarla previamente; nombre/apellido asistente, evento, edición, ciudad, fechas edición y fecha emisión. | Web/Central | PDF a demanda no implementado; sin prueba. | faltante | §7.5, p. 9 |
| W06: Disponibilidad con `fetch` y `async/await`, sin XMLHttpRequest ni recarga; servlet consulta al Central por SOAP. | Web/Central | fetch async/await -> Servlet -> Portal; duplicados rechazados en Central. 62 pruebas JS. | completo | §7.6, pp. 9–10 |
| W07: Filtro `jakarta.servlet.Filter` web intercepta consultas de evento escritorio/móvil, sin modificar servlets existentes; informa visita por SOAP. | Web/Central | Filtro de visitas, contador y contrato SOAP no implementados. | faltante | §7.7, p. 10 |
| W08: Cabezal del sitio ofrece búsqueda; nombre o descripción contienen texto sin distinguir mayúsculas; vacío lista todos los elegibles. | Web/Central | Busqueda requerida no implementada; sin prueba. | faltante | §7.8, p. 10 |
| W09: Excluir eventos finalizados y ediciones no Aceptadas de búsqueda; ordenar fecha alta descendente por defecto, alfabético asc/desc y volver al defecto; enlaces a consultas. | Web/Central | Filtros y tres ordenes requeridos no implementados; sin prueba. | faltante | §7.8, p. 10 |
| T01: Mantener pruebas previas y agregar JUnit de lógica: seguimiento válido/inválido, finalización, archivo aceptado/terminado, asistencia, búsqueda/órdenes y visitas. | Pruebas | Regresion actual aprobada; faltan pruebas de los nuevos casos no implementados. | parcial | §7.10, p. 12 |
| T02: Al menos 80 % de líneas de lógica del Central, medido con IntelliJ Run with Coverage. | Pruebas | JaCoCo logica 1733/2017 lineas (85.92%); falta medicion IntelliJ exigida. | parcial | §7.10, p. 12 |
| E01: Un comando Maven documentado genera `servidor.jar` y `web.war`, sin depender del IDE. | Entrega/documentacion | verify aislado produce servidor.jar y web.war con Java 25. | completo | §7.9, pp. 10–12 |
| E02: `servidor.jar` ejecutable con `java -jar`, con dependencias, Central y Swing; publica SOAP al iniciar. | Entrega/documentacion | CentralJarArranqueIT prueba JAR real HTTPS; Swing visual pendiente. | parcial | §7.9, p. 12 |
| E03: `web.war` listo para Tomcat; despliegue mediante Manager o copia a `webapps`. | Entrega/documentacion | TomcatPortalHttpsIT despliega WAR real; sitio movil pendiente. | parcial | §7.9, p. 12 |
| E04: Análisis actualizado: modelo de dominio y DSS de casos nuevos/modificados fundamentales. | Entrega/documentacion | Plan existente; modelo/DSS finales de nuevos casos pendientes. | parcial | §8.1, p. 13 |
| E05: Diseño distribuido: operaciones SOAP, datatypes, excepciones remotas, configuración y móvil. | Entrega/documentacion | Contratos y arquitectura documentados; diseno movil pendiente. | parcial | §8.2, p. 13 |
| E06: Entregar fuente completo abrible en IntelliJ y construible desde terminal. | Entrega/documentacion | Fuente completo compila aislado; apertura IntelliJ y entrega no ensayadas. | parcial | §8, p. 13 |
| E07: README con requisitos, comando de ambos paquetes, despliegue/ejecución por nodo y ejemplos/claves de `~/.eventosUy`. | Entrega/documentacion | README y despliegue existentes; faltan instrucciones del sitio movil. | parcial | §8, p. 13 |
| E08: Instrucciones de creación de base y datos: tres roles, usuarios siguiéndose, video, evento finalizado, edición archivada y otra archivable, registros con/sin asistencia. | Entrega/documentacion | Faltan datos demostrativos de los nuevos atributos y relaciones. | faltante | §8, p. 13 |
| E09: Entrega única Moodle: lunes **23/11/2026 a las 09:00**. Defensa mismo día, franjas matutina 11–15 y vespertina 15–19; turno específico por Moodle. | Entrega/documentacion | Fecha oficial 23/11/2026 09:00; turno y subida Moodle no verificables. | no verificable | §1, p. 1; §8, p. 13 |

## Casos anteriores y frontera Web

| Caso | Swing | Web y prueba |
| --- | --- | --- |
| Alta/consulta/modificacion usuario | ISistema directo | AltaUsuario, Usuarios, Usuario, ModificarUsuario -> Portal; pruebas de atomicidad, imagen, contrasena, autorizacion y rollback |
| Login/logout | Actor local | Login/Logout -> P03; HttpSession guarda token, Bearer hacia Central; pruebas P03 y Tomcat real |
| Categoria/aceptar-rechazar edicion | ISistema con Actor administrador | Sin autoridad administrativa remota; EscriturasActorTest |
| Alta/consulta evento | ISistema, alta compuesta con imagen | AltaEvento/Evento/Inicio -> Portal; rollback DAO y comprobaciones Swing |
| Alta/consulta edicion | ISistema, alta compuesta con imagen | AltaEdicion/ConsultaEdicion -> Portal; pruebas de edicion, propietario y estado |
| Alta/consulta tipo registro | ISistema con Actor | AltaTipoRegistro/ConsultaTipoRegistro -> Portal; autorizacion, duplicados, concurrencia |
| Registro/consulta registro | ISistema con Actor | RegistroEdicion/ConsultaRegistro -> Portal; cupos, codigo, propiedad y suplantacion |
| Alta/consulta institucion | ISistema | AltaInstitucion/ConsultaInstitucion -> Portal; imagen atomica y rollback |
| Alta/consulta patrocinio | ISistema | AltaPatrocinio/ConsultaPatrocinio -> Portal; permisos, visibilidad, transaccion y concurrencia |
| Imagen/disponibilidad | Utilidades Swing | Imagen/DisponibilidadUsuario -> Portal; validacion imagen, acceso y AJAX |

Estas filas inventarian los 21 Servlets actuales y los 16 casos Swing anteriores. Los filtros de seguridad y el listener no acceden a persistencia. Web conserva parsing HTTP, multipart, navegacion y mensajes; Sistema resuelve autorizacion, identidad, propiedad y reglas. No existe aun el filtro de visitas exigido por W07. El WAR inspeccionado y probado no incluye logica, JPA, Hibernate ni PostgreSQL. El modulo Web usa Central/logica solo en alcance test, no en runtime.

## Correcciones y commits de esta auditoria

| SHA | Correccion y verificacion |
| --- | --- |
| 29f1b518a0c4479f5087d2b2a3c1e660d8d4774a | Exigir Actor en todas las escrituras protegidas; eliminar sobrecargas que fabricaban administrador; migrar Swing, fixtures y semillas; cinco nuevas pruebas persistentes de autorizacion |
| 1c995c56a0ebfec6fde90e6770222cec9aa5927a | Evento/edicion e imagen se guardan con una operacion compuesta desde Swing; retirar segunda transaccion y reintentos parciales; dos regresiones DAO y diez comprobaciones Swing |
| 860f71e85d62615fbdf5631a16187cd0346ec11d | Retirar URL/credenciales JDBC predeterminadas del codigo y XML; cuatro pruebas de configuracion y prueba de arranque del JAR real |

Sistema ahora exige administrador para categoria y cambio de estado; rechaza asociar imagen a otro usuario o registrar a otro asistente. Las firmas protegidas requieren Actor; el alta publica de usuario sigue disponible intencionalmente. Se preservan Actor, P03, PBKDF2, validacion de imagen, bloqueos antes de hidratacion y las operaciones atomicas de perfiles, instituciones y patrocinios. No se cambiaron contratos remotos ni dependencias.

No se encontro otra ruta publica de ISistema que permita cambiar la contrasena sin Actor autorizado. Las comprobaciones directas y remotas permanecen separadas: Central deriva Actor de P03; ningun rol del formulario concede autoridad. CSRF de navegador se valida en Web y la autorizacion se vuelve a comprobar en Central. Los tokens permanecen en HttpSession del servidor, no se publican en formularios ni logs; P03 conserva vencimiento fijo.

## Pruebas y limitaciones

- Reactor Java 25 completo en arbol aislado: `mvn -o -Pcobertura verify`, SUCCESS. Regenera contratos, compila todos los modulos y produce ambos paquetes.
- JUnit ordinarias: 317 logica + 16 Central + 131 Web = **464**, cero fallos, errores u omitidas.
- Integracion explicita: **2**, CentralJarArranqueIT y TomcatPortalHttpsIT, ambas aprobadas. Total JUnit/IT: **466** casos distintos.
- JavaScript: **62/62**. Swing headless: **15/15** (diez guardados compuestos/barreras Actor y cinco patrocinios).
- JaCoCo de logica: **1733/2017 lineas, 85.92%**. No equivale a la medicion IntelliJ Run with Coverage exigida por T02; esa evidencia sigue pendiente. Tampoco convierte funcionalidades ausentes en completas.
- Antes de cada ejecucion persistente se verifico por JDBC `current_database() = eventosuy_test`. Las IT repiten la proteccion. No se uso ni reinicializo otra base.
- JAR probado fuera del arbol fuente, con configuracion ~/.eventosUy temporal y sin variables EVENTOS_DB_* en el proceso hijo: consulta SOAP HTTPS real contra PostgreSQL de prueba.
- Tomcat real: login/HttpSession, CSRF, SOAP HTTPS autenticado, PostgreSQL y error controlado 503 al caer Central; inspeccion del WAR sin persistencia.
- Primer intento de la nueva IT del JAR fallo al leer el log con MalformedInputException: Windows emitia otra codificacion. Se fijo UTF-8 explicitamente en stdout/stderr del proceso hijo; se repitieron las dos IT y aprobaron. No fue un fallo de transaccion o transporte.
- Pendientes manuales: Swing visual completo, navegadores/celular fisicos, cuatro nodos LAN y certificados reales, IntelliJ Coverage, defensa y Moodle. Los nuevos casos faltantes carecen de pruebas y no se consideran verificados.

Evidencia local: revision-commits-local/auditoria-lab3-dev-20261115/, incluidos configuracion-regresion.log, configuracion-it-final.log, javascript.log, manifiestos de archivos y arboles aislados. Son evidencia privada, no archivos de entrega ni commits.

## Equipo y comparacion de ramas

Se ejecuto `git fetch --all --prune`. El inventario detallado de ramas y autoria se conserva en [Integracion del equipo](INTEGRACION-EQUIPO-2026-11-15.md). No se identificaron nuevos aportes pendientes en las referencias revisadas.

| Autor / referencia | Aporte | Estado |
| --- | --- | --- |
| Emmanuel, origin/lab3-emmanuel, 2911ab1 | Disponibilidad SOAP/AJAX; padre a29ee1f | Adaptado previamente en 1cd5f5a a Portal HTTPS; no duplicar EventosService ni JavaScript |
| Valentina, 7db899c | Trece regresiones de eventos/categorias | Integradas en 45053f5; preservadas con Actor explicito |
| Bentancor, origin/cambios-eventos-uy-2026-11-14, 1afff269 | Usuarios/instituciones con imagen, perfiles, gestion y patrocinios | Integracion selectiva previa preservada; historial raiz independiente no fusionado |
| Adrian, 65cfcb3 | Patrocinios | Ancestro integrado; conservar validaciones y bloqueos posteriores |

`dev` y `origin/dev` apuntan a **5fb46db2743c10609c470c78b71ed8c210e8ba14**. Este es el ancestro comun de dev y lab3-desarrollo. dev tiene **cero commits exclusivos**: no hay funcionalidades ni archivos exclusivos que rescatar. `origin/lab3-desarrollo` apunta a a29ee1fc9f2362e812ca939068a4fc205ca1985f. Los commits posteriores siguen locales.

No hay conflictos textuales entre dev y nuestra rama en este estado: dev es ancestro y una integracion seria fast-forward. El riesgo pendiente es funcional, no un conflicto de Git. Las diferencias comprenden Central/contratos SOAP, migracion Web completa, seguridad, atomicidad, Swing, pruebas y documentacion. La lista exacta de commits y archivos esta en commits-solo-lab3.txt y archivos-vs-dev.txt de la evidencia local. El commit documental de este informe se identifica con `git log`.

No se creo rama temporal: la fase 6 exige auditoria satisfactoria y faltan 17 requisitos mas los parciales. No se ejecuto merge, cambio de rama, push ni PR. Tampoco se fuerza un merge entre historiales independientes.

## Plan concreto de cierre e integracion

1. Seguimiento: modelo/JPA/Sistema y pruebas; luego contrato SOAP y perfiles. Bloques pequenos que compilen independientemente.
2. Finalizacion y archivo: estados persistentes, fechas, autorizacion y visibilidad. La letra no define propiedad para finalizar como si lo hace para archivar: no inventar una regla sin documentarla.
3. Asistencia propia idempotente y constancia PDF autorizada; probar ajenos, inexistentes y rollback. No agregar una restriccion temporal no expresada por la letra.
4. Video y consultas; sitio movil con acceso exclusivo de asistentes y navegacion requerida.
5. Filtro de visitas -> SOAP -> contador atomico; top cinco Swing; busqueda y sus tres ordenes.
6. Datos demostrativos, modelo/DSS, documentacion, cobertura IntelliJ y ensayo LAN de cuatro dispositivos.
7. Repetir auditoria y fetch. Si se cierra el cumplimiento y dev sigue siendo ancestro, crear rama temporal desde origin/dev y hacer merge local de lab3-desarrollo. Si dev avanzo, revisar sus commits primero. Repetir pruebas sobre el resultado y presentarlo antes de publicar.

## Preservacion y ejecucion

Inicio: HEAD 31df84f64e847000ef1c6b8ec2ca46b4df47ca60, indice y versionados limpios; revision-commits-local/ ya existia. Estado y SHA-256 de originales registrados en estado-inicial.txt y hashes-iniciales.json. Cada commit se preparo desde su arbol aislado, verificando todo el indice y la conservacion byte a byte del arbol de trabajo. Respaldos, logs, credenciales y herramientas de auditoria quedaron fuera de commits. No se eliminaron archivos/ramas ni se uso reset, stash, rebase o pull.

Produccion: seguir README y configuracion ~/.eventosUy/central.properties / web.properties; `mvn package` genera servidor-central/target/servidor.jar y webapp/target/web.war. `java -jar servidor.jar` inicia Central HTTPS y Swing en el mismo proceso; `--sin-swing` sirve para comprobaciones sin interfaz. Desplegar web.war en Tomcat 11, configurar su confianza TLS y destino Central; no instalar PostgreSQL/JPA en Tomcat.

Pruebas/auxiliares que usan BaseDeDatos directamente requieren configurar explicitamente EVENTOS_DB_HOST, EVENTOS_DB_PORT, EVENTOS_DB_NAME=eventosuy_test, EVENTOS_DB_USER y EVENTOS_DB_PASSWORD. Ya no existen valores predeterminados de conexion. Nunca imprimir las credenciales ni elegir una base alternativa cuando falte configuracion.
