# Integración del equipo sobre Laboratorio 3 · 15/11/2026

## Punto de partida y preservación

Se continuó en `lab3-desarrollo` desde
`2b7075281030ac80159c85654065d2dc0326b07b`, con 14 commits locales por delante
de `origin/lab3-desarrollo`. El índice y los archivos versionados estaban
limpios; únicamente estaba pendiente la evidencia de `revision-commits-local/`.
Se registraron hashes SHA-256 de los 303 archivos versionados. El respaldo
anterior de 238 originales se verificó íntegramente y se creó/verificó además
un bundle completo de las 57 referencias existentes después del fetch.

`git fetch --all --prune` agregó solamente `origin/lab3-emmanuel`; ninguna
referencia remota previamente existente cambió de SHA. No se hizo push, pull,
merge, rebase, cambio de rama, eliminación de archivos ni descarte de trabajo.
Las adaptaciones se confirmaron en commits locales por bloque.

## Identificación de Emmanuel y alcance real

La identificación es concluyente: `origin/lab3-emmanuel` contiene el commit
`2911ab1a8135bdc20b69e22c93a654bf1709c239`, firmado por **Emmanuel Lacava** el
15/11/2026 a las 13:21:11 -0300, con asunto «Implementar disponibilidad de
usuarios mediante SOAP». Su padre y ancestro común con nuestra rama es
`a29ee1fc9f2362e812ca939068a4fc205ca1985f`. Es su único commit exclusivo.

Modifica 10 archivos: publicador/cliente y WSDL/XSD de eventos, Servlet de
disponibilidad, JSP de alta de usuario, un script AJAX, tres pruebas SOAP,
dos escenarios JavaScript y documentación de avance. No modifica Swing,
entidades, DTO, manejadores, esquema de persistencia ni CSS.

La disponibilidad ya estaba implementada en nuestro Portal SOAP/HTTPS como
parte de la integración selectiva de Bentancor. Por eso no se incorporó
literalmente la extensión alternativa de EventosService ni un segundo script
para el mismo formulario. Se conservaron sus aportes compatibles y se adaptaron
sus pruebas al servicio y arquitectura vigentes.

## Cambios incorporados y conflictos resueltos

| Archivo o área | Nuestra versión | Aporte remoto y resolución |
|---|---|---|
| `Sistema.disponibleNickname/Correo` | Validación funcional y consulta de existencia centralizadas, límites de esquema y correo válido | La normalización de Emmanuel estaba en el publicador. Se trasladó a Sistema mediante `strip()`, preservando validación, nulidad, límites y comparación de correo existente. |
| `EventosPublicador`, cliente de eventos y WSDL/XSD | Contrato público de eventos conservado; Portal ya expone disponibilidad y ambos clientes exigen HTTPS | No se duplicaron operaciones con los nombres alternativos `nicknameDisponible/correoDisponible` en EventosService, ni se restauró el destino HTTP anterior. Se conserva la API de Portal. |
| `DisponibilidadUsuarioServlet` | Consulta por `campo`/`valor`, respuesta booleana JSON, acceso remoto exclusivamente | Se incorporó la respuesta JSON 503 sanitizada del aporte de Emmanuel. No se colocaron reglas funcionales ni consultas locales en el Servlet. |
| `FiltroAcceso` | CSRF previo a las escrituras y navegación por rol; cabecera/categorías para páginas | La consulta AJAX pública ya no necesita crear sesión ni consultar categorías. Esto permite que llegue al Servlet y conserve JSON ante una caída de Central. El control CSRF de POST sigue ejecutándose antes. Las operaciones privadas no cambian. |
| `disponibilidad-usuarios.js` | Debounce, cancelación, control de versión, validación del JSON y mensajes accesibles | Se incorporaron `cache: no-store` y la comparación del valor actual, incluso si cambió sin disparar un evento. Se conservó un único script. |
| `alta-usuario.jsp` | IDs, URL de contexto, mensajes `aria-live` y un script funcional | Se conservó el formulario actual: no se duplicaron IDs, mensajes ni solicitudes con el script alternativo de Emmanuel. |
| `DisponibilidadSoapTest` | Transporte Portal y validaciones de Sistema | Se adaptaron sus tres escenarios: identificadores normalizados, entradas vacías sin consulta de persistencia y fault sin detalles internos. Ahora prueban HTTPS real con cliente generado y Sistema; solo la consulta de existencia usa dobles. |
| Pruebas JavaScript y HTTP | Casos existentes de contexto, codificación, ocupado, carreras y errores | Se conservaron los dos escenarios de Emmanuel en las pruebas existentes y se agregó la regresión del valor cambiado sin evento. Una prueba filtro/Servlet exige JSON 503 y prohíbe crear sesión o consultar categorías. |

Los seis WSDL/XSD versionados permanecen sin cambios. La comparación automática
con wsgen y la regeneración wsimport aprobaron. No se añadieron dependencias.
Se mantienen Java 25, Metro 4.0.5, Actor y el registro único P03 de vencimiento
fijo, con Bearer solo entre Tomcat y Central.

## Otros aportes del equipo

| Autor / referencia | Evaluación y resultado |
|---|---|
| Joaquín Bentancor, `origin/cambios-eventos-uy-2026-11-14`, `1afff269` | Sin commits nuevos. Continúa siendo un historial independiente. Altas y modificaciones atómicas de usuarios, instituciones, consultas y disponibilidad ya estaban adaptadas; no se fusionó su proyecto raíz ni se duplicaron funciones. |
| Valentina Carrique, `origin/feature/tests-eventos-categorias`, `7db899c3` | Se detectaron e incorporaron sus **13 pruebas** de categorías, eventos y pertenencia de ediciones. La única adaptación funcional del ejecutor reemplaza la omisión por variable de entorno con `PruebaConBaseSegura`, que verifica la base efectiva mediante JDBC antes de escribir. Todos los casos originales se conservan. |
| Valentina, ramas de Swing y consultas ya ancestras de HEAD | Sus aportes anteriores ya forman parte de nuestra historia; no hay commits nuevos que aplicar. Se comprobó la compatibilidad Swing del árbol integrado. |
| Adrián Prado, `origin/feature/casos_adrian`, `65cfcb3f` | Ya es ancestro de HEAD: alta/consulta de patrocinios y pruebas conservadas con nuestras mejoras posteriores de autorización, privacidad y concurrencia. |
| Emmanuel, `lab2-Emmanuel` y `lab2-emmanuel-tests` | Ambas puntas son ancestras de HEAD; no hay aportes nuevos pendientes en ellas. |
| Emmanuel, `Revision2`, `cb4d004b` | Commit histórico de agosto sobre `src/` anterior a Maven/JPA. Sus relaciones asistente/institución, organizador/edición, registros y cupos están cubiertas por el modelo y persistencia actuales. No se restablecieron listas en memoria, fechas del cliente ni firmas antiguas. |
| `origin/Documentación`, cuatro commits de agosto | DSS de Laboratorio 1 y proyecto inicial, sin cambios funcionales nuevos de Laboratorio 3. Se conservaron como historia en la referencia y el respaldo; no se mezclaron con la documentación de arquitectura vigente. |

No se detectaron otros casos de uso nuevos en las referencias actualizadas.
La lista completa de 24 ramas inspeccionadas figura al final. Que un commit
antiguo no sea ancestro no implica que su funcionalidad falte: se compararon
los contenidos para evitar volver a versiones anteriores.

## Validación

| Comprobación | Resultado |
|---|---|
| Reactor aislado completo, Java 25, `-Pcobertura verify` | Aprobado en contratos, lógica, Swing, Central y Web |
| JUnit estándar | **453 aprobadas**: 306 lógica, 16 Central y 131 Web; 0 fallos, errores u omitidas |
| Integración Tomcat/HTTPS adicional | **1 aprobada**, con **51 solicitudes HTTP**, incluidas las consultas de espera de arranque |
| JavaScript | **62 aprobadas**, sin fallos ni omisiones |
| Swing headless | **5 comprobaciones aprobadas** de Actor y compatibilidad |
| Cobertura de líneas de lógica | **85,79 %**: 1715 cubiertas, 284 no cubiertas |
| Contratos SOAP | Los seis WSDL/XSD coinciden semánticamente con los regenerados; proxies wsimport compilados |
| WAR real | Sin lógica, JPA, Hibernate ni PostgreSQL; Tomcat usa exclusivamente clientes SOAP/HTTPS |
| Persistencia | Confirmación JDBC de `current_database() = eventosuy_test` antes de cada ejecución persistente; ninguna otra base utilizada |

La prueba real inicia un CATALINA_BASE temporal sobre Tomcat 11.0.27, publica
Central HTTPS y usa HttpClient con cookies. Comprueba altas, imágenes, perfiles,
contraseñas, instituciones, ediciones, tipos, patrocinios, registro con código,
autorización, CSRF, renovación/HttpOnly de cookie, logout y rechazo sin Central.
La versión de producción de esta prueba es idéntica a la del árbol final:
el bloque posterior solo añadió pruebas de lógica y documentación.

Las regresiones de contraseñas, imágenes, transacciones, institución concurrente
al registrar y cupos/patrocinios siguen aprobadas. No se reemplazaron sus
implementaciones. No se ejecutaron pruebas persistentes sobre desarrollo o
producción, ni se modificaron esquemas fuera de `eventosuy_test`.

El informe de cobertura y los logs están en
`revision-commits-local/integracion-equipo-20261115/`, fuera de los commits.
No se repitió revisión visual manual ni prueba entre dos computadoras; no se
modificó firewall ni se desplegó públicamente.

## Commits locales y estado

| Commit | Bloque |
|---|---|
| `1cd5f5a25084bcc3f4302f6aa555287e9787792d` | Disponibilidad de Emmanuel adaptada a Portal HTTPS: 7 archivos |
| `45053f53b626b6d1b3462c4a758897ffb0806188` | Las 13 regresiones de eventos/categorías de Valentina: 1 archivo |

El commit documental se identifica mediante `git log`. Antes de cada commit
de código se comprobó que el índice completo reprodujera el árbol aislado
validado y que confirmar no alterara el trabajo local. El índice final queda
vacío, sin modificaciones versionadas pendientes; se conserva únicamente
`revision-commits-local/` sin seguimiento como evidencia y respaldo.

No quedan funciones nuevas de estas ramas pendientes de integración. Los
dominios todavía pendientes del roadmap general mantienen el alcance previo:
esta sesión no inventa nuevas funciones de seguimiento, móvil, PDF o ranking.
No se publicó nada en GitHub.

Para repetir: definir `EVENTOS_DB_NAME=eventosuy_test`, ejecutar
`mvn -Pcobertura verify` y las pruebas Node; para Tomcat, configurar
`TOMCAT_HOME` y ejecutar `TomcatPortalHttpsIT` según el
[procedimiento vigente](INTEGRACION-2026-11-15.md#ejecución-y-repetición).

## Inventario de referencias remotas

Las autorías siguientes corresponden al último commit de cada referencia.

| Rama | SHA de la punta | Autor | Fecha | Resultado |
|---|---|---|---|---|
| `origin/ConsultaeventoyAltacat` | `196dac7b1271d36dbfe6bf01f2aaebb3e783d863` | valentina-carrique | 2026-08-30 | Ya es ancestro de HEAD |
| `origin/Documentación` | `1b64ac4e6016fe10a23682ede978d8f4e9279703` | Joaquin571 | 2026-08-17 | DSS historicos de Laboratorio 1 |
| `origin/Revision2` | `cb4d004bb1cb95b4fa65fed6d889c89915791e35` | Emmanuel Lacava | 2026-08-31 | Modelo historico; garantias cubiertas actualmente |
| `origin/Revision6` | `21d69b5e364bfdee450a6c50274b53c3629928e0` | Joaquin571 | 2026-09-10 | Ya es ancestro de HEAD |
| `origin/cambios-eventos-uy-2026-11-14` | `1afff26908f57886599c3630d12194e158f8ce88` | Joaquin Bentancor | 2026-11-14 | Raiz independiente; integracion selectiva previa |
| `origin/caso_consultaDeRegistro` | `389caede11f8137c4f6916e5d4c6a6e8c168762d` | valentina-carrique | 2026-09-13 | Ya es ancestro de HEAD |
| `origin/consultaeventoarreglado` | `a0489507a26cebd13d13b6a4e1718a186558d82c` | valentina-carrique | 2026-09-12 | Ya es ancestro de HEAD |
| `origin/dev` | `5fb46db2743c10609c470c78b71ed8c210e8ba14` | Joaquin571 | 2026-11-03 | Ya es ancestro de HEAD |
| `origin/feature/casos_adrian` | `65cfcb3f7674a8e2b6ebda81bb4977f110bb5a8c` | Adrian Prado | 2026-10-26 | Ya es ancestro de HEAD |
| `origin/feature/instituciones-patrocinios` | `f789eed23f5e31c13d5d784f023ae8589241aa28` | Joaquin571 | 2026-10-04 | Ya es ancestro de HEAD |
| `origin/feature/persistencia` | `d5293a00c74fc1898d88f678541d93d3017fdbeb` | Joaquin571 | 2026-08-31 | Ya es ancestro de HEAD |
| `origin/feature/registros-joaquinb` | `3b91fb311133681fec04aa01a686a1a3d54f46a5` | Joaquin571 | 2026-10-19 | Ya es ancestro de HEAD |
| `origin/feature/swing-usuario-imagenes` | `b938a9c7642719438b229b6eafe6bf731e5adf1c` | valentina-carrique | 2026-10-29 | Ya es ancestro de HEAD |
| `origin/feature/tests-eventos-categorias` | `7db899c3d580558f6266085ddf34c94c90e065d0` | valentina-carrique | 2026-10-30 | Pruebas adaptadas en 45053f5 |
| `origin/joaquin-b-registros` | `d7f2333ec3185e62ee55f482494ab41dca462ac9` | Joaquin Bentancor | 2026-10-25 | Ya es ancestro de HEAD |
| `origin/lab2-Emmanuel` | `db5b7f8aecbca59bca8ad02f0a92e002311aa48c` | Emmanuel Lacava | 2026-10-06 | Ya es ancestro de HEAD |
| `origin/lab2-cobertura` | `0284e7cc1240fc7e7f08d5454d70aea0bc56301b` | Joaquin571 | 2026-10-29 | Ya es ancestro de HEAD |
| `origin/lab2-emmanuel-tests` | `8ec0c7c23db3d23a965f184e695fb51575c704c2` | Emmanuel Lacava | 2026-10-29 | Ya es ancestro de HEAD |
| `origin/lab2-swing-datos` | `2524e695bfb7314ab65e408d7527f13abfa62da7` | Joaquin571 | 2026-10-26 | Ya es ancestro de HEAD |
| `origin/lab2-web-usuarios` | `47b3ccbcad188d294dc504e72fa511b58d9b078b` | Joaquin571 | 2026-10-19 | Ya es ancestro de HEAD |
| `origin/lab3-desarrollo` | `a29ee1fc9f2362e812ca939068a4fc205ca1985f` | Joaquin571 | 2026-11-06 | Ya es ancestro de HEAD |
| `origin/lab3-emmanuel` | `2911ab1a8135bdc20b69e22c93a654bf1709c239` | Emmanuel Lacava | 2026-11-15 | Adaptada en 1cd5f5a |
| `origin/main` | `075166cd8ab72b682dfaf7f657cc82b4a5a0342a` | Joaquin571 | 2026-08-04 | Ya es ancestro de HEAD |
| `origin/swingEdicionEventos` | `8569d035327b54bd79c95658efa1ea940bba85e4` | valentina-carrique | 2026-09-12 | Ya es ancestro de HEAD |
