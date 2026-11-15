# Integración de usuarios y separación Web/Central · 15/11/2026

La rama de trabajo es `lab3-desarrollo`. Se integraron aportes compatibles de
`origin/cambios-eventos-uy-2026-11-14` sin fusionar su commit raíz ni sustituir
nuestras implementaciones de autorización, sesiones, patrocinios o persistencia.
Esta actualización fue autorizada como desarrollo continuo con commits locales.
No se realizó push, pull, merge, rebase, cambio de rama ni limpieza destructiva.

## Resultado y límites

Los 21 Servlet de producción acceden a Central mediante clientes SOAP/HTTPS.
`ContextoListener` configura clientes; no inicia ni cierra JPA. El WAR contiene
el módulo `contratos`, Metro y dependencias de presentación, sin lógica,
Hibernate, Jakarta Persistence ni driver PostgreSQL. La dependencia de lógica en
Web tiene exclusivamente alcance `test` para las regresiones de integración.

```text
Navegador --HttpSession/cookie--> Tomcat/Servlet
         --SOAP/HTTPS + Authorization: Bearer--> Central
         --> Sistema --> manejadores --> PostgreSQL

Swing (en el proceso Central) --> ISistema --> Sistema
```

Sistema determina permisos, propiedad, visibilidad, costo, cupos, códigos y
transacciones. Web conserva parsing HTTP, límites multipart, selección de
vistas, formato, mensajes y CSRF. Los indicadores de presentación proceden de
proyecciones autorizadas; ningún formulario define Actor ni privilegios.

## Integración selectiva de Bentancor

| Aporte | Implementación resultante |
|---|---|
| Alta de asistentes y organizadores con imagen | Se conservó la etapa 1 ya confirmada; una transacción de manejador guarda usuario, hash e imagen. Las firmas sin imagen siguen delegando en la operación compuesta. |
| Modificación de perfiles y contraseña | Adaptada con Actor obligatorio, validación en Sistema y bloqueo anterior a cargar la entidad. Imagen nula conserva la existente; cualquier fallo revierte la modificación. Swing usa una sola operación. |
| Alta de instituciones con imagen | Operación atómica autorizada para organizador o administrador local; adaptación Swing y Web, duplicados y rollback cubiertos. |
| Consultas y gestión de usuarios | Listado, perfil público/propio, alta y modificación remotos; registros y ediciones privadas se filtran en Central. |
| Gestión de instituciones | Alta y consulta/listado con imagen. La rama de referencia no aporta modificación o baja de instituciones; no se inventó ese contrato. |
| Patrocinios | Se conservaron nuestras reglas, contratos, bloqueos, transacciones y privacidad del código; solo se adaptaron consumidores remotos. |
| Disponibilidad AJAX | Se incorporaron debounce, cancelación y descarte de respuestas antiguas; las consultas pasan por Central y no sustituyen la validación definitiva del alta. |
| Sesiones y versiones del compañero | Se conservaron P03, Actor, Java 25 y Metro 4.0.5. No se incorporaron sesiones paralelas ni firmas que omitan autorización. |

## Concurrencia y seguridad

El defecto de `asociarImagen(USUARIO, ...)` ocurría al cargar un usuario antes
de esperar por la escritura de imagen: el flush posterior podía restaurar un
hash anterior. Ahora se bloquea la fila mediante `FOR UPDATE` **antes** de
hidratar la entidad. La misma disciplina se aplica a imágenes de evento,
edición e institución; los cambios de contraseña y de perfil bloquean también
antes de leer el usuario modificable. No se agregaron entidades compartidas
entre transacciones.

La auditoría de los otros escritores reprodujo un registro con patrocinio que
usaba la institución anterior del asistente después de confirmar un cambio de
perfil. `ManejadorRegistros.registrar` ahora bloquea la fila de Usuario antes
de cargar Asistente; conserva los bloqueos de tipo/patrocinio y las reglas
existentes. La regresión exige rollback del registro, institución y contraseña
actuales, y rechazo de la contraseña anterior. Otras dos intercalaciones
comprueban preservación de imagen al decidir una edición y una única decisión
de estado; estas dos ya eran correctas y su implementación se conservó.

Se conservaron las dos reproducciones por subtipo y se añadieron intercalaciones
de imagen con cambio de perfil. Las regresiones comprueban que la contraseña
anterior deja de autenticar y que datos, imagen y contraseña no se pierden ni
quedan parcialmente confirmados. Las otras escrituras públicas de ISistema
no ofrecen una variante de modificación o cambio de clave sin Actor; el alta
duplicada no reemplaza usuarios existentes.

P03 mantiene una única colección de sesiones por Central y vencimiento fijo.
El navegador recibe solo la cookie HttpOnly de HttpSession, con seguimiento
exclusivamente por cookie. Login renueva la sesión local. La credencial P03 se
guarda únicamente en el servidor Web, se transmite por Bearer y se revoca al
cerrar sesión; nunca se reenvía al navegador ni se imprime. Aunque falle el
transporte al cerrar, la sesión local se invalida. Una sesión remota inválida
invalida la local. El rol del cabezal es solo presentación.

CSRF se valida antes del controlador en toda escritura HTTP, incluido multipart;
Central autentica y autoriza independientemente mediante P03. Imágenes de
ediciones ocultas, registros ajenos y códigos de patrocinio se protegen en
Sistema. Los faults controlados se traducen a 400, 403, 404, login o 503 según
corresponda, sin exponer errores SQL ni credenciales. No existe respaldo local
en Tomcat cuando Central está apagado. Los clientes rechazan destinos HTTP.

## Contratos y consumidores

`PortalService`/`PortalPort`, namespace `https://eventos.uy/soap/portal`, expone
26 operaciones en `/portal`. Fuente: `central.soap.PortalPublicador`; contrato
versionado: `webapp/src/main/resources/wsdl/PortalService.wsdl` y su XSD.
Los wrappers de Portal tienen paquete propio para evitar colisiones con el
servicio de eventos. Maven ejecuta wsgen y wsimport; no se confirman clases
generadas de `target`.

`ContratosRegeneracionTest` compara los seis WSDL/XSD de eventos, sesiones y
Portal con el resultado de wsgen. Metro puede variar el orden de declaraciones
globales entre JVM: la prueba compara el contrato XML conservando tipos,
cardinalidades, acciones y orden de campos, sin confundir esa variación con un
cambio de contrato.

| Área | Servlet | Operaciones principales |
|---|---|---|
| Usuarios | AltaUsuario, ModificarUsuario, Usuario, Usuarios, DisponibilidadUsuario | altaUsuario, modificarUsuario, usuarioActual, consultarPerfil, listarUsuarios, disponibleNickname/Correo |
| Sesión | Login, Logout; FiltroAcceso | P03 iniciarSesion/cerrarSesion; usuarioActual para validar la sesión vigente |
| Instituciones | AltaInstitucion, ConsultaInstitucion | altaInstitucion, listarInstituciones, consultarInstitucion |
| Eventos/ediciones | Inicio, Evento, AltaEvento, AltaEdicion, ConsultaEdicion | listarEventosPorCategoria, consultarEdiciones, consultarDetalleEdicion, altaEvento/Edicion |
| Tipos y registros | AltaTipoRegistro, ConsultaTipoRegistro, RegistroEdicion, ConsultaRegistro | altaTipoRegistro, consultarTipoRegistro, formularioRegistro, registrar, consultarRegistros |
| Patrocinios | AltaPatrocinio, ConsultaPatrocinio | obtenerOpcionesAltaPatrocinio, altaPatrocinio, consultarPatrocinios |
| Imágenes y categorías | Imagen; FiltroAcceso y formularios | obtenerImagen, listarCategorias |

Los DTO de transporte son beans JAXB, primitivas, fechas ISO y `byte[]`; no
contienen entidades ni Actor. Las contraseñas solo viajan como entrada explícita
de la operación, nunca en las respuestas de perfiles. EventosService conserva
su contrato original y SesionesService conserva P03.

## Validación final

| Comprobación | Resultado |
|---|---|
| Reactor aislado Java 25: compile, wsgen, wsimport, test y package | Aprobado en todos los módulos |
| JUnit final | 436: 293 lógica, 16 Central, 127 Web; 0 fallos, errores u omitidas |
| Integración TomcatPortalHttpsIT adicional | 1 aprobada, 49 solicitudes HTTP verificadas contra Tomcat 11.0.27 y Central HTTPS reales |
| JavaScript | 61 aprobadas; incluye 5 nuevas de disponibilidad AJAX |
| Swing headless | 5 comprobaciones aprobadas de propagación/encapsulamiento de Actor |
| Persistencia | `current_database()` confirmado por JDBC como `eventosuy_test` antes de cada ejecución persistente; sin uso de otra base |
| WAR | Sin lógica/JPA/Hibernate/PostgreSQL; despliegue con un CATALINA_BASE temporal y variables DB retiradas de Tomcat |
| Regresiones HTTPS reales con Sistema/PostgreSQL | 6 aprobadas: atomicidad, duplicados, cambio de clave, imagen, perfiles, patrocinios, registros y revocación |

La IT comprueba además renovación de JSESSIONID, HttpOnly, ausencia de la
credencial P03 en la respuesta del navegador, CSRF, autorización de asistente,
PRG, consultas privadas y respuesta 503 al apagar Central. Las pruebas de
transporte unitarias conservan rechazo de confianza ajena/SAN incorrecto,
malformación de encabezados, vencimiento fijo y cierre concurrente.

No se repitieron interacciones visuales manuales de Swing ni una defensa entre
dos computadoras. No se configuró firewall ni se desplegó en LAN. La confianza
TLS y los certificados de despliegue siguen siendo configuración externa.
La IT utiliza `java.net.http.HttpClient` con CookieManager; no automatiza un
navegador gráfico. JavaScript se verificó mediante las 61 regresiones de Node.
Otros dominios del roadmap (seguimiento, PDF, móvil, ranking, archivo y
finalización) no se implementaron dentro de esta integración del integrante 2.
Sus acuerdos pendientes permanecen en CONTRATOS-EQUIPO; no son regresiones de
los casos integrados.

## Evidencia, preservación y commits

Se inventariaron y copiaron 238 archivos originales con SHA-256 antes de editar.
El respaldo, manifests, árboles aislados, diffs y logs permanecen en
`revision-commits-local/desarrollo-integral/`, fuera de los commits. El contenido
del índice de cada bloque se contrastó con todo el árbol aislado validado y se
comprobó que confirmar no modificara los bytes del trabajo local. La rama del
compañero permanece intacta.

| Commit | Bloque |
|---|---|
| b96f599 | Etapa 2 atómica, autorizada y sin restauración de hash obsoleto |
| 343566e | Consulta y privacidad de patrocinios |
| 47fc75c | P03 y publicación HTTPS |
| 0e3ea83 | Alta atómica de instituciones |
| b188188 | Casos y proyecciones autorizadas del portal en Sistema |
| 72ed4d8 | Contratos independientes y Portal SOAP |
| 713e0f3 | Migración completa de consumidores Web |
| 3d3b6d0 | Integración real Tomcat/Central y verificaciones de aislamiento |
| 27694c5 | Institución vigente al registrar, autorización solo en Central y regresión de los contratos regenerados |

El informe del commit de documentación se obtiene con `git log`; no contiene
artefactos, logs, respaldos ni credenciales. Los documentos de auditoría de
10–11/11 se conservan como historia y sus hallazgos se cierran en este informe.

Dos controles auxiliares produjeron `AssertionError` por causas distintas de
un fallo del parche. En la auditoría anterior, un log PowerShell UTF-16LE se
leyó como cp1252: los caracteres NUL impedían encontrar `BUILD SUCCESS`. En la
verificación posterior a `27694c5`, se editó anticipadamente la plantilla
`web.properties.example` mientras seguía el control de hashes del trabajo
local. Se repitió entonces una comparación completa del commit contra el
árbol aislado validado: **cero diferencias**, índice vacío y únicamente las
tres líneas previstas de plantilla pendientes para documentación. El resultado
está en `verificacion-postcommit-seguridad.json`, fuera de los commits.

## Ejecución y repetición

1. Seleccionar JDK 25 y Maven. Construir con `mvn -DskipTests package`.
2. Configurar externamente `~/.eventosUy/central.properties` según la plantilla
   de `servidor-central`, con PostgreSQL autorizado, PKCS#12, claves y SAN
   correctos. Central valida el esquema existente; no lo recrea.
3. Ejecutar `java -jar servidor-central/target/servidor.jar`. Swing se abre en
   ese mismo proceso y llama a ISistema. Para Central sin interfaz:
   `java -jar servidor-central/target/servidor.jar --sin-swing`.
4. En la cuenta de Tomcat, crear `~/.eventosUy/web.properties` con `central.host`,
   `central.puerto`, `central.ruta.portal=/portal` y
   `central.ruta.sesiones=/sesiones`. Puede elegirse otra ubicación mediante
   `-Deventos.web.config=C:/ruta/web.properties`.
5. Configurar la confianza del certificado público en la JVM de Tomcat
   (`javax.net.ssl.trustStore`, tipo y contraseña externos). No copiar la clave
   privada de Central ni desactivar la validación de certificados/hostname.
6. Desplegar `webapp/target/web.war` y abrir el contexto `/web`. Tomcat no necesita
   configuración ni conectividad PostgreSQL. Para despliegue público, configurar
   también HTTPS del navegador al contenedor/proxy según el entorno.

Regresiones con datos de prueba exclusivamente:

```powershell
$env:EVENTOS_DB_NAME = 'eventosuy_test'
mvn package
node --test webapp/src/test/js/*.test.cjs
$env:TOMCAT_HOME = 'C:/ruta/apache-tomcat-11'
mvn '-Dtest=TomcatPortalHttpsIT' '-Dsurefire.failIfNoSpecifiedTests=false' test
java '-Djava.awt.headless=true' -cp 'estacion-trabajo/target/test-classes;estacion-trabajo/target/classes;logica/target/classes' swing.AltaPatrocinioPanelActorTest
```

La barrera JDBC forma parte de las suites persistentes y la IT. Si no confirma
`eventosuy_test`, esas pruebas fallan antes de inicializar persistencia. Para el
recorrido Tomcat también puede utilizarse
`webapp/src/test/manual/verificar-portal-https.ps1` con rutas de JDK/Tomcat.
