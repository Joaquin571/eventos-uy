# Correcciones localizadas S01, S02 y S03

03/11/2026. Rama `dev`, base `0284e7cc1240fc7e7f08d5454d70aea0bc56301b`. Se conservaron todos los cambios previos. Sin commits, push ni migraciones.

## S01: autenticación determinista

`ManejadorUsuarios.buscarPorNicknameOCorreo` obtiene todos los candidatos de la consulta parametrizada. `Sistema.autenticar` verifica sus contraseñas y devuelve un DTO únicamente si exactamente una cuenta coincide con las credenciales. No depende del orden de filas.

- Nickname de A igual a correo de B, contraseñas distintas: cada contraseña autentica su cuenta correspondiente.
- Ambas cuentas con la misma contraseña: se rechaza el identificador ambiguo; no se elige una cuenta arbitrariamente. Se puede utilizar otro identificador no ambiguo de la cuenta.
- Credencial incorrecta, cuenta inexistente, identificador excesivo/no persistible o ambigüedad: mismo mensaje genérico, sin enumerar cuentas.
- No se prohíbe `@`, no se cambia la unicidad y no se modifican índices ni `ISistema`.

Se conservó el método antiguo de consulta de una fila para compatibilidad interna, documentado expresamente como inadecuado para autenticar. La autenticación utiliza la búsqueda completa.

## S02: límites de almacenamiento y errores públicos

Se inspeccionó por JDBC exclusivamente `information_schema.columns` de `eventosuy_test`, después de ejecutar la protección H02. Las columnas varchar revisadas de las entidades tienen longitud 255, coherente con los mapeos JPA existentes sin longitud explícita. No se consultaron datos de la base habitual ni se modificó el esquema.

`Sistema.validarTexto` rechaza más de 255 caracteres Unicode y el carácter NUL, no admitido por PostgreSQL en estos textos. Se cuentan puntos de código, no unidades UTF-16: 255 emojis se aceptan y se comprueba su persistencia.

| Datos recibidos y almacenados | Validación |
|---|---|
| Usuario: nickname, nombre, correo | Longitud/NUL; se conserva formato de correo y unicidad |
| Asistente: apellido; modificación de nombre/apellido | Longitud/NUL; se conserva validación de nacimiento |
| Organizador: descripción y sitio web opcional | Longitud/NUL en alta y modificación |
| Institución: nombre, descripción, sitio web opcional en lógica | Longitud/NUL; el controlador conserva su validación existente de URL |
| Evento: nombre, sigla, descripción | Longitud/NUL antes de persistir |
| Edición: nombre, sigla, ciudad y país | Longitud/NUL; se conservan fechas, referencias y estado inicial |
| Tipo: nombre, descripción | Longitud/NUL; se conservan costo finito/no negativo y cupo positivo |
| Patrocinio: código | Longitud/NUL; se conservan aporte, cantidad, referencias, nivel y tope económico |
| Categoría y referencias obligatorias | Se aplica el límite en validaciones de texto antes de crear datos |
| Imagen | Conserva H11: MIME/formatos, bytes, decodificación y límites propios |

Las selecciones de entidades existentes se resuelven contra la lógica; no se convierten en nuevos textos persistidos. Los números y fechas conservan sus parseos y validaciones existentes. La contraseña original no se almacena en una columna varchar: se guarda su hash, por lo que no se le inventó el límite de 255 del texto persistido. Tampoco se impuso una nueva política de URL a datos que ya admitía la lógica.

Los errores de negocio siguen llegando al formulario con mensajes útiles. `AltaEventoServlet` ya no convierte una RuntimeException inesperada en un error de negocio mostrando su mensaje técnico. Las excepciones inesperadas y respuestas 500 utilizan `WEB-INF/views/error-interno.jsp`, configurada en `WEB-INF/web.xml`. La página no imprime la excepción, el mensaje técnico ni los atributos de error; funciona sin consultar la lógica ni incluir un cabezal que pueda volver a fallar.

La página mantiene el estado HTTP 500 y pide revisar el resultado antes de reenviar: no afirma que cualquier operación fallida haya hecho rollback. Tomcat puede registrar el detalle internamente; no se devuelve al navegador.

## S03: resultado parcial explícito, sesión y PRG

Se conserva esta secuencia en `ModificarUsuarioServlet`:

1. Leer y validar la imagen antes de modificar datos.
2. Modificar datos y contraseña con la operación atómica existente H05.
3. Intentar asociar la imagen opcional.
4. Si falla el paso 3, registrar el error internamente y preparar una advertencia genérica que explica que el perfil y, si se cambió, la contraseña ya se guardaron, pero la imagen no se pudo actualizar.
5. Actualizar `usuario` en HttpSession consultando los datos guardados.
6. Guardar mensajes flash y redirigir al perfil. El GET propio consume el mensaje una sola vez y la JSP lo muestra escapado.

No se revierte ni se afirma revertir un commit confirmado. No se incorporan transacciones anidadas. CSRF, identidad tomada de sesión, propiedad y PRG siguen vigentes.

## Pruebas y resultados

Con Temurin 25.0.5, `EVENTOS_DB_NAME=eventosuy_test` y comprobación JDBC H02 antes de JPA:

- `mvn -Pcobertura clean install`: **BUILD SUCCESS**, lógica, Swing y web compilados.
- Lógica: **236 JUnit**, cero fallos, errores u omitidas.
- Web: **73 JUnit**, cero fallos, errores u omitidas.
- JavaScript: **56 pruebas aprobadas**, cero fallos u omitidas.
- `AuditarSeguridadWeb`, WAR real en Tomcat 11.0.27: **63 comprobaciones correctas, cero expectativas fallidas**.
- Regresión `VerificarCsrfPrgWeb`, mismo Tomcat: **120 verificaciones HTTP/JSP/Part/PostgreSQL aprobadas**, incluyendo imágenes PNG, JPEG, GIF y WEBP.
- `git diff --check`: sin errores.

Las tres reproducciones originales pasan ahora:

| Reproducción | Resultado |
|---|---|
| Colisión nickname/correo, claves distintas | Ambos POST de login devuelven 302 y se verifica la identidad en JUnit |
| Nombre de 256 caracteres | HTTP 200 con error de validación, sin detalles internos ni usuario insertado |
| Fallo inyectado al asociar imagen tras modificar perfil | HTTP 302; datos y clave nueva confirmados; advertencia explícita, cabezal actualizado y flash de una sola lectura |

Se ampliaron las pruebas HTTP a campos excesivos de usuario, evento, edición, institución y tipo; límite exacto de 255; formatos de correo/NUL; costos negativos/no finitos; y excepción inesperada inyectada en consulta de usuarios. Esta última conserva HTTP 500, muestra la página genérica y no filtra el detalle técnico artificial.

Las regresiones HTTP conservan además las comprobaciones de roles, propiedad, parámetros manipulados, registros privados, ediciones/imágenes no públicas, escape de texto y cierre de sesión. Son pruebas de integración automatizadas sobre Tomcat/PostgreSQL, no inspección visual humana.

Se agregaron tres pruebas JUnit de login y ocho de límites. Dos pruebas anteriores H05 ahora provocan su error SQL directamente en el manejador, porque Sistema lo rechaza anticipadamente con S02: se conserva la comprobación de rollback real de datos y hash, además de las nuevas comprobaciones de rechazo preventivo.

JaCoCo: **1496/1783 líneas = 83,903533371 %**, 287 sin cubrir; **582/742 ramas = 78,436657682 %**. Informe HTML: `logica/target/site/jacoco/index.html`. No se excluyeron clases ni se suman los auxiliares HTTP al conteo JUnit o a esta cobertura.

## Archivos de esta tanda

Producción:

- `logica/src/main/java/implementacion/Sistema.java`.
- `logica/src/main/java/manejadores/ManejadorUsuarios.java`.
- `webapp/src/main/java/web/AltaEventoServlet.java`.
- `webapp/src/main/java/web/ModificarUsuarioServlet.java`.
- `webapp/src/main/java/web/UsuarioServlet.java`.
- `webapp/src/main/webapp/WEB-INF/views/usuario.jsp`.
- `webapp/src/main/webapp/WEB-INF/views/error-interno.jsp` (nuevo).
- `webapp/src/main/webapp/WEB-INF/web.xml` (nuevo).

Pruebas:

- `logica/src/test/java/implementacion/LoginTest.java`.
- `logica/src/test/java/implementacion/LimitesTextoTest.java` (nuevo).
- `logica/src/test/java/implementacion/ModificacionPerfilAtomicaTest.java`.
- `webapp/src/test/manual/AuditarSeguridadWeb.java`.

Documentación: este informe y una nota de cierre en `auditoria-H07-H08-seguridad.md`. Los cambios anteriores del árbol no se atribuyen a esta tanda. No se restauró el prototipo ni se modificaron POM o dependencias en esta corrección.

## Repetición

Los comandos del informe H07/H08 siguen siendo válidos: compilación completa con cobertura, `node --test webapp/src/test/js/*.test.cjs`, y `auditar-seguridad.ps1` con rutas de Tomcat/JDK 25. El auxiliar de seguridad ahora debe finalizar correctamente. Ejecutar además `verificar-csrf-prg.ps1` para las regresiones multipart/CSRF/PRG existentes.

## Riesgos residuales

- H07/H08 siguen siendo las limitaciones documentadas del JAR confiable; estas correcciones no convierten ISistema en una API remota autorizada.
- Una identidad compartida con contraseña idéntica se rechaza por seguridad; la resolución no modifica ni fusiona cuentas existentes.
- Perfil + imagen siguen siendo dos operaciones; el resultado parcial ahora es explícito. No afirmar atomicidad global de altas o modificaciones con imagen.
- Las respuestas ya enviadas parcialmente por un fallo de red no pueden sustituirse de forma retroactiva por una página genérica.
- Persisten las advertencias conocidas de Tomcat/Hibernate en el entorno de pruebas; los errores artificiales inyectados se registran internamente de manera esperada.
- Falta inspección visual humana de las pantallas afectadas. La verificación HTTP real no se presenta como prueba visual.

No se modificó la base habitual, no se ejecutaron migraciones, no se cambió ISistema y no se realizaron commits ni push. Logs temporales permanecen fuera de Git; PDF y log local anteriores se conservaron.
