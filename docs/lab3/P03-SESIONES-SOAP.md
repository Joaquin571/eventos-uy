# P03 · Inicio y cierre de sesión SOAP

> Integrado en Web y Portal el 15/11 sin un segundo mecanismo de sesiones:
> [estado actual y validación real](INTEGRACION-2026-11-15.md).


## Contrato implementado

`SesionesPublicador` recibe por constructor un único `SesionesRemotas` compartido. Usa `WebServiceContext` exclusivamente para adaptar encabezados HTTP. `iniciarSesion(identificador, contrasenia)` delega a `SesionesRemotas.iniciar`, que reutiliza `ISistema.autenticar`. La respuesta SOAP contiene nickname, nombre y un rol de presentación que solo puede ser ASISTENTE u ORGANIZADOR. La referencia opaca aparece únicamente en `X-EventosUy-Session` de la respuesta HTTP, acompañada por `Cache-Control: no-store`.

`cerrarSesion()` exige un único encabezado `Authorization` con esquema Bearer y referencia de 43 caracteres Base64 URL. La ausencia, duplicación, agrupación con coma y formato incorrecto producen el fault `SOLICITUD_MALFORMADA`. Toda referencia con formato válido recibe una respuesta correcta, incluso si era desconocida, venció o ya fue revocada. El cierre elimina la entrada de forma atómica mediante `ConcurrentHashMap.remove`, sin devolver información sobre su existencia. Dos cierres concurrentes responden correctamente y la referencia queda inutilizable para `requerirActor`.

Los otros códigos controlados son `CREDENCIALES_INVALIDAS` y `SERVICIO_NO_DISPONIBLE`. Los mensajes y el detalle del fault carecen de datos de entrada, causas internas y referencias de sesión. Identificador y contraseña viajan en la solicitud de login sobre HTTPS. La credencial opaca no forma parte de los parámetros ni los DTO SOAP. Mantener desactivados los volcados de mensajes y encabezados de Metro, y excluir las credenciales de los logs de transporte durante la futura integración.

El WSDL y XSD de sesiones están en `webapp/src/main/resources/wsdl/`. Las ejecuciones Maven de `wsgen` y `wsimport` generan contrato y proxy por separado de los eventos públicos. El cliente se genera en `web.soap.generado.sesiones`, bajo `target/`. La interfaz SOAP no acepta `Actor`, nickname de actor, rol ni privilegio administrativo. `identificador` sigue siendo el nickname o correo presentado junto con la contraseña.

El generador `wsgen` 4.0.5 exige un constructor público sin argumentos para esta clase de implementación. Se conserva uno que falla inmediatamente para impedir una publicación accidental sin inyectar el registro compartido. La publicación por instancia usa el constructor con `SesionesRemotas`.

### Investigación del constructor

Se compiló una clase de sonda de nivel superior con `@WebService` y únicamente un constructor público con parámetro. Con Java 25 y el runtime Metro 4.0.5 instalado, `Endpoint.create(instancia)` y `publish(HttpContext)` publicaron correctamente esa instancia. En cambio, ejecutar `com.sun.tools.ws.WsGen` del artefacto `jaxws-tools:4.0.5` sobre la misma clase terminó con código 1 y `ModelerException`. El diagnóstico indica erróneamente que la clase es interna y debe ser static, aunque la sonda es de nivel superior.

La inspección de `WebServiceVisitor.isLegalImplementation` en ese artefacto confirma que el procesador busca un constructor público sin parámetros y rechaza la implementación cuando falta. La comprobación ocurre antes de validar un eventual `endpointInterface`. El generador contempla otra rama para el modelo propietario stateful de Metro, que requiere otro diseño de publicación. Mantener el flujo actual de generación reproducible con Maven implica conservar el constructor vacío. El runtime por instancia no lo ejecuta. Quitar esta exigencia obligaría a cambiar el flujo de generación o el modelo del servicio, por lo que se mantiene la solución existente. La sonda se creó solamente bajo `webapp/target/investigacion-wsgen-constructor`.

## Publicación aislada sobre el mismo HttpsServer

Este es el esquema que ejerce `SesionesSoapHttpsTest`. El arranque productivo ya usa el mismo mecanismo; ver [Arranque del Central con HTTPS](ARRANQUE-CENTRAL-HTTPS.md). Su ejecución con persistencia real y la integración web siguen pendientes.

```java
var registro = new SesionesRemotas(sistema, new SecureRandom(), reloj, duracion);
var eventos = Endpoint.create(new EventosPublicador(sistema));
var sesiones = Endpoint.create(new SesionesPublicador(registro));
eventos.publish(servidorHttps.createContext("/eventos"));
sesiones.publish(servidorHttps.createContext("/sesiones"));
servidorHttps.start();
```

La prueba publica además `/sesiones-otra` con otra instancia de la fachada inyectada con el mismo registro. Un login en la primera ruta puede cerrarse desde la segunda. `EventosPublicador` usa el mismo `ISistema` y conserva su consulta pública. El listener HTTPS se configura con `HttpsConfigurator` y certificado temporal PKCS#12 con SAN válido para loopback. El cliente conserva las verificaciones TLS del JDK y confía en el certificado público de prueba por puerto.

## Ejecutar y regenerar

Con Java 25, desde la raíz:

```powershell
mvn -pl webapp -am '-Dtest=SesionesSoapHttpsTest,SesionesRemotasTest,EncabezadosSoapHttpsTest,InicioSoapTest,EventosPublicadorTest,ConfiguracionCentralTest' '-Dsurefire.failIfNoSpecifiedTests=false' test
```

Las pruebas usan un doble de `ISistema`. Solo se admiten las operaciones de autenticación y listado requeridas por la preparación. El doble no abre JPA ni PostgreSQL. El cliente de prueba anidado en `SesionesSoapHttpsTest` crea un puerto generado por invocación, envía `HTTP_REQUEST_HEADERS` y lee `HTTP_RESPONSE_HEADERS`. Un handler inspecciona respuestas y faults en memoria para comprobar la ausencia de secretos, sin imprimir los envelopes.

Si se cambia el contrato Java:

```powershell
mvn -pl servidor-central -am -DskipTests process-classes
Copy-Item servidor-central/target/wsdl/SesionesService.wsdl webapp/src/main/resources/wsdl/SesionesService.wsdl
Copy-Item servidor-central/target/wsdl/SesionesService_schema1.xsd webapp/src/main/resources/wsdl/SesionesService_schema1.xsd
```

Revisar el diff de WSDL/XSD y recompilar Web para regenerar el cliente. La dirección de marcador del WSDL local se reemplaza en cada puerto con el endpoint HTTPS de la prueba. Las clases generadas y los certificados temporales quedan fuera de los fuentes.

## Verificación y límites

La suite comprueba login válido, contraseña incorrecta, datos de login incompletos, entrega de credencial por HTTP, dos sesiones independientes, cierre desde otro publicador con el mismo registro, inutilización de una referencia revocada en el registro, encabezados ausentes/duplicados/malformados, ausencia de secretos en respuestas/faults, rechazo de un resultado de autenticación sin rol remoto y aislamiento de logins concurrentes. También comprueba que el cierre de una referencia desconocida responde correctamente y que dos cierres simultáneos reciben éxito sin permitir reutilizar la sesión.

La ejecución es local por loopback con Metro 4.0.5 y Java 25. El vencimiento exacto se cubre en las pruebas unitarias del registro con reloj controlado. El arranque HTTPS está implementado y probado con un doble; quedan pendientes su ejecución con persistencia real, la prueba de sesiones entre computadoras y la integración de los Servlet. El único caso de negocio usado por la fachada es la autenticación ya existente. Todavía no se publica alta de tipo de registro ni se agregan operaciones protegidas de otros dominios.

La ejecución del 10/11/2026, después del ajuste de idempotencia, terminó con BUILD SUCCESS y 34 pruebas aprobadas, sin fallos ni omisiones. Se ejecutaron 8 pruebas de sesiones SOAP sobre HTTPS, 10 del registro de sesiones, 4 de encabezados HTTPS, 6 de la portada SOAP, 2 del publicador de eventos y 4 de configuración del Central. Los certificados fueron temporales y la prueba mantuvo la verificación TLS. Java 25 informó la advertencia ya observada de Metro sobre inyección mediante sun.misc.Unsafe. Los errores simulados de las regresiones de eventos producen logs esperados y la suite finaliza correctamente.
