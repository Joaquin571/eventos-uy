# Instalación desde cero — Eventos UY Lab 3

Usar Windows x64 y PowerShell 5.1. Extraer el ZIP completo en `C:\entrega-lab3` en
ambas PC y abrir PowerShell en esa carpeta. No se necesitan Git, Maven, IntelliJ ni
archivos en `%USERPROFILE%\.eventosUy`. El JAR contiene Central, Swing, lógica y
persistencia; el WAR contiene servlets y clientes SOAP, sin controlador PostgreSQL.
La arquitectura es Webapp → SOAP/HTTPS → Central → PostgreSQL.

## Primera instalación

Seguir los pasos 1 a 4 de este manual una sola vez. En PC1 conservar
`PC1/config/central.properties`, `PC1/servidor.jar` y el certificado existente.
En PC2 conservar `PC2/config/web.properties`, el truststore y el `web.war`
instalado en Tomcat. Los comandos diarios no recrean la base, certificados ni
truststore y no descargan Tomcat.

Si Java 25 no se detecta automáticamente, guardar su ruta una sola vez en cada
PC (la ruta puede ser absoluta o relativa a la carpeta de esta entrega):

```powershell
.\INICIAR-PC1.ps1 -ConfigurarRutas -Java 'C:\Program Files\Eclipse Adoptium\jdk-25\bin\java.exe'
.\INICIAR-PC2.ps1 -ConfigurarRutas -Java 'C:\Program Files\Eclipse Adoptium\jdk-25\bin\java.exe'
```

En PC2, la ruta predeterminada es
`%LOCALAPPDATA%\EventosUyTools\apache-tomcat-11.0.29`. Si Tomcat está en otra
ubicación, guardarla una vez con
`.\INICIAR-PC2.ps1 -ConfigurarRutas -Tomcat 'D:\Tomcat\apache-tomcat-11.0.29'`.
Las rutas quedan en `PC1/config/arranque.json` y `PC2/config/arranque.json`;
estos archivos no contienen contraseñas. La clave del truststore se solicita
de forma interactiva en cada arranque de PC2. No ponerla en una línea de
comandos ni en el manual. En PC1, Central lee las claves de su configuración
privada ya existente; restringir el acceso a ese archivo al usuario de PC1.

## Arranque diario

Con PostgreSQL 17 disponible en PC1, ejecutar desde cualquier directorio:

```powershell
& 'C:\entrega-lab3\INICIAR-PC1.ps1'
```

En PC2, ejecutar desde cualquier directorio:

```powershell
& 'C:\entrega-lab3\INICIAR-PC2.ps1'
```

Cambiar `C:\entrega-lab3` por la carpeta real. Cada script comprueba rutas,
versiones y puertos antes de iniciar el proceso; `-SoloVerificar` hace esas
comprobaciones sin iniciar nada. Mantener abiertas las consolas. PC2 establece
`CATALINA_HOME` y `CATALINA_BASE` sólo para el proceso de Tomcat y mantiene la
validación TLS de Java con el truststore PKCS12 existente.

## 1. Instalar requisitos

Ejecutar en cada PC sólo el bloque correspondiente:

```powershell
# PC1
.\Instalar-Requisitos.ps1 -Rol Central -SoloPlan
.\Instalar-Requisitos.ps1 -Rol Central
.\Verificar-Requisitos.ps1 -Rol Central

# PC2
.\Instalar-Requisitos.ps1 -Rol Webapp -SoloPlan
.\Instalar-Requisitos.ps1 -Rol Webapp
.\Verificar-Requisitos.ps1 -Rol Webapp
```

El instalador muestra cambios y pide escribir `INSTALAR`. Instala sólo lo faltante:
Temurin JDK 25 y PostgreSQL 17 en PC1; Temurin JDK 25 y Tomcat 11.0.29 en PC2.
Usa WinGet para Java/PostgreSQL y el ZIP oficial de Apache con SHA-512 fijado para
Tomcat. PostgreSQL abre un instalador **interactivo** para contraseña, puerto y ruta.
WinGet/App Installer, Internet, permisos de instalación y reinicio pendiente pueden
requerir intervención. Abrir otra consola tras instalar Java. No instala Maven,
PostgreSQL en PC2 ni Tomcat en PC1. Si ya existe software en rutas no habituales,
indicar `-JavaRuta` (java.exe), `-PostgresRuta` (psql.exe) o `-TomcatRuta` (raíz de
Tomcat) a los scripts de requisitos para evitar duplicados.

## 2. PC1: base nueva

Estas órdenes suponen puerto 5432 y la ruta PostgreSQL predeterminada. `psql` pide
la contraseña elegida durante la instalación:

```powershell
$pg = Join-Path $env:ProgramFiles 'PostgreSQL\17\bin'
& "$pg\psql.exe" -X -h 127.0.0.1 -p 5432 -U postgres -d postgres
```

Dentro de `psql`, ejecutar sólo para un rol y una base **nuevos**:

```text
CREATE ROLE eventosuy_demo LOGIN;
\password eventosuy_demo
CREATE DATABASE eventosuy_defensa_demo OWNER eventosuy_demo;
\q
```

De vuelta en PowerShell:

```powershell
.\base-datos\Preparar-Base.ps1 -UsuarioDb eventosuy_demo
.\base-datos\Cargar-Demo.ps1 -UsuarioDb eventosuy_demo
```

El preparador exige base vacía con sufijo `_demo`, crea el esquema y aplica los SQL
incluidos. El cargador agrega un conjunto de demostración **cada vez** que se
ejecuta; usarlo una sola vez. Ambos piden la clave PostgreSQL; el cargador pide
además una clave nueva para las cuentas demo. Guardar los usuarios que informa en
un lugar privado. PostgreSQL sólo necesita escuchar en `127.0.0.1:5432`; PC2 no
debe tener acceso al puerto 5432. Para otra carpeta usar `-PostgresBin` en ambos
scripts; para otro puerto usar `-PuertoDb` y ajustar `Preparar-Central.ps1`.

## 3. PC1: TLS y Central

Elegir la IP/DNS por la que PC2 llega a PC1. Para la red Tailscale comprobada:

```powershell
.\PC1\certificados\Preparar-Certificado.ps1 -HostCentral 100.115.156.116
.\PC1\Preparar-Central.ps1 -HostCentral 100.115.156.116 -DbUsuario eventosuy_demo
.\INICIAR-PC1.ps1
```

Para agregar una IP LAN al certificado desde el principio, usar
`-OtrosSan 'IP:192.168.1.10'` en el primer comando. El script genera
`PC1/certificados/central.p12` **privado** y `central-publico.cer` **público**;
anotar el SHA-256 que muestra. El segundo pide contraseñas TLS y PostgreSQL y crea
`PC1/config/central.properties`. Conserva la configuración existente; `-Actualizar`
la respalda antes de reemplazarla. `Iniciar-Central.ps1` detecta Java 25 y pasa
`-Deventos.central.config` con la ruta absoluta. Mantener la consola abierta;
Ctrl+C detiene Central. Swing requiere una sesión de escritorio. `-Escucha` permite
elegir otra interfaz para Central. Permitir TCP 9128 en el firewall de PC1 para la
red prevista.

```powershell
.\PC1\Verificar-Central.ps1 -HostCentral 100.115.156.116 -CertificadoPublico .\PC1\certificados\central-publico.cer
```

Transferir a PC2 **sólo** `central-publico.cer` y su SHA-256 por un medio confiable.
No transferir `central.p12` ni `central.properties`.

## 4. PC2: confianza HTTPS y Webapp

Copiar el certificado recibido a
`C:\entrega-lab3\PC2\certificados\central-publico.cer`. Sustituir `HASH_PC1` por
el SHA-256 mostrado en PC1:

```powershell
.\PC2\certificados\Preparar-Confianza.ps1 -Sha256Esperado 'HASH_PC1'
.\PC2\Preparar-Web.ps1 -HostCentral 100.115.156.116
.\PC2\Verificar-JavaTls.ps1 -HostCentral 100.115.156.116
.\PC2\Instalar-Web.ps1
.\INICIAR-PC2.ps1
```

El primer script verifica el hash, pide contraseña y crea
`PC2/certificados/tomcat-truststore.p12`. Evitar comillas, `%` y `!` en esa
contraseña por `catalina.bat`. `Preparar-Web.ps1` crea
`PC2/config/web.properties`; si ya existe con otros valores se detiene, y
`-Actualizar` guarda respaldo antes de reemplazar. `Instalar-Web.ps1` copia
`web.war` al contexto `/web` y no reemplaza otro WAR sin `-Reemplazar`.
`Iniciar-Web.ps1` detecta Tomcat 11 y Java 25, comprueba el truststore y pasa a
la JVM `-Deventos.web.config` apuntando a `PC2/config/web.properties`, además de
las propiedades del truststore PKCS12. Mantener esa consola abierta. Para rutas
no predeterminadas usar `-Tomcat`, `-JavaHome`, `-WebConfig` y `-Truststore`.

```powershell
.\PC2\Verificar-Web.ps1 -HostCentral 100.115.156.116 -CertificadoPublico .\PC2\certificados\central-publico.cer
```

Abrir `http://IP_PC2:8080/web/eventos` y `http://IP_PC2:8080/web/movil`.
Permitir TCP 8080 en el firewall de PC2 para los navegadores. Si Tomcat se
ejecuta como servicio Windows, el script de consola no configura ese servicio:
definir en la JVM del servicio `eventos.web.config`, `javax.net.ssl.trustStore`,
`javax.net.ssl.trustStoreType=PKCS12` y `javax.net.ssl.trustStorePassword`, con
permisos de lectura para la cuenta del servicio. Esta entrega automatiza el modo
`catalina.bat run`.

## 5. Red, diagnóstico y límites

Para pasar de LAN a Tailscale sin recompilar, cambiar `central.host` y si corresponde
`central.escucha` en `PC1/config/central.properties`, y `central.host` en
`PC2/config/web.properties`. El certificado debe tener el IP/DNS exacto en SAN.
Si falta, emitir otro certificado y truststore; los scripts TLS no sobrescriben
archivos existentes. Reiniciar Central y Tomcat, luego verificar TCP y Java TLS.

Para un HTTP 503 seguir [DIAGNOSTICO-PC2.md](DIAGNOSTICO-PC2.md).
`Test-NetConnection` sólo prueba TCP; `curl -k` omite la validación TLS.
`Verificar-JavaTls.ps1` prueba la JVM y el truststore real. El nuevo WAR registra
`Fallo SOAP Portal` con clase, mensaje y causa raíz saneados en los logs de Tomcat.

No reenviar la copia **después** de configurarla: contendrá claves en
`central.properties` y almacenes TLS. Usar el ZIP original para distribuir.
La validación de los nuevos comandos de arranque se hace con `-SoloVerificar`
antes de abrir otra instancia. Ejecutar el arranque real sólo cuando la instancia
anterior se haya cerrado normalmente.

## Datos de defensa para la base activa

La carga adicional está preparada para `eventosuy_demo` y **no se ejecuta durante
la instalación ni durante el arranque diario**. Usa exclusivamente las altas de
`ISistema`. Conserva los registros anteriores y compara nombres naturales antes
de cada alta. Una segunda ejecución completa no añade duplicados. Ante un error
intermedio, conservar el respaldo y revisar el resultado antes de repetir.

Se prepararon 16 eventos, 16 ediciones futuras (del 12/02/2027 al 13/11/2027),
16 tipos de entrada General, 10 categorías, 4 organizadores, 6 asistentes,
2 instituciones, 2 patrocinios y 12 registros. Catorce ediciones quedan aceptadas,
una ingresada y una rechazada. No hay entidad de solicitud de patrocinio en este
modelo. Los nombres de los eventos están en `base-datos/CargarDatosDefensa.java`.
Las ciudades incluyen Montevideo, San José de Mayo, Punta del Este, Canelones y
Colonia del Sacramento. Los precios van de $0 a $1500.
Cada evento y edición nuevos incluye una banda PNG creada localmente al cargar,
guardada como bytes mediante el mecanismo JPA de imágenes. La validación comprueba
formato y tamaño antes de cargar. No se usan enlaces externos para las imágenes.

Los escenarios centrales son Festival de Música de Montevideo y Congreso de
Tecnología e Innovación (patrocinios); Feria del Libro de Montevideo y Feria de
Artesanos de Canelones (entrada gratuita); Encuentro de Videojuegos y Tecnología
(edición ingresada); Congreso de Innovación Educativa (edición rechazada).
Los demás eventos cubren gastronomía, maratón, emprendimientos, cine, desarrollo
de software, ciberseguridad, fútbol, inteligencia artificial, turismo y jazz.

Los organizadores ficticios son `cultura_sur`, `encuentros_uy`, `innovacion_uy`
y `deporte_uy`. Los asistentes son `ana_pereira`, `lucas_fernandez`,
`sofia_rodriguez`, `martin_silva`, `valeria_gomez` y `diego_martinez`. Todos usan
correos `@example.com` y la clave nueva que se solicita al momento de cargar.
El administrador se usa en Swing local y no tiene una cuenta web. Las
instituciones ficticias son Cooperativa Horizonte y Fundación Río de la Plata;
patrocinan el congreso tecnológico y el festival de música, respectivamente.

### Validar, respaldar y cargar en PC1

Copiar los archivos nuevos de `base-datos` a la misma carpeta de la entrega en
PC1. Con Java 25, PostgreSQL 17 y la configuración privada de Central ya
instalados, ejecutar desde la raíz de la entrega:

```powershell
.\base-datos\Cargar-Datos-Defensa.ps1
```

Ese comando comprueba versiones, conexión, nombre de la base, esquema JPA y
coherencia de las fichas sin escribir datos. **Solo después de autorizar la carga
real**, ejecutar:

```powershell
.\base-datos\Cargar-Datos-Defensa.ps1 -Cargar
```

Antes de cualquier alta, el script crea un respaldo PostgreSQL 17 de formato
personalizado en `base-datos/respaldos/eventosuy_demo_FECHA.dump`, verifica que
tenga datos de tablas con `pg_restore -l` y muestra su SHA-256. La carpeta y los
archivos `.dump` son privados y no se incluyen en Git. El script toma usuario,
contraseña, host y puerto de `PC1/config/central.properties`, sin imprimir
contraseñas. Solicita la clave de las cuentas ficticias de manera interactiva.
No cambia la configuración, el esquema, certificados ni servicios.

### Reanudación tras una carga parcial

En las dos ejecuciones fallidas iniciales quedaron cargados los 4 organizadores,
6 asistentes, 10 categorías, 16 eventos, 16 ediciones, 16 entradas General y
2 instituciones. No quedaron patrocinios ni inscripciones. La causa fue el
primer patrocinio: 4 entradas de $1500 sumaban $6000 y superaban el 20% del
aporte de $25000 ($5000). El cargador corregido prepara 3 entradas: $4500.
El segundo patrocinio prepara 3 entradas de $850: $2550 frente a un máximo de
$4000. La validación previa comprueba ambos importes y las relaciones antes de
crear otro respaldo. También compara los registros existentes con las fichas;
si encuentra una clave ocupada con datos incompatibles, se detiene sin escribir.

Para reanudar, sustituir `Cargar-Datos-Defensa.ps1`, `CargarDatosDefensa.java`
y el manifiesto SHA-256 por las versiones del commit de corrección. Ejecutar
primero `Cargar-Datos-Defensa.ps1` sin parámetros. Una vez autorizado, ejecutar
`-Cargar`; omitirá las entidades ya compatibles y añadirá los 2 patrocinios y
12 inscripciones faltantes. La contraseña solicitada se usa solo al crear
usuarios faltantes: las claves de los 10 usuarios ya creados no se cambian.
Después ejecutar `-Verificar`.

Cada caso de uso de `ISistema` abre y confirma su propia transacción JPA. Por
eso la carga completa no puede envolverse en una transacción única desde este
script sin cambiar los manejadores de la aplicación. La validación previa,
el respaldo obligatorio y la reanudación idempotente reducen el riesgo de un
estado parcial. No se cambió la lógica transaccional del Central.

### Verificar resultados y consultar desde PC2

En PC1, ejecutar:

```powershell
.\base-datos\Cargar-Datos-Defensa.ps1 -Verificar
```

Comprobar el listado de 16 eventos y sus ediciones y tipos de entrada. Desde PC2
abrir `http://100.69.184.0:8080/web/eventos`; buscar por “Música”, “Tecnología”
y “Canelones”, filtrar por categoría y abrir los detalles. Iniciar sesión con
un asistente ficticio y comprobar sus registros y otras ediciones aceptadas.
Un organizador puede consultar sus ediciones; la edición de videojuegos muestra
el estado ingresada y el congreso educativo, rechazada, en las vistas autorizadas.
Los patrocinios permiten demostrar la consulta correspondiente. El flujo web
continúa por SOAP HTTPS hacia PC1.

### Restauración si se autoriza

La restauración sustituye datos de la base y requiere detener las escrituras de
la aplicación y una autorización separada. Conservar el archivo `.dump` y su
SHA-256. Verificarlo con `Get-FileHash` y `pg_restore -l`; crear una base
**nueva** de recuperación, y restaurar allí primero para inspeccionar los datos:

```powershell
$pg = Join-Path $env:ProgramFiles 'PostgreSQL\17\bin'
& "$pg\pg_restore.exe" -l 'RUTA_DEL_RESPALDO.dump'
& "$pg\createdb.exe" -h 127.0.0.1 -p 5432 -U postgres eventosuy_recuperacion
& "$pg\pg_restore.exe" -h 127.0.0.1 -p 5432 -U postgres -d eventosuy_recuperacion --no-owner --no-privileges 'RUTA_DEL_RESPALDO.dump'
```

PostgreSQL solicitará la clave del usuario de restauración. Comprobar la base
recuperada antes de decidir un cambio de servicio. No restaurar directamente
sobre `eventosuy_demo` mientras Central está en servicio.

### Commits

Los cambios de arranque y despliegue se registran con
`chore(infra): estabilizar arranque distribuido PC1 y PC2`; esta carga y el
presente apartado con `feat(seed): preparar datos realistas para defensa Lab 3`.
Consultar los hashes definitivos con `git log -2 --oneline`. No hacer push antes
de la defensa sin decidirlo expresamente.
