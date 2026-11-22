# Pruebas entre dispositivos: LAN y Tailscale

No confundir prueba local con prueba distribuida. La auditoría del 21/11 ejecutó SOAP HTTPS y PostgreSQL reales en loopback; no tuvo acceso a PC 2, PC 3 ni teléfono. Tailscale no se detectó en PATH, ubicaciones habituales ni como servicio. No se instaló ni se cambiaron políticas, firewall o configuración de red.

## 1. Registrar cada nodo

En cada computadora, anotar función, IP LAN, versión Java y usuario que ejecuta el proceso. No compartir contraseñas ni tokens. En el teléfono anotar red y URL utilizada.

```powershell
java -version
Get-NetIPAddress -AddressFamily IPv4 | Select-Object InterfaceAlias,IPAddress
Get-Command tailscale -ErrorAction SilentlyContinue
Get-Service '*tailscale*' -ErrorAction SilentlyContinue
```

Si Tailscale ya está instalado y autorizado por el propietario:

```powershell
tailscale status
tailscale ip -4
$ipOtroNodo = Read-Host 'IP Tailscale del otro nodo'
tailscale ping $ipOtroNodo
```

`status` debe mostrar equipo autenticado y pares; `ip -4` debe devolver la IP propia; `ping` debe alcanzar el par. Estos resultados no verifican SOAP ni Tomcat. Si falta instalación, login o permisos, solicitar al responsable su preparación: no ejecutar `tailscale up`, cambiar ACL, instalar ni modificar firewall como parte de una comprobación de lectura.

Las políticas efectivas deben revisarse por el administrador de la tailnet: PC Web → puerto HTTPS Central y clientes → puerto HTTP/HTTPS Tomcat. Una conexión fallida puede deberse al listener, al firewall, a la política o al servicio; el resultado aislado no identifica por sí solo la causa.

## 2. LAN obligatoria y VPN alternativa

En PC 1, `central.host` debe ser una dirección real alcanzable por PC 2. `central.escucha` debe corresponder a una interfaz local válida para esa prueba. En PC 2, `web.properties` debe apuntar al mismo host, puerto y rutas. Usar certificado con SAN para ese host/IP y truststore de la JVM Tomcat. No desactivar TLS ni usar `curl -k`.

Primero probar IP LAN de PC 1. Para el ensayo Tailscale, repetir con su IP de Tailscale y un certificado que la cubra. No colocar la IP Tailscale de PC 2 como destino de Central ni `localhost` en una configuración que debe llegar a otra computadora. HTTPS conserva SOAP sobre HTTP protegido por TLS; no se añadió un protocolo alternativo a SOAP.

Desde PC 2:

```powershell
$centralIp = Read-Host 'IP LAN o Tailscale REAL de PC Central'
$puertoSoap = [int](Read-Host 'Puerto HTTPS configurado en Central')
$rutaPortal = Read-Host 'Ruta Portal, por ejemplo /portal'
$certificadoPublico = Read-Host 'Ruta al certificado público confiable de Central'
Test-NetConnection $centralIp -Port $puertoSoap
$wsdl = 'https://{0}:{1}{2}?wsdl' -f $centralIp,$puertoSoap,$rutaPortal
curl.exe --fail --show-error --cacert $certificadoPublico $wsdl
```

Repetir para las rutas de Eventos y Sesiones. TCP abierto no demuestra WSDL válido; WSDL válido no demuestra que la operación, el login o la persistencia funcionen. Hacer después consultas y escrituras reales desde el sitio.

Para probar cambio de IP sin recompilar: conservar hashes de `servidor.jar` y `web.war`, detener los procesos propios, editar únicamente sus `.properties`, seleccionar el certificado adecuado, reiniciar y repetir consulta/login/escritura desde PC 2. Los hashes deben permanecer idénticos. Registrar ambas IP efectivamente usadas y resultados; no hay IP de equipo remoto inventada en esta guía.

## 3. Tomcat y clientes

Después de generar los artefactos completos, desplegar `web.war` en Tomcat 11 de PC 2 con su configuración externa y truststore. Seguir [despliegue](../README_DESPLIEGUE_LAB3.md). Verificar el listener HTTP real en `conf/server.xml`: una interfaz limitada a loopback no recibe a PC 3 ni al teléfono. No modificar un Tomcat existente ni sus puertos sin autorización del propietario.

Desde PC 3, y luego desde el celular, usar las direcciones reales:

```powershell
$webIp = Read-Host 'IP LAN o Tailscale REAL de PC Web'
$puertoWeb = [int](Read-Host 'Puerto HTTP de Tomcat')
Test-NetConnection $webIp -Port $puertoWeb
$inicio = 'http://{0}:{1}/web/inicio' -f $webIp,$puertoWeb
curl.exe --fail --show-error $inicio
```

Abrir `/web/inicio` y `/web/movil` en los navegadores. Para Tailscale en el teléfono, comprobar previamente que el teléfono esté autenticado en la tailnet y tenga acceso al equipo Web; no alcanza que las dos computadoras lo estén. Para LAN, comprobar Wi-Fi, aislamiento de clientes y acceso a la red elegida.

Probar anchos 320, 375, 390, 430, 768 y 1024 px. En cada uno registrar desbordamiento horizontal, navegación hamburguesa, foco/formularios, tamaño de botones, tarjetas, imágenes y video 16:9. Repetir al menos login, consulta y asistencia en teléfono físico; una emulación de viewport no demuestra conectividad del teléfono.

## 4. Fallos y recuperación, solo sobre instancias de prueba

| Fallo provocado | Cómo hacerlo sin cambiar políticas | Resultado que debe registrarse |
| --- | --- | --- |
| Central apagado | Detener Central de prueba, mantener Tomcat | Página propia 503 de servicio no disponible; sin traza ni datos privados. Reiniciar y repetir consulta. |
| Puerto SOAP incorrecto | Copia privada de `web.properties` con puerto de prueba cerrado; reiniciar Tomcat de prueba | Error controlado dentro de timeout; restaurar archivo y reiniciar; consulta vuelve a funcionar. |
| Host/IP incorrecto | Copia privada con destino de prueba inaccesible; reiniciar | Error controlado y posterior recuperación. No usar un equipo ajeno como destino de la prueba. |
| Endpoint incorrecto | Ruta inexistente en configuración de prueba | Página de error controlada; corregir ruta y reiniciar. |
| PostgreSQL no disponible | Instancia Central de prueba apuntando a puerto JDBC cerrado | Central falla al arrancar sin abrir Swing como si estuviera listo; Tomcat informa indisponibilidad. Restaurar puerto y reiniciar. No detener PostgreSQL compartido. |
| Web apagado | Detener exclusivamente Tomcat de prueba | El navegador informa conexión no disponible. Un servidor apagado no puede generar una página propia. Reiniciar y verificar recuperación. |
| Interrupción de conectividad | Desconectar manualmente solo el dispositivo de prueba, con autorización de su usuario | Timeout controlado; reconectar y volver a consultar. No tocar firewall/ACL para simular el fallo. |
| Sesión vencida tras reiniciar Central | Reiniciar Central de prueba con sesión web previa | Se requiere login nuevamente; móvil vuelve a su login. |

Guardar fecha, nodo origen/destino, caso, código HTTP/mensaje y resultado de recuperación. No registrar claves, encabezados Authorization ni cookies.

## 5. Orden recomendado para el ensayo de defensa

1. Java 25, Maven, `mvn -Pcobertura clean verify` con `eventosuy_test` correctamente configurada. Preservar informes. Distribuir los dos paquetes completos y comprobar hashes.
2. Preparar/restaurar una base demo independiente y cargar los [datos Lab 3](DATOS_DEMO_LAB3.md). Ajustar `db.*` privados del Central.
3. Arrancar `java -jar servidor.jar` en PC 1; comprobar Swing y los tres WSDL.
4. Desde PC 2, TCP + TLS + WSDL. Arrancar Tomcat con `web.war`; comprobar sus logs y el contexto `/web`.
5. Desde PC 3 y teléfono: login por nickname/correo, consultas, video, registro propio, asistencia única, PDF, logout y rechazo de organizador móvil.
6. En escritorio como organizador: edición con/sin video, archivo válido, rechazo de archivo ajeno o inválido, finalización, seguimiento mutuo y búsquedas/órdenes.
7. Consultar ranking en Swing, reiniciar Central, volver a iniciar sesión y verificar datos persistidos.
8. Repetir por Tailscale si está disponible y permitido; preservar siempre el ensayo LAN obligatorio.
9. Ejecutar la matriz de fallos y recuperar el funcionamiento normal. Conservar evidencia y un respaldo restaurable de la base demo.
