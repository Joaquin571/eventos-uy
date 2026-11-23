# Verificaciones realizadas al empaquetar

## Actualización de entrega limpia

- `PC1/config/central.properties` y `PC2/config/web.properties` se crean desde
  plantillas del paquete; `Iniciar-Central.ps1` y `Iniciar-Web.ps1` pasan las rutas
  absolutas mediante propiedades JVM. No requieren `.eventosUy`.
- Cambio mínimo en `central.Main` para leer `-Deventos.central.config`. Maven offline
  focalizado: `mvn -o -pl servidor-central -am '-Dtest=MainConfiguracionTest'
  '-Dsurefire.failIfNoSpecifiedTests=false' package` terminó con `BUILD SUCCESS`;
  1 prueba, 0 fallas. `PC1/servidor.jar` coincide por SHA-256 con el JAR generado.
- `PC2/web.war` conserva el diagnóstico SOAP/TLS ya compilado y coincide por
  SHA-256 con `webapp/target/web.war`. No se repitió la suite Webapp.
- El ensayo real de instaladores, certificado entre dos PC y funcionamiento de
  Central/Webapp sigue pendiente.

- Origen: `424aae3b83e1d15481fb71bae17a59bdadfd12d3`.
- Compilación previa: `mvn -o -DskipTests package`, `BUILD SUCCESS` en los seis
  proyectos del reactor. No se repitieron suites completas aprobadas para el commit.
- El JAR Shade contiene `central.Main`, `swing.principal`, el preparador de esquema,
  el cargador demo, `META-INF/persistence.xml` y `org.postgresql.Driver`.
- Desde un directorio temporal ajeno al repositorio, `java -cp servidor.jar
  persistencia.PrepararEsquemaNuevo` cargó la clase y rechazó la invocación sin
  `--base-vacia` como corresponde. `java -jar servidor.jar --sin-swing` cargó
  `central.Main` y rechazó la falta de configuración privada. Esto comprueba
  resolución de clases y manifiesto fuera del proyecto; el arranque operativo
  requiere PostgreSQL, TLS y configuración reales.
- El WAR se generó correctamente. Contiene `contratos` y `jaxws-rt`, y no contiene
  `logica`, `servidor-central`, `hibernate-core` ni `postgresql` en `WEB-INF/lib`.
  `ContextoListener` sólo crea clientes SOAP y lee `web.properties`.
- La sintaxis de todos los scripts PowerShell fue aceptada por el parser. Los
  scripts de arranque, SQL y conectividad no se ejecutaron contra una instalación
  real durante el empaquetado.
- La detección de requisitos encontró Java 25.0.5 y PostgreSQL 17.3 para `Central`,
  y Java 25.0.5 y Tomcat 11.0.27 para `Webapp` cuando se indicó la ruta local de
  Tomcat. `Instalar-Requisitos.ps1 -SoloPlan` no realizó cambios en ambos roles.
  El plan Webapp sin ruta explícita propuso Tomcat 11.0.29 y ningún otro paquete.
- En el equipo de preparación WinGet no está disponible; no se ejecutó una
  instalación real ni se ensayó el instalador de PostgreSQL. Los IDs WinGet y el
  ZIP/SHA-512 de Tomcat se contrastaron con fuentes oficiales.

Pendiente de PC1 y PC2 reales: servicio PostgreSQL, schema y seed, Swing con pantalla,
Tomcat 11, firewall, TLS/SAN/truststore, WSDL entre máquinas y recorrido funcional
completo. Un HTTP 200 en la portada y tres WSDL correctos no verifican por sí solos
login, operaciones de negocio ni persistencia tras reinicio.

## Actualización de diagnóstico PC2

- `mvn -o -pl webapp -am '-Dtest=DiagnosticoSoapTest,ConfiguracionClienteEventosTest,InicioSoapTest' '-Dsurefire.failIfNoSpecifiedTests=false' package`:
  `BUILD SUCCESS`; 12 pruebas focalizadas, sin fallas. Se regeneró `web.war`.
- El WAR nuevo contiene `DiagnosticoSoap.class` y el alias `/eventos` del servlet
  de inicio. Se copió a `PC2/web.war`; su SHA-256 coincide con `webapp/target/web.war`.
- `Preparar-Web.ps1` generó en una ruta temporal `central.host=100.115.156.116`,
  puerto 9128 y rutas `/eventos`, `/portal`, `/sesiones`; la segunda ejecución fue
  idempotente. Los scripts de PC2 pasaron el parser de PowerShell 5.1.
- `Instalar-Web.ps1` se probó contra una estructura temporal de Tomcat 11.0.27:
  detectó la versión, copió el WAR y, con `-Reemplazar`, guardó respaldo del WAR y de
  la aplicación expandida. El WAR instalado coincidió por SHA-256 con el paquete.
- `DiagnosticoHttps.java` compiló en modo fuente con Java 25 y rechazó correctamente
  una invocación sin parámetros. La sonda TLS no pudo probarse contra PC1 real.
- No hay acceso a PC2 ni a su certificado desde este equipo. La causa concreta del
  503 observado requiere la sonda TLS de Java y el nuevo log `Fallo SOAP Portal`.
