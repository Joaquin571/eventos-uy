# Estado actualizado de aportes del equipo - Laboratorio 3

## Corte y metodo

Fetch finalizado: **2026-11-16T20:18:02-03:00**.

Auditoria del 16/11/2026, sobre `lab3-desarrollo` en `e9181fe15e418d6ea0bd54255a5e1760e6b2a146`, antes del eventual commit exclusivo de este informe. Se ejecuto `git fetch --all --prune`: salida vacia, codigo 0. Se compararon los SHA de TODAS las referencias antes/despues: ninguna cambio, incluidas ramas existentes. No hay nuevos commits remotos desde la auditoria anterior. Esto describe lo publicado en el remoto accesible, no trabajo sin subir ni otros repositorios.

Estado inicial: indice vacio y ningun archivo versionado modificado; existia `?? revision-commits-local/`, evidencia local no confirmada, que se preservo. Se registraron hashes SHA-256 de todos los versionados y referencias antes/despues en `revision-commits-local/estado-aportes-20261116/`. No se ejecuto merge, rebase, cherry-pick, cambio de rama ni modificacion funcional.

Se inventariaron 31 ramas locales, 24 remotas reales (origin/HEAD es solo alias de origin/dev), y 180 commits distintos alcanzables. Se leyeron autor, correo, fecha de autor, asunto y archivos por commit. La autoria Git declarada no es una firma criptografica ni demuestra propiedad exclusiva de archivos copiados. Se agruparon los dos nombres de Valentina porque comparten correo, y las dos direcciones de Emmanuel/Joaquin571 porque comparten identidad declarada y contexto; no se confundio Joaquin571 con Joaquin Bentancor.

Se compararon contenidos de las ramas, APIs, entidades, DTO, publicadores, consumidores, vistas y pruebas con HEAD. La busqueda de funciones nuevas abarco 808 blobs fuente distintos de las puntas de las 55 ramas, incluidos historiales independientes; se descartaron falsos positivos de MIME PDF en Tomcat y de la prueba que rechaza imagen application/pdf. Tambien se revisaron los diffs historicos relevantes. No se dedujo cumplimiento por nombre de rama ni por presencia de un merge.

Fuente normativa releida: `Laboratorio 3.pdf`, 14 paginas, SHA-256 `d0f35fd8a59cf15f4c09b3f6cf13b991f439c0bd6f13f492670e46eaf9a28401`. Se cruzo con [auditoria previa](AUDITORIA-LAB3-DEV-2026-11-15.md) y el reparto vigente en [EQUIPO](EQUIPO/INTEGRANTE-1.md) y [contratos](CONTRATOS-EQUIPO.md). El reparto numerado no identifica nominalmente a los integrantes 2-5; no se inventaron nombres.

## Respuesta por integrante

| Integrante | Funcionalidades aportadas | Ya integradas | Pendientes de integrar | Trabajo faltante / responsabilidad |
| --- | --- | --- | --- | --- |
| Vos: Joaquin571; documentacion: Joaquin, integrante 1 | Base Maven/JPA, integraciones anteriores, Actor, autorizacion, atomicidad, SOAP/HTTPS, P03, configuracion, migracion Web, pruebas y documentacion | Implementaciones actuales y regresiones; 24 commits propios de Lab3 posteriores a dev | Cuatro commits de DSS LAB1 en Documentacion requieren revision documental; no son funciones Lab3 | J04: contador persistente de visitas y ranking Swing. J05: cobertura IntelliJ, datos/modelo y ensayo distribuido todavia incompletos |
| Emmanuel Lacava | Ediciones/tipos Web previos, correcciones de logica/Swing, pruebas; disponibilidad SOAP/AJAX en 2911ab1 | Casos previos conservados. Comportamiento disponibilidad y sus escenarios adaptados al Portal HTTPS en 1cd5f5a | Ninguna funcionalidad completa nueva encontrada pendiente. Revision2 tiene un hash no incorporado, pero sus relaciones/cupos estan cubiertos por el modelo JPA actual | La documentacion no lo asigna nominalmente a un puesto Lab3. No se le atribuye seguimiento, movil u otro faltante por deduccion |
| Joaquin Bentancor | Registro/consulta anterior; alta y modificacion de usuarios con imagen, instituciones, perfiles, disponibilidad y fachada de usuarios/patrocinios en 1afff269 | Atomicidad usuarios/perfiles/instituciones; consultas, privacidad, patrocinios y operaciones equivalentes en Sistema/Portal; registro previo preservado | Validacion especifica de login movil solo asistentes y su prueba: reutilizable, pero NO adaptada a P03. No hay sitio movil completo | Su codigo se presenta como bloque/integrante 2, pero el reparto general no confirma su nombre; no atribuirle por ello otros puestos |
| Valentina Carrique (valentina-carrique / Valentina) | Swing alta/consulta eventos/usuario/registro; imagenes y estados; Web eventos; 13 pruebas de eventos/categorias | Swing previo adaptado; Web actual remoto; 13/13 escenarios presentes en EventosYCategoriasTest con Actor y barrera JDBC | Ninguna funcionalidad o prueba de esa suite pendiente encontrada | Ultimo aporte publicado observado: 30/10. No hay implementacion Lab3 nueva en sus ramas. Puesto nominal Lab3 no documentado |
| Adrian Prado | Swing consulta/registro/edicion, Web login/usuarios anteriores, alta/consulta patrocinio y PatrocinioTest | Sus cuatro commits son ancestros y los comportamientos siguen presentes, reforzados con autorizacion y transacciones actuales | Ninguna funcionalidad nueva pendiente encontrada | Ultimo aporte observado: 26/10. No hay asignacion nominal Lab3 verificable |

No se encontraron mas identidades de autor en los 180 commits alcanzables. Los conteos de commits no miden esfuerzo ni cumplimiento: incluyen merges, documentacion y copias completas.

## Comparacion funcional y clasificacion

| Aporte / origen | Clasificacion | Evidencia actual y decision |
| --- | --- | --- |
| Emmanuel 2911ab1: disponibilidad nickname/correo SOAP | Integrada completamente (comportamiento adaptado) | Sistema.disponibleNickname/disponibleCorreo -> PortalPublicador -> ClienteCentral -> DisponibilidadUsuarioServlet. webapp/src/test/java/web/soap/DisponibilidadSoapTest.java conserva tres escenarios de normalizacion, entradas invalidas y faults sanitizados; web/DisponibilidadUsuarioTest.java prueba adaptador HTTP |
| Emmanuel: extension de EventosPublicador/EventosService y ClienteEventosCentral | Reemplazada | Disponibilidad se publica en contratos/ServicioPortal y Portal HTTPS, no en el servicio publico de listado. No incorporar literalmente otro contrato para duplicar la operacion |
| Emmanuel: disponibilidad-usuario.js y escenarios JS | Integrada completamente (adaptada) | webapp/src/main/webapp/js/disponibilidad-usuarios.js enlazado desde alta-usuario.jsp; debounce, AbortController, rechazo de respuestas antiguas, async/await y estado de validez; escenarios en disponibilidad-usuarios.test.cjs. 62/62 pruebas JS ejecutadas en esta auditoria |
| Emmanuel: correcciones de Revision2 cb4d004 | Reemplazada | Cambiaba relaciones Asistente-Institucion, Registro-Asistente y colecciones de TipoRegistro sin JPA. Hoy esas asociaciones existen con mapeos JPA y control transaccional; RegistroTest/PatrocinioTest y consultas cubren los recorridos. No trasladar clases de src/ de agosto sobre logica actual |
| Bentancor: alta usuario con DtImagen | Integrada completamente | ISistema/Sistema altaAsistente/altaOrganizador compuestos, cascada en ManejadorUsuarios; AltaUsuarioPanel ya no hace segunda transaccion. AltaUsuarioConImagenTest; commit 56ac172 |
| Bentancor: modificar datos, clave opcional e imagen | Integrada completamente (adaptada y reforzada) | Sistema y ManejadorUsuarios con Actor y bloqueo antes de hidratar; ModificacionPerfilConImagenTest, ModificacionPerfilAtomicaTest, AsociacionImagenConcurrenciaTest y ConcurrenciaEscritoresRelacionadosTest. b96f599/27694c5 preservan imagen y evitan restaurar hash antiguo |
| Bentancor: alta institucion con imagen | Integrada completamente | ISistema.altaInstitucion(datos,imagen,Actor), Sistema/manejador, AltaInstitucionPanel/Servlet; InstitucionConImagenTest; 0e3ea83. No significa CRUD de modificacion/borrado: la rama tampoco aporta ese caso |
| Bentancor: usuarios/perfil/instituciones/patrocinios por servicio | Reemplazada, con comportamiento incorporado | IUsuariosWeb/ControladorUsuariosWeb no se copiaron: Sistema centraliza reglas; PortalPublicador y contratos de transporte exponen operaciones; Servlet/ClienteCentral solo adaptan. PortalAutorizacionTest, ConsultaPatrociniosVisibilidadTest y PortalPersistenciaHttpsTest |
| Bentancor: SesionesUsuarios y token como parametro SOAP | Descartada justificadamente | Su validar renueva la expiracion. Se conserva exclusivamente P03 de vencimiento fijo y token en Authorization Bearer, fuera de DTO/formularios. No integrar SesionesUsuariosTest literalmente ni reintroducir sesiones paralelas |
| Bentancor: publicador HTTP de usuarios y DTO de dominio JAXB | Descartada justificadamente | PublicadorUsuarios construye http://; se conserva Central HTTPS y modulo contratos separado. Evita restaurar HTTP o acoplar tipos JPA al transporte |
| Bentancor: iniciarSesion(..., boolean movil) y test organizadorNoPuedeIngresarAlModoMovil | Pendiente (subfuncion reutilizable) | En 1afff269, logica/src/main/java/usuarios/ControladorUsuariosWeb.java y logica/src/test/java/usuarios/UsuariosWebTest.java. Rechaza organizadores tras autenticar. Nuestra API P03 no distingue canal movil: falta politica central equivalente y test adaptado; no aceptar roles del formulario. No copiar su motor de sesiones |
| Bentancor: bloque completo usuarios/instituciones/sesiones | Integrada parcialmente | Lo funcional equivalente anterior esta incorporado; politica movil falta, infraestructura incompatible se reemplazo. Su raiz contiene 213 archivos copiados: no atribuirle como novedades propias toda la base previa |
| Valentina: EventosYCategoriasTest 7db899c | Integrada completamente | Trece metodos @Test conservados. Diff contra HEAD cambia preparacion de base a PruebaConBaseSegura y pasa Actor explicito; mantiene las aserciones de duplicados, categorias, rollback y asociacion de edicion. 45053f5 |
| Valentina: Swing 20af1e4/00e4750/b938a9c | Integrada completamente (adaptada) | ModificarUsuarioPanel, altas con imagen y consultas con estado; registro solo ACEPTADA. ConsultaEventoPanel/ConsultaEdicionEventoPanel/RegistroEdicionEventoPanel actuales. Imagen compuesta reforzada posteriormente por 1c995c5 |
| Adrian: patrocinio 65cfcb3 | Integrada completamente (adaptada) | Alta/ConsultaPatrocinio, Sistema, ManejadorPatrocinios, PatrocinioTest actuales. Privacidad/codigo, propiedad, unicidad y bloqueos mas recientes se conservan |
| DSS LAB1 en origin/Documentacion: e271ea8, 880a127, b39034c, 1b64ac4 | Pendiente documental | docs/dss/*.drawio*.png ausentes en HEAD; cuatro commits de Joaquin571. No demuestran modelo/DSS Lab3 actualizado. Revisar visualmente y vigencia antes de recuperar; no se descartan por ser antiguos |
| feature/correcciones-logica 090da2c (solo local) | Descartada justificadamente / efecto ya presente | Solo borra el archivo accidental `how --stat cb4d004`, que no existe en HEAD. No aporta funcionalidad ni precisa integracion |

La suite de Bentancor se compara por escenarios, no por nombre de clase: sus pruebas de atomicidad/privacidad tienen equivalentes actuales; la prueba de rechazo de organizador en modo movil NO. No se afirma haber trasladado cada clase de pruebas literalmente.

## Requisitos que nadie tiene completos en las ramas observadas

| Funcionalidad obligatoria | Lo encontrado | Responsable documentado y trabajo pendiente |
| --- | --- | --- |
| Seguimiento | Sin API, relacion persistente, UI o pruebas del caso | Integrante 3, D02; nombre no consignado |
| Video | Sin atributo/contrato, alta ni iframe YouTube | Integrante 3, D03; coordina movil con 5 |
| Finalizar evento | Sin estado, operacion ni filtrado completo | Integrante 3, D04 |
| Archivar edicion | Sin ARCHIVADA/fecha ni operacion/visibilidad | Integrante 4, R03; coordina 3 |
| Asistencia | Sin dato ni confirmacion propia idempotente | Integrante 4, R02 |
| Constancia PDF | Sin generador ni descarga autorizada | Integrante 4, R04; biblioteca/licencia coordinada con Joaquin |
| Busqueda y ordenes | No existe busqueda textual/estados/tres ordenes de la letra; buscar por clave no equivale | Integrante 5, M03 |
| Visitas persistentes / top cinco | Sin contador atomico ni ranking Swing | Joaquin, J04 |
| Filtro Web de visitas SOAP | Sin Filter para consultas escritorio/movil | Integrante 5, M04; depende J04 |
| Sitio movil completo | Sin ruta/vistas/menu/recorridos propios; solo guardia de login en rama Bentancor | Integrante 5, M01/M02/M05; backend R02/D03 y politica de identidad coordinada con 2 |

'Nadie' significa que no hay implementacion completa en las ramas locales/remotas disponibles tras fetch. No prueba que no exista trabajo sin publicar. No corresponde convertir responsabilidades numeradas en nombres personales sin un acuerdo nominal.

## Validacion y limite de evidencia

Esta tarea no modifico codigo ni accedio a PostgreSQL. Se releyo la implementacion efectiva y las pruebas existentes, y se ejecutaron nuevamente 62 pruebas JavaScript: todas aprobadas. Las 466 pruebas JUnit/IT y 15 comprobaciones Swing son evidencia de la auditoria inmediatamente anterior sobre el mismo codigo funcional, no una nueva ejecucion de esta sesion. Se comparo ese arbol validado con los archivos funcionales actuales. No se ejecutaron las ramas antiguas ni se certifica que compilen tal como estan.

La matriz oficial anterior sigue con 18 completos, 18 parciales, 17 faltantes y 3 no verificables. No se cambio la clasificacion por mera existencia de una idea parcial en una rama. No se verifican trabajo privado, asignaciones verbales, cuatro dispositivos fisicos, Moodle ni IntelliJ Coverage.

## dev e integracion futura

`origin/dev` y `dev` siguen en **5fb46db2743c10609c470c78b71ed8c210e8ba14**, sin nuevos commits. Es el ancestro comun con HEAD; conteo simetrico: **0 exclusivos de dev / 24 de lab3-desarrollo**, antes del commit de este informe. `origin/lab3-desarrollo` sigue en a29ee1fc9f2362e812ca939068a4fc205ca1985f, 21 commits por detras de nuestra rama.

No hay conflictos textuales que resolver entre estas dos puntas: una integracion desde dev seria fast-forward. Esto no acredita cumplimiento del laboratorio. Faltaria decidir si se publica una base en progreso o se espera al cierre funcional, completar los requisitos si el objetivo es entregar, repetir fetch/verificaciones y preparar la rama temporal autorizada en una tarea de integracion. Este informe no modifica dev ni integra codigo.

Recomendacion: reutilizar primero la politica movil de Bentancor como especificacion/prueba sobre P03, sin copiar su infraestructura; acordar nominalmente los puestos 2-5 y pedir estado de los casos nuevos. Luego cerrar D02-D04, R02-R04, J04 y M01-M05 por bloques verificables. Separar la recuperacion documental LAB1 de la documentacion pendiente Lab3. No hacer merge de raices independientes ni copiar proyectos completos.

## Evidencia reproducible

En revision-commits-local/estado-aportes-20261116/: refs-antes.txt, refs-despues.txt, fetch.txt, hashes-iniciales.txt, inventario.json (cada commit con todos sus archivos y referencias alcanzables), busqueda-funcionalidades.json (coincidencias por blob/rama), letra-oficial.txt y javascript.log. Esa evidencia no se agrega al commit. Los apendices siguientes incluyen todos los SHA de ramas y commits propios; ramas contenedoras no implican autoria de todos sus cambios.

## Cruce de los 35 requisitos parciales o faltantes

La referencia normativa y el criterio exacto de cada ID se conservan en REQUISITOS.md y en la auditoria previa. Cada fila identifica aporte remoto, presencia/reutilizacion y trabajo adicional. Ninguna de estas filas pasa a completo por este analisis.

| ID / requisito | Rama, autor, commit/archivo o ausencia | Responsable documentado | Integracion/reutilizacion y trabajo adicional |
| --- | --- | --- | --- |
| A01: Separar Servidor Central y Servidor Web; exclusivamente el Central mantiene lógica y persistencia. | No hay otro despliegue aportado | J01/J02/J05, Joaquin y equipo | Ensayo dos equipos |
| A18: Java SE 25, IntelliJ para desarrollo, Maven, Tomcat 11, PostgreSQL mediante JPA solo en Central y Git. | Toolchain actual; no evidencia IDE nueva | J05/equipo | Comprobar IntelliJ |
| F01: Persistir todos los nuevos datos: relaciones de seguimiento, video, asistencia, finalización, archivo y visitas. | Ninguna rama tiene nuevos datos completos | J04, D02-D04, R02/R03 | Persistir nuevos datos y regresiones |
| F03: Consultas Swing incluyen eventos finalizados y ediciones archivadas, indicando su estado. | Ninguna rama tiene finalizado/archivado | D04/R03, integrantes 3/4 | Modelo y consultas Swing |
| F04: Swing: cinco eventos más visitados, orden descendente, posición, nombre y cantidad de visitas. | Ninguna rama tiene top cinco | J04, Joaquin | Persistencia contador/ranking Swing |
| F05: Alta de edición: solo eventos no finalizados; video YouTube opcional, además de datos anteriores; nombre único, organizador autenticado y estado inicial Ingresada. | Alta previa presente en ramas Emmanuel/Valentina; no video/finalizacion | D03/D04, integrante 3 | Ampliar modelo/DTO/alta SOAP |
| F06: Consulta de edición: video en página si existe; conservar tipos, patrocinios y registro propio; dueño ve registros con indicador de asistencia. | Detalle base conservado; no video/asistencia | D03/R02, integrantes 3/4 | DTO y consultas autorizadas |
| F07: Asistentes y organizadores pueden seguir y dejar de seguir desde listado/perfil; no a sí mismos ni duplicar relación. | Ninguna rama implementa seguimiento | D02, integrante 3 | Modelo/Sistema/SOAP/UI/pruebas |
| F08: Organizador finaliza evento no finalizado; se oculta de listas/búsquedas y no admite nuevas ediciones. | Ninguna rama implementa finalizar | D04, integrante 3 | Estado/filtros/alta posterior |
| F09: Organizador archiva una edición propia Aceptada cuyo fin es anterior al día actual; registrar fecha de archivo. | Ninguna rama implementa archivo | R03, integrante 4 | Transaccion/fecha/propiedad |
| F10: Archivadas no aparecen en listas/búsquedas generales; se consultan en perfil de su organizador; registros se conservan y asistente los ve en su perfil. | Ninguna rama implementa visibilidad archivadas | R03/D04, integrantes 4/3 | Conservar registros y accesos propios |
| F11: Asistente descarga constancia PDF desde su registro solo si confirmó asistencia. | Ninguna rama genera constancia autorizada | R04, integrante 4 | R02 antes; PDF a demanda |
| F12: Perfil consultado muestra seguidores/seguidos y acciones de seguir/dejar de seguir cuando correspondan. | Bentancor 1afff269 ControladorUsuariosWeb.consultarPerfil: perfil base sin relaciones; reemplazado por Sistema.consultarPerfil | D02, integrante 3 | Reutilizar perfil actual y agregar seguimiento |
| F14: Móvil: inicio por nickname/correo y contraseña; solo asistentes; rechazar organizadores y credenciales inválidas. | Bentancor 1afff269 ControladorUsuariosWeb.iniciarSesion y UsuariosWebTest: guardia movil no integrada | W01/M01, integrantes 2/5 | Reutilizable como politica central P03; no copiar sesiones |
| F15: Móvil: cierre de sesión vuelve al inicio de sesión. | Logout base en ramas previas y actual; no ruta movil | M01, integrante 5; W01 coordina 2 | Navegacion movil y regresion |
| F16: Móvil: evento → ediciones Aceptadas → detalle completo con organizador, tipos, patrocinios, imagen/video y registro propio si existe. | Consulta base Emmanuel/Bentancor/Valentina; no sitio movil | M02, integrante 5; D03/R02 | UI movil y datos ampliados |
| F17: Móvil: consultar registros propios y detalle de tipo, fecha, costo, código usado y asistencia. | Bentancor d7f2333 Registro/ConsultaRegistro: base integrada; no asistencia/movil | M02/R02, integrantes 5/4 | Ampliar DTO/UI conservando privacidad |
| F18: Móvil: registrar asistencia a edición en que está registrado; persistir confirmación; si ya estaba confirmada, informarlo y no volver a ofrecerla. | Ninguna rama confirma asistencia | R02/M02, integrantes 4/5 | Confirmacion propia idempotente |
| W01: Móvil dentro del mismo `web.war`, ruta propia y diseño para pantallas pequeñas, reutilizando servlets y acceso SOAP. | Ninguna rama contiene sitio movil propio | M01/M02, integrante 5 | Ruta y vistas SOAP |
| W02: Pantalla inicial de login; navbar con Consulta Edición, Consulta Registro, Registrar Asistencia y Logout; hamburguesa en ancho reducido. | Ninguna rama contiene menu movil requerido | M01/M02, integrante 5 | Navbar/registro/asistencia/logout |
| W03: Bootstrap 5 o equivalente incluido localmente; alcance responsive obligatorio: casos de §6.3. | Bootstrap local en base compartida; no casos moviles completos | M05, integrante 5 | Responsive y ensayo fisico |
| W04: Video YouTube mediante iframe en escritorio y móvil; convertir URL normal a embed, móvil 100 % ancho y relación 16:9. | Ninguna rama tiene iframe/video de edicion | D03/M05, integrantes 3/5 | Validar URL y embed 16:9 |
| W05: Constancia PDF generada a demanda, sin almacenarla previamente; nombre/apellido asistente, evento, edición, ciudad, fechas edición y fecha emisión. | Solo falsos positivos application/pdf de Tomcat/rechazo de imagen | R04, integrante 4 | Generar y autorizar PDF |
| W07: Filtro `jakarta.servlet.Filter` web intercepta consultas de evento escritorio/móvil, sin modificar servlets existentes; informa visita por SOAP. | Ninguna rama tiene filtro de visitas | M04/J04, integrante 5/Joaquin | Filtro SOAP y contador Central |
| W08: Cabezal del sitio ofrece búsqueda; nombre o descripción contienen texto sin distinguir mayúsculas; vacío lista todos los elegibles. | Ninguna rama tiene busqueda textual requerida | M03, integrante 5 | Nombre/descripcion/vacio |
| W09: Excluir eventos finalizados y ediciones no Aceptadas de búsqueda; ordenar fecha alta descendente por defecto, alfabético asc/desc y volver al defecto; enlaces a consultas. | Ninguna rama tiene filtros/tres ordenes requeridos | M03, integrante 5; D04/R03 | Ordenar y enlazar sin filtrar privados |
| T01: Mantener pruebas previas y agregar JUnit de lógica: seguimiento válido/inválido, finalización, archivo aceptado/terminado, asistencia, búsqueda/órdenes y visitas. | Valentina 7db899c y Emmanuel 8ec0c7c/2911ab1: suites previas incorporadas; no tests casos ausentes | Todos por dominio; J05 coordina | Agregar pruebas al implementar cada caso |
| T02: Al menos 80 % de líneas de lógica del Central, medido con IntelliJ Run with Coverage. | JaCoCo actual 85.92%; ningun informe nuevo IntelliJ | J05/equipo | Medicion IntelliJ exigida |
| E02: `servidor.jar` ejecutable con `java -jar`, con dependencias, Central y Swing; publica SOAP al iniciar. | JAR actual probado; ninguna evidencia visual nueva | J02/J05, Joaquin | Ensayo Swing real sin IDE |
| E03: `web.war` listo para Tomcat; despliegue mediante Manager o copia a `webapps`. | WAR actual probado; sin sitio movil en otras ramas | W05/M01-M05 | Completar movil |
| E04: Análisis actualizado: modelo de dominio y DSS de casos nuevos/modificados fundamentales. | Documentacion: cuatro commits DSS LAB1 sin incorporar; no DSS Lab3 | D05/R05 y equipo | Recuperar solo si vigente; producir analisis Lab3 |
| E05: Diseño distribuido: operaciones SOAP, datatypes, excepciones remotas, configuración y móvil. | Contratos actuales; ninguna documentacion movil implementada nueva | J01/J05 y M05 | Diseno del movil final |
| E06: Entregar fuente completo abrible en IntelliJ y construible desde terminal. | Fuente actual construible; sin nueva evidencia IntelliJ | J05/equipo | Abrir y verificar entrega |
| E07: README con requisitos, comando de ambos paquetes, despliegue/ejecución por nodo y ejemplos/claves de `~/.eventosUy`. | README existente; instrucciones movil inexistente | J05/M05 y equipo | Documentar recorrido final |
| E08: Instrucciones de creación de base y datos: tres roles, usuarios siguiéndose, video, evento finalizado, edición archivada y otra archivable, registros con/sin asistencia. | Ninguna rama tiene dataset nuevos estados/seguimiento/asistencia | D05/R05/J05 y equipo | Seed completo en entorno aislado |

## Inventario completo de ramas

Fechas son las de autor del commit punta (zona horaria registrada), no la fecha de subida al servidor. Exclusivos = commits no alcanzables desde HEAD; cero no garantiza por si solo preservacion de comportamiento.

| Referencia | SHA completo | Autor de punta / fecha | Commits exclusivos | Relacion / decision |
| --- | --- | --- | --- | --- |
| `local/ConsultaeventoYAltacat` | `87ecc0b7050697385a7655cb49f03dfc963fbc6c` | Joaquin571 / 2026-08-31 21:47:58 -0300 | 0 | Ancestro, historial ya alcanzable |
| `local/Documentación` | `1b64ac4e6016fe10a23682ede978d8f4e9279703` | Joaquin571 / 2026-08-17 18:22:26 -0300 | 4 | Diverge; contenido comparado en matriz funcional |
| `local/Registros_Y_Eventos` | `8b5b34fb2efd6765304d8b37d8ca4d6672a1c380` | Joaquin571 / 2026-08-29 21:08:15 -0300 | 0 | Ancestro, historial ya alcanzable |
| `local/Revision4` | `1c9bfccac067c77104b4ff1ab5ca95f8e5aa5be9` | Joaquin571 / 2026-09-10 21:42:08 -0300 | 0 | Ancestro, historial ya alcanzable |
| `local/altaEvento` | `f44eb0aad8f97343c5f1f4196a489320f6dc043f` | Joaquin571 / 2026-08-29 09:15:36 -0300 | 0 | Ancestro, historial ya alcanzable |
| `local/dev` | `5fb46db2743c10609c470c78b71ed8c210e8ba14` | Joaquin571 / 2026-11-03 21:16:06 -0300 | 0 | Ancestro, historial ya alcanzable |
| `local/feature/alta-institucion` | `bbe797efb93b55270a794a9c273f9967cb3147ac` | Joaquin571 / 2026-08-26 21:59:37 -0300 | 0 | Ancestro, historial ya alcanzable |
| `local/feature/clases_faltantes` | `1b78de8523594bc0abd123814e8e856a8b443ecb` | Joaquin571 / 2026-08-28 22:32:09 -0300 | 0 | Ancestro, historial ya alcanzable |
| `local/feature/correcciones-logica` | `090da2c801547c5b37a4819db16e82d5f335214e` | Joaquin571 / 2026-08-31 23:26:06 -0300 | 1 | Diverge; contenido comparado en matriz funcional |
| `local/feature/instituciones-patrocinios` | `f789eed23f5e31c13d5d784f023ae8589241aa28` | Joaquin571 / 2026-10-04 20:46:54 -0300 | 0 | Ancestro, historial ya alcanzable |
| `local/feature/jpa-entidades` | `3fac20a4902f6dc5aaf605bcddeb4b2469ba32d2` | Joaquin571 / 2026-08-31 22:59:34 -0300 | 0 | Ancestro, historial ya alcanzable |
| `local/feature/modificar-usuario` | `af8b3ee0e64980b298d7b956f145967b5e41c5ae` | Joaquin571 / 2026-08-25 13:49:10 -0300 | 0 | Ancestro, historial ya alcanzable |
| `local/feature/patrocinio` | `9b047b76659416ec9c24647f56a07b12de7f8030` | Joaquin571 / 2026-08-28 20:54:06 -0300 | 0 | Ancestro, historial ya alcanzable |
| `local/feature/persistencia` | `d5293a00c74fc1898d88f678541d93d3017fdbeb` | Joaquin571 / 2026-08-31 22:03:35 -0300 | 0 | Ancestro, historial ya alcanzable |
| `local/feature/persistencia-jpa` | `6bb0eed70e1e2b14673a0d17edf5bda42a4d211d` | Joaquin571 / 2026-09-08 16:03:14 -0300 | 0 | Ancestro, historial ya alcanzable |
| `local/lab2-cobertura` | `0284e7cc1240fc7e7f08d5454d70aea0bc56301b` | Joaquin571 / 2026-10-29 21:49:22 -0300 | 0 | Ancestro, historial ya alcanzable |
| `local/lab2-consultas-edicion-tipo` | `69883001d6633031185f63cd1b9064905a226419` | Joaquin571 / 2026-10-28 22:39:05 -0300 | 0 | Ancestro, historial ya alcanzable |
| `local/lab2-integracion-equipo` | `4880c2f05e48ed8a18d1faa1922dff1bbd7a36b6` | Joaquin571 / 2026-10-29 19:54:20 -0300 | 0 | Ancestro, historial ya alcanzable |
| `local/lab2-logica-estado-edicion` | `bf5ed78da2fc652bf2a325092cf1c9e920f0375f` | Joaquin571 / 2026-10-19 07:27:47 -0300 | 0 | Ancestro, historial ya alcanzable |
| `local/lab2-logica-login` | `48ffeaa294f3fcc0085d3df478199fddf429f12d` | Joaquin571 / 2026-10-19 07:11:59 -0300 | 0 | Ancestro, historial ya alcanzable |
| `local/lab2-patrocinios-web` | `489245c32d0051b09bae13274631d8d2818384de` | Joaquin571 / 2026-10-27 23:08:57 -0300 | 0 | Ancestro, historial ya alcanzable |
| `local/lab2-seguridad-patrocinios` | `489245c32d0051b09bae13274631d8d2818384de` | Joaquin571 / 2026-10-27 23:08:57 -0300 | 0 | Ancestro, historial ya alcanzable |
| `local/lab2-swing` | `9c8b0a3f479939f758a43152a5a22be0ddd6042c` | Joaquin571 / 2026-10-29 19:42:53 -0300 | 0 | Ancestro, historial ya alcanzable |
| `local/lab2-swing-datos` | `2524e695bfb7314ab65e408d7527f13abfa62da7` | Joaquin571 / 2026-10-26 08:11:24 -0300 | 0 | Ancestro, historial ya alcanzable |
| `local/lab2-web-infraestructura` | `d6c3acc5aa159982632874a76b9ea399ed1baf97` | Joaquin571 / 2026-10-19 16:31:49 -0300 | 0 | Ancestro, historial ya alcanzable |
| `local/lab2-web-institucion` | `3b3eccbf841f3bc7f3b7d459ba634f53c8fdb0af` | Joaquin571 / 2026-10-19 22:02:49 -0300 | 0 | Ancestro, historial ya alcanzable |
| `local/lab2-web-usuarios` | `47b3ccbcad188d294dc504e72fa511b58d9b078b` | Joaquin571 / 2026-10-19 17:18:19 -0300 | 0 | Ancestro, historial ya alcanzable |
| `local/lab3-desarrollo` | `e9181fe15e418d6ea0bd54255a5e1760e6b2a146` | Joaquin571 / 2026-11-16 19:56:02 -0300 | 0 | Ancestro, historial ya alcanzable |
| `local/restructura-modulos` | `58978569a754931f89448405f806e2542eae57f2` | Joaquin571 / 2026-10-18 16:08:05 -0300 | 0 | Ancestro, historial ya alcanzable |
| `local/revision` | `0e0f61c2e485dd1718a96cf60f67d4ab7305bf5d` | Emmanuel Lacava / 2026-08-29 20:03:06 -0300 | 0 | Ancestro, historial ya alcanzable |
| `local/swingEdicionEventos` | `8569d035327b54bd79c95658efa1ea940bba85e4` | valentina-carrique / 2026-09-12 13:42:32 -0300 | 0 | Ancestro, historial ya alcanzable |
| `origin/ConsultaeventoyAltacat` | `196dac7b1271d36dbfe6bf01f2aaebb3e783d863` | valentina-carrique / 2026-08-30 20:04:25 -0300 | 0 | Ancestro, historial ya alcanzable |
| `origin/Documentación` | `1b64ac4e6016fe10a23682ede978d8f4e9279703` | Joaquin571 / 2026-08-17 18:22:26 -0300 | 4 | Diverge; contenido comparado en matriz funcional |
| `origin/Revision2` | `cb4d004bb1cb95b4fa65fed6d889c89915791e35` | Emmanuel Lacava / 2026-08-31 22:12:42 -0300 | 1 | Diverge; contenido comparado en matriz funcional |
| `origin/Revision6` | `21d69b5e364bfdee450a6c50274b53c3629928e0` | Joaquin571 / 2026-09-10 22:21:57 -0300 | 0 | Ancestro, historial ya alcanzable |
| `origin/cambios-eventos-uy-2026-11-14` | `1afff26908f57886599c3630d12194e158f8ce88` | Joaquin Bentancor / 2026-11-14 18:03:29 -0300 | 1 | Sin ancestro comun; comparar/adaptar contenido |
| `origin/caso_consultaDeRegistro` | `389caede11f8137c4f6916e5d4c6a6e8c168762d` | valentina-carrique / 2026-09-13 10:02:52 -0300 | 0 | Ancestro, historial ya alcanzable |
| `origin/consultaeventoarreglado` | `a0489507a26cebd13d13b6a4e1718a186558d82c` | valentina-carrique / 2026-09-12 15:06:29 -0300 | 0 | Ancestro, historial ya alcanzable |
| `origin/dev` | `5fb46db2743c10609c470c78b71ed8c210e8ba14` | Joaquin571 / 2026-11-03 21:16:06 -0300 | 0 | Ancestro, historial ya alcanzable |
| `origin/feature/casos_adrian` | `65cfcb3f7674a8e2b6ebda81bb4977f110bb5a8c` | Adrian Prado / 2026-10-26 01:36:29 -0300 | 0 | Ancestro, historial ya alcanzable |
| `origin/feature/instituciones-patrocinios` | `f789eed23f5e31c13d5d784f023ae8589241aa28` | Joaquin571 / 2026-10-04 20:46:54 -0300 | 0 | Ancestro, historial ya alcanzable |
| `origin/feature/persistencia` | `d5293a00c74fc1898d88f678541d93d3017fdbeb` | Joaquin571 / 2026-08-31 22:03:35 -0300 | 0 | Ancestro, historial ya alcanzable |
| `origin/feature/registros-joaquinb` | `3b91fb311133681fec04aa01a686a1a3d54f46a5` | Joaquin571 / 2026-10-19 22:03:15 -0300 | 0 | Ancestro, historial ya alcanzable |
| `origin/feature/swing-usuario-imagenes` | `b938a9c7642719438b229b6eafe6bf731e5adf1c` | valentina-carrique / 2026-10-29 18:50:37 -0300 | 0 | Ancestro, historial ya alcanzable |
| `origin/feature/tests-eventos-categorias` | `7db899c3d580558f6266085ddf34c94c90e065d0` | valentina-carrique / 2026-10-30 17:58:36 -0300 | 1 | Diverge; contenido comparado en matriz funcional |
| `origin/joaquin-b-registros` | `d7f2333ec3185e62ee55f482494ab41dca462ac9` | Joaquin Bentancor / 2026-10-25 21:08:02 -0300 | 0 | Ancestro, historial ya alcanzable |
| `origin/lab2-Emmanuel` | `db5b7f8aecbca59bca8ad02f0a92e002311aa48c` | Emmanuel Lacava / 2026-10-06 20:50:36 -0300 | 0 | Ancestro, historial ya alcanzable |
| `origin/lab2-cobertura` | `0284e7cc1240fc7e7f08d5454d70aea0bc56301b` | Joaquin571 / 2026-10-29 21:49:22 -0300 | 0 | Ancestro, historial ya alcanzable |
| `origin/lab2-emmanuel-tests` | `8ec0c7c23db3d23a965f184e695fb51575c704c2` | Emmanuel Lacava / 2026-10-29 11:02:16 -0300 | 0 | Ancestro, historial ya alcanzable |
| `origin/lab2-swing-datos` | `2524e695bfb7314ab65e408d7527f13abfa62da7` | Joaquin571 / 2026-10-26 08:11:24 -0300 | 0 | Ancestro, historial ya alcanzable |
| `origin/lab2-web-usuarios` | `47b3ccbcad188d294dc504e72fa511b58d9b078b` | Joaquin571 / 2026-10-19 17:18:19 -0300 | 0 | Ancestro, historial ya alcanzable |
| `origin/lab3-desarrollo` | `a29ee1fc9f2362e812ca939068a4fc205ca1985f` | Joaquin571 / 2026-11-06 21:50:32 -0300 | 0 | Ancestro, historial ya alcanzable |
| `origin/lab3-emmanuel` | `2911ab1a8135bdc20b69e22c93a654bf1709c239` | Emmanuel Lacava / 2026-11-15 13:21:11 -0300 | 1 | Diverge; contenido comparado en matriz funcional |
| `origin/main` | `075166cd8ab72b682dfaf7f657cc82b4a5a0342a` | Joaquin571 / 2026-08-04 23:08:03 -0300 | 0 | Ancestro, historial ya alcanzable |
| `origin/swingEdicionEventos` | `8569d035327b54bd79c95658efa1ea940bba85e4` | valentina-carrique / 2026-09-12 13:42:32 -0300 | 0 | Ancestro, historial ya alcanzable |

## Inventario de autoria y archivos

Los siguientes apendices son exhaustivos para los commits alcanzables al corte. Un archivo aparece si fue modificado por algun commit del autor; en commits raiz es una copia completa, no una novedad atribuible. Para merges, diff-tree sin -m no atribuye al integrador cada archivo de los padres. El JSON de evidencia conserva la relacion exacta commit-archivos-ramas.

### Vos / Joaquin571

139 commits propios; ultimo aporte por fecha de autor: 2026-11-16T19:56:02-03:00; 603 rutas historicas afectadas.

Referencias que contienen sus commits (no todas son ramas personales): `local/ConsultaeventoYAltacat`, `local/Documentación`, `local/Registros_Y_Eventos`, `local/Revision4`, `local/altaEvento`, `local/dev`, `local/feature/alta-institucion`, `local/feature/clases_faltantes`, `local/feature/correcciones-logica`, `local/feature/instituciones-patrocinios`, `local/feature/jpa-entidades`, `local/feature/modificar-usuario`, `local/feature/patrocinio`, `local/feature/persistencia`, `local/feature/persistencia-jpa`, `local/lab2-cobertura`, `local/lab2-consultas-edicion-tipo`, `local/lab2-integracion-equipo`, `local/lab2-logica-estado-edicion`, `local/lab2-logica-login`, `local/lab2-patrocinios-web`, `local/lab2-seguridad-patrocinios`, `local/lab2-swing`, `local/lab2-swing-datos`, `local/lab2-web-infraestructura`, `local/lab2-web-institucion`, `local/lab2-web-usuarios`, `local/lab3-desarrollo`, `local/restructura-modulos`, `local/revision`, `local/swingEdicionEventos`, `origin/ConsultaeventoyAltacat`, `origin/Documentación`, `origin/Revision2`, `origin/Revision6`, `origin/caso_consultaDeRegistro`, `origin/consultaeventoarreglado`, `origin/dev`, `origin/feature/casos_adrian`, `origin/feature/instituciones-patrocinios`, `origin/feature/persistencia`, `origin/feature/registros-joaquinb`, `origin/feature/swing-usuario-imagenes`, `origin/feature/tests-eventos-categorias`, `origin/lab2-Emmanuel`, `origin/lab2-cobertura`, `origin/lab2-emmanuel-tests`, `origin/lab2-swing-datos`, `origin/lab2-web-usuarios`, `origin/lab3-desarrollo`, `origin/lab3-emmanuel`, `origin/main`, `origin/swingEdicionEventos`.

| Commit | Fecha autor | Asunto | Archivos afectados |
| --- | --- | --- | --- |
| `e9181fe15e418d6ea0bd54255a5e1760e6b2a146` | 2026-11-16T19:56:02-03:00 | docs(lab3): auditar requisitos y condicionar integracion en dev | 1 |
| `860f71e85d62615fbdf5631a16187cd0346ec11d` | 2026-11-16T17:41:13-03:00 | fix(configuracion): exigir conexion externa y probar jar central | 4 |
| `1c995c56a0ebfec6fde90e6770222cec9aa5927a` | 2026-11-15T23:26:57-03:00 | fix(swing): persistir eventos y ediciones con imagen atomicamente | 5 |
| `29f1b518a0c4479f5087d2b2a3c1e660d8d4774a` | 2026-11-15T23:25:51-03:00 | fix(seguridad): exigir Actor en todas las escrituras protegidas | 36 |
| `31df84f64e847000ef1c6b8ec2ca46b4df47ca60` | 2026-11-15T20:08:34-03:00 | docs(lab3): registrar integracion selectiva de Emmanuel y Valentina | 2 |
| `45053f53b626b6d1b3462c4a758897ffb0806188` | 2026-11-15T20:00:31-03:00 | test(logica): integrar regresiones de eventos y categorias de Valentina | 1 |
| `1cd5f5a25084bcc3f4302f6aa555287e9787792d` | 2026-11-15T19:54:02-03:00 | feat(usuarios): integrar disponibilidad de Emmanuel en Portal HTTPS | 7 |
| `2b7075281030ac80159c85654065d2dc0326b07b` | 2026-11-15T19:24:11-03:00 | docs(lab3): registrar integracion y ejecucion remota verificable | 17 |
| `27694c5144c5c92bc371c9748960d77d7906a289` | 2026-11-15T18:18:35-03:00 | fix(seguridad): autorizar en Central y leer perfiles bloqueados | 8 |
| `3d3b6d04d4ead1aa97171b35305c0f683117098b` | 2026-11-15T17:45:00-03:00 | test(integracion): verificar Tomcat con Central HTTPS y sin JPA | 5 |
| `713e0f3212bbd960d863761dc96b9dfa9ac58f71` | 2026-11-15T17:35:51-03:00 | refactor(web): consumir Central exclusivamente mediante SOAP HTTPS | 63 |
| `72ed4d8a8967c083c99231da6512a06b14b8958f` | 2026-11-15T17:04:57-03:00 | feat(soap): publicar portal autorizado con contratos independientes | 47 |
| `b188188029946ba9f177d16bdea5a83673e7c79d` | 2026-11-15T00:26:38-03:00 | feat(logica): centralizar consultas y operaciones autorizadas del portal | 9 |
| `0e3ea839f9d798a949a6f36cab70526d7282753a` | 2026-11-15T00:09:46-03:00 | feat(instituciones): integrar alta atomica autorizada con imagen | 21 |
| `47fc75c7eb740b36b702e4cb14ac173253c7dfd2` | 2026-11-14T22:57:51-03:00 | feat(central): consolidar sesiones P03 y arranque SOAP sobre HTTPS | 19 |
| `343566ea280d5ccb23e554abf95d5ee4b71ade06` | 2026-11-14T22:36:37-03:00 | refactor(patrocinios): autorizar consultas y proteger codigos privados | 22 |
| `b96f5991230df41e425c7dc5ea6a5279aae9fcfa` | 2026-11-14T22:31:38-03:00 | fix(usuarios): hacer atomica y autorizada la modificacion de perfiles | 19 |
| `56ac1726d7ea7e63f854fb5e8c19bf5bd9bdff95` | 2026-11-14T21:32:28-03:00 | feat(usuarios): integrar alta atomica de usuarios con imagen | 4 |
| `7b27f8bd1deb23ea165f73ae5cd3015249095c63` | 2026-11-14T20:51:16-03:00 | refactor(patrocinios): centralizar autorización y escritura transaccional | 21 |
| `bc3c2f897b6f960d7660ff4b084e6c17464b116b` | 2026-11-14T20:37:23-03:00 | refactor(logica): centralizar autorización del alta de tipos de registro | 22 |
| `a1a15e78ef3dfed093e55827fbe5c5039a8f1836` | 2026-11-09T20:55:06-03:00 | chore(lab3): auditar avance e incorporar observaciones de defensa | 3 |
| `a29ee1fc9f2362e812ca939068a4fc205ca1985f` | 2026-11-06T21:50:32-03:00 | Documenta contratos SOAP y trabajo del equipo para Laboratorio 3 | 7 |
| `586eab42467bb18d9a3b6de870b92985efde3557` | 2026-11-06T21:41:17-03:00 | Implementa primer corte SOAP para consulta de eventos | 21 |
| `6bdfb1780fbf34cd9bbacf23ea01b9d917e20ee9` | 2026-11-05T21:07:41-03:00 | Prepara requisitos y roadmap del Laboratorio 3 | 3 |
| `5fb46db2743c10609c470c78b71ed8c210e8ba14` | 2026-11-03T21:16:06-03:00 | entrega final | 37 |
| `9ab14ccd08357a2a05c01978868a703352665ec7` | 2026-11-03T17:43:30-03:00 | Completa Laboratorio 2 con pruebas y documentación de diseño | 158 |
| `0284e7cc1240fc7e7f08d5454d70aea0bc56301b` | 2026-10-29T21:49:22-03:00 | Agrega pruebas JUnit y medicion de cobertura JaCoCo | 7 |
| `4880c2f05e48ed8a18d1faa1922dff1bbd7a36b6` | 2026-10-29T19:54:20-03:00 | Integra mejoras de Swing de Valentina conservando validaciones e imágenes | 0 |
| `9cfd301d75114fb92387a93bca58e186611256f8` | 2026-10-29T19:45:39-03:00 | Integra pruebas adicionales de Emmanuel | 0 |
| `9c8b0a3f479939f758a43152a5a22be0ddd6042c` | 2026-10-29T19:42:53-03:00 | Implementa mejoras de Swing e integración con web | 21 |
| `69883001d6633031185f63cd1b9064905a226419` | 2026-10-28T22:39:05-03:00 | Implementa etapas 1 a 3 del Laboratorio 2 | 31 |
| `489245c32d0051b09bae13274631d8d2818384de` | 2026-10-27T23:08:57-03:00 | Merge casos adrian | 0 |
| `062c1f35d42d81765322d3a2eee41b59e5392c36` | 2026-10-27T23:00:15-03:00 | Swing: el registro a edicion lista solo ediciones aceptadas | 1 |
| `19d4b2c20df8c10a40aa014a7c90a2a30e702898` | 2026-10-27T23:00:15-03:00 | Logica: motivos de error en altaEdicion y altaTipoRegistro; pruebas de estados y reglas de alta | 3 |
| `0de6958a270e71a57b91e1659b208e9105a72502` | 2026-10-26T21:48:30-03:00 | fix consulta edicion | 20 |
| `12bf4dd766aec57106a17f4f28caed7535630b4b` | 2026-10-26T08:27:10-03:00 | Merge joaquin-b-registros into dev | 0 |
| `46de4c3c1b916144700554e3e7834554d9df7a11` | 2026-10-26T08:12:29-03:00 | Merge pull request #35 from Joaquin571/lab2-swing-datos | 0 |
| `2524e695bfb7314ab65e408d7527f13abfa62da7` | 2026-10-26T08:11:24-03:00 | Pendiente swing | 1 |
| `8600de8f027936643b37392ec2145208b53ad2a5` | 2026-10-25T17:14:09-03:00 | Merge pull request #34 from Joaquin571/feature/eventos-web | 0 |
| `40d24be9af0a780cb0112eb1e4b0fb82b11d1aad` | 2026-10-25T17:13:40-03:00 | Merge pull request #33 from Joaquin571/lab2-emmanuel-entrega-final | 0 |
| `417c40121cbd8406f4adcac4722c84a66caac656` | 2026-10-25T17:13:00-03:00 | Merge pull request #32 from Joaquin571/lab2-swing-datos | 0 |
| `29a2e354d514b2d5e8dd2819cfe8be2da7e56b4b` | 2026-10-22T20:24:39-03:00 | lab2-swing-datos | 6 |
| `849cd773131a375ac3c03fcccfc290f041306c96` | 2026-10-21T19:22:56-03:00 | Logica: setters de DtAsistente y validaciones al modificar usuario | 2 |
| `c289aa6c284e171026494e3ad40996c21d428fbe` | 2026-10-21T19:22:56-03:00 | README: base de datos, credenciales y como correr Swing y la web | 1 |
| `fb02e131d1ce3c398b44934eda46e69b71474eea` | 2026-10-21T19:22:56-03:00 | Swing: alta de usuario con contraseña e imagen, y pantalla Aceptar/Rechazar Edicion | 3 |
| `f592ffb2f4913b6a147d1f60466799df34832115` | 2026-10-21T19:22:55-03:00 | Datos de prueba: ediciones en los tres estados, tipos, instituciones, patrocinio y registros | 3 |
| `3b91fb311133681fec04aa01a686a1a3d54f46a5` | 2026-10-19T22:03:15-03:00 | Merge pull request #31 from Joaquin571/lab2-web-institucion | 0 |
| `3b3eccbf841f3bc7f3b7d459ba634f53c8fdb0af` | 2026-10-19T22:02:49-03:00 | Web: alta de institucion | 3 |
| `5d8ad3510512e0008e3d8a91fb0358a2537bc8b4` | 2026-10-19T17:30:39-03:00 | Web: imagenes de usuario (alta y perfil) y modificar mis datos | 11 |
| `2221da8308dada6189d0e81ea1dd2e54211b9b7a` | 2026-10-19T17:19:21-03:00 | Merge pull request #30 from Joaquin571/lab2-web-usuarios | 0 |
| `47b3ccbcad188d294dc504e72fa511b58d9b078b` | 2026-10-19T17:18:19-03:00 | Logica: imagenes de usuario, evento, edicion e institucion | 14 |
| `5f452da2eb26265879b5aaf402f51e61369d0faf` | 2026-10-19T17:18:19-03:00 | Web: alta de usuario, listado y perfil | 13 |
| `c59f7faa5dbaf957ef4dcb484476511183caad99` | 2026-10-19T16:33:08-03:00 | Merge pull request #29 from Joaquin571/lab2-web-infraestructura | 0 |
| `d6c3acc5aa159982632874a76b9ea399ed1baf97` | 2026-10-19T16:31:49-03:00 | Web: infraestructura, login, cierre de sesion e inicio con categorias | 19 |
| `b9009ef3e77ab744699e4b4f5d8b17afcfa644bf` | 2026-10-19T07:28:57-03:00 | Merge pull request #28 from Joaquin571/lab2-logica-estado-edicion | 0 |
| `bf5ed78da2fc652bf2a325092cf1c9e920f0375f` | 2026-10-19T07:27:47-03:00 | Logica ediciones | 6 |
| `965bf0c6e1bd1903b562b95fcdd095559f18d89d` | 2026-10-19T07:13:28-03:00 | Merge pull request #27 from Joaquin571/lab2-logica-login | 0 |
| `48ffeaa294f3fcc0085d3df478199fddf429f12d` | 2026-10-19T07:11:59-03:00 | Logica: contraseña con hash, login y cambio de contraseña | 7 |
| `a4c6e32cfa4f2018cd2967673988b5730df858c7` | 2026-10-18T16:10:48-03:00 | Merge pull request #26 from Joaquin571/restructura-modulos | 0 |
| `58978569a754931f89448405f806e2542eae57f2` | 2026-10-18T16:08:05-03:00 | Reestructura en módulos Maven: logica, estacion-trabajo y webapp | 189 |
| `93dcc1ae9b72b639e40f0b32c987b7557fda2db8` | 2026-10-13T22:01:16-03:00 | Merge pull request #25 from Joaquin571/lab2-valentina | 0 |
| `9f002e328134814530772d5d2d90bf2172126f8e` | 2026-10-13T17:46:01-03:00 | Merge pull request #24 from Joaquin571/lab2-Emmanuel-Final | 0 |
| `4c2e4e9e821dcfb3c425e79fe8c3b6e854292de5` | 2026-10-12T17:48:22-03:00 | Resolución de conflictos en merge | 0 |
| `43968926ff7284b7ec95edc4d79331b9c31d4f88` | 2026-10-12T17:39:06-03:00 | Imagenes, correcciones en url, header, guard | 35 |
| `73b592f146fabe70fceca705301bb933e05f8f6d` | 2026-10-10T20:04:37-03:00 | Merge pull request #23 from Joaquin571/lab2-joaquin | 0 |
| `25ba4de612cec7fe158682906b7c29642c880351` | 2026-10-10T20:03:28-03:00 | fix login | 2 |
| `112871f0c3418ebb3582a5d0efd118d564b38292` | 2026-10-10T09:46:17-03:00 | Merge casos de usuario y login de Adrian | 0 |
| `4fd4108477e2b069a9d21730357f0497398aa24c` | 2026-10-10T09:39:59-03:00 | fix alta institución | 1 |
| `95d4e1cf4147ecc8cf8c1f03e7e48bb1edba0a1e` | 2026-10-10T09:36:21-03:00 | Merge pull request #22 from Joaquin571/lab2-valentina | 0 |
| `5f8e84320ac6f9a2434db65e2f44a8040d6a948b` | 2026-10-09T19:29:32-03:00 | Merge pull request #21 from Joaquin571/lab2-Emmanuel-Final | 0 |
| `4bd0ecab9acb1f7278dc4ece83df8a4d436eaa70` | 2026-10-09T19:29:12-03:00 | Merge branch 'dev' into lab2-Emmanuel-Final | 0 |
| `4d45f5b9ddebbceae70a11f7a55d21ad866477a3` | 2026-10-09T19:16:35-03:00 | Merge pull request #20 from Joaquin571/lab2-valentina | 0 |
| `8b9e4bef8450657e970ff33da92ca2b45c142e93` | 2026-10-08T20:27:43-03:00 | fix: validar registros gratuitos por patrocinio desde backend | 10 |
| `c9f9e084eafc21f7522ce7874b1e1e1fae05dad3` | 2026-10-07T20:43:07-03:00 | Merge pull request #19 from Joaquin571/lab2-Emmanuel | 0 |
| `f789eed23f5e31c13d5d784f023ae8589241aa28` | 2026-10-04T20:46:54-03:00 | consulta patrocinio | 2 |
| `5ae3d6e0556b93dfe2e04258d64476bf5675d08f` | 2026-10-04T14:51:17-03:00 | alta institucion y patrocinio | 9 |
| `08094599a7e1c688e411e660d99f002a59bef36a` | 2026-10-02T22:06:45-03:00 | Estructura lab2 | 21 |
| `e49e03b593807ccda5b8a8c367ba64a609cfcf68` | 2026-10-02T21:39:05-03:00 | Estructura lab 2 | 2 |
| `3d4bb018be4c2737d587ace4cfc7a8b2d84e0b5d` | 2026-09-13T20:55:01-03:00 | Agrega vista principal | 1 |
| `f942eaed88ec5ca96a5edbe4a7fa866433a91c0e` | 2026-09-13T20:39:44-03:00 | Limpieza de sistema e interfaces | 3 |
| `76e5820d3aae8a1eaf617ac6f6c61b0b9ee566d1` | 2026-09-13T20:23:15-03:00 | Delete contexto_registro.txt | 1 |
| `ca7d42ceaa948fe3d0088fbd276617b93126f383` | 2026-09-13T18:03:39-03:00 | ajustes | 7 |
| `a62fa764d48abb9b84b0234db3526fd9f957ea66` | 2026-09-13T17:18:40-03:00 | Merge pull request #18 from Joaquin571/feature/consulta-edicion-evento-completa | 0 |
| `87145da88859884e09b78bb4674e5c08e345abfc` | 2026-09-13T17:16:06-03:00 | ajustes al cambio y limpieza | 3 |
| `9273ef30ea08f332bd1270f73f4aecb65dab8b2e` | 2026-09-13T16:15:28-03:00 | Ajustes clases, saco setters | 7 |
| `754003b8c8febec53a86b686a58283597a18f08b` | 2026-09-13T14:32:02-03:00 | Bloquea campo correo usuario | 1 |
| `9defc881bb2231504e70980e19b1da52e1cd9ab3` | 2026-09-13T14:19:18-03:00 | Agrega navegación a ediciones o registros desde consulta de usuario | 3 |
| `af4f1a09eb6836363f9ab30c38d01e3ef3f9d33f` | 2026-09-13T13:35:16-03:00 | Agrega actualización de lista instituciones | 2 |
| `79df8e29ea2059c87d05ba254c4256560d1908b1` | 2026-09-13T12:22:09-03:00 | Merge | 0 |
| `004df6a176835f861b2ba0f77ac8f09ee2eab9b5` | 2026-09-13T12:05:06-03:00 | Merge pull request #17 from Joaquin571/revision7 | 0 |
| `6417c7e5cf86ba8cd3b6e5bbc0c684d27aacdfe5` | 2026-09-12T15:50:28-03:00 | Merge pull request #16 from Joaquin571/consultaeventoarreglado | 0 |
| `7be6c79718873c1378cd016a812bf38730c349f0` | 2026-09-12T14:24:46-03:00 | Merge | 0 |
| `006baa910787ec1186a4cde802a4ce4bbcd6f8eb` | 2026-09-12T14:16:14-03:00 | Arregla alta tipo registro | 10 |
| `21d69b5e364bfdee450a6c50274b53c3629928e0` | 2026-09-10T22:21:57-03:00 | Errores de swing en alta tipo registro | 5 |
| `cb242c5df8f8dc77f202aee94d21d7dd90a8233b` | 2026-09-10T21:42:29-03:00 | Merge pull request #14 from Joaquin571/Revision4 | 0 |
| `1c9bfccac067c77104b4ff1ab5ca95f8e5aa5be9` | 2026-09-10T21:42:08-03:00 | Resolver conflictos con dev | 0 |
| `7871b5aa7ae5b279f14ee555ba51c24b0b5df071` | 2026-09-08T16:21:51-03:00 | Merge pull request #15 from Joaquin571/feature/persistencia-jpa | 0 |
| `6bb0eed70e1e2b14673a0d17edf5bda42a4d211d` | 2026-09-08T16:03:14-03:00 | JPA. entity manager y pruebas | 11 |
| `fa47de936c17163f77af8fd6a3e604b09c8798f8` | 2026-08-31T23:26:52-03:00 | Merge pull request #13 from Joaquin571/feature/correcciones-logica | 0 |
| `090da2c801547c5b37a4819db16e82d5f335214e` | 2026-08-31T23:26:06-03:00 | Elimina archivo accidental | 1 |
| `21fd0d073c76325d72879d208998d8ed1eef26e7` | 2026-08-31T23:25:16-03:00 | Corrige relaciones y logica de ediciones registros y patrocinios | 10 |
| `af4ad57c9251cbde352f2726555da00fa42b22da` | 2026-08-31T23:00:40-03:00 | Merge pull request #12 from Joaquin571/feature/jpa-entidades | 0 |
| `3fac20a4902f6dc5aaf605bcddeb4b2469ba32d2` | 2026-08-31T22:59:34-03:00 | JPA en entidades y arreglos con func toString | 15 |
| `d5293a00c74fc1898d88f678541d93d3017fdbeb` | 2026-08-31T22:03:35-03:00 | Ignora archivos generados de Maven | 49 |
| `21de4213b89d9931406285aaba3c2653334fe710` | 2026-08-31T21:59:43-03:00 | Merge pull request #11 from Joaquin571/feature/persistencia | 0 |
| `3a8c536619d75bb86eda1e159de449c0b372d6e0` | 2026-08-31T21:57:37-03:00 | Integra dev en feature persistencia | 0 |
| `1103795cdd7100087ac977f98b03759a52f73d5d` | 2026-08-31T21:48:47-03:00 | Merge pull request #10 from Joaquin571/ConsultaeventoYAltacat | 0 |
| `87ecc0b7050697385a7655cb49f03dfc963fbc6c` | 2026-08-31T21:47:58-03:00 | Arreglo conflicto evento y edicion | 0 |
| `928057b3adf79842b5a82e1b535828aa820529e5` | 2026-08-31T21:42:20-03:00 | Integra consulta evento, categorias y registros | 8 |
| `591bc029eb0f4b13946a0334ef1aa6999cbc7ed9` | 2026-08-31T17:40:12-03:00 | Integra ConsultaeventoYAltacat con dev | 0 |
| `4ca8ff4f82378be47399880c1ed4a503ef8099da` | 2026-08-29T21:08:55-03:00 | Merge pull request #9 from Joaquin571/Registros_Y_Eventos | 0 |
| `8b5b34fb2efd6765304d8b37d8ca4d6672a1c380` | 2026-08-29T21:08:15-03:00 | Integra Registros y Eventos con dev | 0 |
| `a786c0276a1ff8e75a8c7379e3eb4361122ea9df` | 2026-08-29T20:58:46-03:00 | Merge pull request #8 from Joaquin571/revisión | 0 |
| `8945f8aa769c09ea0864d245e9a52e1b021fec94` | 2026-08-29T20:47:40-03:00 | agrega entity edicion, persistence xml | 137 |
| `a3cad5590d3cdf391630c39e61c1142a490c6c55` | 2026-08-29T09:18:47-03:00 | Merge pull request #7 from Joaquin571/altaEvento | 0 |
| `f44eb0aad8f97343c5f1f4196a489320f6dc043f` | 2026-08-29T09:15:36-03:00 | Integra altaEvento con dev | 0 |
| `b756b392ed37697b006a71043d1afffce42e4c33` | 2026-08-28T22:32:59-03:00 | Merge pull request #6 from Joaquin571/feature/clases_faltantes | 0 |
| `1b78de8523594bc0abd123814e8e856a8b443ecb` | 2026-08-28T22:32:09-03:00 | Clases y DT faltantes | 9 |
| `9ef6cda017bf0b52b02ccb3810abf2d9b65cf1d8` | 2026-08-28T22:09:07-03:00 | principal | 1 |
| `8c0ac1988c6b49391aa68baf020782dd9f21828b` | 2026-08-28T21:57:32-03:00 | Merge pull request #5 from Joaquin571/feature/Consulta_Usuario | 0 |
| `fa9ea8738872f86e3f9288535f438334a0a0511d` | 2026-08-28T21:57:06-03:00 | Resuelve conflictos de Consulta Usuario con dev | 0 |
| `551c5d5895c091361a9c4398bfedb4b4893288db` | 2026-08-28T21:40:56-03:00 | Ajuste consulta | 2 |
| `9f2bffd1148f3ff2c4e167e124d7d8fec30b3f84` | 2026-08-28T21:29:20-03:00 | Merge pull request #4 from Joaquin571/feature/patrocinio | 0 |
| `9b047b76659416ec9c24647f56a07b12de7f8030` | 2026-08-28T20:54:06-03:00 | agrega consulta de patrocinio, alta de patrocinio | 7 |
| `32eebb525676f71d4e12745bc4c178708c85c8e6` | 2026-08-26T22:51:35-03:00 | agrega clase, dt, manejador, funciones en Isistema y sistema de Patrocinio | 8 |
| `b5619483ae2db6685c7592bf0a66d3a0823fe172` | 2026-08-26T22:14:30-03:00 | Merge pull request #3 from Joaquin571/feature/alta-institucion | 0 |
| `bbe797efb93b55270a794a9c273f9967cb3147ac` | 2026-08-26T21:59:37-03:00 | agrega conexion de instituciones con modificar | 13 |
| `22f17659077e540b0da2d77d94fea51721938cac` | 2026-08-25T15:55:11-03:00 | Resolver conflictos entre alta y modificar usuario /2 | 5 |
| `2100a92ded6e674e68c06c8217003212b5f3ed5a` | 2026-08-25T14:10:24-03:00 | Merge pull request #2 from Joaquin571/feature/modificar-usuario | 0 |
| `af8b3ee0e64980b298d7b956f145967b5e41c5ae` | 2026-08-25T13:49:10-03:00 | Resolver conflictos entre alta y modificar usuario | 0 |
| `f59514c2a9752870efe9053cce651c6bf9be0d06` | 2026-08-25T13:20:53-03:00 | Merge pull request #1 from Joaquin571/feature/alta-usuario | 0 |
| `bef8e92ab7e5b62cad0ab02cf3c7cb3a5112849f` | 2026-08-25T13:16:28-03:00 | Implementar modificar usuario | 9 |
| `f64411fe3d10105c25181b621cd26842d1b93852` | 2026-08-23T21:15:09-03:00 | Añade manejadores de usuarios, institucion y eventos(en proceso), añade interfaz ISistema, añade sistema. | 27 |
| `3bf4399123b0e377ddb0762f354afa2384ca21c8` | 2026-08-21T23:39:22-03:00 | añade estructura principal swing, estructura del proyecto, clase de usuario, manejador usuario | 25 |
| `1b64ac4e6016fe10a23682ede978d8f4e9279703` | 2026-08-17T18:22:26-03:00 | agrega DSS alta de categoria | 1 |
| `b39034c871e3c2a24cbaa9c178a6b1a1c2bf1cc9` | 2026-08-17T17:24:21-03:00 | Modificación de DSS, se quitan retornos del sistema | 2 |
| `880a127c7ed4fb54f65ade8e2d03ad2ff40ce882` | 2026-08-15T17:57:15-03:00 | docs: agrega DSS de Alta de patrocinio y modifica registro a edicion de evento | 3 |
| `e271ea8aafe1f7e018c01589f9d5cd3adfcf65c9` | 2026-08-15T16:21:56-03:00 | docs: agrega DSS de registro a edicion de evento | 1 |
| `075166cd8ab72b682dfaf7f657cc82b4a5a0342a` | 2026-08-04T23:08:03-03:00 | Inicio del proyecto - Tarea 1 | 7 |

<details>
<summary>Listado completo de rutas historicas afectadas</summary>

- `"docs/dss/Dise\303\261o LAB1-Alta de Categoria.drawio.png"`
- `"docs/dss/Dise\303\261o LAB1-Alta de Patrocinio.drawio.png"`
- `"docs/dss/Dise\303\261o LAB1-Registro a Edici\303\263n de Evento.drawio (1).png"`
- `"docs/dss/Dise\303\261o LAB1-Registro a Edici\303\263n de Evento.drawio.png"`
- `.gitignore`
- `.idea/.gitignore`
- `.idea/compiler.xml`
- `.idea/dataSources.xml`
- `.idea/encodings.xml`
- `.idea/jarRepositories.xml`
- `.idea/misc.xml`
- `.idea/modules.xml`
- `.idea/uiDesigner.xml`
- `.idea/vcs.xml`
- `.smarttomcat/webapp/conf/catalina.properties`
- `.smarttomcat/webapp/conf/context.xml`
- `.smarttomcat/webapp/conf/jaspic-providers.xml`
- `.smarttomcat/webapp/conf/jaspic-providers.xsd`
- `.smarttomcat/webapp/conf/logging.properties`
- `.smarttomcat/webapp/conf/server.xml`
- `.smarttomcat/webapp/conf/tomcat-users.xml`
- `.smarttomcat/webapp/conf/tomcat-users.xsd`
- `.smarttomcat/webapp/conf/web.xml`
- `AGENTS.md`
- `Laboratorio 2 - 2026 (1).pdf`
- `PA_2026.iml`
- `README.md`
- `contexto_registro.txt`
- `contratos/pom.xml`
- `contratos/src/main/java/contratos/ConversorDatos.java`
- `contratos/src/main/java/contratos/ServicioPortal.java`
- `contratos/src/main/java/contratos/dto/DtAsistente.java`
- `contratos/src/main/java/contratos/dto/DtCategoria.java`
- `contratos/src/main/java/contratos/dto/DtConsultaPatrocinios.java`
- `contratos/src/main/java/contratos/dto/DtConsultaRegistros.java`
- `contratos/src/main/java/contratos/dto/DtDetalleEdicion.java`
- `contratos/src/main/java/contratos/dto/DtEdicion.java`
- `contratos/src/main/java/contratos/dto/DtEvento.java`
- `contratos/src/main/java/contratos/dto/DtFormularioRegistro.java`
- `contratos/src/main/java/contratos/dto/DtImagen.java`
- `contratos/src/main/java/contratos/dto/DtInstitucion.java`
- `contratos/src/main/java/contratos/dto/DtOpcionesAltaPatrocinio.java`
- `contratos/src/main/java/contratos/dto/DtOrganizador.java`
- `contratos/src/main/java/contratos/dto/DtPatrocinio.java`
- `contratos/src/main/java/contratos/dto/DtPerfilUsuario.java`
- `contratos/src/main/java/contratos/dto/DtRegistro.java`
- `contratos/src/main/java/contratos/dto/DtSeleccionEdicion.java`
- `contratos/src/main/java/contratos/dto/DtTipoRegistro.java`
- `contratos/src/main/java/contratos/dto/DtUsuario.java`
- `contratos/src/main/java/contratos/dto/EntradaUsuario.java`
- `contratos/src/main/java/contratos/dto/EstadoEdicion.java`
- `contratos/src/main/java/contratos/dto/FechaXml.java`
- `contratos/src/main/java/contratos/dto/NivelPatrocinio.java`
- `contratos/src/main/java/contratos/dto/TipoEntidadImagen.java`
- `docs/H13-H14-unicidad-concurrencia.md`
- `docs/auditoria-H07-H08-seguridad.md`
- `docs/correcciones-S01-S03.md`
- `docs/cumplimiento-7-3.md`
- `docs/diseno/DECISIONES.md`
- `docs/diseno/README.md`
- `docs/diseno/fuentes/01-modelo-dominio.puml`
- `docs/diseno/fuentes/02-mapeo-jpa.puml`
- `docs/diseno/fuentes/03-dss-usuarios.puml`
- `docs/diseno/fuentes/04-dss-eventos-ediciones.puml`
- `docs/diseno/fuentes/05-dss-registros-patrocinios.puml`
- `docs/diseno/fuentes/06-secuencia-usuarios.puml`
- `docs/diseno/fuentes/07-secuencia-perfil-imagen.puml`
- `docs/diseno/fuentes/08-secuencia-evento-edicion.puml`
- `docs/diseno/fuentes/09-secuencia-estados-swing.puml`
- `docs/diseno/fuentes/10-secuencia-registro.puml`
- `docs/diseno/fuentes/11-secuencia-patrocinio.puml`
- `docs/diseno/fuentes/12-arquitectura.puml`
- `docs/diseno/herramientas.json`
- `docs/diseno/imagenes/01-modelo-dominio.png`
- `docs/diseno/imagenes/02-mapeo-jpa.png`
- `docs/diseno/imagenes/03-dss-usuarios.png`
- `docs/diseno/imagenes/04-dss-eventos-ediciones.png`
- `docs/diseno/imagenes/05-dss-registros-patrocinios.png`
- `docs/diseno/imagenes/06-secuencia-usuarios.png`
- `docs/diseno/imagenes/07-secuencia-perfil-imagen.png`
- `docs/diseno/imagenes/08-secuencia-evento-edicion.png`
- `docs/diseno/imagenes/09-secuencia-estados-swing.png`
- `docs/diseno/imagenes/10-secuencia-registro.png`
- `docs/diseno/imagenes/11-secuencia-patrocinio.png`
- `docs/diseno/imagenes/12-arquitectura.png`
- `docs/diseno/renderizar.ps1`
- `docs/lab3/ARRANQUE-CENTRAL-HTTPS.md`
- `docs/lab3/AUDITORIA-2026-11-09.md`
- `docs/lab3/AUDITORIA-LAB3-DEV-2026-11-15.md`
- `docs/lab3/AUDITORIA-SERVLETS-ARQUITECTURA.md`
- `docs/lab3/AUDITORIA-SERVLETS-CAPAS.md`
- `docs/lab3/CONTRATOS-EQUIPO.md`
- `docs/lab3/CORTE-SOAP.md`
- `docs/lab3/EQUIPO/GUIA-GIT.md`
- `docs/lab3/EQUIPO/INTEGRANTE-1.md`
- `docs/lab3/EQUIPO/INTEGRANTE-2.md`
- `docs/lab3/EQUIPO/INTEGRANTE-3.md`
- `docs/lab3/EQUIPO/INTEGRANTE-4.md`
- `docs/lab3/EQUIPO/INTEGRANTE-5.md`
- `docs/lab3/ESTADO-ACTUAL.md`
- `docs/lab3/INTEGRACION-2026-11-15.md`
- `docs/lab3/INTEGRACION-EQUIPO-2026-11-15.md`
- `docs/lab3/P03-SESIONES-SOAP.md`
- `docs/lab3/PRUEBA-SOAP-HTTPS.md`
- `docs/lab3/REFERENCIA_WEBSERVICES.md`
- `docs/lab3/REQUISITOS.md`
- `docs/lab3/ROADMAP.md`
- `docs/migraciones/H13-procedimiento.md`
- `docs/migraciones/H13-unicidad-ci.sql`
- `estacion-trabajo/pom.xml`
- `estacion-trabajo/src/main/java/swing/AceptarRechazarEdicionPanel.java`
- `estacion-trabajo/src/main/java/swing/AltaCategoriaPanel.java`
- `estacion-trabajo/src/main/java/swing/AltaEdicionEventoPanel.java`
- `estacion-trabajo/src/main/java/swing/AltaEventoPanel.java`
- `estacion-trabajo/src/main/java/swing/AltaInstitucionPanel.form`
- `estacion-trabajo/src/main/java/swing/AltaInstitucionPanel.java`
- `estacion-trabajo/src/main/java/swing/AltaPatrocinioPanel.form`
- `estacion-trabajo/src/main/java/swing/AltaPatrocinioPanel.java`
- `estacion-trabajo/src/main/java/swing/AltaTipoRegistro.form`
- `estacion-trabajo/src/main/java/swing/AltaTipoRegistro.java`
- `estacion-trabajo/src/main/java/swing/AltaUsuarioPanel.java`
- `estacion-trabajo/src/main/java/swing/Apariencia.java`
- `estacion-trabajo/src/main/java/swing/ConsultaEdicionEventoPanel.java`
- `estacion-trabajo/src/main/java/swing/ConsultaEventoPanel.java`
- `estacion-trabajo/src/main/java/swing/ConsultaPatrocinioPanel.form`
- `estacion-trabajo/src/main/java/swing/ConsultaPatrocinioPanel.java`
- `estacion-trabajo/src/main/java/swing/ConsultaRegistroPanel.java`
- `estacion-trabajo/src/main/java/swing/ConsultaTipoRegistroPanel.java`
- `estacion-trabajo/src/main/java/swing/ConsultaUsuarioPanel.java`
- `estacion-trabajo/src/main/java/swing/FormularioSwing.java`
- `estacion-trabajo/src/main/java/swing/ImagenSwing.java`
- `estacion-trabajo/src/main/java/swing/ModificarUsuarioPanel.form`
- `estacion-trabajo/src/main/java/swing/ModificarUsuarioPanel.java`
- `estacion-trabajo/src/main/java/swing/RegistroEdicionEventoPanel.java`
- `estacion-trabajo/src/main/java/swing/principal.java`
- `estacion-trabajo/src/test/java/swing/AltaEventoEdicionAtomicaTest.java`
- `estacion-trabajo/src/test/java/swing/AltaPatrocinioPanelActorTest.java`
- `estacion-trabajo/src/test/manual/README.md`
- `estacion-trabajo/src/test/manual/VerificarArranque.java`
- `estacion-trabajo/src/test/manual/VerificarBloqueD.java`
- `estacion-trabajo/src/test/manual/VerificarImagenesSegurasSwing.java`
- `estacion-trabajo/src/test/manual/VerificarSwing.java`
- `how --stat cb4d004`
- `logica/pom.xml`
- `logica/src/main/java/clases/Asistente.java`
- `logica/src/main/java/clases/Categoria.java`
- `logica/src/main/java/clases/ConImagen.java`
- `logica/src/main/java/clases/Edicion.java`
- `logica/src/main/java/clases/Evento.java`
- `logica/src/main/java/clases/Imagen.java`
- `logica/src/main/java/clases/Institucion.java`
- `logica/src/main/java/clases/NivelPatrocinio.java`
- `logica/src/main/java/clases/Organizador.java`
- `logica/src/main/java/clases/Patrocinio.java`
- `logica/src/main/java/clases/Registro.java`
- `logica/src/main/java/clases/TipoRegistro.java`
- `logica/src/main/java/clases/Usuario.java`
- `logica/src/main/java/datatypes/Actor.java`
- `logica/src/main/java/datatypes/DtAltaTipoRegistro.java`
- `logica/src/main/java/datatypes/DtAsistente.java`
- `logica/src/main/java/datatypes/DtCategoria.java`
- `logica/src/main/java/datatypes/DtConsultaPatrocinios.java`
- `logica/src/main/java/datatypes/DtConsultaRegistros.java`
- `logica/src/main/java/datatypes/DtDetalleEdicion.java`
- `logica/src/main/java/datatypes/DtEdicion.java`
- `logica/src/main/java/datatypes/DtEvento.java`
- `logica/src/main/java/datatypes/DtFormularioRegistro.java`
- `logica/src/main/java/datatypes/DtImagen.java`
- `logica/src/main/java/datatypes/DtInstitucion.java`
- `logica/src/main/java/datatypes/DtOpcionesAltaPatrocinio.java`
- `logica/src/main/java/datatypes/DtOrganizador.java`
- `logica/src/main/java/datatypes/DtPatrocinio.java`
- `logica/src/main/java/datatypes/DtPerfilUsuario.java`
- `logica/src/main/java/datatypes/DtRegistro.java`
- `logica/src/main/java/datatypes/DtSeleccionEdicion.java`
- `logica/src/main/java/datatypes/DtTipoRegistro.java`
- `logica/src/main/java/datatypes/DtUsuario.java`
- `logica/src/main/java/datatypes/EstadoEdicion.java`
- `logica/src/main/java/datatypes/TipoEntidadImagen.java`
- `logica/src/main/java/datos/CargarDatosPrueba.java`
- `logica/src/main/java/excepciones/AccesoDenegadoException.java`
- `logica/src/main/java/excepciones/NoEncontradoException.java`
- `logica/src/main/java/implementacion/Fabrica.java`
- `logica/src/main/java/implementacion/Sistema.java`
- `logica/src/main/java/implementacion/ValidadorImagen.java`
- `logica/src/main/java/interfaces/ISistema.java`
- `logica/src/main/java/manejadores/ConflictosPersistencia.java`
- `logica/src/main/java/manejadores/ManejadorEventos.java`
- `logica/src/main/java/manejadores/ManejadorImagenes.java`
- `logica/src/main/java/manejadores/ManejadorInstituciones.java`
- `logica/src/main/java/manejadores/ManejadorPatrocinios.java`
- `logica/src/main/java/manejadores/ManejadorRegistros.java`
- `logica/src/main/java/manejadores/ManejadorUsuarios.java`
- `logica/src/main/java/persistencia/BaseDeDatos.java`
- `logica/src/main/java/seguridad/HashContrasenia.java`
- `logica/src/main/resources/META-INF/persistence.xml`
- `logica/src/test/java/implementacion/AltaUsuarioConImagenTest.java`
- `logica/src/test/java/implementacion/AsociacionImagenConcurrenciaTest.java`
- `logica/src/test/java/implementacion/ConcurrenciaEscritoresRelacionadosTest.java`
- `logica/src/test/java/implementacion/ConsultaPatrociniosVisibilidadTest.java`
- `logica/src/test/java/implementacion/ConsultaTipoRegistroTest.java`
- `logica/src/test/java/implementacion/ConsultasSistemaTest.java`
- `logica/src/test/java/implementacion/DecodificacionImagenTest.java`
- `logica/src/test/java/implementacion/EscriturasActorTest.java`
- `logica/src/test/java/implementacion/EstadoEdicionConcurrenciaTest.java`
- `logica/src/test/java/implementacion/EstadoEdicionTest.java`
- `logica/src/test/java/implementacion/EtapaUnoTest.java`
- `logica/src/test/java/implementacion/EventosYCategoriasTest.java`
- `logica/src/test/java/implementacion/ImagenesTest.java`
- `logica/src/test/java/implementacion/InstitucionConImagenTest.java`
- `logica/src/test/java/implementacion/LimitesTextoTest.java`
- `logica/src/test/java/implementacion/LoginTest.java`
- `logica/src/test/java/implementacion/ModificacionPerfilAtomicaTest.java`
- `logica/src/test/java/implementacion/ModificacionPerfilConImagenTest.java`
- `logica/src/test/java/implementacion/PatrocinioTest.java`
- `logica/src/test/java/implementacion/PersistenciaConcurrenteTest.java`
- `logica/src/test/java/implementacion/PortalAutorizacionTest.java`
- `logica/src/test/java/implementacion/RegistroTest.java`
- `logica/src/test/java/implementacion/ReglasAltaTest.java`
- `logica/src/test/java/implementacion/SingletonConcurrenteTest.java`
- `logica/src/test/java/implementacion/UnicidadConcurrenciaTest.java`
- `logica/src/test/java/implementacion/UsuarioTest.java`
- `logica/src/test/java/implementacion/ValidacionesAdicionalesTest.java`
- `logica/src/test/java/implementacion/ValidacionesPersistenciaTest.java`
- `logica/src/test/java/implementacion/ValidadorImagenTest.java`
- `logica/src/test/java/manejadores/AltaEventoEdicionRollbackImagenTest.java`
- `logica/src/test/java/manejadores/ConflictosPersistenciaTest.java`
- `logica/src/test/java/persistencia/ConfiguracionBaseTest.java`
- `logica/src/test/java/seguridad/HashContraseniaTest.java`
- `logica/src/test/java/soporte/ImagenesPrueba.java`
- `logica/src/test/java/soporte/ProteccionBasePruebas.java`
- `logica/src/test/java/soporte/ProteccionBasePruebasTest.java`
- `logica/src/test/java/soporte/PruebaConBaseSegura.java`
- `logica/src/test/manual/AuditarFronteraLogica.java`
- `pom.xml`
- `proyecto_completo.txt`
- `servidor-central/pom.xml`
- `servidor-central/src/main/java/central/ConfiguracionCentral.java`
- `servidor-central/src/main/java/central/Main.java`
- `servidor-central/src/main/java/central/ServidorHttpsCentral.java`
- `servidor-central/src/main/java/central/seguridad/SesionesRemotas.java`
- `servidor-central/src/main/java/central/soap/ConsultaException.java`
- `servidor-central/src/main/java/central/soap/CredencialesSoap.java`
- `servidor-central/src/main/java/central/soap/EventoSoap.java`
- `servidor-central/src/main/java/central/soap/EventosPublicador.java`
- `servidor-central/src/main/java/central/soap/MapeadorPortal.java`
- `servidor-central/src/main/java/central/soap/OperacionFault.java`
- `servidor-central/src/main/java/central/soap/OperacionSoapException.java`
- `servidor-central/src/main/java/central/soap/PortalPublicador.java`
- `servidor-central/src/main/java/central/soap/SesionSoapException.java`
- `servidor-central/src/main/java/central/soap/SesionesPublicador.java`
- `servidor-central/src/main/java/central/soap/UsuarioSesionSoap.java`
- `servidor-central/src/main/resources/central.properties.example`
- `servidor-central/src/test/java/central/ConfiguracionCentralTest.java`
- `servidor-central/src/test/java/central/EventosPublicadorTest.java`
- `servidor-central/src/test/java/central/seguridad/SesionesRemotasTest.java`
- `src/Main.java`
- `src/clases/Asistente.java`
- `src/clases/Categoria.java`
- `src/clases/Edicion.java`
- `src/clases/Evento.java`
- `src/clases/Institucion.java`
- `src/clases/NivelPatrocinio.java`
- `src/clases/Organizador.java`
- `src/clases/Patrocinio.java`
- `src/clases/Registro.java`
- `src/clases/TipoRegistro.java`
- `src/clases/Usuario.java`
- `src/clases/categoria.java`
- `src/clases/edicion.java`
- `src/clases/evento.java`
- `src/clases/institucion.java`
- `src/clases/patrocinio.java`
- `src/clases/registro.java`
- `src/clases/tipoRegistro.java`
- `src/datatypes/DtAsistente.java`
- `src/datatypes/DtCategoria.java`
- `src/datatypes/DtEdicion.java`
- `src/datatypes/DtEvento.java`
- `src/datatypes/DtInstitucion.java`
- `src/datatypes/DtOrganizador.java`
- `src/datatypes/DtPatrocinio.java`
- `src/datatypes/DtRegistro.java`
- `src/datatypes/DtTipoRegistro.java`
- `src/datatypes/DtUsuario.java`
- `src/implementacion/Fabrica.java`
- `src/implementacion/Sistema.java`
- `src/interfaces/ISistema.java`
- `src/main/java/clases/Asistente.java`
- `src/main/java/clases/Categoria.java`
- `src/main/java/clases/Edicion.java`
- `src/main/java/clases/Evento.java`
- `src/main/java/clases/Institucion.java`
- `src/main/java/clases/NivelPatrocinio.java`
- `src/main/java/clases/Organizador.java`
- `src/main/java/clases/Patrocinio.java`
- `src/main/java/clases/Registro.java`
- `src/main/java/clases/TipoRegistro.java`
- `src/main/java/clases/Usuario.java`
- `src/main/java/datatypes/DtAltaTipoRegistro.java`
- `src/main/java/datatypes/DtAsistente.java`
- `src/main/java/datatypes/DtCategoria.java`
- `src/main/java/datatypes/DtEdicion.java`
- `src/main/java/datatypes/DtEvento.java`
- `src/main/java/datatypes/DtInstitucion.java`
- `src/main/java/datatypes/DtOrganizador.java`
- `src/main/java/datatypes/DtPatrocinio.java`
- `src/main/java/datatypes/DtRegistro.java`
- `src/main/java/datatypes/DtTipoRegistro.java`
- `src/main/java/datatypes/DtUsuario.java`
- `src/main/java/implementacion/Fabrica.java`
- `src/main/java/implementacion/Sistema.java`
- `src/main/java/interfaces/ISistema.java`
- `src/main/java/manejadores/ManejadorEventos.java`
- `src/main/java/manejadores/ManejadorInstituciones.java`
- `src/main/java/manejadores/ManejadorPatrocinios.java`
- `src/main/java/manejadores/ManejadorRegistros.java`
- `src/main/java/manejadores/ManejadorUsuarios.java`
- `src/main/java/persistencia/BaseDeDatos.java`
- `src/main/java/swing/AltaCategoriaPanel.java`
- `src/main/java/swing/AltaEdicionEventoPanel.java`
- `src/main/java/swing/AltaEventoPanel.java`
- `src/main/java/swing/AltaInstitucionPanel.form`
- `src/main/java/swing/AltaInstitucionPanel.java`
- `src/main/java/swing/AltaPatrocinioPanel.form`
- `src/main/java/swing/AltaPatrocinioPanel.java`
- `src/main/java/swing/AltaTipoRegistro.form`
- `src/main/java/swing/AltaTipoRegistro.java`
- `src/main/java/swing/AltaUsuarioPanel.java`
- `src/main/java/swing/Apariencia.java`
- `src/main/java/swing/ConsultaEdicionEventoPanel.java`
- `src/main/java/swing/ConsultaEventoPanel.java`
- `src/main/java/swing/ConsultaPatrocinioPanel.form`
- `src/main/java/swing/ConsultaPatrocinioPanel.java`
- `src/main/java/swing/ConsultaRegistroPanel.java`
- `src/main/java/swing/ConsultaTipoRegistroPanel.java`
- `src/main/java/swing/ConsultaUsuarioPanel.java`
- `src/main/java/swing/ModificarUsuarioPanel.form`
- `src/main/java/swing/ModificarUsuarioPanel.java`
- `src/main/java/swing/RegistroEdicionEventoPanel.java`
- `src/main/java/swing/principal.java`
- `src/main/resources/META-INF/persistence.xml`
- `src/main/webapp.zip`
- `src/main/webapp/css/styles.css`
- `src/main/webapp/img/antel.png`
- `src/main/webapp/img/default-avatar.png`
- `src/main/webapp/img/utec.png`
- `src/main/webapp/index.html`
- `src/main/webapp/js/app.js`
- `src/main/webapp/js/consultaUsuario.js`
- `src/main/webapp/js/datos.js`
- `src/main/webapp/js/edicion.js`
- `src/main/webapp/js/evento.js`
- `src/main/webapp/js/guard.js`
- `src/main/webapp/js/header.js`
- `src/main/webapp/js/institucion.js`
- `src/main/webapp/js/login.js`
- `src/main/webapp/js/modificar-usuario.js`
- `src/main/webapp/js/patrocinio.js`
- `src/main/webapp/js/registro.js`
- `src/main/webapp/js/selects.js`
- `src/main/webapp/js/tipoRegistro.js`
- `src/main/webapp/js/usuario.js`
- `src/main/webapp/pages/alta-edicion.html`
- `src/main/webapp/pages/alta-evento.html`
- `src/main/webapp/pages/alta-institucion.html`
- `src/main/webapp/pages/alta-patrocinio.html`
- `src/main/webapp/pages/alta-tipo-registro.html`
- `src/main/webapp/pages/alta-usuario.html`
- `src/main/webapp/pages/consulta-patrocinio.html`
- `src/main/webapp/pages/consulta-registro.html`
- `src/main/webapp/pages/edicion.html`
- `src/main/webapp/pages/evento.html`
- `src/main/webapp/pages/login.html`
- `src/main/webapp/pages/logout.html`
- `src/main/webapp/pages/mis-registros.html`
- `src/main/webapp/pages/modificar-usuario.html`
- `src/main/webapp/pages/patrocinio.html`
- `src/main/webapp/pages/perfil.html`
- `src/main/webapp/pages/registro-edicion.html`
- `src/main/webapp/pages/tipo-registro.html`
- `src/main/webapp/pages/usuario.html`
- `src/main/webapp/pages/usuarios.html`
- `src/main/webapp/vendor/bootstrap/css/bootstrap.min.css`
- `src/main/webapp/vendor/bootstrap/js/bootstrap.bundle.min.js`
- `src/manejadores/ManejadorEventos.java`
- `src/manejadores/ManejadorInstituciones.java`
- `src/manejadores/ManejadorPatrocinios.java`
- `src/manejadores/ManejadorUsuarios.java`
- `src/swing/AltaEventoPanel.java`
- `src/swing/AltaInstitucionPanel.form`
- `src/swing/AltaInstitucionPanel.java`
- `src/swing/AltaPatrocinioPanel.form`
- `src/swing/AltaPatrocinioPanel.java`
- `src/swing/AltaUsuarioPanel.java`
- `src/swing/Apariencia.java`
- `src/swing/ConsultaEventoPanel.java`
- `src/swing/ConsultaPatrocinioPanel.form`
- `src/swing/ConsultaPatrocinioPanel.java`
- `src/swing/ConsultaUsuarioPanel.java`
- `src/swing/ModificarUsuarioPanel.form`
- `src/swing/ModificarUsuarioPanel.java`
- `src/swing/principal.java`
- `target/classes/clases/Asistente.class`
- `target/classes/clases/Categoria.class`
- `target/classes/clases/Edicion.class`
- `target/classes/clases/Evento.class`
- `target/classes/clases/Institucion.class`
- `target/classes/clases/NivelPatrocinio.class`
- `target/classes/clases/Organizador.class`
- `target/classes/clases/Patrocinio.class`
- `target/classes/clases/Registro.class`
- `target/classes/clases/TipoRegistro.class`
- `target/classes/clases/Usuario.class`
- `target/classes/com/intellij/uiDesigner/core/AbstractLayout.class`
- `target/classes/com/intellij/uiDesigner/core/DimensionInfo.class`
- `target/classes/com/intellij/uiDesigner/core/GridConstraints.class`
- `target/classes/com/intellij/uiDesigner/core/GridLayoutManager.class`
- `target/classes/com/intellij/uiDesigner/core/HorizontalInfo.class`
- `target/classes/com/intellij/uiDesigner/core/LayoutState.class`
- `target/classes/com/intellij/uiDesigner/core/Spacer.class`
- `target/classes/com/intellij/uiDesigner/core/SupportCode$TextWithMnemonic.class`
- `target/classes/com/intellij/uiDesigner/core/SupportCode.class`
- `target/classes/com/intellij/uiDesigner/core/Util.class`
- `target/classes/com/intellij/uiDesigner/core/VerticalInfo.class`
- `target/classes/datatypes/DtAsistente.class`
- `target/classes/datatypes/DtCategoria.class`
- `target/classes/datatypes/DtEdicion.class`
- `target/classes/datatypes/DtEvento.class`
- `target/classes/datatypes/DtInstitucion.class`
- `target/classes/datatypes/DtOrganizador.class`
- `target/classes/datatypes/DtPatrocinio.class`
- `target/classes/datatypes/DtRegistro.class`
- `target/classes/datatypes/DtTipoRegistro.class`
- `target/classes/datatypes/DtUsuario.class`
- `target/classes/implementacion/Fabrica.class`
- `target/classes/implementacion/Sistema.class`
- `target/classes/interfaces/ISistema.class`
- `target/classes/manejadores/ManejadorEventos.class`
- `target/classes/manejadores/ManejadorInstituciones.class`
- `target/classes/manejadores/ManejadorPatrocinios.class`
- `target/classes/manejadores/ManejadorUsuarios.class`
- `target/classes/swing/AltaEventoPanel.class`
- `target/classes/swing/AltaInstitucionPanel.class`
- `target/classes/swing/AltaPatrocinioPanel.class`
- `target/classes/swing/AltaUsuarioPanel.class`
- `target/classes/swing/Apariencia.class`
- `target/classes/swing/ConsultaPatrocinioPanel.class`
- `target/classes/swing/ConsultaUsuarioPanel.class`
- `target/classes/swing/ModificarUsuarioPanel.class`
- `target/classes/swing/principal.class`
- `webapp/pom.xml`
- `webapp/src/main/java/web/AccesoEdiciones.java`
- `webapp/src/main/java/web/AccesoPatrocinios.java`
- `webapp/src/main/java/web/AltaEdicionServlet.java`
- `webapp/src/main/java/web/AltaEventoServlet.java`
- `webapp/src/main/java/web/AltaInstitucionServlet.java`
- `webapp/src/main/java/web/AltaPatrocinioServlet.java`
- `webapp/src/main/java/web/AltaTipoRegistroServlet.java`
- `webapp/src/main/java/web/AltaUsuarioServlet.java`
- `webapp/src/main/java/web/ConsultaEdicionServlet.java`
- `webapp/src/main/java/web/ConsultaInstitucionServlet.java`
- `webapp/src/main/java/web/ConsultaPatrocinioServlet.java`
- `webapp/src/main/java/web/ConsultaRegistroServlet.java`
- `webapp/src/main/java/web/ConsultaTipoRegistroServlet.java`
- `webapp/src/main/java/web/ContextoListener.java`
- `webapp/src/main/java/web/Csrf.java`
- `webapp/src/main/java/web/DetalleRegistroVista.java`
- `webapp/src/main/java/web/DisponibilidadUsuarioServlet.java`
- `webapp/src/main/java/web/EventoServlet.java`
- `webapp/src/main/java/web/FiltroAcceso.java`
- `webapp/src/main/java/web/ImagenServlet.java`
- `webapp/src/main/java/web/InicioServlet.java`
- `webapp/src/main/java/web/LoginServlet.java`
- `webapp/src/main/java/web/LogoutServlet.java`
- `webapp/src/main/java/web/MensajesFlash.java`
- `webapp/src/main/java/web/ModificarUsuarioServlet.java`
- `webapp/src/main/java/web/RegistroEdicionServlet.java`
- `webapp/src/main/java/web/RegistroVista.java`
- `webapp/src/main/java/web/SesionWeb.java`
- `webapp/src/main/java/web/Subidas.java`
- `webapp/src/main/java/web/UsuarioServlet.java`
- `webapp/src/main/java/web/UsuariosServlet.java`
- `webapp/src/main/java/web/remoto/ClienteCentral.java`
- `webapp/src/main/java/web/remoto/ClienteSesionesCentral.java`
- `webapp/src/main/java/web/remoto/ConfiguracionWeb.java`
- `webapp/src/main/java/web/remoto/ErrorCentral.java`
- `webapp/src/main/java/web/remoto/ServicioSesiones.java`
- `webapp/src/main/java/web/soap/ClienteEventosCentral.java`
- `webapp/src/main/resources/web.properties.example`
- `webapp/src/main/resources/wsdl/EventosService.wsdl`
- `webapp/src/main/resources/wsdl/EventosService_schema1.xsd`
- `webapp/src/main/resources/wsdl/PortalService.wsdl`
- `webapp/src/main/resources/wsdl/PortalService_schema1.xsd`
- `webapp/src/main/resources/wsdl/SesionesService.wsdl`
- `webapp/src/main/resources/wsdl/SesionesService_schema1.xsd`
- `webapp/src/main/webapp/WEB-INF/views/alta-edicion.jsp`
- `webapp/src/main/webapp/WEB-INF/views/alta-evento.jsp`
- `webapp/src/main/webapp/WEB-INF/views/alta-institucion.jsp`
- `webapp/src/main/webapp/WEB-INF/views/alta-patrocinio.jsp`
- `webapp/src/main/webapp/WEB-INF/views/alta-tipo-registro.jsp`
- `webapp/src/main/webapp/WEB-INF/views/alta-usuario.jsp`
- `webapp/src/main/webapp/WEB-INF/views/consulta-institucion.jsp`
- `webapp/src/main/webapp/WEB-INF/views/consulta-patrocinio.jsp`
- `webapp/src/main/webapp/WEB-INF/views/consulta-registro.jsp`
- `webapp/src/main/webapp/WEB-INF/views/edicion.jsp`
- `webapp/src/main/webapp/WEB-INF/views/error-interno.jsp`
- `webapp/src/main/webapp/WEB-INF/views/fragmentos/cabezal.jsp`
- `webapp/src/main/webapp/WEB-INF/views/fragmentos/csrf.jsp`
- `webapp/src/main/webapp/WEB-INF/views/inicio.jsp`
- `webapp/src/main/webapp/WEB-INF/views/login.jsp`
- `webapp/src/main/webapp/WEB-INF/views/modificar-usuario.jsp`
- `webapp/src/main/webapp/WEB-INF/views/registro-edicion.jsp`
- `webapp/src/main/webapp/WEB-INF/views/servicio-no-disponible.jsp`
- `webapp/src/main/webapp/WEB-INF/views/tipo-registro.jsp`
- `webapp/src/main/webapp/WEB-INF/views/usuario.jsp`
- `webapp/src/main/webapp/WEB-INF/views/usuarios.jsp`
- `webapp/src/main/webapp/WEB-INF/web.xml`
- `webapp/src/main/webapp/css/styles.css`
- `webapp/src/main/webapp/img/antel.png`
- `webapp/src/main/webapp/img/default-avatar.png`
- `webapp/src/main/webapp/img/utec.png`
- `webapp/src/main/webapp/index.html`
- `webapp/src/main/webapp/js/alta-edicion-fechas.js`
- `webapp/src/main/webapp/js/alta-patrocinio.js`
- `webapp/src/main/webapp/js/app.js`
- `webapp/src/main/webapp/js/consultas-selectores.js`
- `webapp/src/main/webapp/js/datos.js`
- `webapp/src/main/webapp/js/disponibilidad-usuarios.js`
- `webapp/src/main/webapp/js/edicion.js`
- `webapp/src/main/webapp/js/evento.js`
- `webapp/src/main/webapp/js/guard.js`
- `webapp/src/main/webapp/js/header.js`
- `webapp/src/main/webapp/js/institucion.js`
- `webapp/src/main/webapp/js/login.js`
- `webapp/src/main/webapp/js/patrocinio.js`
- `webapp/src/main/webapp/js/registro.js`
- `webapp/src/main/webapp/js/selects.js`
- `webapp/src/main/webapp/js/tipoRegistro.js`
- `webapp/src/main/webapp/js/usuario.js`
- `webapp/src/main/webapp/js/validacion-altas.js`
- `webapp/src/main/webapp/js/validacion-formularios.js`
- `webapp/src/main/webapp/pages/alta-edicion.html`
- `webapp/src/main/webapp/pages/alta-evento.html`
- `webapp/src/main/webapp/pages/alta-institucion.html`
- `webapp/src/main/webapp/pages/alta-patrocinio.html`
- `webapp/src/main/webapp/pages/alta-tipo-registro.html`
- `webapp/src/main/webapp/pages/alta-usuario.html`
- `webapp/src/main/webapp/pages/consulta-patrocinio.html`
- `webapp/src/main/webapp/pages/consulta-registro.html`
- `webapp/src/main/webapp/pages/edicion.html`
- `webapp/src/main/webapp/pages/evento.html`
- `webapp/src/main/webapp/pages/login.html`
- `webapp/src/main/webapp/pages/logout.html`
- `webapp/src/main/webapp/pages/modificar-usuario.html`
- `webapp/src/main/webapp/pages/registro-edicion.html`
- `webapp/src/main/webapp/pages/tipo-registro.html`
- `webapp/src/main/webapp/pages/usuario.html`
- `webapp/src/main/webapp/pages/usuarios.html`
- `webapp/src/main/webapp/vendor/bootstrap/css/bootstrap.min.css`
- `webapp/src/main/webapp/vendor/bootstrap/js/bootstrap.bundle.min.js`
- `webapp/src/test/java/web/AltasPrgTest.java`
- `webapp/src/test/java/web/ConsultasEdicionTest.java`
- `webapp/src/test/java/web/CsrfTest.java`
- `webapp/src/test/java/web/DisponibilidadUsuarioTest.java`
- `webapp/src/test/java/web/InicioSoapTest.java`
- `webapp/src/test/java/web/ModificarPerfilAtomicoTest.java`
- `webapp/src/test/java/web/PatrociniosAutorizacionTest.java`
- `webapp/src/test/java/web/PatrociniosFlujoTest.java`
- `webapp/src/test/java/web/PortalDoble.java`
- `webapp/src/test/java/web/SesionRemotaWebTest.java`
- `webapp/src/test/java/web/SubidasTest.java`
- `webapp/src/test/java/web/UsuarioFechaAltaTest.java`
- `webapp/src/test/java/web/soap/ArranqueHttpsCentralTest.java`
- `webapp/src/test/java/web/soap/CentralJarArranqueIT.java`
- `webapp/src/test/java/web/soap/ContratosRegeneracionTest.java`
- `webapp/src/test/java/web/soap/ConversorContratoTest.java`
- `webapp/src/test/java/web/soap/DisponibilidadSoapTest.java`
- `webapp/src/test/java/web/soap/EncabezadosSoapHttpsTest.java`
- `webapp/src/test/java/web/soap/PortalPersistenciaHttpsTest.java`
- `webapp/src/test/java/web/soap/PortalSoapHttpsTest.java`
- `webapp/src/test/java/web/soap/PruebaHttpsCliente.java`
- `webapp/src/test/java/web/soap/PruebaHttpsServidor.java`
- `webapp/src/test/java/web/soap/SesionesSoapHttpsTest.java`
- `webapp/src/test/java/web/soap/SoporteTls.java`
- `webapp/src/test/java/web/soap/TomcatPortalHttpsIT.java`
- `webapp/src/test/js/alta-edicion-fechas.test.cjs`
- `webapp/src/test/js/alta-patrocinio.test.cjs`
- `webapp/src/test/js/consultas-selectores.test.cjs`
- `webapp/src/test/js/disponibilidad-usuarios.test.cjs`
- `webapp/src/test/js/validacion-altas.test.cjs`
- `webapp/src/test/js/validacion-formularios.test.cjs`
- `webapp/src/test/manual/AuditarSeguridadWeb.java`
- `webapp/src/test/manual/README.md`
- `webapp/src/test/manual/VerificarBaseConsultas.java`
- `webapp/src/test/manual/VerificarConsultasWeb.java`
- `webapp/src/test/manual/VerificarCsrfPrgWeb.java`
- `webapp/src/test/manual/VerificarPatrociniosWeb.java`
- `webapp/src/test/manual/auditar-seguridad.ps1`
- `webapp/src/test/manual/verificar-consultas.ps1`
- `webapp/src/test/manual/verificar-csrf-prg.ps1`
- `webapp/src/test/manual/verificar-patrocinios.ps1`
- `webapp/src/test/manual/verificar-portal-https.ps1`

</details>

### Emmanuel Lacava

14 commits propios; ultimo aporte por fecha de autor: 2026-11-15T13:21:11-03:00; 50 rutas historicas afectadas.

Referencias que contienen sus commits (no todas son ramas personales): `local/ConsultaeventoYAltacat`, `local/Registros_Y_Eventos`, `local/Revision4`, `local/dev`, `local/feature/correcciones-logica`, `local/feature/instituciones-patrocinios`, `local/feature/jpa-entidades`, `local/feature/persistencia`, `local/feature/persistencia-jpa`, `local/lab2-cobertura`, `local/lab2-consultas-edicion-tipo`, `local/lab2-integracion-equipo`, `local/lab2-logica-estado-edicion`, `local/lab2-logica-login`, `local/lab2-patrocinios-web`, `local/lab2-seguridad-patrocinios`, `local/lab2-swing`, `local/lab2-swing-datos`, `local/lab2-web-infraestructura`, `local/lab2-web-institucion`, `local/lab2-web-usuarios`, `local/lab3-desarrollo`, `local/restructura-modulos`, `local/revision`, `local/swingEdicionEventos`, `origin/Revision2`, `origin/Revision6`, `origin/caso_consultaDeRegistro`, `origin/consultaeventoarreglado`, `origin/dev`, `origin/feature/casos_adrian`, `origin/feature/instituciones-patrocinios`, `origin/feature/persistencia`, `origin/feature/registros-joaquinb`, `origin/feature/swing-usuario-imagenes`, `origin/feature/tests-eventos-categorias`, `origin/lab2-Emmanuel`, `origin/lab2-cobertura`, `origin/lab2-emmanuel-tests`, `origin/lab2-swing-datos`, `origin/lab2-web-usuarios`, `origin/lab3-desarrollo`, `origin/lab3-emmanuel`, `origin/swingEdicionEventos`.

| Commit | Fecha autor | Asunto | Archivos afectados |
| --- | --- | --- | --- |
| `2911ab1a8135bdc20b69e22c93a654bf1709c239` | 2026-11-15T13:21:11-03:00 | Implementar disponibilidad de usuarios mediante SOAP | 10 |
| `8ec0c7c23db3d23a965f184e695fb51575c704c2` | 2026-10-29T11:02:16-03:00 | Agrega pruebas JUnit adicionales para mejorar cobertura | 1 |
| `8b2778b6bb69e350bbcf024c1c1de851ab9d4f14` | 2026-10-24T09:38:56-03:00 | Implementar tres casos de uso web del Lab 2 | 6 |
| `742499e5fb4765e5d223eb2dcab036fa839edc8d` | 2026-10-23T09:18:14-03:00 | Recuperar cambios de alta de edicion | 1 |
| `908522dd365b5e49b5adb3537694bc9daa8c0235` | 2026-10-13T12:20:12-03:00 | Corregir alta y consulta de ediciones y alta de tipos de registro | 3 |
| `02b4b8c76a2cd0243f7965cbe8f97bce971b959c` | 2026-10-09T10:44:56-03:00 | Lab 2: alta y consulta de ediciones y alta de tipos de registro | 4 |
| `db5b7f8aecbca59bca8ad02f0a92e002311aa48c` | 2026-10-06T20:50:36-03:00 | Corrige acceso a registro en consulta de edicion | 1 |
| `2086604a2025f06ec9b885a539e5515e99caea99` | 2026-10-06T20:19:51-03:00 | Completa consulta de edicion y alta tipo registro Lab 2 | 2 |
| `5f815674c479cb6f7340e3edbc4e66b7db3646f4` | 2026-10-05T22:49:19-03:00 | Completa alta de edicion Lab 2 | 1 |
| `6031b816b81b14e91e351c538224ce09c5e44acc` | 2026-09-12T23:58:01-03:00 | Organizador, asistente y unas cuantas cosas más!!! ver y avisar | 11 |
| `7bd7ca5259a9f7535383150a848a3bf5b657fb82` | 2026-09-07T22:18:41-03:00 | Swing y corregido cosas de la logica | 6 |
| `cb4d004bb1cb95b4fa65fed6d889c89915791e35` | 2026-08-31T22:12:42-03:00 | Corregido errores en logica | 10 |
| `87aa716fb119a29ca70a29cc44acb1b6a8159a5a` | 2026-08-31T20:27:30-03:00 | Corregido errores | 2 |
| `0e0f61c2e485dd1718a96cf60f67d4ab7305bf5d` | 2026-08-29T20:03:06-03:00 | Mejora de estructura! | 5 |

<details>
<summary>Listado completo de rutas historicas afectadas</summary>

- `.idea/dataSources.xml`
- `ENTREGA_AVANCE_LAB3.md`
- `logica/src/test/java/implementacion/ValidacionesAdicionalesTest.java`
- `servidor-central/src/main/java/central/soap/EventosPublicador.java`
- `servidor-central/src/test/java/central/DisponibilidadSoapTest.java`
- `src/clases/Asistente.java`
- `src/clases/Edicion.java`
- `src/clases/Evento.java`
- `src/clases/Patrocinio.java`
- `src/clases/Registro.java`
- `src/clases/TipoRegistro.java`
- `src/clases/Usuario.java`
- `src/datatypes/DtEdicion.java`
- `src/datatypes/DtPatrocinio.java`
- `src/implementacion/Sistema.java`
- `src/interfaces/ISistema.java`
- `src/main/java/clases/Organizador.java`
- `src/main/java/datatypes/DtAltaTipoRegistro.java`
- `src/main/java/datatypes/DtRegistro.java`
- `src/main/java/implementacion/Sistema.java`
- `src/main/java/interfaces/ISistema.java`
- `src/main/java/manejadores/ManejadorEventos.java`
- `src/main/java/manejadores/ManejadorRegistros.java`
- `src/main/java/swing/AltaEdicionEventoPanel.java`
- `src/main/java/swing/AltaTipoRegistro.form`
- `src/main/java/swing/AltaTipoRegistro.java`
- `src/main/java/swing/ConsultaPatrocinioPanel.form`
- `src/main/java/swing/ConsultaUsuarioPanel.java`
- `src/main/java/swing/RegistroEdicionEventoPanel.java`
- `src/main/java/swing/principal.java`
- `src/main/webapp/js/datos.js`
- `src/main/webapp/js/edicion.js`
- `src/main/webapp/pages/alta-edicion.html`
- `src/main/webapp/pages/alta-tipo-registro.html`
- `src/main/webapp/pages/edicion.html`
- `src/manejadores/ManejadorUsuarios.java`
- `webapp/src/main/java/web/AltaEdicionServlet.java`
- `webapp/src/main/java/web/AltaTipoRegistroServlet.java`
- `webapp/src/main/java/web/ConsultaEdicionServlet.java`
- `webapp/src/main/java/web/DisponibilidadUsuarioServlet.java`
- `webapp/src/main/java/web/soap/ClienteEventosCentral.java`
- `webapp/src/main/resources/wsdl/EventosService.wsdl`
- `webapp/src/main/resources/wsdl/EventosService_schema1.xsd`
- `webapp/src/main/webapp/WEB-INF/views/alta-edicion.jsp`
- `webapp/src/main/webapp/WEB-INF/views/alta-tipo-registro.jsp`
- `webapp/src/main/webapp/WEB-INF/views/alta-usuario.jsp`
- `webapp/src/main/webapp/WEB-INF/views/edicion.jsp`
- `webapp/src/main/webapp/js/disponibilidad-usuario.js`
- `webapp/src/main/webapp/pages/alta-edicion.html`
- `webapp/src/test/js/disponibilidad-usuario.test.cjs`

</details>

### Joaquin Bentancor

5 commits propios; ultimo aporte por fecha de autor: 2026-11-14T18:03:29-03:00; 277 rutas historicas afectadas.

Referencias que contienen sus commits (no todas son ramas personales): `local/dev`, `local/feature/instituciones-patrocinios`, `local/lab2-cobertura`, `local/lab2-consultas-edicion-tipo`, `local/lab2-integracion-equipo`, `local/lab2-logica-estado-edicion`, `local/lab2-logica-login`, `local/lab2-patrocinios-web`, `local/lab2-seguridad-patrocinios`, `local/lab2-swing`, `local/lab2-swing-datos`, `local/lab2-web-infraestructura`, `local/lab2-web-institucion`, `local/lab2-web-usuarios`, `local/lab3-desarrollo`, `local/restructura-modulos`, `origin/cambios-eventos-uy-2026-11-14`, `origin/dev`, `origin/feature/casos_adrian`, `origin/feature/instituciones-patrocinios`, `origin/feature/registros-joaquinb`, `origin/feature/swing-usuario-imagenes`, `origin/feature/tests-eventos-categorias`, `origin/joaquin-b-registros`, `origin/lab2-Emmanuel`, `origin/lab2-cobertura`, `origin/lab2-emmanuel-tests`, `origin/lab2-swing-datos`, `origin/lab2-web-usuarios`, `origin/lab3-desarrollo`, `origin/lab3-emmanuel`.

| Commit | Fecha autor | Asunto | Archivos afectados |
| --- | --- | --- | --- |
| `1afff26908f57886599c3630d12194e158f8ce88` | 2026-11-14T18:03:29-03:00 | Incorporar proyecto eventos.uy y configuracion de usuarios | 213 |
| `d7f2333ec3185e62ee55f482494ab41dca462ac9` | 2026-10-25T21:08:02-03:00 | Registro a edición y consulta de registro (web), DtRegistro con asistente y uso de código, pruebas | 166 |
| `fb5b44a6a3b7304f9fc6e2ced4b4a3325228821f` | 2026-10-10T14:07:11-03:00 | Adapta consulta de tipo de registro, registro a edición y consulta de registro al login | 5 |
| `cf951bc0ada61b622ac1c16106728cd4efdfabf5` | 2026-10-08T20:05:06-03:00 | Agrega consultas de tipo de registro y de registros para asistentes y organizadores | 6 |
| `b296ca66897c718aa27457d96ced2b5c57c31b52` | 2026-09-13T17:10:13-03:00 | Fix: Agregar información completa en Consulta de Edición de Evento | 4 |

<details>
<summary>Listado completo de rutas historicas afectadas</summary>

- `.gitignore`
- `.idea/.gitignore`
- `.idea/compiler.xml`
- `.idea/dataSources.xml`
- `.idea/encodings.xml`
- `.idea/jarRepositories.xml`
- `.idea/misc.xml`
- `.idea/uiDesigner.xml`
- `.idea/vcs.xml`
- `.smarttomcat/webapp/conf/catalina.properties`
- `.smarttomcat/webapp/conf/context.xml`
- `.smarttomcat/webapp/conf/jaspic-providers.xml`
- `.smarttomcat/webapp/conf/jaspic-providers.xsd`
- `.smarttomcat/webapp/conf/logging.properties`
- `.smarttomcat/webapp/conf/server.xml`
- `.smarttomcat/webapp/conf/tomcat-users.xml`
- `.smarttomcat/webapp/conf/tomcat-users.xsd`
- `.smarttomcat/webapp/conf/web.xml`
- `Laboratorio 2 - 2026 (1).pdf`
- `PA_2026.iml`
- `README.md`
- `estacion-trabajo/pom.xml`
- `estacion-trabajo/src/main/java/swing/AceptarRechazarEdicionPanel.java`
- `estacion-trabajo/src/main/java/swing/AltaCategoriaPanel.java`
- `estacion-trabajo/src/main/java/swing/AltaEdicionEventoPanel.java`
- `estacion-trabajo/src/main/java/swing/AltaEventoPanel.java`
- `estacion-trabajo/src/main/java/swing/AltaInstitucionPanel.form`
- `estacion-trabajo/src/main/java/swing/AltaInstitucionPanel.java`
- `estacion-trabajo/src/main/java/swing/AltaPatrocinioPanel.form`
- `estacion-trabajo/src/main/java/swing/AltaPatrocinioPanel.java`
- `estacion-trabajo/src/main/java/swing/AltaTipoRegistro.form`
- `estacion-trabajo/src/main/java/swing/AltaTipoRegistro.java`
- `estacion-trabajo/src/main/java/swing/AltaUsuarioPanel.java`
- `estacion-trabajo/src/main/java/swing/Apariencia.java`
- `estacion-trabajo/src/main/java/swing/ConsultaEdicionEventoPanel.java`
- `estacion-trabajo/src/main/java/swing/ConsultaEventoPanel.java`
- `estacion-trabajo/src/main/java/swing/ConsultaPatrocinioPanel.form`
- `estacion-trabajo/src/main/java/swing/ConsultaPatrocinioPanel.java`
- `estacion-trabajo/src/main/java/swing/ConsultaRegistroPanel.java`
- `estacion-trabajo/src/main/java/swing/ConsultaTipoRegistroPanel.java`
- `estacion-trabajo/src/main/java/swing/ConsultaUsuarioPanel.java`
- `estacion-trabajo/src/main/java/swing/FormularioSwing.java`
- `estacion-trabajo/src/main/java/swing/ImagenSwing.java`
- `estacion-trabajo/src/main/java/swing/IniciarConUsuarios.java`
- `estacion-trabajo/src/main/java/swing/ModificarUsuarioPanel.form`
- `estacion-trabajo/src/main/java/swing/ModificarUsuarioPanel.java`
- `estacion-trabajo/src/main/java/swing/RegistroEdicionEventoPanel.java`
- `estacion-trabajo/src/main/java/swing/principal.java`
- `estacion-trabajo/src/test/manual/README.md`
- `estacion-trabajo/src/test/manual/VerificarArranque.java`
- `estacion-trabajo/src/test/manual/VerificarBloqueD.java`
- `estacion-trabajo/src/test/manual/VerificarImagenesSegurasSwing.java`
- `estacion-trabajo/src/test/manual/VerificarSwing.java`
- `logica/pom.xml`
- `logica/src/main/java/clases/Asistente.java`
- `logica/src/main/java/clases/Categoria.java`
- `logica/src/main/java/clases/ConImagen.java`
- `logica/src/main/java/clases/Edicion.java`
- `logica/src/main/java/clases/Evento.java`
- `logica/src/main/java/clases/Imagen.java`
- `logica/src/main/java/clases/Institucion.java`
- `logica/src/main/java/clases/NivelPatrocinio.java`
- `logica/src/main/java/clases/Organizador.java`
- `logica/src/main/java/clases/Patrocinio.java`
- `logica/src/main/java/clases/Registro.java`
- `logica/src/main/java/clases/TipoRegistro.java`
- `logica/src/main/java/clases/Usuario.java`
- `logica/src/main/java/datatypes/DtAltaTipoRegistro.java`
- `logica/src/main/java/datatypes/DtAsistente.java`
- `logica/src/main/java/datatypes/DtCategoria.java`
- `logica/src/main/java/datatypes/DtConsultaPatrocinio.java`
- `logica/src/main/java/datatypes/DtEdicion.java`
- `logica/src/main/java/datatypes/DtEvento.java`
- `logica/src/main/java/datatypes/DtFormularioPatrocinio.java`
- `logica/src/main/java/datatypes/DtImagen.java`
- `logica/src/main/java/datatypes/DtInstitucion.java`
- `logica/src/main/java/datatypes/DtOrganizador.java`
- `logica/src/main/java/datatypes/DtPatrocinio.java`
- `logica/src/main/java/datatypes/DtPerfilUsuario.java`
- `logica/src/main/java/datatypes/DtRegistro.java`
- `logica/src/main/java/datatypes/DtSesion.java`
- `logica/src/main/java/datatypes/DtTipoRegistro.java`
- `logica/src/main/java/datatypes/DtUsuario.java`
- `logica/src/main/java/datatypes/EstadoEdicion.java`
- `logica/src/main/java/datatypes/FechaXml.java`
- `logica/src/main/java/datatypes/TipoEntidadImagen.java`
- `logica/src/main/java/datos/CargarDatosPrueba.java`
- `logica/src/main/java/implementacion/Fabrica.java`
- `logica/src/main/java/implementacion/Sistema.java`
- `logica/src/main/java/implementacion/ValidadorImagen.java`
- `logica/src/main/java/interfaces/ISistema.java`
- `logica/src/main/java/interfaces/IUsuariosWeb.java`
- `logica/src/main/java/manejadores/ConflictosPersistencia.java`
- `logica/src/main/java/manejadores/ManejadorEventos.java`
- `logica/src/main/java/manejadores/ManejadorImagenes.java`
- `logica/src/main/java/manejadores/ManejadorInstituciones.java`
- `logica/src/main/java/manejadores/ManejadorPatrocinios.java`
- `logica/src/main/java/manejadores/ManejadorRegistros.java`
- `logica/src/main/java/manejadores/ManejadorUsuarios.java`
- `logica/src/main/java/persistencia/BaseDeDatos.java`
- `logica/src/main/java/seguridad/HashContrasenia.java`
- `logica/src/main/java/soap/ErrorUsuarios.java`
- `logica/src/main/java/soap/FalloUsuarios.java`
- `logica/src/main/java/soap/PublicadorUsuarios.java`
- `logica/src/main/java/soap/UsuariosEndpoint.java`
- `logica/src/main/java/usuarios/ControladorUsuariosWeb.java`
- `logica/src/main/java/usuarios/ErrorAccesoUsuarios.java`
- `logica/src/main/java/usuarios/SesionesUsuarios.java`
- `logica/src/main/resources/META-INF/persistence.xml`
- `logica/src/main/resources/usuarios-central.properties`
- `logica/src/test/java/implementacion/ConsultaTipoRegistroTest.java`
- `logica/src/test/java/implementacion/ConsultasSistemaTest.java`
- `logica/src/test/java/implementacion/DecodificacionImagenTest.java`
- `logica/src/test/java/implementacion/EstadoEdicionConcurrenciaTest.java`
- `logica/src/test/java/implementacion/EstadoEdicionTest.java`
- `logica/src/test/java/implementacion/EtapaUnoTest.java`
- `logica/src/test/java/implementacion/ImagenesTest.java`
- `logica/src/test/java/implementacion/LimitesTextoTest.java`
- `logica/src/test/java/implementacion/LoginTest.java`
- `logica/src/test/java/implementacion/ModificacionPerfilAtomicaTest.java`
- `logica/src/test/java/implementacion/PatrocinioTest.java`
- `logica/src/test/java/implementacion/PersistenciaConcurrenteTest.java`
- `logica/src/test/java/implementacion/RegistroTest.java`
- `logica/src/test/java/implementacion/ReglasAltaTest.java`
- `logica/src/test/java/implementacion/SingletonConcurrenteTest.java`
- `logica/src/test/java/implementacion/UnicidadConcurrenciaTest.java`
- `logica/src/test/java/implementacion/UsuarioTest.java`
- `logica/src/test/java/implementacion/ValidacionesAdicionalesTest.java`
- `logica/src/test/java/implementacion/ValidacionesPersistenciaTest.java`
- `logica/src/test/java/implementacion/ValidadorImagenTest.java`
- `logica/src/test/java/manejadores/ConflictosPersistenciaTest.java`
- `logica/src/test/java/seguridad/HashContraseniaTest.java`
- `logica/src/test/java/soporte/ImagenesPrueba.java`
- `logica/src/test/java/soporte/ProteccionBasePruebas.java`
- `logica/src/test/java/soporte/ProteccionBasePruebasTest.java`
- `logica/src/test/java/soporte/PruebaConBaseSegura.java`
- `logica/src/test/java/usuarios/ImagenesAtomicasUsuariosTest.java`
- `logica/src/test/java/usuarios/SesionesUsuariosTest.java`
- `logica/src/test/java/usuarios/UsuariosWebTest.java`
- `logica/src/test/manual/AuditarFronteraLogica.java`
- `pom.xml`
- `src/main/java/implementacion/Sistema.java`
- `src/main/java/interfaces/ISistema.java`
- `src/main/java/manejadores/ManejadorRegistros.java`
- `src/main/java/swing/ConsultaEdicionEventoPanel.java`
- `src/main/webapp/js/datos.js`
- `src/main/webapp/js/registro.js`
- `src/main/webapp/js/tipoRegistro.js`
- `src/main/webapp/pages/consulta-registro.html`
- `src/main/webapp/pages/registro-edicion.html`
- `src/main/webapp/pages/tipo-registro.html`
- `webapp/pom.xml`
- `webapp/src/main/java/web/AccesoEdiciones.java`
- `webapp/src/main/java/web/AccesoPatrocinios.java`
- `webapp/src/main/java/web/AltaEdicionServlet.java`
- `webapp/src/main/java/web/AltaEventoServlet.java`
- `webapp/src/main/java/web/AltaInstitucionServlet.java`
- `webapp/src/main/java/web/AltaPatrocinioServlet.java`
- `webapp/src/main/java/web/AltaTipoRegistroServlet.java`
- `webapp/src/main/java/web/AltaUsuarioServlet.java`
- `webapp/src/main/java/web/ConsultaEdicionServlet.java`
- `webapp/src/main/java/web/ConsultaPatrocinioServlet.java`
- `webapp/src/main/java/web/ConsultaRegistroServlet.java`
- `webapp/src/main/java/web/ConsultaTipoRegistroServlet.java`
- `webapp/src/main/java/web/ContextoListener.java`
- `webapp/src/main/java/web/Csrf.java`
- `webapp/src/main/java/web/DetalleRegistroVista.java`
- `webapp/src/main/java/web/DisponibilidadUsuarioServlet.java`
- `webapp/src/main/java/web/EventoServlet.java`
- `webapp/src/main/java/web/FiltroAcceso.java`
- `webapp/src/main/java/web/FormularioUsuario.java`
- `webapp/src/main/java/web/ImagenServlet.java`
- `webapp/src/main/java/web/InicioServlet.java`
- `webapp/src/main/java/web/LoginServlet.java`
- `webapp/src/main/java/web/LogoutServlet.java`
- `webapp/src/main/java/web/MensajesFlash.java`
- `webapp/src/main/java/web/ModificarUsuarioServlet.java`
- `webapp/src/main/java/web/RegistroEdicionServlet.java`
- `webapp/src/main/java/web/RegistroVista.java`
- `webapp/src/main/java/web/Subidas.java`
- `webapp/src/main/java/web/UsuarioServlet.java`
- `webapp/src/main/java/web/UsuariosServlet.java`
- `webapp/src/main/java/web/remoto/ClienteUsuarios.java`
- `webapp/src/main/java/web/remoto/ConfiguracionUsuariosCliente.java`
- `webapp/src/main/java/web/remoto/ConsultaInstitucionServlet.java`
- `webapp/src/main/java/web/remoto/ConversorUsuarios.java`
- `webapp/src/main/java/web/remoto/ErrorRemotoUsuarios.java`
- `webapp/src/main/resources/usuarios-web.properties`
- `webapp/src/main/webapp/WEB-INF/views/alta-edicion.jsp`
- `webapp/src/main/webapp/WEB-INF/views/alta-evento.jsp`
- `webapp/src/main/webapp/WEB-INF/views/alta-institucion.jsp`
- `webapp/src/main/webapp/WEB-INF/views/alta-patrocinio.jsp`
- `webapp/src/main/webapp/WEB-INF/views/alta-tipo-registro.jsp`
- `webapp/src/main/webapp/WEB-INF/views/alta-usuario.jsp`
- `webapp/src/main/webapp/WEB-INF/views/consulta-institucion.jsp`
- `webapp/src/main/webapp/WEB-INF/views/consulta-patrocinio.jsp`
- `webapp/src/main/webapp/WEB-INF/views/consulta-registro.jsp`
- `webapp/src/main/webapp/WEB-INF/views/edicion.jsp`
- `webapp/src/main/webapp/WEB-INF/views/error-interno.jsp`
- `webapp/src/main/webapp/WEB-INF/views/evento.jsp`
- `webapp/src/main/webapp/WEB-INF/views/fragmentos/cabezal.jsp`
- `webapp/src/main/webapp/WEB-INF/views/fragmentos/csrf.jsp`
- `webapp/src/main/webapp/WEB-INF/views/inicio.jsp`
- `webapp/src/main/webapp/WEB-INF/views/login.jsp`
- `webapp/src/main/webapp/WEB-INF/views/modificar-usuario.jsp`
- `webapp/src/main/webapp/WEB-INF/views/registro-edicion.jsp`
- `webapp/src/main/webapp/WEB-INF/views/tipo-registro.jsp`
- `webapp/src/main/webapp/WEB-INF/views/usuario.jsp`
- `webapp/src/main/webapp/WEB-INF/views/usuarios-no-disponible.jsp`
- `webapp/src/main/webapp/WEB-INF/views/usuarios.jsp`
- `webapp/src/main/webapp/WEB-INF/web.xml`
- `webapp/src/main/webapp/css/styles.css`
- `webapp/src/main/webapp/img/antel.png`
- `webapp/src/main/webapp/img/default-avatar.png`
- `webapp/src/main/webapp/img/utec.png`
- `webapp/src/main/webapp/index.html`
- `webapp/src/main/webapp/js/alta-edicion-fechas.js`
- `webapp/src/main/webapp/js/alta-patrocinio.js`
- `webapp/src/main/webapp/js/app.js`
- `webapp/src/main/webapp/js/consultas-selectores.js`
- `webapp/src/main/webapp/js/datos.js`
- `webapp/src/main/webapp/js/disponibilidad-usuarios.js`
- `webapp/src/main/webapp/js/edicion.js`
- `webapp/src/main/webapp/js/evento.js`
- `webapp/src/main/webapp/js/guard.js`
- `webapp/src/main/webapp/js/header.js`
- `webapp/src/main/webapp/js/institucion.js`
- `webapp/src/main/webapp/js/login.js`
- `webapp/src/main/webapp/js/patrocinio.js`
- `webapp/src/main/webapp/js/registro.js`
- `webapp/src/main/webapp/js/selects.js`
- `webapp/src/main/webapp/js/tipoRegistro.js`
- `webapp/src/main/webapp/js/usuario.js`
- `webapp/src/main/webapp/js/validacion-altas.js`
- `webapp/src/main/webapp/js/validacion-formularios.js`
- `webapp/src/main/webapp/pages/alta-edicion.html`
- `webapp/src/main/webapp/pages/alta-evento.html`
- `webapp/src/main/webapp/pages/alta-institucion.html`
- `webapp/src/main/webapp/pages/alta-patrocinio.html`
- `webapp/src/main/webapp/pages/alta-tipo-registro.html`
- `webapp/src/main/webapp/pages/alta-usuario.html`
- `webapp/src/main/webapp/pages/consulta-patrocinio.html`
- `webapp/src/main/webapp/pages/consulta-registro.html`
- `webapp/src/main/webapp/pages/edicion.html`
- `webapp/src/main/webapp/pages/evento.html`
- `webapp/src/main/webapp/pages/login.html`
- `webapp/src/main/webapp/pages/logout.html`
- `webapp/src/main/webapp/pages/modificar-usuario.html`
- `webapp/src/main/webapp/pages/registro-edicion.html`
- `webapp/src/main/webapp/pages/tipo-registro.html`
- `webapp/src/main/webapp/pages/usuario.html`
- `webapp/src/main/webapp/pages/usuarios.html`
- `webapp/src/main/webapp/vendor/bootstrap/css/bootstrap.min.css`
- `webapp/src/main/webapp/vendor/bootstrap/js/bootstrap.bundle.min.js`
- `webapp/src/test/java/web/AltasPrgTest.java`
- `webapp/src/test/java/web/ConsultasEdicionTest.java`
- `webapp/src/test/java/web/CsrfTest.java`
- `webapp/src/test/java/web/PatrociniosAutorizacionTest.java`
- `webapp/src/test/java/web/PatrociniosFlujoTest.java`
- `webapp/src/test/java/web/SubidasTest.java`
- `webapp/src/test/java/web/UsuarioFechaAltaTest.java`
- `webapp/src/test/java/web/remoto/ConversorUsuariosTest.java`
- `webapp/src/test/js/alta-edicion-fechas.test.cjs`
- `webapp/src/test/js/alta-patrocinio.test.cjs`
- `webapp/src/test/js/consultas-selectores.test.cjs`
- `webapp/src/test/js/validacion-altas.test.cjs`
- `webapp/src/test/js/validacion-formularios.test.cjs`
- `webapp/src/test/manual/AuditarSeguridadWeb.java`
- `webapp/src/test/manual/README.md`
- `webapp/src/test/manual/VerificarBaseConsultas.java`
- `webapp/src/test/manual/VerificarConsultasWeb.java`
- `webapp/src/test/manual/VerificarCsrfPrgWeb.java`
- `webapp/src/test/manual/VerificarPatrociniosWeb.java`
- `webapp/src/test/manual/auditar-seguridad.ps1`
- `webapp/src/test/manual/verificar-consultas.ps1`
- `webapp/src/test/manual/verificar-csrf-prg.ps1`
- `webapp/src/test/manual/verificar-patrocinios.ps1`

</details>

### Valentina Carrique

18 commits propios; ultimo aporte por fecha de autor: 2026-10-30T17:58:36-03:00; 47 rutas historicas afectadas.

Referencias que contienen sus commits (no todas son ramas personales): `local/ConsultaeventoYAltacat`, `local/Registros_Y_Eventos`, `local/Revision4`, `local/altaEvento`, `local/dev`, `local/feature/alta-institucion`, `local/feature/clases_faltantes`, `local/feature/correcciones-logica`, `local/feature/instituciones-patrocinios`, `local/feature/jpa-entidades`, `local/feature/modificar-usuario`, `local/feature/patrocinio`, `local/feature/persistencia`, `local/feature/persistencia-jpa`, `local/lab2-cobertura`, `local/lab2-consultas-edicion-tipo`, `local/lab2-integracion-equipo`, `local/lab2-logica-estado-edicion`, `local/lab2-logica-login`, `local/lab2-patrocinios-web`, `local/lab2-seguridad-patrocinios`, `local/lab2-swing`, `local/lab2-swing-datos`, `local/lab2-web-infraestructura`, `local/lab2-web-institucion`, `local/lab2-web-usuarios`, `local/lab3-desarrollo`, `local/restructura-modulos`, `local/revision`, `local/swingEdicionEventos`, `origin/ConsultaeventoyAltacat`, `origin/Revision2`, `origin/Revision6`, `origin/caso_consultaDeRegistro`, `origin/consultaeventoarreglado`, `origin/dev`, `origin/feature/casos_adrian`, `origin/feature/instituciones-patrocinios`, `origin/feature/persistencia`, `origin/feature/registros-joaquinb`, `origin/feature/swing-usuario-imagenes`, `origin/feature/tests-eventos-categorias`, `origin/lab2-Emmanuel`, `origin/lab2-cobertura`, `origin/lab2-emmanuel-tests`, `origin/lab2-swing-datos`, `origin/lab2-web-usuarios`, `origin/lab3-desarrollo`, `origin/lab3-emmanuel`, `origin/swingEdicionEventos`.

| Commit | Fecha autor | Asunto | Archivos afectados |
| --- | --- | --- | --- |
| `7db899c3d580558f6266085ddf34c94c90e065d0` | 2026-10-30T17:58:36-03:00 | Pruebas JUnit de eventos y categorias | 1 |
| `b938a9c7642719438b229b6eafe6bf731e5adf1c` | 2026-10-29T18:50:37-03:00 | Swing: estado de las ediciones en consultas y registro solo en Aceptadas | 3 |
| `00e475048e1df82bc94f3cd61b1401d299ae424d` | 2026-10-29T17:33:25-03:00 | Swing: imagen opcional en altas de evento, edicion e institucion | 5 |
| `20af1e42486bb71ed237438cc7637b8686929301` | 2026-10-29T15:41:16-03:00 | Swing: modificar usuario con contraseña e imagen | 2 |
| `2bcf7f9d2d7522548bf4cc48af27636ffe324cc7` | 2026-10-25T14:15:45-03:00 | Alta de evento: servlet y JSP | 2 |
| `970eb1e37ecc45ce35a01995f6e88d2532cc0373` | 2026-10-25T14:15:19-03:00 | Consulta de evento: servlet y JSP | 4 |
| `18f71ab493c56b312babb2d6032f7a885cdb9f67` | 2026-10-13T18:35:22-03:00 | Corrige boton Cancelar en modificar usuario | 2 |
| `a1da47537d896e905d7023715daea8802e774739` | 2026-10-09T20:14:09-03:00 | Corrige categorias en datos.js a objetos con id y nombre | 1 |
| `f14e0bd3c2da5b8f0c2644e69c84df74994eb61a` | 2026-10-09T19:55:28-03:00 | Merge branch 'dev' of https://github.com/Joaquin571/eventos-uy into lab2-valentina  the commit. | 0 |
| `933f0e62418aeb87acb84aceb3c6d8705e8fa6da` | 2026-10-09T19:55:13-03:00 | Modificar datos de usuario Lab 2 | 5 |
| `8c113fbc391f2fc88efc51de554f727b5d3e70a8` | 2026-10-08T21:52:49-03:00 | Merge branch 'dev' of https://github.com/Joaquin571/eventos-uy into dev | 0 |
| `68b2447a9a8e8bd8d1af02658efb011ffb7db3ef` | 2026-10-08T21:52:37-03:00 | Alta y consulta de evento Lab 2 | 6 |
| `389caede11f8137c4f6916e5d4c6a6e8c168762d` | 2026-09-13T10:02:52-03:00 | caso de usao Comsulta de registro de evento | 6 |
| `a0489507a26cebd13d13b6a4e1718a186558d82c` | 2026-09-12T15:06:29-03:00 | pronto 100% consulta evento | 5 |
| `8569d035327b54bd79c95658efa1ea940bba85e4` | 2026-09-12T13:42:32-03:00 | arregle la ventana de alta edicion Evento | 1 |
| `196dac7b1271d36dbfe6bf01f2aaebb3e783d863` | 2026-08-30T20:04:25-03:00 | consultaevento y alta evento sin persistencia | 8 |
| `5b899da8c0676a4d753e40291650a447f6e51556` | 2026-08-29T02:41:46-03:00 | Caso de uso Alta Evento sin persistencia | 8 |
| `b21d9556fd04a675f9c271b414d08294368eacaa` | 2026-08-24T17:17:44-03:00 | Implementacion de interfaz swing para Alta de Usuario | 3 |

<details>
<summary>Listado completo de rutas historicas afectadas</summary>

- `estacion-trabajo/src/main/java/swing/AltaEdicionEventoPanel.java`
- `estacion-trabajo/src/main/java/swing/AltaEventoPanel.java`
- `estacion-trabajo/src/main/java/swing/AltaInstitucionPanel.form`
- `estacion-trabajo/src/main/java/swing/AltaInstitucionPanel.java`
- `estacion-trabajo/src/main/java/swing/ConsultaEdicionEventoPanel.java`
- `estacion-trabajo/src/main/java/swing/ConsultaEventoPanel.java`
- `estacion-trabajo/src/main/java/swing/ModificarUsuarioPanel.form`
- `estacion-trabajo/src/main/java/swing/ModificarUsuarioPanel.java`
- `estacion-trabajo/src/main/java/swing/RegistroEdicionEventoPanel.java`
- `estacion-trabajo/src/main/java/swing/SelectorImagen.java`
- `how --stat cb4d004`
- `logica/src/test/java/implementacion/EventosYCategoriasTest.java`
- `src/clases/Categoria.java`
- `src/clases/Evento.java`
- `src/datatypes/DtEvento.java`
- `src/datatypes/DtUsuario.java`
- `src/implementacion/Sistema.java`
- `src/interfaces/ISistema.java`
- `src/main/java/datatypes/DtEdicion.java`
- `src/main/java/datatypes/DtRegistro.java`
- `src/main/java/implementacion/Sistema.java`
- `src/main/java/interfaces/ISistema.java`
- `src/main/java/manejadores/ManejadorRegistros.java`
- `src/main/java/swing/AltaEdicionEventoPanel.java`
- `src/main/java/swing/ConsultaEdicionEventoPanel.java`
- `src/main/java/swing/ConsultaEventoPanel.java`
- `src/main/java/swing/ConsultaRegistroPanel.java`
- `src/main/java/swing/principal.java`
- `src/main/webapp/css/styles.css`
- `src/main/webapp/index.html`
- `src/main/webapp/js/datos.js`
- `src/main/webapp/js/evento.js`
- `src/main/webapp/js/modificar-usuario.js`
- `src/main/webapp/js/usuario.js`
- `src/main/webapp/pages/alta-evento.html`
- `src/main/webapp/pages/evento.html`
- `src/main/webapp/pages/modificar-usuario.html`
- `src/manejadores/ManejadorEventos.java`
- `src/swing/AltaCategoriaPanel.java`
- `src/swing/AltaEventoPanel.java`
- `src/swing/AltaUsuarioPanel.java`
- `src/swing/ConsultaEventoPanel.java`
- `src/swing/principal.java`
- `webapp/src/main/java/web/AltaEventoServlet.java`
- `webapp/src/main/java/web/EventoServlet.java`
- `webapp/src/main/webapp/WEB-INF/views/alta-evento.jsp`
- `webapp/src/main/webapp/WEB-INF/views/evento.jsp`

</details>

### Adrian Prado

4 commits propios; ultimo aporte por fecha de autor: 2026-10-26T01:36:29-03:00; 37 rutas historicas afectadas.

Referencias que contienen sus commits (no todas son ramas personales): `local/ConsultaeventoYAltacat`, `local/Registros_Y_Eventos`, `local/Revision4`, `local/altaEvento`, `local/dev`, `local/feature/clases_faltantes`, `local/feature/correcciones-logica`, `local/feature/instituciones-patrocinios`, `local/feature/jpa-entidades`, `local/feature/persistencia`, `local/feature/persistencia-jpa`, `local/lab2-cobertura`, `local/lab2-consultas-edicion-tipo`, `local/lab2-integracion-equipo`, `local/lab2-logica-estado-edicion`, `local/lab2-logica-login`, `local/lab2-patrocinios-web`, `local/lab2-seguridad-patrocinios`, `local/lab2-swing`, `local/lab2-swing-datos`, `local/lab2-web-infraestructura`, `local/lab2-web-institucion`, `local/lab2-web-usuarios`, `local/lab3-desarrollo`, `local/restructura-modulos`, `local/revision`, `local/swingEdicionEventos`, `origin/ConsultaeventoyAltacat`, `origin/Revision2`, `origin/Revision6`, `origin/caso_consultaDeRegistro`, `origin/consultaeventoarreglado`, `origin/dev`, `origin/feature/casos_adrian`, `origin/feature/instituciones-patrocinios`, `origin/feature/persistencia`, `origin/feature/registros-joaquinb`, `origin/feature/swing-usuario-imagenes`, `origin/feature/tests-eventos-categorias`, `origin/lab2-Emmanuel`, `origin/lab2-cobertura`, `origin/lab2-emmanuel-tests`, `origin/lab2-swing-datos`, `origin/lab2-web-usuarios`, `origin/lab3-desarrollo`, `origin/lab3-emmanuel`, `origin/swingEdicionEventos`.

| Commit | Fecha autor | Asunto | Archivos afectados |
| --- | --- | --- | --- |
| `65cfcb3f7674a8e2b6ebda81bb4977f110bb5a8c` | 2026-10-26T01:36:29-03:00 | Casos de AltaPatrocinio/ConsultaPatrocinio y su respectivo test | 8 |
| `cdca1412603f9021b2033c87d14d1151d5958138` | 2026-10-10T01:02:02-03:00 | Agrego los casos de uso para login/logout y alta/consulta usuario, queda por implementar en consulta la redireccion a las ediciones | 11 |
| `e4bbbf209918e89af5b150aa83df4d52414d07ff` | 2026-08-29T19:54:18-03:00 | Agregué registro a edición de evento, consulta de registro,alta a edicion de evento y consulta edicion de evento, modifique ModificarUsuarioPanel para intentar correr pero esta incorrecto | 17 |
| `b5a78d3c79836ac63424f82b201d40418a14e731` | 2026-08-24T19:02:08-03:00 | Agrego el swing de Consulta Usuario | 5 |

<details>
<summary>Listado completo de rutas historicas afectadas</summary>

- `contexto_registro.txt`
- `logica/src/main/java/implementacion/Sistema.java`
- `logica/src/main/java/interfaces/ISistema.java`
- `logica/src/main/java/manejadores/ManejadorPatrocinios.java`
- `logica/src/test/java/implementacion/PatrocinioTest.java`
- `proyecto_completo.txt`
- `src/clases/Asistente.java`
- `src/clases/Edicion.java`
- `src/clases/Evento.java`
- `src/clases/Registro.java`
- `src/datatypes/DtRegistro.java`
- `src/implementacion/Sistema.java`
- `src/interfaces/ISistema.java`
- `src/main/webapp/css/styles.css`
- `src/main/webapp/index.html`
- `src/main/webapp/js/app.js`
- `src/main/webapp/js/consultaUsuario.js`
- `src/main/webapp/js/login.js`
- `src/main/webapp/js/usuario.js`
- `src/main/webapp/pages/alta-usuario.html`
- `src/main/webapp/pages/login.html`
- `src/main/webapp/pages/logout.html`
- `src/main/webapp/pages/usuario.html`
- `src/main/webapp/pages/usuarios.html`
- `src/manejadores/ManejadorEventos.java`
- `src/manejadores/ManejadorUsuarios.java`
- `src/swing/AltaEdicionEventoPanel.java`
- `src/swing/ConsultaEdicionEventoPanel.java`
- `src/swing/ConsultaTipoRegistroPanel.java`
- `src/swing/ConsultaUsuarioPanel.java`
- `src/swing/ModificarUsuarioPanel.java`
- `src/swing/RegistroEdicionEventoPanel.java`
- `src/swing/principal.java`
- `webapp/src/main/java/web/AltaPatrocinioServlet.java`
- `webapp/src/main/java/web/ConsultaPatrocinioServlet.java`
- `webapp/src/main/webapp/WEB-INF/views/alta-patrocinio.jsp`
- `webapp/src/main/webapp/WEB-INF/views/consulta-patrocinio.jsp`

</details>

## Cierre

No hay nuevas funcionalidades remotas completas esperando una integracion masiva. Quedan una subfuncion movil reutilizable de Bentancor y documentos antiguos pendientes de evaluacion; las principales funciones nuevas del Lab3 siguen sin implementacion completa publicada. La responsabilidad nominal de los puestos 2-5 no esta establecida en la documentacion inspeccionada. El siguiente paso debe aclarar ese reparto y desarrollar los bloques faltantes, preservando la base integrada.
