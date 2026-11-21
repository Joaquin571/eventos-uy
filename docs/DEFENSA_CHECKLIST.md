# Eventos UY Laboratorio 3: checklist de defensa

Versión breve de la [guía de despliegue](../README_DESPLIEGUE_LAB3.md). La [letra oficial](../Laboratorio%203.pdf), §§3, 7.9 y 8, pide **cuatro dispositivos en la misma red y sin IntelliJ**: PC 1 con Central, Swing y PostgreSQL; PC 2 con Tomcat y la Webapp; PC 3 con navegador de escritorio; y teléfono con navegador. `IP_CENTRAL` e `IP_WEB` son marcadores, no direcciones publicadas. Confirmar con el docente si acepta una VPN como «misma red»; llevar una alternativa LAN comprobada.

## Antes de salir

- [ ] Confirmar horario, grupo y acceso al medio de entrega. Comprobar la fecha vigente en la letra y en la plataforma del curso.
- [ ] Preparar cuatro dispositivos, cargadores, red operativa y acceso a Internet si se mostrará video de YouTube.
- [ ] Comprobar JDK 25 en PC 1 y PC 2, PostgreSQL en PC 1 y Tomcat 11 en PC 2. Si se reconstruye, comprobar también Maven: `java -version`, `mvn -version`.
- [ ] Construir desde el POM raíz con `mvn '-DskipTests' clean package`; revisar `servidor-central/target/servidor.jar` y `webapp/target/web.war`, sus fechas y hashes. Copiar sólo los artefactos necesarios a los equipos de destino.
- [ ] Confirmar esquema, datos de demostración y respaldo privado de la base de defensa. No mezclarla con `eventosuy_test` ni ejecutar JUnit sobre la base de defensa.
- [ ] Confirmar `central.properties` en la cuenta que ejecuta Central y `web.properties` en la cuenta Tomcat. Mantener contraseñas, PKCS#12 privado, truststore con contraseña y respaldos fuera del repositorio.
- [ ] Verificar que el certificado HTTPS de Central tiene SAN para la **IP o DNS real** de la red elegida; transferir a PC 2 sólo el certificado público y comprobar su huella antes de crear el truststore.
- [ ] Confirmar con el administrador reglas de firewall: PostgreSQL local; SOAP HTTPS desde PC 2 a PC 1; HTTP Web desde PC 3 y teléfono a PC 2.

## Al llegar: red y arranque

1. [ ] Conectar los cuatro dispositivos. Medir las IP reales; si se usa VPN autorizada, confirmar que cada cliente que la necesita está conectado y aprobado.
2. [ ] Iniciar PostgreSQL en PC 1 y comprobar conexión, `current_database()` y `current_schema()` **antes de cualquier operación con escritura**.
3. [ ] Desde la carpeta de PC 1 donde se copió el JAR, arrancar `java -jar .\servidor.jar`. Esperar las tres URL WSDL y la ventana Swing; `--sin-swing` no sirve para demostrar la Estación de Trabajo.
4. [ ] Desde PC 2 comprobar `Test-NetConnection IP_CENTRAL -Port 9128` y `https://IP_CENTRAL:9128/portal?wsdl` con certificado confiable. Confirmar que SAN y `central.host` coinciden.
5. [ ] Comprobar `web.properties` y truststore en la JVM de Tomcat. Con Tomcat detenido, desplegar `web.war` y arrancarlo. Revisar logs si falla el despliegue.
6. [ ] Desde PC 3 abrir `http://IP_WEB:8080/web/inicio`; desde el teléfono abrir `http://IP_WEB:8080/web/movil`. Ajustar el puerto si Tomcat usa otro. Si se despliega `ROOT.war`, las rutas empiezan en `/`.
7. [ ] Demostrar consulta, login, una operación que escriba en la base de demostración, resultado visible y persistencia tras reiniciar Central. Demostrar Swing y volver a iniciar sesión Web si la sesión remota venció.
8. [ ] Registrar equipo, hora, caso y resultado sin capturar credenciales ni encabezados de sesión.

## Recorrido móvil

- [ ] Iniciar sesión como asistente y abrir una edición con video. Comprobar el reproductor al ancho de la pantalla; en una edición sin video no debe verse un marco vacío.
- [ ] Abrir **Registrar asistencia**. Elegir un registro propio de una edición aceptada y ya iniciada; confirmar y mostrar el mensaje de éxito y la marca **Asistió**.
- [ ] Volver al mismo registro: el botón no debe ofrecerse de nuevo. En una edición futura, mostrar el aviso de que debe comenzar antes de registrar asistencia.
- [ ] Sin sesión válida, comprobar redirección al login móvil. Explicar que Webapp invoca `confirmarAsistencia` de Central por SOAP y no escribe directamente en PostgreSQL.
- [ ] Para preguntas de código, usar la [guía de arquitectura](lab3/DEFENSA_ARQUITECTURA.md). Para JUnit y cobertura, usar la [guía de pruebas](lab3/GUIA_PRUEBAS_COBERTURA.md); la defensa distribuida no depende de IntelliJ.

## Si falla la red o un servicio

- [ ] Sin VPN aceptada o disponible, usar una LAN local funcional entre los cuatro dispositivos. Actualizar `central.host`, `central.escucha`, `web.properties`, reglas de firewall y certificado SAN para las IP efectivas; reiniciar Central y Tomcat. No desactivar la validación TLS.
- [ ] Central no responde: verificar listener, `Test-NetConnection IP_CENTRAL -Port 9128`, URL WSDL y logs de Central.
- [ ] Falla TLS: comparar host configurado, SAN, certificado público y truststore de la JVM Tomcat. El test TCP no prueba TLS.
- [ ] Web devuelve 404: verificar nombre/contexto del WAR (`web.war` → `/web`, `ROOT.war` → `/`) y logs de Tomcat.
- [ ] Teléfono no accede: probar primero PC 3 → PC 2, comprobar Wi-Fi/VPN, IP Web, aislamiento de clientes y firewall.
- [ ] Falla PostgreSQL o JPA: comprobar base efectiva, servicio y esquema; detenerse antes de ejecutar SQL sobre una base inesperada.

Comandos y pasos extensos: [despliegue](../README_DESPLIEGUE_LAB3.md) y [configuraciones](CONFIGURACION_EJEMPLOS.md). El ensayo físico de la topología, TLS, datos y respaldo es una **comprobación pendiente**, no un resultado acreditado por este checklist.
