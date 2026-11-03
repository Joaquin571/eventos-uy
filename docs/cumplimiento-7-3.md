# Validación de datos — apartado 7.3 — Laboratorio 2

Fuente: `Laboratorio 2 - 2026 (1).pdf`, §7.3, página 9. Revisado el 03/11/2026.

## Qué exige y qué no exige

El apartado tiene tres obligaciones distintas:

1. Validar en el navegador los datos ingresados.
2. Elegir los valores existentes mediante listas o combos: categoría, evento,
   edición, tipo, institución y nivel. No convertir esas claves en texto libre.
3. Controlar con JavaScript los demás datos: obligatorios, tipos, confirmación de
   contraseña y formatos, sin sustituir los rechazos de negocio de la lógica.

No exige que todos los formularios tengan las mismas reglas. No exige consultar
duplicados con AJAX, publicar códigos, reconstruir cupos en JavaScript, inventar
una edad mínima o imponer una política nueva de complejidad de contraseñas.
Las reglas específicas se obtienen de §6.2 y de los contratos ya implementados.

HTML5 permanece en las JSP como respaldo. Con JavaScript activo, los scripts
controlan el envío y consultan también `ValidityState`, sin reimplementar el
parser de fechas/URL del navegador. Usar las APIs HTML5 desde JavaScript sigue
siendo validación JavaScript; no significa confiar en ella como seguridad.

## Matriz: estado encontrado y corrección

Prefijos: vistas = `webapp/src/main/webapp/WEB-INF/views/`; scripts =
`webapp/src/main/webapp/js/`; controladores = `webapp/src/main/java/web/`;
lógica = `logica/src/main/java/implementacion/Sistema.java` y manejadores.

| Formulario / JSP | Validaciones exigidas para sus datos | HTML5 existente conservado | JavaScript encontrado antes de esta tanda | Servidor conservado | Corrección y estado de presentación |
|---|---|---|---|---|---|
| Login — login.jsp | Identificador y contraseña obligatorios; identificador admite nickname o correo, no necesariamente email | required; text/password | Sin validador propio | LoginServlet → Sistema.autenticar: identificador, contraseña y rechazo de credenciales | formLogin + validador general; sin imponer email al nickname ni recortar la contraseña. Completo |
| Alta usuario — alta-usuario.jsp | Nickname, nombre, correo válido, contraseña/confirmación; asistente: apellido y nacimiento no futuro; organizador: descripción y URL opcional; institución desde combo; imagen opcional | required, email, date, URL; required de subtipo asignado por JS | Alternaba campos, fecha max y confirmación; no validaba todos los obligatorios en JS; confirmación obsoleta al corregir contraseña principal | AltaUsuarioServlet; Sistema.altaAsistente/altaOrganizador y validarDatosUsuario/validarContrasenia/validarNacimiento | Validador general por campos activos, correo igual al formato de Sistema, mensajes por campo; campos de otro subtipo deshabilitados sin borrar valores; confirmación se limpia desde ambos inputs. Completo en navegador |
| Modificar usuario — modificar-usuario.jsp | Datos según subtipo; nickname/correo no editables; contraseña opcional, confirmación si cambia; fecha no futura; institución/URL/imagen opcionales | disabled en claves; required en datos básicos; date/URL | Fecha max y comparación de contraseñas; mismos problemas de error obsoleto y mensajes limitados | ModificarUsuarioServlet usa nickname de sesión; Sistema.modificar* y ManejadorUsuarios: validación, subtipo y transacción | Validador general ignora claves deshabilitadas, permite ambas contraseñas vacías, conserva valores y muestra errores por campo. Completo |
| Alta evento — alta-evento.jsp | Nombre/sigla/descripción; al menos una categoría existente; imagen opcional | required y checkboxes provenientes de lógica | Obligatorios blancos/categorías con aviso global | AltaEventoServlet; Sistema.altaEvento: obligatorios, categorías existentes y nombre único | Aviso global reemplazado por mensajes junto a campos y grupo; incluso ausencia total de categorías impide enviar. Completo |
| Alta edición — alta-edicion.jsp | Evento desde combo; nombre/sigla/ciudad/país, fechas válidas y fin ≥ inicio; imagen opcional | required, select y date | alta-edicion-fechas.js para orden; resto dependía de HTML5 | AltaEdicionServlet: organizador desde sesión; Sistema.altaEdicion: datos, fechas y unicidad | Se conserva validador de fechas y se añade general para obligatorios, tipos, imagen y mensajes. No impone que el inicio sea futuro. Completo |
| Alta institución — alta-institucion.jsp | Nombre/descripción/sitio obligatorio con URL HTTP/HTTPS conforme al flujo existente; imagen opcional | required, URL, file accept | validacion-altas.js ya validaba campos y límite de imagen con mensajes | AltaInstitucionServlet valida URL y datos; Sistema.altaInstitucion y manejador validan datos/duplicados | No se reemplaza el script existente. Completo; el formato de URL del Servlet no se presenta como regla nueva de lógica |
| Alta tipo — alta-tipo-registro.jsp | Edición propia desde combo; nombre/descripción; costo finito ≥0, cupo entero positivo compatible con int | required, number min/step | validacion-altas.js ya cubría obligatorios, rangos y formato numérico | AltaTipoRegistroServlet: rol/propiedad/parseo; Sistema.altaTipoRegistro: datos/costo/cupo/duplicado | Se conserva script específico y límites HTML5. Completo |
| Alta patrocinio — alta-patrocinio.jsp, POST | Institución/nivel/tipo desde combos; aporte numérico positivo, cantidad entera no negativa, código obligatorio; aviso del 20% ya existente | required; inputmode decimal/numeric; combos | alta-patrocinio.js: aporte/cantidad/código/20%, con mensajes nativos; no cubría todos los obligatorios con mensajes junto a campos | AltaPatrocinioServlet y AccesoPatrocinios: selección/propiedad; Sistema.altaPatrocinio: datos, código, institución/edición/tipo, duplicados, 20% | General añade mensajes por campo; validador específico conserva el 20% y ahora rechaza overflow de float y sintaxis no decimal como el backend. Opciones de error no disponibles permanecen visibles/deshabilitadas. Completo |
| Registro — registro-edicion.jsp, POST | Tipo desde radios existentes; modalidad desde opciones; código obligatorio solo al elegir esa modalidad; sin validar en navegador pertenencia/usos reales | Radio tipo required; campo código required asignado dinámicamente | Mostraba/ocultaba código y alternaba required | RegistroEdicionServlet y Sistema.registroAEdicion/ManejadorRegistros.registrar: estado, asistente, tipo, institución, código, usos, cupo y duplicados | General añade selección de radio, rechazo de blancos en código y mensajes por grupo. General no exige código. Completo |
| Alta patrocinio — GET evento | Seleccionar evento existente | Combo required | requestSubmit al cambiar | AltaPatrocinioServlet valida/reconstruye selección | Cumple mediante lista. Autoenvío es mejora de navegación, no validador de datos libres |
| Alta patrocinio — GET edición | Seleccionar edición propia existente del evento | Combo required; evento oculto | requestSubmit al cambiar | AltaPatrocinioServlet/AccesoPatrocinios | Cumple mediante lista y control de servidor |
| Consulta patrocinio — GET evento | Elegir evento existente | Combo required | No necesita validador de texto: no hay texto libre | ConsultaPatrocinioServlet | Cumple mediante lista |
| Consulta patrocinio — GET edición | Elegir edición visible existente | Combo required; evento oculto | No necesita reglas de fechas, contraseña o números | ConsultaPatrocinioServlet/AccesoPatrocinios | Cumple mediante lista |
| Consulta patrocinio — GET institución | Elegir patrocinio/institución existente de la edición | Combo required; selección previa oculta | No necesita validador de texto | ConsultaPatrocinioServlet | Cumple mediante lista y control de servidor |
| Consulta edición — GET evento, edicion.jsp | Elegir evento existente | Combo required | consultas-selectores.js autoenvía | ConsultaEdicionServlet/AccesoEdiciones | Cumple mediante lista |
| Registro — GET evento | Elegir evento existente | Combo required | Autoenvío onchange | RegistroEdicionServlet selecciona ediciones aceptadas | Cumple mediante lista; no modifica registros |
| Consulta tipo — GET evento, tipo-registro.jsp | Elegir evento existente | Combo required | consultas-selectores.js | ConsultaTipoRegistroServlet | Cumple mediante lista |
| Consulta tipo — GET edición | Elegir edición aceptada existente | Combo required; evento oculto | consultas-selectores.js | ConsultaTipoRegistroServlet/AccesoEdiciones | Cumple mediante lista |
| Consulta tipo — GET tipo | Elegir tipo existente en edición | Combo required; selecciones previas ocultas | consultas-selectores.js | ConsultaTipoRegistroServlet | Cumple mediante lista |
| Logout — cabezal.jsp | Ningún dato de negocio; solo acción y token de infraestructura | POST | No precisa validación de campos inventados | FiltroAcceso/Csrf/LogoutServlet | No aplica validación de datos de §7.3; CSRF y POST conservados |

Las categorías del cabezal y las ediciones de un evento se seleccionan también
por enlaces generados desde datos de lógica; no son formularios de texto libre.
No se añadieron validadores ficticios de números o fechas a esos recorridos.

## Implementación y conservación

* `validacion-formularios.js` atiende los siete formularios que aún tenían
  controles parciales. Reutiliza los validadores de fecha y patrocinio; no borra
  sus errores personalizados al validar campos básicos.
* Cada campo recibe feedback Bootstrap accesible, aria-live y aria-describedby,
  conservando instrucciones previamente asociadas. Radios/checkboxes usan un
  mensaje de grupo. Los textos se asignan con textContent, nunca con HTML del usuario.
* Un envío inválido no reinicia ni reescribe valores. Se enfoca el primer campo
  inválido. Los campos del subtipo oculto conservan su valor en el DOM pero no
  se validan ni se envían; el servidor ya distinguía los subtipos.
* Contraseñas: no se añade longitud mínima, complejidad, regex ni trim. Los
  espacios siguen siendo caracteres de la clave, conforme al contrato actual.
* Fechas: se corrige la fecha máxima de nacimiento a día local del navegador,
  en lugar de extraer el día UTC. El servidor conserva LocalDate.now como autoridad.
* No se modifica CSRF, PRG, ISistema, contratos ni código productivo Java.
* La imagen sigue siendo opcional. accept y el aviso de tamaño son ayudas;
  firma, contenido decodificado y límites de seguridad siguen en la lógica.

## Pruebas y límites de la conclusión

La suite JS incluye pruebas de cada regla nueva, scripts reales de alta y
modificación de usuario, cambio de rol sin pérdida de datos, corrección de
contraseña desde ambos campos, fecha local, campos inactivos, mensajes,
accesibilidad, reset, grupos, valores límite y ausencia de formularios.
Las reglas específicas de fecha, patrocinio e institución/tipo conservan sus tests.

El verificador del WAR en Tomcat comprueba las nueve JSP/formularios con los
scripts que corresponden, sesiones y tokens, altas, rechazos del servidor sin
JavaScript, multipart y PRG. Sus peticiones HTTP no ejecutan JavaScript; las
pruebas JS usan dobles del DOM. No se confunde eso con inspección visual humana.

La **validación de navegador y selección de valores de §7.3 queda implementada**
para todos los formularios. Posteriormente se autorizó y aplicó H13 únicamente en
`eventosuy_test`, con respaldo previo, índices funcionales de correo/tipo y la
corrección aprobada de búsquedas de patrocinio por claves exactas. Las pruebas
concurrentes comprueban ahora también el rechazo de correos equivalentes cuando
ambas transacciones habían visto cero filas. Ver el
[procedimiento H13](migraciones/H13-procedimiento.md). Las validaciones del servidor
no se retiraron ni debilitaron. Otra base necesita su propia aplicación autorizada;
no se certifica que tenga los índices solo porque la suite local esté aprobada.
La letra no prescribe una política de mayúsculas/minúsculas: la comparación
insensible a ellas ya es una decisión de la implementación actual de Sistema.
H13 es una brecha de coherencia/concurrencia de esa política, no una regla nueva
que se atribuya a la consigna.

Resultado final, 03/11/2026:

| Verificación | Resultado |
|---|---|
| Entorno | JDK Temurin 25.0.5; release 25 |
| mvn -Pcobertura clean install | BUILD SUCCESS, los tres módulos |
| JUnit lógica | 215 aprobadas, sin fallos/errores/omitidas |
| JUnit web | 73 aprobadas, sin fallos/errores/omitidas |
| JavaScript | 56 aprobadas, sin fallos/omitidas |
| WAR Tomcat 11 | 120 verificaciones HTTP/JSP/Part/PostgreSQL aprobadas |
| Líneas lógica | 1472/1756 = 83,82687927107061 % |
| Ramas lógica | 568/728 = 78,02197802197803 % |
| Base de pruebas | eventosuy_test, confirmada por JDBC/H02 antes de JPA |
| Git | diff --check correcto; sin staging, commits, merges ni push |

Los avisos habituales de cierre de Tomcat sobre desregistro JDBC y detección de
fugas con add-opens no impidieron las comprobaciones ni se ocultaron cambiando
dependencias. Los logs temporales no se incluyen en Git.

## Obligatorio frente a mejoras adicionales

Obligatorios: controles JS de los datos ingresados, elección desde listas,
coincidencia de contraseñas y validaciones del servidor según §6.2/§7.3.
Los mensajes por campo, accesibilidad y conservación se implementan también
por requisito explícito del usuario en esta tarea.

Mejoras opcionales no incorporadas: validación remota de disponibilidad,
indicador de fortaleza de clave, vista previa web, CAPTCHA, nuevos patrones de
nickname, nuevas restricciones de edad o fechas futuras de evento. La confirmación
visual humana y la documentación académica siguen siendo tareas de entrega
distintas de agregar validación JavaScript.
