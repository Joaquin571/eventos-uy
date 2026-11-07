# Contratos y acuerdos de trabajo del equipo — Laboratorio 3

## 1. Alcance y estados

Reparto por dominios aprobado por el equipo. Base funcional compartida: commit `586eab4` (primer corte SOAP). Este documento no agrega operaciones ni modifica el WSDL existente.

- **EXISTENTE:** comprobado en código y disponible en la base indicada.
- **ACUERDO DE TRABAJO:** regla para preparar/revisar futuros Pull Requests.
- **PROPUESTA:** contrato futuro, todavía no implementado; su firma/XML requiere revisión antes de generarlo o consumirlo.
- **PENDIENTE DE APROBACIÓN:** decisión expresamente enumerada en la sección 11. No resolverla unilateralmente.

Fuente normativa: letra local del Laboratorio 3, §§3–8. Trazabilidad completa en [REQUISITOS](REQUISITOS.md); técnica de referencia en [REFERENCIA_WEBSERVICES](REFERENCIA_WEBSERVICES.md). La demo y el PDF no forman parte del producto ni de los commits. [ROADMAP](ROADMAP.md) documenta la propuesta inicial; [CORTE-SOAP](CORTE-SOAP.md) documenta el avance real posterior. No recrear módulos que ya existen.

## 2. Contrato existente que se conserva

| Elemento | Valor real |
| --- | --- |
| Publicador | `servidor-central/src/main/java/central/soap/EventosPublicador.java` |
| Namespace | `https://eventos.uy/soap/eventos` |
| Servicio / puerto / interfaz XML | `EventosService` / `EventosPort` / `EventosPublicos` |
| Operación | `listarEventos()`; sin parámetros; devuelve lista de `EventoSoap`; declara `ConsultaException`. |
| Campos de `EventoSoap` | `nombre`, `sigla`, `descripcion`, `fechaAlta` como String ISO opcional y lista `categorias` de String. |
| Fault | Elemento XML `ConsultaFault`; excepción generada del consumidor `ConsultaException_Exception`. |
| Consumidor | `InicioServlet` → `ClienteEventosCentral` → `web.soap.generado`, generado por wsimport. |
| Contrato local | `webapp/src/main/resources/wsdl/EventosService.wsdl` y `EventosService_schema1.xsd`. |

No renombrar estos elementos, cambiar tipos o ampliar el contrato mientras se trabaja en documentación. La propuesta de servicios de la sección 3 no autoriza una sustitución automática de este servicio.

La web sigue incluyendo lógica local para el resto de los casos. `ContextoListener` inicia JPA; `FiltroAcceso` consulta categorías localmente. La separación final todavía está pendiente, no es un cumplimiento ya alcanzado.

## 3. Servicios por dominio y responsables

Son agrupaciones propuestas, no seis clases que ya existan ni un servicio por caso de uso. Las rutas de futuros endpoints, namespaces, nombres XML y ubicación de DTO nuevos se cerrarán en J01. Conservar compatibilidad del primer servicio.

| Dominio propuesto | Responsable del contrato | Operaciones basadas en métodos reales de ISistema | Capacidades nuevas propuestas |
| --- | --- | --- | --- |
| Usuarios | Integrante 2; seguimiento: 3; revisión común: Joaquín | `autenticar`, `existeUsuario`, `existeCorreoElectronico`, `altaAsistente`, `altaOrganizador`, `modificarAsistente`, `modificarOrganizador`, `listarUsuarios`, `consultarUsuario` | Seguir, dejar de seguir, consultar seguidores/seguidos. |
| Eventos, ediciones, categorías y tipos | Integrante 3; archivo: 4 | `listarEventos` (única publicada), `altaEvento`, `listarCategorias`, `altaEdicion`, `obtenerEdicionesEvento`, `obtenerEdicionesOrganizador`, `altaTipoRegistro`, `obtenerTiposRegistroEdicion`, `consultarTipoRegistro` | Finalizar evento y archivar edición. |
| Instituciones y patrocinios | Integrante 2 | `existeInstitucion`, `altaInstitucion`, `listarInstituciones`, `altaPatrocinio`, `obtenerPatrociniosEdicion`, `consultarPatrocinio(edicion,institucion)` | No se agrega un caso nuevo en este bloque. |
| Registros y asistencia | Integrante 4 | `registroAEdicion` de cinco parámetros, `obtenerRegistrosAsistente`, `obtenerRegistrosEdicion` | Confirmar asistencia; datos autorizados para constancia. |
| Imágenes | Joaquín | `validarImagen`, `asociarImagen`, `obtenerImagen` | Adaptación remota, conservando validación y privacidad actuales. |
| Búsqueda y visitas | Integrante 5; persistencia de visitas: Joaquín | No hay operaciones actuales equivalentes en ISistema. | Buscar con orden; informar visita. Ranking Swing por llamada local; no necesita SOAP sin consumidor remoto. |

Los responsables implementarán sus fachadas delgadas cuando se autorice; Joaquín coordina convenciones y arranque, no escribe todos los publicadores. Las fachadas delegan en controladores obtenidos mediante `implementacion.Fabrica`, sin JPA ni reglas de negocio (§7.1).

### Operaciones que no deben publicarse por inercia

- Logout actual invalida `HttpSession`; no tiene llamada de lógica ni necesita SOAP por sí mismo.
- `cambiarContrasenia` existe localmente, pero el perfil actual usa `modificarAsistente/modificarOrganizador`. No dividir esas operaciones atómicas.
- `consultarPatrocinio(codigo)` no es la operación pública utilizada por `ConsultaPatrocinioServlet`.
- `altaCategoria`, `listarCategoriasFormateadas` y `aceptarRechazarEdicion` tienen consumidores Swing; conservarlos locales. Publicarlos requeriría justificar consumidor y permisos.
- No publicar dos sobrecargas con un mismo nombre XML ambiguo. `obtenerEdicionesEvento` tiene dos firmas Java; acordar una forma remota o nombres XML distintos. No borrar las sobrecargas locales de registro de cuatro/cinco parámetros.

## 4. Entradas y salidas a acordar

Los nombres de esta tabla describen contenidos; **no son clases nuevas existentes ni firmas aprobadas**. Los `Dt*` citados son referencias del modelo local, no clases que deban incluirse en el WAR.

| Operación o grupo | Entrada conceptual | Salida conceptual | Referencias locales reales |
| --- | --- | --- | --- |
| Autenticar | Identificador nickname/correo y contraseña. | Datos mínimos de usuario autenticado y rol; nunca contraseña/hash/confirmación. | `DtUsuario`, `DtAsistente`, `DtOrganizador`. |
| Alta/modificación de usuario | Datos del subtipo, contraseña/confirmación solo cuando corresponda; identidad propia según acuerdo de autenticación. | Resultado y/o datos actualizados; negocio conserva validaciones. | `altaAsistente`, `altaOrganizador`, `modificarAsistente`, `modificarOrganizador`. |
| Consulta/lista de usuarios | Nickname o consulta pública. | Proyección pública; datos privados solo en contexto autorizado. Relaciones de seguimiento por identificar. | `consultarUsuario`, `listarUsuarios`. |
| Disponibilidad | Nickname o correo. | Boolean disponible/no disponible, sin detalles internos. | `existeUsuario`, `existeCorreoElectronico`. |
| Eventos/categorías | Datos de alta o identificador. | `EventoSoap` existente; categorías con nombre y padre; ampliaciones futuras revisadas. | `DtEvento`, `DtCategoria`. |
| Ediciones | Datos de edición, evento y contexto del organizador; video opcional futuro. | Edición con fechas, organizador, estado; archivo/video cuando se implementen. | `DtEdicion`, `EstadoEdicion`. |
| Tipos de registro | Edición y nombre, o datos de alta. | Nombre, descripción, costo y cupo. | `DtTipoRegistro`. |
| Instituciones/patrocinios | Datos de institución/patrocinio y claves por nombre. | Datos de institución y patrocinio; código según autorización. | `DtInstitucion`, `DtPatrocinio`, `clases.NivelPatrocinio`. |
| Registro | Identidad autorizada, edición, tipo, datos de registro y código opcional. | Resultado; consultas con tipo, fecha, costo, identidad y uso de código, más asistencia futura. | `DtRegistro`; la lógica fija costo/fecha reales. |
| Asistencia | Registro/edición propios y confirmación. | Estado de asistencia; tratamiento de repetición acordado. | Nueva capacidad; no existe método actual. |
| Constancia | Identificación autorizada del registro propio con asistencia. | Datos de §7.5 para generar PDF en Web, propuesta pendiente de biblioteca. | Consultas reales de usuario, edición y registro; composición/operación específica por decidir. |
| Imagen | Tipo/clave y, para carga, bytes originales/MIME. | Bytes/MIME o ausencia; errores conocidos sin datos técnicos. | `DtImagen`, `TipoEntidadImagen`. |
| Búsqueda | Texto y orden. | Resultados con tipo evento/edición, nombre, referencia de navegación y fecha necesaria para ordenar. | Nueva capacidad; Edicion hoy no tiene descripción. |
| Visitas | Nombre de evento consultado. | Confirmación del incremento; ranking local: posición/nombre/cantidad. | Nueva capacidad; Evento hoy no tiene contador. |

## 5. Usuario y roles

**Existente:** la sesión guarda `DtUsuario`; roles web por `instanceof DtOrganizador/DtAsistente`; atributo request `rol` con `visitante`, `asistente`, `organizador`. Administrador usa Swing, no tiene login web. Móvil admite solamente asistentes (§5/§6.3).

**Dirección de diseño propuesta, pendiente P01:** separar datos de autenticación, perfil y sesión; una representación de sesión propia del Web, independiente de `logica.jar`, con nickname, nombre y rol explícito. Elegir en J01 si la respuesta remota usa un enum de rol o DTO polimórficos y cómo se proyectan en sesión. No implementar ambos enfoques en ramas distintas. Los textos actuales de rol de la presentación pueden preservarse mediante conversión.

Contraseñas y confirmaciones son solo entrada de operaciones que las necesitan. Ninguna salida, sesión o JSP debe guardarlas/devolverlas. Los DTO de consulta no deben adquirir getters de contraseña por conveniencia de JAXB.

## 6. Fechas, enumerados y JAXB

- Conservar `EventoSoap.fechaAlta` como String ISO `yyyy-MM-dd`, opcional, sin alterar su contrato existente.
- Propuesta común para nuevas fechas de calendario: String ISO y conversión localizada a `LocalDate`; no enviar directamente LocalDate sin adaptación JAXB ni formatear transporte como `dd/MM/yyyy`.
- La presentación formatea las fechas. Fechas de alta, registro, archivo y emisión las fija el sistema según el caso; no confiar en fechas de alta enviadas por formularios.
- Definir valores/nombres XML y opcionalidad al cerrar cada contrato. No compartir una entidad para reutilizar su enum. `NivelPatrocinio` está hoy en paquete `clases`, pero su equivalente de transporte puede ser un enum, no el JAR productivo.
- Estados locales actuales: INGRESADA, ACEPTADA, RECHAZADA. ARCHIVADA es una ampliación pendiente, no existe todavía.
- DTO de transporte escritos a mano: constructor público vacío y getters/setters, listas sin entidades; probar nulos, lista vacía, Unicode y fechas.
- Las listas en clases generadas pueden ofrecer un getter con lista mutable: no editar el generado para agregar setters.
- No cambiar costos float ni otras firmas existentes solo para homogeneizar DTO en esta etapa; cualquier cambio de representación requiere revisión y prueba de compatibilidad.

## 7. Excepciones, identidad y autorización

El corte actual tiene un fault declarado genérico para fallas de consulta. Se conserva. No convertirlo silenciosamente en el contrato de todos los errores de negocio.

Para futuras operaciones: errores de negocio conocidos → excepciones declaradas/faults y mensajes españoles útiles; errores técnicos inesperados → respuesta genérica e indisponibilidad controlada. No mostrar faultString arbitrario, SQL, Hibernate/JDBC, trazas, propiedades o credenciales. Los códigos/campos comunes de futuros faults son decisión P02.

Reglas de trabajo:

1. El usuario de una operación propia se obtiene de la sesión, no de un campo oculto manipulable. Mantener CSRF, roles, propiedad y PRG en Web.
2. Definir en J01 qué identidad/contexto recibe el Central y cómo lo verifica. Un nickname o rol enviado por el cliente no acredita identidad por sí solo. No habilitar escrituras protegidas sin cerrar P03.
3. No imponer JWT, OAuth, WS-Security u otra tecnología no exigida sin aprobación. Tampoco afirmar que SOAP proporciona autorización automáticamente.
4. Un organizador consulta registros/códigos solo de sus ediciones; asistente consulta sus registros y constancia; imagen privada respeta visibilidad. Autorizar GET y POST cuando corresponda, además de ocultar botones.
5. Móvil vuelve a comprobar que la sesión es de asistente; no admitir una sesión de organizador tomada del escritorio.
6. Swing conserva acceso administrativo directo a ISistema en el mismo proceso. Ese contexto local no convierte las operaciones HTTP en administrativas.
7. Las reglas transaccionales siguen en lógica. No reintentar automáticamente escrituras tras timeout: puede haberse confirmado la operación aunque no llegara la respuesta.

## 8. Imágenes y multipart

Transferir bytes originales y MIME con tipos simples/DTO; no rutas del disco ni entidades. Mantener formatos PNG/JPEG/GIF/WEBP, límites y decodificación existentes. La validación remota debe usar la lógica real; no sustituirla por validar solo extensión o `Content-Type` del navegador.

El servlet sigue leyendo `Part` mediante `Subidas` con límites y CSRF compatibles con multipart. Validar antes de dar de alta y conservar el tratamiento actual de fallos de asociación posteriores. Una respuesta SOAP no implica una transacción distribuida entre llamadas distintas.

Las imágenes de edición mantienen controles de acceso. El publicador delega en la lógica, y el cliente Web no obtiene imágenes mediante JDBC o archivos compartidos. Responsable J03, con revisión de visibilidad de 3/4.

## 9. Configuración y generación del cliente

Existente: Metro/JAX-WS 4.0.5, Java 25, wsgen en Central y wsimport en Web. `~/.eventosUy/central.properties` contiene endpoint y conexión JPA; `web.properties` contiene destino Central. No versionar los archivos personales con secretos. Las plantillas `.example` sí se versionan.

`Main` publica antes de abrir Swing. Central actual valida esquema; no crea tablas nuevas. El procedimiento de creación/evolución del esquema y eliminación de defaults de conexión pendientes pertenece a J02, sin ejecutar migraciones durante la preparación.

Flujo de contrato para PR posteriores:

1. Revisar firma, DTO, permisos y errores con responsable de servicio y consumidores.
2. Aprobar el cambio de contrato antes de implementar/generar.
3. Modificar fuente del publicador/DTO y compilar Central con wsgen.
4. Actualizar WSDL y todos sus XSD locales en el mismo PR. No incluir direcciones de la computadora personal como configuración efectiva del consumidor.
5. Compilar Web: wsimport genera clases en `target/generated-sources/wsimport`. No editar ni versionar esas clases.
6. Probar cliente generado contra publicador real y revisar el diff XML. Normalizar líneas vacías finales de snapshots sin cambiar contenido XML; wsgen puede agregarlas.
7. Comprobar cambio de IP mediante properties sin regenerar cliente. Construcción normal desde WSDL local, sin necesitar Central encendido.

Para repetir únicamente el corte sin persistencia, ver [CORTE-SOAP](CORTE-SOAP.md). No ejecutar suites PostgreSQL sin autorización y barrera JDBC previa sobre eventosuy_test.

## 10. Propiedad de archivos y revisión

- Joaquín coordina `ISistema`, configuración, POM, ciclo de vida, DTO/faults compartidos y generación; no obliga a que toda implementación espere una migración web completa.
- Integrante 2 mantiene sesión/roles, helpers de patrocinio y primera migración de perfiles. Integrante 3 agrega seguimiento a esos perfiles después de W02.
- Integrante 3 mantiene recorrido de edición; integrante 4 aporta asistencia/archivo en PR coordinados. Integrante 5 reutiliza datos/controladores para móvil.
- `Evento` cambia por D04/J04/M03; `Edicion` por D03/R03/M03. Integrar acuerdos de modelo pequeños antes de editar ramas largas en paralelo.
- No reorganizar/reformatear `Sistema.java` ni `ManejadorEventos.java` para resolver coordinación. Avisar qué métodos/secciones se modifican.
- No resolver conflictos en fuentes wsimport. Resolver fuentes/contrato, revisar y regenerar. PR siempre hacia lab3-desarrollo, nunca dev.

Guías individuales y procedimiento en [EQUIPO/GUIA-GIT](EQUIPO/GUIA-GIT.md).

## 11. Decisiones pendientes que requieren aprobación

| ID | Decisión | Responsable que presenta propuesta | Bloquea |
| --- | --- | --- | --- |
| P01 | Campos/tipo de respuesta de autenticación, usuario/rol de sesión y perfiles de transporte. | 2 con Joaquín; revisan 3/5. | W01/W02 y login móvil real. |
| P02 | Nombres XML, namespaces/rutas de servicios nuevos, DTO y esquema común de errores de negocio. | Dueños de dominio con Joaquín. | Publicación de operaciones nuevas; no bloquea tests locales de negocio. |
| P03 | Identidad remota verificable y frontera de confianza Web–Central para operaciones protegidas. | Joaquín con 2 y revisión del equipo. | Escrituras SOAP protegidas. |
| P04 | Autorización para finalizar evento: la letra no define propiedad del evento como sí lo hace para archivar edición propia. | 3; aclaración con docentes si hace falta. | Regla final de D04. |
| P05 | Fuente/modelado de descripción de edición para búsqueda; hoy no está en Edicion/DtEdicion. | 5 con 3/4. | Cierre de M03; no sustituir por descripción del evento sin acuerdo. |
| P06 | Conteo de consultas inválidas/refresh, dispatchers del filtro y desempates del ranking. | 1 y 5. | Aceptación conjunta J04/M04. No inventar visitantes únicos diarios. |
| P07 | Biblioteca PDF/licencia y composición u operación de datos autorizados; propuesta: generación en Web. | 4 con Joaquín. | Dependencia y descarga de R04. |
| P08 | Cambios de esquema, defaults para datos previos y procedimiento de instalación/evolución. | 1 con cada dueño de modelo. | Ejecución posterior de scripts o migraciones; no autorizada aquí. |

Archivo exige edición propia Aceptada y fin anterior a hoy; asistencia no tiene restricción temporal explícita en la letra. No agregar reglas por suposición. Toda aprobación se registra en PR/documentación antes de cambiar el contrato, sin atribuir a la letra decisiones del equipo.
