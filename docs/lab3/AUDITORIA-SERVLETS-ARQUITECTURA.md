# Auditoría técnica de Servlet y arquitectura · Lab 3 Eventos UY

> Auditoría histórica anterior a la integración del 15/11. Sus hallazgos sobre
> Web local, HTTP, sesiones pendientes y atomicidad fueron tratados en
> [el informe vigente](INTEGRACION-2026-11-15.md), con pruebas y commits.
> Se conserva el contenido original como trazabilidad, no como estado actual.


Revisión estática del 10/11/2026 sobre `a1a15e7` en `lab3-desarrollo`. Las líneas corresponden a esa revisión. Se inspeccionaron fuentes, configuración Maven/JPA, consumidores Swing y pruebas existentes. Las conclusiones sobre commits separados surgen de los límites transaccionales del código. El informe no incluye pruebas ejecutadas, conexiones a bases, mediciones de latencia ni comprobaciones sobre un despliegue real.

## 1. Resumen ejecutivo

- **19 Servlet propios**. El corte SOAP actual publica solo `listarEventos()` y lo consume la portada. El resto de la web sigue invocando `ISistema` local, incluido el listado de categorías del cabezal.
- **12 hallazgos agrupados por causa**. Crítica 0, Alta 6, Media 5, Baja 1. «Alta» identifica integridad afectada en el código actual o una barrera necesaria antes de publicar la operación. La tabla de Servlet indica la severidad máxima aplicable a cada clase, por lo que sus filas no suman 12.
- La web realiza controles efectivos de rol, propiedad y visibilidad en varios recorridos. Las operaciones de lógica que reutilizaría SOAP carecen en esos casos de contexto de actor. La exposición de operaciones protegidas por el endpoint actual no está demostrada.
- Las altas de usuarios, instituciones, eventos y ediciones y la modificación de usuario confirman datos e imagen en dos transacciones. El mismo patrón existe en Swing. Perfil y contraseña ya se confirman juntos. El cambio debe incluir la imagen en esa unidad.
- Primer corte recomendado: acordar identidad y frontera de las operaciones protegidas, y demostrar rollback de **perfil, contraseña e imagen** con el Integrante 2. En paralelo, el Integrante 3 puede trabajar la consulta pública de evento y preparar la operación autorizada de alta de tipo.

## 2. Inventario completo

Se cruzaron archivos Java versionados, herencia de Servlet, anotaciones, referencias de registro y `webapp/src/main/webapp/WEB-INF/web.xml`. El descriptor agrega páginas de error, sin declarar Servlet adicionales. La búsqueda en disco también encontró `ComprarEntradas`, `ConsultarPelicula`, `ListarPeliculas` y `VerPoster` en `ProyectoWebServices/web/src/main/java/uy/edu/curso/cartelera/web/`, además de ejemplos del Tomcat incluido en esa carpeta. Son fuentes del material de referencia ignorado por Git con POM independiente. Los 19 de la tabla son los del reactor Eventos UY.

**Rutas de la tabla:** cada archivo está bajo `webapp/src/main/java/web/`. Responsables según el reparto documentado del Lab 3. «Central» significa la capa lógica ejecutándose en el Central, con persistencia a través de manejadores. Ninguno de los 19 Servlet abre `EntityManager` o llama a `persist` por su cuenta. Sí hay un listener web que arranca JPA local y un Servlet que importa un enum del paquete de dominio.

| Servlet y archivo | Responsabilidad actual | Problemas | Severidad | Capa correcta | Métodos afectados | Impacto SOAP | Responsable | Pruebas necesarias |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| `AltaEdicionServlet.java` | Alta con organizador de sesión, fechas e imagen | Orden de fechas duplicado, actor solo en web, alta y foto separadas (001, 006, 008) | Alta | Central valida actor, fechas y unidad de escritura | `doPost`, `obtenerOrganizador` | Alta con actor verificado e imagen opcional | 3 con Joaquín | Rol, fechas, rollback, PRG |
| `AltaEventoServlet.java` | Alta de evento con categorías e imagen | Rol en filtro, dominio duplicado, alta parcial y captura genérica (001, 006, 008, 011) | Alta | Central valida y persiste, web parsea y redirige | `doPost`, `mostrarFormulario` | Alta compuesta y consulta de categorías | 3 con Joaquín | Rol, categorías inválidas, error de persistencia |
| `AltaInstitucionServlet.java` | Alta con datos de sitio e imagen | Política de URL divergente, lectura de duplicados antes de alta, rol web y escritura partida (001, 006, 008) | Alta | Central valida y escribe | `doPost` | Alta compuesta con política de URL acordada | 2 con Joaquín | URL, duplicados concurrentes, rollback |
| `AltaPatrocinioServlet.java` | Alta y armado de selectores dependientes | Propiedad, compatibilidad y rangos en web, escaneo de ediciones, enum de dominio (004, 008) | Alta | Central autoriza y ofrece opciones válidas, web convierte tipos | `doPost`, `mostrarFormulario` | Alta autorizada y consultas acotadas | 2, coordina 3 | Propietario/ajeno, 20 %, consistencia de selección |
| `AltaTipoRegistroServlet.java` | Alta de tipo en edición propia | Propiedad solo en web, costo/cupo duplicados (001, 008) | Alta | Central controla actor, edición y rangos | `doPost` 64–119 | Alta autorizada y conservar entrada administrativa local | 3 | Dueño, ajeno, administrador, límites, PRG |
| `AltaUsuarioServlet.java` | Alta pública por subtipo, formulario y foto | Alta y foto separadas, validaciones duplicadas, valor por defecto del subtipo (006, 008) | Alta | Central valida y persiste, web lee formulario | `doPost`, `armarAsistente`, `armarOrganizador` | Entrada de alta separada de las proyecciones de salida | 2 con Joaquín | Ambos subtipos, subtipo inválido, rollback, multipart |
| `ConsultaEdicionServlet.java` | Selector y detalle con registros según actor | Visibilidad y composición de datos protegidos en web (003, 005) | Alta | Central devuelve detalle autorizado, web formatea | `doGet`, `prepararRegistros` | Consulta de detalle con proyección por actor | 3 con 4 | Visitante, dueño, ajeno, asistente registrado |
| `ConsultaPatrocinioServlet.java` | Consulta y selector de patrocinio | Redacción del código y visibilidad después de obtener datos en web (003, 004) | Alta | Central limita campos y filas antes de responder | `doGet` | Proyección pública y variante autorizada | 2 con 3 | Código ausente para terceros, recurso oculto, enlaces |
| `ConsultaRegistroServlet.java` | Registros propios o de edición a cargo | Propiedad y selección de registros protegidos en web, consultas adicionales por asistente (005) | Alta | Central autoriza y proyecta, web formatea | `doGet`, `consultaDelAsistente`, `consultaDelOrganizador` | Consultas separadas por finalidad | 4 | Propio, ajeno, dueño, edición ajena, selección vacía |
| `ConsultaTipoRegistroServlet.java` | Selector y detalle de un tipo | Visibilidad y relación de la selección mediante helper web (003) | Alta | Central resuelve edición y visibilidad | `doGet` | Consulta autorizada por edición y tipo | 3 | Estados, dueño y ajeno, tipo en otra edición, 404 |
| `EventoServlet.java` | Detalle público por nombre | Lista completa para hallar una clave y fija política de ediciones públicas en web (007) | Media | Central consulta detalle público | `doGet` | Consulta por nombre, conserva contrato de portada | 3 | Ausencia, coincidencia exacta, estados, imagen opcional |
| `ImagenServlet.java` | Entrega bytes, MIME y fallback | La privacidad de edición depende de web. Se usa una consulta completa para autorizar (003) | Alta | Central autoriza bytes, web sirve HTTP | `doGet`, `tipoDe` | Operación de lectura acotada con autorización | Joaquín con 3 | 404 ajeno, bytes/MIME, caché, imagen ausente |
| `InicioServlet.java` | Portada SOAP y filtro por categoría | Dependencia indirecta de JPA por cabezal (009). Filtrar la lista recibida es admisible como presentación mientras no sustituya una política de visibilidad | Media | Web renderiza, Central define el conjunto público | `doGet`, `FiltroAcceso.doFilter` | Mantener operación y migrar categorías | 3 con 2/Joaquín | Filtro, lista vacía, 503, arranque de WAR sin DB |
| `LoginServlet.java` | Autentica por lógica y renueva sesión | Sin hallazgo propio en la frontera HTTP. Comparte dependencia local del filtro (009) | Sin hallazgos | Lógica verifica credenciales, web gestiona sesión | `doPost` | Resultado de autenticación acotado y sesión web propia | 2 | Credenciales, rotación de sesión, fallos de Central |
| `LogoutServlet.java` | Invalida sesión, POST, GET con 405 | Sin hallazgo propio. CSRF provisto por filtro | Sin hallazgos | Web, con eventual revocación Central si el diseño la requiere | `doPost`, `doGet` | Ninguna llamada necesaria por el comportamiento actual | 2 | POST con CSRF, sesión invalidada, GET 405 |
| `ModificarUsuarioServlet.java` | Edita el perfil de sesión y refresca el encabezado | Identidad solo web, perfil/clave separados de foto y validaciones repetidas (002, 006, 008) | Alta | Central aplica autorización y transacción, web actualiza sesión al éxito | `doPost`, `perfilActual`, `armarAsistente`, `armarOrganizador` | Modificación propia compuesta | 2 con Joaquín | Actor ajeno, ambos subtipos, rollback, sesión tras éxito |
| `RegistroEdicionServlet.java` | Inscripción general o con código | Identidad solo web, precarga de tipo y DTO de costo/fecha ignorados por lógica, control de estado fuera de la escritura (005, 010) | Alta | Central registra usando actor, estado y cupos | `doPost`, `mostrarFormulario`, `eventoDeLaEdicion` | Comando de inscripción sin costo/fecha del cliente | 4 con 3 | Actor, modalidad, cupos, estado, concurrencia |
| `UsuarioServlet.java` | Perfil público y datos adicionales propios | Visibilidad local de ediciones/registros y campos de perfil público sin matriz acordada (003, 005, 012) | Alta | Central proyecta según actor, web formatea | `doGet` | Perfil público/propio con campos explícitos | 2 con 3/4 | Matriz de actor/campo, ediciones visibles, registros propios |
| `UsuariosServlet.java` | Listado de usuarios agrupado para la JSP | DTO local más amplio que las tarjetas (012). Agrupar por subtipo es presentación | Media | Central devuelve resumen, web agrupa | `doGet` | Resumen de usuarios sin arrastrar DTO de escritura | 2 | Roles agrupados, campos de respuesta, ausencia de credenciales |

### Dependencias transversales

- `FiltroAcceso.doFilter` hace CSRF, restricciones de ruta, UTF-8, caché y carga el cabezal. La tabla de rutas y el token CSRF son barreras HTTP útiles. `rolDe` infiere el rol del DTO en sesión, no de un parámetro. El mapeo por defecto de cualquier otro subtipo a asistente conviene sustituirlo por rol explícito al cambiar el modelo de sesión. `Csrf.validarPost` exige un token único de sesión y rechaza su presencia en la URL. `ImagenServlet` tiene su propio control de acceso porque `FiltroAcceso.esEstatico` trata `/imagen` de forma especial.
- `AccesoEdiciones.resolver/esVisible/esPropietario/aceptadas` y `AccesoPatrocinios.buscarEdicion/esEdicionPropia/esPropietario/esVisible` repiten búsqueda y reglas por actor. `usuario`, `usuarioDeSesion` y `texto` son adaptación HTTP. `DetalleRegistroVista` y `RegistroVista` formatean valores para JSP. `MensajesFlash` transporta mensajes entre POST y GET.
- `Subidas.leerImagen` obtiene `Part`, limita lectura y arma `DtImagen`. El límite HTTP sirve para acotar la petición. `Sistema.validarImagen` y `ValidadorImagen` verifican el contenido para cualquier cliente. Conservar ambas barreras. `ContextoListener` crea JPA en Tomcat. `ClienteEventosCentral` contiene dirección, timeouts y traducción del fault de la portada.

## 3. Hallazgos individuales

### ARCH-001 · Alta · Las altas protegidas necesitan un actor en lógica

**Evidencia.** `webapp/src/main/java/web/AltaTipoRegistroServlet.java:64–78` (mismo bloque en `5fb46db`). Las líneas observadas 65–68 son el comienzo de la consulta de ediciones del organizador y su `stream`. El bloque comprueba pertenencia mediante `anyMatch`. En `logica/src/main/java/implementacion/Sistema.java:631–675` la firma es `altaTipoRegistro(DtTipoRegistro dt, String nombreEdicion)`. `FiltroAcceso.java:26–40` restringe las altas por rol. `Sistema.java:459–489, 513–565, 365–378` valida datos de evento, edición e institución. En alta de edición se resuelve `dt.getNombreOrganizador()` como recurso existente.

**Riesgo y condición.** Desde Java, un consumidor con acceso a la lógica puede solicitar alta sobre una edición válida sin presentar al propietario. El POST normal controla esa propiedad. Un futuro publicador que delegue al método actual perdería el control si lo expone sin contexto. El hecho de que un organizador exista tampoco acredita que sea quien hace la petición.

**Corrección e impacto.** Operaciones autorizadas en lógica para altas de evento, institución, edición y tipo. Derivar organizador desde identidad verificada en alta web de edición. En tipo, comprobar el dueño antes de persistir y reutilizar la edición gestionada de `ManejadorEventos.addTipoRegistro`. Mantener un punto de entrada administrativo local para `AltaTipoRegistro` y `AltaEdicionEventoPanel` de Swing. Integrante 3 lidera evento/edición/tipo y 2 institución. Probar dueño, organizador ajeno, asistente, visitante, edición inexistente y administrador local. Verificar estado persistido tras rechazos.

### ARCH-002 · Alta · Modificación de perfil y contraseña sin identidad en la API lógica

**Evidencia.** `webapp/src/main/java/web/ModificarUsuarioServlet.java:41–94` obtiene el perfil desde sesión y usa `sistema.modificarAsistente(armarAsistente(req, form, perfil))`. `logica/src/main/java/implementacion/Sistema.java:293–361` identifica el destino con `dt.getNickname()`. `Sistema.java:115–120` ejecuta `manejadorUsuarios.cambiarContrasenia(nickname, nueva)` tras validar la nueva clave y la confirmación.

**Riesgo y condición.** Un llamador Java con acceso a estos métodos y datos válidos puede operar sobre un destino por nickname. El Servlet actual deriva ese destino de sesión. La sobrecarga pública de cambio de contraseña tampoco acredita al actor por sí misma. Esto afecta la frontera remota propuesta, sin evidencia de una ruta HTTP/SOAP actual para llamar al método de cambio de clave aislado.

**Corrección e impacto.** `Sistema.modificarPerfilPropio` o variantes por subtipo toman un actor resuelto y un comando sin nickname destino. Resolver al usuario y aplicar datos, clave opcional e imagen en `ManejadorUsuarios`. Acordar si el cambio independiente queda reservado a administración o se reemplaza por un caso propio con política de credenciales. `ModificarUsuarioPanel` conserva su caso administrativo. Probar ambos subtipos, sesión ausente, actor ajeno y renovación de la sesión web tras éxito.

### ARCH-003 · Alta · Visibilidad de ediciones, tipos e imágenes en helpers web

**Evidencia.** `webapp/src/main/java/web/AccesoEdiciones.java:20–50` usa `edicion.getEstado() == EstadoEdicion.ACEPTADA || esPropietario(usuario, edicion)`. `AccesoPatrocinios.java:20–58` replica la política. `ConsultaEdicionServlet.java:19–40`, `ConsultaTipoRegistroServlet.java:13–39`, `UsuarioServlet.java:69–81` e `ImagenServlet.java:20–35` aplican reglas del recurso. `logica/src/main/java/implementacion/Sistema.java:590–615, 679–703, 937–943` ofrece datos por claves sin actor.

**Riesgo y condición.** La web protege el detalle y comprueba visibilidad antes de obtener bytes de edición. El filtro general de rutas carece de esa política de recursos. Si un adaptador remoto expone las consultas genéricas, podría devolver una edición oculta, sus tipos o su imagen a un tercero. Aplicar el filtro luego de transferir los datos deja el contenido fuera de la frontera que debía protegerlo.

**Corrección e impacto.** Centralizar la política y distinguir lista pública, detalle de propietario y consulta administrativa. Resolver la relación evento–edición y su visibilidad en lógica, con consultas dirigidas en `ManejadorEventos`. `ManejadorImagenes` entrega bytes a través de un caso autorizado. Coordinar con 3, 4 y Joaquín para estados de archivo y acceso por perfil. Probar visitante, dueño, ajeno, administrador y combinaciones de claves, incluyendo que una denegación no devuelva bytes ni campos protegidos.

### ARCH-004 · Alta · Alta de patrocinio y proyección del código dependen de web

**Evidencia.** `webapp/src/main/java/web/AltaPatrocinioServlet.java:34–80` usa `AccesoPatrocinios.esEdicionPropia(sistema, usuario, edicion)` antes del alta. `ConsultaPatrocinioServlet.java:41–58` lee un DTO completo y proyecta `esDuenio ? patrocinio.getCodigoPatrocinio() : null`. `logica/src/main/java/implementacion/Sistema.java:753–834, 837–905` valida montos, tipo, institución y límite del 20 %, pero no recibe actor para el alta. Ambas sobrecargas de consulta y el listado incluyen código.

**Riesgo y condición.** La ruta actual comprueba dueño para alta y limita el código en la vista. Exponer directamente `altaPatrocinio`, `consultarPatrocinio` u `obtenerPatrociniosEdicion` dejaría que la autorización y el filtrado dependieran del consumidor. Los códigos tienen valor funcional en la inscripción.

**Corrección e impacto.** `Sistema.altaPatrocinio` autorizado, comprobación en la unidad de trabajo y dos proyecciones: resumen público y detalle privado del dueño. `ManejadorPatrocinios.addPatrocinio` conserva validación persistente y commit único. `AltaPatrocinioPanel` y `ConsultaPatrocinioPanel` conservan vía administrativa. Integrantes 2/3 acuerdan la selección de ediciones. Probar que solo el dueño puede crear por el canal remoto y que el XML para terceros carece del código, incluidas listas y errores.

### ARCH-005 · Alta · Registros protegidos por la sesión y reglas de cada Servlet

**Evidencia.** `webapp/src/main/java/web/RegistroEdicionServlet.java:54–100` pasa `asistente.getNickname()` desde sesión. `ConsultaRegistroServlet.java:73–126` verifica edición propia antes de `sistema.obtenerRegistrosEdicion(edicion)`. `ConsultaEdicionServlet.java:53–74` y `UsuarioServlet.java:50–69` tienen reglas relacionadas. `logica/src/main/java/implementacion/Sistema.java:254–269, 707–750, 910–917` recibe identificadores en las consultas y en las dos sobrecargas de `registroAEdicion`.

**Riesgo y condición.** En las rutas actuales la sesión vincula al asistente y la consulta del organizador valida propiedad. Una futura fachada que deje elegir el asistente en el mensaje o llame a las consultas genéricas no impondría esas restricciones. Identificar una edición o una cuenta solo selecciona el recurso. La autorización debe ocurrir antes de devolver el conjunto de registros.

**Corrección e impacto.** `registrarEnEdicion(actor, comando)`, `consultarMisRegistros(actor)` y `consultarRegistrosDeEdicion(actor, edicion)`. La lógica obtiene fecha, costo y elegibilidad, y reutiliza `ManejadorRegistros.registrar`. Coordinar con 3 la proyección de consulta de edición y con 2 la de perfil. Swing y el cargador usan una vía administrativa explícita donde corresponda. Probar identidad propia, actor ajeno, edición ajena, tipo incorrecto, duplicados, cupos y códigos.

### ARCH-006 · Alta · Escrituras parciales de datos e imagen

**Evidencia.** `webapp/src/main/java/web/AltaUsuarioServlet.java:50–80`, `ModificarUsuarioServlet.java:54–80`, `AltaInstitucionServlet.java:66–93`, `AltaEventoServlet.java:75–106` y `AltaEdicionServlet.java:80–120` hacen dos llamadas de escritura. En modificación, `sistema.asociarImagen(TipoEntidadImagen.USUARIO, perfil.getNickname(), imagen)` sucede después de confirmar el perfil. `logica/src/main/java/manejadores/ManejadorImagenes.java:39–64` abre su propio EM y transacción. Véase la matriz de límites en §5.

**Riesgo y condición.** Un fallo en la segunda transacción deja confirmado el alta o el perfil y la clave nuevos. La validación previa reduce fallos de formato. Un fallo de persistencia posterior sigue siendo posible. Varios caminos informan éxito con advertencia. Swing también permite completar algunas imágenes más tarde.

**Corrección e impacto.** Guardar el agregado con imagen opcional en una transacción del manejador dueño, con validación de formato en lógica y referencias gestionadas en el mismo EM. Adaptar altas y modificación web/Swing. El endpoint de escritura debe representar el caso de uso completo. Probar rollback real para datos e imagen, contraseña original tras error, reemplazo exitoso con eliminación del huérfano anterior y comportamiento sin imagen. Joaquín coordina con 2/3.

### ARCH-007 · Media · EventoServlet reconstruye un detalle desde un listado

**Evidencia.** `webapp/src/main/java/web/EventoServlet.java:30–56` recorre `for (DtEvento candidato : sistema.listarEventos())` y llama a `obtenerEdicionesEvento(..., EstadoEdicion.ACEPTADA)`. `logica/src/main/java/manejadores/ManejadorEventos.java:49–67` ya dispone de `obtenerEvento(String)`.

**Riesgo y condición.** Cada detalle carga el listado y sus categorías antes de buscar. Una traducción directa a SOAP aumentaría datos transferidos y llamadas. El filtro fijo en web dificulta unificar la futura visibilidad de finalizados y archivadas.

**Corrección e impacto.** Agregar `consultarEventoPublico(nombre)` en `ISistema/Sistema` y reutilizar el manejador por clave. La proyección incluye ediciones visibles y un indicador de imagen obtenido sin transferir sus bytes. El Servlet conserva 404, fecha, atributos y vista. Mantener la consulta administrativa y el listado SOAP vigente. Probar clave exacta, ausencia, política de estados y ausencia de llamada al listado completo.

### ARCH-008 · Media · Validaciones funcionales duplicadas o divergentes

**Evidencia.** `webapp/src/main/java/web/AltaInstitucionServlet.java:25–26, 51–80` aplica `URL.matcher(form.get("sitioWeb")).matches()` y exige sitio. `logica/src/main/java/implementacion/Sistema.java:365–378` exige nombre y descripción y valida el texto del sitio, con otra semántica. `AltaEdicionServlet.java:66–79` compara fechas, `AltaTipoRegistroServlet.java:85–103` valida costo y cupo y `AltaUsuarioServlet.java:58–63, 90–115` decide subtipo y repite reglas del asistente/organizador. `ModificarUsuarioServlet.java:96–131` repite reglas de perfil. `AltaPatrocinioServlet.java:50–80` mezcla conversión con reglas de relación y montos.

**Riesgo y condición.** Swing y futuros consumidores pueden aceptar entradas distintas para la misma operación. Hay decisiones aún necesarias, por ejemplo si el sitio de una institución es obligatorio para todos los canales. La opción de subtipo desconocida cae en la rama asistente del alta web. Esa rama puede fallar por campos requeridos. No se verificó un efecto de seguridad.

**Corrección e impacto.** Acordar una política funcional y aplicar reglas en `Sistema`. Mantener `Float.parseFloat`, `Integer.parseInt`, `LocalDate.parse`, conversión de enum, errores de forma y repoblado en web. Las ayudas JS son retroalimentación temprana. Probar límites en lógica y mensajes/valores/PRG en web. Coordinar con 2/3 y con Swing antes de cambiar la política de sitio o subtipo.

### ARCH-009 · Media · JPA en el WAR y dependencia indirecta de la portada

**Evidencia.** `webapp/src/main/java/web/ContextoListener.java:17–29` ejecuta `BaseDeDatos.inicializar()` y guarda la instancia de `ISistema`. `FiltroAcceso.java:82–86` ejecuta `ContextoListener.sistema(req).listarCategorias()` antes de la cadena en páginas dinámicas. `webapp/pom.xml:17–28` incluye lógica en producción y Central en `test`.

**Riesgo y condición.** Tomcat requiere persistencia aun para llegar al listado remoto. Una falla de la lectura local del cabezal puede ocurrir antes de `InicioServlet.doGet` y quedar fuera de su manejo de 503. Quitar la dependencia Maven antes de migrar el listener, el filtro y los demás consumidores rompe compilación o arranque.

**Corrección e impacto.** Separar la carga de datos del cabezal de las barreras HTTP, migrar categorías y cada consumidor a clientes y DTO de transporte, y retirar la inicialización local cuando desaparezca el último uso. Revisar `NivelPatrocinio`, los DTO de sesión y la dependencia de tests por separado. Integrante 2 lidera W05, Joaquín revisa empaquetado. Probar Tomcat sin acceso PostgreSQL y verificar `WEB-INF/lib` y el comportamiento ante indisponibilidad del Central.

### ARCH-010 · Media · Estado de edición comprobado fuera de la transacción de registro

**Evidencia.** `logica/src/main/java/implementacion/Sistema.java:733–748` comprueba `edicion.getEstado() != EstadoEdicion.ACEPTADA` antes de llamar a `registrar`. `logica/src/main/java/manejadores/ManejadorRegistros.java:158–210, 337–362` abre un EM nuevo, carga la edición sin lock explícito, bloquea asistente/tipo y calcula el costo y persiste en esa transacción. Hay bloqueo adicional al aplicar patrocinio.

**Riesgo y condición.** El chequeo de estado y la escritura observan momentos distintos. Con el flujo actual de transición desde Ingresada hacia Aceptada/Rechazada no se comprobó que una edición aceptada pueda perder esa condición por esas rutas. Lab 3 agrega archivo. Si la política exige impedir inscripciones cuando se archiva, se debe coordinar esa transición con la inscripción en la base.

**Corrección e impacto.** Revalidar el estado en la transacción de registro y acordar con el Integrante 3/4 la estrategia de sincronización de ambas escrituras. Usar el orden de adquisición de locks definido para los recursos compartidos y verificarlo con pruebas. Simplificar el comando para excluir el `DtRegistro` precalculado en `RegistroEdicionServlet.doPost`. Probar carrera registro/archivo bajo la política acordada y mantener regresión de cupos.

### ARCH-011 · Baja · Manejo de errores mezcla fallos esperados e inesperados

**Evidencia.** `webapp/src/main/java/web/AltaEventoServlet.java:85–95` vuelve a lanzar las excepciones de ejecución y convierte cualquier otra `Exception` en `new IllegalArgumentException(e.getMessage())`. `logica/src/main/java/implementacion/Sistema.java:459–488` usa `throws Exception` para reglas esperadas. `webapp/src/main/webapp/WEB-INF/web.xml:3–10` define página interna genérica para excepciones y 500.

**Riesgo y condición.** El contrato no distingue la categoría del error por tipo y algunos mensajes se trasladan directamente a la vista. Las excepciones comprobadas observadas de alta de evento corresponden a negocio. No se demostró divulgación de un fallo técnico en esa ruta. La política de éxito parcial de imágenes se trata en ARCH-006.

**Corrección e impacto.** Distinguir fallos esperados del caso de uso, denegación, conflicto y fallo técnico, con mensajes de presentación controlados. Acordar el mapeo HTTP/SOAP con Joaquín y conservar compatibilidad del fault de portada. Probar error de dominio, conflicto y error simulado de infraestructura sin detalles internos en la respuesta.

### ARCH-012 · Media · DTO de usuario más amplio que la proyección pública

**Evidencia.** `logica/src/main/java/implementacion/Sistema.java:196–251` construye DTO de usuario con correo y, para asistentes, fecha de nacimiento. `logica/src/main/java/datatypes/DtUsuario.java:12, 36–39` también declara propiedades de entrada de contraseña/confirmación. En `webapp/src/main/webapp/WEB-INF/views/usuario.jsp:60–73` se renderiza `${perfil.correoElectronico}` y la fecha del asistente en el perfil. `usuarios.jsp:29–48` consume un subconjunto para sus tarjetas.

**Riesgo y condición.** El perfil público actual muestra esos campos según el código. Hace falta confirmar con la consigna y el equipo cuáles deben ser públicos antes de cambiar ese comportamiento. El código de consulta construye nuevos DTO sin rellenar contraseñas. Reutilizar el mismo tipo de entrada y salida en el WSDL incluiría propiedades innecesarias en el contrato y facilitaría exposiciones accidentales.

**Corrección e impacto.** Acordar matriz pública/propia/administrativa, separar entrada de credenciales, resumen y perfil, y devolver al Servlet solo campos autorizados. Integrante 2 con 3 para perfiles y 4 para datos de registros. Verificar el esquema y XML de respuesta y la JSP para cada rol. Los DTO locales de Swing pueden conservar campos administrativos.

## 4. Seguridad y frontera de identidad

| Concepto | Evidencia actual | Responsabilidad propuesta |
| --- | --- | --- |
| Autenticación | `LoginServlet.doPost` llama a `Sistema.autenticar` y renueva sesión. `Usuario.contraseniaCoincide` usa `HashContrasenia`. | La lógica verifica credenciales. Tomcat gestiona su sesión. Central verifica la identidad en cada operación protegida mediante el mecanismo acordado. |
| Autorización | Rol desde sesión en `FiltroAcceso`. Propiedad y visibilidad en Servlet/helpers. | La barrera por ruta permanece en web. La autorización del recurso se aplica en el caso de uso Central antes de proyectar datos protegidos o escribir. |
| Invariantes | `Sistema` y manejadores validan rangos, relaciones, duplicados, cupos y estado en distintos puntos. | La misma regla se aplica independientemente de la interfaz. Las invariantes sensibles a cambios concurrentes se comprueban en la transacción. |
| CSRF | `Csrf` vincula POST y sesión de navegador. | Sigue siendo una defensa del canal web. SOAP requiere verificar su propia identidad y canal de confianza. |

**Modelo a acordar.** Propuesta para discutir con 2 y Joaquín: Central autentica y emite una referencia opaca a una identidad, con expiración y revocación. La web conserva esa referencia en sesión del lado servidor. El adaptador SOAP la verifica en Central mediante un canal protegido acordado, construye un `Actor` interno y llama al caso de uso. Central resuelve el rol desde el estado confiable. Una alternativa de delegación requiere acreditar la identidad del Web y la integridad, vigencia y alcance de su afirmación sobre el usuario. Deben acordarse transporte protegido, duración, invalidación, fallos y administración Swing. El endpoint actual se publica por HTTP. Su configuración de demostración no acredita un canal para credenciales de producción.

**Entradas locales y remotas.** `ISistema` y `Sistema` son accesibles desde Java dentro de un proceso que cargue lógica. Los métodos genéricos y sobrecargas administrativas actuales son parte de esa superficie local. Una firma pública Java por sí sola no abre un endpoint remoto. El publicador actual solo declara `EventosPublicador.listarEventos`. Inventariar y restringir qué operaciones puede usar cada adaptador. Separar la interfaz administrativa local de las operaciones autorizadas de usuario. Evitar una bandera de administrador en un DTO o un `Actor` construible desde valores del mensaje. `ManejadorRegistros.addRegistro` es una entrada de persistencia distinta de `registrar`, no usada por el recorrido web auditado. Mantener los manejadores detrás de casos de uso y revisar usos de pruebas y cargador antes de reducir su visibilidad.

**Demostración de defensa.** Ejecutar la misma matriz visitante/actor válido/actor ajeno/administrador por la entrada lógica autorizada y por el publicador real con cliente generado. Comprobar que faltan campos sensibles en la respuesta denegada o pública y que el estado persistido queda igual ante denegación. Inspeccionar WSDL para confirmar la ausencia de operaciones administrativas. Código arbitrario dentro del mismo proceso pertenece a otra frontera de confianza.

## 5. Transacciones, atomicidad y concurrencia

`logica/src/main/resources/META-INF/persistence.xml:4` declara `RESOURCE_LOCAL`. `BaseDeDatos.getEntityManager()` crea un EM desde la fábrica. Los manejadores llaman a `em.getTransaction()`, `begin()`, `commit()` y `rollback()`, y cierran el EM. En esta infraestructura no se identificó un interceptor de transacciones declarativas para `Sistema` o los manejadores. Agregar `@Transactional` por sí solo no integra las transacciones existentes.

| Caso | Primera transacción en `logica/src/main/java/manejadores/` | Segunda transacción | Consumidor Swing bajo `estacion-trabajo/src/main/java/swing/` |
| --- | --- | --- | --- |
| Alta asistente/organizador | `ManejadorUsuarios.addUsuario` 24–64, begin 28 y commit 56 | `ManejadorImagenes.asociar` 39–64, begin 44 y commit 55 | `AltaUsuarioPanel.registrarUsuario` |
| Modificar asistente/organizador | `ManejadorUsuarios.modificarAsistente` 124–161 y `modificarOrganizador` 169–200, commit 153/191 con perfil y hash opcional | Misma asociación independiente | `ModificarUsuarioPanel.guardarCambios` |
| Alta institución | `ManejadorInstituciones.addInstitucion` 20–39, begin 25 y commit 32 | Misma asociación independiente | `AltaInstitucionPanel`, `ImagenSwing.completarAlta` |
| Alta evento | `ManejadorEventos.addEvento` 26–46, begin 30 y commit 36 | Misma asociación independiente | `AltaEventoPanel`, `ImagenSwing.completarAlta` |
| Alta edición | `ManejadorEventos.addEdicion` 96–118, begin 101 y commit 108 | Misma asociación independiente | `AltaEdicionEventoPanel`, `ImagenSwing.completarAlta` |

`Usuario.java:26`, `Institucion.java:17`, `Evento.java:30` y `Edicion.java:41`, todos en `logica/src/main/java/clases/`, declaran `@OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)`. La cascada aplica operaciones JPA al objeto asociado dentro de la misma unidad de persistencia. `orphanRemoval` permite eliminar la imagen anterior al sustituirla. En los recorridos auditados, la imagen se asocia después de que existe el dueño. No se observó una imagen confirmada primero seguida de un alta fallida. El reemplazo individual ya tiene una transacción propia y puede tener sentido para un caso independiente autorizado.

**Diseño gradual.** Validar bytes antes de escribir. En alta, construir la imagen y asociarla al agregado antes de `persist`. En modificación, cargar al usuario gestionado y cambiar datos, hash opcional e imagen en ese mismo EM. Comprobar referencias y permisos de escritura antes de modificar, dentro de la unidad cuando dependan de datos mutables. Ante error, rollback y traducción en el límite de aplicación. Retener la imagen anterior cuando no se suministra reemplazo. Actualizar sesión web solo tras el éxito. Una lectura fallida o una desconexión posterior al commit no revierte el commit. Evitar reintentos automáticos de escrituras tras un timeout hasta acordar su semántica.

**Otros flujos revisados.** `ManejadorPatrocinios.addPatrocinio` (28–102) resuelve las referencias gestionadas y persiste una vez. `ManejadorRegistros.registrar` (158–363) resuelve cupos y costo y escribe el registro en una transacción. El uso del patrocinio se representa por la relación del registro, sin commit independiente de un contador. `ManejadorEventos.addTipoRegistro` (275–327) y `cambiarEstadoEdicion` (378–407) gestionan su propia escritura y bloqueo. Las lecturas previas de `Sistema` pueden observar otro estado y agregan round trips, pero no son commits parciales de esos casos. `CargarDatosPrueba` encadena altas, estados y registros con tolerancia a fallos por entrada. Es una carga de demostración con semántica por paso, fuera de los casos HTTP. No se halló otro caso web con dos commits de escritura comparable a ARCH-006.

**Concurrencia.** Las consultas previas de existencia no sustituyen restricciones de unicidad y traducción de conflictos al confirmar. Verificar el estado de edición al registrar según ARCH-010. En reemplazos simultáneos de perfil/imagen, decidir si se acepta última escritura o se detecta conflicto. No se comprobó una pérdida de datos bajo carrera. Si se adopta control optimista o un lock, comprobar su efecto sobre borrado de huérfanos, tiempo de bloqueo y orden de adquisición.

## 6. Contratos de lógica y SOAP

Firmas ilustrativas para discusión. Los nombres y tipos nuevos expresan intención y todavía no existen. Se puede comenzar con pocas operaciones por dominio y conservar temporalmente las entradas de Swing mientras se migran sus consumidores.

| Operación de lógica propuesta | Significado y consumidores | Contrato remoto |
| --- | --- | --- |
| `void modificarPerfilPropio(Actor actor, CambioPerfil cambio, DtImagen imagen)` | Actor resuelto en Central. Comando sin destino arbitrario, cambio de clave opcional y reemplazo de foto opcional. Afecta `Sistema`, `ManejadorUsuarios` y `ModificarUsuarioServlet`. | Operación compuesta con entrada de perfil e imagen. Salida sin credenciales y faults previstos. |
| `boolean altaTipoRegistro(Actor actor, String edicion, DtTipoRegistro datos)` | Identificador de recurso y DTO con nombre, descripción, costo y cupo. La lógica valida propiedad. La variante administrativa de `AltaTipoRegistro` Swing tiene acceso local explícito. | Alta autorizada. Acordar un resultado sin ambigüedad entre `false` y excepción antes de fijar el WSDL. |
| `DetalleEventoPublico consultarEventoPublico(String nombre)` | Busca una clave y devuelve detalle visible, ediciones públicas e indicador de imagen. Puede expresar ausencia con el contrato acordado. | Consulta por nombre independiente del listado vigente. |
| `DtImagen obtenerImagenVisible(Actor actor, TipoEntidadImagen tipo, String clave)` | Verifica visibilidad en el Central y obtiene bytes y MIME si corresponde. Para consulta pública se usa un contexto anónimo explícito. | DTO de transporte para lectura con bytes/MIME. Denegación y ausencia mapeadas según política de visibilidad. |
| `void registrarEnEdicion(Actor actor, SolicitudRegistro solicitud)` | Solicitud con edición, tipo y modalidad/código opcional. Lógica deriva asistente, fecha y costo. | Comando sin `DtRegistro` de escritura. Consultas propia y de organizador por separado. |

Aplicar el mismo diseño de comando e imagen opcional a `altaAsistente`, `altaOrganizador`, `altaInstitucion`, `altaEvento` y `altaEdicion`. Revisar también `cambiarContrasenia`, `altaPatrocinio`, `obtenerPatrociniosEdicion`, ambas `consultarPatrocinio`, ambas `obtenerEdicionesEvento`, `obtenerEdicionesOrganizador`, `obtenerTiposRegistroEdicion`, `consultarTipoRegistro` y las consultas de usuarios y registros. La API SOAP necesita los casos migrados y sus consultas de selección, no un espejo de todos los métodos internos.

**DTO.** Mantener `DtEvento`, `DtEdicion`, `DtUsuario`, `DtPatrocinio`, `DtRegistro` y `DtImagen` como tipos locales donde sirvan. Definir DTO de transporte separados para comandos, resumen público, proyección privada, sesión e imagen. Separar código de patrocinio y credenciales de las salidas públicas. Para SOAP, usar nombres, fechas representables, estados de transporte y bytes. El corte existente muestra `central.soap.EventoSoap` y su equivalente generado en web. Los clientes no reciben entidades, `EntityManager` ni clases de Hibernate.

**Publicación y migración.** `servidor-central/src/main/java/central/Main.java:11–28` publica mediante `Endpoint.publish` antes de Swing. Los publicadores delegan al caso autorizado. `wsgen` y `wsimport` mantienen el vínculo entre Java y WSDL/XSD. Conservar `listarEventos()` y su fault. Actualizar contrato, cliente y pruebas por recorrido. Para retirar JPA del WAR deben desaparecer `ContextoListener` productivo, el acceso a categorías local, imports de `ISistema/datatypes/clases` y los consumidores locales. Revisar las dependencias transitivas del WAR empaquetado. `servidor-central` en alcance `test` de web tiene una finalidad distinta.

## 7. Prioridades, dependencias y responsables

| Prioridad | Corte revisable | Responsable sugerido | Complejidad | Depende de | Riesgo de regresión |
| --- | --- | --- | --- | --- | --- |
| P0 | Definir identidad Central, interfaz de casos autorizados, roles y vía Swing | Joaquín + 2, revisan 3/4 | Alta | Matriz de permisos y decisión P03 | Alto. Sesión, propiedad y flujos administrativos |
| P0 | Perfil, clave e imagen en la misma transacción | 2 + Joaquín | Alta | Firma local y reglas de modificación propia | Alto. Rollback, imagen anterior y sesión |
| P0 | Altas con imagen, por agregado | 2 usuarios/instituciones, 3 eventos/ediciones, Joaquín revisa | Media por agregado | Patrón probado del corte anterior, acuerdos de asociación | Alto. Duplicados, referencias y mensajes |
| P0 | Alta de tipo/edición y operaciones por rol | 3 con 2 en institución | Media | Entrada autorizada y administración local acordadas | Medio. Swing, permisos y PRG |
| P0 | Patrocinios, detalle de edición, tipos, bytes y registros protegidos | 2/3/4 por dominio, Joaquín en imágenes | Alta | Identidad, matriz de visibilidad y proyecciones | Alto. Datos privados y estados |
| P0 antes de archivo | Revalidar elegibilidad de registro dentro de la transacción | 4 con 3 | Media | Política y coordinación de locks con archivo | Alto. Cupos, contención y orden de escritura |
| P1 | Consulta pública de evento por clave | 3 | Baja a media | Acordar visibilidad del detalle | Medio. Enlaces y 404 |
| P1 | Unificar validaciones y DTO de perfil | 2/3 según dominio | Media | Acordar campos públicos y política de URL | Medio. Formularios, perfiles y Swing |
| P1 | Migrar categorías y retirar dependencia local del WAR | 2 + Joaquín | Alta | Todos los consumidores productivos migrados para el retiro final | Alto. Arranque de Tomcat y cabecera |
| P2 | Simplificar composición y traducción de errores | Cada dueño, Joaquín coordina faults | Baja a media | Contratos de errores acordados | Medio. Mensajes y retención de formularios |

El Integrante 5 consume las proyecciones desde móvil y coordina con 3 los enlaces. No se le asigna la implementación de la política de autorización. Los nombres de los Integrantes 2–5 siguen sin recuperarse. La adaptación transversal de Swing requiere un responsable acordado. Para evitar conflictos en `Sistema` y `ManejadorEventos`, dividir cambios por método y revisar juntos los contratos antes de editar. La consulta de evento, la validación de bytes y los tests de transacción local pueden avanzar mientras se acuerda identidad remota. La publicación de escrituras protegidas depende de esa decisión.

## 8. Plan incremental y pruebas

1. **Acordar frontera y contratos mínimos.** Registrar la matriz actor/recurso/acción, identidad confiable, perfiles público/privado, administración Swing, errores y semántica de imagen ausente. Resolver especialmente la propiedad de tipo y el momento de autorización de consultas.
2. **Probar el primer agregado.** Integrante 2 y Joaquín hacen perfil+clave+imagen como unidad. Extender luego a cada alta con cambios pequeños y validar ambos consumidores. Los errores de validación se comprueban aparte del fallo de flush/commit. Consultar con un EM nuevo para comprobar qué quedó confirmado.
3. **Migrar permisos por dominio.** Integrante 3 agrega la consulta pública por clave y la variante autorizada de tipo. Integrantes 2/3/4 centralizan sus proyecciones. Mantener 404/403, mensajes y PRG según el recorrido. La revisión de cada Servlet debe poder señalar una única fuente de propiedad, rango o visibilidad en lógica.
4. **Publicar cada caso con sus pruebas.** Verificar WSDL, cliente generado y llamadas con identidad verificada. No basta probar un Servlet con un mock. El cliente remoto debe observar el mismo rechazo y no recibir los campos protegidos. Actualizar categorías y eliminar JPA local cuando esté migrado el último consumidor.

| Protección existente inspeccionada | Qué cubre y qué falta |
| --- | --- |
| `webapp/src/test/java/web/ConsultasEdicionTest.java`, `PatrociniosAutorizacionTest.java`, `PatrociniosFlujoTest.java` | Visibilidad, dueño, imagen privada y códigos en el canal web con dobles. Agregar equivalentes en lógica y SOAP con verificación de los campos de respuesta. |
| `AltasPrgTest`, `CsrfTest`, `SubidasTest`, `UsuarioFechaAltaTest` y tests JS | PRG, formulario, sesión/CSRF, multipart, fecha y UX. `AltasPrgTest.imagenFallidaMantieneRedirectExitoYAdvertenciaSinRepetirAlta` documenta el contrato anterior. Cambiar esa expectativa en el corte que introduce rollback. |
| `logica/src/test/java/implementacion/ModificacionPerfilAtomicaTest.java`, `ImagenesTest`, `DecodificacionImagenTest`, `ValidadorImagenTest` | Perfil+clave, asociación independiente y bytes. Agregar pruebas de cada operación compuesta, ambos subtipos, imagen sin huérfanos y fallo SQL provocado en entorno aislado. |
| `ReglasAltaTest`, `RegistroTest`, `PatrocinioTest`, `PersistenciaConcurrenteTest`, `UnicidadConcurrenciaTest`, `EstadoEdicionConcurrenciaTest` | Invariantes y concurrencia existentes. Agregar actor ajeno y carrera de registro/archivo cuando exista esa transición. |
| `servidor-central/src/test/java/central/EventosPublicadorTest.java`, `webapp/src/test/java/web/InicioSoapTest.java` | Contrato real de listado, cliente generado y 503. Agregar casos para identidad inválida/expirada, autorización de recurso, DTO privado y rollback. |

Las futuras pruebas que escriban se ejecutan con `EVENTOS_DB_NAME=eventosuy_test` en base independiente y con `ProteccionBasePruebas`. Para rollback, comprobar tanto el efecto de un fallo de datos como el de imagen y observar los valores originales desde otra unidad de persistencia. En revisión de arquitectura, buscar usos de manejadores/JPA y comparaciones de propiedad/estado en Web y publicadores, y leer cada coincidencia en contexto. Complementar con pruebas sobre lógica y SOAP y con inspección del WAR sin dependencias de persistencia.

## 9. Conceptos para la defensa

| API y origen en el proyecto | Qué recibe y devuelve | Por qué interviene |
| --- | --- | --- |
| `HttpServlet.doGet/doPost`, `HttpServletRequest.getParameter(String)`, `getSession(false)`. `jakarta.servlet:jakarta.servlet-api:6.1.0`, alcance `provided` | El Servlet recibe request/response. El parámetro es texto o ausencia. La sesión puede ser nula. Las operaciones de despacho y lectura pueden producir `ServletException` o `IOException`. | Convertir parámetros y traducir resultados a HTTP son tareas del adaptador. |
| `HttpServletRequest.getPart(String)`, `Part.getInputStream()` y `InputStream.readNBytes(int)` | Obtienen el fragmento multipart y leen hasta el límite de bytes. Pueden fallar por I/O, configuración multipart o límites del contenedor. | Proteger el transporte y pasar bytes a un validador de contenido reutilizable. |
| `EntityManager.find(Class, id)`, `persist(Object)`, `flush()`, `EntityTransaction.begin/commit/rollback`. `jakarta.persistence:jakarta.persistence-api:3.2.0` con Hibernate `7.1.1.Final` y PostgreSQL JDBC `42.7.7` | `find` carga una entidad o devuelve ausencia. `persist` la incorpora al contexto. `flush` sincroniza con la base. `commit` confirma y puede lanzar `RollbackException`. La persistencia puede lanzar `PersistenceException`. | El dueño de la transacción controla qué cambios se confirman juntos. Ver SQL durante flush no implica que ya exista un commit. |
| `EntityManager.find(..., LockModeType.PESSIMISTIC_WRITE)` y `lock(...)` de JPA | Cargan o bloquean una entidad dentro de una transacción. Pueden fallar por contención, timeout o estado de la transacción. | Alinear comprobación y escritura de una invariante concurrente. El orden de locks importa. |
| `Endpoint.publish(String, Object)` y `BindingProvider.getRequestContext()`. Jakarta XML WS mediante `com.sun.xml.ws:jaxws-rt:4.0.5` | Publica un implementador y devuelve el endpoint. El contexto del proxy permite definir dirección. Puede producir `WebServiceException` ante fallo de publicación o transporte. | Distinguir dirección, contrato y lógica. `ConsultaException` declarada aparece como un fault en el WSDL. |
| JAXB y `@XmlType` en el runtime SOAP. `jaxws-maven-plugin:4.0.5` para `wsgen/wsimport` | JAXB transforma objetos y XML. El plugin genera contrato o cliente según el módulo. Los fallos de mapeo o contrato se observan al construir o invocar. | El DTO expresa qué cruza la red y permite evolucionar lógica/persistencia sin exponer entidades. |

**Preguntas de defensa**

1. ¿Por qué `anyMatch` de `AltaTipoRegistroServlet` protege una ruta y aun así falta autorización en la operación que reutilizaría SOAP?
2. ¿Qué diferencia hay entre convertir un costo con `Float.parseFloat` y decidir si ese costo es válido en el dominio?
3. ¿En qué punto se confirma el alta actual y por qué el rollback de `ManejadorImagenes` no alcanza a esa escritura?
4. ¿Qué aporta `cascade = ALL` al guardar el agregado y qué ocurre con la foto anterior mediante `orphanRemoval`?
5. ¿Qué debe validar Central antes de construir `Actor`? ¿Qué diferencia hay entre ese actor y la clave del recurso pedida?
6. ¿Cómo probarías que una consulta pública de patrocinio omite el código en el XML aunque la entidad persistente lo contenga?
7. ¿Cómo afecta archivar una edición a una inscripción concurrente y qué comprobación debe ocurrir dentro de la transacción?
8. ¿Por qué hoy la portada puede depender de PostgreSQL desde Tomcat aunque `InicioServlet` use SOAP? ¿Qué evidencia mostrarías para dar por terminada la separación?
