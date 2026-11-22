# Auditoría de Eventos UY — Laboratorio 3

Fecha inicial: 21/11/2026; última revisión: 22/11/2026. Rama: `lab3-integracion-equipo`, base `e043965`, con cambios locales sin commit ni push.

## A. Resumen general

**El proyecto mejoró y supera el 80 % de cobertura de lógica, pero no se declara listo para la defensa física.** Se ejecutaron **587 JUnit distintas aprobadas**, 62 pruebas JavaScript y tres verificadores Swing. No hubo fallos en las últimas ejecuciones de esas pruebas. Los paquetes finales incorporan las correcciones de PDF, búsqueda y listado público y pasaron cuatro IT, con **99 solicitudes HTTP reales**, incluido el recorrido móvil y la descarga del PDF desde el WAR. La defensa entre dispositivos, los seis viewports y la medición desde la interfaz IntelliJ siguen pendientes.

Se leyó el contenido textual de las **14 páginas** de `C:\Users\benta\Downloads\Laboratorio 3.pdf`, proporcionado durante la auditoría (SHA-256: `D0F35FD8A59CF15F4C09B3F6CF13B991F439C0BD6F13F492670E46EAF9A28401`). Se cotejaron arquitectura (§3), casos de uso (§6), restricciones (§7) y entrega (§8). Las figuras se utilizaron por sus leyendas y descripción normativa; no se acredita aquí revisión visual de sus imágenes ni equivalencia visual de la interfaz. El texto extraído para consulta está en `.build-cache/auditoria/letra-lab3.txt`, sin agregar el PDF al repositorio.

Se corrigieron configuración inconsistente entre clientes SOAP, clasificación incorrecta de validaciones de video, consulta fallida de registros históricos y restricciones de asistencia no exigidas por la letra. Se agregó un cargador reproducible de los escenarios nuevos de defensa y documentación de análisis/diseño y redes.

La arquitectura conserva cinco módulos. Central obtiene `ISistema` de la Fábrica, inicia JPA, publica tres servicios HTTPS y abre Swing en la misma JVM. Web consume SOAP mediante clientes generados; sus dependencias hacia lógica y Central son solo de pruebas. No se encontraron imports de entidades, implementación, JDBC ni JPA en los fuentes principales web. Se inspeccionó el WAR final y se desplegó en Tomcat 11 sin dependencias de lógica/JPA/JDBC.

No se modificaron datos de la instalación habitual de PostgreSQL, archivos privados en el directorio del usuario, políticas de Tailscale ni firewall. Se crearon exclusivamente instancias y datos temporales dentro del espacio de trabajo para las pruebas.

## B. Matriz de cumplimiento

Estados: **CUMPLE** indica evidencia de lo expresamente probado en la fila; **PARCIAL** distingue fuente/transporte local de despliegue completo; **NO VERIFICADO** no implica aprobación. Las pruebas locales no acreditan cuatro equipos.

| Requisito | Estado | Archivo o componente | Prueba realizada | Resultado | Observaciones |
| --- | --- | --- | --- | --- | --- |
| Central concentra lógica/JPA | CUMPLE | `central.Main`, `Sistema`, manejadores | Revisión y 358 JUnit de lógica | Aprobadas | Incluye persistencia real. |
| Web sin acceso directo a PostgreSQL | CUMPLE | `webapp/pom.xml`, fuentes y WAR | Scopes/imports, inspección ZIP y Tomcat real | Sin lógica/JPA/JDBC en WAR | La IT además elimina variables DB del proceso Tomcat. |
| Swing invoca lógica directamente y comparte JVM | PARCIAL | `central.Main`, `swing.principal` | Fuente y tres verificadores de paneles | Aprobados | Falta abrir el JAR final y recorrer toda la GUI. |
| Cuatro dispositivos en LAN | NO VERIFICADO | Despliegue | Sin acceso a otros nodos/teléfono | Pendiente | No sustituido por localhost. |
| Exclusivamente SOAP Web–Central | CUMPLE | Clientes, publicadores | Transporte Metro HTTPS real | Aprobado | HTTPS es HTTP protegido por TLS; no se introdujo REST. |
| Jakarta XML WS 4 / Metro | CUMPLE | POM Central/Web | Dependencia 4.0.5 y comunicaciones reales | Aprobadas | No basta inspección de POM: hubo invocaciones. |
| Publicación antes de Swing, sin Tomcat en Central | PARCIAL | `Main`, `ServidorHttpsCentral` | Fuente y pruebas de servidor HTTPS | Aprobadas | Arranque del JAR completo verificado; falta recorrido visual de Swing. |
| WSDL/XSD consistentes | CUMPLE | Recursos WSDL y publicadores | Tres `wsgen`, tres `wsimport`, comparación semántica | 1 JUnit aprobada | No se editaron fuentes generadas. |
| WSDL desde otra computadora | NO VERIFICADO | Red y TLS | Solo loopback disponible | Pendiente | Instrucciones en guía de redes. |
| DTO JAXB, fechas, listas, enumerados, imágenes | CUMPLE | `contratos.dto`, `ConversorDatos` | Conversión y SOAP real | Aprobadas | Tipos de transporte separados de entidades. |
| SOAP Faults de negocio comprensibles | CUMPLE | `PortalPublicador` | Errores de validación, permiso y fallo técnico | Aprobadas | Corregida clasificación de video inválido. |
| Fachadas sin negocio ni JPA | CUMPLE | `central.soap` | Revisión de delegación y autorización por identidad | Conforme al recorrido revisado | Mapeo y traducción de errores en frontera. |
| Configuración Central/JDBC externa | CUMPLE | `ConfiguracionCentral` | Pruebas de configuración y arranque HTTPS | Aprobadas | `Main` fuerza `validate`. |
| Sin credenciales en `persistence.xml` | CUMPLE | Descriptor y `BaseDeDatos` | JUnit de descriptor/propiedades | Aprobadas | Pruebas exigen entorno explícito. |
| Todos los clientes respetan archivo externo | CUMPLE | `ConfiguracionWeb`, `ClienteEventosCentral` | Tres regresiones nuevas | Aprobadas | Ubicación habitual y alternativa verificadas. |
| Cambiar IP y reiniciar mismos artefactos entre nodos | NO VERIFICADO | Paquetes/configuración/red | Endpoints sustituidos localmente; sin paquetes nuevos | Prueba física pendiente | No se acredita con lectura de propiedades. |
| Central apagado: página propia sin traza | CUMPLE | `InicioServlet`, `FiltroAcceso` | Tomcat con WAR real y Central detenido | HTTP 503 controlado | Verificado por HTTP; sin respaldo local de datos en Tomcat. |
| Seguir/dejar de seguir y no duplicar relaciones | CUMPLE | Sistema/manejador usuarios | Unitarias, concurrencia y SOAP/PostgreSQL | Aprobadas | Idempotencia no genera segunda relación. |
| Seguimiento persiste al reinicializar | CUMPLE | `PortalPersistenciaHttpsTest` | Cerrar/iniciar JPA y volver a consultar por SOAP | Aprobada | Relaciones mutuas en el conjunto demo. |
| Finalizar, ocultar y rechazar nuevas ediciones | CUMPLE | Sistema/eventos | Pruebas de lógica/concurrencia y SOAP | Aprobadas | Regla de rol organizador; la letra no define dueño del evento. |
| Archivo: propietario, Aceptada, fin anterior, fecha | CUMPLE | `Edicion`, `ManejadorEventos` | Positivos/negativos, permisos y persistencia | Aprobadas | No se habilitaron transiciones inversas. |
| Archivo conserva registros con/sin asistencia | CUMPLE | Lógica y consultas de registro | Nueva integración completa por HTTPS | Aprobada | Registros y datos de constancia sobreviven a reinicialización JPA. |
| Archivadas visibles en perfil del organizador | CUMPLE | `Sistema.consultarPerfil`, `usuario.jsp` | Lógica, SOAP persistente y JSP real | Aprobado para visitante y propietario | El público ve nombre/estado/fecha, sin registros ajenos ni enlace al detalle restringido. Ingresadas/rechazadas siguen privadas. |
| Video opcional, normalización y rechazo de URL inválida | CUMPLE | `VideoYoutube`, alta y SOAP | JUnit y nueva regresión real | Aprobadas | Dirección almacenada usa `youtube-nocookie.com/embed`. |
| Iframe en escritorio/móvil y 16:9 | PARCIAL | JSP y CSS | Revisión de plantillas/CSS, atributos del servlet | Fuente preparada | Falta reproducción y render real. |
| Asistencia propia y confirmación única | CUMPLE | Sistema/registros | Persistencia, concurrencia, permisos y SOAP | Aprobadas | Se quitaron condiciones de fecha/estado ausentes en §6.3. |
| Asistencia en registro futuro o archivado | CUMPLE | Lógica y consultas web | Regresión futura en lógica y archivada por HTTPS | Aprobadas | Registro existente y actor asistente siguen siendo obligatorios. |
| Datos/autorización de constancia | CUMPLE | `obtenerDatosConstancia` | SOAP/PostgreSQL: sin asistencia, propia, ajena y tras archivo | Aprobadas | No acredita el archivo PDF final. |
| PDF válido, contenido y `application/pdf` | CUMPLE | `GeneradorConstanciaPdf`, servlet y WAR | Cinco JUnit y descarga HTTP real desde Tomcat | Aprobadas | El PDF descargado conserva `Łucía Pérez`, evento y edición; constancia ajena rechazada. |
| Visitas por filtro escritorio/móvil | CUMPLE | `FiltroVisitasEvento` | Rutas, recursos auxiliares, errores y SOAP | Aprobadas | No se modificaron los servlets para contar visitas. |
| Visitas persistentes y ranking top cinco | CUMPLE | Manejador eventos, Swing | PostgreSQL, concurrencia y ejecutor Swing | Aprobadas | Falta recorrido visual físico. |
| Búsqueda nombre/descripcion, vacío y órdenes | CUMPLE | Sistema/manejador | Lógica y cliente SOAP | Aprobadas | Filtros de finalización/estados incluidos. |
| Buscador en cabezales y enlaces de resultados | PARCIAL | Cabezales escritorio/móvil y JSP búsqueda | JSP móvil real en Tomcat: formulario GET, destino, campo y recursos | Buscador móvil agregado y renderizado | Falta recorrido completo del resultado y seis viewports en navegador. |
| Disponibilidad asíncrona nickname/correo | CUMPLE | JS, servlet y SOAP | Node, JUnit y SOAP persistente | Aprobadas | `fetch`, `async/await`; no sustituye unicidad final. |
| Unicidad concurrente y rollback | CUMPLE | Manejadores e índices | PostgreSQL real, escrituras concurrentes | Aprobadas | Se observaron rechazos SQL esperados en pruebas negativas. |
| Login móvil nickname/correo, solo asistentes | CUMPLE | Sesiones, Sistema y filtros | Unitarias y SOAP real | Aprobadas | Organizador rechazado; administrador solo Swing. |
| Sesión, logout y redirección móvil | CUMPLE | Filtros/servlets | Pruebas de sesión y revocación | Aprobadas | Falta teléfono físico. |
| Responsive 320/375/390/430/768/1024 px | NO VERIFICADO | Vistas móviles/Bootstrap | Intento de Chrome headless: proceso gráfico falla por acceso denegado | Pendiente | Se dejó runner CDP para esos tamaños; render JSP no acredita diseño visual. |
| Móvil en mismo WAR, recursos locales | CUMPLE | `webapp`, `vendor/bootstrap` | Inspección del WAR y despliegue real | Recursos móviles/Bootstrap presentes | Interacción en teléfono sigue pendiente. |
| Integridad, transacciones y cierre de recursos | CUMPLE | JPA/manejadores | Regresiones PostgreSQL y concurrencia | Aprobadas en casos ejecutados | No se promete ausencia universal de defectos. |
| Respaldo/restauración PostgreSQL | CUMPLE | Clúster temporal | `pg_dump -Fc`, `pg_restore --exit-on-error` | Conteos origen/destino coincidentes | Datos sintéticos, no base real de defensa. |
| Migraciones sobre esquema actual | CUMPLE | Siete SQL versionados | Dos pasadas sobre restauración temporal | Sin errores | No demuestra todas las versiones históricas. |
| JUnit de lógica preservadas y ampliadas | CUMPLE | `logica/src/test` | 358 JUnit de lógica | Aprobadas | No se eliminaron tests fallidos. |
| Cobertura lógica ≥80 % por JaCoCo | CUMPLE | Todas las clases de `logica` | Agente real y análisis de clases Java 25 | **85,28 %** | 2103/2466 líneas, sin exclusiones de seed/DTO. |
| IntelliJ Run with Coverage | NO VERIFICADO | IDE | Agente JaCoCo extraído del IDE, sin ejecutar su interfaz | Pendiente | No se presenta JaCoCo como resultado del botón del IDE. |
| Java SE 25 | CUMPLE | JBR 25.0.3 de IntelliJ | Build Maven completo con `release 25` y JUnit con ese runtime | Aprobado | Build ejecutado por el usuario en PowerShell normal, log aportado. |
| Build Maven y artefactos finales | CUMPLE | Reactor y plugins | Segundo `clean package`: BUILD SUCCESS; inspección y tres IT finales | Paquetes completos probados | WAR contiene exactamente la clase PDF compilada y probada; hashes finales registrados. |
| Tomcat 11 instalado y arranca | CUMPLE | Tomcat 11.0.29 | Base aislada, JDK 25 y WAR completo | HTTP real aprobado | Se probaron también parada y despliegue. |
| JAR ejecutable y WAR desplegado sin IDE | CUMPLE | Shade/WAR/IT | Tres IT sobre los paquetes Maven completos | Aprobadas | Central como proceso JAR independiente y Tomcat con WAR real; todo en loopback. |
| Tailscale instalado/autenticado/conectividad/ACL | NO VERIFICADO | Red | No se repite en esta revisión | Diferido a mañana por el usuario | Instalado después de la inspección inicial; no se acreditan pares, permisos ni acceso remoto. |
| Seguridad de rol, parámetros, XSS, CSRF | PARCIAL | Filtros, lógica, JSP y tests | JUnit/Node/autorización SOAP | Aprobados casos ejecutados | Falta auditoría dinámica completa con navegador visual; PDF real y recorrido HTTP sí ejecutados. |
| Datos reproducibles Lab 3 | CUMPLE | `CargarDatosLab3`, `DatosLab3Test` | PostgreSQL, reinicialización y CLI real | Aprobados | Solo altas con prefijo nuevo y base `_test`/`_demo`. |
| Análisis, diseño, secuencias y guía de red | CUMPLE | Documentos enlazados abajo | Diagramas cotejados con clases y métodos | Agregados | Aún debe revisarlos el equipo para su entrega. |
| Simulación de defensa de principio a fin | PARCIAL | Cuatro nodos, JAR/WAR, teléfono | Paquetes reales probados localmente | Falta distribución física | Redes y teléfono diferidos a mañana. |

## C. Errores y riesgos por prioridad

| Prioridad | Hallazgo | Estado |
| --- | --- | --- |
| CRÍTICO | No se confirmó un defecto crítico reproducible abierto en los recorridos ejecutados | Esto no certifica los recorridos no ejecutados. |
| ALTO | Build desde el agente bloqueado; PDFBox inicialmente ausente | Resuelto para la entrega: usuario completó dos builds con Java 25; paquetes finales inspeccionados y probados. El bloqueo del entorno del agente no implica un fallo del build normal. |
| ALTO | Asistencia tenía restricciones de fecha/estado no exigidas por §6.3 | Corregido y probado con registro futuro y archivado. |
| ALTO | Registro propio archivado sin asistencia rompía la consulta al pedir detalle público oculto | Corregido: se consulta el registro autorizado sin otra consulta pública innecesaria. |
| MEDIO | URL YouTube inválida se trataba como servicio caído | Corregido; regresión primero falló y luego pasó por HTTPS/PostgreSQL. |
| MEDIO | Cliente Eventos ignoraba archivo externo seleccionado | Corregido y probado para ubicación habitual, alternativa y archivo inexistente. |
| MEDIO | Cargador anterior no preparaba escenarios nuevos Lab 3 | Nuevo cargador adicional probado, sin reemplazar datos anteriores. |
| MEDIO | Archivadas ausentes en perfil público y ausencia de buscador en cabecera móvil | Corregidos. Perfil y cabecera renderizados por JSP real; permisos comprobados también por PostgreSQL/SOAP. Validación visual pendiente. |
| ALTO | PDF fallaba con `Ł`/`Ω` y saltos de línea en los datos | Reproducidos dos fallos reales. Corregidos con Liberation Sans embebida desde PDFBox y normalización de controles; seis tests aprobados. |
| BAJO | Guías enseñaban restricciones de asistencia y limitación de configuración ya corregidas | Actualizadas. |

## D. Correcciones realizadas y pruebas posteriores

| Archivo | Problema/cambio | Prueba | Resultado |
| --- | --- | --- | --- |
| `webapp/.../remoto/ConfiguracionWeb.java` | Ubicación compartida del archivo `.properties` | `ConfiguracionClienteEventosTest` | 3 aprobadas. |
| `webapp/.../soap/ClienteEventosCentral.java` | Respeta `eventos.web.config`; conserva ruta habitual | Misma regresión y pruebas de cliente SOAP | Aprobadas. |
| `servidor-central/.../soap/PortalPublicador.java` | Reconoce validación de `VideoYoutube` como negocio | `videoInvalidoDevuelveValidacionDeNegocioSinGuardarEdicion` | Falla reproducida antes; aprobada después. |
| `logica/.../ManejadorRegistros.java` | Quita fecha/estado adicionales; mantiene registro propio, bloqueo y confirmación única | `AsistenciaYArchivoTest`, integración de asistencia/archivo | Aprobadas. |
| `webapp/.../ConsultaRegistroServlet.java` y `MovilRegistrosServlet.java` | Ofrecen asistencia para registro propio pendiente sin consultar detalle público de edición | `MovilVideoYAsistenciaTest`, pruebas de permisos | Aprobadas; fallos reales de consulta siguen propagándose. |
| `webapp/.../movil/registros.jsp` | Elimina aviso de fecha de inicio que ya no corresponde a la letra | Revisión de plantilla y regresión del servlet | Lógica aprobada; render completo pendiente. |
| `logica/.../datos/CargarDatosLab3.java` | Conjunto adicional reproducible, prefijo nuevo, sin contraseña fija ni borrados | `DatosLab3Test` y ejecución real del `main` | Aprobados; no imprime la clave. |
| `logica/.../Sistema.java`, `WEB-INF/views/usuario.jsp` | Archivadas en perfil público con nombre/estado/fecha, sin enlace roto ni registros privados | `AsistenciaYArchivoTest`, Portal persistente y render JSP público/propio | Aprobados; ingresadas siguen ocultas al visitante. |
| `movil/fragmentos/cabecera.jsp`, `css/movil.css` | Buscador GET reutiliza `/buscar`; campo y navegación pueden reacomodarse | JSP real, recursos Bootstrap y formulario renderizado | HTTP 200; viewports aún pendientes por fallo de Chrome. |
| `GeneradorConstanciaPdf.java` | Helvetica rechazaba texto extendido y saltos de línea; usa fuente TTF incluida en PDFBox y normaliza controles | Seis pruebas PDFBox reales, dos fallaron antes de la corrección | Aprobadas; sin depender de fuentes instaladas en el sistema. |
| Tests de asistencia, móvil, configuración, datos y Portal persistente | Amplían regresión y ajustan expectativas a la letra original | Suite final detallada en E | Aprobadas. |
| README y guías | Instalación, datos, red y explicación del recorrido real | Revisión de comandos y correspondencia con fuente | Guías agregadas; pruebas manuales pendientes identificadas. |

No se cambiaron nombres de clases existentes ni se eliminaron funcionalidades. No se reemplazaron fallos por datos simulados ni se desactivaron aserciones para aprobar.

## E. Resultados de testing

### Ejecuciones finales

| Grupo | Ejecutadas | Aprobadas | Fallidas | Evidencia en `.build-cache/auditoria/` |
| --- | ---: | ---: | ---: | --- |
| JUnit lógica y Central, Java 25 con agente JaCoCo | 376 | 376 | 0 | `regresion-final.log` |
| JUnit web, configuración, permisos, sesiones, móvil y transporte | 182 | 182 | 0 | `regresion-final.log` |
| JUnit SOAP HTTPS → Sistema → JPA → PostgreSQL reales | 18 | 18 | 0 | `regresion-final.log` |
| JUnit contratos regenerados WSDL/XSD | 1 | 1 | 0 | `wsdl-final.log` |
| JUnit PDF/servlet, incluidas tres regresiones nuevas | 6 | 6 | 0 | `pruebas-pdf-limites.log`; fallos previos en `pdf-rojo.log` y `pdf-limites-rojo.log`. |
| JUnit IT JAR, instalación limpia, Tomcat/HTTP y PostgreSQL inaccesible | 4 | 4 | 0 | `it-final-ampliada.log`, `it-sin-base.log`; 99 solicitudes HTTP. |
| **Total JUnit distintas ejecutadas** | **587** | **587** | **0** | No suma las repeticiones de la misma prueba. |
| JavaScript con Node | 62 | 62 | 0 | `node --test webapp/src/test/js/*.test.cjs` |
| Ejecutores Swing `main`, sin ventanas | 3 | 3 | 0 | Patrocinio/actor, altas atómicas, ranking/EDT. |

Los 376 JUnit son **358 de lógica y 18 de Central**. Los tres ejecutores Swing no se contaron como JUnit. Las pruebas de servlet y transporte con dobles de lógica no sustituyen las 18 integraciones persistentes reales.

Las tres pruebas PDF originales y las tres IT inicialmente bloqueadas ya se ejecutaron; se agregaron otras tres regresiones PDF. Los nombres `*IT` no forman parte automáticamente de la selección habitual `*Test` de Surefire: aquí se seleccionaron explícitamente. Se mantienen pendientes las verificaciones físicas y visuales indicadas en F.

Los fallos anteriores al arreglo están preservados en `registro-rojo.log` y `pruebas-integracion-rojo.log`. El primer error de compilación del test nuevo de datos, por usar un accessor de DTO en lugar del del record lógico, se corrigió antes de la ejecución final. El descubrimiento fallido de esa prueba no se contó como ejecución aprobada.

### Cobertura real de lógica

**JaCoCo 0.8.15.202606040825: 2103/2466 líneas, 85,28 %.** Medición repetida después de la corrección del listado público.

Se instrumentaron y analizaron todas las clases de los paquetes de producción de `logica`: `clases`, `datos`, `datatypes`, `excepciones`, `implementacion`, `interfaces`, `manejadores`, `persistencia` y `seguridad`. No se incluyeron Central/Web/Swing en el denominador; tampoco se excluyeron cargadores ni datatypes para mejorar el porcentaje. La ejecución CLI posterior del seed no se sumó a esta medición JUnit.

Informes finales: [HTML](.build-cache/auditoria/cobertura-final/index.html), [XML](.build-cache/auditoria/cobertura-final/jacoco.xml), `cobertura-final.log` y `regresion-final.exec`.

| Clase | Líneas sin cubrir / total | Cobertura |
| --- | ---: | ---: |
| `datos.CargarDatosPrueba` | 82/82 | 0,00 % |
| `manejadores.ManejadorRegistros` | 40/168 | 76,19 % |
| `implementacion.Sistema` | 21/805 | 97,39 % |
| `datos.CargarDatosLab3` | 24/67 | 64,18 % |
| `datatypes.DtConstanciaAsistencia` | 18/36 | 50,00 % |
| `datatypes.DtEdicion` | 19/48 | 60,42 % |
| `clases.Edicion` | 17/67 | 74,63 % |
| `datatypes.DtAltaTipoRegistro` | 16/16 | 0,00 % |
| `datatypes.DtRegistro` | 15/38 | 60,53 % |

Para la medición exigida en IntelliJ: seleccionar JDK 25, configurar las cinco variables `EVENTOS_DB_*` para **eventosuy_test**, verificar previamente la base efectiva y ejecutar los tests de `logica` con **Run with Coverage**. Medir exclusivamente producción de `logica`, sin excluir clases del denominador. Guardar el informe del IDE. El porcentaje anterior es de JaCoCo; no se inventó una ejecución de la interfaz IntelliJ.

### Compilación, dependencias y empaquetado

Se intentó Maven offline con repositorio local de caché tanto en JDK 26 como en **JBR 25.0.3**, encontrado en IntelliJ. El intento final `mvn -o -Dmaven.repo.local=.build-cache/repository -DskipTests package` terminó en **BUILD FAILURE** en `logica`, por acceso denegado a `jakarta.persistence-api-3.2.0.jar`. El problema de resolución de rutas reales/ZipFS también se reprodujo con `javac` fuera de Maven. Evidencia: `maven-java25.log`.

Para avanzar sin cambiar POM ni permisos se creó un adaptador temporal de `JavaCompiler` que lee dependencias disponibles y evita esa resolución problemática. Con **JDK 25 y `--release 25`** se compilaron 124 fuentes de contratos/lógica/Swing/Central, 57 fuentes de pruebas/soporte y 178 fuentes web incluidas las generadas. También se compilaron las pruebas web disponibles. Evidencia: `compilacion-*-java25.log`.

Posteriormente el usuario ejecutó `mvn -Dmaven.repo.local=.build-cache/repository -DskipTests clean package` en su PowerShell normal con JBR 25.0.3: **BUILD SUCCESS** de los seis proyectos del reactor, en 41,527 s según el log aportado. Se descargó PDFBox 3.0.3 y sus dependencias. Se compilaron las 180 fuentes web, incluidas ambas clases PDF, y las 35 fuentes de pruebas web. No se afirma que ese comando ejecutó tests: indicaba `Tests are skipped`.

Se inspeccionaron los ZIP resultantes y se ejecutaron los tests desde las clases compiladas por Maven. Se confirmó `Main-Class: central.Main`, Central/Swing/Hibernate/driver/descriptor presentes en el JAR, y escritorio/móvil/PDF/Bootstrap presentes en el WAR sin dependencias de lógica/JPA/JDBC. Tras corregir el PDF, el usuario repitió `clean package`: **BUILD SUCCESS**, seis módulos aprobados, 35,875 s y finalización `2026-11-21T12:45:42-03:00` según su log. SHA-256 de los paquetes finales probados:

- `servidor.jar`: `BDE0C79B416E06F47AED92FB1A273E12E39C237F5C7E210DE9117AFC0D5F7B79`.
- `web.war`: `94E592202A06AB6301D62A88AC82DDD89FBB4F60E5AD3834F49147164A05767A`.

Durante los tests ampliados se reprodujeron dos fallos PDF y se corrigió el generador. El segundo build incluyó esa corrección; se comparó por SHA-256 la clase `GeneradorConstanciaPdf.class` del WAR con la clase compilada por Maven y resultaron idénticas. Pasaron de nuevo los cinco JUnit PDF y las tres IT sobre los paquetes finales. La IT de Tomcat se amplió posteriormente solo en el código de prueba, sin cambiar producción ni los paquetes, y se recompiló con Java 25 antes de ejecutarla. No se editaron archivos dentro del WAR para aparentar un build nuevo.

Los logs Maven aportados incluyen advertencias Shade sobre recursos repetidos (licencias, manifiesto, avisos y `META-INF/hk2-locator/default`) y advertencias de operaciones genéricas no comprobadas en tests. Las ejecuciones de pruebas también informan SLF4J sin implementación en el classpath del runner. No se ocultaron estas advertencias ni se aplicaron exclusiones ciegas; los servicios y paquetes ejecutados aprobaron las verificaciones indicadas. El intento anterior del agente que falló en `wsgen` queda en `maven-tras-pdf.log`, pero no invalida los builds completos del usuario.

### PostgreSQL, SOAP y Tomcat

Se creó PostgreSQL **17.11** aislado en `.build-cache/auditoria/postgres`, escuchando solo en **127.0.0.1:55432**. La base `eventosuy_test` y esquema `public` fueron confirmados por SQL/JDBC. No se usó la base habitual del puerto 5432. La autenticación de confianza de esta instancia desechable está limitada a loopback; no es una configuración recomendada para despliegue.

Se probaron persistencia, rollback, restricciones, concurrencia, seguimiento, imágenes, sesiones, visitas, archivo y asistencia. La integración real prueba permisos y datos de constancia después de reinicializar JPA. Algunos recorridos de visitas usan procesos separados. No se acredita una defensa distribuida por esto.

Un respaldo `pg_dump -Fc` se restauró sin errores en `eventosuy_auditoria_restore`; en ese punto ambas bases tenían 348 usuarios, 179 eventos, 175 ediciones y 38 registros. Los tests y el nuevo seed posteriores agregaron más datos; esos conteos corresponden al punto del respaldo. Se ejecutaron dos veces los **siete** SQL versionados sobre la restauración, con `ON_ERROR_STOP=1`, sin errores. No se aplicaron a datos reales ni se infiere compatibilidad con cualquier versión histórica.

Tomcat **11.0.29** se inició con JDK 25 usando un `CATALINA_BASE` temporal independiente, sin modificar la instalación. Escuchó en loopback, devolvió HTTP 404 como corresponde a una instancia sin aplicaciones y se detuvo correctamente. Tras detenerlo, la conexión fue rechazada. Esto comprueba arranque/parada del contenedor, **no despliegue de Eventos UY**. Evidencia: `tomcat-arranque.log`, `tomcat-parada.log`, `tomcat-http.html`.

En la continuación se desplegó un contexto temporal de **pruebas de componentes de presentación**, con copias de las JSP de producción y las bibliotecas JSTL/Bootstrap reales. Jasper compiló las JSP. Se comprobó por HTTP 200 la cabecera móvil y su buscador, los recursos CSS/JS locales, el perfil público/propio con edición archivada, el escape HTML de un nombre con `<` y los enlaces permitidos. Los datos de esa prueba de plantilla son fixtures, no una sustitución de Central: la autorización y persistencia del perfil también se probaron por SOAP/PostgreSQL. Evidencia: `pruebas-jsp.log`, `perfil-publico.html`, `perfil-propio.html`, `tomcat-componentes.log`.

Se intentó automatizar Chrome sin modificar su sandbox. Chrome alcanzó a crear su endpoint de depuración, pero terminó con código `-2147483645` después de fallos de acceso del proceso GPU (`-1073741790`). Por ello no se aprobaron los seis viewports. Se conserva el runner `.build-cache/auditoria/probar-cabecera.cjs` y el diagnóstico `chrome-diagnostico.log`; requiere un Chrome ejecutable en el entorno normal. No se desactivaron protecciones para forzar una aprobación.

Tailscale: no se detectaron comando, servicio ni ejecutable en las ubicaciones habituales consultadas. No se pudieron comprobar autenticación, IP, pares, ACL, `ping`, SOAP remoto ni acceso del teléfono. No se instaló ni configuró nada.

### IT completas sobre los artefactos Maven

Las tres IT se ejecutaron con JDK 25, PostgreSQL temporal y `TOMCAT_HOME` apuntando a Tomcat 11.0.29; la ejecución final dio **3 aprobadas, 0 fallidas** en 153,191 s:

1. `CentralJarArranqueIT`: proceso `java -jar servidor.jar --sin-swing` sin fuentes ni variables `EVENTOS_DB_*` en el hijo; configuración externa y PostgreSQL reales; consulta por cliente SOAP con validación TLS.
2. `InstalacionLimpiaIT`: preparador del JAR, esquema nuevo aislado, rechazo de segunda preparación, SQL de migración, arranque/reinicio y lectura persistida sin IDE. Solo se eliminó su propio esquema sintético al terminar.
3. `TomcatPortalHttpsIT`: WAR completo desplegado en `CATALINA_BASE` temporal, sin variables JDBC en Tomcat; **69 solicitudes HTTP** para consultas, login/logout, roles, CSRF, altas multipart con imagen, modificaciones y cambio de contraseña, patrocinios y registros. Se agregó login móvil por correo, rechazo de contraseña incorrecta y de organizador, consulta móvil con video normalizado, búsqueda en cabecera, asistencia única, ausencia de segunda acción, descarga PDF real con `Łucía Pérez`, rechazo de PDF sin asistencia/ajeno y logout móvil. Tras apagar Central se comprobó HTTP 503, sin respaldo local en Tomcat.

Estas IT finales acreditan ejecución de los paquetes regenerados, procesos y comunicación real en loopback; no sustituyen otros equipos, el teléfono ni una medición visual de los tamaños de pantalla. No hace falta repetir la compilación por la ampliación exclusiva del test.

## F. Pruebas pendientes y causas

1. **Cambio de IP sin recompilar:** dos destinos reales, hashes iguales, edición de `.properties`, reinicio y caso de escritura desde PC Web. No reemplazarlo por una lectura de configuración.
2. **Red y fallos físicos:** LAN, Tailscale cuando esté autorizado, firewall/ACL, SAN/truststore, IP/puerto/ruta incorrectos, caída y recuperación de PostgreSQL/Central/Web y desconexión segura de un nodo de prueba.
3. **Interfaz visual:** navegador sobre WAR completo, seis viewports solicitados y teléfono físico. Los recorridos HTTP ya pasan; faltan reproducción real de YouTube, menús táctiles, ausencia de overflow, foco/formularios, lectura visual del PDF y navegación en dispositivos.
4. **IntelliJ Coverage:** medición desde su interfaz y evidencia del porcentaje de lógica. El reactor completo con perfil Maven de cobertura tampoco fue acreditado; sí se midió cobertura real de lógica por agente: 85,28 %.
5. **Datos definitivos de defensa:** cargar el conjunto nuevo en una base demo independiente, conservar los nicknames/contraseña privada y ensayar respaldo/restauración de esa base. Los datos temporales de JUnit no sustituyen esa preparación.

Comando para seleccionar IT, después de generar los paquetes y con las variables de pruebas correctas:

```powershell
$env:TOMCAT_HOME = 'C:\ruta\apache-tomcat-11'
mvn -pl webapp -am '-Dtest=CentralJarArranqueIT,InstalacionLimpiaIT,TomcatPortalHttpsIT,CentralSinBaseIT' '-Dsurefire.failIfNoSpecifiedTests=false' test
```

## G. Preparación para la defensa

Guías actualizadas:

- [Instalación y despliegue por equipo](README_DESPLIEGUE_LAB3.md).
- [Propiedades, TLS, Tomcat y PostgreSQL](docs/CONFIGURACION_EJEMPLOS.md).
- [Ensayo local con un comando](docs/ENSAYO_LOCAL.md).
- [Datos reproducibles de Lab 3](docs/DATOS_DEMO_LAB3.md).
- [LAN, Tailscale, comandos y fallos/recuperación](docs/PRUEBAS_RED_LAB3.md).
- [Modelo y secuencias de la implementación](docs/lab3/ANALISIS_DISENO_ACTUAL.md).
- [Arquitectura y preguntas de defensa](docs/lab3/DEFENSA_ARQUITECTURA.md).
- [Checklist](docs/DEFENSA_CHECKLIST.md).

Orden resumido: Java 25 y Maven → tests con `eventosuy_test` → paquetes completos → base demo y respaldo → configurar Central y TLS → ejecutar `java -jar servidor.jar` → comprobar los tres WSDL desde PC 2 → configurar confianza y desplegar `web.war` en Tomcat → escritorio y celular → casos de cada rol → ranking Swing → reinicios y persistencia → fallos y recuperación.

En la ubicación habitual `~/.eventosUy` consultada solo se encontraron los archivos anteriores `usuarios-central.properties` y `usuarios-web.properties`, no los nombres vigentes `central.properties` y `web.properties`. Deben prepararse desde las plantillas en la cuenta que ejecutará cada proceso. No se escribieron archivos privados fuera del espacio de trabajo ni se inventaron direcciones de los equipos remotos.

La prueba remota debe registrar IP LAN/Tailscale reales por nodo. La única dirección de pruebas acreditada aquí es loopback; no se propone usarla entre computadoras. El teléfono necesita pertenecer a la LAN adecuada o estar autenticado y autorizado en Tailscale, según el ensayo elegido.

## H. Evaluación final

**Verificado:** lógica/regresión con PostgreSQL real, cobertura superior al 80 %, contratos y transporte SOAP HTTPS, permisos y sesiones, correcciones descritas, datos reproducibles, PDFBox real, JAR ejecutable y WAR completo final en Tomcat con **99 solicitudes HTTP**, incluido móvil/PDF y nuevos casos Lab 3. No se observan fallos abiertos en las 587 JUnit y 62 pruebas JS ejecutadas.

**No acreditado:** recorrido visual del WAR en seis tamaños y teléfono, medición en la interfaz IntelliJ, conectividad entre cuatro dispositivos, Tailscale y defensa física completa sin IDE.

Los procesos temporales de QA se detuvieron; sus datos y evidencia quedaron en `.build-cache/auditoria/`, ignorado por Git. El ensayo local independiente se describe en la actualización al final. No se detuvieron servicios compartidos del usuario.

La prioridad antes de la entrega es preparar la base/configuración de defensa y ensayar los cuatro dispositivos con estos paquetes. Hasta entonces no corresponde afirmar cumplimiento físico total.

## Actualización: PDF con campos largos y ensayo local

Se reprodujo un tercer fallo PDF: datos válidos de longitud máxima, incluso sin espacios, se dibujaban fuera del margen. `GeneradorConstanciaPdf.Escritor` ahora divide por ancho real de los caracteres y agrega páginas cuando corresponde, sin recortar los valores. La nueva prueba verifica contenido completo y límites de cada carácter en todas las páginas con PDFBox. Resultado: **6/6 pruebas PDF aprobadas**, evidencia `pruebas-pdf-limites.log`; fallo previo en `pdf-limites-rojo.log`.

Se compiló la corrección con el compilador Java 25 y el adaptador de lectura de dependencias requerido por el entorno restringido. Se ejecutó el objetivo real `maven-war-plugin:3.4.0:war` offline con los módulos ya construidos: **BUILD SUCCESS** (`empaquetado-pdf-largo.log`). El hash WAR registrado arriba corresponde a este empaquetado actualizado; el JAR permanece sin cambios. No se atribuye este empaquetado a un tercer `clean package` completo.

Se agregó `scripts/EnsayoLocal.ps1` y la guía `docs/ENSAYO_LOCAL.md`. El script prepara un clúster PostgreSQL independiente con autenticación SCRAM, base `eventosuy_ensayo_demo`, secretos generados y configuración externa dentro de `.build-cache/ensayo-local/usuario/.eventosUy`. Inicia el JAR y el WAR reales con certificados locales. Los puertos escuchan únicamente en loopback; no modifica servicios, reglas de red ni el perfil habitual. `.build-cache/` quedó ignorado también en `.gitignore` para evitar incorporar credenciales accidentalmente.

Primera ejecución real: creación de base/esquema, índices, carga de tres usuarios y escenarios, publicación de los tres WSDL, Tomcat 11 y respuestas HTTP 200 de `/web/inicio`, `/web/movil` y `/web/login`. Consulta directa al JAR recuperó un evento público y una categoría. Se ejecutó `Detener`: paró los procesos propios y conservó la base. No se declara validación visual de Swing ni de navegador por estas comprobaciones.

Durante la repetición del recorrido HTTP completo se observaron dos fallos de tiempo: arranque Tomcat mayor a 50 segundos y primera compilación de JSP mayor a 25 segundos. Se conservaron ambos logs (`tomcat-pdf-largo.log`, `tomcat-pdf-largo-repetido.log`) y se ajustó exclusivamente la tolerancia de la prueba a 150 segundos de arranque y 60 por solicitud. Las aserciones funcionales, permisos y contenido no se eliminaron ni cambiaron. El resultado de la repetición final se registra a continuación.

Repetición final del WAR actualizado: **TomcatPortalHttpsIT aprobada, 69 solicitudes HTTP verificadas, 0 fallos, 83,625 s** (`tomcat-pdf-largo-final.log`). Incluye la descarga y lectura PDF reales después de la corrección de márgenes. Se compararon ambas clases PDF del WAR, incluida la clase interna Escritor, con las clases compiladas y probadas: SHA-256 idénticos. Total acumulado de pruebas distintas aprobadas: **585 JUnit**, sin sumar repeticiones. Los dos intentos con timeout anteriores permanecen documentados.

Ensayo reiniciado y verificado: se conservaron exactamente los tres usuarios demo, sin otra carga. Un recorrido HTTP adicional inició sesión con el asistente generado tanto en escritorio como en móvil y obtuvo HTTP 200 en perfil/registros propios. La portada reiniciada también respondió HTTP 200. En el primer acceso frío de ese recorrido hubo un 503 transitorio; al repetir con los servicios inicializados pasaron ambas sesiones. Se mantiene como riesgo de arranque y no se oculta ese primer resultado. No se alteraron los tiempos de producción para obtener una respuesta positiva.

Al cerrar esta revisión quedan activos únicamente los procesos propios del ensayo local, con escritorio en `http://127.0.0.1:59037/web/inicio` y móvil en `http://127.0.0.1:59037/web/movil`. Central se inició con `--sin-swing` para la comprobación automatizada; la ventana Swing no se acredita en esta ejecución. Usar `scripts/EnsayoLocal.ps1 -Accion Estado` para confirmar puertos actuales y `-Accion Detener` para parar y conservar los datos. La siguiente ejecución puede elegir otros puertos. El PostgreSQL temporal de QA en 55432 quedó detenido.
Acceso Wi-Fi preparado posteriormente: Tomcat del ensayo escucha en `192.168.1.93:57867` y `/web/movil` respondió HTTP 200 desde esta PC usando esa dirección. SOAP y PostgreSQL siguen limitados a loopback. Se agregó `-DireccionWeb` al script para elegir una interfaz explícita; no se modificó el firewall. Falta confirmar el acceso desde el teléfono: HTTP local a una IP Wi-Fi no acredita el trayecto entre dispositivos. La IP Tailscale observada con `ipconfig` fue `100.87.5.46`; su API local rechazó acceso desde el agente y no se acreditó autenticación, pares ni conectividad VPN. Las URLs anteriores de loopback fueron reemplazadas por el nuevo arranque.
## Revisión nocturna del 22/11/2026: correcciones y evidencia

Tailscale y el ensayo entre dispositivos quedan diferidos por pedido del usuario; no se instalaron herramientas ni se cambiaron políticas de red durante esta revisión.

### Fallos nuevos reproducidos

| Prioridad | Archivo | Problema observado | Corrección | Verificación |
| --- | --- | --- | --- | --- |
| MEDIO | `webapp/src/main/webapp/WEB-INF/views/buscar.jsp` | HTTP 200 con resultados, pero sin viewport, CSS Bootstrap ni JS del menú; el cabezal quedaba sin sus recursos y el menú no podía funcionar | Incorporar recursos locales, viewport, etiquetas de formulario y columnas adaptables; conservar escape de texto, filtros y enlaces | `busqueda-antes.html` acredita la ausencia; la IT ampliada comprueba recursos y orden de resultados del WAR real |
| MEDIO | `logica/src/main/java/implementacion/Sistema.java` | `listarEventosPublicos` consultaba nuevamente cada evento para leer un estado ya incluido en su datatype | Filtrar el estado de los datatypes obtenidos por el listado, sin releer cada entidad | Nueva JUnit con Hibernate/PostgreSQL real: **682 consultas JPQL antes y 1 después**. `listado-rojo.log`, `listado-verde.log`; 4/4 casos de búsqueda/finalización/video aprobados tras corregir |
| BAJO | `docs/lab3/GUIA_PRUEBAS_COBERTURA.md` | Comando de navegación apuntaba a una carpeta personal de otro desarrollador | Indicar que los comandos se ejecutan desde la raíz real del repositorio | Revisión de documentación |

La prueba nueva de consultas verifica el comportamiento público y un límite constante de consultas JPQL; no usa mocks ni elimina datos. No se atribuyen todos los timeouts anteriores a esa ineficiencia: un intento abarcó una interrupción prolongada del entorno y debe distinguirse del tiempo de CPU.

### Ejecuciones y empaquetado

- La primera regresión encontró 582 JUnit: 581 aprobadas y una fallida porque el ejecutor local se lanzó desde la raíz y la comprobación WSDL espera el directorio `webapp`. Al ejecutarla desde ese módulo, **1/1 aprobada** (`wsdl-regresion.log`). No se modificó ni desactivó la comprobación del contrato.
- **62/62 pruebas JavaScript aprobadas**, sin omisiones (`regresion-js.log`).
- **Tres verificadores Swing aprobados**: ranking/EDT/reintento; actor de patrocinio; guardado compuesto e imagen. Evidencia `EventosMasVisitadosPanelTest-regresion.log`, `AltaPatrocinioPanelActorTest-regresion.log`, `AltaEventoEdicionAtomicaTest-regresion.log`.
- La cobertura anterior a la optimización se volvió a medir: **2106/2469 líneas, 85,30 %**, sobre todas las clases de `logica`, sin excluir datatypes ni cargadores. `cobertura-regresion.log` e informe HTML/XML en `cobertura-regresion/`. Se recalcula después del cambio de lógica.
- La vista de búsqueda se empaquetó con `maven-war-plugin:3.4.0:war`: **BUILD SUCCESS** (`empaquetado-busqueda.log`).
- Tras compilar `Sistema.java` con Java 25, se ejecutaron los plugins Maven JAR y Shade con la configuración del proyecto: **BUILD SUCCESS** en lógica y Central (`empaquetado-logica-listado.log`, `empaquetado-central-listado.log`). El JAR conserva `Main-Class: central.Main`, el descriptor JPA, el proveedor JPA y todas las dependencias. No se editaron clases directamente dentro de ZIP/JAR/WAR.
- El compilador normal del entorno del agente continúa necesitando el adaptador de lectura de dependencias ya documentado. El cambio no modifica POM ni contratos SOAP. No se afirma una nueva ejecución completa de `clean verify` mediante el reactor por el agente.
- La IT completa se amplió para comprobar seguimiento por asistente y organizador, dejar de seguir, autorrelación rechazada y solicitudes repetidas sin duplicar la relación, búsquedas/órdenes/enlaces, conteo desde escritorio/móvil, alta HTTP con video y rechazo de URL inválida, archivo válido e inválido, permisos, conservación de registros y finalización. Se registra su resultado final después de la regresión.

### Limitación visual reiterada

Se intentó Microsoft Edge headless con perfil temporal independiente y sin desactivar su sandbox. El proceso gráfico falló por acceso denegado (`edge-visual-error.log`). No se acreditan visualmente los seis tamaños, funcionamiento táctil ni reproducción de YouTube. Bootstrap/JSP/HTTP y los atributos del iframe se verifican por separado.
Regresión posterior a la optimización: **582/582 JUnit aprobadas, 0 omitidas y 0 fallos**, en 256,739 s (`regresion-final.log`); la comprobación adicional de contratos fue **1/1 aprobada** desde `webapp` (`wsdl-final.log`). La nueva prueba de consultas está incluida en esas 582. Cobertura final: **2103/2466 líneas, 85,28 %**, medida sobre las clases actuales de `logica/target/classes` con el agente real JaCoCo 0.8.15. El HTML/XML está en `.build-cache/auditoria/cobertura-final/`; resumen por clase en `cobertura-final.log`.

Clases con cobertura menor al 80 % que siguen incluidas en el denominador: `CargarDatosPrueba` 0 %, `ManejadorRegistros` 76,19 %, `CargarDatosLab3` 64,18 %, `DtEdicion` 60,42 %, `DtConstanciaAsistencia` 50 %, `Edicion` 74,63 %, `DtAltaTipoRegistro` 0 %, `DtRegistro` 60,53 %, `PrepararEsquemaNuevo` 56,52 %, `Evento` 71,43 %, `TipoRegistro` 69,57 % y `Categoria` 50 %. El requisito es el porcentaje global de líneas de lógica, no que cada clase alcance 80 %. No se excluyeron esas clases para elevar la medición. La medición mediante el botón de IntelliJ sigue pendiente.
### Cierre de la revisión nocturna

- **587 JUnit distintas aprobadas**, ninguna omitida en las ejecuciones finales: 582 en `regresion-final.log`, 1 de contratos en `wsdl-final.log`, 3 IT en `it-final-ampliada.log` y 1 IT en `it-sin-base.log`. No se suman las repeticiones ni los ejecutores `main` como JUnit.
- Las tres IT de paquetes pasaron en **144,670 s**, con **99 solicitudes HTTP verificadas**. El recorrido ampliado aprobó los recursos de búsqueda, sus órdenes y enlaces; seguimiento sin duplicar relaciones; visitas de escritorio y móvil; video válido/inválido; archivo propio válido, rechazo de asistente y edición que termina hoy, invisibilidad pública y conservación del registro; finalización y exclusión pública. Se mantienen las comprobaciones previas de asistencia/PDF, CSRF, roles, sesiones, imagen, patrocinios y error 503 al apagar Central.
- `CentralSinBaseIT` pasó en **28,085 s**: JAR real con destino PostgreSQL inaccesible, salida no exitosa, sin publicación WSDL, causa de conexión real y sin claves sintéticas en el log. No se detuvo PostgreSQL compartido para provocar ese fallo.
- **62/62 JavaScript** y los **tres verificadores Swing** aprobados. Los verificadores se repitieron también después de empaquetar el nuevo JAR (`*-final.log`). No equivalen a inspección visual completa de ventanas.
- Cobertura final: **85,28 % (2103/2466 líneas de lógica)**. El umbral global de 80 % se supera; la medición visual en IntelliJ permanece pendiente.
- Se comparó por SHA-256 `implementacion/Sistema.class` dentro del JAR con la clase compilada y probada: idénticas. Hash del JAR: `BDE0C79B416E06F47AED92FB1A273E12E39C237F5C7E210DE9117AFC0D5F7B79`. Hash del WAR: `94E592202A06AB6301D62A88AC82DDD89FBB4F60E5AD3834F49147164A05767A`.
- El ensayo demo se reinició con ambos artefactos actualizados. La búsqueda en la instancia real respondió HTTP 200 y contiene viewport, CSS Bootstrap y JS del menú. Quedó disponible localmente en `http://127.0.0.1:52979/web/inicio`; este puerto reemplaza los anteriores. El acceso remoto no se mantuvo como parte del ensayo nocturno: PostgreSQL, SOAP y Tomcat están limitados a loopback, sin modificar firewall. El clúster QA de puerto 55432 quedó detenido.

**Pendientes reales:** Tailscale y conexión entre equipos/teléfono, seis viewports con navegador funcional, recorrido visual completo de Swing, reproducción de YouTube, medición desde IntelliJ y defensa física. No se afirma cumplimiento total de esos puntos. No quedan pruebas fallidas en las ejecuciones finales descritas; los intentos fallidos anteriores permanecen documentados. No se hicieron commits ni push y no se modificó la base habitual del usuario.