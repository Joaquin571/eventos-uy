# Pruebas y cobertura - Eventos UY Laboratorio 3

Esta guía usa exclusivamente PostgreSQL `eventosuy_test` en `localhost:5432`. Las pruebas de lógica crean y modifican datos: nunca usar `eventosuy` ni `eventosuy_final`. No subir contraseñas, archivos `.dump`, reportes `target/` ni configuraciones privadas a Git.

## 1. Preparar la base de pruebas

Requisitos: PostgreSQL 17, JDK 25, Maven y el esquema de Lab 3 aplicado a `eventosuy_test`. Antes de migrar una base existente, hacer un respaldo fuera del repositorio y verificar que `SELECT current_database()` devuelve `eventosuy_test`. Los scripts de Lab 3 se aplican solo tras comprobar columnas, restricciones y duplicados existentes; el orden y la situación real se documentan al final de esta guía.

En PowerShell, desde la raíz del proyecto, introducir la contraseña mediante un diálogo seguro; no escribirla en un comando ni en un archivo:

```powershell
Set-Location 'C:\Users\Joaco\Desktop\PA_2026'
$env:EVENTOS_DB_HOST = 'localhost'
$env:EVENTOS_DB_PORT = '5432'
$env:EVENTOS_DB_NAME = 'eventosuy_test'
$credencialPruebas = Get-Credential -Message 'Credencial local de eventosuy_test'
$env:EVENTOS_DB_USER = $credencialPruebas.UserName
$env:EVENTOS_DB_PASSWORD = $credencialPruebas.GetNetworkCredential().Password

& 'C:\Program Files\PostgreSQL\17\bin\psql.exe' -h localhost -p 5432 -U $env:EVENTOS_DB_USER -d eventosuy_test -X -c 'SELECT current_database(), current_schema()'
& 'C:\Program Files\PostgreSQL\17\bin\psql.exe' -h localhost -p 5432 -U $env:EVENTOS_DB_USER -d eventosuy_test -X -c "SELECT table_name,column_name,data_type FROM information_schema.columns WHERE table_schema='public' AND ((table_name='registro' AND column_name='asistenciaconfirmada') OR (table_name='edicion' AND column_name IN ('fechaarchivo','videoyoutube','descripcion')) OR (table_name='evento' AND column_name IN ('visitas','finalizado'))) ORDER BY table_name,column_name"
```

La barrera `soporte.ProteccionBasePruebas` exige `EVENTOS_DB_NAME=eventosuy_test`, URL JDBC terminada en esa base y confirmación mediante `SELECT current_database()` antes de ejecutar pruebas persistentes. El comando anterior permite una comprobación manual adicional. Si no indica `eventosuy_test`, detenerse.

## 2. Ejecutar JUnit con Maven

Los argumentos Maven que empiezan con `-D` van **entre comillas** en PowerShell. Primero ejecutar casos focalizados:

```powershell
mvn '-Dtest=ConsultasSistemaTest,PatrocinioTest,VisitasEventoTest,AsistenciaYArchivoTest' '-Dsurefire.failIfNoSpecifiedTests=false' test
```

Tras integrar el sitio móvil, verificar también el video de edición, la confirmación de asistencia, el acceso móvil y el transporte SOAP HTTPS junto con el flujo de patrocinios:

```powershell
mvn -o '-Dtest=MovilVideoYAsistenciaTest,MovilAccesoTest,FiltroAccesoMovilTest,SesionMovilWebTest' '-Dsurefire.failIfNoSpecifiedTests=false' test
mvn -o '-Dtest=PortalSoapHttpsTest,DisponibilidadSoapTest,PatrociniosFlujoTest,PatrociniosAutorizacionTest,InicioSoapTest' '-Dsurefire.failIfNoSpecifiedTests=false' test
```

Si pasan, ejecutar una sola suite completa con el perfil JaCoCo. Es el ciclo `clean verify` solicitado, con `-Pcobertura` para activar medición y umbral:

```powershell
mvn '-Pcobertura' clean verify
```

El perfil `cobertura` del POM raíz activa el agente JaCoCo y genera informes HTML/XML/CSV en `logica/target/site/jacoco/`; el POM de `logica` exige 80 % de líneas. Los resultados JUnit por clase están en `logica/target/surefire-reports/` y, para otros módulos, en sus respectivos `target/surefire-reports/`. Revisar `Failures` (aserciones) y `Errors` (excepciones o infraestructura) por separado. Si falla una clase, corregir la causa y repetir **sólo esa clase** antes de plantear otra suite completa.

## 3. Demostrar cobertura en IntelliJ IDEA

1. Abrir el proyecto desde su `pom.xml` con JDK 25. Usar la misma base `eventosuy_test` y credenciales locales. En **Run > Edit Configurations**, configurar las cinco variables `EVENTOS_DB_*` en la configuración o plantilla JUnit. No compartir ni versionar esa configuración si contiene contraseña.
2. Ejecutar `soporte.ProteccionBasePruebas` o una clase JUnit persistente y comprobar en la consola la línea `Base efectiva confirmada por JDBC: eventosuy_test`.
3. Seleccionar el módulo `logica` o su directorio `src/test/java` y elegir **Run with Coverage**. En las opciones del runner incluir el código productivo de `logica`, no tests ni clases generadas. Esperar a que termine la suite.
4. Abrir la ventana **Coverage** y mostrar el porcentaje de **Lines** de `logica`. Abrir una clase del módulo: verde significa línea ejecutada; amarillo, línea parcialmente cubierta; rojo, línea no ejecutada. Mostrar una clase de negocio cubierta y una con líneas rojas para explicar los casos pendientes. Si hay varias suites de cobertura activas, seleccionar solo la ejecución que se está defendiendo en **Run > Manage Coverage Reports**.
5. Comparar con `logica/target/site/jacoco/index.html`. JaCoCo es evidencia adicional; la letra del laboratorio pide el porcentaje medido con **Run with Coverage** de IntelliJ. Ambos porcentajes pueden diferir por filtros y motor de instrumentación; indicar cuál se está mostrando.

Referencia de la interfaz: [documentación oficial de IntelliJ IDEA sobre cobertura](https://www.jetbrains.com/help/idea/code-coverage.html).

## 4. Esquema, respaldo y migraciones

El 20/11/2026 se comprobó por lectura `current_database()=eventosuy_test` y `current_schema()=public` en PostgreSQL 17. Estaban presentes `registro.asistenciaconfirmada BOOLEAN NOT NULL DEFAULT FALSE`, `edicion.fechaarchivo`, `edicion.videoyoutube` (255), `edicion.descripcion` (2048), `evento.visitas BIGINT NOT NULL DEFAULT 0`, `evento.finalizado BOOLEAN NOT NULL DEFAULT FALSE`, la restricción de estado que admite `ARCHIVADA`, la tabla `seguimiento_usuario` con claves foráneas y restricción de no seguirse a sí mismo, y los índices `ux_usuario_correo_ci` y `ux_tipo_registro_edicion_nombre_ci`. Las comprobaciones de duplicados para ambos índices devolvieron cero. **No se aplicó ninguna migración durante esta validación.** El usuario ya había aplicado `sql/lab3-adrian-asistencia.sql` y creado el respaldo `eventosuy_test-pre-lab3.dump` fuera del repositorio; se verificó su existencia. No volver a ejecutar el preparador de base vacía sobre esta instalación.

Los scripts del repositorio son `logica/src/main/resources/db/migration/V20261116_01__visitas_evento.sql`, `sql/lab3-emmanuel-video.sql`, `sql/lab3-emmanuel-busqueda.sql`, `sql/lab3-adrian-asistencia.sql`, `logica/src/main/resources/db/seguimiento-usuario.sql` y `logica/src/main/resources/db/migration/V20261116_02__unicidad_ci.sql`. `sql/lab3-emmanuel-finalizacion.sql` repite la columna `evento.finalizado` que ya agrega el script de visitas. Si una futura inspección encuentra un faltante, hacer un respaldo nuevo **antes** de aplicar sólo el script necesario:

```powershell
$respaldo = Join-Path $env:USERPROFILE ('Desktop\eventosuy_test-' + (Get-Date -Format 'yyyyMMdd-HHmmss') + '.dump')
$env:PGPASSWORD = $env:EVENTOS_DB_PASSWORD
& 'C:\Program Files\PostgreSQL\17\bin\pg_dump.exe' -h localhost -p 5432 -U $env:EVENTOS_DB_USER -d eventosuy_test -Fc -f $respaldo
if ($LASTEXITCODE -ne 0) { throw 'Falló el respaldo: no migrar' }
& 'C:\Program Files\PostgreSQL\17\bin\pg_restore.exe' -l $respaldo | Select-Object -First 10
Remove-Item Env:PGPASSWORD
```

Después de confirmar manualmente el destino, el tipo de columna y la ausencia de conflictos, un script faltante se aplica con `psql -X -v ON_ERROR_STOP=1 -h localhost -p 5432 -U $env:EVENTOS_DB_USER -d eventosuy_test -f '<ruta-del-script>'`. Nunca usar otro nombre de base ni ejecutar todos los scripts a ciegas.

## 5. Resultado del cierre

Después del fast-forward de Valentina (`258658e`), las pruebas focalizadas móviles dieron **31 aprobadas**, las de SOAP HTTPS y patrocinios **53 aprobadas** y las persistentes de patrocinio/Portal HTTPS terminaron en `BUILD SUCCESS`, sin fallos ni errores (21/11/2026). Antes de las pruebas persistentes se confirmó por PostgreSQL `current_database()=eventosuy_test`, esquema `public` y la columna `registro.asistenciaconfirmada`; no se aplicaron migraciones.

La ejecución única `mvn -o '-Pcobertura' clean verify` sobre esta integración terminó en **BUILD SUCCESS**: 356 JUnit de `logica`, 18 de `servidor-central` y 197 de `webapp`; **571 aprobadas, 0 fallidas y 0 errores**. La regla JaCoCo de 80 % pasó. El informe de `logica` (`target/site/jacoco/index.html` y `jacoco.xml`) mide **2050 de 2405 líneas, 85,24 %**. `datos/CargarDatosPrueba` (82 líneas) y `datatypes/DtAltaTipoRegistro` (16) quedaron sin líneas ejecutadas; `ManejadorRegistros` conserva 41 líneas sin cubrir y `Sistema`, 29. JaCoCo incluye estas clases en el denominador.

El porcentaje de **IntelliJ Run with Coverage** todavía debe medirse en la defensa siguiendo el apartado 3; no sustituirlo por 85,24 % sin comprobarlo en el IDE.

## 6. Recorrido móvil para la defensa

1. Iniciar sesión como asistente en el teléfono y abrir una edición con video de YouTube. Comprobar que el reproductor se ajusta al ancho; en una edición sin video no debe aparecer un marco vacío.
2. Entrar a **Registrar asistencia** y elegir un registro propio de una edición aceptada que ya comenzó. Confirmar y mostrar el mensaje de éxito y la marca **Asistió** al regresar a la lista.
3. Volver al mismo registro: la confirmación no debe ofrecerse otra vez. En una edición futura, mostrar el aviso de que la asistencia se habilita cuando comienza. Sin sesión, comprobar que vuelve al login móvil.
4. Explicar que la Webapp invoca `confirmarAsistencia` de Central por SOAP; no escribe directamente en PostgreSQL. Mostrar `MovilVideoYAsistenciaTest` y el informe JaCoCo junto con **Run with Coverage** de IntelliJ.
