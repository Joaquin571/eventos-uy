# Plan del integrante 1 - Joaquín González

Fecha: 16/11/2026. Corte de código: `c41a71e112cf4709443c84634fd9bcb2b77ee31c`, rama lab3-desarrollo. Este documento planifica: no implementa funciones ni modifica el reparto. La confirmación del usuario identifica a **Joaquín González como integrante 1** y a **Joaquín Bentancor como integrante 2**. Sustituye la incertidumbre nominal de informes anteriores; no cambia los bloques de las guías.

## Tu alcance exacto

La guía [INTEGRANTE-1](EQUIPO/INTEGRANTE-1.md), secciones 1-12, asigna J01-J05: acuerdos de transporte/identidad/faults; configuración, ciclo de vida, esquema y paquetes; imágenes remotas y adaptación ImagenServlet/Subidas; visitas persistentes concurrentes y ranking Swing; integración, cobertura, documentación, seed conjunto e instalación/ensayo distribuido. Incluye coordinar ISistema/DTO/POM, regeneración de contratos y revisiones de integración.

**No te asigna implementar los 21 Servlets ni los nuevos dominios de los compañeros.** Ya contribuiste a migrarlos en 713e0f3 y a completar el Portal en 72ed4d8: es trabajo integrado que se conserva, no una obligación de absorber seguimiento, video, asistencia, archivo, PDF, móvil o búsqueda. J05 coordina esos aportes y sus pruebas; los dueños funcionales siguen siendo 2-5.

Fuentes: letra oficial (14 páginas, §§3, 6.1, 7.1-7.3, 7.7, 7.9-7.11, 8), [REQUISITOS](REQUISITOS.md), [CONTRATOS-EQUIPO](CONTRATOS-EQUIPO.md), las cinco guías, ESTADO-ACTUAL, las auditorías del 15/11 y [ESTADO-APORTES-EQUIPO](ESTADO-APORTES-EQUIPO.md). Se revisaron código y pruebas actuales, no solo esos informes. ESTADO-ACTUAL conserva un cierre histórico del 11/11 y métricas del 15/11: no deben interpretarse como estado vigente de cada método.

## Estado por responsabilidad

| Bloque | Oficial / caso de uso | Estado de cierre | Implementación efectiva | Falta exacta |
| --- | --- | --- | --- | --- |
| J01 | A04-A10; SOAP, datos y errores. P03 es decisión de seguridad del equipo compatible con la letra | Parcial | contratos separado, ConversorDatos, DTO JAXB y fechas; Portal/Eventos/Sesiones; Actor derivado de P03; faults y WSDL/XSD regenerables | Registrar revisión funcional de contratos con los responsables confirmados y decisiones pendientes P04-P08; no declarar cerrados los contratos de dominios aún ausentes |
| J02 | A02, A05, A13-A18, E01-E03/E07; arranque/instalación | Parcial | Main, ConfiguracionCentral, ServidorHttpsCentral, BaseDeDatos; configuración externa, TLS previo a JPA, cierre; Maven produce servidor.jar y web.war; IT de ambos artefactos | Procedimiento de esquema nuevo/evolución y README coherente; apertura Swing real y cambio de destino/ensayo LAN sin reconstruir |
| J03 | A11, F02, acuerdos de imágenes; carga/recuperación desde Web | Completo para los casos y estados actuales | ImagenServlet/Subidas -> ClienteCentral -> PortalPublicador -> Sistema; bytes y MIME; validación en Central, atomicidad y privacidad de edición | Conservarlo. Ampliar matriz cuando 3/4 incorporen finalización/archivo y repetir ensayo físico; no reescribir transporte |
| J04 | F01/F04, W07 compartido, T01; consulta de los cinco eventos más visitados | Faltante | Evento carece de contador, ISistema/Sistema y ManejadorEventos no tienen visitas/ranking; no hay panel ranking | Contador persistente, incremento concurrente, top cinco, panel local; contrato de incremento con 5 y pruebas de reinicio/concurrencia |
| J05 | A01/A03/A17, T01/T02, E04-E09; cierre integrado | Parcial | WAR sin persistencia, pruebas remotas/seguridad/atomicidad y documentos técnicos | Medición IntelliJ >=80%, modelo/DSS finales, seed completo aportado por dominios, instalación limpia y ensayo de cuatro dispositivos, revisión de entrega |

### J01: archivos, operaciones y prueba

- Java: `contratos/src/main/java/contratos/ServicioPortal.java`, `ConversorDatos.java`, `dto/*`, `servidor-central/src/main/java/central/soap/{PortalPublicador,EventosPublicador,SesionesPublicador,MapeadorPortal,OperacionSoapException}.java`, `central/seguridad/SesionesRemotas.java`, `logica/src/main/java/datatypes/Actor.java`.
- Web: `web/remoto/{ClienteCentral,ClienteSesionesCentral,ConfiguracionWeb}.java`, `SesionWeb`, `FiltroAcceso`, `ContextoListener`. Login/logout pertenecen funcionalmente a Bentancor; vos coordinás identidad/transporte. JSP/JS propios de J01: ninguno; login.jsp y cabezal.jsp son consumidores compartidos.
- SOAP real: Portal tiene 26 operaciones; Eventos conserva listarEventos; Sesiones tiene iniciarSesion/cerrarSesion. Las operaciones nuevas de visitas/otros dominios todavía NO existen. Tokens por Authorization Bearer, sin Actor remoto, contraseñas de salida ni segunda sesión.
- Sistema: las operaciones del Portal delegan en sus equivalentes autorizados; `autenticar` se reutiliza desde P03. No agregar negocio a los publicadores ni una identidad global mutable.
- Pruebas: `ConversorContratoTest`, `ContratosRegeneracionTest.fuentesWsdlXsdCoincidenConLosContratosRegenerados`, `PortalSoapHttpsTest`, `SesionesSoapHttpsTest`, `EncabezadosSoapHttpsTest`, `SesionesRemotasTest` y `PortalAutorizacionTest`.
- Cierre: conservar lo integrado en 47fc75c/72ed4d8; registrar responsables revisores y acuerdos pendientes por extensión. La integración técnica y las pruebas existentes sí están demostradas; no hay evidencia de aceptación nominal de todos los bloques futuros.

### J02: archivos, operaciones y prueba

- Java: `servidor-central/src/main/java/central/{Main,ConfiguracionCentral,ServidorHttpsCentral}.java`, `logica/src/main/java/persistencia/BaseDeDatos.java`. POM raíz, Central, contratos y Web; `META-INF/persistence.xml`; plantillas .example.
- Main valida TLS, inicializa EMF con propiedades externas y `hibernate.hbm2ddl.auto=validate`, publica servicios y abre `swing.principal` en el mismo proceso. `--sin-swing` solo evita la ventana. Cierra endpoints, listener, ejecutor y JPA.
- Servlet/JSP/JS: no hay caso de negocio propio; ContextoListener crea clientes. El WAR usa `web/remoto/ConfiguracionWeb.cargar`; el cliente sustituye destino por properties y exige HTTPS.
- SOAP: ciclo de vida de los tres servicios existentes; no requiere un método nuevo de Sistema para configuración.
- Pruebas: `ConfiguracionCentralTest`, `ConfiguracionBaseTest`, `EventosPublicadorTest`, `ArranqueHttpsCentralTest`, `CentralJarArranqueIT`, `TomcatPortalHttpsIT`, `InicioSoapTest`.
- **Incumplimiento documental concreto:** README sección Credenciales sigue afirmando defaults localhost/postgres/root que 860f71e eliminó; sección Compilar dice que el WAR incluye lógica y da un nombre antiguo. En código actual el paquete es `webapp/target/web.war` y no contiene JPA. También enlaza `docs/migraciones/H13-procedimiento.md`, ausente del árbol actual. Main no crea un esquema nuevo. No instruir a otra persona a seguir ese enlace o arrancar sobre base vacía como si funcionara.
- Próxima corrección de J02: documentación única y procedimiento verificable de creación/evolución con revisión P08; sin improvisar DDL destructivo ni usar la base habitual. Los esquemas nuevos de 3/4/5 deben entrar luego en ese procedimiento.

### J03: recorrido Web concreto

| Tramo | Implementación | Prueba / alcance |
| --- | --- | --- |
| Multipart | `Subidas.leerImagen`, Part opcional, lectura acotada a 2 MB + 1; cinco Servlets @MultipartConfig (altas usuario/institución/evento/edición y modificar usuario) | SubidasTest: ausencia, tamaño declarado, flujo deshonesto y bytes intactos |
| CSRF/tamaño contenedor | `FiltroAcceso` -> `Csrf.validarPost`; captura IllegalStateException de parseo y devuelve 413 antes del Servlet | CsrfTest.limiteMultipartDelContenedorSeDevuelveComo413SinEjecutarElControlador; no es un defecto pendiente de manejo |
| Escritura | `ClienteCentral.altaUsuario/modificarUsuario/altaInstitucion/altaEvento/altaEdicion`, EntradaUsuario o datos+DtImagen | AltasPrgTest, ModificarPerfilAtomicoTest, suites de atomicidad, PortalPersistenciaHttpsTest |
| Validación Central | `Sistema.validarImagen` -> ValidadorImagen; altas/modificaciones compuestas llaman antes de persistir | ValidadorImagenTest, DecodificacionImagenTest, ImagenesTest; PNG/JPEG/GIF/WEBP y límite |
| Recuperación | `ImagenServlet.doGet` -> `ClienteCentral.obtenerImagen` -> `PortalPublicador.obtenerImagen` -> `Sistema.obtenerImagenVisible`; ManejadorImagenes obtiene bytes | ConsultasEdicionTest.imagenOcultaSeRechazaAntesDeLeerBytes y otros tres métodos de imagen; PortalAutorizacionTest; HTTP privado en Tomcat IT |
| Atomicidad/concurrencia | ManejadorUsuarios, ManejadorInstituciones, ManejadorEventos y ManejadorImagenes; lock antes de hidratar fila | AltaUsuarioConImagenTest, InstitucionConImagenTest, ModificacionPerfilConImagenTest, AsociacionImagenConcurrenciaTest, ConcurrenciaEscritoresRelacionadosTest, AltaEventoEdicionRollbackImagenTest |

`ImagenServlet` decide MIME/respuesta HTTP y fallback a img/default-avatar.png; no valida propiedad. Sistema autoriza la edición antes de leer bytes. La red transporta DTO de contratos + byte[], nunca archivos de disco compartido ni entidades.

JSP relacionadas: alta-usuario, modificar-usuario, alta-institucion, alta-evento, alta-edicion, evento, edicion, usuario y consulta-institucion; los src de imágenes llaman /imagen. JS: validacion-formularios.js, validacion-altas.js y alta-edicion-fechas.js; son presentación, no sustituyen validación Central. No hace falta un nuevo Servlet de subida ni un endpoint REST.

Pendiente adicional de calidad, sin rebajar lo demostrado: ampliar IT real con archivos que excedan límites de Tomcat y con todos los formatos. Hoy la excepción del contenedor está cubierta con doble y los flujos reales usan PNG. Al incorporar ARCHIVADA/finalizado se repite acceso directo a /imagen con 3/4.

### J04: lo que sí tenés que desarrollar primero

Nombres siguientes **propuestos, no existentes**: `Sistema.registrarVisitaEvento(nombre)` y `Sistema.obtenerEventosMasVisitados(Actor)`; `DtEventoVisitado`; `ConsultaEventosMasVisitadosPanel`. Acordar firmas P06 antes de implementarlas.

1. Agregar contador long persistente a Evento, valor inicial/backfill 0, esquema compatible con datos previos. Revisar modelo compartido con D04/M03; no esperar a que estén completos.
2. ManejadorEventos ejecuta incremento en una transacción: preferir actualización que solo toca contador o bloqueo previo a hidratación. Nunca merge de Evento obsoleto que restaure imagen/descripción/estado. Comprobar interacción con el bloqueo existente de ManejadorImagenes.
3. Sistema valida existencia y política de visibilidad; centraliza el criterio. Ranking administrativo requiere Actor local, máximo cinco, posición/nombre/cantidad, orden descendente. Desempate determinista a acordar (la letra no fija uno).
4. Panel Swing llama directamente a ISistema; entrada en principal. Sin cliente SOAP para ranking si no hay consumidor remoto.
5. Exponer incremento delgado por SOAP y DTO simple para el filtro del 5; actualizar WSDL/XSD y consumidor. No agregar conteo dentro de EventoServlet ni ConsultaEdicionServlet. El filtro, sus dispatchers y su política de refresh son M04, no tu tarea individual.
6. Pruebas: incremento existente/inexistente, N incrementos paralelos = N, carreras con imagen y futura finalización, rollback, ranking 0/<5/>5/empates, acceso administrativo, cierre/reapertura EMF conserva contador. Barrera JDBC primero, exactamente eventosuy_test.

Propuesta de P06 para revisión conjunta, no requisito inventado: contar consulta válida de evento mediante REQUEST una sola vez; refresh es otra visita; no contar recursos inexistentes ni dispatch FORWARD/ERROR/ASYNC; no visitantes únicos, IPs ni cookies de conteo. No reintentar escrituras automáticamente tras timeout. El usuario/equipo revisa esta decisión al aprobar la etapa de desarrollo.

### J05: integración compartida, no implementación ajena

- Ya: retiro de persistencia del WAR (713e0f3), autorización/locks conservados (27694c5/29f1b51), IT Tomcat real (3d3b6d0), pruebas e imágenes atómicas (1c995c5), configuración externa/JAR (860f71e).
- Falta: JUnit de visitas y regresiones de cada extensión al integrarse; IntelliJ Run with Coverage >=80%. JaCoCo previo 1733/2017 = 85,92% es evidencia adicional, no reemplazo del requisito.
- Modelo/DSS: vos consolidás arquitectura/visitas; 3/4/5 entregan sus diagramas/decisiones/datos. Los DSS LAB1 antiguos de Documentación no resuelven E04 del Lab3.
- Seed: `datos/CargarDatosPrueba` contiene casos anteriores; no seguimiento/video/archivo/asistencia. Los dueños de dominio aportan esos datos; vos integrás y documentás una carga segura. No ejecutar borrado/reinicialización por el comentario histórico del seeder.
- Ensayo: Central+Swing+PostgreSQL, Tomcat, navegador escritorio y teléfono en cuatro dispositivos de la misma red, sin IDE. HTTPS local con dos procesos no sustituye este ensayo. Registrar también cambio de IP/configuración sin rebuild y arranque Swing real.

## Porcentaje calculable de tu parte

No se usa cantidad de commits, líneas, Servlets ni puntos de dificultad. La siguiente es una **lista explícita de 28 criterios de cierre**, desglosados de J01-J05 y sus pruebas/definición de terminado. Es un indicador binario documental: completo = 1; parcial o faltante = 0. Todos pesan igual; no estima horas ni avance físico del laboratorio completo. Los criterios técnicos ya aprobados conservan su evidencia; no se suman por implementar funciones ajenas.

| Criterio | Bloque | Entregable verificable | Estado | Evidencia / falta |
| --- | --- | --- | --- | --- |
| C01 | J01 | DTO de transporte sin dominio/JPA | completo | contratos y ConversorContratoTest |
| C02 | J01 | Fechas y JAXB interoperables | completo | DTO/ConversorDatos y SOAP real |
| C03 | J01 | Identidad/rol confiables, P03 único | completo | SesionesRemotas, Bearer y pruebas P03 |
| C04 | J01 | Faults declarados y técnicos sanitizados | completo | PortalPublicador y PortalSoapHttpsTest |
| C05 | J01 | Servicios organizados y cliente regenerable | completo | wsgen/wsimport y ContratosRegeneracionTest |
| C06 | J01 | Acuerdos/revisión con consumidores y decisiones abiertas registrados | parcial | Contratos actuales documentados; falta revisión nominal y cierre de ampliaciones P04-P08 con sus dueños |
| C07 | J02 | Configuración externa Central/Web/JDBC/TLS | completo | ConfiguracionCentral/ConfiguracionWeb; 860f71e |
| C08 | J02 | Publicación antes de Swing y cierre íntegro | completo | Main/ServidorHttpsCentral y pruebas arranque |
| C09 | J02 | Un comando genera ambos artefactos | completo | verify/package previo, POM finalName |
| C10 | J02 | Artefactos ejecutables sin fuentes/IDE | completo | CentralJarArranqueIT y TomcatPortalHttpsIT; modalidad headless probada |
| C11 | J02 | Esquema inicial/evolución e instalación reproducibles | parcial | Main validate; enlace H13 ausente y README desactualizado |
| C12 | J02 | Configurar otros nodos/LAN sin reconstruir | parcial | Configuración y TLS probados localmente; falta ensayo LAN y Swing visual |
| C13 | J03 | Bytes y MIME por SOAP | completo | DtImagen y cliente/Portal |
| C14 | J03 | Formatos/límites/decodificación en Central | completo | ValidadorImagenTest y DecodificacionImagenTest |
| C15 | J03 | Privacidad de edición antes de leer bytes | completo | Sistema.obtenerImagenVisible y pruebas de acceso |
| C16 | J03 | Multipart acotado con CSRF y operación compuesta | completo | SubidasTest, CsrfTest y pruebas atómicas |
| C17 | J03 | Recuperación HTTP/fallback y bytes originales | completo | ImagenServlet y ConsultasEdicionTest |
| C18 | J03 | Errores de imagen sin éxito parcial y límite controlado | completo | Rollback tests; CsrfTest 413. Ampliación IT límite real es mejora de evidencia |
| C19 | J04 | Contador persistente con datos previos | faltante | Evento sin contador |
| C20 | J04 | Incremento concurrente y aislamiento probados | faltante | Sin método ni regresión de visitas |
| C21 | J04 | Top cinco administrativo y panel local | faltante | Sin API/panel |
| C22 | J04 | Conteos sobreviven reinicio | faltante | Sin persistencia/prueba del caso |
| C23 | J05 | WAR sin lógica/JPA/Hibernate/PostgreSQL | completo | 33 bibliotecas inspeccionadas en esta revisión; IT previa |
| C24 | J05 | Regresión de la base integrada y transporte real | completo | 466 JUnit/IT previas, 62 JS y 15 comprobaciones Swing; no certifica los casos ausentes |
| C25 | J05 | >=80% líneas medido con IntelliJ | parcial | JaCoCo 85,92%, no hay informe IntelliJ |
| C26 | J05 | Análisis/DSS/diseño final consolidado | parcial | Documentación técnica presente; faltan dominios nuevos |
| C27 | J05 | Seed conjunto completo reproducible | parcial | Datos anteriores; faltan nuevos escenarios |
| C28 | J05 | Ensayo físico de cuatro dispositivos registrado | faltante | Sin evidencia de defensa distribuida |

**17 completos, 6 parciales, 5 faltantes: 17/28 × 100 = 60,71% de criterios cerrados.** Distribución: J01 5/6; J02 4/6; J03 6/6; J04 0/4; J05 2/6. El 39,29% restante no implica la misma proporción de esfuerzo. Si solo se contaran los cinco bloques completos, habría 1/5 (J03); por eso no se presenta ese cálculo grueso como avance del trabajo.

## Secuencia inmediata y dependencias

1. Etapa documental/contrato: corregir README y procedimiento de esquema (J02), registrar P06 con 5 y cambios de Evento con 3 (J01). Este plan no modifica el README ni ejecuta DDL.
2. Tu primer bloque funcional: contador e incremento concurrente J04. Segundo: top cinco y panel Swing. Ambos compilables y verificables sin sitio móvil.
3. Contrato SOAP de incremento (J01/J04 compartido) y prueba real; integrante 5 conecta su Filter M04.
4. Vos cerrás J02/J05 mientras 3/4/5 entregan sus bloques. No desarrollar seguimiento/asistencia/móvil para simular que tu entrega individual avanzó.
5. [Roadmap de equipo](ROADMAP-FINAL-EQUIPO.md) fija archivos, pruebas, aceptación y commits por etapa. [Estado de Servlets](ESTADO-SERVLETS.md) distingue frontera remota correcta de casos nuevos pendientes.

De Bentancor necesitás revisión de sesión/consumidores, guardia móvil adaptada a P03 y compatibilidad del WAR, no otro mecanismo de autenticación. Del 3: acuerdos Evento/Edicion y visibilidad de estados; del 4: archivo/asistencia y proyecciones privadas; del 5: contrato/semántica de visitas y filtro, luego consumidor móvil. Modelo/DSS/seed de cada dominio son dependencias del cierre J05, no de empezar J04.

## Evidencia y límites de esta planificación

No se modificó código funcional ni se ejecutaron pruebas persistentes. Se leyeron los 21 Servlets, filtro/listener/CSRF/Subidas, clientes, fachadas, métodos de Sistema y pruebas; se inspeccionó el WAR actual y se actualizó Git. Las 466 JUnit/IT, 62 JavaScript y 15 comprobaciones Swing son resultados históricos del mismo código funcional ya validado, no una nueva ejecución completa de esta tarea. No se da por probado un recorrido nuevo inexistente.

`origin/dev` sigue en 5fb46db2743c10609c470c78b71ed8c210e8ba14, ancestro; 0 exclusivos de dev y 25 propios de lab3-desarrollo al corte. Fetch sin cambios. Solo integrar después de completar criterios acordados y ensayar; no hay merge ni push en esta actividad.
