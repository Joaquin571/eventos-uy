# Eventos UY Lab 3 — entrega para dos PC Windows

Copiar esta carpeta completa a cada PC, por ejemplo a `C:\entrega-lab3`.
No hacen falta Git, IntelliJ, Maven ni archivos previos en el perfil de usuario.

- PC1: Java 25, PostgreSQL 17, `PC1/servidor.jar`, Swing y SOAP/HTTPS.
- PC2: Java 25, Tomcat 11, `PC2/web.war` y cliente SOAP/HTTPS.
- `base-datos/`: esquema nuevo y datos de demostración.

Seguir [documentacion/MANUAL.md](documentacion/MANUAL.md). Comprobar archivos con
`documentacion/SHA256SUMS.txt`.

La configuración operativa se crea en `PC1/config/central.properties` y
`PC2/config/web.properties`; los almacenes TLS en las carpetas `certificados/`.
Esos archivos privados no forman parte de esta distribución ni deben reenviarse.
