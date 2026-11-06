# Laboratorio 3 — Requisitos y trazabilidad

## Fuentes, alcance y estado

Fuente normativa: `Laboratorio 3.pdf`, leído completo (14 páginas). Las páginas indicadas son las del PDF. `ProyectoWebServices/` es una referencia técnica local, no una dependencia ni parte de la entrega. Si difieren, prevalece la letra.

Preparación realizada el 5 de noviembre de 2026, desde `dev` en `5fb46db2743c10609c470c78b71ed8c210e8ba14`. Se revisaron los cuatro POM existentes de eventos.uy únicamente para planificar su evolución, sin volver a auditar el Laboratorio 2.

**Estado de esta etapa:** letra ANALIZADA y requisitos DOCUMENTADOS. Las filas siguientes son criterios de aceptación del Lab 3, no afirmaciones de funcionalidades implementadas. Su implementación y verificación quedan PENDIENTES. Los casos anteriores deben conservarse, pero no fueron reevaluados aquí.

## 1. Arquitectura y restricciones obligatorias

| ID | Fuente | Requisito | Criterio verificable para el cierre |
| --- | --- | --- | --- |
| A01 | §3, pp. 1–2; §8.3, p. 13 | Separar Servidor Central y Servidor Web; exclusivamente el Central mantiene lógica y persistencia. | Dos procesos en dos computadoras; el WAR no contiene el JAR de lógica, entidades, Hibernate ni acceso JDBC/JPA a PostgreSQL. |
| A02 | §3, pp. 1–2; §7.9, p. 12 | Swing usa llamadas directas a la lógica y comparte proceso con el Central. | Un `java -jar servidor.jar` publica SOAP y abre Swing; Swing no usa SOAP para sus casos de uso. |
| A03 | §3, p. 2; §7.9, p. 12; §8, p. 13 | Defensa en cuatro dispositivos de la misma red: Central/Swing/PostgreSQL, Web/Tomcat, navegador de escritorio y celular. | Recorrer casos reales entre esos cuatro nodos, sin IDE y sin sustituirlos por cuatro ventanas locales. |
| A04 | §7.1, pp. 6–7; §7.11, p. 13 | SOAP mediante Jakarta XML Web Services 4, Eclipse Metro, dependencia `com.sun.xml.ws:jaxws-rt`. | Operaciones invocables mediante WSDL y cliente Metro; no sustituir la comunicación Web–Central por REST, RMI o Spring Boot. |
| A05 | §7.1, p. 6 | Publicar mediante `Endpoint.publish`, al iniciar el Central antes de mostrar la ventana principal; Central sin Tomcat. | Arranque registra endpoint accesible antes de abrir Swing; Tomcat solo aloja la web. |
| A06 | §7.1, p. 6 | Publicadores delgados: obtener controladores por la Fábrica y delegar; no implementar negocio ni acceder a JPA en ellos. | Revisar recorridos SOAP → fachada → controladores existentes → persistencia. |
| A07 | §7.1, pp. 6–7 | Transferir tipos simples, enumerados, datatypes, listas/arreglos y `byte[]`; no objetos del dominio. | Contrato WSDL/XSD sin entidades ni infraestructura interna; ida y vuelta de todos los tipos usados. |
| A08 | §7.1, p. 6 | Datatypes aptos para JAXB: constructor público vacío y getters/setters públicos por atributo. | Pruebas de serialización y revisión del contrato; fechas representadas mediante una decisión explícita y compatible. |
| A09 | §7.1, p. 6 | Excepciones de negocio declaradas, convertidas en SOAP Fault, con mensajes comprensibles en web. | Probar un error de negocio desde un cliente generado y su presentación, sin filtrar errores internos. |
| A10 | §7.1, p. 7; §7.11, p. 13 | Cliente generado por `wsimport` usando `jaxws-maven-plugin`; no editar clases generadas. | Compilación limpia genera cliente desde WSDL; fuentes manuales separadas de `target/generated-sources`. |
| A11 | §7.1, p. 7 | Imágenes por SOAP como `byte[]`; la web no accede a su almacenamiento. | Carga y recuperación atraviesan SOAP; no rutas de archivos ni conexión PostgreSQL desde el WAR. |
| A12 | §7.1, p. 7 | Si el Central no responde, página de error propia sin trazas. | Apagar Central o configurar endpoint inaccesible y comprobar respuesta web controlada. |
| A13 | §7.2, p. 7 | Configuración externa en `~/.eventosUy`, sin direcciones, puertos ni credenciales incrustados en código. | Cambiar archivos y reiniciar sin reconstruir artefactos; no secretos reales versionados. |
| A14 | §7.2, p. 7 | Central configura host/puerto/ruta SOAP y URL/usuario/contraseña PostgreSQL; valores de conexión se pasan al crear EMF. | Arranque con configuración externa; `persistence.xml` no fija credenciales de conexión. |
| A15 | §7.2, p. 7 | Web configura host/puerto/ruta Central y reemplaza la dirección del WSDL. | Mismo WAR funciona con otra IP/puerto/ruta tras editar propiedades, sin regenerar cliente. |
| A16 | §7.2, p. 7 | Directorio externo solo para configuración; recursos y bibliotecas dentro de JAR/WAR. | Copiar únicamente artefacto y configuración a una máquina sin árbol fuente; arranque correcto. |
| A17 | §7.2, p. 7 | Publicación con IP LAN del Central y acceso al sitio con IP del Web. | WSDL y sitio accesibles desde los otros equipos; no depender de `localhost` ni rutas del desarrollador. |
| A18 | §7.11, p. 13 | Java SE 25, IntelliJ para desarrollo, Maven, Tomcat 11, PostgreSQL mediante JPA solo en Central y Git. | Compilar con Java 25 dentro y fuera del IDE; comprobar dependencias, despliegue e historial. |

El esquema pedido es distribución por procesos y red. No exige que Swing sea cliente remoto: hacerlo así contradiría A02. Un endpoint HTTP del navegador que devuelve disponibilidad no reemplaza SOAP: es el servlet quien debe consultar al Central por SOAP (§7.6).

## 2. Modelo y casos de uso

| ID | Fuente | Requisito | Criterio verificable para el cierre |
| --- | --- | --- | --- |
| F01 | §4, pp. 2–3 | Persistir todos los nuevos datos: relaciones de seguimiento, video, asistencia, finalización, archivo y visitas. | Reiniciar procesos y comprobar conservación en PostgreSQL por JPA. |
| F02 | §5, p. 3; §6, p. 3 | Conservar actores y casos anteriores no modificados, ahora con web consumiendo SOAP. Fechas nuevas por defecto: día actual, salvo indicación distinta. | Matriz de regresión por visitante/asistente/organizador y administrador Swing; verificar fechas desde lógica. |
| F03 | §6.1, p. 3 | Consultas Swing incluyen eventos finalizados y ediciones archivadas, indicando su estado. | Administrador puede consultarlos aunque desaparezcan de listados públicos. |
| F04 | §6.1, p. 3; §7.7, p. 10 | Swing: cinco eventos más visitados, orden descendente, posición, nombre y cantidad de visitas. | Tabla con esos datos y máximo cinco resultados; conteos persistidos. |
| F05 | §6.2, p. 4 | Alta de edición: solo eventos no finalizados; video YouTube opcional, además de datos anteriores; nombre único, organizador autenticado y estado inicial Ingresada. | Rechazo de evento finalizado/duplicado; alta con/sin video e imagen; dueño correcto y fecha actual. |
| F06 | §6.2, p. 4 | Consulta de edición: video en página si existe; conservar tipos, patrocinios y registro propio; dueño ve registros con indicador de asistencia. | Recorridos por rol con/sin video y asistencia; sin exposición de registros ajenos. |
| F07 | §6.2, p. 4 | Asistentes y organizadores pueden seguir y dejar de seguir desde listado/perfil; no a sí mismos ni duplicar relación. | Pruebas positivas y negativas; quitar relación existente; perfil muestra seguidores y seguidos actualizados. |
| F08 | §6.2, p. 4 | Organizador finaliza evento no finalizado; se oculta de listas/búsquedas y no admite nuevas ediciones. | Persistencia del estado y rechazo de alta posterior, también invocando lógica directamente. |
| F09 | §4, p. 3; §6.2, pp. 4–5 | Organizador archiva una edición propia Aceptada cuyo fin es anterior al día actual; registrar fecha de archivo. | Rechazar ajena, Ingresada, Rechazada, ya Archivada, fin hoy o futuro; aceptar propia con fin ayer. |
| F10 | §6.2, p. 5 | Archivadas no aparecen en listas/búsquedas generales; se consultan en perfil de su organizador; registros se conservan y asistente los ve en su perfil. | Matriz de visibilidad, enlaces directos y registros anteriores; archivar no elimina registros. |
| F11 | §6.2, p. 5 | Asistente descarga constancia PDF desde su registro solo si confirmó asistencia. | Propio con asistencia permite PDF; sin asistencia o registro ajeno no. |
| F12 | §6.2, p. 5 | Perfil consultado muestra seguidores/seguidos y acciones de seguir/dejar de seguir cuando correspondan. | Visitante consulta; usuarios habilitados operan sobre su identidad autenticada. |
| F13 | §6.2, p. 5; §7.6, pp. 9–10 | Alta de usuario informa disponibilidad de nickname y correo mientras se escribe. | Mensajes junto a campos sin recarga, con resultados reales del Central. |
| F14 | §5, p. 3; §6.3, p. 5 | Móvil: inicio por nickname/correo y contraseña; solo asistentes; rechazar organizadores y credenciales inválidas. | Login inicial y rechazo de ambos casos, conservando seguridad de sesiones. |
| F15 | §6.3, p. 5 | Móvil: cierre de sesión vuelve al inicio de sesión. | Sesión invalidada; acceso posterior a funciones protegidas vuelve a exigir autenticación. |
| F16 | §6.3, pp. 5–6 | Móvil: evento → ediciones Aceptadas → detalle completo con organizador, tipos, patrocinios, imagen/video y registro propio si existe. | Navegación real en celular y restricciones de estados/identidad. |
| F17 | §6.3, p. 6 | Móvil: consultar registros propios y detalle de tipo, fecha, costo, código usado y asistencia. | No aceptar cambiar identificador para consultar un registro ajeno. |
| F18 | §6.3, p. 6 | Móvil: registrar asistencia a edición en que está registrado; persistir confirmación; si ya estaba confirmada, informarlo y no volver a ofrecerla. | Un asistente no confirma registros ajenos ni inexistentes; segunda visita refleja confirmación anterior. |

Transiciones de edición que deben coexistir (§4, p. 3): alta → **Ingresada**; Ingresada → **Aceptada o Rechazada**; Aceptada → **Archivada** bajo las condiciones de F09. No se deduce una transición inversa ni una transición Rechazada → Archivada.

## 3. Interfaz, búsqueda y comprobación

| ID | Fuente | Requisito | Criterio verificable para el cierre |
| --- | --- | --- | --- |
| W01 | §7.3, pp. 7–8 | Móvil dentro del mismo `web.war`, ruta propia y diseño para pantallas pequeñas, reutilizando servlets y acceso SOAP. | Un único WAR atiende escritorio y móvil; verificar teléfono físico. |
| W02 | §7.3.1–3, pp. 7–8 | Pantalla inicial de login; navbar con Consulta Edición, Consulta Registro, Registrar Asistencia y Logout; hamburguesa en ancho reducido. | Acciones accesibles al tocar; tarjetas/grilla se adaptan; detalle del registro permite confirmar asistencia. |
| W03 | §7.3, p. 7; §7.11, p. 13 | Bootstrap 5 o equivalente incluido localmente; alcance responsive obligatorio: casos de §6.3. | Sin necesidad de CDN para CSS/JS del framework; comprobar esos casos en móvil. |
| W04 | §7.4, p. 9 | Video YouTube mediante iframe en escritorio y móvil; convertir URL normal a embed, móvil 100 % ancho y relación 16:9. | URL de ejemplo válida se reproduce integrada; ausencia de video no rompe vista. |
| W05 | §7.5, p. 9 | Constancia PDF generada a demanda, sin almacenarla previamente; nombre/apellido asistente, evento, edición, ciudad, fechas edición y fecha emisión. | Inspección del PDF y tipo de respuesta; sin PDFs persistidos previamente. Puede generarse en web o Central. |
| W06 | §7.6, pp. 9–10 | Disponibilidad con `fetch` y `async/await`, sin XMLHttpRequest ni recarga; servlet consulta al Central por SOAP. | Inspeccionar JS y peticiones; duplicados siguen rechazándose al enviar el alta. |
| W07 | §7.7, p. 10 | Filtro `jakarta.servlet.Filter` web intercepta consultas de evento escritorio/móvil, sin modificar servlets existentes; informa visita por SOAP. | Trazar filtro → SOAP → incremento persistente; reinicio mantiene conteo y Swing lo consulta. |
| W08 | §7.8, p. 10 | Cabezal del sitio ofrece búsqueda; nombre o descripción contienen texto sin distinguir mayúsculas; vacío lista todos los elegibles. | Casos de mayúsculas, coincidencia parcial, descripción, vacío y ausencia de resultados. |
| W09 | §7.8, p. 10 | Excluir eventos finalizados y ediciones no Aceptadas de búsqueda; ordenar fecha alta descendente por defecto, alfabético asc/desc y volver al defecto; enlaces a consultas. | Tests de filtros y tres órdenes, más navegación desde resultados. |
| T01 | §7.10, p. 12 | Mantener pruebas previas y agregar JUnit de lógica: seguimiento válido/inválido, finalización, archivo aceptado/terminado, asistencia, búsqueda/órdenes y visitas. | Suite pasa con casos positivos/negativos y efectos persistidos cuando corresponda. |
| T02 | §7.10, p. 12 | Al menos 80 % de líneas de lógica del Central, medido con IntelliJ Run with Coverage. | Informe de medición actualizado al código Lab 3. JaCoCo existente sirve como comprobación adicional, no sustituir silenciosamente lo indicado. |

No se exigen pruebas automáticas SOAP, Swing ni web (§7.10), pero sí que esas funciones operen en la defensa. Las pruebas de integración propuestas en ROADMAP son una decisión de calidad del equipo, no otro requisito de cobertura. No reutilizar porcentajes del Lab 2 como evidencia de cobertura del Lab 3.

## 4. Empaquetado, documentación y defensa

| ID | Fuente | Requisito | Criterio verificable para el cierre |
| --- | --- | --- | --- |
| E01 | §7.9, pp. 10–12 | Un comando Maven documentado genera `servidor.jar` y `web.war`, sin depender del IDE. | Compilación desde copia limpia con Java 25; sin exigir Central encendido para generar el cliente de un contrato ya versionado. Esta última condición es la estrategia reproducible propuesta. |
| E02 | §7.9, p. 12 | `servidor.jar` ejecutable con `java -jar`, con dependencias, Central y Swing; publica SOAP al iniciar. | Copiar JAR y configuración a equipo Central sin fuentes/Maven/IDE; abrir Swing y WSDL. |
| E03 | §7.9, p. 12 | `web.war` listo para Tomcat; despliegue mediante Manager o copia a `webapps`. | Copiar WAR y configuración al equipo Web sin fuentes ni lógica local; consultas/modificaciones remotas correctas. |
| E04 | §8.1, p. 13 | Análisis actualizado: modelo de dominio y DSS de casos nuevos/modificados fundamentales. | Documentar seguidores, video, asistencia, estados de evento/edición y visitas; concordancia con implementación final. |
| E05 | §8.2, p. 13 | Diseño distribuido: operaciones SOAP, datatypes, excepciones remotas, configuración y móvil. | Diagramas/contratos y explicación de decisiones concretas, no solo estas previsiones. |
| E06 | §8, p. 13 | Entregar fuente completo abrible en IntelliJ y construible desde terminal. | Clon/copia limpia, apertura y build; excluir demo, secretos, logs, dumps y artefactos temporales. |
| E07 | §8, p. 13 | README con requisitos, comando de ambos paquetes, despliegue/ejecución por nodo y ejemplos/claves de `~/.eventosUy`. | Otra persona puede instalar siguiendo exclusivamente esas instrucciones, sin contraseñas reales. |
| E08 | §8, p. 13 | Instrucciones de creación de base y datos: tres roles, usuarios siguiéndose, video, evento finalizado, edición archivada y otra archivable, registros con/sin asistencia. | Recorrido reproducible con datos de demostración aislados; sin depender de la base habitual del equipo. |
| E09 | §1, p. 1; §8, p. 13 | Entrega única Moodle: lunes **23/11/2026 a las 09:00**. Defensa mismo día, franjas matutina 11–15 y vespertina 15–19; turno específico por Moodle. | Confirmar horario del grupo y tarea Moodle; ensayo de cuatro dispositivos sin IDE. |

La letra no especifica nombre, tamaño ni formato ZIP de la subida. Debe verificarse en Moodle; no inventar ese requisito ni producir un paquete final en esta preparación.

## 5. Recomendaciones y elecciones, separadas de obligaciones

- La letra recomienda commits frecuentes (§1), ensayar temprano en dos equipos (§1), document/literal (§7.1), crear configuración desde plantillas (§7.2), Maven Wrapper y scripts (§7.9). Son recomendaciones; la distribución, configuración externa, Maven y artefactos sí son obligatorios.
- `maven-shade-plugin` es un ejemplo de empaquetador, no el único permitido. Se propone usarlo por correspondencia con la demo.
- Biblioteca PDF a elección (§7.5); PDFBox/OpenPDF/iText son ejemplos con implicancias de licencia, no dependencias ya aprobadas.
- `/web/movil` es un ejemplo de ruta (§7.3), no un mapping literal obligatorio. Se propone una ruta `/movil` relativa al contexto, por confirmar al implementar.
- JSON es un ejemplo de respuesta breve (§7.6); fetch con async/await y consulta SOAP del servlet sí son obligatorios.
- No se exige implementar todos los casos del organizador en móvil ni una aplicación nativa.

## 6. Decisiones pendientes antes de implementar reglas

1. **Finalizar evento:** la letra habilita al organizador, pero no define expresamente propiedad del evento como hace con archivar su edición. Acordar interpretación y documentarla antes de agregar una restricción de dueño.
2. **Descripción de edición en búsqueda:** revisar el modelo vigente al diseñar el contrato para identificar cómo representar y consultar la descripción exigida por §7.8; no inventar un atributo existente ni omitir esa parte.
3. **Visitas:** definir empates del top cinco y tratamiento de consultas inválidas/refrescos/fallas; no imponer visitantes únicos, cookies de conteo ni límites diarios que no pide la letra.
4. **Asistencia:** no se expresa una restricción por fecha de celebración en §6.3. No agregarla sin aclaración. Sí se exige registro propio y evitar ofrecer una segunda confirmación.
5. **Transporte:** seleccionar conversiones de fechas, herencia DTO y contrato de errores, sin entidades remotas ni suponer que todo DTO actual ya cumple JAXB.
6. **Sesión e identidad:** diseñar cómo las operaciones remotas mantienen los controles por actor y propiedad de los casos de uso existentes. El cliente generado no autentica ni autoriza por sí solo.

Documentos relacionados: [ROADMAP](ROADMAP.md) y [análisis de ProyectoWebServices](REFERENCIA_WEBSERVICES.md).
