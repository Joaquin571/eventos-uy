# Integrante 4 — Registros, asistencia, archivo y constancia

## 1. Objetivo

Migrar registros a SOAP e implementar asistencia, archivo de ediciones y constancia PDF. Mantener cupos, costos, patrocinios, duplicados y privacidad anteriores. Proveer al móvil operaciones de negocio reutilizables, no implementar otra lógica dentro de la interfaz.

Base: corte `586eab4`. Rama propuesta `lab3-registros-asistencia`, todavía no creada. Leer [contratos](../CONTRATOS-EQUIPO.md) y [guía Git](GUIA-GIT.md).

## 2. Tareas asignadas

| ID | Trabajo | Prioridad / dificultad | Aceptación |
| --- | --- | --- | --- |
| R01 | Migrar registro y consulta a SOAP. | P0 / 3 | General/con patrocinio conservan reglas y permisos. |
| R02 | Confirmar asistencia y mostrarla. | P1 / 5 | Solo registro propio; confirmación persiste; no ofrecer repetición si ya se confirmó. |
| R03 | Archivar edición. | P1 / 5 | Propia, Aceptada, fin anterior a hoy; fecha de archivo, registros conservados y visibilidad correcta. |
| R04 | Constancia PDF a demanda. | P1 / 5 | Propia con asistencia; campos de §7.5; application/pdf y sin PDFs almacenados previamente. |
| R05 | Evidencias, dominio/DSS y datos de demostración. | P2 / 2 | Registros con/sin asistencia, edición archivada y otra archivable; efectos y documentación verificables. |

## 3. Archivos y módulos

Lógica: `Registro.java`, `Edicion.java` en `logica/src/main/java/clases/`; `DtRegistro.java`, `DtEdicion.java`, `EstadoEdicion.java` en datatypes; `Sistema.java`, `ISistema.java`, `ManejadorRegistros.java` y `ManejadorEventos.java`.

Web: `RegistroEdicionServlet`, `ConsultaRegistroServlet`, `RegistroVista`, `DetalleRegistroVista`, `registro-edicion.jsp`, `consulta-registro.jsp`; aportes coordinados a `ConsultaEdicionServlet`, `edicion.jsp`, `UsuarioServlet`, `usuario.jsp`. Controladores/vista de archivo y descarga PDF nuevos por crear, sin nombre de clase fijado todavía. Publicadores y clientes del dominio; POM solo para biblioteca aprobada. Swing `ConsultaEdicionEventoPanel`, `ConsultaEventoPanel`/`ConsultaUsuarioPanel` si requieren mostrar archivo, coordinados con 3.

## 4. Métodos reales reutilizables

`registroAEdicion` de cinco parámetros ya usado por Web; conservar también la sobrecarga local de cuatro. `obtenerRegistrosAsistente`, `obtenerRegistrosEdicion`, `consultarUsuario`, `obtenerEdicionesOrganizador`, `obtenerEdicionesEvento`, `consultarTipoRegistro`, `obtenerTiposRegistroEdicion`.

`ManejadorRegistros.registrar` concentra registro; `ManejadorEventos.cambiarEstadoEdicion` usa bloqueo para aceptar/rechazar; `Edicion.aceptar/rechazar` solo parten de Ingresada. Archivo necesita transición específica desde Aceptada, no relajar todas las transiciones. `DetalleRegistroVista.de` prepara el detalle actual y todavía no contiene asistencia.

## 5. Nuevas funcionalidades

Agregar asistencia al modelo/DTO y operaciones de confirmación; estado ARCHIVADA y fecha de archivo; descarga de constancia. Hoy no existen esas capacidades en ISistema. No borrar registros al archivar ni suprimir el registro propio del perfil del asistente.

La letra no impone fecha de celebración como condición para confirmar asistencia; no inventarla. Archivo sí exige fechaFin < hoy, no fechaFin <= hoy. No reutilizar la fecha de alta como fecha de archivo.

## 6. Operaciones SOAP

Dueño de Registros/asistencia: registro general/con código, consultas y nueva confirmación. Coordinar archivo en el grupo de ediciones con 3. Datos de constancia: acordar operación específica o composición autorizada de lecturas; propuesta de generación PDF en Web, pendiente biblioteca/licencia y aprobación P07. Ningún PDF pregenerado en base/disco como mecanismo de entrega.

## 7. Dependencias

J01/P03 para identidad y errores; W01 para sesión; D01 para edición/tipos y D03 para cambios de Edicion. M02 consume confirmación/estado real. El detalle de edición lo mantiene 3: incorporar asistencia/archivo en PR coordinados. P08 antes de ejecutar cualquier cambio de esquema; no se autoriza por asignar esta tarea.

## 8. Orden recomendado

R01 conserva recorrido existente; preparar JUnit de R02/R03 en paralelo tras acordar modelo; publicar/consumir contratos aprobados; añadir estado en consultas; R04 después de asistencia y datos remotos reales. R05 con cada caso. No esperar toda la migración Web para escribir lógica y pruebas.

## 9. Pruebas obligatorias

- Registro general/código, cupo, duplicado, institución/tipo/edición y costo; regresión `RegistroTest`, `PatrocinioTest`, `UnicidadConcurrenciaTest` según alcance.
- Asistencia propia, ajena, registro inexistente y confirmación repetida; persistencia y consulta después de confirmar.
- Archivo: aceptada propia terminada; rechazar ajena, Ingresada/Rechazada/Archivada, fin hoy/futuro; fecha y conservación de registros. Mantener `EstadoEdicionTest/EstadoEdicionConcurrenciaTest` y controles existentes.
- PDF con todos los campos, contenido/tipo correcto, sin asistencia/ajeno rechazado; no basta ocultar el botón.
- Consulta web y cliente generado con permisos; coordinación de interfaz móvil con 5.
- JPA solo eventosuy_test con barrera JDBC; nunca cambiar datos habituales. Concurrencia cuando se comprueben invariantes afectadas.

## 10. Criterio de terminado

Casos §§6.2/6.3 y PDF §7.5 completos; operación autorizada desde Central y consumidor; registro no se pierde al archivar. Archivo solo visible según excepciones de perfil/registro, también por URL directa. JUnit y contribución al ≥80 % de líneas de lógica; pruebas automáticas diferenciadas de inspección visual. Biblioteca aprobada y empaquetada, dominio/DSS y demo entregados.

## 11. No modificar sin coordinación

`Edicion`, `DtEdicion`, `EstadoEdicion` y consultas compartidas con 3; `Evento`/búsqueda con 1/5; sesión y perfil con 2; POM/faults/ISistema y seed con Joaquín. No editar fuentes generadas, relajar aceptar/rechazar, duplicar reglas de registro o reescribir DTO por comodidad. No tocar dev, referencias ni ejecutar migraciones sin autorización.

## 12. Entrega por PR

Separar R01, R02, R03 y R04 hacia lab3-desarrollo. Especificar efecto persistido, permisos, fechas/estados y rollback cuando aplique; pruebas y cobertura del bloque; contrato y consumidor regenerable. Revisión por 3 para archivo/detalle, 5 para consumo móvil y Joaquín para infraestructura/PDF. Aportar modelo, DSS y seed con los escenarios exigidos (§8).
