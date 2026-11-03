# Análisis y decisiones de diseño del Laboratorio 2

Reconstruido del código actual para los apartados §8.1 y §8.2 de la letra. No atribuye motivaciones históricas a integrantes ni propone un rediseño. El modelo conceptual, el mapeo JPA y las secuencias se complementan: no son tres implementaciones diferentes.

## Dominio y correspondencia con JPA

El modelo conceptual 01 expresa las asociaciones válidas por los casos de uso de `Sistema`. La vista 02 representa el mapeo que realmente aceptan las anotaciones. No se presenta una validación Java como una restricción SQL. Todas las entidades están en `logica/src/main/java/clases/`.

| Clase real | Identidad/atributos persistidos | Asociaciones reales |
|---|---|---|
| Usuario abstracto | nickname; nombre, fechaAlta, correoElectronico, contraseniaHash | Imagen opcional; herencia JOINED hacia Asistente y Organizador |
| Asistente | Identidad heredada; apellido, fechaNacimiento | Institucion opcional, registros |
| Organizador | Identidad heredada; descripcion, sitioWeb | Ediciones que organiza |
| Evento | nombre; sigla, descripcion, fechaAlta | Categorias ManyToMany, ediciones, imagen opcional |
| Edicion | idNombre; sigla, fechas, ciudad, pais, estado | Organizador, evento, tiposRegistros, patrocinios, registros, imagen opcional |
| TipoRegistro | id Long generado; idNombre, descripcion, costo float, cupo int | Edicion no nula, registros y patrocinios |
| Registro | id Long generado; fechaRegistro, costo float | Asistente, edicion, tipoRegistro y patrocinio opcional |
| Patrocinio | codigoPatrocinio; fecha, montoAporte float, cantRegistrosGrat int, nivel | Institucion, edicion, tipoRegistro |
| Institucion | nombre; descripcion, sitioWeb | Asistentes, patrocinios, imagen opcional |
| Categoria | nombre | Padre opcional, subcategorias, eventos |
| Imagen | id Long generado; tipoContenido, contenido byte[] | Los propietarios mantienen la referencia; no tiene un atributo propietario |

Son 11 entidades y 12 tablas: `evento_categoria` materializa la relación ManyToMany. Las colecciones están representadas por asociaciones, no como atributos escalares adicionales. `ConImagen` es una interfaz técnica común a Usuario, Evento, Edicion e Institucion, no una entidad. `EstadoEdicion` pertenece a datatypes y `NivelPatrocinio` a clases; ambos se almacenan como STRING, no como tablas de entidades.

**Multiplicidades:** un asistente puede no tener institución; una categoría puede no tener padre; una entidad puede no tener imagen. La lógica crea una edición con un evento y un organizador y exige al menos una categoría en un evento. Sin embargo, los ManyToOne de Edicion/Registro/Patrocinio no declaran `nullable=false`: la vista física muestra 0..1 y ManyToMany admite una colección vacía. Solo TipoRegistro.edicion declara explícitamente NOT NULL. Los recorridos internos directos no equivalen a los casos autorizados: ver auditoría H07/H08.

Cada referencia de imagen usa OneToOne LAZY, cascade ALL y orphanRemoval. Una tabla propietaria no comparte su imagen con otra fila de esa misma tabla; no existe una restricción de exclusividad global entre las cuatro tablas propietarias. Los casos de uso de `ManejadorImagenes.asociar` crean una Imagen nueva. Por eso los diagramas usan asociaciones y no afirman una composición exclusiva global garantizada por PostgreSQL.

**Claves y unicidad:** nickname, nombre de evento, idNombre de edición, nombre de institución/categoría y código son claves exactas. TipoRegistro y Registro tienen identidad técnica Long. TipoRegistro tiene unicidad por edición/nombre y Registro por asistente/edición; Patrocinio por institución/edición, además de su PK de código. H13 complementa el esquema con UNIQUE `lower(correoelectronico)` y `(edicion_id, lower(nombre))`. Esa unicidad insensible a mayúsculas no redefine todas las búsquedas ni las claves de instituciones/ediciones. No la crea Hibernate automáticamente; ver `../migraciones/H13-procedimiento.md`.

## Distribución y MVC

Maven produce un JAR lógico, un JAR Swing y un WAR que incluye la lógica y sus dependencias. El diagrama 12 corresponde a §3: procesos separados, misma PostgreSQL, sin RPC ni comunicación Swing → servidor web. Un singleton existe por classloader/JVM; no es un estado compartido entre procesos. Las consultas utilizan EntityManager por operación y ven los commits de la otra aplicación.

Los clientes usan `ISistema`; la implementación es `Sistema`, obtenida por `Fabrica`. No hay un segundo objeto remoto llamado ISistema: en las secuencias ambos nombres rotulan la misma instancia. Los manejadores acceden a `BaseDeDatos.getEntityManager()`. Hibernate implementa JPA y genera el SQL; las flechas a PostgreSQL resumen ese trabajo, no métodos SQL escritos en los servlets.

Servlets controlan petición, sesión, selección de vistas, acceso por rol/propiedad y PRG. Sistema conserva reglas de negocio y conversión a DTO. JSP usa JSTL/c:out y no consulta entidades/JPA. ContextoListener inicializa/cierra persistencia como ciclo de vida, no como caso de uso. La interfaz y los DTO permiten sustituir el transporte más adelante; exponer remotamente esta biblioteca requeriría una frontera autenticada adicional (H08), no bastaría publicar todos sus métodos.

## Sesión y privacidad

Login verifica hash PBKDF2, invalida sesión previa y guarda un DtUsuario. El rol se deduce del subtipo. Si nickname y correo identifican cuentas diferentes, se verifican todos los candidatos: solo una coincidencia de contraseña permite acceso; cero o varias producen el mismo error genérico. Los DTO de consulta no devuelven contraseña/hash.

FiltroAcceso mantiene rutas por rol y CSRF de sesión en POST, también multipart. Logout no modifica estado mediante GET. La propiedad se obtiene de sesión y DTO de edición, no de un nickname editable. AccesoEdiciones/AccesoPatrocinios protegen edición, registros y código; ImagenServlet verifica también visibilidad de ediciones no aceptadas. El DTO público de patrocinio contiene código null salvo para el organizador dueño.

## Imágenes y estados

Las imágenes se guardan como bytes originales mediante casos de uso de lógica, con validación real ImageIO (WEBP mediante dependencia Maven autorizada). Se limitan bytes/dimensiones/fotogramas antes de decodificar. No hay conversión ni migración de imágenes existentes. Los servlets leen Part con Subidas y no acceden al almacenamiento directamente.

Edicion nace INGRESADA. `aceptar()`/`rechazar()` solo admiten ese origen; no vuelve a Ingresada. `ManejadorEventos.cambiarEstadoEdicion` obtiene PESSIMISTIC_WRITE antes de validar y confirmar. Registro exige ACEPTADA; el organizador ve sus estados privados y el público solo ACEPTADA. Los cambios se ven entre clientes consultando la misma base.

## Transacciones y consistencia que muestran las secuencias

Perfil y contraseña se modifican juntos en un único commit del manejador de usuarios, después de verificar subtipo. Una imagen opcional se valida antes, pero se asocia después en otra transacción. Si falla esta segunda operación, el controlador comunica el perfil guardado con advertencia, actualiza sesión y redirige; no afirma rollback de un commit anterior.

Registro bloquea asistente, tipo y patrocinio cuando corresponde; comprueba correspondencia del código, usos, cupo y duplicados dentro de la transacción. Los usos son el conteo de registros asociados: no hay un contador descontado independientemente. La unicidad PostgreSQL actúa además ante carreras. Las fechas de alta/registro/patrocinio las asigna la lógica, y el costo del registro proviene del tipo o es cero por código válido.

Alta de patrocinio valida referencias, unicidad y costo de gratuitos ≤20 % del aporte; la propiedad web se verifica antes de llamar la fachada. Los diagramas no convierten varias consultas de preparación en una transacción global ni presentan el singleton como mecanismo de concurrencia. Los tipos float/int reflejan el código existente, sin introducir un cambio monetario a BigDecimal.

## Lectura y límites de los diagramas

Los DSS 03–05 son caja negra: los mensajes HTTP y acciones del administrador son eventos externos observables, no nuevas firmas Java. Se eligieron usuarios/seguridad, alta y estado de edición, registro y patrocinio porque concentran los cambios y reglas fundamentales de esta iteración. No se pretende diagramar todos los getters ni todos los casos secundarios.

Las realizaciones 06–11 abren esa caja: muestran nombres reales y resumen consultas auxiliares/retornos para mantener legibilidad. Una línea con varias operaciones separadas por punto y coma representa pasos consecutivos existentes. Los participantes JPA que agrupan EntityManager/EntityTransaction son una abreviatura de esas APIs reales, no una clase nueva. El filtro/container precede al servlet aunque no se repita una lifeline para él en cada figura. Los fragmentos alt/opt expresan caminos alternativos; no se ejecutan ambas ramas.

Cada error revierte solo la transacción activa correspondiente. Errores de negocio conservan formulario/mensaje; errores inesperados pueden dar 500 genérico según web.xml. No se afirma que todos los errores vuelvan a una JSP de formulario. Las secuencias se contrastaron con código; no sustituyen la ejecución de pruebas ni una inspección visual humana de las pantallas.
