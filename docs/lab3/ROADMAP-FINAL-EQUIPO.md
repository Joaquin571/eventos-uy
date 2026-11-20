# Roadmap final del equipo - Eventos UY, Laboratorio 3

Fecha: 16/11/2026. Base: c41a71e, lab3-desarrollo. Este plan no modifica código, no autoriza por sí mismo ejecutar migraciones y no reasigna tareas. Complementa [PLAN-INTEGRANTE-1](PLAN-INTEGRANTE-1.md) y [ESTADO-SERVLETS](ESTADO-SERVLETS.md). El objetivo es completar la letra conservando lo integrado, no reemplazarlo por ramas antiguas.

## Responsabilidades confirmadas y evidencia Git

La información nueva del usuario confirma **1 Joaquín González** y **2 Joaquín Bentancor**. Los documentos de 3/4/5 no llevan nombre personal. No se puede asignar Emmanuel, Valentina y Adrián uno a cada puesto por descarte: sus commits atraviesan bloques y no constituyen un acuerdo de reparto.

| Número / nombre | Asignado por documento | Implementado en nuestra base | Pendiente | Dependencias |
| --- | --- | --- | --- | --- |
| 1: Joaquín González | J01 contratos/identidad; J02 configuración/arranque/esquema/paquetes; J03 imágenes; J04 visitas y ranking; J05 integración, cobertura, docs, seed y ensayo | DTO/Metro/HTTPS/P03, paquetes reales, WAR remoto sin persistencia, imágenes atómicas y privadas, regresiones y documentación técnica. J03 cerrado actual | Visitas/ranking, P06/P08, README/instalación coherente, acuerdos finales, IntelliJ Coverage y cierre documental/datos/LAN | Acuerdos Evento con 3; filtro con 5; estados/imagen con 3/4; consumidores/sesión con 2; aportes docs/seed de todos |
| 2: Joaquín Bentancor | W01 sesión/roles/filtro/listener; W02 usuarios; W03 instituciones/patrocinios; W04 disponibilidad; W05 retirar lógica del WAR | Comportamientos incorporados/adaptados en Portal, P03 y Sistema. Usuarios/instituciones atómicos, privacidad y patrocinios, disponibilidad AJAX; runtime sin JPA | Adaptar guardia móvil de su rama a P03 con 5, revisión de consumidores/ampliaciones y entrega documental. No reintroducir SesionesUsuarios | J01/J03; D01/R01 ya base; seguimiento del 3, móvil del 5 |
| 3: nombre no confirmado | D01 eventos/ediciones/tipos remotos; D02 seguimiento; D03 video; D04 finalizar; D05 DSS/seed | D01 base migrado en nuestra integración; imágenes/tipos/propiedad y consultas existentes | D02-D04 y sus pruebas/datos/modelo; ampliar visibilidad pública/admin | W02/P03, J03; comparte Evento con J04/M03 y Edicion con R03/M03 |
| 4: nombre no confirmado | R01 registros remotos; R02 asistencia; R03 archivo; R04 PDF; R05 DSS/seed | R01 general/código y consultas autorizadas integrados, cupos/transacciones conservados | R02-R04 y pruebas/datos/modelo | W01/P03; D01/D03; P07 PDF y P08 esquema con 1; móvil consume desde 5 |
| 5: nombre no confirmado | M01 entrada móvil; M02 consultas/asistencia móvil; M03 búsqueda; M04 filtro visitas; M05 responsive/ensayo/docs | Base Bootstrap local, sesión y clientes reutilizables; ningún sitio móvil completo implementado | M01-M05, contratos/pruebas por bloque | W01, D03/D04, R02/R03, J03/J04; P05 descripción y P06 conteo |

### Qué demuestran los commits de las otras personas

- **Emmanuel Lacava:** 2911ab1, origin/lab3-emmanuel, 15/11: disponibilidad SOAP/AJAX, pruebas SOAP y JS; adaptado en 1cd5f5a. Sus commits de octubre (02b4b8c, 908522d, 742499e, 8b2778b) trabajan ediciones/tipos y 8ec0c7c agrega pruebas, integradas previamente. Su aporte reciente coincide con W04 del bloque 2 y otros con D01: esto **no confirma su número**, ni lo convierte automáticamente en integrante 3, 4 o 5.
- **Valentina Carrique:** 20af1e4/00e4750/b938a9c modifican Swing (perfil, imagen, estados) y 7db899c agrega 13 escenarios de eventos/categorías; 45053f5 los adapta a nuestra base. Alta/consulta evento Web en 2bcf7f9/970eb1e. Número Lab3 **no confirmado**.
- **Adrián Prado:** 65cfcb3 alta/consulta patrocinio y PatrocinioTest, más consultas/registro Swing y usuarios Web anteriores. Son aportes de dominios hoy repartidos entre 2/3/4; número Lab3 **no confirmado**.
- **Bentancor:** 1afff269, 14/11, raíz independiente; usuarios/perfiles/instituciones con imagen y fachada/sesiones. Se reaprovechó atomicidad, se sustituyeron infraestructura HTTP y sesiones renovables por Portal HTTPS/P03. Su número 2 queda confirmado por el usuario, no por atribución automática del nombre de rama.

No hay aportes remotos nuevos en el fetch de esta tarea. Hay una validación móvil reutilizable de Bentancor, no un sitio móvil listo para fusionar. Los documentos anteriores conservan contexto histórico; esta confirmación nominal prevalece para planificar.

## Reglas para todas las etapas

Arquitectura: navegador -> Tomcat -> SOAP/HTTPS -> Central -> Sistema -> manejadores -> PostgreSQL. Swing -> ISistema dentro del mismo proceso Central. Java 25 y Metro 4.0.5. Actor y P03 únicos, token solo servidor Web/Bearer, ningún rol administrativo remoto. Cada cambio conserva atomicidad, PBKDF2, validación de imagen, locks previos a hidratación, privacidad y patrocinios.

Los nombres de nuevas APIs, DTO, paneles y controladores que aparecen abajo son **propuestos**; no existen todavía. Revisar firmas/contratos con dueño y consumidor antes de implementarlas. Ningún stage publica código generado; se versionan fuente DTO/fachada y WSDL/XSD, regenerando cliente con Maven.

Cada bloque compila todos los módulos, ejecuta sus pruebas y las regresiones afectadas; no se ejecutan suites persistentes sin verificar previamente vía JDBC `current_database() = eventosuy_test`. Ante otra base se detienen las escrituras; no se sustituye por la habitual. Sin reset, pérdida de cambios, push ni integración remota automática.

## Etapa 0 - Tu preparación de contrato e instalación

1. **Función:** cerrar propuesta P06 y procedimiento P08/README; revisión nominal de contratos actuales. Es J01/J02 tuyo con decisiones compartidas, no implementación de dominios ajenos.
2. **Responsable:** Joaquín González; revisores 2, 3 y 5 según consumidor/modelo. No bloquea diseñar pruebas de contador.
3. **Archivos:** README.md, docs/lab3/CONTRATOS-EQUIPO.md, ARRANQUE-CENTRAL-HTTPS.md; procedimiento nuevo de esquema en docs y scripts versionados por proponer. Central/Main y ConfiguracionCentral se inspeccionan para describir su conducta; no cambiar validate a update para ocultar ausencia de instalación.
4. **Sistema:** sin método nuevo en este corte documental; acordar visibilidad del evento para visita y ranking local administrativo.
5. **Central/SOAP:** acordar incremento público, payload nombre y fault de recurso inexistente; mantener los tres servicios actuales.
6. **Web:** sin cambio a Servlet/JSP; acordar REQUEST del filtro con 5. Corregir instrucciones obsoletas sobre lógica en WAR y credenciales predeterminadas; enlace H13 ausente requiere solución reproducible, no redirección a un archivo supuesto.
7. **Pruebas:** validar comandos y vínculos, plan de esquema con datos previos; instalación real se ejecuta solo sobre base autorizada, primero eventosuy_test con JDBC.
8. **Aceptación:** P06 documenta refresh/invalidaciones/dispatchers/empates; P08 define creación y evolución segura sin destruir datos. Otra persona puede construir/configurar los artefactos siguiendo texto correcto.
9. **Commit:** `docs(despliegue): alinear instalacion externa y acuerdo de visitas`. Scripts/ejecución de esquema, si se aprueban, en commit separado comprobado.

## Etapa 1 - Tu primer bloque funcional: contador de visitas

1. **Función:** J04 contador persistente e incremento concurrente. Puede completarse sin la interfaz móvil ni el Filter.
2. **Responsable:** Joaquín González; coordinar Evento con 3. La implementación de finalizar sigue siendo D04 del 3.
3. **Archivos:** logica/clases/Evento.java, interfaces/ISistema.java, implementacion/Sistema.java, manejadores/ManejadorEventos.java; esquema/migración y tests de visitas nuevos. Revisar ManejadorImagenes para interacciones, sin reemplazar sus bloqueos.
4. **Sistema:** propuesta registrarVisitaEvento(nombre), valida existencia y visibilidad según P06; contador long inicial/backfill 0. Persistencia solo en manejador, actualización limitada al contador o lock previo a cargar; no leer-sumar-merge obsoleto.
5. **Central/SOAP:** sin publicación en este primer commit; la lógica debe compilar y probarse localmente antes del transporte.
6. **Servlet/JSP:** ninguno. No modificar EventoServlet ni ConsultaEdicionServlet para contar.
7. **Pruebas:** N incrementos paralelos producen exactamente N en base; dos eventos no se mezclan; inexistente/entrada inválida según política; rollback; carrera con imagen conserva ambos datos; cerrar/reabrir EMF conserva el valor. Cuando D04 exista, agregar carrera con finalización y visibilidad.
8. **Aceptación:** contador persistido sin pérdida ni escrituras ajenas; datos existentes conservados. La prueba de reinicio no puede limitarse a otra instancia del mismo singleton con EMF/cache abierta.
9. **Commit:** `feat(visitas): persistir incrementos concurrentes de eventos`.

## Etapa 2 - Tu ranking administrativo Swing

1. **Función:** J04 consulta top cinco y panel local, oficial §6.1/§7.7 (F04).
2. **Responsable:** Joaquín González. Depende del contador, no del Filter ni del móvil.
3. **Archivos:** DtEventoVisitado propuesto, ISistema/Sistema/ManejadorEventos; nuevo swing/ConsultaEventosMasVisitadosPanel.java y entrada en principal.java; pruebas correspondientes.
4. **Sistema:** propuesta obtenerEventosMasVisitados(Actor), exige administrador local, devuelve como máximo cinco posición/nombre/cantidad ordenados por visitas descendente y desempate acordado. La letra no fija desempate ni exige omitir eventos con cero visitas: no imponerlo silenciosamente.
5. **Central/SOAP:** ranking no se publica si no tiene consumidor remoto. Contrato de transporte no necesario para Swing.
6. **Servlet/JSP:** ninguno; no inventar página Web administrativa para este caso.
7. **Pruebas:** base sin eventos, menos/más de cinco, empates/cero visitas, orden y posiciones, conteos tras reinicio, Actor nulo/asistente/organizador denegados; panel usa ISistema y columnas exigidas. Añadir evidencia visual del panel real, aparte del doble headless.
8. **Aceptación:** administrador abre desde principal y ve posición, nombre y cantidad correctos sin SOAP ni nueva conexión propia desde Swing.
9. **Commit:** `feat(swing): consultar los cinco eventos mas visitados`.

## Etapa 3 - Compartido: incremento SOAP y Filter del integrante 5

1. **Función:** cerrar vínculo J04/M04: consulta de evento escritorio/móvil -> Filter -> SOAP -> contador.
2. **Responsable:** 1 implementa/revisa operación Central y contrato; 5 implementa Filter/mappings/consumidor. 2 revisa infraestructura Web si se toca ContextoListener/FiltroAcceso. No asignar el Filter individualmente a González.
3. **Archivos:** contratos/ServicioPortal y DTO si hace falta; PortalPublicador; WSDL/XSD del Portal; ClienteCentral; nuevo Filter propuesto FiltroVisitas; tests de HTTP/SOAP. FiltroAcceso de seguridad no se reutiliza mezclando política de conteo.
4. **Sistema:** reutiliza etapa 1, no duplica lógica. Acepta visitante cuando P06 lo habilite; no necesita una identidad de formulario para incrementar.
5. **Central/SOAP:** fachada delgada registrarVisitaEvento; fault seguro; HTTPS; cliente regenerado. Prueba contra servicio real, no solo mock.
6. **Servlet/JSP:** no tocar los Servlets de consulta para sumar; Filter mapea rutas reales de escritorio y móvil, REQUEST solamente según P06, sin duplicar por forward/error. No necesita JSP propia.
7. **Pruebas:** consulta válida una vez, refresh según acuerdo, inexistente sin incremento, FORWARD no duplica, imagen/estáticos no cuentan, Central caído controlado, concurrencia HTTP y persistencia real. No reintento automático de escritura ante timeout.
8. **Aceptación:** contador cambia por navegación real y se refleja en Swing después de reiniciar Central. Sin conteo en memoria de Tomcat ni JPA en WAR.
9. **Commits:** `feat(soap): exponer registro de visitas de eventos` (1); `feat(web): informar visitas mediante filtro SOAP` (5).

## Etapa 4A - Otros integrantes: seguimiento, video y finalizar

1. **Función:** D02 seguimiento, D03 video, D04 finalización; D05 acompaña. Son tres bloques independientes tras D01 base, no un commit gigante.
2. **Responsable:** integrante 3, nombre no confirmado; González revisa interfaces/modelo/contratos compartidos. Bentancor revisa perfiles que mantiene. No asignar el bloque a Emmanuel por inferencia.
3. **Archivos:** Usuario/Evento/Edicion y DTO, manejadores, Sistema/ISistema; consultas Swing de estados; JSP usuario/usuarios/edicion, alta-edicion y controladores; seed/modelo/DSS de su dominio.
4. **Sistema:** relaciones seguir/dejar y consultas; video opcional validado y persistido; finalizar y rechazar nuevas ediciones, visibilidad pública separada de consulta administrativa. D04 comparte fila Evento con visitas: conservar contador y lock antes de hidratar al modificar estado.
5. **Central/SOAP:** extender DTO/operaciones de seguimiento y finalización, video en alta/detalle, faults y snapshots; no aceptar rol/nickname de autoridad del formulario.
6. **Servlet/JSP:** nuevos controladores de acción de seguimiento/finalizar por acordar; modificar AltaEdicionServlet, ConsultaEdicionServlet, EventoServlet, InicioServlet, UsuariosServlet/UsuarioServlet y JSP relacionadas. Incluir iframe seguro de YouTube, no HTML arbitrario; para filtrar estados se llama Sistema, no JSP.
7. **Pruebas:** propio/duplicado/inexistente seguimiento; persistencia; con/sin video y URL embed; finalizar repetido/permiso, ocultación y alta posterior denegada; URL directa y consulta admin; concurrencia con visita e imagen.
8. **Aceptación:** F05-F08/F12 y partes de F03/F06; video funciona en escritorio y queda reutilizable móvil. No imponer propiedad para finalizar ni fin de todas las ediciones sin acuerdo P04: la letra no lo expresa.
9. **Commits:** `feat(usuarios): gestionar seguimiento autorizado`; `feat(ediciones): incorporar video opcional`; `feat(eventos): finalizar y filtrar consultas publicas`.

## Etapa 4B - Otros integrantes: asistencia, archivo y PDF

1. **Función:** R02 asistencia, R03 archivo, R04 constancia; R05 acompaña. Asistencia y archivo pueden avanzar en paralelo tras acordar Edicion; PDF depende de asistencia.
2. **Responsable:** integrante 4, nombre no confirmado; 3 revisa edición/visibilidad, 1 revisa contrato/esquema/PDF dependencia y 2 perfil consumidor.
3. **Archivos:** Registro/Edicion/EstadoEdicion y DTO, Sistema/ISistema, manejadores; DetalleRegistroVista, consultas registro/edición/perfil; nuevo controlador PDF y acción archivo; pruebas y seed/DSS.
4. **Sistema:** confirmar asistencia solo registro propio e idempotente; archivar propia ACEPTADA con fechaFin < hoy, registrar fecha y preservar registros; proyección autorizada para PDF. No prohibir asistencia por fecha si la letra no lo pide ni relajar aceptar/rechazar.
5. **Central/SOAP:** operaciones/datos autorizados de asistencia, archivo y constancia, contratos regenerables; no devolver registros ajenos para filtrar después. PDF puede generarse a demanda en Web o Central; decidir biblioteca/licencia P07 antes de agregarla.
6. **Servlet/JSP:** ampliar ConsultaRegistroServlet/ConsultaEdicionServlet/UsuarioServlet y vistas; nuevas acciones archivo/asistencia/descarga por acordar. Tipo application/pdf, sin archivo pregenerado persistido como mecanismo de entrega.
7. **Pruebas:** propio/ajeno/inexistente/repetido asistencia; archivo ajeno/Ingresada/Rechazada/ya Archivada/fin hoy/futuro; rollback y conservación de registros; visibilidad y acceso /imagen; PDF con ocho datos de letra y permisos; concurrentes relevantes.
8. **Aceptación:** F09-F11/F18 y partes de F03/F06/F10/F17; constancia propia solo con asistencia confirmada, archivada consultable por excepciones autorizadas.
9. **Commits:** `feat(registros): confirmar asistencia propia`; `feat(ediciones): archivar preservando registros`; `feat(constancias): generar PDF de asistencia autorizado`.

## Etapa 4C - Otros integrantes: móvil y búsqueda

1. **Función:** M01/M02 sitio móvil, M03 búsqueda, M05 responsive. M01 puede iniciar sobre consultas base, pero no cerrarse con video/asistencia simulados.
2. **Responsable:** integrante 5, nombre no confirmado; Bentancor adapta/revisa política de sesión móvil; 3/4 suministran servicios. González revisa infraestructura, no desarrolla el sitio móvil completo para cerrar su J04.
3. **Archivos:** rutas/vistas/CSS móviles dentro de webapp, cabezal.jsp; controlador/vista búsqueda; ISistema/Sistema/ManejadorEventos, DTO y contratos búsqueda; política móvil Central sobre P03. Reusar Bootstrap local.
4. **Sistema:** búsqueda nombre/descripcion case-insensitive, vacío, exclusiones y tres órdenes; fuente de descripción de edición P05 acordada con 3/4. Móvil consume misma asistencia y visibilidad. Reutilizar guardia Bentancor de 1afff269 como escenario, no su SesionesUsuarios.
5. **Central/SOAP:** operaciones búsqueda y política central de acceso móvil solo asistente; sin segundo registro de sesiones ni token en formulario. Verificar también rechazo de organizador con token de escritorio al acceder a funciones móviles.
6. **Servlet/JSP:** adaptar/reutilizar Login/Logout, ConsultaEdicion/ConsultaRegistro y FiltroAcceso con 2/3/4; nuevas rutas móviles con menú login/edición/registro/asistencia/logout; búsqueda en cabezal con enlaces/ordenes. No segundo WAR.
7. **Pruebas:** asistente válido, organizador rechazado, cierre vuelve login móvil, privacidad, asistencia real; búsqueda por texto/descripción/vacío/estados/tres órdenes; JS e iframe 16:9; teléfono físico, no solo emulación.
8. **Aceptación:** F14-F18, W01-W05/W08-W09; mismo WAR y exclusivamente SOAP/HTTPS. Las reglas se comprueban en Central, no se confía en ocultar el menú.
9. **Commits:** `feat(movil): integrar acceso y consultas por SOAP`; `feat(busqueda): consultar eventos y ediciones con orden`; `test(movil): validar recorridos en dispositivo real`. Separar correcciones de presentación si son independientes.

## Etapa 5 - Tu cierre de instalación e integración por cortes

1. **Función:** J02/J05, consolidar modelo/contratos y regresiones de cada entrega; terminar esquema/seed/docs.
2. **Responsable:** González coordina; cada dueño entrega su código, pruebas, datos y modelo/DSS. No esperar al último día para integrar todos los cambios compartidos juntos.
3. **Archivos:** scripts/procedimiento de esquema, README, docs/lab3, CargarDatosPrueba y aportes de datos; POM/contratos/fuentes tocados solo por incompatibilidades comprobadas. Inventario final de Servlets/Filter y WAR.
4. **Sistema:** sin nueva regla ajena por conveniencia; corregir incompatibilidades comprobadas preservando Actor, transacciones, bloqueos, imágenes y patrocinios. No ampliar tu alcance a implementar casos faltantes de otros sin una decisión explícita posterior.
5. **Central/SOAP:** regenerar snapshots/proxy y probar todos los servicios después de cada corte. Verificar que cambios de estado/DTO no rompan proyecciones existentes.
6. **Web:** consolidar consumidores y rutas de todos los bloques; revisar 21 base y nuevos controladores/filtros, CSRF, errores HTTP, JSP y enlaces; mantener WAR sin persistencia.
7. **Pruebas:** reactor Java 25 completo, unitarias/integración, seguridad/concurrencia, JS, Swing; SOAP/HTTPS real y Tomcat -> Central -> PostgreSQL; barrera JDBC. Medir IntelliJ Run with Coverage del árbol final >=80%; JaCoCo suplementario, no sustituto.
8. **Aceptación:** ningún caso marcado completo depende de datos simulados; seed cubre nuevos estados/relaciones/asistencia; documentación y diagramas coinciden con implementación. Artefactos arrancan desde instalación limpia y fuentes/recursos empaquetados, sin secretos.
9. **Commits:** uno por incompatibilidad real: `fix(integracion): ...`; luego `docs(entrega): completar modelo despliegue y datos demostrativos`. Evitar mezclar correcciones independientes bajo un solo commit genérico.

## Etapa 6 - Ensayo físico y validación final del equipo

1. **Función:** J05 cierre A03/A17/E09; no confundir implementación con evidencia de defensa.
2. **Responsable:** González organiza despliegue; 2/3/4/5 prueban sus dominios; 5 documenta teléfono.
3. **Archivos:** checklist/evidencias sin datos sensibles, configuración externa por nodo, instrucciones README y reportes de cobertura.
4. **Sistema:** sin nuevas operaciones por el ensayo; cualquier defecto encontrado vuelve a su dueño/bloque con regresión.
5. **Central/SOAP:** certificados SAN/confianza y destinos LAN correctos, cambio de IP/puerto por properties sin rebuild; vencimiento/revocación P03 conservados.
6. **Web:** WAR real Tomcat, navegador escritorio y celular; formularios/imagen/video/asistencia/PDF y errores sin trazas, sin lógica local.
7. **Pruebas:** cuatro dispositivos mismos nodos de la letra, sin IDE; abrir Swing/ranking real; probar cierre/reinicio Central y persistencia; no usar una base habitual para pruebas destructivas.
8. **Aceptación:** registrar dispositivos/roles/casos/resultados y pendientes cero bloqueantes; confirmar turno/Moodle con el equipo. La fecha de letra es 23/11/2026 09:00; horario del grupo no se inventa.
9. **Commit:** `docs(validacion): registrar ensayo distribuido del laboratorio` (solo evidencia publicable, no configuraciones con secretos).

## Etapa 7 - Preparar dev, cuando se cierre el criterio acordado

Fetch de esta tarea exitoso sin cambios en referencias existentes. `origin/dev` = `dev` = **5fb46db2743c10609c470c78b71ed8c210e8ba14**, ancestro de lab3-desarrollo; al corte **0 commits exclusivos dev / 25 exclusivos lab3-desarrollo** (incluye el informe c41a71e; no incluye el commit de estos planes). `origin/lab3-desarrollo` continúa en a29ee1f. El conteo exacto crecerá con la documentación y las etapas.

Ahora no hay conflicto textual: sería fast-forward. Eso solo resuelve compatibilidad de Git, no el cumplimiento. No hace falta esperar a que J04 exista para reconocer que el historial es compatible; sí hace falta cerrar J04 y los demás casos si se pretende publicar una entrega completa. Una eventual publicación como base en progreso requeriría distinguir explícitamente ese objetivo.

Antes de integración final: todos los casos exigidos y tareas acordadas con responsables revisados, regresiones sobre árbol exacto, contratos regenerados, WAR inspeccionado, autorización/concurrencia sin regresiones, documentación y seed, cobertura IntelliJ y ensayo físico. Resolver datos de entrada P04-P08, no imponer requisitos no escritos. Actualizar refs otra vez: si dev avanzó, comparar sus archivos/commits antes de resolver nada.

En una etapa de integración posteriormente autorizada: crear rama temporal desde origin/dev actualizado, incorporar lab3-desarrollo mediante merge local compatible (fast-forward si sigue ancestro), preservar ramas originales y repetir pruebas sobre resultado. Nunca allow-unrelated-histories con Bentancor ni copiar su raíz. Presentar resultado antes de push; **este plan no hace merge ni publica**.

## Qué podés empezar sin esperar a otros

Tu prioridad funcional es **J04 contador + pruebas concurrentes**, seguida del ranking Swing. Solo exige acordar P06 y el cambio de Evento con el 3/5. En paralelo podés corregir documentación de instalación y preparar P08/ensayo. No necesitás que Emmanuel sea identificado como 3/4/5 ni que el sitio móvil esté listo para avanzar en ese backend.

El cierre J05 sí depende de D02-D04/R02-R04/M01-M05 y de sus datos/diagramas. Esas dependencias se siguen como trabajo de equipo; no se incluyen como obligación individual adicional de González.
