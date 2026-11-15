# Auditoría de Servlet y separación de capas — Eventos UY

> Auditoría histórica anterior a la integración del 15/11. Sus hallazgos sobre
> Web local, HTTP, sesiones pendientes y atomicidad fueron tratados en
> [el informe vigente](INTEGRACION-2026-11-15.md), con pruebas y commits.
> Se conserva el contenido original como trazabilidad, no como estado actual.


## 1. Alcance, evidencia y resultado cuantitativo

Actualización de cierre del **11/11/2026**, rama `lab3-desarrollo`, HEAD `a1a15e7`, incluyendo los cambios sin confirmar. Se contrastaron los fuentes, firmas, consumidores, diffs y pruebas de los cortes aprobados. El inventario original se conserva y se actualizan sus conclusiones con evidencia del árbol actual. Las referencias históricas de los hallazgos abiertos sirven para localizar el método; las de los cortes cerrados se renovaron. Para comandos y resultados de cierre, ver [ESTADO-ACTUAL.md](ESTADO-ACTUAL.md).

Frente a la primera versión de esta auditoría, quedaron centralizados **alta de tipo de registro, alta de patrocinio y consulta/visibilidad de patrocinios**. También quedó corregida la propagación del actor en Swing. **Ninguno de esos Servlet fue migrado a SOAP.** El Central publica eventos y sesiones por HTTPS, pero el login web continúa local y el cliente normal de portada sigue construyendo HTTP.

Se mantiene como referencia histórica `AUDITORIA-SERVLETS-ARQUITECTURA.md`; no se modificó ni se toman sus conclusiones antiguas sobre patrocinios como defectos todavía abiertos.

| Métrica | Cantidad | Criterio reproducible |
|---|---:|---|
| Servlet propios de producción del módulo | **19** | Clases que extienden `HttpServlet`, cruzadas con `@WebServlet` y `WEB-INF/web.xml` |
| Servlet con reglas B en su código o en helpers web que invocan | **12** | Columna B de §3; incluye duplicaciones y decisiones de visibilidad, no todo `if` |
| Servlet que llaman directamente a lógica local | **17** | Invocan `ContextoListener.sistema(req)` en su propio fuente; no hace falta importar `ISistema` para contar |
| Servlet que utilizan SOAP | **1** | `InicioServlet` → `ClienteEventosCentral.listarEventos()` |
| Servlet exclusivamente desacoplados de lógica local en su recorrido normal completo | **0** | El filtro consulta categorías localmente; el listener inicializa JPA para el WAR |
| Servlet con `EntityManager`, SQL/JDBC, manejadores o entidades JPA directos | **0** | No confundir DTO ni el enum `NivelPatrocinio` con entidades gestionadas |
| Servlet que requieren adaptación para completar la arquitectura objetivo | **19** | 12 con reglas B; 5 con sustitución de llamadas locales; portada con TLS/dependencia indirecta; logout con revocación remota |

Las cantidades se superponen: no sumar 12 + 17 + 1. Los 12 se enumeran en §3. `InicioServlet` y `LogoutServlet` son los únicos sin llamada local propia. Todos mantienen dependencia indirecta del WAR; por ejemplo, `/logout` admitido atraviesa la carga local de categorías. `/imagen` evita esa carga en el filtro, pero consulta lógica local en el propio Servlet. No se afirma que cualquier petición rechazada por CSRF o rol llegue a lógica.

No se encontraron Servlet administrativos separados en este módulo ni registros adicionales de Servlet en `web.xml` (solo páginas de error). La administración local corresponde a Swing. Los materiales externos al reactor y clases de Tomcat no son Servlet propios de Eventos UY. No se inventan endpoints móviles, AJAX, ranking o administración ausentes del módulo actual.

## 2. Inventario completo: rutas, métodos, usos y dependencias

Cada nombre de la primera columna es un archivo real bajo **`webapp/src/main/java/web/`**. GET/POST enumera overrides explícitos; no implica que los demás métodos estén implementados. Todos los POST atraviesan `FiltroAcceso` y `Csrf.validarPost` cuando se ejecutan dentro del WAR con su mapeo anotado. Las invocaciones directas de tests pueden omitir el filtro: no confundirlas con el recorrido desplegado.

Abreviaturas de pruebas: **AP** `webapp/src/test/java/web/AltasPrgTest.java`; **CE** `ConsultasEdicionTest.java`; **PA** `PatrociniosAutorizacionTest.java`; **PF** `PatrociniosFlujoTest.java`; **CS** `CsrfTest.java`; **UF** `UsuarioFechaAltaTest.java`; **IS** `InicioSoapTest.java`. Las siete están bajo ese mismo directorio. **MC** `webapp/src/test/manual/VerificarConsultasWeb.java`, **MP** `VerificarPatrociniosWeb.java`, **MS** `AuditarSeguridadWeb.java`: fuentes manuales con persistencia. MP tuvo 29 comprobaciones reales aprobadas en el bloque anterior; no se repitió en el cierre documental. MC y MS no se ejecutaron en este cierre. «Sin test dedicado localizado» no significa ausencia de pruebas de lógica o JS relacionadas.

| Servlet / ruta HTTP | GET / POST y caso de uso | Llamadas locales directas y otras dependencias | Sesión, rol y permisos actuales | SOAP y pruebas localizadas |
|---|---|---|---|---|
| `AltaEdicionServlet` `/alta-edicion` | GET formulario; POST alta de edición con imagen | `listarEventos`, `validarImagen`, `altaEdicion`, `asociarImagen`; `Subidas`, flash | Organizador en filtro y `obtenerOrganizador`; nickname desde sesión | Local. AP, JS `alta-edicion-fechas.test.cjs`, MC indirecto por datos |
| `AltaEventoServlet` `/alta-evento` | GET formulario; POST alta con categorías e imagen | `listarCategorias`, `validarImagen`, `altaEvento`, `asociarImagen` | Organizador por filtro; no envía actor al alta | Local. JS `validacion-altas.test.cjs`, pruebas de lógica `ReglasAltaTest`; sin test Java dedicado del Servlet localizado |
| `AltaInstitucionServlet` `/alta-institucion` | GET formulario; POST alta con imagen | `validarImagen`, `existeInstitucion`, `altaInstitucion`, `asociarImagen`; regex URL | Organizador por filtro; sin actor en lógica | Local. JS `validacion-altas.test.cjs`, `ReglasAltaTest`; sin test Java dedicado del Servlet localizado |
| `AltaPatrocinioServlet` `/alta-patrocinio` | GET selectores autorizados; POST patrocinio | `obtenerOpcionesAltaPatrocinio`, `altaPatrocinio(dt, evento, actor)`; helper solo de sesión, enum `clases.NivelPatrocinio` | Actor desde sesión; propiedad, relaciones y rangos en `Sistema`, callback transaccional | **Local, SOAP pendiente.** PA, PF, MP; lógica `PatrocinioTest` y unicidad |
| `AltaTipoRegistroServlet` `/alta-tipo-registro` | GET ediciones propias; POST alta de tipo | `obtenerEdicionesOrganizador`, única `altaTipoRegistro(tipo, edicion, actor)`; `Actor.usuarioAutenticado` | Organizador de sesión; propiedad decidida en `Sistema`, no en Servlet | **Local; migración SOAP pendiente**. AP, `ConsultaTipoRegistroTest`, `UnicidadConcurrenciaTest` |
| `AltaUsuarioServlet` `/alta-usuario` | GET formulario; POST alta asistente/organizador con imagen | `listarInstituciones`, `validarImagen`, `altaAsistente`/`altaOrganizador`, `asociarImagen`; `Subidas` | Solo visitante por filtro; claves se trasladan al DTO de entrada, no se verifican aquí | Local. JS `validacion-formularios.test.cjs`, lógica `UsuarioTest`/`ReglasAltaTest`; sin test Java dedicado del Servlet localizado |
| `ConsultaEdicionServlet` `/edicion` | GET detalle, tipos, organizador, patrocinadoras y registros | `listarEventos`, lecturas de edición/usuario/imagen/tipos/registros; `obtenerInstitucionesPatrocinadoras(nombre, actor)` | Resumen de patrocinadoras autorizado en Sistema; visibilidad general y registros siguen en web/helper | Local. CE, PF, MC, MP. Solo el resumen de patrocinios quedó corregido |
| `ConsultaPatrocinioServlet` `/consulta-patrocinio` | GET selectores y detalle autorizado | `consultarPatrocinios(evento, edicion, institucion, actor)`; `AccesoPatrocinios.usuarioDeSesion` | Actor de sesión o `null` anónimo; visibilidad, selector y código decididos en Sistema | **Local, SOAP pendiente.** PA, PF, MP y `ConsultaPatrociniosVisibilidadTest` real |
| `ConsultaRegistroServlet` `/consulta-registro` | GET registros propios o de edición a cargo y detalle | `obtenerRegistrosAsistente`, `obtenerEdicionesOrganizador`, `obtenerRegistrosEdicion`, `consultarUsuario`; `DetalleRegistroVista` | Filtro exige sesión; Servlet distingue subtipo y valida edición propia | Local. MC, MS; sin suite Java unitaria dedicada localizada |
| `ConsultaTipoRegistroServlet` `/tipo-registro` | GET selector y detalle de tipo | `listarEventos`, `obtenerEdicionesEvento` vía helper, `obtenerTiposRegistroEdicion`, `consultarTipoRegistro`; `AccesoEdiciones` | Pública; helper limita edición aceptada o propia | Local. CE, MC, JS `consultas-selectores.test.cjs` |
| `EventoServlet` `/evento` | GET detalle público de evento | `listarEventos` para buscar nombre, `obtenerEdicionesEvento(nombre, ACEPTADA)`, `obtenerImagen` | Pública; política de estados decidida en web | Local. MC, JS selectores; sin test Java dedicado del Servlet localizado |
| `ImagenServlet` `/imagen` | GET bytes/MIME o avatar por defecto | `obtenerImagen`; para edición, `listarEventos` y `obtenerEdicionesEvento` por helper | Filtro trata GET como estático; permiso de edición en `AccesoEdiciones`; caché privada para edición | Local. CE, MC |
| `InicioServlet` `""`, `/inicio` | GET portada y filtro de categoría | **Sin llamada local propia**; `ClienteEventosCentral`, clases generadas; filtro carga categorías locales | Pública; filtro por categoría sobre lista pública recibida | **SOAP**, aunque configuración normal del cliente aún construye HTTP. IS; suites HTTPS del cliente generado no equivalen a integración de esta portada |
| `LoginServlet` `/login` | GET formulario; POST autentica y crea sesión HTTP nueva | `autenticar`; DTO usuario de sesión | Visitante por filtro; invalida sesión anterior; no verifica claves en web | Local. MC, MP, MS y `logica/.../LoginTest`; sin test Java unitario específico del Servlet localizado |
| `LogoutServlet` `/logout` | POST invalida sesión; GET responde 405 y `Allow: POST` | **Sin llamada local propia**, ni cliente SOAP; filtro consulta categorías | Filtro exige sesión y CSRF al POST; no revoca sesión Central todavía | Sin SOAP. CS, prueba manual CSRF/PRG |
| `ModificarUsuarioServlet` `/modificar-usuario` | GET perfil/formulario; POST modifica subtipo, clave opcional e imagen | `consultarUsuario`, `listarInstituciones`, `validarImagen`, `modificarAsistente`/`Organizador`, `asociarImagen` | Sesión por filtro; destino derivado de sesión; no actor en operación lógica | Local. JS formularios; lógica `ModificacionPerfilAtomicaTest`; falta suite dedicada de este flujo con imagen |
| `RegistroEdicionServlet` `/registro-edicion` | GET formulario; POST inscripción general o por código | `consultarTipoRegistro`, `registroAEdicion`, `listarEventos`, `obtenerEdicionesEvento(..., ACEPTADA)`, `obtenerTiposRegistroEdicion` | Asistente de sesión por filtro y helper; pasa nickname al caso de uso | Local. MC, MS, lógica `RegistroTest`, `EstadoEdicionTest` |
| `UsuarioServlet` `/usuario` | GET perfil público/propio; registros o ediciones | `consultarUsuario`, `obtenerRegistrosAsistente`, `obtenerEdicionesOrganizador`; `RegistroVista` | Pública; comparación con sesión decide campos relacionados y estados visibles | Local. UF, MC |
| `UsuariosServlet` `/usuarios` | GET listado agrupado por subtipo | `listarUsuarios` | Pública; agrupar DTO por subtipo es presentación | Local. `ConsultasSistemaTest` protege listado lógico; sin suite dedicada del Servlet localizada |

### Cadena local y elementos transversales

El recorrido común es **Servlet → `ContextoListener.sistema(req)` → `ISistema`/`Sistema` → manejadores → `BaseDeDatos` → Hibernate/PostgreSQL**. No es una llamada remota por estar expresada con una interfaz. `ContextoListener.java:17–25` inicializa y cierra JPA y obtiene el controlador con `Fabrica`. En `FiltroAcceso.java:84` se llama a `listarCategorias` para todas las páginas que superan sus controles y no son estáticas. `webapp/pom.xml:23–28` mantiene dependencia de compilación de `logica`, que arrastra las dependencias de persistencia. El Central aparece en el POM como dependencia de **test**, no confundirlo con una fachada instalada en Tomcat.

`ISistema` retorna DTO, no entidades. No hay `new Sistema`, acceso a manejadores ni `Connection` dentro de los 19 Servlet. La Fábrica aparece directamente en el listener, no en ellos. `AltaPatrocinioServlet.java:3` importa un enum del paquete `clases`: acoplamiento al paquete del dominio, **no** acceso a una entidad JPA ni a una sesión Hibernate.

`MensajesFlash`, `RegistroVista`, `DetalleRegistroVista`: mensajes PRG y formateo legítimos; no mover formatos de moneda/fecha a persistencia. `Subidas.java:19–37`: lectura de `Part`, cierre de stream y límite de bytes del transporte legítimos. `Sistema.validarImagen`/`ValidadorImagen` deben seguir validando contenido para todos los clientes.

## 3. Matriz de responsabilidades por Servlet

**A**: HTTP/presentación correctos. **B**: regla funcional, autorización de recurso o visibilidad en web (directa o helper). **C**: persistencia; distinguir acceso directo de coordinación fragmentada. **D**: dependencia local a reemplazar. El riesgo máximo incluye incumplimiento arquitectónico y riesgo de exponer la operación, no prueba de explotación remota actual.

| Servlet | A: conservar | B: centralizar o dejar de duplicar | C: situación comprobada | D: cambio de transporte | Riesgo |
|---|---|---|---|---|---|
| AltaEdicion | Parseo ISO, sesión, multipart, PRG, mensajes | **Sí:** orden de fechas duplicado; rol de negocio no acreditado en lógica | Alta + imagen separadas; sin EM directo | Alta compuesta y consultas SOAP | Alto |
| AltaEvento | Parámetros, conjunto de categorías, mensajes | **Sí:** exige categoría y campos ya exigidos en lógica; permiso solo filtro | Alta + imagen separadas | Alta compuesta y categorías SOAP | Alto |
| AltaInstitucion | Formulario, mensajes, multipart | **Sí:** política de URL obligatoria/con punto solo web y preconsulta de unicidad | Alta + imagen separadas | Alta compuesta SOAP | Alto |
| AltaPatrocinio | Parsear números/enum, sesión, mensajes, PRG | **No:** reglas del caso centralizadas, incluido el formulario | Callback en transacción del manejador, antes de persistir; sin EM web | Alta y opciones ya corregidas deben migrarse a SOAP | Medio |
| AltaTipoRegistro | Sesión, parseo de costo/cupo, mensajes de vacíos, DTO, PRG | **No:** propiedad/rangos en `Sistema`; listado propio es preparación de formulario, pero debe autorizarse en Central al migrar | Una escritura transaccional; sin EM web | Alta y opciones propias SOAP pendientes | Medio |
| AltaUsuario | Parametrizar subtipo/DTO, convertir fecha, claves de entrada, multipart | **Sí:** nacimiento futuro/apellido/descripción duplicados; cualquier subtipo desconocido cae en asistente | Alta + imagen separadas | Alta compuesta e instituciones SOAP | Alto |
| ConsultaEdicion | Fechas, moneda, atributos/vistas, 404 | **Sí, parcial:** visibilidad general y registros; patrocinadoras ya centralizadas | Solo nombres autorizados de patrocinadoras; otras lecturas aún locales | Detalle autorizado SOAP pendiente | Alto |
| ConsultaPatrocinio | Parámetros, sesión, 404, formatos y vistas | **No:** Sistema devuelve selección y DTO autorizado | Web ya no recibe códigos para redactarlos; sin EM directo | Exponer consultas ya corregidas mediante SOAP | Medio |
| ConsultaRegistro | Sesión, selección de detalle, nombre completo, formatos | **Sí:** rol/propiedad y selección de datos protegidos | Sin EM directo; consulta por nickname/edición sin actor lógico | Consulta propia y de edición autorizada SOAP | Alto |
| ConsultaTipoRegistro | Parseo de claves, 404, moneda | **Sí:** helper decide visibilidad/relación evento-edición | Sin EM directo | Consulta visible autorizada SOAP | Alto |
| Evento | Parámetro y 404, fecha, vista | **Sí:** fija ACEPTADA como política pública; compone detalle a partir de lista | Sin EM directo; lecturas innecesarias de lista/bytes | Detalle público SOAP | Medio |
| Imagen | MIME, bytes, nosniff, caché y fallback | **Sí:** permiso de imagen de edición en helper web | Sin EM directo; obtiene bytes por lógica local | Lectura autorizada de imagen SOAP | Alto |
| Inicio | Categoría como filtro de presentación, 503, vista | **No:** no decide acceso a recursos protegidos | Categorías/JPA indirectos | Corregir configuración TLS y migrar categorías | Medio |
| Login | Parámetros, rotación de sesión, mensajes | **No:** comprobación de contraseña ya delegada a `Sistema` | Local indirecta vía autenticar | Consumir servicio de sesiones HTTPS existente | Alto |
| Logout | POST/405, CSRF, invalidación y redirect | **No:** todavía solo gestiona sesión HTTP | Ninguna propia | Integrar revocación Central existente | Medio |
| ModificarUsuario | Sesión, parseo, DTO, refrescar sesión después del éxito | **Sí:** nacimiento futuro/campos duplicados; identidad autorizada solo en web | Perfil+clave juntos; imagen en otra transacción | Modificación propia compuesta y consultas SOAP | Alto |
| RegistroEdicion | Números/selección, errores, PRG, formatos | **Sí:** disponibilidad por estado en selector, tipo/modalidad/código e identidad seleccionada fuera del caso | Escrito por manejador; revisar estado en misma unidad | Inscripción con actor remoto y opciones SOAP | Alto |
| Usuario | Formatos, plantilla por subtipo, mensajes | **Sí:** qué registros y estados se pueden ver según propiedad | Sin EM directo | Perfil proyectado según actor desde Central | Alto |
| Usuarios | Agrupar subtipos para JSP | **No:** no hay política de recurso propia en agrupación | Sin EM directo | Sustituir listado por SOAP con DTO acordado | Medio |

**Cómo se cuentan los 12 B:** AltaEdicion, AltaEvento, AltaInstitucion, AltaUsuario, ConsultaEdicion, ConsultaRegistro, ConsultaTipoRegistro, Evento, Imagen, ModificarUsuario, RegistroEdicion y Usuario. Los otros siete no mantienen reglas B del caso revisado, pero todos siguen pendientes de completar transporte/desacoplamiento. `ConsultaEdicionServlet` permanece en los 12 por sus otras consultas; corregir su resumen de patrocinadoras no cierra el Servlet completo.

Un mensaje «campo obligatorio» antes de convertir un número vacío es adaptación del transporte. En cambio, establecer que una fecha de nacimiento no puede ser futura, que el costo no puede ser negativo, que una edición no aceptada es privada o que solo el dueño puede leer registros son restricciones independientes del cliente. Los checks de formulario pueden mejorar UX; no deben ser la autoridad ni mantener una segunda implementación funcional. Los predicados de visibilidad no pasan a ser presentación por terminar en `setAttribute`.

## 4. Hallazgos concretos y riesgos

### CAP-01 — Alto — Tomcat sigue siendo un nodo de lógica y persistencia

**Archivos/métodos:** `ContextoListener.java:17–25,29–30`, `FiltroAcceso.java:82–86`, `webapp/pom.xml:23–28`; `persistencia/BaseDeDatos.java:14–18,105–114,138–142`.

```java
BaseDeDatos.inicializar();
evento.getServletContext().setAttribute(ATRIBUTO_SISTEMA, Fabrica.getInstance().getISistema());
// FiltroAcceso
req.setAttribute("categorias", ContextoListener.sistema(req).listarCategorias());
```

Está demostrada la inicialización de JPA dentro del WAR y el acceso local a categorías, incluso para portada/login/logout. La inicialización sin argumentos usa la configuración/defaults de `BaseDeDatos`; no se imprimieron ni utilizaron sus credenciales. Esto impide ejecutar el Web sin acceso a persistencia y contradice la separación de procesos. No se comprobó contra qué base se conectaría un Tomcat desplegado.

**Corrección propuesta:** migrar casos y categorías a clientes SOAP; retirar inicialización/cierre JPA del listener solo cuando no queden consumidores. Retirar la dependencia de implementación de `logica` del WAR tras adaptar DTO/modelos web. No basta con borrar imports: probar inicio del WAR sin driver/configuración JDBC. Mantener el filtro con HTTP, CSRF y navegación por sesión.

### CAP-02 — Alto — Identidad web local y contratos de negocio sin actor en otros dominios

**Evidencia:** `LoginServlet.doPost:29–39`, `ModificarUsuarioServlet.perfilActual:91–94` y `doPost:60–69`, `AltaEdicionServlet.doPost:86–97`, `RegistroEdicionServlet.doPost:94–95`; `FiltroAcceso.java:28–39`. `Sistema.modificarAsistente:293–328`, `modificarOrganizador:333–359`, `altaEdicion:513–564` y `registroAEdicion:726–751` no reciben actor.

```java
sistema.registroAEdicion(asistente.getNickname(), edicion, tipo, dt, codigoUsado);
```

El Servlet deriva el nickname desde sesión, por lo que ese POST no confía simplemente en un parámetro de identidad. Sin embargo, la API lógica no sabe quién ejecuta la operación; existencia del organizador o asistente no demuestra identidad del llamador. Un futuro publicador que copie estos parámetros permitiría escoger la identidad afectada. Es un riesgo de la futura exposición, **no una operación protegida SOAP actualmente publicada**. La Fábrica/`ISistema` Java tampoco son una frontera de autenticación frente a código arbitrario en el proceso.

**Corrección:** acordar actor en los contratos de casos protegidos y centralizar la política en `Sistema`. El adaptador del Central obtiene ese actor exclusivamente con `SesionesRemotas.requerirActor(bearer)` antes de delegar; no aceptar rol, admin ni un `Actor` serializado desde el Web. Swing sigue con acceso local confiable y conserva sus funciones; no agregarle SOAP. Login web debe consumir `iniciarSesion` y almacenar la credencial solo del lado servidor en sesión HTTP; logout debe invocar revocación y conservar invalidación local/CSRF. Definir tratamiento de indisponibilidad y sesión vencida antes de implementarlo.

### CAP-03 — Alto — Visibilidad de ediciones, tipos e imágenes está en helpers web

**Evidencia abierta:** `AccesoEdiciones.esPropietario:21–25`, `esVisible:27–30`, `resolver:33–45`, `aceptadas:48–50`. Consumidores todavía pendientes: `ConsultaEdicionServlet.doGet`, `ConsultaTipoRegistroServlet.doGet`, `ImagenServlet.doGet` y `UsuarioServlet.doGet`. La copia de estas reglas en `AccesoPatrocinios` fue eliminada; ese helper solo obtiene el usuario de sesión.
```java
return edicion != null && (edicion.getEstado() == EstadoEdicion.ACEPTADA
        || esPropietario(usuario, edicion));
```

Sigue siendo una política de acceso a datos en AccesoEdiciones y parcialmente en otros Servlet; el caso de patrocinios ya no la utiliza. `Sistema.obtenerEdicionesEvento`, `obtenerTiposRegistroEdicion`, `consultarTipoRegistro:682–705` y `obtenerImagen:963–968` no aplican actor/visibilidad. Los recorridos web comprobados estáticamente rechazan antes de obtener bytes privados, pero la lógica genérica no ofrece esa garantía a otro consumidor.

**Corrección:** definir en `Sistema` la política pública/propia y consultas con DTO autorizados. El Central debe filtrar antes de transferir. Conservar 404 y cabeceras en web. No hacer que el cliente reciba todas las ediciones para elegir cuáles puede ver. Acordar posteriormente cómo finalizados/archivadas del Lab 3 modifican la política; no inventar esas reglas en la migración.

### CAP-04 — Resuelto en lógica — Patrocinios; transporte SOAP pendiente

**Problema original, ya corregido:** el POST comprobaba propiedad con `AccesoPatrocinios.esEdicionPropia`; la consulta obtenía un DTO completo y luego eliminaba el código con `esDuenio ? codigo : null`. Ya no existen esas reglas en el helper ni en el Servlet.

**Evidencia actual:**

- `Sistema.altaPatrocinio:804–852` recibe DTO, evento opcional y actor, valida funcionalidad y aporta la política de propiedad al callback. `ManejadorPatrocinios.addPatrocinio:33–60` bloquea edición/tipo, ejecuta el callback en la transacción y recién después persiste. Las restricciones únicas siguen protegiendo carreras.
- `Sistema.obtenerOpcionesAltaPatrocinio:774–800` prepara el formulario con ediciones propias; `AltaPatrocinioServlet:66,83–84` adapta HTTP y delega. `AccesoPatrocinios:12–15` solo lee la sesión.
- `Sistema:855–932` contiene la visibilidad y la proyección de las consultas. Todas las variantes públicas requieren actor: por código, por edición/institución y por listado. `null` está documentado como consulta anónima, no como administrador.
- `ConsultaPatrocinioServlet:23–42` recibe `DtConsultaPatrocinios` ya autorizado. No redacta códigos ni decide estados/propiedad. `ConsultaEdicionServlet:42–43` recibe solo nombres mediante `obtenerInstitucionesPatrocinadoras`; no recibe DTO reservados para armar ese resumen.
- Visitante/asistente/organizador ajeno: aceptadas, sin código. Propietario: sus ediciones en los tres estados, con código. Administrador local: consulta completa. No se listan todas las privadas del propietario en el selector: solo se conserva la privada seleccionada y autorizada.
- `principal` pasa su actor a los paneles de alta/consulta; ninguno de esos paneles crea administradores al guardar o consultar.

**Pruebas:** `PatrocinioTest`, `ConsultaPatrociniosVisibilidadTest`, `UnicidadConcurrenciaTest`, PA/PF/CE y el verificador HTTP real protegen estos recorridos. Las pruebas de Sistema verifican datos persistidos/proyecciones reales, no solo mocks.

**Pendiente:** migración SOAP con identidad resuelta en Central y DTO JAXB adecuados, además de los riesgos generales del WAR local. No reabrir el negocio aprobado ni declarar esos Servlet completamente migrados.

### CAP-05 — Alto — Consulta de registros y perfil dependen de propiedad decidida en web

**Evidencia:** `ConsultaRegistroServlet.consultaDelAsistente:68–90`, `consultaDelOrganizador:98–126`; `ConsultaEdicionServlet.prepararRegistros:49–68`; `UsuarioServlet.doGet:41–79`; `Sistema.obtenerRegistrosAsistente:254–269`, `obtenerRegistrosEdicion:936–942`.

```java
if (!esPropia) { resp.sendError(403, "La edición no pertenece al organizador."); return false; }
for (DtRegistro registro : sistema.obtenerRegistrosEdicion(edicion)) { /* vista */ }
```

La web tiene barreras efectivas, pero las consultas lógicas reciben nickname o edición y retornan datos sin actor. `puedeRegistrarse` en consulta de edición mezcla una decisión funcional con el armado de vista: puede mostrarse como ayuda, pero la elegibilidad real debe decidirse al escribir. Formatear un registro o elegir una fila ya autorizada sí es presentación.

**Corrección:** consultas de registros propios y de edición autorizada, perfil proyectado según actor, resueltos por `Sistema`. Mantener nombres/fechas/moneda en vistas. Evitar un viaje SOAP adicional por cada asistente si un DTO de consulta puede incluir los nombres permitidos.

### CAP-06 — Alto — Cinco flujos coordinan commits independientes para entidad e imagen

**Evidencia por Servlet:**

| Flujo / método | Primera escritura | Asociación posterior |
|---|---|---|
| `AltaUsuarioServlet.doPost` | `altaAsistente`/`altaOrganizador`, 59–63 | 72–77 |
| `ModificarUsuarioServlet.doPost` | `modificarAsistente`/`Organizador`, 60–64 | 67–74 |
| `AltaInstitucionServlet.doPost` | `altaInstitucion`, 76–80 | 86–91 |
| `AltaEventoServlet.doPost` | `altaEvento`, 85–96 | 100–105 |
| `AltaEdicionServlet.doPost` | `altaEdicion`, 97–104 | 106–117 |

`ManejadorUsuarios.addUsuario:24–56`, `modificarAsistente:124–153`, `modificarOrganizador` y `ManejadorInstituciones.addInstitucion:22–32`, `ManejadorEventos.addEvento:31–37`/`addEdicion:102–109` confirman sus unidades. `Sistema.asociarImagen:947–958` delega a **otra** unidad en `ManejadorImagenes.asociar:39–63`: nuevo EM, begin, asignación, commit. La validación previa no une transacciones.

```java
// ModificarUsuarioServlet
sistema.modificarAsistente(armarAsistente(req, form, perfil));
// Después, con otra transacción:
sistema.asociarImagen(TipoEntidadImagen.USUARIO, perfil.getNickname(), imagen);
```

Si falla la segunda escritura después de la primera, los datos anteriores ya están confirmados; varios caminos muestran éxito parcial. Perfil y contraseña **ya se guardan juntos**: no confundir ese avance con atomicidad de la imagen. Las asociaciones de `Usuario:26`, `Evento:30`, `Edicion:41`, `Institucion:17` son `@OneToOne(cascade=ALL, orphanRemoval=true)`: ayudan dentro del mismo contexto, no revierten un commit anterior.

**Corrección:** una operación lógica compuesta por caso con imagen opcional, validada en `Sistema`; un manejador dueño ejecuta una transacción con entidades gestionadas y cascadas. Sin transacciones web ni `@Transactional` agregado sin infraestructura. Adaptar después Swing/cargador al mismo caso, sin implementar una política paralela en SOAP. El fallo inducido y rollback total requieren integración contra `eventosuy_test`; no se probaron aquí.

### CAP-07 — Medio — Validaciones funcionales duplicadas o divergentes

**Archivos/métodos/líneas:** `AltaEdicionServlet.doPost:74–78` compara fechas, ya validado por `Sistema.altaEdicion:541–546`; `AltaUsuarioServlet.armarAsistente:89–98` y `ModificarUsuarioServlet.armarAsistente:96–108` rechazan nacimiento futuro, ya validado por `Sistema.validarNacimiento`; `AltaEventoServlet.doPost:61–71` repite campos/categorías de `Sistema.altaEvento:463–470`; `AltaInstitucionServlet:25–26,51–73` exige URL y la verifica con regex, mientras `Sistema.altaInstitucion:369–371` solo exige nombre/descripción y valida longitud del sitio. Hay además preconsulta de duplicados antes del alta real.

El control de dominio está duplicado en fechas/categorías; el sitio de institución tiene política divergente entre consumidores. No se debe introducir la regex como requisito nuevo sin acordar la política esperada. La preconsulta no garantiza unicidad concurrente: la escritura debe conservar su comprobación/restricción persistente.

`AltaUsuarioServlet.doPost:59–63` interpreta cualquier tipo distinto de `organizador` como asistente. Diferenciar parámetro desconocido de una elección válida al adaptar el transporte; no agregar una jerarquía nueva de usuarios por este motivo. Parsear fechas/números, recortar espacios y conservar mensajes específicos por campo siguen en web. Los DTO de fecha alta de evento/edición/patrocinio preparados con `LocalDate.now()` no son autoridad: `Sistema` ya fija su propia fecha en esas altas.

### CAP-08 — Medio — EventoServlet arma el detalle público y decide su política

**Archivo/método:** `EventoServlet.doGet:33–56`.

```java
for (DtEvento candidato : sistema.listarEventos()) { /* buscar clave */ }
sistema.obtenerEdicionesEvento(evento.getNombre(), EstadoEdicion.ACEPTADA);
```

Buscar una coincidencia en un selector no es necesariamente negocio; aquí compone una consulta de detalle y fija que todas las ediciones públicas deben ser aceptadas. La política debe residir en `Sistema`, especialmente al incorporar finalización/archivo. Además consulta bytes de imagen para saber si hay imagen. Riesgo: migración con múltiples viajes y reglas que cada cliente debe repetir.

**Corrección:** consulta de detalle público por nombre con ediciones autorizadas e indicador de imagen, implementada en `Sistema`, adaptada a DTO SOAP. Formato de fecha, manejo 404 y vista permanecen en Servlet. No ampliar `listarEventos` para devolver entidades completas.

### CAP-09 — Alto — Registro: identidad y coordinación del caso repartidas; estado fuera de la escritura

**Evidencia:** `RegistroEdicionServlet.doPost:67–95`, `mostrarFormulario:132–165`, `eventoDeLaEdicion:184–193`; `Sistema.registroAEdicion:733–750`; `ManejadorRegistros.registrar:158–352`.

La web decide que cualquier modalidad distinta de `codigo` sea general, exige código según modalidad y preconsulta el tipo; luego construye un DTO con costo/fecha que la lógica no usa como autoridad. `Sistema` ya fija la fecha y comprueba ACEPTADA; el manejador resuelve tipo, costo, cupos, código y unicidad en la transacción. **No afirmar que la web calcula el costo definitivo ni que no existe validación de estado en lógica.**

El estado se lee en `Sistema` antes de abrir la transacción de `registrar`, que bloquea edición/tipo pero no vuelve a verificar el estado. Existe una ventana potencial si otro caso cambia el estado entre lectura y bloqueo. Su intercalación no se reprodujo aquí. La propiedad/identidad tampoco está en la firma lógica.

**Corrección:** acordar contrato de inscripción con actor y entrada mínima; conservar la política en `Sistema` y comprobar invariantes dependientes del estado bajo la misma unidad transaccional de escritura, con un mecanismo interno coherente con el corte de tipos. No mover decisiones de negocio al publicador ni duplicarlas en el manejador. Mantener parseo, mensajes y selectores en web con opciones autorizadas desde Central.

### CAP-10 — Alto — Servicio HTTPS implementado, consumidor web normal aún genera HTTP

**Evidencia:** `InicioServlet.doGet:30–36`; `web/soap/ClienteEventosCentral.java:27–35,50`; `central/ServidorHttpsCentral.java:30–63` y `central/Main.java:13–25`.

```java
return new URI("http", null, host, puerto, ruta, null, null).toString();
```

`InicioServlet` sí usa SOAP y un puerto nuevo por llamada. Su filtro de categoría es presentación legítima de una colección pública. Pero el constructor normal del cliente lee properties y fabrica una URL HTTP; el Central actual publica HTTPS exclusivamente. El constructor alternativo recibe una URI y permite pruebas TLS, lo que no cambia la configuración de producción del Servlet. Las pruebas de HTTPS con cliente generado no demuestran integración del WAR normal.

**Corrección:** configurar HTTPS y confianza externa, conservar verificación de hostname y certificado y el error 503 sin fallback local; migrar categorías para que la página tampoco dependa de PostgreSQL. No restablecer HTTP en el Central para evitar esta adaptación.

### CAP-11 — Medio — DTO locales amplios no son automáticamente contratos SOAP adecuados

**Evidencia:** `UsuariosServlet.doGet:26–31`, `UsuarioServlet.doGet:34–58`; `Sistema.listarUsuarios:196–223` y `consultarUsuario:226–251`; `datatypes/DtUsuario.java:15–39`.

Los DTO de usuario tienen campos de entrada de contraseña/confirmación y constructores parametrizados; **en los mapeos actuales de consulta no se rellenan contraseñas**. No hay evidencia de una fuga actual de claves por esos listados. El riesgo es publicar automáticamente clases de escritura/herencia y campos no acordados como contrato de lectura. La agrupación de `UsuariosServlet` es correcta, no una regla que deba trasladarse a `Sistema`.

**Corrección:** proyecciones mínimas JAXB para consultas y entradas separadas de salidas cuando contienen secretos; decidir los campos públicos/propios en `Sistema` y usar fachadas solo para mapear transporte. No exponer entidades, ni asumir que todo DTO actual tiene constructor vacío/adaptadores JAXB necesarios.

## 5. Verificación expresa del corte de altaTipoRegistro

1. **Una firma pública de negocio:** `logica/src/main/java/interfaces/ISistema.java:44` y `implementacion/Sistema.java:631`: `boolean altaTipoRegistro(DtTipoRegistro dt, String nombreEdicion, Actor actor)`. No hay sobrecarga de negocio antigua sin actor en esos fuentes.
2. **Validación central:** `Sistema.java:632–654` exige actor organizador o administrador local, nombre/edición/descripción, costo finito/no negativo y cupo positivo. El administrador local solo exceptúa la propiedad; no omite esas validaciones.
3. **Propiedad central:** lambda en `Sistema.java:664–668`, `!actor.esAdministradorLocal() && !actor.nickname().equals(propietario)`, lanza excepción de negocio.
4. **Consistencia transaccional:** `ManejadorEventos.addTipoRegistro:282–331` abre EM/tx, bloquea edición `PESSIMISTIC_WRITE:290`, obtiene dueño y ejecuta `comprobarPropietario.accept(...):296` **antes** de consultar duplicados `298–312` y persistir `320`. La comprobación de unicidad y rollback permanecen allí; no se duplicó la política del propietario en persistencia. `Consumer<String>` pasa solo la clave, no entidades hacia web.
5. **Excepciones:** la denegación es `IllegalArgumentException`; el catch ejecuta rollback si activo y `ConflictosPersistencia.traducir:9–15` conserva el error salvo SQLState 23505 en su cadena. Cierra EM en finally. Edición inexistente se traduce en `Sistema.java:673–676` sin dar más información al organizador.
6. **Servlet actual:** `AltaTipoRegistroServlet.java:62–79` convierte parámetros y pasa `Actor.usuarioAutenticado(organizador)` derivado de sesión. Ya no recorre ediciones para autorizar el POST. `campoNumericoRequerido:105–110` conserva mensajes de HTTP por costo/cupo vacíos; no comprueba rangos. `obtenerOrganizador:113–138` sigue como barrera de sesión y navegación web, sin sustituir la autorización de lógica.
7. **DTO/consumidores:** web usa `DtTipoRegistro`; Swing `estacion-trabajo/src/main/java/swing/AltaTipoRegistro.java:264` utiliza la misma operación local. El manejador público es infraestructura interna: su existencia no constituye una segunda operación de `ISistema`, pero no debe publicarse por SOAP ni ofrecerse al Web.
8. **Límite de confianza:** `Actor.usuarioAutenticado(DtUsuario)` no autentica el DTO por sí mismo y `Actor.administradorLocal()` es una fábrica local confiable. Código Java arbitrario podría fabricarlos; por eso el remoto nunca debe recibir un actor o rol del cliente. Un futuro publicador debe construirlo con `SesionesRemotas.requerirActor`, no deserializarlo.
9. **Pruebas existentes:** AP verifica envío de actor de sesión e ignorar parámetros de rol/nickname, pero usa un doble que simula denegación: **no prueba por sí solo el rollback real**. `ConsultaTipoRegistroTest` usa `PruebaConBaseSegura`, ejecuta `Sistema` real y comprueba propietario/admin, ajeno frente a nombre nuevo y duplicado, asistente/null y ausencia de inserciones. `UnicidadConcurrenciaTest.tiposQueSoloDifierenEnMayusculasEsperanElMismoPadre` protege la carrera. Estas suites y AP se repitieron al cierre, con la base confirmada por JDBC como eventosuy_test; todas aprobaron.

Conclusión: **no se detectó regresión de negocio en el Servlet de este corte ni una firma alternativa de `ISistema` que evite permisos. La migración de alta de tipo y del selector de ediciones a SOAP sigue pendiente.** No declarar que el caso ya funciona remotamente.

### 5.1. Otros cortes aprobados y revisión de encapsulación

Alta de patrocinio conserva una sola firma de escritura `altaPatrocinio(DtPatrocinio, String, Actor)`; las consultas no ofrecen una ruta alternativa de creación. Las firmas viejas de alta de tipo, alta de patrocinio y consulta de patrocinios sin actor desaparecieron de `ISistema`/`Sistema`; consumidores productivos adaptados y compilados.

La búsqueda productiva encuentra una única llamada a `ManejadorPatrocinios.addPatrocinio`, desde Sistema. El método del manejador sigue siendo público y acepta callback: el encapsulamiento interno es una convención de capas, no una barrera frente a código Java arbitrario en el proceso. No hay una ruta HTTP/SOAP productiva que invoque un callback vacío. `Actor.usuarioAutenticado` tampoco acredita identidad por sí mismo: se usa con usuario de sesión autenticada o con el resultado confiable de `autenticar`. Las fábricas de actor administrativo quedan en el arranque Swing y en el cargador local, no dentro de los paneles corregidos.

No se detectaron nuevos accesos JPA en Tomcat ni redacción tardía de códigos en Web. Los cambios acumulados de SOAP/HTTPS y los cortes previos forman parte del árbol sin confirmar: no confundir el diff contra HEAD con modificaciones hechas durante el cierre documental. Esta actividad solo actualiza este informe y crea ESTADO-ACTUAL.

## 6. Contratos SOAP actuales y faltantes

### Existentes y alcance real

| Servicio y operación | Fuente/contrato | Estado observado |
|---|---|---|
| `EventosService.listarEventos()` | `servidor-central/src/main/java/central/soap/EventosPublicador.java:22–40`; `webapp/src/main/resources/wsdl/EventosService.wsdl` | Fachada mapea `DtEvento` a `EventoSoap`, delega y traduce error. Es la única operación SOAP invocada por un Servlet de producción |
| `SesionesService.iniciarSesion(identificador, contrasenia)` | `SesionesPublicador.java:37–59`; `wsdl/SesionesService.wsdl` | Delega a registro → `ISistema.autenticar`. Usuario mínimo JAXB; credencial solo encabezado `X-EventosUy-Session`, no DTO. Web aún no lo consume |
| `SesionesService.cerrarSesion()` | `SesionesPublicador.java:61–96`; mismo WSDL | Lee único Bearer HTTP, rechaza ausente/duplicado/malformado, revocación idempotente; web aún no lo consume |

`ServidorHttpsCentral.iniciar:44–55` crea un registro y publica ambas rutas en un listener HTTPS. `SesionesRemotas.iniciar` solo admite identidades de asistentes/organizadores autenticadas por `Sistema`, genera 32 bytes de `SecureRandom`, conserva actor y vencimiento fijo. `requerirActor` rechaza sesión desconocida/vencida y `revocar` elimina la referencia. No hay operación protegida de negocio publicada que lo consuma todavía. El rol que retorna el login es dato de salida, no un permiso administrativo solicitado por el cliente.

### Brechas por funcionalidad (nombres remotos finales por acordar)

No se propone publicar automáticamente todos los métodos de `ISistema`. Esta matriz enumera capacidades faltantes para los Servlet existentes; un detalle compuesto puede reemplazar varias lecturas y no requiere una operación remota por fila lógica.

| Dominio / consumidores | Métodos locales actuales requeridos | Capacidad SOAP faltante / preparación previa |
|---|---|---|
| Categorías / filtro, alta evento | `listarCategorias` | Consulta pública de categorías; no necesita duplicar negocio ni enviar entidades |
| Usuario/listados / Usuarios, Usuario | `listarUsuarios`, `consultarUsuario`, `obtenerEdicionesOrganizador`, `obtenerRegistrosAsistente` | Listado mínimo y perfil público/propio con proyección autorizada; visibilidad en `Sistema` |
| Alta usuario / AltaUsuario | `altaAsistente`, `altaOrganizador`, `listarInstituciones`, `asociarImagen` | Alta por subtipo con imagen opcional en una unidad; catálogo instituciones. Confirmación/clave solo en entrada TLS |
| Perfil / ModificarUsuario | `consultarUsuario`, `modificarAsistente`, `modificarOrganizador`, `asociarImagen` | Modificación propia autorizada y atómica, sin nickname que suplante actor; mantener clave opcional |
| Instituciones / AltaInstitucion y formularios | `altaInstitucion`, `existeInstitucion`, `listarInstituciones` | Alta autorizada con imagen y listado. La preconsulta de existencia no sustituye unicidad; acordar URL |
| Evento / Evento, AltaEvento y selectores | `listarEventos` (ya remoto solo en portada), `altaEvento`, `obtenerEdicionesEvento`, `obtenerImagen` | Detalle público, alta autorizada con categorías/imagen. Reutilizar listado existente donde corresponda |
| Edición / AltaEdicion, ConsultaEdicion | `altaEdicion`, `obtenerEdicionesEvento` (ambas variantes), lecturas de organizador/tipos/patrocinios/registros | Alta autorizada con imagen; detalle y opciones según actor; proyecciones sin datos privados innecesarios |
| Tipo / AltaTipo, ConsultaTipo | `altaTipoRegistro(...,Actor)`, `obtenerEdicionesOrganizador`, `obtenerTiposRegistroEdicion`, `consultarTipoRegistro` | Alta protegida con actor resuelto en Central; opciones propias y consulta visible. No incluir Actor como parámetro SOAP |
| Inscripción / RegistroEdicion | `registroAEdicion` (dos firmas), `consultarTipoRegistro`, listados de selección | Inscripción autorizada con entrada mínima; opciones disponibles; sin costo/fecha confiados al cliente |
| Consulta registros / ConsultaRegistro, perfil, edición | `obtenerRegistrosAsistente`, `obtenerRegistrosEdicion`, `consultarUsuario` | Mis registros y registros de edición a cargo; DTO con nombres permitidos sin lectura por usuario adicional |
| Patrocinio / AltaPatrocinio, ConsultaPatrocinio | Alta y consultas con Actor; `obtenerOpcionesAltaPatrocinio`, `consultarPatrocinios`, `obtenerInstitucionesPatrocinadoras` | Negocio ya centralizado; faltan transporte SOAP, proyecciones JAXB y cliente remoto con identidad Central |
| Imágenes / Imagen y todas las altas | `obtenerImagen`, `validarImagen`, `asociarImagen` | Lectura autorizada de bytes/MIME; escritura incorporada al caso compuesto. No exponer asociación genérica sin permisos ni usar validación como transacción |
| Login/logout | `autenticar` y sesión HTTP | Contrato remoto **ya existe**; falta cliente web/gestión de referencia y errores. No crear otro servicio de autenticación |

Los métodos administrativos de `ISistema` no utilizados por esos Servlet (`aceptarRechazarEdicion`, `altaCategoria`, `cambiarContrasenia` aislado) no deben exponerse por comodidad. La sobrecarga actual `consultarPatrocinio(codigo, actor)` tampoco es necesaria para copiar la consulta actual del Servlet: no publicarla como búsqueda pública de códigos. Visitas/ranking, asistencia, finalización/archivo, búsqueda o disponibilidad AJAX que la letra exige se diseñarán en sus cortes; **no existen Servlet propios adicionales que puedan contarse como ya migrados**.

## 7. Orden actualizado de refactorización por funcionalidades

| Prioridad / grupo | Siguiente trabajo propuesto, sin implementar | Dependencias y criterio de salida |
|---|---|---|
| P0 — Próximo caso: consulta de registros | Centralizar `ConsultaRegistroServlet`: registros propios del asistente y registros de edición a cargo, con actor confiable y DTO acotado | Coordinar consulta de edición/perfil; probar Sistema directo, dueño/ajeno/asistente/anónimo/admin y ausencia de datos personales ajenos |
| P0 — Integridad de imágenes | Primer corte de escritura: modificación de perfil con imagen, seguida de altas de usuario/institución/evento/edición | Una operación y transacción por caso; perfil/clave/imagen deben revertir juntos; conservar comportamiento sin imagen y Swing |
| P0/P1 — Visibilidad restante | Ediciones, tipos, imágenes y perfiles; cerrar lo que queda de `AccesoEdiciones` | No trasladar entidades ni listas privadas para filtrar en Web; estados y propiedad definidos en Sistema |
| P0 — Frontera remota antes de publicar operaciones protegidas | Corregir configuración HTTPS/confianza del cliente y conectar login/logout web a sesiones opacas existentes | No aceptar Actor/rol/nickname como prueba de identidad; conservar HTTP/CSRF y no reintentar escrituras automáticamente |
| P1 — Altas e inscripción restantes | Actores y validaciones de evento/edición/institución/perfil/registro; consistencia de estado al registrar | Coordinar los contratos y la unidad transaccional con los manejadores; evitar duplicaciones y conservar validaciones anteriores |
| P1 — SOAP de casos corregidos y catálogos | Adaptar los tres cortes aprobados, categorías y consultas públicas a DTO/errores SOAP | Primero resolver TLS/identidad; no volver a implementar negocio en fachadas ni migrar Swing a SOAP |
| P1 — Retirar lógica/persistencia del WAR | Reemplazar llamadas locales y quitar listener JPA/dependencias de implementación | Todos los consumidores migrados; probar Tomcat sin driver/configuración PostgreSQL |
| P2 — Calidad e integración final | Reducir lecturas repetidas y revisar presentación, empaquetado y defensa LAN | Repetir regresiones relevantes por corte; verificar dispositivos reales, sin dar por hecha la LAN |

El siguiente caso recomendado es **consulta de registros**, por el acceso a datos personales actualmente protegido solo en web. La atomicidad de imágenes sigue siendo bloqueante antes de migrar esas escrituras. TLS/identidad son dependencias obligatorias antes de publicar operaciones protegidas, aunque no impiden el próximo refactor local.

No se altera el reparto: coordinar con los responsables documentados de registros, edición y perfiles; P03 sigue con Joaquín y el integrante 2/equipo. No se asignan integrantes arbitrariamente.

### Qué solo necesita transporte y qué necesita negocio

- **Sin regla B del caso revisado por trasladar:** AltaTipoRegistro, AltaPatrocinio, ConsultaPatrocinio, Inicio, Login, Logout y Usuarios. Aun así, tienen transporte, identidad remota, DTO o dependencias indirectas pendientes. En el GET de alta de tipo todavía debe acordarse una consulta propia segura al migrar su selector.
- **Con regla B abierta:** los 12 de §3. ConsultaEdicion solo tiene cerrado el resumen de patrocinadoras, no su detalle completo ni sus registros.
- **Persistencia directa a retirar:** listener JPA. **Coordinación persistente a corregir:** los cinco flujos de imagen. No agregar transacciones al Servlet ni a la fachada para unir llamadas independientes.

## 8. Pruebas para evitar regresiones y demostrar las fronteras

| Grupo | Protección existente localizada | Pruebas que faltan o deben adaptarse |
|---|---|---|
| HTTP/seguridad | CS: POST, token ausente/incorrecto/duplicado/en URL, sesión anterior, multipart/413, rol y logout GET/POST; `SubidasTest` | Conservar al cambiar el modelo de sesión; verificar rechazo antes de invocar cliente SOAP; pruebas de login/logout web con Central caído y referencia expirada |
| Portada/TLS | IS: proxy SOAP real con transporte HTTP aislado y HTTP Servlet simulado; `web/soap/ArranqueHttpsCentralTest`, `EncabezadosSoapHttpsTest`, `SesionesSoapHttpsTest`; Central `ConfiguracionCentralTest`, `EventosPublicadorTest`, `seguridad/SesionesRemotasTest` | Cliente público normal configurado por properties → Central HTTPS real; truststore ajeno y SAN incorrecto; categorías sin acceso local; no confundir fixtures HTTP con cumplimiento TLS productivo |
| Tipo | AP con dobles: parseo, mensajes de vacíos, PRG, CSRF, actor de sesión. Lógica real: `ConsultaTipoRegistroTest`, `UnicidadConcurrenciaTest`, `ReglasAltaTest`, `ValidacionesAdicionalesTest`, `LimitesTextoTest` | Proxy protegido real: dueño/ajeno/asistente, Bearer ausente/manipulado/vencido/revocado, admin remoto imposible; rollback y unicidad con lectura real posterior en base de pruebas |
| Edición/tipo/imagen de consulta | CE con dobles: estados, dueño/terceros, relaciones, registros, imagen rechazada antes de bytes; MC con Tomcat/persistencia | Reproducir matriz llamando `Sistema` y SOAP directamente, sin pasar por helpers; DTO/ XML sin datos de ediciones ocultas |
| Patrocinio | `PatrocinioTest` y `ConsultaPatrociniosVisibilidadTest` reales; PA/PF/CE, MP con 29 comprobaciones HTTP/JSP aprobadas en el bloque anterior | Falta la versión SOAP de la misma matriz, sin códigos en XML público ni variantes antiguas sin actor; no reimplementar las reglas ya probadas |
| Usuarios/perfil | UF solo formato fecha, pruebas JS de formulario; `UsuarioTest`, `LoginTest`, `ConsultasSistemaTest`, `ModificacionPerfilAtomicaTest` | Listados/perfiles JAXB sin secretos; edición propia por actor real; ambos subtipos; clave opcional; fallo de imagen revierte perfil y clave; no confiar en destino de cliente |
| Eventos/instituciones/altas | JS formularios, `ReglasAltaTest`, `ImagenesTest`, `ValidadorImagenTest`, `DecodificacionImagenTest`, pruebas de unicidad | Tests Java del flujo web y Central para altas compuestas; fallos antes/durante commit, éxito sin imagen, duplicado concurrente, URL acordada |
| Inscripción/registros | `RegistroTest`, `EstadoEdicionTest`, `EstadoEdicionConcurrenciaTest`, `UnicidadConcurrenciaTest`; MC/MS manuales | Carrera cambio de estado frente a registro; rol/identidad/propiedad al llamar lógica y SOAP directamente; cupo/código/último uso, selección inválida y costo fijado por Central |
| Retirada de lógica | No se localizó prueba que arranque el WAR actual sin JPA | Inspección de dependencias y WAR; arrancar Tomcat sin driver/variables JDBC, listar categorías y recorrer páginas con Central remoto; simular Central indisponible sin fallback local |

Las suites web con `Proxy` de `ISistema` prueban adaptación y llamadas, **no la existencia de autorización/rollback en lógica real**. Los tests de lógica que heredan `soporte.PruebaConBaseSegura` y los programas manuales de Tomcat/persistencia solo pueden ejecutarse tras la comprobación de base. En el cierre se seleccionaron regresiones de los tres cortes aprobados; ver resultados en ESTADO-ACTUAL. Las integraciones necesitan `EVENTOS_DB_NAME=eventosuy_test` y las protecciones efectivas de URL/base existentes antes de escribir datos.

La comprobación de separación no se limita a que desaparezcan imports: revisar que no queden predicados por dueño/estado/redacción de campos en Servlet o helpers, que las consultas protegidas reciban actor confiable en lógica y que una llamada directa a `Sistema` o SOAP tenga el mismo resultado de autorización. Una inspección automatizada de dependencias puede ayudar, pero no reemplaza la revisión de qué datos se transfieren antes de decidir permisos.

## 9. Estado de cumplimiento del Lab 3 y límites

| Criterio | Estado comprobado por fuente |
|---|---|
| Swing local dentro del Central | Conservado; no necesita migración SOAP para cumplir este criterio |
| Web–Central exclusivamente SOAP HTTPS | **No cumplido todavía:** 17 Servlet llaman lógica local; portada configura HTTP; JPA inicializa en Tomcat |
| Central publica ambos servicios HTTPS | Implementado en arranque actual; sin nuevo despliegue ni ejecución en esta auditoría |
| Identidad remota verificable | Registro y servicio de sesiones implementados; login/logout web no integrados; ningún caso protegido de negocio publicado aún |
| Reglas centralizadas en `Sistema` | Parcial: tres casos aprobados centralizados; 12 Servlet/helpers aún contienen reglas B |
| Persistencia encapsulada | Servlet sin EM directo, pero listener JPA local y cinco flujos de imagen con unidad fragmentada |
| DTO, sin entidades remotas | Frontera actual usa DTO; faltan contratos/proyecciones para las operaciones restantes y retirar dependencia local de implementación |
| Imágenes por SOAP | Pendiente en flujos web; ahora se consultan/asocian por lógica local |
| Desacoplar WAR de PostgreSQL | Pendiente de retirar listener, carga local de categorías y llamadas locales |
| Defensa en varias computadoras | No comprobada por análisis estático; procedimiento HTTPS/LAN documentado previamente, sin despliegue aquí |

Este diagnóstico no acredita vulnerabilidades explotables en un endpoint protegido inexistente. Sí demuestra decisiones que hoy pertenecen al cliente web y que no se deben perder al trasladar su transporte. Los resultados históricos y la selección repetida al cierre se distinguen en ESTADO-ACTUAL; no equivalen a ejecutar toda la suite ni a probar la defensa en LAN. Solo se modifica documentación en el cierre; se conservan los cambios previos sin confirmar.
