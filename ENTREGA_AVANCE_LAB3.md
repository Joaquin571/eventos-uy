# Eventos-UY — entrega de avance del Laboratorio 3

**Fecha:** 15/11/2026. **Destino sugerido:** rama `lab3-emmanuel`, con Pull Request hacia `lab3-desarrollo` después de revisión del equipo.

## Qué contiene este ZIP

Proyecto fuente completo tomado del ZIP recibido (`eventos-uy-dev(1).zip`) con el avance de disponibilidad (`lab3_integracion_en_progreso.zip`) y pruebas adicionales. No incluye `.git`, `.idea`, archivos compilados ni credenciales. No es un Laboratorio 3 finalizado.

## Cambios añadidos sobre el proyecto original

1. **W04 / disponibilidad de usuarios:** `EventosPublicador` publica `nicknameDisponible` y `correoDisponible` por SOAP, delegando en `ISistema.existeUsuario` y `ISistema.existeCorreoElectronico`. Respuestas booleanas y error de consulta sin exponer detalles internos.
2. **Contrato SOAP:** operaciones añadidas al WSDL/XSD local para que `wsimport` genere el cliente.
3. **Cliente web:** `ClienteEventosCentral` llama las operaciones del Central usando la configuración externa ya existente.
4. **Servlet HTTP:** `/disponibilidad-usuario` valida tipo/valor, responde JSON, deshabilita caché y devuelve 503 si falla el Central.
5. **Formulario de registro:** indicadores de disponibilidad y script `disponibilidad-usuario.js` con debounce, cancelación de consultas anteriores, validación del campo y mensaje accesible.
6. **Pruebas:** `DisponibilidadSoapTest` cubre identificadores existentes/libres, normalización, valores vacíos y fault ante error interno; `disponibilidad-usuario.test.cjs` comprueba petición y respuesta de la interfaz.

**Nota:** la validación de unicidad al registrar continúa siendo responsabilidad del servidor; el aviso AJAX no reemplaza la validación de negocio. No se alteraron deliberadamente las operaciones existentes de alta.

## Verificaciones realmente realizadas

- `node --test webapp/src/test/js/*.test.cjs`: **58 pruebas aprobadas, 0 fallidas** (incluye las 2 nuevas).
- XML analizado correctamente: `pom.xml`, `webapp/pom.xml`, `servidor-central/pom.xml`, `EventosService.wsdl` y `EventosService_schema1.xsd`.
- **No ejecutado:** Maven/Java 25, JUnit Java, `wsgen`/`wsimport` reales, PostgreSQL, despliegue Tomcat 11, pruebas de SOAP en red ni defensa en cuatro dispositivos. El entorno disponible usa Java 21 y no dispone de `mvn`; por lo tanto no afirmar compilación exitosa.

## Pendientes principales según la letra y `docs/lab3/REQUISITOS.md`

- **Arquitectura:** migrar TODOS los casos de uso web a SOAP, quitar `logica`/JPA/Hibernate del WAR, evitar accesos locales a BD, manejar Central caído y empaquetar/verificar `servidor.jar` y WAR separados.
- **Usuarios:** autenticación, perfiles, altas y modificaciones remotas; seguir/dejar de seguir y mostrar seguidores/seguidos.
- **Eventos/ediciones:** video de edición, finalizar evento, archivar edición aceptada y pasada con permisos correctos.
- **Registros:** confirmación de asistencia desde móvil y constancia PDF descargable con control de autorización.
- **Móvil:** vistas/recorridos requeridos para asistentes dentro del mismo WAR, sin segundo backend.
- **Búsquedas y estadísticas:** búsqueda/orden, filtro por cantidad de visitas, registro de consultas y ranking de cinco eventos en Swing.
- **Imágenes:** transporte de bytes por SOAP sin lectura directa desde Web.
- **Calidad y despliegue:** verificar cobertura mínima del 80% con JUnit/JaCoCo, pruebas funcionales e integración, Java 25, Tomcat 11, PostgreSQL y cuatro dispositivos.

El repositorio ya contenía un primer corte SOAP para listar eventos y documentación de configuración; **no confundir ese primer corte con la migración total**. Varias decisiones de contrato y autorización están pendientes en `docs/lab3/CONTRATOS-EQUIPO.md` (sección 11). Acordarlas antes de modificar WSDL compartido o reglas de negocio.

## Integración con Git

1. En la carpeta local de la rama `lab3-emmanuel`, **copiar solo los archivos de cambios** del ZIP `lab3_archivos_modificados.zip` respetando rutas. No reemplazar `.git` ni borrar cambios de compañeros.
2. Revisar `git status` y `git diff`; coordinar las operaciones nuevas del contrato SOAP con quien mantenga el WSDL.
3. Compilar y probar en Java 25 con Maven, regenerando el cliente SOAP antes de hacer el merge.
4. Commit sugerido: `Agregar disponibilidad SOAP de usuarios y pruebas`.
5. Subir `lab3-emmanuel` y abrir Pull Request hacia `lab3-desarrollo`; el equipo resuelve la integración.

**Advertencia:** el ZIP completo es respaldo de trabajo, no un reemplazo seguro de la rama compartida. Para Git usar preferentemente el ZIP pequeño de cambios.
