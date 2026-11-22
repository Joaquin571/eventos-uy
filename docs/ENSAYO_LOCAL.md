# Ensayo local con datos independientes

Desde la raíz del repositorio, después de construir `servidor.jar` y `web.war`:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/EnsayoLocal.ps1
```

Requiere Java 25, PostgreSQL 17 y Tomcat 11 instalados. Las rutas predeterminadas corresponden a esta computadora; se pueden cambiar con `-JdkRuta`, `-PostgresRuta` y `-TomcatRuta`. La primera preparación puede tardar varios minutos. `-SinSwing` permite probar solo los servidores; el inicio normal abre la estación Swing después de publicar SOAP.

El script crea un clúster PostgreSQL propio y la base `eventosuy_ensayo_demo`, sin utilizar la base habitual. Prepara el esquema, agrega datos de demostración, genera certificados locales y ejecuta los paquetes reales. PostgreSQL, SOAP HTTPS y Tomcat escuchan únicamente en loopback. Los puertos disponibles se eligen en cada inicio; se muestran las direcciones de escritorio y móvil al terminar.

Los usuarios y su contraseña generada se encuentran en `.build-cache/ensayo-local/usuario/.eventosUy/acceso-demo.properties`. Abrir ese archivo localmente para iniciar sesión; no compartirlo ni subirlo. El administrador utiliza Swing. Los datos incluyen seguidores, videos, estados, asistencias y visitas. Las configuraciones de cada proceso quedan en ese mismo directorio privado, utilizado como `user.home`, sin cambiar la configuración habitual de Windows.

```powershell
# Consultar procesos propios y direcciones actuales
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/EnsayoLocal.ps1 -Accion Estado

# Detener solamente los procesos registrados por este ensayo
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/EnsayoLocal.ps1 -Accion Detener
```

La parada conserva datos, certificados y usuarios. Un nuevo inicio reutiliza la base y no repite la carga si ya existe el archivo de acceso. Si falla el arranque, consultar los registros en `.build-cache/ensayo-local/`, ejecutar `Detener` y corregir la causa antes de iniciar otra vez. El script verifica PID y hora de inicio antes de detener procesos.

Este ensayo coloca todos los procesos en una PC. La defensa requiere distribuir Central/Swing/PostgreSQL en PC 1, Tomcat en PC 2, navegador en PC 3 y móvil en un teléfono. Para ese recorrido seguir [pruebas de red](PRUEBAS_RED_LAB3.md), con direcciones LAN reales y certificados correspondientes. La alternativa Tailscale también requiere pruebas desde cada nodo; iniciar este ensayo local no acredita esa conectividad.

## Acceso del celular por Wi-Fi

Para publicar solamente Tomcat en la IP Wi-Fi de esta PC, detener el ensayo y arrancar con la dirección real de la interfaz:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/EnsayoLocal.ps1 -Accion Detener
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/EnsayoLocal.ps1 -DireccionWeb 192.168.1.93
```

El celular debe estar en el mismo Wi-Fi. Usar la URL móvil que imprime el script; el puerto puede cambiar en cada arranque. `192.168.1.93` es la dirección observada en este equipo, no un valor universal. PostgreSQL y SOAP permanecen en loopback. No se modifican reglas de firewall. Para usar Tailscale en su lugar, la dirección elegida debe pertenecer a su interfaz activa, y el celular debe tener acceso a la misma tailnet; ese recorrido requiere verificación separada.