# Integración de patrocinios

Requiere Java 25 o superior, Tomcat 11 instalado, PostgreSQL y la base `eventosuy_test` existente.
Las credenciales y el host se toman de las mismas variables `EVENTOS_DB_*` de la aplicación.
No usa datos del prototipo ni modifica la base habitual.

1. Ejecutar `mvn test` con Java compatible y `EVENTOS_DB_NAME=eventosuy_test`, después de verificar la conexión.
2. Desde la raíz ejecutar en PowerShell:

```powershell
./webapp/src/test/manual/verificar-patrocinios.ps1 -TomcatRuta 'C:/ruta/apache-tomcat-11' -JdkRuta 'C:/ruta/jdk-25'
```

El script inicia Tomcat en un puerto local libre y contexto `/pruebas`, registra los servlets de producción y el filtro de acceso, y usa login real, JSP reales e `ISistema` real. Verifica la base mediante `SELECT current_database()` antes de cualquier alta. Crea datos con nombres únicos en `eventosuy_test`; no elimina datos. Detiene Tomcat al finalizar.
Comprueba el alta, la recuperación de errores, la consulta, la fecha, el escape HTML, los enlaces y la privacidad por rol/estado. No sustituye la revisión de interacción JavaScript en un navegador.

## Consultas de edición, tipos e imágenes protegidas

```powershell
./webapp/src/test/manual/verificar-consultas.ps1 -TomcatRuta 'C:/ruta/apache-tomcat-11' -JdkRuta 'C:/ruta/jdk-25'
```

El segundo script usa las mismas condiciones de seguridad de base y puerto local. Comprueba los selectores GET sin JavaScript, los enlaces, las fechas, el perfil, los registros general/con código por rol, los tipos con nombre repetido entre ediciones, el escape HTML y los bytes de las imágenes públicas/privadas. También verifica estados vacíos y respuestas 404.

Las pruebas JavaScript se ejecutan con Node, sin dependencias adicionales:

```powershell
node --test webapp/src/test/js/alta-patrocinio.test.cjs webapp/src/test/js/consultas-selectores.test.cjs
```
