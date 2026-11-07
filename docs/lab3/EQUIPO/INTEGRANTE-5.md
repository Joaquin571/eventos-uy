# Integrante 5 — Móvil, búsqueda y filtro de visitas

## 1. Objetivo

Versión móvil dentro del mismo WAR, búsqueda y filtro web de visitas. El backend persistente de visitas y ranking Swing corresponden a Joaquín; disponibilidad AJAX corresponde al 2. No duplicar negocio ni implementar una aplicación móvil nativa.

Base: corte `586eab4`. Rama propuesta `lab3-movil-busqueda`, todavía no creada. Leer [contratos](../CONTRATOS-EQUIPO.md) y [guía Git](GUIA-GIT.md).

## 2. Tareas asignadas

| ID | Trabajo | Prioridad / dificultad | Aceptación |
| --- | --- | --- | --- |
| M01 | Entrada móvil, login, logout y navegación. | P0–P1 / 5 | Solo asistentes; login inicial y barra/hamburguesa; logout vuelve al login. |
| M02 | Consulta edición/registro y confirmación desde móvil. | P1 / 5 | Datos propios, imagen/video y asistencia real; reutiliza controladores/acceso SOAP. |
| M03 | Búsqueda lógica/contrato/web. | P1 / 5 | Nombre/descripción, case-insensitive, vacío, estados, órdenes y enlaces. |
| M04 | Filtro web de visitas por SOAP. | P1 / 3 | Cubre escritorio y móvil sin poner conteo dentro de servlets existentes. |
| M05 | Responsive, pruebas físicas y documentación móvil. | P2 / 3 | Teléfono real, sin desbordes; video 16:9; navegación y evidencias documentadas. |

## 3. Archivos y módulos

- Web existente: `LoginServlet`, `LogoutServlet`, `ConsultaEdicionServlet`, `ConsultaRegistroServlet`, `EventoServlet`, `FiltroAcceso`, `fragmentos/cabezal.jsp`; modificar solo mediante coordinación/reutilización del responsable.
- Vistas/CSS móviles, controlador/vista de búsqueda y filtro de visitas nuevos por crear en webapp, con nombres/rutas acordados. Ruta sugerida /movil relativa al contexto, no obligación literal de la letra.
- Búsqueda: `logica/src/main/java/interfaces/ISistema.java`, `implementacion/Sistema.java`, `manejadores/ManejadorEventos.java`, DTO y, solo después de acuerdo, `clases/Edicion.java`/`DtEdicion.java` por descripción faltante.
- Publicador/cliente de búsqueda y consumidor de visita; Bootstrap local de `webapp/src/main/webapp/vendor/`. Pruebas JUnit/JS y documentación de interfaz/diseño.

## 4. Métodos reales reutilizables

`autenticar`, `listarEventos`, `obtenerEdicionesEvento`, `obtenerEdicionesOrganizador`, `consultarUsuario`, `obtenerTiposRegistroEdicion`, `obtenerPatrociniosEdicion`, `obtenerRegistrosAsistente`, `obtenerImagen`. Consumir sus equivalentes SOAP al integrarse, no llamar a manejadores desde Web.

`AccesoEdiciones`, `DetalleRegistroVista` y los servlets existentes ya preparan información que debe reutilizarse/adaptarse con sus dueños. Hoy no existen métodos de búsqueda, confirmación de asistencia ni visitas; no programar contra nombres supuestos.

## 5. Funcionalidades nuevas

Ruta/vistas móviles para los casos de §6.3; búsqueda de §7.8; filtro de §7.7. Móvil no exige todas las altas del organizador. Pantalla inicial login; menú de edición, registros, asistencia y logout; eventos/ediciones en grilla/tarjetas. Video a ancho disponible y 16:9.

Búsqueda: nombre o descripción contienen texto sin distinguir mayúsculas; vacío lista elegibles; excluye finalizados y ediciones no Aceptadas; fechaAlta descendente por defecto y opción nombre asc/desc y volver. Cada resultado enlaza a su consulta. Edicion hoy carece de descripción: cerrar P05 antes de inventar un atributo o usar descripción del evento como sustituto.

## 6. Operaciones SOAP

Responsable de la propuesta de búsqueda y su consumidor. Para visitas, acordar con J04 una operación de incremento persistente. El filtro solo comunica la visita, no conserva el contador en Web. Móvil reutiliza servicios de 2/3/4 y J03. No generar otro WAR ni meter lógica/JPA para "desbloquear" interfaz.

## 7. Dependencias

W01/P01 para sesión/login, D01/D03 para detalle/video, R01/R02 para registro/asistencia, J03 para imágenes, D04/R03 para filtros de estados, J04/P06 para visitas. Cabezal compartido con 2; el menú móvil se coordina sin redefinir roles. Contratos y P03 antes de consumo de escrituras protegidas.

## 8. Orden recomendado

Preparar estructura responsive y pruebas con datos simulados mientras se cierran contratos. Integrar M01 con W01; M02 conforme lleguen consultas/asistencia reales. Diseñar búsqueda temprano, implementarla al resolver descripción/estados. M04 cuando exista operación de visitas y estén acordados mappings; cerrar M05 con teléfono real. No dar por terminado un flujo que aún usa dobles.

## 9. Pruebas obligatorias

- Login asistente válido, credenciales inválidas y organizador rechazado, incluso con sesión de escritorio. Logout POST/CSRF y vuelta al login; URLs protegidas.
- Consultas propias/ajenas, Aceptadas y estados ocultos; sin registro, registrado y con asistencia; confirmación no se vuelve a ofrecer tras guardarse.
- Búsqueda por nombre/descripción, mayúsculas, parcial, vacío, sin resultados, estados excluidos y tres órdenes; JUnit de lógica obligatorio (§7.10).
- Filtro escritorio/móvil: incremento esperado sin duplicar por dispatch interno; caso inválido según P06; caída de Central controlada. Concurrencia/persistencia del contador se verifica con Joaquín.
- JS de interacción nueva y prueba física responsive: menú, imágenes, teclado, navegación, iframe y sin desbordes. Registrar tamaño/dispositivo y qué se ejecutó.
- Persistencia solo eventosuy_test con barrera JDBC y autorización. No ejecutar seed ni migraciones sobre base habitual.

## 10. Bloque terminado

Mismo web.war con ruta móvil propia y casos de §6.3; búsqueda completa y visitas informadas por filtro. Solo cliente SOAP para datos; contratos reales, no dobles en producto. Tests y contribución a ≥80 % lógica, documentación móvil/diseño/DSS y ensayo físico sin IDE con el equipo. No afirmar que el emulador reemplaza el teléfono de defensa.

## 11. No modificar sin coordinación

Sesión, login/logout y FiltroAcceso (2); controladores de edición/video (3) y registro/asistencia (4); Evento/visitas/ISistema y POM (1); Edicion por búsqueda (3/4); cabezal (2). No editar scripts/estilos ajenos globalmente, generado wsimport ni reglas del backend por una necesidad visual. No tocar dev ni referencia; no agregar dependencias o migraciones sin aprobación.

## 12. PR esperado

Separar M01/M02, M03 y M04; M05 aporta evidencias y correcciones acotadas. Base lab3-desarrollo; detallar rutas relativas al contexto, servicios consumidos, estados y pruebas, más capturas/checklist de teléfono sin datos sensibles. Revisión cruzada con dueños de los consumidores y Joaquín para contrato/filtro. Entregar documentación del diseño móvil y búsqueda para §8.
