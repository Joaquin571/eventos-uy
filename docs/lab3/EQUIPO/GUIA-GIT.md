# Guía Git y entorno — equipo Laboratorio 3

## 1. Reglas y punto de partida

- Integración: **lab3-desarrollo**. `dev` corresponde al Lab 2 y permanece intacta.
- El corte funcional compartido está en **586eab4**. El commit documental posterior también debe recibirse antes de comenzar; crear ramas desde la punta actual de origin/lab3-desarrollo, no desde 6bdfb17.
- Una rama propia por bloque; PR pequeños hacia lab3-desarrollo. No hacer push directo a integración salvo el coordinador con autorización. No force push, reset destructivo ni reescritura de historia compartida.
- Las ramas de integrantes solo se proponen aquí; esta preparación no las crea.
- Leer [CONTRATOS-EQUIPO](../CONTRATOS-EQUIPO.md), la guía individual y [CORTE-SOAP](../CORTE-SOAP.md). No ejecutar migraciones ni pruebas sobre base habitual.

| Integrante | Rama propuesta |
| --- | --- |
| 1 — Joaquín | lab3-infra-soap |
| 2 | lab3-web-usuarios-patrocinios |
| 3 | lab3-social-eventos |
| 4 | lab3-registros-asistencia |
| 5 | lab3-movil-busqueda |

Los comandos siguientes son instrucciones para trabajo posterior aprobado, no acciones ejecutadas por crear este documento. PowerShell, desde raíz del repositorio salvo indicación distinta.

## 2. Clonar y comprobar base

Elegir una carpeta nueva, sin sobrescribir otra copia:

```powershell
git clone https://github.com/Joaquin571/eventos-uy.git eventos-uy-lab3
Set-Location eventos-uy-lab3
git fetch origin
git switch lab3-desarrollo
git pull --ff-only origin lab3-desarrollo
git status --short --branch
git log --oneline -5
git merge-base --is-ancestor 586eab4 HEAD
```

El último comando debe terminar con código 0 (en PowerShell, revisar `$LASTEXITCODE`). Si no contiene el corte, hay divergencia, falla fetch o aparecen cambios locales inesperados, detenerse y consultar; no forzar ni mezclar con dev.

Si ya hay una copia local, empezar por `git status` y confirmar su rama. No cambiar de rama con cambios de otra persona ni guardarlos automáticamente en stash. Conservarlos y coordinar con su autor. No volver a clonar encima de la carpeta de trabajo.

Configurar autoría propia si hace falta, solo en esta copia:

```powershell
git config user.name "Tu nombre y apellido"
git config user.email "Tu correo de GitHub o correo privado noreply"
```

No usar el nombre/correo de otro integrante.

## 3. Java 25 y Maven

Instalar JDK 25 y Maven; comprobar que el Maven de terminal y el importador de IntelliJ usen Java 25. No usar JDK 26 ni cambiar release para resolver un entorno incorrecto.

```powershell
java -version
mvn -version
```

Si es necesario seleccionar JDK solo para la terminal, sustituir la ruta por la instalación real de esa computadora:

```powershell
$env:JAVA_HOME = 'C:\ruta-real\jdk-25'
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"
java -version
mvn -version
```

Esa ruta es un ejemplo, no un archivo existente del proyecto. En IntelliJ: Project SDK y Maven Runner/Importer con JDK 25; importar POM raíz y sus cuatro módulos. Actualmente no hay wrapper propio que deba copiarse de la demo: usar `mvn` instalado.

Compilación/empaquetado sin ejecutar pruebas de base:

```powershell
mvn -DskipTests package
```

Primera compilación puede descargar dependencias. No necesita Central encendido para generar el cliente del WSDL local. Produce servidor-central/target/servidor.jar y, en esta fase, webapp/target/webapp-1.0-SNAPSHOT.war; `web.war` final pertenece a J02. No versionar target.

Propiedades personales bajo `~/.eventosUy`: copiar plantillas `.example`, completar localmente sin subir secretos, no sobrescribir configuraciones existentes. El Central actual valida esquema y necesita uno ya existente; compilar no autoriza a crearlo/migrarlo. Tomcat aún necesita su configuración local de pruebas por los casos sin migrar. Leer CORTE-SOAP antes de arrancar.

## 4. Crear la rama propia

Solo después de tener base limpia y actualizada. Ejemplo para integrante 3; los demás sustituyen por su nombre de la tabla:

```powershell
git status --short --branch
git switch -c lab3-social-eventos
git branch --show-current
```

No crear esa rama si ya existe: cambiar a la propia después de verificar su estado o coordinar. No crear ramas desde dev. No incluir cambios sin commit de la computadora de otro integrante como si fueran parte de la base compartida.

## 5. Pruebas antes de un commit/PR

Verificaciones SOAP/web actuales sin persistencia:

```powershell
mvn clean package '-Dtest=ConfiguracionCentralTest,EventosPublicadorTest,InicioSoapTest,UsuarioFechaAltaTest,SubidasTest,PatrociniosFlujoTest,PatrociniosAutorizacionTest,CsrfTest,ConsultasEdicionTest,AltasPrgTest' '-Dsurefire.failIfNoSpecifiedTests=false'
```

Son 6 pruebas Central y 79 Web en la base del corte; los totales cambiarán al agregar pruebas. Esa selección **no** ejecuta las pruebas de lógica PostgreSQL ni mide su cobertura. No presentarla como suite completa.

Pruebas JavaScript si se modifica JS y Node está disponible:

```powershell
$pruebasJsLab3 = @(Get-ChildItem webapp/src/test/js -Filter '*.test.cjs' | ForEach-Object { $_.FullName })
node --test @pruebasJsLab3
```

Pruebas nuevas de lógica y suite completa: requieren autorización para escribir únicamente en eventosuy_test, configuración local correcta y protección existente verificando **SELECT current_database() antes de JPA/esquema/datos**. Definir solo EVENTOS_DB_NAME no demuestra seguridad. Seguir el procedimiento vigente del README/`soporte.ProteccionBasePruebas` y verificar que JDBC y JPA usen la misma conexión efectiva, especialmente al evolucionar properties.

Después de esa comprobación, la ejecución completa prevista es `mvn -Pcobertura clean install`, con EVENTOS_DB_NAME=eventosuy_test y credenciales locales configuradas sin escribirlas en Git. No correrla como primer paso ni desactivar guardas para que pase. El mínimo de §7.10 es ≥80 % de líneas de lógica del Central, medido en IntelliJ Run with Coverage; JaCoCo es seguimiento adicional.

Agregar tests de la tarea y sus errores; un mock no prueba PostgreSQL, un endpoint local no prueba LAN ni una vista real de Tomcat. Registrar separadamente ensayo visual y dispositivos reales.

## 6. Revisar y crear commits propios

```powershell
git status --short --branch
git diff --check
git diff --stat
git diff
```

Confirmar que no hay cambios ajenos y que solo se está en la rama propia. Agregar archivos concretos, nunca `git add .`/`git add -A` indiscriminados. Ejemplo de archivo compartido: preparar solo los hunks propios, después de coordinar:

```powershell
git add -p -- logica/src/main/java/implementacion/Sistema.java
git diff --cached --name-status
git diff --cached --check
git diff --cached
git commit -m "Implementa seguimiento de usuarios con pruebas"
```

El mensaje es un ejemplo: describir el cambio real. Añadir también las pruebas/documentos concretos correspondientes antes del commit. Si se preparó un archivo por error, `git restore --staged -- RUTA-DEL-ARCHIVO` lo saca del índice y conserva su contenido local; sustituir la ruta, no ejecutar el marcador literal. No usar restore sobre contenido de trabajo para resolver una duda.

No incluir PDF de consigna/demo, .env, properties reales con secretos, claves/tokens, bases, dumps, respaldos, logs, ZIP, target, out, .idea, .smarttomcat o fuentes wsimport. Los `.example` sin secretos, fuentes y snapshots WSDL/XSD revisados sí corresponden. El .git/info/exclude de Joaquín es local: no protege otras copias. Si se tienen materiales en el clon, excluirlos localmente sin borrarlos ni subirlos.

Revisar valores y no solo nombres de archivo. Si apareció un secreto en un commit, detener publicación y avisar al coordinador; no ocultarlo con otro commit y suponer que dejó de estar en el historial.

## 7. Actualizar la rama con integración

Con trabajo propio guardado en commits revisados y árbol limpio, permanecer en la rama de tarea:

```powershell
git branch --show-current
git status --short --branch
git fetch origin
git merge origin/lab3-desarrollo
```

Esto incorpora integración a la rama propia, no hace merge a dev ni publica integración. No ejecutar desde dev ni desde lab3-desarrollo por confusión. Si cambió un contrato, regenerar consumidor con Maven, revisar todos los afectados y repetir pruebas. No rebase de historia publicada ni force push como solución automática.

## 8. Resolver conflictos sin perder aportes

Si Git informa conflictos, no continuar publicando:

```powershell
git status
git diff --name-only --diff-filter=U
git diff --cc
```

1. Leer ambas versiones y el requisito; avisar al dueño del archivo compartido. No elegir todo `ours`/`theirs` ni borrar marcadores sin preservar ambas funcionalidades.
2. Resolver manualmente solo el alcance entendido; si no se entiende, dejar merge pendiente y coordinar. No ejecutar reset/clean ni descartar archivos locales.
3. Si hay WSDL/DTO, resolver fuentes/contrato y regenerar; no editar clases wsimport. Revisar compatibilidad, no solo archivos conflictivos.
4. Preparar explícitamente cada archivo resuelto con `git add -- RUTA-RESUELTA` (sustituir marcador); revisar `git diff --cached --check`, contenido, compilación y pruebas.
5. Comprobar que no quedan archivos sin resolver. Confirmar el merge con mensaje descriptivo solo cuando la resolución fue revisada. No hace falta reescribir commits anteriores para resolverlo.

## 9. Publicar rama propia y abrir PR

Una vez aprobado publicar la tarea, ejemplo para integrante 3:

```powershell
git push -u origin lab3-social-eventos
```

Ante rechazo/divergencia, detenerse, fetch y revisar; nunca force push. En GitHub: Pull requests → New pull request; **base lab3-desarrollo**, compare rama propia. Revisar que no sea dev/main. No abrir un PR que incluya commits ajenos inesperados.

El PR incluye IDs de tareas, comportamiento, métodos/archivos afectados, cambios de contrato aprobados y consumidores, pruebas/comandos/resultados, cobertura lógica cuando corresponda, permisos, documentación y pendientes. Indicar datos simulados vs PostgreSQL y prueba visual real. Pedir revisión al dueño consumidor y Joaquín si toca compartidos. No merge propio a integración sin revisión/acuerdo. Se recomienda merge normal preservando aportes; no squash/cherry-pick sin decisión del equipo.

## 10. Checklist de cierre del equipo

- Casos anteriores preservados y nuevos según letra; no reglas inventadas.
- Solo Central accede a JPA/PostgreSQL, Swing directo mismo proceso, Web consume SOAP.
- ≥80 % líneas de lógica; pruebas previas y nuevas, fuentes generadas regenerables.
- Un comando produce servidor.jar y web.war; ejecución sin IntelliJ, properties externas y recursos locales.
- Modelo de dominio y DSS fundamentales actualizados; diseño distribuido/DTO/faults/configuración/móvil.
- Seed/instrucciones con tres roles, seguimientos, video, finalizado, archivada/archivable y registros con/sin asistencia; no depende de datos habituales.
- Ensayo en cuatro dispositivos y README verificable desde cero. Entrega Moodle 23/11/2026 09:00 y defensa ese día según horario del grupo; no inventar formato ZIP que la letra no fija.
- Cada integrante conoce su dominio y puede explicar el recorrido completo Web → SOAP → lógica → JPA y Swing → lógica. No presentar una simulación como prueba visual/distribuida.
