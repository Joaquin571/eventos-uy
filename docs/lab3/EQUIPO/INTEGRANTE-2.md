# Integrante 2 — Usuarios, sesión, instituciones y patrocinios

## 1. Objetivo

Migrar el dominio web de usuarios, instituciones y patrocinios a SOAP; mantener sesión/roles e implementar disponibilidad AJAX. Coordinar el retiro final de lógica/JPA del WAR. No asumir la migración de eventos ni registros de los compañeros.

Base compartida: corte `586eab4`. Rama propuesta `lab3-web-usuarios-patrocinios`, todavía no creada. Leer [contratos](../CONTRATOS-EQUIPO.md) y [guía Git](GUIA-GIT.md).

## 2. Tareas asignadas

| ID | Trabajo | Prioridad / dificultad | Aceptación |
| --- | --- | --- | --- |
| W01 | Login, sesión, roles, filtro y listener. | P0 / 5 | Datos de sesión independientes de logica.jar; renovación, CSRF y restricciones conservadas. |
| W02 | Alta/modificación/listado/perfil de usuarios. | P0 / 5 | Operaciones por SOAP con privacidad, atomicidad de perfil y mensajes de imagen parcial actuales. |
| W03 | Instituciones y patrocinios. | P0 / 5 | Altas/consultas por SOAP; propiedad y código privado preservados. |
| W04 | Disponibilidad de nickname/correo mientras se escribe. | P1 / 3 | fetch + async/await, mensajes junto a campos, sin recarga; backend revalida duplicados. |
| W05 | Retiro definitivo de lógica local del Web. | P2 / 3 | Sin inicialización JPA/imports productivos de lógica; dependencias productivas y de tests revisadas. |

## 3. Archivos y módulos

En `webapp/src/main/java/web/`: `LoginServlet`, `LogoutServlet`, `FiltroAcceso`, `ContextoListener`, `AltaUsuarioServlet`, `ModificarUsuarioServlet`, `UsuariosServlet`, `UsuarioServlet`, `AltaInstitucionServlet`, `AltaPatrocinioServlet`, `ConsultaPatrocinioServlet`, `AccesoPatrocinios`.

JSP homónimas en `webapp/src/main/webapp/WEB-INF/views/`, `fragmentos/cabezal.jsp`; `js/validacion-formularios.js`, `validacion-altas.js`, `alta-patrocinio.js` solo en lo necesario. Nuevo servlet/JavaScript de disponibilidad por crear. Publicadores/DTO del dominio en servidor-central y consumidores en Web, con nombres acordados. `webapp/pom.xml` y `WEB-INF/web.xml` coordinados con Joaquín. W05 alcanza tests que hoy dependen de clases de lógica/Central; no basta quitar una dependencia productiva.

## 4. Métodos reales reutilizables

`ISistema.autenticar`, `existeUsuario`, `existeCorreoElectronico`, `altaAsistente`, `altaOrganizador`, `modificarAsistente`, `modificarOrganizador`, `listarUsuarios`, `consultarUsuario`, `existeInstitucion`, `altaInstitucion`, `listarInstituciones`, `altaPatrocinio`, `obtenerPatrociniosEdicion`, `consultarPatrocinio(edicion,institucion)`.

Consume también `obtenerEdicionesOrganizador`, `listarEventos`, `obtenerEdicionesEvento`, tipos de registro y operaciones de imagen de los otros dominios. `MensajesFlash` y `Csrf` siguen siendo utilidades HTTP, no necesitan un servicio propio.

## 5. Funcionalidades nuevas

W04 agrega consulta asincrónica en alta de usuario (§7.6); reutiliza comprobaciones existentes de unicidad. No inventar restricciones nuevas de nickname ni cambiar semántica de correo. DTO de sesión/rol y controlador de disponibilidad siguen pendientes de acuerdo, no existen por estar descritos aquí.

## 6. Contratos SOAP

Responsable de Usuarios y de Instituciones/patrocinios. Separar entrada con contraseña de salidas/perfil/sesión. Datos de código de patrocinio según autorización. Fallos de negocio declarados y errores técnicos seguros. No publicar una búsqueda por código como consulta pública sin consumidor/permiso.

## 7. Dependencias

P01/P03 y J01 antes de cerrar login/escrituras. J03 para imágenes. D01 para selectores de evento/edición/tipo; R01 para registros en perfil. Después de W02, el 3 agrega seguimiento a UsuarioServlet/usuarios.jsp/usuario.jsp en PR sucesivos. El 5 reutiliza sesión y acuerda rutas móviles, sin redefinir roles. W05 espera a todos los consumidores migrados.

## 8. Orden recomendado

Cerrar modelo de sesión/rol; migrar consultas y autenticación; W02; W03; W04 en paralelo cuando existan operaciones de disponibilidad. No retirar prematuramente JPA del listener: hacerlo en W05 con inventario completo. Migración de perfil antes del seguimiento del compañero 3.

## 9. Pruebas obligatorias

- Login válido/inválido y ambigüedad ya resuelta; renovación de sesión, logout POST y GET 405; roles y CSRF.
- Alta/modificación de ambos subtipos, duplicados y límites; conservación de mensajes/valores, PRG y comportamiento de imagen parcialmente fallida.
- Patrocinios propios/ajenos, código no expuesto a público/asistente, errores 20 %, cupo gratuito y duplicados.
- AJAX: disponible/no disponible, vacío/formato, respuestas obsoletas y fallo de Central; validación definitiva al enviar permanece.
- Cliente generado contra servicio real y regresión de `PatrociniosAutorizacionTest`, `PatrociniosFlujoTest`, `CsrfTest`, `AltasPrgTest`, `UsuarioFechaAltaTest`, según cambios; nuevas pruebas JS.
- Si se prueba persistencia, solo eventosuy_test con barrera JDBC previa. Ningún seeder sobre base habitual.

## 10. Bloque terminado

Casos de este dominio por SOAP, sin exposición de datos privados ni regresiones de perfil/sesión. Disponibilidad según §7.6 y sin XMLHttpRequest. W05 revisa imports, clases de sesión/helpers, dependencias y contenido WAR; indisponibilidad con página propia. Pruebas y documentación de operaciones/errores incluidas. JUnit lógico conserva mínimo conjunto del 80 % (§7.10).

## 11. Archivos que requieren coordinación

`ISistema`, DTO comunes, POM, ContextoListener/FiltroAcceso, `cabezal.jsp`, `UsuarioServlet` y JSP de perfil/listado, `AccesoEdiciones`, `Subidas/ImagenServlet`, seed. No modificar reglas ni manejadores de eventos/registros para resolver un consumidor. No tocar dev, referencias, credenciales ni esquema por iniciativa propia.

## 12. Entrega mediante PR

Separar W01, W02, W03, W04 y W05. Base lab3-desarrollo, contratos aprobados, fuentes WSDL/XSD cuando cambien y cliente regenerable, sin generado versionado. Incluir matriz de permisos, pruebas, instrucciones y pendientes. Revisión por Joaquín y por 3/4 si toca perfil o selectores compartidos. Adjuntar aportes de diseño/DSS y datos demo al coordinador (§8).
