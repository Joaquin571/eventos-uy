# Integrante 1 — Joaquín

## 1. Objetivo

Responsable de arquitectura, infraestructura SOAP, configuración, imágenes, empaquetado, integración técnica y despliegue. También implementa persistencia de visitas y ranking Swing. No asume toda la migración web ni escribe todas las fachadas de los demás dominios.

Base: corte SOAP `586eab4`; integración exclusivamente en lab3-desarrollo. Leer [contratos](../CONTRATOS-EQUIPO.md) y [guía Git](GUIA-GIT.md). La rama propuesta es `lab3-infra-soap`; todavía no fue creada.

## 2. Tareas asignadas

| ID | Trabajo | Prioridad / dificultad relativa | Aceptación |
| --- | --- | --- | --- |
| J01 | Acordar DTO, fechas, usuario/rol, identidad remota, faults y organización de servicios. | P0 / 3 | Acuerdo revisado por consumidores; ninguna entidad de dominio viaja; decisiones pendientes explícitas. |
| J02 | Completar configuración, ciclo de vida, esquema inicial y empaquetado. | P0–P2 / 5 | Un comando produce servidor.jar/web.war; propiedades por nodo; arranque sin IDE y procedimiento de instalación reproducible. |
| J03 | Transferencia SOAP de imágenes y migración de ImagenServlet/Subidas. | P0 / 5 | Formatos, bytes, límites, privacidad, multipart y mensajes actuales conservados. |
| J04 | Persistencia de visitas y ranking Swing. | P1 / 5 | Incremento seguro concurrente; top cinco posición/nombre/cantidad; reinicio conserva conteos. |
| J05 | Integración, cobertura, documentación, seed conjunto y ensayo distribuido. | P2 / 5 | WAR productivo sin lógica/JPA/driver PostgreSQL; cobertura y cuatro dispositivos comprobados. |

Los puntos son estimación relativa, no días. El reparto conserva margen de integración; no son métricas de productividad.

## 3. Archivos y módulos

- `servidor-central/pom.xml`, `src/main/java/central/Main.java`, `ConfiguracionCentral.java`, `soap/` y plantillas.
- `pom.xml`, `webapp/pom.xml` en coordinación con retirada de dependencias por W05.
- `logica/src/main/java/persistencia/BaseDeDatos.java`, `logica/src/main/resources/META-INF/persistence.xml`.
- Imágenes: `webapp/src/main/java/web/ImagenServlet.java`, `Subidas.java`, clientes SOAP; lógica existente de imágenes sin duplicar negocio en fachada.
- Visitas: `logica/src/main/java/clases/Evento.java`, `implementacion/Sistema.java`, `interfaces/ISistema.java`, `manejadores/ManejadorEventos.java`, DTO y pruebas.
- Swing: `estacion-trabajo/src/main/java/swing/principal.java`; nuevo panel de ranking por crear. Consultas existentes se coordinan con 3/4.
- README, docs/lab3 y aportes integrados a `logica/src/main/java/datos/CargarDatosPrueba.java`.

## 4. Métodos existentes reutilizables

`Fabrica.getInstance().getISistema()`, `BaseDeDatos.inicializar(Map)`, `cerrar`, `ISistema.validarImagen/asociarImagen/obtenerImagen`, `Sistema.listarEventos` y `ManejadorEventos.obtenerEventos`. El publicador existente es `EventosPublicador.listarEventos`; `Main.main` ya publica antes de `swing.principal.main`.

No existe contador/ranking en la API actual. No confundir su diseño pendiente con un método disponible.

## 5. Funcionalidades nuevas propias

Contador persistente y ranking administrativo; completar instalación/empaquetado/configuración y transporte de imágenes. Conservar Swing local en el mismo proceso; no convertirlo a cliente SOAP.

## 6. Contratos SOAP

Coordinar convenciones generales y mantener compatibilidad del listado existente. Implementar adaptación remota de imágenes. Para visitas, entregar operación de lógica y acordar contrato con 5. El ranking Swing no necesita SOAP sin consumidor remoto. Las fachadas delegan, no contienen JPQL/JPA.

## 7. Dependencias

- 2 define consumidor y sesión; W05 retira inicialización JPA de Web cuando todos hayan migrado.
- 3 comparte Evento/finalización y consultas de edición para privacidad de imagen.
- 4 comparte estados/archivo y datos de registro.
- 5 necesita incremento de visitas para su filtro y acuerdos de conteo/desempate.
- Decisiones P01–P03/P06/P08 de CONTRATOS-EQUIPO antes de sus integraciones sensibles.

## 8. Orden recomendado

J01 primero. Avanzar configuración/paquetes e imágenes por cortes; acordar modelo Evento con 3 y luego J04, sin esperar todo el sitio. Mantener pruebas de contrato. Terminar J02/J05 después de W05 y ensayar dos equipos temprano, cuatro al cierre.

## 9. Pruebas obligatorias del bloque

- Regresión `ConfiguracionCentralTest`, `EventosPublicadorTest`, `InicioSoapTest` y web afectada.
- SOAP con imágenes válidas/inválidas, ausencia e imagen privada; cliente generado y multipart con CSRF.
- Visitas concurrentes sin pérdida; ranking con menos/más de cinco eventos, orden y persistencia tras reinicio.
- Empaquetado real: Metro/JAXB, JPA e ImageIO desde servidor.jar; WAR sin dependencias productivas prohibidas.
- Configuración externa, destino LAN, indisponibilidad y cambio de IP sin rebuild.
- Persistencia solo con barrera JDBC sobre eventosuy_test y autorización; nunca base habitual. No cambiar la barrera para hacer pasar tests.

## 10. Definición de terminado

JUnit del bloque y regresión aprobados; contribución a ≥80 % de líneas de lógica (IntelliJ, JaCoCo adicional); documentación y seed integrados. Instalación limpia sin IDE con servidor.jar y web.war; ensayo físico registrado sin confundirlo con dobles. Plantillas sin secretos y referencias excluidas.

## 11. No modificar sin coordinación

Firmas de ISistema; DTO/faults ya publicados; Evento/Edicion y sus DTO; ContextoListener/FiltroAcceso y POM Web; cabezal; seed; Sistema/ManejadorEventos completos. No reformatear archivos compartidos. No tocar dev, materiales de referencia, bases ni migraciones sin autorización.

## 12. Entrega mediante PR

PR pequeños por contrato/configuración, imágenes y visitas/ranking hacia lab3-desarrollo. Explicar operaciones/cambios de paquete, consumidores afectados, pruebas y riesgos. Adjuntar fuente del contrato y snapshots WSDL/XSD revisados cuando corresponda, nunca fuentes wsimport. Revisión por responsable consumidor; instrucciones de despliegue y decisiones aprobadas registradas. J05 consolida análisis/dominio/DSS y diseño distribuido de todos los bloques (§8).
