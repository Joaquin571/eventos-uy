# Auditoría del Lab 3 y observaciones de la defensa

Fecha: 09/11/2026. Base inspeccionada: `a29ee1f`. Auditoría de código e historial local; no se implementaron correcciones funcionales, no se ejecutaron migraciones ni se conectó PostgreSQL.

## 1. Estado actual del Lab 3

### Git y alcance de la evidencia

- Rama actual: `lab3-desarrollo`, upstream `origin/lab3-desarrollo`, ambos en `a29ee1f`; diferencia 0/0.
- `dev` y `origin/dev` apuntan a `5fb46db` (entrega final Lab 2). `git rev-list --left-right --count dev...HEAD`: 0/3. Los tres commits de Lab 3 son lineales, sin merges nuevos:
  1. `6bdfb17`: requisitos, roadmap y referencia técnica (05/11).
  2. `586eab4`: primer corte SOAP, configuración y pruebas (06/11).
  3. `a29ee1f`: contratos SOAP y reparto por dominios (06/11).
- Únicas referencias Lab 3 encontradas: la rama actual y su tracking remoto. Las ramas propuestas `lab3-infra-soap`, `lab3-web-usuarios-patrocinios`, `lab3-social-eventos`, `lab3-registros-asistencia` y `lab3-movil-busqueda` no están creadas según las referencias disponibles.
- Existen ramas históricas `lab2-*`, `feature/*`, `dev`, `restructura-modulos`, `revision`/`Revision4`, `Documentación`, `Registros_Y_Eventos`, `altaEvento`, `ConsultaeventoYAltacat`, `swingEdicionEventos` y otras referencias remotas. No se integró ni se cambió ninguna rama. `origin/HEAD` señala `origin/dev`.
- Historial de integración Lab 2: `4880c2f` (Swing Valentina), `9cfd301` (tests Emmanuel), `489245c` (casos Adrián), `12bf4dd` (registros Joaquín B), PR #35/#34/#33/#32. Esos nombres históricos no prueban identidades del reparto numerado del Lab 3.
- No se hizo `fetch`: las referencias `origin/*` son las almacenadas localmente, no una comprobación en vivo del servidor GitHub.
- Estado inicial completamente limpio: sin cambios staged, unstaged ni archivos no rastreados visibles. `git diff` y `git diff --cached` sin cambios. Por tanto, no había funcionalidades pendientes de confirmar ni cambios sueltos del Lab 2/Lab 3.
- Ignorados presentes: `.idea/`, `.smarttomcat/`, los cuatro `target/`, `ProyectoWebServices/` y `Laboratorio 3.pdf`. Se mantienen fuera del commit. La letra se usa como fuente local, no se agrega con `git add -f`.
- No se encontraron archivos `AGENTS.md` en el inventario del proyecto. Documentación Lab 3 recuperada: REQUISITOS, ROADMAP, REFERENCIA_WEBSERVICES, CORTE-SOAP, CONTRATOS-EQUIPO y seis guías de EQUIPO. README y guías manuales web/Swing completan el contexto.

### Qué exige la consigna

Se contrastó el texto de las 14 páginas de `Laboratorio 3.pdf` con [REQUISITOS](REQUISITOS.md). No se hizo revisión visual de las maquetas; las obligaciones resumidas aquí proceden del texto de §§3–8.

| Área | Exigencia |
| --- | --- |
| Distribución | Central concentra lógica/JPA/PostgreSQL; Swing directo en el mismo proceso; Web separado consume exclusivamente SOAP y no incluye lógica/JPA. Defensa en cuatro dispositivos de la misma red. |
| SOAP | Jakarta XML WS 4/Metro; Endpoint.publish antes de Swing; fachadas delegan por Fábrica; DTO aptos JAXB, faults declarados, imágenes byte[], cliente wsimport, página propia ante indisponibilidad. |
| Configuración/paquetes | Properties externas en `~/.eventosUy`, incluidas conexión DB y dirección efectiva del cliente; Java 25, Maven, Tomcat 11; un comando genera `servidor.jar` y `web.war`; despliegue sin IDE. |
| Social/video | Seguir/dejar de seguir sin auto-seguimiento ni duplicados, mostrar seguidores/seguidos; video YouTube opcional y embebido en escritorio/móvil. |
| Estados | Finalizar evento lo oculta del sitio e impide nuevas ediciones; archivar edición propia Aceptada con fin anterior a hoy, guardar fecha y conservar registros; acceso por perfil según la letra. Swing sigue consultando finalizados/archivados. |
| Asistencia/PDF | Asistente confirma asistencia a registro propio desde móvil; no ofrecer confirmación repetida; constancia propia solo tras asistencia, generada a demanda, con nombre/apellido, evento/edición, ciudad, fechas y emisión. |
| Móvil | Mismo WAR, ruta propia, login solo asistentes, logout al login, navegación y consultas de edición/registro/asistencia; framework responsive local, menú y video 16:9. |
| Disponibilidad | Nickname/correo con fetch y async/await mientras se escribe, servlet consulta SOAP; lógica sigue rechazando duplicados. |
| Búsqueda/visitas | Nombre/descripción parcial sin distinguir mayúsculas; vacío lista elegibles; excluye finalizados/no Aceptadas; fecha descendente y nombre asc/desc. Filtro web informa visitas por SOAP; persistencia y top cinco Swing. |
| Verificación/entrega | Conservar regresión, JUnit de nuevos casos y ≥80% líneas de lógica medido en IntelliJ; análisis/diseño/DSS, README e instalación/datos demo. Entrega Moodle 23/11/2026 09:00. |

### Clasificación del avance

| Estado | Evidencia y límites |
| --- | --- |
| Terminado documental | Requisitos trazados y reparto/contratos existentes ya confirmados en Git. Las decisiones P01–P08 siguen pendientes, no son contratos implementados. |
| Implementado y comprobado automáticamente | `InicioServlet.doGet → ClienteEventosCentral.listarEventos → EventosPublicador.listarEventos → ISistema`. DTO EventoSoap, WSDL/XSD local, wsgen/wsimport, configuración del destino, faults y HTTP 503. Tests actuales prueban HTTP SOAP real con doble de lógica y Servlet con request/response simulados. |
| Parcial | Módulo Central, Main publica antes de abrir Swing y POM prepara JAR; configuración externa inicial y generación de cliente. No se verificó en esta auditoría el JAR empaquetado ni despliegue real. WAR conserva dependencia `logica`; ContextoListener inicializa JPA y FiltroAcceso obtiene categorías localmente. Resto de recorridos usa ISistema local. No cumple todavía la separación final. |
| Sin implementación encontrada | Seguimiento, video, finalización, ARCHIVADA/fecha de archivo, asistencia, constancia PDF, disponibilidad remota asincrónica, búsqueda nueva, visitas/ranking, versión móvil propia y cobertura de los nuevos casos. Se inspeccionaron modelo, ISistema, Sistema, rutas web y documentación; ausencia referida a esta rama, no a trabajo externo sin integrar. |
| No comprobado | Persistencia real de Lab 3, cobertura actual de lógica, Tomcat/Swing visual, arranque sin IDE ni ensayo LAN con dos/cuatro equipos. No usar métricas históricas de Lab 2 como cierre de Lab 3. |

## 2. Qué hicimos hasta ahora

Los tres commits anteriores son todo el avance integrado del Lab 3 respecto a `dev`. Las altas, registros, estados Ingresada/Aceptada/Rechazada, subida de imágenes y controles web auditados son herencia del Lab 2; sus defectos no aparecieron por SOAP. El commit SOAP también adapta `BaseDeDatos.inicializar(Map)` para el Central, sin convertir el resto de la web a remoto.

En esta sesión: reconstrucción de Git y consigna, auditoría de los 19 Servlet y helpers de acceso, recorrido de transacciones/imágenes web y Swing, ejecución selectiva de regresión sin DB y este informe. Ningún cambio de código productivo o tests.

## 3. Distribución original recuperada

Fuente: [CONTRATOS-EQUIPO](CONTRATOS-EQUIPO.md), que declara reparto por dominios aprobado, y [guías individuales](EQUIPO/GUIA-GIT.md). No se recuperó evidencia para asociar Integrantes 2–5 con nombres reales. Se conserva la identificación original.

| Responsable documentado | IDs originales y alcance |
| --- | --- |
| Integrante 1: Joaquín | J01 contratos/identidad/faults; J02 configuración/ciclo de vida/paquetes; J03 imágenes SOAP; J04 persistencia de visitas y ranking Swing; J05 integración/cobertura/documentación/demo/despliegue. |
| Integrante 2 | W01 sesión/login/filtros/listener; W02 usuarios/perfiles; W03 instituciones/patrocinios; W04 disponibilidad; W05 retirar lógica/JPA de Web. |
| Integrante 3 | D01 eventos/ediciones/tipos por SOAP; D02 seguimiento; D03 video; D04 finalizar/visibilidad; D05 modelo/DSS/demo de su dominio. |
| Integrante 4 | R01 registros SOAP; R02 asistencia; R03 archivo; R04 PDF; R05 evidencias/modelo/DSS/demo. |
| Integrante 5 | M01 entrada/login/logout/navegación móvil; M02 consultas/asistencia móvil; M03 búsqueda; M04 filtro de visitas; M05 responsive/prueba física/documentación. |

El roadmap inicial es propuesta histórica; el reparto posterior de contratos/guías es la evidencia más reciente. No se encontraron aprobaciones posteriores que cierren P01–P08 ni PR/commits funcionales de esos bloques en Lab 3.

## 4. Distribución actualizada con las observaciones de la defensa

Las filas originales conservan su responsable. Las ampliaciones A01–A08 son propuestas derivadas del dominio ya asignado; no acreditan aceptación personal posterior. Estados referidos a implementación, no a haber escrito una guía. Rutas abreviadas: L=`logica/src/main/java`, W=`webapp/src/main/java/web`, S=`estacion-trabajo/src/main/java/swing`, C=`servidor-central/src/main/java/central`. Las pruebas van en `src/test` del módulo correspondiente.

| Tarea | Responsable | Módulo | Archivos involucrados | Estado | Dependencias | Criterio de aceptación |
| --- | --- | --- | --- | --- | --- | --- |
| J01: cerrar contratos, identidad y faults | Joaquín con dueños | logica / webapp; servidor-central | L/interfaces/ISistema.java; C/soap; W/soap | En progreso | P01–P03 | Contratos y autorización acordados, WSDL compatible y consumidor regenerable. |
| J02: completar configuración y paquetes | Joaquín | logica / estacion-trabajo / webapp; servidor-central | POM; L/persistencia/BaseDeDatos.java; persistence.xml; C/Main.java, ConfiguracionCentral.java | En progreso | P08, W05 para cierre | JAR/WAR finales en máquinas sin IDE y sin configuración incrustada. |
| J03: imágenes SOAP | Joaquín | logica / webapp; servidor-central | L/implementacion/Sistema.java; L/manejadores/ManejadorImagenes.java; W/ImagenServlet.java, Subidas.java; C/soap | Pendiente | J01, A06 | Bytes/validación/privacidad remotos y operaciones compuestas atómicas. |
| J04: visitas y top cinco | Joaquín | logica / estacion-trabajo | L/clases/Evento.java; Sistema, ISistema, ManejadorEventos; S/principal.java y panel nuevo | Pendiente | P06, M04 | Conteo concurrente persistente y tabla top cinco correcta tras reinicio. |
| J05: integración, cobertura y entrega | Joaquín coordina; todos aportan | logica / webapp / estacion-trabajo | README, docs, L/datos/CargarDatosPrueba.java, tests | Pendiente | Bloques funcionales | ≥80% IntelliJ, datos completos, cuatro dispositivos sin IDE. |
| W01: sesión y acceso SOAP | Integrante 2 | webapp; servidor-central | W/LoginServlet.java, LogoutServlet.java, FiltroAcceso.java, ContextoListener.java; C/soap | Pendiente | J01/P01/P03 | Sesión sin DTO lógico; CSRF, renovación, roles y errores conservados. |
| W02: usuarios/perfiles SOAP | Integrante 2 | logica / webapp; servidor-central | W/AltaUsuarioServlet.java, ModificarUsuarioServlet.java, UsuarioServlet.java, UsuariosServlet.java; L/Sistema y ManejadorUsuarios en sus paquetes | Pendiente | W01, A04, A06 | Recorridos/privacidad y perfil+clave+imagen atómicos; sin advertencia de éxito parcial. |
| W03: instituciones/patrocinios SOAP | Integrante 2 | logica / webapp; servidor-central | W/AltaInstitucionServlet.java, AltaPatrocinioServlet.java, ConsultaPatrocinioServlet.java, AccesoPatrocinios.java; manejadores de dominio | Pendiente | J01, A04, A06 | Propiedad/códigos privados en lógica y alta con imagen en una transacción. |
| W04: disponibilidad asincrónica | Integrante 2 | webapp / logica; servidor-central | AltaUsuarioServlet/formulario; nuevo servlet/JS; C/soap | Pendiente | W01/J01 | fetch+async/await SOAP y rechazo de duplicados al alta. |
| W05: retirar lógica local del WAR | Integrante 2; revisa Joaquín | webapp | pom.xml, ContextoListener, FiltroAcceso, imports y tests web | Pendiente | Todas las migraciones | WAR sin lógica, JPA ni driver; compilación y regresión reproducibles. |
| D01: migración eventos/ediciones/tipos | Integrante 3 | logica / webapp; servidor-central | W/EventoServlet.java, AltaEventoServlet.java, AltaEdicionServlet.java, AltaTipoRegistroServlet.java, ConsultaEdicionServlet.java, ConsultaTipoRegistroServlet.java, AccesoEdiciones.java; C/soap | En progreso | J01, A02/A03/A05 | Solo portada remota hoy; cierre requiere todos los recorridos sin reglas web. |
| D02: seguimiento | Integrante 3 | logica / webapp | L/clases/Usuario.java, Sistema, ISistema, DTO; W/UsuarioServlet.java, UsuariosServlet.java y JSP | Pendiente | W02/J01 | Persistencia; no auto-seguimiento/duplicados; seguir/dejar y perfiles correctos. |
| D03: video | Integrante 3 | logica / webapp | L/clases/Edicion.java, datatypes/DtEdicion.java; W/AltaEdicionServlet.java, ConsultaEdicionServlet.java y JSP | Pendiente | J01, coordinación R03/M03 | Alta con/sin video; embed escritorio/móvil y persistencia. |
| D04: finalizar y visibilidad | Integrante 3 | logica / webapp / estacion-trabajo | Evento, Sistema, ISistema, ManejadorEventos; EventoServlet, AccesoEdiciones; S/ConsultaEventoPanel.java | Pendiente | P04, A02/A05 | Oculto públicamente, bloquea alta edición, visible al administrador. |
| D05/R05: análisis, DSS y demo de dominios | Integrantes 3/4, cada uno en su bloque | logica / webapp / estacion-trabajo | docs, L/datos/CargarDatosPrueba.java | Pendiente | Casos D/R | Documentación y datos coinciden con comportamiento real. |
| R01: registros SOAP | Integrante 4 | logica / webapp; servidor-central | W/RegistroEdicionServlet.java, ConsultaRegistroServlet.java; L/Sistema y ManejadorRegistros en sus paquetes | Pendiente | J01, A05 | Cupos/costos/códigos y privacidad desde lógica; HTTP conserva PRG. |
| R02: asistencia | Integrante 4 | logica / webapp | L/clases/Registro.java, datatypes/DtRegistro.java, Sistema, ISistema; ConsultaRegistroServlet y ConsultaEdicionServlet | Pendiente | R01/J01 | Propio, persistente; ajeno/inexistente rechazado; repetición controlada. |
| R03: archivar edición | Integrante 4; coordina 3 | logica / webapp / estacion-trabajo | L/clases/Edicion.java, EstadoEdicion, DtEdicion, ManejadorEventos; consultas web/Swing | Pendiente | J01/P08, D04 para visibilidad conjunta | Propia Aceptada, fin < hoy, fecha archivo; no borra registros. |
| R04: constancia PDF | Integrante 4 | logica / webapp | Contrato autorizado; nuevo controlador PDF y vistas; pom.xml | Pendiente | R02/P07 | Solo propia con asistencia; application/pdf, campos completos y generación a demanda. |
| M01/M02: móvil y recorridos | Integrante 5 | webapp | Ruta/vistas móviles nuevas; LoginServlet, LogoutServlet, ConsultaEdicionServlet, ConsultaRegistroServlet | Pendiente | W01, D01/D03, R01/R02 | Solo asistentes y datos reales SOAP; navbar, logout y confirmación funcionales. |
| M03: búsqueda | Integrante 5 | logica / webapp | Sistema, ISistema, ManejadorEventos, DTO; cabezal y servlet/vista nuevos | Pendiente | P05, D04/R03 | Nombre/descripción, vacío, estados, tres órdenes y enlaces comprobados. |
| M04: filtro visitas | Integrante 5 | webapp | Nuevo Filter; EventoServlet/mappings móviles solo como referencias de rutas | Pendiente | J04/P06/J01 | Informa por SOAP en ambos contextos sin conteo en Servlet ni doble dispatch. |
| M05: responsive y teléfono | Integrante 5 | webapp | Vistas/CSS/vendor y docs | Pendiente | M01/M02 | Dispositivo físico documentado, sin desbordes y video 16:9. |
| A01: completar revisión transversal de responsabilidades | Joaquín coordina; 2/3/4 corrigen sus dominios | logica / webapp | Los 19 Servlet y helpers inventariados en §5 | En progreso | Inventario completado; A02–A05 para cierre | Cada regla tiene un dueño en lógica; ningún Servlet decide negocio. |
| A02: corregir consulta de evento | Integrante 3, revisión Joaquín | logica / webapp | W/EventoServlet.java#doGet; L/interfaces/ISistema.java, implementacion/Sistema.java, manejadores/ManejadorEventos.java | Pendiente | J01/P03 para contexto remoto | Consulta por clave y visibilidad centralizada; 404 y formato conservados. |
| A03: corregir alta tipo de registro | Integrante 3 | logica / webapp | W/AltaTipoRegistroServlet.java#doPost; Sistema.altaTipoRegistro; ManejadorEventos.addTipoRegistro | Pendiente | J01/P03 | Propiedad y rangos comprobados en lógica incluso al omitir web; Swing administrativo sigue válido. |
| A04: refactor usuarios/instituciones/patrocinios | Integrante 2 | logica / webapp | Servlet W02/W03 y AccesoPatrocinios; Sistema y manejadores | Pendiente | J01, A06 | Dominio/privacidad centralizados; parsing, sesión, PRG y mensajes permanecen web. |
| A05: refactor consultas/registro/visibilidad | 3: ediciones/tipos; 4: registros; 2: perfil/patrocinio | logica / webapp | AccesoEdiciones, AccesoPatrocinios, ConsultaEdicionServlet.prepararRegistros, ConsultaRegistroServlet, RegistroEdicionServlet, UsuarioServlet | Pendiente | J01/P03; A02 | Permisos por actor en lógica, mismo acceso público/propio y sin filtrar DTO privados. |
| A06: unidad transaccional con imagen | Joaquín: mecanismo/J03; 2: usuarios/instituciones; 3: eventos/ediciones | logica / webapp | Sistema/ISistema; ManejadorUsuarios/Eventos/Instituciones/Imagenes; cinco flujos web de §5 | Pendiente | Diseño de operación compuesta, coordinación J01 | Alta/modificación y asociación en un EM/tx; error revierte entidad, imagen y clave cuando corresponda. |
| A07: adaptar altas/modificación Swing | Backend 2/3 por dominio con Joaquín; responsable específico de adaptación Swing pendiente | logica / estacion-trabajo | S/AltaUsuarioPanel.java, AltaEventoPanel.java, AltaEdicionEventoPanel.java, AltaInstitucionPanel.java, ModificarUsuarioPanel.java, ImagenSwing.java | Pendiente | A06 | Una invocación atómica por caso; no mensaje ni reintento de alta parcial. |
| A08: regresión y transaccionalidad | Cada dueño 2/3 en su dominio; Joaquín coordina; 4 aporta regresión de consultas | logica / webapp / estacion-trabajo | Tests ImagenesTest, ModificacionPerfilAtomicaTest, nuevos tests de unidad de negocio; AltasPrgTest y suites de permisos | Pendiente | A02–A07; base segura | Fallo real de persistencia imagen/datos, lectura con EM nuevo, sin huérfanos; regresión HTTP y Swing documentada. |

Conflictos explícitos: J03 no puede resolver atomicidad publicando `alta` y `asociarImagen` como dos llamadas; coordinar API con 2/3. Perfil W02/D02, edición D03/R03/M03, Evento D04/J04/M03 y consultas de registros 3/4 son compartidos. No reformatear Sistema/ManejadorEventos enteros. No inferir un dueño de Swing de commits históricos.

## 5. Problemas arquitectónicos identificados

### Inventario completo de Servlet

Todas las rutas siguientes están bajo `webapp/src/main/java/web/`. Las propuestas de operaciones nuevas son responsabilidades a diseñar, no firmas existentes.

| Archivo y métodos | Hallazgo y destino propuesto |
| --- | --- |
| EventoServlet.doGet | Busca por nombre recorriendo listarEventos; no existe consultarEvento en ISistema. Regla de ediciones públicas selecciona ACEPTADA en web. Agregar consulta por clave/proyección pública en lógica sobre ManejadorEventos.obtenerEvento y resolver visibilidad allí. Fecha formateada, atributos, 404 y forward siguen web. No es una escritura ni una transacción web. |
| AltaTipoRegistroServlet.doPost | Líneas 65–68 actuales pertenecen a obtenerEdicionesOrganizador/stream; el bloque 64–78 decide propiedad por anyMatch. Igual bloque en `dev` de la defensa. Sistema.altaTipoRegistro valida existencia, duplicado, costo/cupo, pero no recibe actor ni valida propiedad. Diseñar variante de operación autorizada para web; conservar contexto administrativo Swing. Parsing Float/Integer sigue web; costo finito/no negativo y cupo positivo son negocio, hoy duplicado. |
| AltaEventoServlet.doPost | Campos/categorías obligatorios duplican Sistema.altaEvento; fecha efectiva ya la fija lógica. Además alta+imagen separadas. Conservar multipart, colección de parámetros, redirect/formulario; operación compuesta en lógica. |
| AltaEdicionServlet.doPost | Orden fin/inicio duplicado con Sistema.altaEdicion; fecha efectiva ya la fija lógica. Alta+imagen separadas. Parseo LocalDate y error de formato HTTP permanecen. |
| AltaInstitucionServlet.doPost | Decide duplicado mediante consulta previa y aplica obligatoriedad/regex URL del dominio. Sistema.altaInstitucion no reproduce exactamente toda esa política de URL/obligatoriedad web: acordar regla canónica conservando el caso, no copiarla a ciegas. Alta+imagen separadas. |
| AltaUsuarioServlet.armarAsistente/armarOrganizador/doPost | Apellido/descripción/nacimiento futuro duplicados con lógica; alta+imagen separadas. Selección/parsing del formulario queda web; validar subtipo/dominio en operación lógica. |
| ModificarUsuarioServlet.armarAsistente/armarOrganizador/doPost | Repite obligatoriedad y nacimiento futuro; perfil/clave se confirman antes de imagen. Actualización de sesión queda HTTP, solo después del éxito completo. |
| AltaPatrocinioServlet.doPost/mostrarFormulario | Relaciones evento–edición y tipo–edición, propiedad y rangos económicos se deciden en web/helper; parte duplicada en lógica. Centralizar operación autorizada y consultas de opciones elegibles; parsing enum/números y reconstrucción del formulario permanecen. |
| ConsultaPatrocinioServlet.doGet | Visibilidad/propiedad y supresión del código privado en DTO se deciden en web. Lógica debe devolver proyección autorizada; formato de monto/fecha sigue web. |
| ConsultaEdicionServlet.doGet/prepararRegistros | Usa AccesoEdiciones y decide qué registros mostrar y si puede registrarse. Mover autorización/proyección a lógica; nombres de atributos y formato son presentación. |
| ConsultaTipoRegistroServlet.doGet | AccesoEdiciones.resolver determina elegibilidad/visibilidad y relación de selección. Solicitar consulta autorizada en lógica; ausencia de parámetro/404 y formato de costo permanecen web. |
| ConsultaRegistroServlet.consultaDelOrganizador/consultaDelAsistente | Propiedad de edición, registros propios y detalle permitido se resuelven localmente. Operaciones lógicas autorizadas; formateo/nombre completo y mensajes HTTP permanecen web. |
| RegistroEdicionServlet.doPost/mostrarFormulario/eventoDeLaEdicion | Existencia de tipo, modalidad/código requerido y ediciones elegibles están en web; costo y fecha reales ya los fija lógica del registro. No trasladar formato de costos ni selección visual; concentrar reglas en operación lógica y reutilizar consultas elegibles. |
| UsuarioServlet.doGet | Limita registros al perfil propio y ediciones públicas al estado ACEPTADA; Central debe filtrar datos por actor. Formatos de fecha/costo y flags de vista quedan web. |
| ImagenServlet.doGet | AccesoEdiciones protege imagen de edición solo en web; Central debe comprobar visibilidad antes de entregar bytes. Content-Type, caché, nosniff, longitud y stream son HTTP. |
| InicioServlet.doGet | Único consumidor SOAP actual. Filtra categorías de eventos recibidos: consulta funcional candidata a lógica al ampliar contrato. No romper listarEventos sin parámetros ni la consulta completa local de Swing. 503 y forward permanecen web. |
| UsuariosServlet.doGet | Agrupa asistentes/organizadores para dos listas de vista. No se identificó regla de negocio que obligue a mover esa agrupación; migrar origen a SOAP manteniendo presentación. |
| LoginServlet.doPost | Autentica delegando a lógica; invalida y crea HttpSession. Responsabilidad HTTP correcta; adaptar transporte, no trasladar HttpSession al Central. |
| LogoutServlet.doGet/doPost | Invalidación de sesión, POST/405 y redirect: HTTP correcto; no necesita operación SOAP por inercia. |

Helpers también auditados: `AccesoEdiciones.esVisible/esPropietario/resolver/aceptadas` y `AccesoPatrocinios.esVisible/esPropietario/esEdicionPropia/buscarEdicion` contienen política/reconstrucción del dominio; moverlos a otro helper web no resuelve la separación. `Subidas` debe seguir leyendo Part y controlando tamaño HTTP; `ValidadorImagen` conserva validación de bytes/formato/decodificación. FiltroAcceso/CSRF y sesión mantienen barreras web, sin ser la única autorización de las operaciones protegidas del Central.

### Atomicidad de imágenes: problema confirmado por recorrido de código

| Caso | Secuencia persistente independiente | Consecuencia |
| --- | --- | --- |
| Alta usuario (ambos subtipos) | Sistema.altaAsistente/altaOrganizador → ManejadorUsuarios.addUsuario commit; luego asociarImagen → ManejadorImagenes.asociar commit | Usuario puede quedar sin imagen si falla el segundo paso. |
| Alta evento | Sistema.altaEvento → ManejadorEventos.addEvento commit; luego asociarImagen | Evento creado aunque falle imagen. |
| Alta edición | Sistema.altaEdicion → ManejadorEventos.addEdicion commit; luego asociarImagen | Edición creada aunque falle imagen; web incluso comunica éxito y advertencia. |
| Alta institución | Sistema.altaInstitucion → ManejadorInstituciones.addInstitucion commit; luego asociarImagen | Institución creada aunque falle imagen. |
| Modificar usuario | Sistema.modificarAsistente/modificarOrganizador → ManejadorUsuarios confirma perfil+clave; luego asociarImagen | Datos/clave nuevos con imagen anterior; catch RuntimeException declara modificación parcial. |

Los mismos cinco recorridos existen en Swing: AltaUsuarioPanel.registrarUsuario, altas Evento/EdicionEvento/Institucion mediante ImagenSwing.completarAlta, y ModificarUsuarioPanel.guardarCambios mediante ImagenSwing.asociar. No alcanza con corregir Servlet.

No se observó el orden inverso (imagen confirmada antes de alta fallida) en esos flujos. `ManejadorImagenes.asociar` busca entidad existente, asigna imagen y hace rollback de su propia transacción si falla: la sustitución individual no es por sí sola un defecto demostrado. Usuario, Evento, Edicion e Institucion ya tienen `@OneToOne(cascade=ALL, orphanRemoval=true)`: la entidad y su imagen pueden persistirse como agregado; esa cascada no une transacciones distintas.

`ManejadorUsuarios.modificarAsistente/modificarOrganizador` ya confirma datos y contraseña juntos, con rollback y tests `ModificacionPerfilAtomicaTest`; esos tests no incluyen imagen y no se ejecutaron hoy. `ImagenesTest` prueba asociación/recuperación con DB; su existencia tampoco acredita atomicidad compuesta.

### Diseño propuesto (sin implementación en esta auditoría)

1. Una operación de negocio por alta/modificación recibe datos e imagen opcional y contexto autorizado. Las llamadas locales y SOAP delegan en esa misma operación; ausencia de imagen al modificar conserva la actual, salvo acción explícita acordada.
2. Validar dominio y decodificar bytes antes de escribir; verificar relaciones/propiedad sensibles dentro de la unidad transaccional. Esto reduce fallos prevenibles, pero no sustituye rollback SQL.
3. Un solo EntityManager y EntityTransaction abarca datos, hash opcional e Imagen. Resolver asociaciones gestionadas en ese EM. En alta asignar imagen antes de persist y aprovechar cascada; en modificación cargar entidad gestionada y reemplazar asociación con orphanRemoval. No invocar manejadores que abran/cierren otra transacción dentro del caso compuesto.
4. Commit una vez; ante excepción de persistencia/flush/commit, rollback si está activa, cerrar EM y propagar error. No informar éxito parcial ni actualizar sesión como éxito. No compensar con un borrado posterior desde el Servlet.
5. Una petición SOAP para la operación compuesta; no transacción distribuida entre alta y otra llamada de imagen. Publicadores sin JPA. La asociación individual solo se conserva cuando sea un caso independiente justificado.
6. Tests futuros: imagen inválida deja cero entidades nuevas; fallo SQL de imagen tras flush de entidad revierte ambas; fallo de datos no deja imagen huérfana; cambio perfil+clave+imagen fallido conserva los tres valores originales; reemplazo exitoso elimina solo la imagen anterior; alta sin imagen y modificación sin imagen siguen válidas. Consultar después con EM nuevo, comparar filas/relaciones y probar ambos subtipos y cuatro clases con imagen. Incluir errores reales de persistencia, no solo mocks o validación preventiva.
7. Ejecutar esos tests únicamente con base independiente `EVENTOS_DB_NAME=eventosuy_test` y barrera `ProteccionBasePruebas`; no relajarla. Actualizar tests web que hoy esperan éxito parcial, después de acordar el comportamiento atómico pedido en defensa.

## 6. Cambios listos para commit y verificación

Antes de esta auditoría no había cambios nuevos para confirmar. Los avances SOAP y el reparto ya están confirmados; no recrear un commit de esos archivos.

Propuesta de commit documental en `lab3-desarrollo`:

- `docs/lab3/AUDITORIA-2026-11-09.md`: este informe, matriz, inventario y propuesta transaccional.
- `docs/lab3/CONTRATOS-EQUIPO.md`: reemplazar la instrucción de conservar fallos parciales por la tarea atómica y enlazar informe.
- `docs/lab3/EQUIPO/INTEGRANTE-2.md`: actualizar aceptación W02 para no conservar éxito parcial de imagen.
- Mensaje: `chore(lab3): auditar avance e incorporar observaciones de defensa`.
- Excluir todos los `target/`, IDE/Tomcat local, PDFs/demo de referencia, propiedades personales, DB/dumps, credenciales y compilados. No hubo cambios existentes que preservar fuera de estos documentos.
- Listo como documentación revisable; no significa que las correcciones propuestas estén implementadas. No commit, push ni merge hasta la confirmación solicitada para commit; push/merge no forman parte del encargo.

### Pruebas ejecutadas el 09/11/2026

Maven 3.10.0 usa Java 21 por defecto en esta máquina. Se seleccionó temporalmente Temurin 25.0.5 mediante JAVA_HOME/PATH del proceso; no se modificó configuración del usuario ni POM. El intento sandbox inicial falló al abrir bloqueos `.m2`; repetición autorizada fuera del sandbox satisfactoria.

```powershell
$env:JAVA_HOME='C:/Program Files/Eclipse Adoptium/jdk-25.0.5.8-hotspot'
$env:PATH="$env:JAVA_HOME/bin;$env:PATH"
mvn -o -pl webapp -am '-Dtest=ConfiguracionCentralTest,EventosPublicadorTest,InicioSoapTest,UsuarioFechaAltaTest,CsrfTest,ConsultasEdicionTest,AltasPrgTest,PatrociniosFlujoTest,PatrociniosAutorizacionTest,SubidasTest' '-Dsurefire.failIfNoSpecifiedTests=false' test
node --test webapp/src/test/js/*.test.cjs
```

- Maven BUILD SUCCESS: Central 6 y Web 79 = **85 JUnit**, cero fallos/errores/omitidas. Compilación de lógica, Swing, Central y Web; wsgen/wsimport. No es un package limpio ni verificación de artefactos finales.
- Node: **56 pruebas**, todas aprobadas. Primer intento sandbox dio spawn EPERM; repetición autorizada satisfactoria.
- Los errores simulados del publicador se registran durante tests negativos; no son fallos de suite. Metro advierte que monitoreo no está disponible.
- No se ejecutó suite JPA ni se inicializó base: las pruebas seleccionadas usan dobles de ISistema y endpoints HTTP efímeros. No se ejecutó `mvn test` general ni se fijó una variable DB para aparentar aislamiento.
- Sin cobertura de lógica medida en esta auditoría; sin pruebas visuales ni LAN. Atomicidad compuesta confirmada solo como defecto por inspección de límites transaccionales, no por provocar fallo real hoy.

## 7. Próximos pasos por prioridad

1. Revisar y confirmar este commit documental. Resolver identidad de integrantes 2–5 y responsable de adaptación Swing, sin cambiar el reparto por suposición.
2. Joaquín y dominios cierran J01/P01–P03 y diseño A06; acordar firmas de operaciones atómicas antes de publicar escrituras remotas. Conservar administración Swing y compatibilidad de listarEventos.
3. 3 corrige EventoServlet/AltaTipoRegistroServlet; 2/3 implementan atomicidad de sus dominios con Joaquín; integrar A07 y pruebas reales aisladas A08. Corregir regresión sin perpetuar advertencias de éxito parcial.
4. Migrar SOAP por dominios con autorización/proyecciones en lógica; completar A04/A05; luego W05 retira JPA/lógica del WAR. No declarar migrado un caso por tener un publicador sin consumidor.
5. Implementar D02–D04, R02–R04, W04, M01–M04 y J04 según dependencias; cerrar P04–P08 donde corresponda antes de decidir políticas/esquema/biblioteca.
6. Cobertura real, documentación/datos y paquetes finales; ensayo temprano LAN y cierre con cuatro dispositivos sin IDE.
