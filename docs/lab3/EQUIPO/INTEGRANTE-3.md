# Integrante 3 — Eventos, ediciones y seguimiento

## 1. Objetivo

Migrar eventos/ediciones/tipos de registro y desarrollar seguimiento de usuarios, video y finalización de eventos. Entregar recorridos completos de su dominio, sin asumir asistencia/archivo del 4 ni búsqueda/móvil del 5.

Base compartida: corte `586eab4`. Rama propuesta `lab3-social-eventos`, todavía no creada. Leer [contratos](../CONTRATOS-EQUIPO.md) y [guía Git](GUIA-GIT.md).

## 2. Tareas asignadas

| ID | Trabajo | Prioridad / dificultad | Aceptación |
| --- | --- | --- | --- |
| D01 | Migración SOAP de eventos, ediciones, tipos y helpers. | P0 / 5 | Recorridos anteriores conservados, incluidos permisos, enlaces, fechas y selección por nombre. |
| D02 | Seguir/dejar de seguir y consultas de relaciones. | P1 / 5 | Persistente, sin seguimiento propio ni duplicado; perfil/listado permiten operar según sesión. |
| D03 | Video opcional en edición. | P1 / 3 | Alta, persistencia y contrato con video opcional; embed en consulta; ausencia no rompe vista. |
| D04 | Finalizar evento y visibilidad diferenciada. | P1 / 5 | No listado/búsqueda pública ni nuevas ediciones; administrador sigue consultando finalizados. |
| D05 | Modelo/DSS y datos demostrables del dominio. | P2 / 2 | Seguimientos, video y finalización reproducibles; documentación fiel a implementación. |

## 3. Archivos y módulos

- Lógica: `logica/src/main/java/implementacion/Sistema.java`, `interfaces/ISistema.java`, `manejadores/ManejadorUsuarios.java`, `ManejadorEventos.java`, `clases/Usuario.java`, `Evento.java`, `Edicion.java`, `datatypes/DtUsuario.java`, `DtEvento.java`, `DtEdicion.java` según acuerdo.
- Web: `AltaEventoServlet`, `EventoServlet`, `AltaEdicionServlet`, `ConsultaEdicionServlet`, `AltaTipoRegistroServlet`, `ConsultaTipoRegistroServlet`, `AccesoEdiciones`, sus JSP en WEB-INF/views y scripts actuales necesarios.
- Seguimiento: `UsuariosServlet`, `UsuarioServlet`, `usuarios.jsp`, `usuario.jsp` después de migración W02, más controlador nuevo por crear para la acción si así se acuerda.
- Contratos/publicadores del dominio en servidor-central y consumidores Web. DTO de seguimiento/video/finalización todavía no implementados.
- Swing: `ConsultaEventoPanel`, `ConsultaEdicionEventoPanel`, `ConsultaUsuarioPanel` en lo que afecte visibilidad/estados; coordinar con 4 y Joaquín. `AltaEdicionEventoPanel` solo si el diseño aprobado requiere adaptación, no ampliar casos Swing por suposición.

## 4. Métodos reales reutilizables

`altaEvento`, `listarEventos`, `listarCategorias`, `altaEdicion`, `obtenerEdicionesEvento` (dos sobrecargas), `obtenerEdicionesOrganizador`, `altaTipoRegistro`, `obtenerTiposRegistroEdicion`, `consultarTipoRegistro`, `listarUsuarios`, `consultarUsuario`.

`Sistema.aDt` convierte ediciones; los manejadores ya consultan eventos/ediciones. `AccesoEdiciones.resolver/aceptadas/esVisible/esPropietario` preparan selección/visibilidad actual y dependen de tipos locales: migrarlos. `EventosPublicador.listarEventos` es el único servicio existente; no inventar un `consultarEvento` ya disponible, porque ISistema no lo declara.

## 5. Funcionalidades nuevas

D02 necesita relación persistente de usuarios y operaciones nuevas; D03 dato opcional de video; D04 estado finalizado. No existen todavía esos métodos/campos. Resolver autorización de finalización antes de imponer que un evento sea "propio": la letra define propiedad para archivar edición, no la misma regla para evento.

Un evento finalizado no admite nuevas ediciones, pero no se exige que el acto de finalizar borre datos ni se condiciona explícitamente a que todas sus ediciones hayan terminado. No agregar esa regla unilateralmente. Seguimiento exige impedir sí mismo y duplicado; acordar tratamiento de dejar de seguir una relación inexistente.

## 6. Operaciones SOAP

Dueño del grupo Eventos/ediciones/categorías/tipos. Coordina con 4 la operación de archivo y con 2 los consumidores de selectores. Dueño funcional de nuevas operaciones de seguimiento y finalizar; responsable 2 mantiene el contrato general de usuario. Publicadores delgados; faults de negocio declarados; estado/video/fechas consistentes en DTO.

## 7. Dependencias

J01 para contrato/identidad, J03 para imágenes y W01 para sesión. W02 debe integrar migración de perfil antes de cambios de seguimiento. D03 comparte Edicion con archivo R03; D04 comparte Evento con visitas J04 y búsqueda M03. Descripción de edición P05 requiere acuerdo con 5/4 antes de modificar modelo para búsqueda.

## 8. Orden recomendado

Preparar pruebas locales/modelo con firmas acordadas sin esperar toda la web. D01 lecturas/selectores y luego escrituras; D02 lógica/contrato y perfil después de W02; D03 con modelo Edicion coordinado; D04 con consultas públicas/admin diferenciadas. D05 acompaña cada corte, no solo el final.

## 9. Pruebas obligatorias

- Seguimiento válido, dejar de seguir, usuario inexistente, sí mismo, duplicado y relaciones consultadas tras persistir.
- Alta/consulta de edición con/sin video; URL habitual a embed y contenido 16:9 para reutilización móvil, sin permitir iframe arbitrario por confiar en texto del usuario.
- Finalización: persiste, excluye listados/búsqueda y bloquea alta de edición; no oculta al administrador.
- Regresión de estados, consultas y tipos; ayudas existentes `ConsultasSistemaTest`, `ReglasAltaTest`, `EstadoEdicionTest`, `ConsultaTipoRegistroTest`, `ConsultasEdicionTest`, `AltasPrgTest` según alcance.
- SOAP con fechas, estados, categorías, lista vacía y faults; permisos y URLs directas.
- Persistencia exclusivamente eventosuy_test, barrera JDBC antes de JPA/datos. Mantener pruebas anteriores.

## 10. Bloque terminado

Contrato real consumido por web, nuevos casos de §§6.2/7.4 verificables y pruebas de §7.10. No filtrar globalmente `Sistema.listarEventos` de forma que rompa Swing: separar el contexto de consulta. Seguimiento y estados se conservan al reiniciar. Aportes al 80 % conjunto y diseño/DSS/seed entregados; no declarar visual un test con dobles.

## 11. No modificar sin coordinación

`ISistema`, DTO de usuario/sesión y faults; `UsuarioServlet`/JSP de perfil/listado; `Edicion`, `EstadoEdicion`, `Evento` y DTO; `Sistema`/`ManejadorEventos` fuera de sus métodos; `edicion.jsp` compartida con 4; cabezal, filtros, POM, seed y configuración. No agregar campos ni reglas de búsqueda por suposición. No tocar dev ni referencias.

## 12. PR esperado

PR de D01 y PR funcionales separados D02/D03/D04 hacia lab3-desarrollo. Indicar contratos afectados y aprobaciones; DTO/WSDL/XSD y pruebas de cliente, sin generado versionado. Revisiones de 2 para perfil, 4 para Edicion/archivo y Joaquín para arquitectura. Adjuntar modelo/DSS, reglas de visibilidad y datos demo; no cambiar migraciones ni ejecutarlas sin autorización.
