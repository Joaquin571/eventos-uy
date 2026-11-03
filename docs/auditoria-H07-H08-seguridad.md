# Auditoría H07/H08: seguridad, autorización e integridad

> Actualización: S01–S03 fueron corregidos en una tanda posterior. Las reproducciones HTTP ahora pasan (63 comprobaciones, cero fallos). Ver `correcciones-S01-S03.md`. El informe siguiente conserva los resultados históricos de la auditoría inicial; su salida fallida y su afirmación de no modificar producción corresponden a aquella ejecución.

Fecha: 03/11/2026. Rama `dev`, commit base `0284e7cc1240fc7e7f08d5454d70aea0bc56301b`, con las correcciones anteriores todavía sin commit.

## Resultado ejecutivo

Se reprodujeron tres defectos acotados. No se reprodujo acceso HTTP a perfiles privados, registros, imágenes privadas ni operaciones de otro organizador mediante los parámetros ensayados. Esto no demuestra ausencia de cualquier vulnerabilidad.

| ID de esta auditoría | Severidad | Resultado | Prioridad propuesta |
|---|---|---|---|
| S01 | MEDIA | Colisión entre nickname de un usuario y correo de otro impide un inicio de sesión válido | P1 |
| S02 | MEDIA | Alta con un campo demasiado largo produce HTTP 500 y expone detalles Hibernate/JDBC en el Tomcat ensayado | P1 |
| S03 | MEDIA | Fallo de almacenamiento de imagen comunica error de perfil aunque los datos y la contraseña ya se confirmaron | P1 |
| H07 | Limitación arquitectónica | Métodos públicos de manejadores permiten saltar reglas desde Java confiable; no se encontró ese camino en clientes HTTP/Swing | Documentar; sin refactorización general |
| H08 | Limitación arquitectónica | ISistema no recibe un actor autenticado; web autoriza antes de llamar y Swing actúa como administrador | Documentar; revisar antes de una interfaz remota |

No se modificó producción, `ISistema`, configuración de base ni esquema durante esta auditoría. No se ejecutaron migraciones. Los únicos datos escritos fueron fixtures propios en `eventosuy_test`, después de confirmar por JDBC `SELECT current_database()` mediante H02. No se consultó la base habitual.

## Fuente normativa y frontera de confianza

Se contrastaron los apartados del PDF oficial: §3 (distribución/JAR embebido y futura sustitución remota), §5 (actores), §6.1 (administración Swing), §6.2 (casos web, sesión, perfil propio, visibilidad, registros y patrocinios), §7.3 (validaciones), §7.4 (MVC/persistencia) y §7.5 (pruebas).

La letra exige nickname único y correo único; no establece una prohibición de `@` en nicknames ni una unicidad combinada entre ambas columnas. Tampoco establece que la instancia local de ISistema sea un servidor de autorización independiente del cliente. No corresponde inventar esas reglas para justificar un resultado.

Arquitectura observada: navegador → filtro/controladores web → ISistema/Sistema → manejadores → JPA/Hibernate → PostgreSQL. Swing → ISistema/Sistema → los mismos manejadores → PostgreSQL. Cada aplicación incluye su propia lógica; no hay llamada remota entre ellas.

Los límites de seguridad son distintos:

- Un usuario HTTP controla parámetros y archivos, pero no el objeto `usuario` de HttpSession ni las clases ejecutadas en el servidor.
- Swing es la herramienta del administrador sin login, según la letra. Que permita seleccionar otro usuario no equivale a una elevación de privilegios web.
- Un programa Java con acceso al JAR y credenciales de base puede llamar métodos públicos internos o abrir JDBC. El JAR no constituye una barrera contra ese programa.

## S01 — Ambigüedad de identidad al autenticar

**Evidencia:** `ManejadorUsuarios.obtenerPorNicknameOCorreo`, líneas 215–232; `Sistema.autenticar`, líneas 76–90; `LoginServlet.doPost`, líneas 26–44.

La consulta busca `nickname = :clave OR LOWER(correoElectronico) = LOWER(:clave)` y selecciona `findFirst()`. Luego se verifica únicamente la contraseña de esa fila. Dos cuentas legales pueden satisfacer la consulta; no hay orden ni resolución de ambigüedad.

**Reproducción ejecutada:** crear A cuyo nickname sea `colision<unico>@test.uy` y cuyo correo sea otro; crear B con nickname diferente y ese correo. Contraseñas distintas. Ambas altas fueron aceptadas. Al autenticar con el identificador compartido, A obtuvo HTTP 302 y B HTTP 200 con formulario de error, pese a usar su contraseña válida.

**Impacto:** disponibilidad del login por correo y selección no determinista de candidato. No se demostró apropiación de cuenta ni omisión de la verificación del hash. B todavía puede entrar por su nickname.

**Prueba:** `AuditarSeguridadWeb.auditarLoginAmbiguo` comprueba ambos inicios de sesión y falla con el comportamiento actual.

**Corrección localizada propuesta:** resolver todos los candidatos dentro de la implementación de autenticación y verificar sus hashes; autenticar solamente cuando exista una única cuenta compatible con las credenciales. Para el caso de dos cuentas compatibles con la misma contraseña hace falta una decisión explícita y segura: rechazar la ambigüedad o acordar otra política. No introducir silenciosamente una nueva prohibición de nicknames. Agregar regresiones para colisión cruzada, contraseñas distintas/iguales y correo con otras mayúsculas.

**Riesgo de cambio:** medio: conservar autenticación normal por ambos identificadores y mensajes genéricos. No requiere cambiar ISistema ni índices.

## S02 — Entrada excesiva: error interno visible

**Evidencia:** `AltaUsuarioServlet.doPost`, líneas 36–88; `Sistema.altaAsistente`/`altaOrganizador`; campos String JPA de `Usuario`; `ManejadorUsuarios.addUsuario`, líneas 24–65.

**Reproducción ejecutada:** POST multipart de alta de usuario con CSRF válido y `nombre` de 256 caracteres; restantes campos válidos. PostgreSQL rechazó el valor por `character varying(255)`. La excepción de persistencia no fue traducida a error de formulario: HTTP 500. La respuesta del Tomcat de prueba contenía detalles internos de Hibernate/JDBC.

**Impacto:** divulgación técnica y tratamiento incorrecto de una entrada controlable por un visitante. No se demostró exposición de datos de otras personas ni inyección SQL. La consulta posterior confirmó que el usuario no quedó insertado: el rollback de esta alta funciona.

**Corrección mínima propuesta:** validar en lógica los límites que ya impone el esquema y devolver un error de negocio claro antes de persistir; reflejar esos límites en el formulario. Añadir un manejo de error inesperado con mensaje público genérico y registro interno controlado, sin devolver trazas al cliente. Revisar longitudes de los demás campos, sin inventar reglas ajenas al esquema.

**Pruebas necesarias:** longitud máxima y máxima + 1, ausencia de inserción parcial, respuesta sin trazas, conservación de campos salvo contraseña y CSRF todavía obligatorio.

**Riesgo de cambio:** bajo/medio si se limita a las restricciones efectivas. El error visible depende también de la configuración de informes de error de Tomcat; ocultar el informe no resuelve por sí solo la validación faltante.

## S03 — Perfil e imagen: resultado parcial comunicado como fallo

**Evidencia:** `ModificarUsuarioServlet.doPost`, líneas 55–78: primero llama a `modificarAsistente`/`modificarOrganizador`, después a `asociarImagen`, y solo entonces refresca el DTO de sesión y redirige. `ManejadorUsuarios.modificarAsistente` y `modificarOrganizador` confirman su propia transacción.

**Reproducción ejecutada mediante inyección controlada de fallo:** mantener real toda la lógica y sustituir exclusivamente `asociarImagen` por una excepción `IllegalArgumentException` después de validar un PNG válido. Enviar cambios de perfil y nueva contraseña. La respuesta volvió al formulario con error, pero una lectura nueva confirmó el nombre modificado y la autenticación con la contraseña nueva.

**Impacto:** el usuario recibe un resultado engañoso y el DTO de sesión no se actualiza. No hay modificación de un perfil ajeno. No se simuló una caída real de PostgreSQL: se probó de forma determinista el recorrido de error entre dos operaciones independientes.

**Importante:** H05 sigue garantizando atomicidad entre datos del perfil y contraseña. Este hallazgo afecta al conjunto perfil + imagen en el controlador, no demuestra un fallo del rollback de H05. La letra no exige expresamente una única transacción para la imagen opcional.

**Corrección mínima propuesta:** tratar el fallo de imagen posterior al commit como éxito parcial explícito, actualizar la sesión y aplicar PRG con advertencia: perfil guardado, imagen no actualizada. Es el patrón ya utilizado en altas con imagen opcional y evita ampliar ISistema. Si el equipo exige todo-o-nada incluyendo imagen, hará falta diseñar una operación transaccional común; no proponerla como parche mínimo.

**Pruebas necesarias:** éxito completo; imagen rechazada antes de modificar nada; fallo de asociación posterior con datos/contraseña ya guardados, mensaje preciso y sesión renovada. Riesgo bajo para el manejo localizado de resultado parcial.

## H07 — Acceso público a manejadores

**Caracterizaciones ejecutadas con Java y persistencia real:**

1. ISistema rechaza el registro en edición Ingresada, pero `ManejadorRegistros.registrar` permite registrarla si se invoca directamente. La fecha proporcionada también llega al registro.
2. Tras ocupar un tipo con cupo 1, la fachada rechaza al segundo asistente. En cambio, construir un `Registro` con costo -999 y llamar a `ManejadorRegistros.addRegistro` permite persistir un segundo registro.

**Evidencia:** `ManejadorRegistros.registrar`, línea 158; `addRegistro`, línea 82; constructor de `Registro`; métodos públicos `obtenerUsuario`, `obtenerTipoRegistro` y `obtenerEdicion`. Auxiliar `logica/src/test/manual/AuditarFronteraLogica.java`.

Esto demuestra que los métodos internos no constituyen una API de negocio segura. No demuestra un camino HTTP: la búsqueda de imports/llamadas de los clientes no encontró acceso a manejadores ni entidades persistentes en los casos de uso. Web y Swing usan ISistema. `NivelPatrocinio` importado desde `clases` es un enum, no una entidad JPA.

`ContextoListener` accede a `BaseDeDatos` para el ciclo de vida de persistencia, fuera del procesamiento de casos de uso. No se encontraron consultas SQL/JPQL ni EntityManager en los controladores o paneles.

**Conclusión:** cerrar H07 como limitación de encapsulamiento documentada en esta distribución local, no como vulnerabilidad HTTP reproducida. No es necesario ocultar todos los manejadores ni reorganizar paquetes antes de entregar. Para mejorar defensa en profundidad se pueden restringir métodos sin clientes o repetir invariantes críticas en el límite persistente, con pruebas de compatibilidad; eso sería trabajo posterior, no una corrección de autorización web demostrada.

## H08 — ISistema sin identidad autenticada

`autenticar` devuelve un DTO; no instala un actor dentro de Sistema. `cambiarContrasenia(nickname, ...)`, consultas de registros y consulta de patrocinio no comprueban por sí solos quién llama. La caracterización autenticó A y luego pudo cambiar la contraseña de B mediante el mismo objeto ISistema.

**Alcance real:** requiere ejecutar código Java confiable con el JAR. Swing necesita operaciones administrativas y la letra no le exige login. El cliente HTTP no elige el nickname que modifica: `ModificarUsuarioServlet.perfilActual` toma la identidad de sesión. `RegistroEdicionServlet` toma al asistente de sesión; propietario y visibilidad se comprueban antes de consultar recursos.

**Conclusión:** cerrar H08 como frontera de autorización documentada y responsabilidad del adaptador web, sin cambiar ISistema. No se encontró una petición que lo convirtiera en acceso a otro usuario en los escenarios ensayados. Antes de exponer esta API por RPC habría que añadir una frontera autenticada/autorizada del lado servidor, no confiar en identificadores enviados por clientes remotos. No sería correcto publicar ahora la fachada completa como servicio abierto.

## Controles comprobados y diferencias entre clientes

| Superficie | Protección observada | Evidencia de ejecución/inspección |
|---|---|---|
| Sesión | Login invalida sesión previa; logout solo POST; token previo inválido después de logout | `LoginServlet`, `LogoutServlet`, `FiltroAcceso`, prueba HTTP |
| Rol | Mapa de rutas restringidas y rol derivado del DTO de sesión | Asistente con CSRF válido recibe 403 al intentar alta de evento |
| Perfil | Nickname y tipo salen de sesión; correo/nickname del POST no cambian el objetivo | POST manipulado modifica solo al usuario autenticado |
| Propiedad de edición | Organizador sale de sesión en alta; selección ajena se rechaza | GET/POST de alta de patrocinio ajeno 403; tipo ajeno no creado |
| Registros privados | Asistente consulta sus registros; organizador verifica sus ediciones | Parámetros de otro asistente no revelan su registro; organizador ajeno 403 |
| Edición no aceptada | Aceptada o propietario; identificación inválida/no visible devuelve 404 | Visitante, otro organizador y asistente: detalle e imagen 404; dueño: detalle 200 |
| Costo/identidad de registro | Calculados por lógica y sesión | POST con costo 0 y otro nickname registra al actor correcto por costo 100 |
| JSP privadas | Bajo WEB-INF | Acceso directo y variante de mayúsculas ensayada: 404 |
| XSS de perfil | Datos con `c:out`; sitio web mostrado como texto | Payload `<script>` escapado y `javascript:` sin enlace ejecutable |
| Patrocinio privado | Helper de propiedad/visibilidad y DTO público sin código | Inspección de `AccesoPatrocinios`, `ConsultaPatrocinioServlet`; regresiones existentes |
| CSRF/multipart | Token de sesión validado antes de POST; formularios multipart activos | Altas y modificación reales del auxiliar usan tokens; suite CsrfTest/SubidasTest |

Una instancia Java puede leer datos que un navegador no recibe: DTO no equivale a autorización. Esta distinción debe explicarse en la defensa.

Swing puede modificar cualquier usuario y ver estados no públicos porque es administración. Eso está previsto por §6.1. Se observó además que lógica/Swing permiten institución sin sitio web y el formulario web lo exige; es una diferencia de validación de interfaz, no una vulnerabilidad ni un incumplimiento obligatorio demostrado aquí. No se cambió esa política.

No se reprodujeron inyección SQL ni XSS en los recorridos probados. Las consultas inspeccionadas usan parámetros; esto no reemplaza una prueba exhaustiva de todas las entradas ni una auditoría de infraestructura.

## Integridad y transacciones

- Registro normal/patrocinado: `ManejadorRegistros.registrar` bloquea asistente, tipo y, si corresponde, patrocinio dentro de la transacción; vuelve a contar duplicados, cupo y usos antes de persistir. El costo y la asignación de patrocinio no salen del campo HTTP de costo.
- Las pruebas concurrentes existentes cubren último cupo, último uso patrocinado, mismo asistente en tipos distintos, unicidad de usuarios/correos, tipos, ediciones, instituciones y patrocinios. Se ejecutaron nuevamente, no solo se leyeron.
- H13 conserva índices de unicidad de correo y tipo en la base de pruebas; no se recrearon mediante migración en esta auditoría.
- `ManejadorEventos.cambiarEstadoEdicion` bloquea la edición y comprueba el estado dentro de la transacción. Aceptada/Rechazada no vuelven a Ingresada por la fachada. No se confirmó una carrera de cambio inverso durante registro a través de la API prevista.
- Perfil + contraseña: mismo commit en los métodos H05. Perfil + imagen: dos operaciones; ver S03. Altas + imagen también son operaciones separadas, pero sus controladores contemplan advertencia de fallo posterior; no afirmar atomicidad global.
- Los atajos directos de H07 sí rompen invariantes y quedaron demostrados, pero no son el recorrido de los clientes actuales.

## Pruebas ejecutadas y resultado

Con JDK Temurin 25.0.5 y guardia H02:

| Ejecución | Resultado |
|---|---|
| `mvn -Pcobertura clean install` | BUILD SUCCESS, tres módulos compilados |
| JUnit lógica | 225 pruebas, 0 fallos, 0 errores, 0 omitidas |
| JUnit web | 73 pruebas, 0 fallos, 0 errores, 0 omitidas |
| JavaScript | 56 pruebas aprobadas, 0 fallos/omitidas |
| Auditoría HTTP, WAR real en Tomcat 11.0.27 embebido | 27 comprobaciones correctas, 3 expectativas fallidas deliberadamente conservadas como evidencia S01–S03 |
| Caracterización de frontera Java/JPA | 17 comprobaciones correctas; confirman el alcance local de H07/H08 |
| `git diff --check` | Sin errores |

Las pruebas HTTP son de integración, no JUnit ni inspección visual de navegador. Las caracterizaciones de frontera también usan PostgreSQL real. No se suman a las 225/73 pruebas automáticas de Maven. Los tres defectos se mantienen sin corregir por autorización expresa de auditar primero.

JaCoCo de esta ejecución: **1468/1756 líneas = 83,599088838 %**, 288 sin cubrir; **567/728 ramas = 77,884615385 %**. Sigue superando el 80 %. Difiere de 83,77 % comunicado en la ejecución anterior; no se modificó producción en esta auditoría y no se aisló aquí la causa de variación entre ejecuciones. No presentar el porcentaje anterior como resultado de esta corrida.

Informe: `logica/target/site/jacoco/index.html`. Los auxiliares de auditoría no forman parte de Surefire ni de esta medición de cobertura.

Se generaron exclusivamente datos propios con sufijos únicos. El auxiliar H07 dejó, intencionalmente, registros de diagnóstico inválidos en esos fixtures de `eventosuy_test` para demostrar persistencia efectiva; no se borraron ni repararon registros. No utilizar esa base poblada de auditoría como conjunto de datos de demostración final sin preparar otro entorno controlado.

Tomcat informó al detenerse del desregistro del driver JDBC y de opciones `--add-opens` para detección de fugas; no prueban una fuga reproducida. El 500 de S02 sí es un error funcional confirmado y se informa por separado.

## Repetición en Windows

Desde la raíz, con PostgreSQL de pruebas disponible e índices H13 ya presentes:

```powershell
$env:JAVA_HOME = 'C:/Users/Joaco/.jdks/temurin-25.0.5'
$env:PATH = "$env:JAVA_HOME/bin;$env:PATH"
$env:EVENTOS_DB_NAME = 'eventosuy_test'
& 'C:/apache-maven-3.10.0-bin/apache-maven-3.10.0/bin/mvn.cmd' -Pcobertura clean install
node --test webapp/src/test/js/*.test.cjs
git diff --check

& ./webapp/src/test/manual/auditar-seguridad.ps1 `
  -TomcatRuta 'C:/Users/Joaco/Desktop/apache-tomcat-11.0.27' `
  -JdkRuta $env:JAVA_HOME
```

El último comando termina con error intencional mientras S01–S03 sigan presentes. Leer las líneas `OBSERVADO` y el resumen, no confundirlo con BUILD FAILURE de Maven. Arranca Tomcat limitado a loopback y puerto libre; no usa una instancia de producción. El auxiliar comprueba H02 antes de arrancar JPA/Tomcat.

Caracterización Java local, también después de la compilación:

```powershell
$cp = 'logica/target/test-classes;logica/target/classes;webapp/target/webapp-1.0-SNAPSHOT/WEB-INF/lib/*'
& "$env:JAVA_HOME/bin/java.exe" -cp $cp logica/src/test/manual/AuditarFronteraLogica.java
```

Este segundo auxiliar también ejecuta H02 antes de obtener la fachada/JPA. No ejecutarlo contra otra base ni retirar la protección para conseguir un resultado.

## Plan mínimo propuesto, sin implementar

1. S01: corregir selección de credenciales y acordar tratamiento de ambigüedad; pruebas de login cruzado. Sin migración ni cambio de API.
2. S02: validar longitudes compatibles con el esquema y manejar errores públicos sin trazas; probar límites y rollback. No ocultar errores sin corregir la entrada inválida.
3. S03: comunicar resultado parcial de imagen, refrescar sesión y redirigir; probar fallo antes/después del commit. No rehacer H05.
4. H07/H08: conservar documentación del modelo de confianza y el uso exclusivo de fachada. Revisar seguridad antes de RPC. No introducir una refactorización transversal en la entrega.

No hay dependencias obligatorias entre S01, S02 y S03. Cada corrección debe mantener CSRF, PRG, propiedad, validaciones de Sistema y suite actual, y necesita autorización posterior porque esta etapa solo investiga.

Pendiente/no verificado en esta auditoría: inspección visual humana Swing/navegador; revocación de todas las sesiones al cambiar contraseña; endurecimiento HTTPS/cookies/proxy del despliegue final; búsqueda exhaustiva de todas las variantes de ataque. No se elevan estas ausencias a incumplimientos de la letra sin evidencia normativa y funcional.

## Archivos añadidos por esta auditoría

- `docs/auditoria-H07-H08-seguridad.md`.
- `webapp/src/test/manual/AuditarSeguridadWeb.java`.
- `webapp/src/test/manual/auditar-seguridad.ps1`.
- `logica/src/test/manual/AuditarFronteraLogica.java`.

Los demás cambios/eliminaciones del árbol ya pertenecían a las tandas anteriores. No se restauró el prototipo. Logs de ejecución quedaron bajo TEMP y artefactos bajo target, fuera de los archivos propuestos para versionar. Se conservaron PDF y log local existentes. Sin staging, commit, merge ni push.
