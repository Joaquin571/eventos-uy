# Integración de patrocinios

Requiere Java 25 o superior, Tomcat 11 instalado, PostgreSQL y la base `eventosuy_test` existente.
Las credenciales y el host se toman de las mismas variables `EVENTOS_DB_*` de la aplicación.
No usa datos del prototipo ni modifica la base habitual.

1. Ejecutar `mvn test` con Java compatible y `EVENTOS_DB_NAME=eventosuy_test`, después de verificar la conexión.
2. Desde la raíz ejecutar en PowerShell:

```powershell
./webapp/src/test/manual/verificar-patrocinios.ps1 -TomcatRuta 'C:/ruta/apache-tomcat-11' -JdkRuta 'C:/ruta/jdk-25'
```

El script inicia Tomcat en un puerto local libre y contexto `/pruebas`, registra los servlets de producción y el filtro de acceso, y usa login real, JSP reales e `ISistema` real. Verifica la base mediante `SELECT current_database()` antes de cualquier alta. Crea datos con nombres únicos en `eventosuy_test`; no elimina datos. Detiene Tomcat al finalizar.
Comprueba el alta, la recuperación de errores, la consulta, la fecha, el escape HTML, los enlaces y la privacidad por rol/estado. No sustituye la revisión de interacción JavaScript en un navegador.

## Consultas de edición, tipos e imágenes protegidas

```powershell
./webapp/src/test/manual/verificar-consultas.ps1 -TomcatRuta 'C:/ruta/apache-tomcat-11' -JdkRuta 'C:/ruta/jdk-25'
```

El segundo script usa las mismas condiciones de seguridad de base y puerto local. Comprueba los selectores GET sin JavaScript, los enlaces, las fechas, el perfil, los registros general/con código por rol, los tipos con nombre repetido entre ediciones, el escape HTML y los bytes de las imágenes públicas/privadas. También verifica estados vacíos y respuestas 404.

Las pruebas JavaScript se ejecutan con Node, sin dependencias adicionales:

```powershell
node --test webapp/src/test/js/*.test.cjs
```
# Verificación CSRF y Post/Redirect/Get

Después de `mvn -Pcobertura clean install`, con JDK 25 y Tomcat 11:

```powershell
./webapp/src/test/manual/verificar-csrf-prg.ps1 -TomcatRuta 'C:/ruta/apache-tomcat-11' -JdkRuta 'C:/ruta/jdk-25'
```

El verificador exige la barrera JDBC H02 antes de iniciar el WAR. Solo utiliza
`eventosuy_test`, crea identificadores únicos y no ejecuta el seed. Recorre login,
logout, altas, modificación de perfil, registro, multipart con PNG/JPEG/GIF/WEBP, rechazo de
tokens inválidos y sesión expirada, PRG y conservación de datos escapados.
No imprime tokens ni los coloca en URLs. Los logs y clases generados no se entregan.
También comprueba WEBP con y sin pérdida, animaciones, conservación de bytes,
rechazo de archivos truncados/falsificados, exceso de fotogramas y el límite
multipart (413), conservando la imagen anterior tras los rechazos.
Para alta de institución y tipo de registro comprueba que las JSP incluyan el
script local `validacion-altas.js`, los mensajes accesibles por campo y el token
CSRF. Envía también peticiones inválidas sin JavaScript para comprobar que el
servidor sigue rechazándolas sin persistir datos. La interacción del script se
prueba en `validacion-altas.test.cjs` con dobles del DOM; el verificador HTTP no
ejecuta un navegador ni sustituye una inspección visual humana.

Verificación de estas validaciones (03/11/2026): Java 25.0.5,
`mvn -Pcobertura clean install` con BUILD SUCCESS; 215 JUnit de lógica y 73 web,
56 pruebas JavaScript y 120 comprobaciones HTTP del WAR en Tomcat 11 aprobadas.
Persistencia exclusivamente en `eventosuy_test`, comprobada por JDBC mediante H02.
La matriz completa de formularios, controles por rol y límites está en
`docs/cumplimiento-7-3.md`. `validacion-formularios.test.cjs` prueba login,
usuarios, evento, edición, patrocinio y registro, incluidos los scripts reales
de usuario y la conservación de datos. Institución y tipo mantienen su script
específico. La verificación HTTP recorre las nueve JSP con datos de negocio;
logout conserva únicamente el POST protegido, sin validadores de datos ficticios.

La comprobación automática de vistas previas Swing se ejecuta sin ventanas:

```powershell
$env:EVENTOS_DB_NAME = 'eventosuy_test'
$cp = 'estacion-trabajo/target/classes;logica/target/test-classes;webapp/target/webapp-1.0-SNAPSHOT/WEB-INF/lib/*'
New-Item -ItemType Directory -Force estacion-trabajo/target/manual-h11 | Out-Null
javac -encoding UTF-8 -cp $cp -d estacion-trabajo/target/manual-h11 estacion-trabajo/src/test/manual/VerificarImagenesSegurasSwing.java
java -Djava.awt.headless=true -cp "estacion-trabajo/target/manual-h11;$cp" swing.VerificarImagenesSegurasSwing
```

Usar `java` y `javac` del JDK 25. No sustituye la inspección visual humana.

Inventario de POST protegidos: `/login`, `/logout`, `/alta-usuario`,
`/modificar-usuario`, `/alta-evento`, `/alta-edicion`, `/alta-tipo-registro`,
`/alta-institucion`, `/alta-patrocinio`, `/registro-edicion`. Alta de usuario,
modificación, evento, edición e institución usan multipart. GET `/logout` devuelve
405 y conserva la sesión. Los selectores GET siguen siendo consultas.

`CsrfTest` y `AltasPrgTest` atraviesan el filtro real con dobles de HTTP y lógica.
Las pruebas unitarias anteriores que invocan directamente un Servlet conservan
su alcance de controlador; no prueban la cadena de filtros. Los verificadores HTTP
de consultas, patrocinios y bloque D ahora obtienen el token del formulario antes
de enviar POST; no se desactiva el filtro para ejecutarlos.
