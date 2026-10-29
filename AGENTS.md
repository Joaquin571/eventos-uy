# AGENTS.md – eventos.uy (Laboratorio 2, Grupo 04)

Proyecto del curso *Programación de Aplicaciones* (Tecnólogo en Informática, UTEC, 2026).
Este archivo es la guía para un agente de código que trabaje en el repositorio.
**Leelo completo antes de tocar nada.** La fuente de verdad de los requisitos es la letra del
Laboratorio 2; acá está resumida.

- Entrega final: **martes 3 de noviembre de 2026, 23:59** (Moodle). Defensa: fecha a confirmar.
- Idioma: **español** (código de dominio, mensajes de error, comentarios y textos de pantalla).
  Seguí el estilo que ya tiene el código.

---

## 1. Cómo trabajar

1. **Antes de implementar, verificá el estado real**: `git status`, `git log --oneline -15`, y
   buscá si la tarea ya existe (por ejemplo `ls webapp/src/main/java/web`). Varias personas trabajan
   en paralelo y una tarea de este archivo puede estar hecha o a medias.
2. **Cambios mínimos y localizados.** No refactorices, no renombres, no reformatees archivos que
   no sean parte de la tarea. No cambies firmas de `ISistema` salvo que la tarea lo pida, y si lo hacés,
   actualizá todos los clientes (web, Swing, seed y pruebas).
3. **No inventes API.** Antes de llamar a un método, leé su firma en `ISistema` y en los `Dt*`.
4. **Compilá y probá antes de dar algo por terminado.** Si no podés ejecutar (por ejemplo, falta la base
   de datos), decilo explícitamente en vez de asumir que anda.
5. **No agregues librerías** sin pedirlo. Las dependencias están en los `pom.xml` de cada módulo.
6. Los archivos usan **fin de línea CRLF** (Windows). Conservalo.
7. **Git:** una rama por tarea (`lab2-<tema>`), commits chicos con mensaje claro en español, PR hacia
   `dev`. Nunca `push --force`. No commitees `.idea/`, `.smarttomcat/`, `target/`, `out/` ni archivos `.zip`.
8. Si un requisito es ambiguo, **preguntá** en lugar de elegir en silencio.

---

## 2. El proyecto

Maven multimódulo, **Java 25**, PostgreSQL, Tomcat 11 (Jakarta Servlet/JSP, JSTL), Bootstrap 5.

| Módulo | Qué es |
|---|---|
| `logica/` | JAR con reglas de negocio, tipos de datos (`Dt*`), `ISistema`, manejadores y acceso a PostgreSQL (JPA/Hibernate 7) |
| `estacion-trabajo/` | Aplicación Swing del administrador (`swing.principal`). Usa su propia copia de la lógica |
| `webapp/` | WAR con servlets y JSP. Lleva el JAR de la lógica dentro (`WEB-INF/lib`) |

Las dos aplicaciones comparten **la misma base PostgreSQL**: lo que se crea en una se ve en la otra.

### Rutas importantes

```
logica/src/main/java/{clases,datatypes,interfaces,implementacion,manejadores,persistencia,seguridad,datos}
logica/src/test/java/implementacion        ← pruebas JUnit 5 (necesitan PostgreSQL)
estacion-trabajo/src/main/java/swing       ← pantallas Swing (principal.java es el main)
webapp/src/main/java/web                   ← servlets, filtro y listener
webapp/src/main/webapp/WEB-INF/views       ← JSP (no se abren por URL directa)
webapp/src/main/webapp/{css,js,img,vendor} ← estáticos
```

### Compilar, ejecutar y probar

- **Maven:** `mvn` no está en el PATH de Windows; se usa el panel Maven de IntelliJ
  (`eventos-uy → Lifecycle → clean, install`). Debe quedar en verde.
- **Base de datos:** crear una base vacía `eventosuy`. Hibernate crea las tablas solo.
  Credenciales por defecto y variables de entorno (`EVENTOS_DB_HOST`, `EVENTOS_DB_PORT`,
  `EVENTOS_DB_NAME`, `EVENTOS_DB_USER`, `EVENTOS_DB_PASSWORD`): ver `README.md`.
- **Datos de prueba:** ejecutar `logica/src/main/java/datos/CargarDatosPrueba.java`. Usuarios:
  `imm` y `mvdcomics` (organizadores), `juan23`, `maria88`, `ana_utec` (asistentes).
- **Pruebas:** usan la base **`eventosuy_test`** (vacía) con `EVENTOS_DB_NAME=eventosuy_test`.
  Ejecutar con cobertura desde IntelliJ (*Run Tests with Coverage*). Se exige **≥ 80 % de líneas**
  en la capa lógica. No se piden pruebas de Swing ni de la web.
- **Web:** Smart Tomcat, *deployment directory* `webapp/src/main/webapp`, módulo `webapp`,
  contexto `/`, puerto 8080.
- **Swing:** ejecutar `estacion-trabajo/src/main/java/swing/principal.java`.

---

## 3. Reglas de diseño (obligatorias)

### Lógica (`logica`)

- `ISistema` es **la única interfaz** que usan la web y Swing. Debe poder reemplazarse por un cliente
  remoto en la próxima iteración: los clientes nunca usan manejadores ni entidades.
- Entre la lógica y los clientes viajan **tipos de datos** (`Dt*`), nunca entidades JPA.
- Errores de negocio: `IllegalArgumentException` con **mensaje en español**. Transición de estado de
  una edición no permitida: `IllegalStateException`. (`altaEvento` y `altaCategoria` lanzan
  `Exception` marcada por compatibilidad con Swing.)
- Las **fechas de alta, de registro y de patrocinio las asigna la lógica con `LocalDate.now()`**; se
  ignora la fecha que traiga el `Dt`. Nunca debe pedirse una fecha de alta en un formulario.
- Contraseñas: se guardan con hash (`seguridad.HashContrasenia`, PBKDF2). **Nunca** se devuelven ni se
  muestran. La confirmación solo existe en los `Dt` de alta y modificación.
- Imágenes (usuario, evento, edición, institución): se guardan en la base, tabla `imagen`, con
  `ISistema.asociarImagen` / `obtenerImagen` / `validarImagen`. `ValidadorImagen` acepta PNG, JPEG, GIF
  y WEBP de hasta 2 MB y verifica la firma del archivo. La imagen es siempre **opcional**.
- Cada operación abre su propio `EntityManager` y no hay caché en memoria: la web y Swing no se
  desincronizan.

### Web (`webapp`)

- **MVC estricto.** Los servlets solo llaman a `ContextoListener.sistema(req)` (un `ISistema`). Nada
  de JPA, SQL, entidades ni manejadores en servlets o JSP. Las JSP no aplican reglas de negocio.
- JSP en `WEB-INF/views`, sin scriptlets, con JSTL (`<%@ taglib prefix="c" uri="jakarta.tags.core" %>`).
  **Todo texto que venga de datos o del usuario se muestra con `<c:out>`.**
- Cada página incluye `fragmentos/cabezal.jsp` (menú por rol y categorías) y usa
  `${pageContext.request.contextPath}` para las rutas absolutas.
- **Sesión del servidor** (`HttpSession`): atributo `usuario` (un `DtUsuario`). El rol se deduce con
  `instanceof DtOrganizador / DtAsistente` y queda disponible como atributo de la petición `rol`
  (`visitante`, `asistente` u `organizador`).
- **Control de acceso:** `FiltroAcceso` tiene un mapa `PERMITIDOS` con las rutas restringidas por rol.
  Toda ruta nueva restringida **se agrega ahí**. Las no listadas son públicas.
- Formularios con archivo: `enctype="multipart/form-data"` y `@MultipartConfig` en el servlet; la imagen
  se lee con `Subidas.leerImagen`, se valida **antes** del alta con `validarImagen`, y se asocia
  **después** con `asociarImagen`.
- Imágenes en pantalla: `<img src="/imagen?tipo=usuario|evento|edicion|institucion&id=...">` (servlet
  `ImagenServlet`; si no hay imagen devuelve la imagen por defecto).
- Las altas con éxito redirigen (patrón Post/Redirect/Get). Si hay error, se vuelve a mostrar el formulario
  **conservando lo que el usuario escribió** (menos las contraseñas) y con el mensaje que lanzó la lógica.
- Las fechas se muestran como **`dd/MM/yyyy`**, formateadas en el servlet (patrón `RegistroVista`).
- Validación en el navegador con HTML5 y JavaScript (obligatorios, tipos, coincidencia de contraseña,
  fecha de fin no anterior a la de inicio, etc.). **No reemplaza** la de la lógica.
- Bootstrap está incluido en `vendor/`: el sitio no debe depender de servicios externos.

### URLs y claves

La lógica identifica por **nombre**, no por número. Convención de URLs:

| Pantalla | URL |
|---|---|
| Evento | `/evento?nombre=...` |
| Edición | `/edicion?nombre=...` (si falta `evento`, se busca a qué evento pertenece) |
| Tipo de registro | `/tipo-registro?edicion=...&nombre=...` |
| Registro a edición | `/registro-edicion?edicion=...` |
| Consulta de registro | `/consulta-registro?edicion=...` |
| Patrocinio | `/consulta-patrocinio?edicion=...&institucion=...` |
| Perfil | `/usuario?nick=...` |

Codificá los parámetros con `<c:url>` + `<c:param>` (nombres con espacios y tildes).

### Estación de trabajo (`estacion-trabajo`)

- Panel por caso de uso en `swing/`. Algunos usan el diseñador de formularios de IntelliJ (`.form`);
  los nuevos (`AltaUsuarioPanel`, `AceptarRechazarEdicionPanel`) se arman por código: usalos de modelo.
- Los errores de la lógica se capturan y se muestran en un `JOptionPane` con el mensaje.
- El administrador **no inicia sesión**: entra directo.

---

## 4. Reglas de la letra que más se rompen

1. **Estados de la edición:** nace `Ingresada`; solo pasa a `Aceptada` o `Rechazada` (caso de uso
   Aceptar/Rechazar, en Swing); no vuelve. Al **público** solo se le muestran las `Aceptada`. El
   organizador, **en su propio perfil**, ve además sus `Ingresada` y `Rechazada` y puede abrir su detalle;
   para el resto ese detalle no existe. Solo se puede registrar a una edición `Aceptada`.
2. **Registros:** los de un asistente solo los ve ese asistente en su propio perfil. El organizador ve el
   listado de registros solo de **sus** ediciones. Un asistente registrado en una edición ve el detalle
   de su registro en la consulta de la edición.
3. **Patrocinio:** el **código no se publica** en la consulta; lo ve únicamente el organizador de esa
   edición. Reglas: una institución patrocina una edición una sola vez; el código es único en la
   plataforma; el costo de los registros gratuitos no supera el 20 % del aporte.
4. **Registro con código:** el código debe corresponder a la edición, al tipo de registro elegido y a la
   institución del asistente, y tener usos disponibles. Un asistente sin institución solo se registra de
   forma general. Cupo del tipo y un solo registro por asistente y edición.
5. **Un usuario solo modifica su propio perfil:** el nickname sale de la sesión, nunca del formulario.
   Nickname y correo no se modifican.
6. **Sin callejones sin salida:** desde cada resultado debe poder llegarse a los casos relacionados
   (evento → edición → tipo de registro / patrocinio / registro, según el rol). Ningún enlace del menú
   puede dar 404.
7. Operaciones por rol: Visitante (consultas, alta de usuario, login), Asistente (registro, mis
   registros), Organizador (altas de evento, edición, tipo de registro, institución, patrocinio).

---

## 5. Tareas pendientes (en orden de prioridad)

> Estado al 22/10/2026. **Verificá primero qué existe** (ver sección 1): algunas pueden estar hechas.
> Cada tarea termina con compilación en verde y, si toca la lógica, pruebas JUnit.

### T1. Alta y consulta de patrocinio (web)
- `AltaPatrocinioServlet` (`/alta-patrocinio`, solo organizador) + JSP. Flujo: evento → sus ediciones
  (en cualquier estado) → tipos de registro de la edición e instituciones. Campos: institución, nivel
  (Platino, Oro, Plata o Bronce), aporte, tipo y cantidad de registros gratuitos, código. Los errores
  salen de `ISistema.altaPatrocinio` (`IllegalArgumentException`).
- `ConsultaPatrocinioServlet` (`/consulta-patrocinio`, público): evento → ediciones Aceptada →
  patrocinios → detalle (institución, nivel, aporte, tipo, cantidad de gratuitos, fecha de alta). Usar
  `consultarPatrocinio(edicion, institucion)`. **El código solo lo ve el organizador dueño de la edición.**
- Agregar `/alta-patrocinio` al mapa del filtro. Los enlaces del cabezal ya existen.
- **Aceptación:** un visitante y un asistente no ven el código (probar que no aparece en el HTML); el
  organizador dueño sí; los tres errores (institución repetida, código repetido, tope del 20 %) se
  muestran con su mensaje.

### T2. Consulta de tipo de registro (web)
- Servlet `/tipo-registro?edicion=...&nombre=...` (público) + JSP: evento → ediciones Aceptada → tipos →
  todos los datos del tipo (nombre, descripción, costo, cupo). Usar `consultarTipoRegistro`.
- Enlazar desde la consulta de edición (hoy los tipos se muestran como texto, sin enlace).
- **Aceptación:** se llega por enlace desde la consulta de edición y por el flujo de combos.

### T3. Completar la consulta de edición (web)
- `ConsultaEdicionServlet` + `edicion.jsp` hoy muestran solo datos y tipos. Agregar: **imagen**,
  organizador con enlace a su perfil, **patrocinios** (con enlace, sin código), tipos con enlace,
  y según el rol: organizador de esa edición → enlace al listado de registros; asistente registrado →
  su registro; asistente no registrado → botón **Registrarme** (`/registro-edicion?edicion=...`).
- Fechas en `dd/MM/yyyy` (hoy salen como `2026-11-10`).
- **Aceptación:** se cumple la regla 1 y 2 de la sección 4; sin enlaces rotos.

### T4. Estación de trabajo: lo que falta de la sección 6.1
- **Modificar usuario** (`ModificarUsuarioPanel`, usa `.form`): permitir cambiar contraseña (con
  confirmación) e imagen; capturar `IllegalArgumentException` (la lógica ahora valida) y mostrar el mensaje.
- **Imagen opcional** en las altas de evento, edición e institución (`asociarImagen` después del alta).
- **Consultas:** mostrar imagen (si la hay) y estado de las ediciones (los tres estados).
- **Aceptación:** el administrador puede dar de alta un usuario con contraseña, aceptar una edición y
  ver el cambio reflejado en la web sin reiniciar nada.

### T5. Lógica: fechas automáticas
- `registroAEdicion` (versión de **5 parámetros**) debe usar `LocalDate.now()` y validar solo `dt == null`.
  Dejar la sobrecarga de 4 parámetros como está (delega en la de 5).
- **Fecha de alta del usuario:** hoy no se guarda. Agregar `fechaAlta` a `Usuario` (se completa en el
  constructor), a `DtUsuario`, y mostrarla en el perfil como «Miembro desde» (`dd/MM/yyyy`).
- **Aceptación:** el perfil de un usuario nuevo muestra la fecha de hoy.

### T6. Validación en el navegador (JavaScript)
- Revisar los formularios nuevos de la web (evento, edición, tipo de registro, registro, patrocinio):
  obligatorios, tipos, fecha de fin ≥ inicio, costo ≥ 0, cupo > 0, contraseñas coincidentes. Los
  valores que ya existen en el sistema se eligen de una lista (categoría, evento, edición, tipo,
  institución, nivel).

### T7. Pruebas JUnit
- Hoy hay ~79 pruebas (`LoginTest`, `UsuarioTest`, `ImagenesTest`, `ValidadorImagenTest`, `RegistroTest`,
  `EstadoEdicionTest`, `ReglasAltaTest`). Medir la cobertura de líneas de `logica` y, si no llega al
  80 %, agregar pruebas donde falte (consultas de patrocinio, listados, manejadores). Cada prueba usa
  nombres únicos (sufijo con `System.nanoTime()`).

### T8. Limpieza del repositorio
- Borrar las páginas estáticas de la parte 1 ya convertidas a JSP (`webapp/src/main/webapp/pages/*.html`,
  `index.html`) y los `.js` que dejen de usarse. Se borra cada una **solo** cuando su JSP funciona.
- Dejar de versionar `.idea/` y `.smarttomcat/`, y agregar `.idea/`, `.smarttomcat/` y `*.zip` al `.gitignore`.

### Tareas humanas (no son de código)
- Documentación de análisis y diseño (modelo de dominio actualizado, diagramas de secuencia del sistema,
  diseño MVC / JAR embebido / sesión / imágenes / estados). Debe entrar en la entrega.
- Prueba final **desde cero con Java 25**: clonar en una carpeta limpia, bases nuevas (`eventosuy` y
  `eventosuy_test`), seed, web, Swing y pruebas con cobertura. El README debe coincidir con lo que se hace.

---

## 6. Definición de terminado

- [ ] Compila (`clean install` en verde, los tres módulos).
- [ ] La pantalla o caso de uso se probó con visitante, asistente y organizador (según corresponda).
- [ ] Sin enlaces rotos y sin 404 en el menú.
- [ ] Texto del usuario escapado con `<c:out>`; sin scriptlets; sin entidades en las JSP.
- [ ] Rutas restringidas agregadas a `FiltroAcceso`.
- [ ] Si se tocó la lógica: pruebas nuevas o actualizadas, y las existentes siguen en verde.
- [ ] Seed (`CargarDatosPrueba`) sigue funcionando y cubre lo nuevo.
- [ ] README actualizado si cambió algún paso de instalación o ejecución.
- [ ] Sin archivos de editor ni binarios en el commit.

## 7. Trampas conocidas

- `registroAEdicion` tiene **dos sobrecargas** (4 y 5 parámetros): no son un duplicado; no borrar ninguna.
- `altaEdicion`, `altaTipoRegistro` y `altaPatrocinio` lanzan `IllegalArgumentException` con el motivo; no
  asumas que devuelven `false` ante un error.
- `ConsultaEdicionServlet` recibe `evento` y `nombre`; si llega solo `nombre` debe resolver el evento.
- En `ModificarUsuarioPanel` los campos están declarados en el `.form`: editar el `.form` y la clase juntos.
- `Sistema.java` no tenía `import java.time.LocalDate;` originalmente: verificar al usar fechas.
- Las pruebas corren contra PostgreSQL real; sin la base `eventosuy_test` fallan todas con el mismo error
  de conexión (no es un bug del código).
- Si Tomcat da 404 en `/`, la aplicación no arrancó: mirar el log de Tomcat (casi siempre es la base o el driver).
