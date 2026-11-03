# Análisis y diseño - Laboratorio 2

Documentación de §8.1 y §8.2 basada en la implementación actual. Incluye modelo conceptual actualizado, contraste JPA, DSS de casos fundamentales, realizaciones internas y distribución MVC/JAR/Swing. No modifica contratos, código productivo ni esquema.

Leer [DECISIONES.md](DECISIONES.md) para multiplicidades lógicas frente a nulabilidad física, límites de transacción y decisiones de diseño.

## Índice y trazabilidad

| Documento | Fuente reproducible | Exportación | Código contrastado |
|---|---|---|---|
| Modelo de dominio - multiplicidades de los casos de uso | [01-modelo-dominio.puml](fuentes/01-modelo-dominio.puml) | [PNG](imagenes/01-modelo-dominio.png) | clases/*.java; Sistema: validaciones de altas/registro |
| Vista de implementación JPA - atributos técnicos y opcionalidad física | [02-mapeo-jpa.puml](fuentes/02-mapeo-jpa.puml) | [PNG](imagenes/02-mapeo-jpa.png) | clases/*.java: anotaciones, atributos, herencia y asociaciones |
| DSS - alta, autenticación, perfil y cierre | [03-dss-usuarios.puml](fuentes/03-dss-usuarios.puml) | [PNG](imagenes/03-dss-usuarios.png) | AltaUsuarioServlet, LoginServlet, ModificarUsuarioServlet, LogoutServlet |
| DSS - evento, edición y decisión administrativa | [04-dss-eventos-ediciones.puml](fuentes/04-dss-eventos-ediciones.puml) | [PNG](imagenes/04-dss-eventos-ediciones.png) | AltaEventoServlet, AltaEdicionServlet, AceptarRechazarEdicionPanel, consultas |
| DSS - patrocinio y registro general o con código | [05-dss-registros-patrocinios.puml](fuentes/05-dss-registros-patrocinios.puml) | [PNG](imagenes/05-dss-registros-patrocinios.png) | RegistroEdicionServlet, AltaPatrocinioServlet, ConsultaPatrocinioServlet |
| Realización - alta de usuario y autenticación | [06-secuencia-usuarios.puml](fuentes/06-secuencia-usuarios.puml) | [PNG](imagenes/06-secuencia-usuarios.png) | Sistema.altaAsistente/altaOrganizador/autenticar; ManejadorUsuarios.addUsuario/buscarPorNicknameOCorreo |
| Realización - perfil atómico y asociación de imagen posterior | [07-secuencia-perfil-imagen.puml](fuentes/07-secuencia-perfil-imagen.puml) | [PNG](imagenes/07-secuencia-perfil-imagen.png) | ModificarUsuarioServlet.doPost; ManejadorUsuarios.modificarAsistente/modificarOrganizador; ManejadorImagenes.asociar |
| Realización - alta de evento y alta de edición web | [08-secuencia-evento-edicion.puml](fuentes/08-secuencia-evento-edicion.puml) | [PNG](imagenes/08-secuencia-evento-edicion.png) | Sistema.altaEvento/altaEdicion; ManejadorEventos.addEvento/addEdicion |
| Realización - aceptar o rechazar con bloqueo pesimista | [09-secuencia-estados-swing.puml](fuentes/09-secuencia-estados-swing.puml) | [PNG](imagenes/09-secuencia-estados-swing.png) | AceptarRechazarEdicionPanel.cambiarEstado; Sistema.aceptarRechazarEdicion; ManejadorEventos.cambiarEstadoEdicion |
| Realización - registro general o patrocinado y concurrencia | [10-secuencia-registro.puml](fuentes/10-secuencia-registro.puml) | [PNG](imagenes/10-secuencia-registro.png) | Sistema.registroAEdicion (4/5 parámetros); ManejadorRegistros.registrar |
| Realización - alta y consulta de patrocinio con privacidad | [11-secuencia-patrocinio.puml](fuentes/11-secuencia-patrocinio.puml) | [PNG](imagenes/11-secuencia-patrocinio.png) | AltaPatrocinioServlet/ConsultaPatrocinioServlet; Sistema.altaPatrocinio/consultarPatrocinio; ManejadorPatrocinios |
| Diseño y distribución - iteración 2 sin RPC | [12-arquitectura.puml](fuentes/12-arquitectura.puml) | [PNG](imagenes/12-arquitectura.png) | POM raíz/tres módulos; ContextoListener, ISistema, Sistema, BaseDeDatos y principal |

Las clases citadas pertenecen a logica/src/main/java/clases, implementacion, manejadores y persistencia; los controladores a webapp/src/main/java/web y los paneles a estacion-trabajo/src/main/java/swing. Los contratos están en logica/src/main/java/interfaces/ISistema.java.

## Reproducción

Se utilizó Java 25 y PlantUML 1.2026.9 con Smetana, sin Graphviz externo. La versión, SHA-256 del JAR y origen Maven Central están en herramientas.json. La herramienta NO se añade como dependencia ni JAR al proyecto. Obtener esa versión del origen indicado y verificar el hash antes de ejecutar:

```powershell
./docs/diseno/renderizar.ps1 -PlantUmlJar 'RUTA/plantuml-1.2026.9.jar' -Java 'RUTA-JDK-25/bin/java.exe'
```

El script falla ante error de sintaxis/render y exporta cada fuente a imagenes/. No consulta ni modifica bases. Las fuentes son UTF-8/CRLF; los PNG se pueden abrir sin herramienta UML. Para leer las realizaciones grandes, abrir el PNG a resolución original. No incluir el JAR de PlantUML, cachés ni logs en Git o Moodle.

## Verificación normativa y técnica

- §8.1: modelo conceptual 01 y mapeo 02; DSS 03–05 cubren usuarios, edición/decisión administrativa, registro general/patrocinado y patrocinio. La selección prioriza nuevos/modificados y sus reglas fundamentales.
- §8.2: arquitectura 12 y DECISIONES.md explican MVC, JAR embebido, sesión, imágenes y estados.
- Realizaciones 06–11 identifican controladores/paneles, fachada/implementación, manejadores, límites JPA y PostgreSQL sin inventar un servicio ISistema separado.
- Todas las fuentes se renderizan; sus PNG se abrieron para control visual. Se comprueba que clases/atributos provienen del código y se documentan las abstracciones. Una imagen de diagrama revisada no es una prueba visual de la aplicación.
- La instalación limpia, los datos de demostración y las pruebas se documentan en README y docs de estabilización; esta documentación no reemplaza las pruebas ni demuestra ausencia de errores.
