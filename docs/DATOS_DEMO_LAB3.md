# Datos reproducibles de Laboratorio 3

`datos.CargarDatosLab3` agrega un conjunto con prefijo nuevo; no borra ni actualiza conjuntos existentes. Usa los controladores reales, JPA y la configuración PostgreSQL del proceso. Rechaza una repetición de prefijo antes de dar de alta usuarios y exige que el destino del ejecutable termine en `_demo` o `_test`. Se recomienda una base nueva `eventosuy_demo`, separada de desarrollo y de JUnit.

Cada ejecución crea un organizador y dos asistentes. El administrador utiliza Swing, sin cuenta web. Organizador y primer asistente se siguen mutuamente. Hay evento activo y finalizado con distintos contadores, ediciones con/sin video, una archivada, otra aceptada y terminada archivable, una ingresada, una rechazada y registros con/sin asistencia. Las fechas se calculan desde el día de carga; los nombres y descripciones permiten demostrar búsqueda y órdenes. Cada edición tiene tipo General. El cargador anterior sigue disponible para sus instituciones/patrocinios; este conjunto adicional no modifica esos datos.

## Preparación y carga

Después de compilar con Java 25 y `mvn -DskipTests clean package`, crear una base exclusiva y verificar su identidad. No usar la base de defensa existente sin respaldo. Adaptar rutas y datos a la instalación:

```powershell
$env:EVENTOS_DB_HOST = 'localhost'
$env:EVENTOS_DB_PORT = '5432'
$env:EVENTOS_DB_NAME = 'eventosuy_demo'
$credencialDb = Get-Credential -Message 'Usuario PostgreSQL autorizado para la base demo'
$env:EVENTOS_DB_USER = $credencialDb.UserName
$env:EVENTOS_DB_PASSWORD = $credencialDb.GetNetworkCredential().Password
$env:PGPASSWORD = $env:EVENTOS_DB_PASSWORD
& 'C:\Program Files\PostgreSQL\17\bin\createdb.exe' -h $env:EVENTOS_DB_HOST -p $env:EVENTOS_DB_PORT -U $env:EVENTOS_DB_USER eventosuy_demo
& 'C:\Program Files\PostgreSQL\17\bin\psql.exe' -X -v ON_ERROR_STOP=1 -h $env:EVENTOS_DB_HOST -p $env:EVENTOS_DB_PORT -U $env:EVENTOS_DB_USER -d eventosuy_demo -c 'SELECT current_database(), current_schema();'
```

Si `createdb` falla porque la base ya existe, **no recrearla ni borrarla**: inspeccionarla y respaldarla. El preparador siguiente exige esquema vacío y rechaza uno ya poblado:

```powershell
java -cp servidor-central/target/servidor.jar persistencia.PrepararEsquemaNuevo --base-vacia
if ($LASTEXITCODE -ne 0) { throw 'No continuar: revisar preparación de esquema' }
& 'C:\Program Files\PostgreSQL\17\bin\psql.exe' -X -v ON_ERROR_STOP=1 -h $env:EVENTOS_DB_HOST -p $env:EVENTOS_DB_PORT -U $env:EVENTOS_DB_USER -d eventosuy_demo -f logica/src/main/resources/db/migration/V20261116_02__unicidad_ci.sql
if ($LASTEXITCODE -ne 0) { throw 'No continuar: revisar índices' }
$credencialDemo = Get-Credential -UserName 'demo' -Message 'Elegí la contraseña de los usuarios de demostración'
$env:EVENTOS_DEMO_PASSWORD = $credencialDemo.GetNetworkCredential().Password
try {
    java -cp servidor-central/target/servidor.jar datos.CargarDatosLab3 --agregar-datos-demo
    if ($LASTEXITCODE -ne 0) { throw 'La carga no terminó: revisar el último error; no borrar datos' }
} finally {
    Remove-Item Env:EVENTOS_DEMO_PASSWORD,Env:PGPASSWORD,Env:EVENTOS_DB_PASSWORD -ErrorAction SilentlyContinue
}
```

El programa imprime los nicknames y el prefijo, nunca la contraseña. Para login por correo usar `<nickname>@demo.example`. Guardar el prefijo para filtrar las consultas y elegir los datos de esa ejecución. Se puede repetir la carga: genera otro prefijo, sin reemplazar la anterior. Si se interrumpe una carga, puede quedar un conjunto parcial; corregir el problema y crear otro conjunto, sin borrar datos a ciegas.

Central usa `db.*` de `~/.eventosUy/central.properties`, no estas variables. Para defender con esta base, ajustar allí `db.url` y las credenciales privadas al mismo destino. La Webapp no recibe credenciales JDBC.

`DatosLab3Test` comprueba el conjunto contra PostgreSQL real, rechazo de prefijo repetido, fechas/estados, relaciones, video, registros, visitas y persistencia tras reinicializar JPA.
