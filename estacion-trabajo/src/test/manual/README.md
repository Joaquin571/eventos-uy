# Verificaciones de la cuarta etapa de Swing

Estos ejecutores manuales no forman parte de Surefire. Usan paneles Swing reales;
no requieren agregar dependencias a los POM. Las ventanas de confirmación se
responden automáticamente. Crean datos con nombres únicos y no los borran.

Antes de inicializar JPA o escribir, cada ejecutor comprueba por JDBC de sólo
lectura que `current_database()` sea `eventosuy_test`. No usar la base habitual.

## Estado recuperado el 29/10/2026

| Bloque | Implementación existente | Evidencia |
|---|---|---|
| A | Construcción por código de formularios y arranque | `target/arranque.log`: 16 casos construidos |
| B | Modificar usuario: contraseña, confirmación, imagen y errores | `target/bloque-b.log`: 17 comprobaciones |
| C | Imagen opcional en altas de evento, edición e institución | `target/bloque-c.log`: 29 comprobaciones acumuladas de B y C |
| D | Imágenes en consultas y estado de edición | `target/bloque-d-integracion-2.log`: 28 comprobaciones, incluyendo recuperación e integración |

Las eliminaciones existentes de cinco `.form` corresponden a paneles construidos
por código; no se restauraron ni se repitió su implementación al retomar.

## Ejecutar el bloque D y la integración

Requiere haber ejecutado B y C con `VerificarSwing completo`: deja
`target/persistencia-swing.txt` y una imagen temporal para verificar persistencia
desde un proceso nuevo. No ejecutar `clean` antes de recuperar estos archivos.

Primero compilar los tres módulos con `mvn install -DskipTests`.
Después, desde la raíz del repositorio, en PowerShell:

```powershell
$jdkSwing = 'C:\Users\Joaco\.jdks\openjdk-26.0.2'
$tomcatSwing = 'C:\Users\Joaco\Desktop\apache-tomcat-11.0.27'
$env:EVENTOS_DB_NAME = 'eventosuy_test'
$cpSwing = "estacion-trabajo/target/classes;webapp/target/webapp-1.0-SNAPSHOT/WEB-INF/lib/*;$tomcatSwing/lib/*;$tomcatSwing/bin/tomcat-juli.jar"
& "$jdkSwing/bin/javac.exe" --release 25 -encoding UTF-8 -cp $cpSwing -d estacion-trabajo/target/verificaciones estacion-trabajo/src/test/manual/VerificarSwing.java estacion-trabajo/src/test/manual/VerificarBloqueD.java
if ($LASTEXITCODE -ne 0) { throw 'Falló la compilación de las verificaciones.' }
& "$jdkSwing/bin/java.exe" -cp "estacion-trabajo/target/verificaciones;$cpSwing" VerificarBloqueD
if ($LASTEXITCODE -ne 0) { throw 'Falló la verificación del bloque D.' }
```

El ejecutor inicia un Tomcat aislado en puerto libre y contexto `/pruebas`, usando
el WAR expandido de `webapp/target`. No usa ni reinicia el Tomcat de desarrollo.
Inicializa la web antes de cambiar estados o crear el usuario desde Swing.
Comprueba por HTTP visibilidad, imagen, login y perfil después de esos cambios.

Se verificaron los estados Ingresada, Aceptada y Rechazada; las imágenes y su
limpieza en las consultas de usuario, evento, edición y patrocinio; la recuperación
de cuatro imágenes de otro proceso; aceptar/rechazar desde el panel; alta con
contraseña desde Swing; y lectura inmediata desde la web.

## Entorno y límites

El POM exige `release 25`. Se encontró JDK 26, usado con `--release 25`, además de
JDK 8, 17, 21 y 24. No se modificaron los POM. Falta la ejecución final con JDK 25
y revisión visual humana de la distribución de las pantallas.

El primer intento de Maven falló abriendo el archivo de bloqueo de
`maven-install-plugin:3.2.0` en `.m2/repository/.locks`, antes de compilar.
La compilación fuera del sandbox terminó correctamente, confirmando el problema
de acceso al repositorio local. El primer intento del ejecutor D omitía
`bin/tomcat-juli.jar`; se corrigió el classpath, sin agregar bibliotecas.

Tomcat emitió advertencias de detección de fugas y desregistro del driver JDBC al
cerrarse. No impidieron las comprobaciones ni el cierre de la instancia aislada.

La ejecución posterior de `mvn -o install`, con `EVENTOS_DB_NAME=eventosuy_test`
y JDK 26, terminó en `BUILD SUCCESS` para los tres módulos: 90 pruebas de lógica
y 48 de web, sin fallos, errores ni omisiones. Registro:
`target/pruebas-reanudacion.log`. Se ejecutó `install` sin `clean` para conservar
los registros y las imágenes temporales de la sesión anterior.

## Resumen conservado antes de clean install

Los logs de target citados arriba son evidencia histórica revisada el 29/10/2026.
Se conserva aquí su resultado; clean elimina esos logs y las imágenes temporales.
No se deben agregar logs, PDF, target ni archivos temporales al commit.

- A: arranque de 16 casos de uso. Sin revisión visual humana acreditada.
- B: 17 comprobaciones funcionales automatizadas aprobadas.
- B+C: 29 comprobaciones acumuladas aprobadas (no 29 adicionales).
- D e integración (E): 28 comprobaciones aprobadas, incluyendo recuperación de
  cuatro imágenes desde otro proceso, consultas con los tres estados, limpieza de
  imágenes, aceptación/rechazo desde Swing y lectura inmediata por HTTP sin
  reiniciar la web. Incluye alta con contraseña en Swing, login y perfil web.
- Maven install anterior: BUILD SUCCESS el 29/10/2026 a las 19:24:18 -03:00;
  lógica 90, web 48, Swing 0 pruebas Surefire; 138 en total, sin fallos, errores
  ni omisiones. Los ejecutores Swing se ejecutan aparte de Maven.
- Las pruebas con escritura confirmaron eventosuy_test. D comprobó la base por
  JDBC antes de escribir y ambas aplicaciones registraron esa URL.
- JDK usado: 26 con release 25. No se modificaron los POM.
- Pendientes: revisión visual humana y ejecución específica con JDK 25.
- Advertencias de integración: Hibernate HHH000505 ignoró un
  ServiceConfigurationError de BytecodeProviderImpl (not a subtype). Posible
  conflicto de cargadores del ejecutor embebido, causa no demostrada. Tomcat
  desregistró el driver JDBC al cerrar y avisó sobre add-opens para detectar fugas
  de ThreadLocal/RMI. Las comprobaciones terminaron correctamente.
- Advertencias Hibernate: pool integrado y dialecto PostgreSQL explícito.

## Revisión visual humana después de compilar

En IntelliJ, seleccionar JDK 26 para el proyecto y para Maven, sin cambiar el
release 25 del POM. Crear o revisar una configuración Application con clase
principal swing.principal, classpath del módulo estacion-trabajo, directorio de
trabajo en la raíz del repositorio y variable EVENTOS_DB_NAME=eventosuy_test.
Comprobar también las variables de host, puerto, usuario y contraseña según el
README principal. Ejecutar la confirmación JDBC indicada más abajo antes de
hacer altas desde la interfaz. Abrir Swing con Run; el administrador no inicia
sesión.

Alternativa PowerShell desde la raíz, después de mvn clean install:

```powershell
$env:JAVA_HOME = 'C:\Users\Joaco\.jdks\openjdk-26.0.2'
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"
$env:EVENTOS_DB_NAME = 'eventosuy_test'
$cpVisual = 'estacion-trabajo/target/classes;webapp/target/webapp-1.0-SNAPSHOT/WEB-INF/lib/*'
java -cp $cpVisual webapp/src/test/manual/VerificarBaseConsultas.java
if ($LASTEXITCODE -ne 0) { throw 'No se confirmó eventosuy_test.' }
java -cp $cpVisual swing.principal
```

Revisar manualmente, sin dar por aprobados estos puntos hasta observarlos:

1. Abrir los 16 casos del menú. Comprobar títulos, tildes, campos completos,
   botones accesibles, ausencia de recortes y uso con el tamaño habitual de ventana.
2. Modificar usuario: nickname/correo bloqueados, cambio de usuario, contraseña y
   confirmación, error por discrepancia, guardar sin cambiar contraseña e imagen
   opcional. Comprobar que errores conservan los datos y nunca muestran la clave.
3. Altas de evento, edición e institución: imagen y sin imagen, vista previa,
   quitar selección, imagen inválida y mayor a 2 MB. Usar nombres nuevos de prueba.
4. Consultas de usuario, evento, edición y patrocinio: imagen visible, entidad sin
   imagen y selección vacía sin arrastrar la imagen anterior. Para institución,
   observar su imagen en la consulta de patrocinio.
5. Consulta de edición: observar Ingresada, Aceptada y Rechazada. Aceptar/rechazar
   sólo ediciones nuevas de prueba: la decisión no se puede deshacer.
6. Abrir también altas y consultas de patrocinio y tipos de registro: confirmar
   que los formularios migrados desde .form tienen campos y botones utilizables.
7. Si se prueba Swing + web manualmente, ambas aplicaciones deben apuntar a
   eventosuy_test. Verificar que la web ve la aceptación al actualizar y permite
   login con el usuario nuevo de Swing. No usar el Tomcat habitual si apunta a
   eventosuy. La integración HTTP automática ya pasó; falta la observación humana.

Para repetir D después de clean, ejecutar primero VerificarSwing completo para
regenerar persistencia-swing.txt y las imágenes, como parte de las pruebas B+C.
No ejecutar clean entre B+C y D.
## Resultado de clean install (29/10/2026)

- Comando: mvn clean install.
- Entorno confirmado: OpenJDK 26.0.2; Maven 3.10.0; release 25 del POM intacto.
- Antes de ejecutar: confirmación JDBC de sólo lectura con
  VerificarBaseConsultas: eventosuy_test / localhost:5432/eventosuy_test.
- BUILD SUCCESS, finalizado a las 19:32:37 -03:00; duración 26,535 segundos.
- Lógica: 90 pruebas; web: 48; Swing: sin pruebas Surefire.
- Total: 138 pruebas, 0 fallos, 0 errores, 0 omitidas. Los tres módulos recompilados.
- Los ejecutores manuales B/C/D no se repitieron en este cierre; sus resultados
  previos están conservados arriba. Clean eliminó sus archivos de target.
- Log completo temporal fuera del repositorio:
  %TEMP%/pa2026-swing-clean-install-20261029.log. No incluirlo en Git.
- Sin cambios estructurales, de POM, de lógica ni de web.
- git diff --check sin incidencias. Los cambios propuestos para commit pertenecen
  a la cuarta etapa de Swing, sus verificaciones y esta documentación.
- Persisten avisos de compilación sobre operaciones unchecked/unsafe y los
  avisos conocidos de Hibernate (pool integrado y dialecto explícito).
- Pendientes para cierre visual: revisión humana y ejecución específica con
  JDK 25. No se declara aprobada la revisión visual.