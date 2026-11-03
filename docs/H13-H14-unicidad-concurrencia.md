# Auditoría H13 y H14 — 3 de noviembre de 2026

**Actualización posterior:** H13 se aplicó en `eventosuy_test` con respaldo previo.
Se crearon los dos índices funcionales y, tras informar el defecto y recibir
autorización, se corrigieron las dos búsquedas de patrocinio a claves exactas.
El [registro y procedimiento](migraciones/H13-procedimiento.md) contiene el alcance,
respaldo, verificación de filas y nuevas pruebas. No se migró ninguna otra base.
Las tablas y pendientes siguientes conservan la **auditoría inicial anterior a
la migración**: sus puntos 1–3 no describen el estado posterior verificado.

Base Git: `dev`, `0284e7cc1240fc7e7f08d5454d70aea0bc56301b`, con las
correcciones anteriores sin commit. No se modificó `ISistema`.

## Alcance de la inspección de PostgreSQL

Antes de JPA, H02 comprobó `SELECT current_database() = 'eventosuy_test'`.
La auditoría JDBC fue de solo lectura: `pg_constraint`, `pg_indexes`, `information_schema.columns`
y consultas agrupadas de conflictos. Se observaron columnas `varchar` y los
siguientes índices únicos efectivos. No se inspeccionó la base habitual
`eventosuy`; estos resultados no certifican su esquema ni sus datos.

| Dato | Comprobación Java | Restricción efectiva en eventosuy_test | Diagnóstico |
|---|---|---|---|
| Nickname | Igualdad exacta en `Sistema.validarDatosUsuario` / `ManejadorUsuarios` | `usuario_pkey(nickname)` | No duplica el mismo valor incluso con dos transacciones. Variantes de mayúsculas son claves distintas. |
| Correo | `LOWER` en `existeCorreo` y `addUsuario` | UNIQUE de `correoelectronico`, sin `lower` | Diferencia real: correos que solo difieren en mayúsculas pueden pasar dos comprobaciones simultáneas. Migración propuesta, no ejecutada. |
| Evento | Igualdad exacta en `existeEvento` / `addEvento` | `evento_pkey(nombre)` | Mismo nombre protegido por PostgreSQL. No se cambió la sensibilidad a mayúsculas. |
| Edición | Igualdad exacta global en `existeEdicion` / `addEdicion` | `edicion_pkey(idnombre)` | Nombre único global, no por evento. Variantes de mayúsculas son claves distintas. |
| Institución | Igualdad exacta en `existeInstitucion` / `addInstitucion` | `institucion_pkey(nombre)` | Misma clave protegida. Variantes de mayúsculas siguen siendo instituciones distintas. |
| Categoría | Igualdad exacta en `obtenerCategoria` / `addCategoria` | `categoria_pkey(nombre)` | Sin cambios de semántica. |
| Tipo de registro | `LOWER(nombre)` por edición | `uk_tipo_registro_edicion_nombre(edicion_id,nombre)` | Java no distingue mayúsculas, índice sí. Se bloquea la edición antes del COUNT en `addTipoRegistro`; falta índice funcional para proteger también escrituras externas. |
| Código de patrocinio | Igualdad exacta en `existePatrocinio` | `patrocinio_pkey(codigopatrocinio)` | Código global; no se cambió la sensibilidad a mayúsculas. |
| Institución patrocinadora por edición | `LOWER` de ambas claves en `existePatrocinioInstitucionEdicion` | `uk_patrocinio_institucion_edicion(institucion_id,edicion_id)` | El par exacto está protegido. Hay inconsistencia con entidades distintas cuyos nombres solo difieren en mayúsculas; requiere decidir semántica antes de cambiarlo. |
| Asistente por edición | COUNT exacto | `uk_registro_asistente_edicion(asistente_nickname,edicion_id)` | Se bloquea el asistente antes de comprobar duplicados, también si las solicitudes eligen tipos distintos. UNIQUE sigue siendo la última defensa. |

Antes de las nuevas pruebas, los agrupamientos encontraron **cero grupos** de
colisión por `lower` para correo, nickname, evento, edición, institución, código,
tipos por edición y pares patrocinados. Son resultados exclusivos de la base de
pruebas y de ese momento; una migración debe volver a comprobar la base destino.

La consigna exige unicidad, pero no se toma eso como autorización para convertir
todas las claves a minúsculas. Se conserva el comportamiento existente.
“Patrocinador” corresponde aquí a la institución en un patrocinio; no existe una
entidad independiente `Patrocinador`.

## Correcciones sin migración

* `ManejadorEventos.addTipoRegistro`: bloqueo pesimista de la edición dentro de
  la transacción, **antes** de comprobar el nombre; el segundo COUNT ve el commit
  anterior con el aislamiento `READ_COMMITTED` comprobado en esta instalación.
* `ManejadorRegistros.registrar`: bloqueo del asistente antes de los del tipo y
  patrocinio. Evita que el mismo asistente llegue simultáneamente por tipos
  distintos. Se conservan los bloqueos existentes del tipo para cupo y del
  patrocinio para usos, así como el cálculo de costos.
* `ConflictosPersistencia`: después del rollback se traduce únicamente SQLSTATE
  `23505` a `IllegalArgumentException` con mensaje español. Otros errores, como
  `22001`, se conservan. Aplicado a las altas de los manejadores y registros.
* `Fabrica` y los cinco manejadores con inicialización insegura: `getInstance`
  sincronizado, conservando creación diferida. `ManejadorImagenes` ya era seguro.
* `BaseDeDatos`: se sincronizan inicialización, obtención de EntityManager y
  cierre sobre el mismo monitor. Se conserva apertura diferida, reapertura tras
  cierre y un EntityManager nuevo por operación. El monitor no abarca la
  transacción ni comparte un EntityManager entre hilos.
* `Fabrica.getISistema` continúa devolviendo fachadas independientes; no se
  convierte `Sistema` en singleton. Swing y Tomcat siguen teniendo instancias
  propias y coordinándose mediante PostgreSQL, no mediante un monitor Java.

## Migración pendiente y decisiones

Se autorizó **preparar**, no ejecutar,
[`migraciones/H13-unicidad-ci.sql`](migraciones/H13-unicidad-ci.sql).
Propone únicamente índices únicos funcionales para correo y tipos por edición.
No cambia valores ni claves, no fusiona usuarios/tipos ni elimina registros.
Incluye confirmación explícita de la base, bloqueo de escrituras, comprobación de
conflictos y transacción. La ventana de mantenimiento y resolución manual de
conflictos deben autorizarse antes de aplicarlo. No lo ejecutan Maven ni H02.
No se modificaron anotaciones JPA para intentar generar índices automáticamente.

Quedan pendientes:

1. Hasta aplicar la migración, dos altas simultáneas con correos que solo difieran
   en mayúsculas pueden persistir ambas. Un `synchronized` Java no resolvería el
   problema entre Swing y Tomcat en JVM distintas.
2. El bloqueo de la edición protege tipos a través del manejador, pero no SQL
   externo ni programas que omitan ese protocolo. El índice propuesto completa
   esa garantía.
3. `obtenerPatrocinio(edicion,institucion)` y su comprobación de existencia usan
   `LOWER` aunque esas entidades tienen claves exactas. Con pares de nombres que
   solo difieren en mayúsculas la consulta puede ser ambigua. Hay que decidir si
   corregir búsquedas a igualdad exacta o adoptar unicidad insensible a mayúsculas
   también para esas entidades; no se cambió silenciosamente.
4. `addRegistro` es una API pública interna antigua, sin llamadas en el proyecto:
   no equivale al caso de uso `registrar` y no garantiza cupos/uso de códigos.
   Los clientes deben continuar usando exclusivamente `ISistema`. Restringir o
   redirigir esa API requiere revisar compatibilidad, fuera de estos cambios.
5. Los bloqueos agregados serializan altas de tipos de una edición y registros
   de un mismo asistente. No se introdujo un bloqueo global de operaciones.
   Hibernate puede emitir `HHH000444` por bloqueo posterior a la consulta en
   relaciones con herencia; las comprobaciones críticas ocurren tras el bloqueo.
6. Las restricciones de pares tienen columnas JPA opcionales en algunos modelos.
   PostgreSQL permite varios NULL en UNIQUE; `Sistema` rechaza entradas sin
   referencias obligatorias. No se presenta esa restricción como garantía contra
   toda escritura SQL externa inválida.

## Pruebas

`UnicidadConcurrenciaTest`: altas concurrentes de nickname, correo exacto, evento,
edición, institución, tipos con variantes de mayúsculas, pares de patrocinio y
códigos, registros de un asistente entre tipos, último cupo y último uso gratuito.
Las pruebas de bloqueos observan dos transacciones esperando mediante
`pg_blocking_pids`, liberan la primera transacción y verifican una sola operación
confirmada y el estado persistido. No se usa un sleep como evidencia del bloqueo.

`SingletonConcurrenteTest`: 16 hilos sobre el primer acceso de cada singleton
aislado en su propio ClassLoader, sin reiniciar campos estáticos de producción.
Comprueba identidad y que las fachadas sigan siendo independientes.
`PersistenciaConcurrenteTest`: ocho EntityManagers distintos con la misma fábrica.
`ConflictosPersistenciaTest`: traducción de unicidad, conservación de errores
técnicos distintos y de errores de negocio.

Estas pruebas no demuestran ausencia de todos los interbloqueos posibles ni
sustituyen una prueba de carga. Las de persistencia heredan H02 y solo crean datos
nuevos con nombres únicos en `eventosuy_test`.

### Resultado final verificado

* JDK Temurin **25.0.5**, compilación `--release 25`.
* `mvn -Pcobertura clean install`: **BUILD SUCCESS**, los tres módulos.
* Lógica: **215** pruebas, cero fallos, errores u omisiones.
* Web: **73** pruebas, cero fallos, errores u omisiones.
* JavaScript: **15** pruebas aprobadas.
* JaCoCo lógica: **1469/1756 líneas = 83,65603644646924 %**;
  **567/728 ramas = 77,88461538461539 %**. No se excluyeron clases.
* `git diff --check`: correcto; sin commits, merges ni push.
* Informe HTML: `logica/target/site/jacoco/index.html`.
* Script de migración preparado y **no ejecutado**. No se modificaron índices
  ni datos preexistentes; las escrituras de pruebas fueron datos nuevos propios.

Las primeras pruebas de espera fallaron porque el observador contaba solo
bloqueadores directos: PostgreSQL mostraba una cadena transacción inicial →
primer solicitante → segundo solicitante. Se corrigió la prueba para recorrer
recursivamente `pg_blocking_pids`; no se alteró producción para hacerla pasar.
Los mensajes SQLSTATE `23505` durante las carreras de prueba son rechazos
esperados y comprobados, no fallos de la suite. Se mantienen los avisos de
Hibernate por su pool integrado, dialecto explícito y bloqueo en consulta
posterior (`HHH000444`); no se cambiaron dependencias para silenciarlos.
