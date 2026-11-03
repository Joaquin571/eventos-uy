# H13: aplicación controlada de unicidad y coherencia de claves

## Alcance autorizado y ejecución del 03/11/2026

Se autorizó respaldar, inspeccionar y migrar **solo `eventosuy_test`**. No se
consultó ni modificó la base habitual `eventosuy`. La migración no borró, fusionó ni
normalizó filas preexistentes. Los cambios posteriores de datos fueron fixtures nuevos de
pruebas, con identificadores únicos, protegidos por H02.

La migración coincide con el contrato existente:

| Dato | Comparación Java / JPQL | PostgreSQL tras H13 |
|---|---|---|
| Nickname | Exacta | PK exacta, sin cambios |
| Correo | `LOWER` en validación, manejador y login | UNIQUE `lower(correoelectronico)` |
| Evento, edición, institución | Claves exactas | PK exactas, sin cambios |
| Nombre de tipo por edición | `LOWER(nombre)`, edición exacta | UNIQUE `(edicion_id, lower(nombre))` |
| Código de patrocinio | Exacta | PK exacta, sin cambios |
| Institución por edición patrocinada | Ahora exacta en las dos consultas corregidas | UNIQUE del par exacto, sin cambios |
| Asistente registrado por edición | Exacta | UNIQUE del par exacto, sin cambios |

`Usuario.correoElectronico` tiene `nullable=false, unique=true`; esa anotación
solo crea la unicidad exacta. `TipoRegistro` tiene UNIQUE de edición/nombre y sus
columnas son NOT NULL. Los índices funcionales complementan esas restricciones;
no las sustituyen ni renombraron columnas. El schema update de Hibernate no
garantiza crear estos índices funcionales en una base nueva: hay que migrarla.

### Inspección previa

* JDBC/H02 ejecutó `SELECT current_database()` antes de JPA y antes de cualquier
  escritura de prueba. Se confirmó `eventosuy_test` en cada ejecutor.
* PostgreSQL y utilidades cliente: **17.3**; aislamiento observado READ COMMITTED.
* Inspección de `pg_constraint`, `pg_index`, `pg_class`, `pg_namespace` e
  `information_schema.columns`. Los índices nuevos todavía no existían.
* Sin colisiones en `lower(correo)`, `(edicion,lower(nombreTipo))`, ni grupos por
  `lower` de instituciones, ediciones o pares patrocinados en ese momento.
* Las claves de instituciones/ediciones distinguen mayúsculas: no se reinterpretó
  esa política como unicidad insensible a mayúsculas para todas las entidades.

### Respaldo previo

Archivo fuera del repositorio y de target:

```text
C:\Users\Joaco\Desktop\PA_2026-respaldos\eventosuy_test-pre-H13-20261103-151843.dump
```

* Formato custom de pg_dump; tamaño **374894 bytes**.
* SHA-256: `CE464FD4633666556E469CD374604578EE2F2B466174549C949A0C9BE7E3E856`.
* `pg_restore --list` pudo leer el catálogo. También se leyó el archivo completo
  generando el SQL hacia `NUL`, sin conectar ni restaurar ninguna base.
* Se conservó el TOC junto al respaldo, con sufijo `.toc.txt`.
* Esto comprueba legibilidad/descompresión, no equivale a una restauración de
  ensayo. No se restauró encima de datos existentes.
* La contraseña se transmitió por PGPASSWORD temporal del proceso y se restituyó
  al terminar; no se incluyó en argumentos de comando, repositorio ni informe.

### Aplicación

Se utilizó psql `-X`, base explícita `-d eventosuy_test` y
`-v ON_ERROR_STOP=1`. En la misma sesión se estableció
`SET eventosuy.base_destino = 'eventosuy_test'` y se ejecutó `H13-unicidad-ci.sql`.

SHA-256 exacto del script ejecutado:
`9D4C9E18D3BC34CCBDDA4973B95CFF5A6EFED388B7F084643E1BB9B1DA74B567`.
Después se actualizó únicamente el comentario de cabecera para registrar que ya
se aplicó al entorno de pruebas; por eso el hash del archivo actual es diferente.

El resultado fue BEGIN, comprobaciones, LOCK TABLE, dos CREATE INDEX y COMMIT.
El script usa lock_timeout 5s y statement_timeout 60s. Bloquea escrituras sobre
usuario/tipo_registro mientras vuelve a comprobar conflictos y crea los índices.
No es una migración concurrente sin bloqueo y no debe ejecutarse sin ventana
controlada. Cualquier error aborta la transacción; no continuar tras un fallo.

### Verificación inmediata, antes de insertar fixtures

Ambos índices quedaron UNIQUE, `indisvalid=true` e `indisready=true`:

```sql
CREATE UNIQUE INDEX ux_usuario_correo_ci
    ON public.usuario (lower(correoelectronico));
CREATE UNIQUE INDEX ux_tipo_registro_edicion_nombre_ci
    ON public.tipo_registro (edicion_id, lower(nombre));
```

Los conteos y huellas agregadas de contenido de **las 12 tablas** coincidieron
antes y después del DDL. Las huellas se calcularon en transacciones JDBC de solo
lectura, sin imprimir valores de filas. No hubo pérdida ni transformación de
datos. Las secuencias pueden tener huecos por pruebas que hacen rollback; eso no
es pérdida de registros y no se reiniciaron secuencias.

## Defecto de búsqueda detectado y resolución autorizada

Se prepararon fixtures propios con dos ediciones de claves distintas que solo
difieren en mayúsculas. La segunda no tenía patrocinios. Antes de corregir,
`consultarPatrocinio(segunda, institucion)` devolvió el patrocinio de la primera.
La migración no causó ese defecto: estaba en el LOWER de las búsquedas antiguas.

Se informó y se detuvo la modificación de producción. El usuario autorizó
expresamente usar comparaciones exactas en:

* `ManejadorPatrocinios.obtenerPatrocinio(String,String)`.
* `ManejadorPatrocinios.existePatrocinioInstitucionEdicion(String,String)`.

Se eliminaron los LOWER solo de esos predicados. No se modificaron ISistema,
firmas públicas, claves, niveles, cupos, costos ni la política de correo/tipos.
Ahora cada búsqueda identifica el mismo par que la restricción UNIQUE. No se
devuelven patrocinios de otra clave y dos instituciones/ediciones con claves
distintas no se bloquean indebidamente entre sí.

## Pruebas de concurrencia y regresión

`UnicidadConcurrenciaTest` conserva sus 11 pruebas anteriores y agrega:

1. Dos altas por Sistema con correos equivalentes y nicknames distintos: un solo
   éxito; login por variante de correo recupera al ganador y se preserva el texto
   del correo almacenado.
2. Dos transacciones independientes leen cero usuarios equivalentes, sincronizan
   con barrera y persisten. El índice decide la carrera: un commit y un rechazo
   23505, sin usuario/subtipo huérfano.
3. La misma carrera para nombres de tipo equivalentes en una edición, sin usar
   el bloqueo del manejador: demuestra la garantía real del índice.
4. El mismo tipo equivalente sigue permitido en ediciones distintas.
5. Instituciones y ediciones conservan sus claves exactas y variantes distintas.
6. Consulta de patrocinio exacta conserva edición, institución y código.
7. Ediciones distintas por mayúsculas no mezclan consultas ni impiden sus altas.
8. Lo mismo para instituciones distintas por mayúsculas.
9. Una variante inexistente de clave no encuentra el patrocinio de otra entidad.
10. Patrocinios concurrentes de pares distintos confirman ambos y cada consulta
    devuelve su código correcto.

Las nuevas pruebas requieren los dos índices válidos. Si faltan, fallan con un
mensaje de preparación: **JUnit no ejecuta el script ni migra automáticamente**.
Todas las clases persistentes mantienen H02 y solo usan eventosuy_test.

### Resultado final verificado

* `mvn -Pcobertura clean install`, Java **25.0.5**, release25: **BUILD SUCCESS**.
* Lógica: **225** JUnit; web: **73** JUnit; sin fallos, errores u omisiones.
* `UnicidadConcurrenciaTest`: **21** pruebas aprobadas, incluidas las 10 nuevas.
* JavaScript: **56** pruebas aprobadas.
* Tomcat11: **29** verificaciones HTTP/JSP/PostgreSQL de patrocinios aprobadas,
  incluyendo privacidad por roles/estados con la lógica corregida.
* JaCoCo lógica: **1471/1756 líneas = 83,76993166287016 %**;
  **568/728 ramas = 78,02197802197803 %**.
* Los dos índices siguen únicos, válidos y listos después de la suite. Cero grupos
  duplicados de correo CI y de tipo CI por edición.
* `git diff --check` correcto. No se hicieron commits, merges ni push.

Hibernate emite `HHH000475` al no mapear la expresión funcional como una columna
durante su inspección del esquema. No eliminó ni invalidó los índices: su estado
real se verificó en pg_index después de la suite. Se conservan los avisos de pool
integrado, dialecto explícito y detección de fugas de Tomcat; no se cambiaron
dependencias para silenciarlos.

**H13 cerrado en el código y en el entorno eventosuy_test verificado.** La
aplicación a cualquier otra base permanece pendiente de autorización, respaldo,
auditoría y migración propios. Los resultados locales no certifican otro esquema.

## Repetir en eventosuy_test o preparar una base de pruebas nueva

No repetir el CREATE INDEX si los índices ya existen: primero verificar nombre,
tabla, expresión, unicidad y validez. No se usó IF NOT EXISTS porque podría
ocultar un índice homónimo con otra definición.

Para una base nueva de pruebas autorizada:

1. Instalar Java 25 y PostgreSQL; crear `eventosuy_test` vacía y configurar las
   variables EVENTOS_DB_* (nombre **eventosuy_test**).
2. `mvn install -DskipTests` compila también los auxiliares de prueba y crea el
   WAR/classpath; aún no corre las pruebas que requieren la migración.
3. Inicializar el esquema JPA sin datos, **dentro del mismo bloque y después del
   guard JDBC**. Por ejemplo desde la raíz con JShell del JDK25:

```powershell
$env:EVENTOS_DB_NAME = 'eventosuy_test'
$cp = 'logica/target/test-classes;logica/target/classes;webapp/target/webapp-1.0-SNAPSHOT/WEB-INF/lib/*'
jshell --class-path $cp
```

```java
{ soporte.ProteccionBasePruebas.verificar(); try { persistencia.BaseDeDatos.inicializar(); } finally { persistencia.BaseDeDatos.cerrar(); } }
/exit
```

No separar el guard y el arranque JPA en instrucciones que continúen aunque falle
el guard. No usar estos comandos para inicializar otra base por accidente.

4. Repetir auditoría de conflictos y generar respaldo custom con pg_dump,
   incluyendo verificación de legibilidad, hash y ubicación fuera del repositorio.
5. En la ventana autorizada, ejecutar **solo si faltan los índices**:

```text
psql -X --no-password -h HOST -p PUERTO -U USUARIO -d eventosuy_test -v ON_ERROR_STOP=1 -c "SET eventosuy.base_destino = 'eventosuy_test';" -f docs/migraciones/H13-unicidad-ci.sql
```

HOST, PUERTO y USUARIO son marcadores de la conexión verificada. No incluir la
contraseña en esa línea. Guardar la variable temporal de credenciales y restituirla.

6. Verificar índices y ausencia de cambios de filas. Ejecutar con Java25:
   `mvn -Pcobertura clean install` y las pruebas JavaScript. Revisar cobertura.

## Aplicación futura en otra base: no autorizada ni ejecutada ahora

Hace falta una autorización específica que identifique servidor, puerto y base,
responsable, ventana de mantenimiento y respaldo/restauración. El nombre no se
deduce de una variable ambigua. Confirmar current_database por JDBC/SQL en esa
conexión, revisar columnas y collations, buscar conflictos y registrar los índices
existentes. No publicar valores personales en logs.

Si hay colisiones, **detenerse**: este script no decide qué usuario o tipo
conservar. Resolverlas requiere una decisión separada, sin fusiones ni borrados
automáticos. Si no las hay, respaldar/verificar, fijar explícitamente la base
destinataria en la sesión autorizada y aplicar la transacción con ON_ERROR_STOP.
El guard H02 y las pruebas con escritura siguen siendo exclusivos de eventosuy_test:
no se los desactiva para correr la suite sobre la base habitual.

Después verificar definiciones/validez y las comprobaciones de negocio de solo
lectura autorizadas. No restaurar automáticamente un respaldo encima de una base
que pudo recibir escrituras posteriores. Una reversión requiere plan y aprobación
propios; no se restauró ni se borró nada en esta ejecución.

Los respaldos y TOC no se versionan. El código desplegado y su esquema deben
actualizarse de forma coordinada; la prueba de la base de tests no demuestra que
otra base ya tenga los índices.
