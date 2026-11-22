# Análisis y diseño Lab 3: correspondencia con la implementación

Complementa [DEFENSA_ARQUITECTURA](DEFENSA_ARQUITECTURA.md) y representa clases y llamadas existentes. Los diagramas no acreditan despliegue físico.

## Modelo de los cambios

```mermaid
classDiagram
    Usuario <|-- Asistente
    Usuario <|-- Organizador
    Usuario "*" --> "*" Usuario : seguidos
    Organizador "1" <-- "*" Edicion : organizador
    Evento "1" <-- "*" Edicion : evento
    Asistente "1" <-- "*" Registro : asistente
    Edicion "1" <-- "*" Registro : edicion
    TipoRegistro "1" <-- "*" Registro : tipoRegistro
    class Usuario {
        String nickname
        String correoElectronico
        LocalDate fechaAlta
    }
    class Evento {
        String nombre
        boolean finalizado
        long visitas
    }
    class Edicion {
        String idNombre
        EstadoEdicion estado
        LocalDate fechaInicio
        LocalDate fechaFin
        LocalDate fechaArchivo
        String videoYoutube
        String descripcion
    }
    class Registro {
        boolean asistenciaConfirmada
    }
```

`Usuario.seguidos` se mapea a `seguimiento_usuario`, con unicidad seguidor/seguido y restricción contra seguimiento a sí mismo. `ManejadorUsuarios.cambiarSeguimiento` conserva idempotencia mediante `ON CONFLICT`; no crea relaciones duplicadas. Los estados se persisten como enumerados de texto. `Edicion.archivar` acepta exclusivamente Aceptada, con fin anterior al día actual, y registra fecha de archivo. `Registro.confirmarAsistencia` impide segunda confirmación.

```mermaid
stateDiagram-v2
    [*] --> Ingresada : altaEdicion
    Ingresada --> Aceptada : aceptar
    Ingresada --> Rechazada : rechazar
    Aceptada --> Archivada : archivar propia y fin anterior a hoy
```

La asistencia exige registro propio y confirmación única; no incorpora restricciones de fecha/estado que no figuran en §6.3 de la letra. Archivar conserva registros. Finalizar un evento impide nuevas ediciones y lo excluye de consultas públicas y búsqueda; el administrador local sigue consultándolo.

## Componentes y frontera

```mermaid
flowchart LR
    Swing[Estación Swing] -->|ISistema directo| Sistema[Sistema y manejadores]
    Navegador[Navegador escritorio] -->|HTTP| Web[Tomcat 11 y web.war]
    Celular[Navegador móvil] -->|HTTP| Web
    Web -->|SOAP HTTPS Metro| Fachadas[Eventos / Sesiones / Portal]
    Fachadas -->|ISistema obtenido por Fábrica al arrancar| Sistema
    Sistema -->|JPA| PostgreSQL[(PostgreSQL)]
```

`central.Main` configura JPA, publica endpoints y abre Swing. `ServidorHttpsCentral` comparte el registro `SesionesRemotas` entre Sesiones y Portal. Los proxies generados se crean por invocación y reemplazan su endpoint desde configuración externa. Credenciales remotas viajan por encabezado Authorization; la identidad no se acepta como parámetro libre de negocio.

La red transporta los DTO JAXB de `contratos.dto` y los tipos SOAP de Eventos/Sesiones, enumerados, colecciones y `byte[]`. `ConversorDatos` adapta tipos de fechas/colecciones y excluye contraseñas de salidas. Las excepciones declaradas se convierten a SOAP Fault con códigos de negocio/permiso/sesión y errores técnicos sanitizados. La web no incluye entidades ni JPA como dependencias de producción.

## Secuencia: asistencia y constancia

```mermaid
sequenceDiagram
    actor A as Asistente
    participant W as Servlet móvil/escritorio
    participant P as PortalPublicador
    participant S as Sistema
    participant M as ManejadorRegistros
    participant DB as PostgreSQL
    A->>W: POST asistencia + CSRF
    W->>P: SOAP confirmarAsistencia(edicion), Bearer
    P->>P: resolver identidad en SesionesRemotas
    P->>S: confirmarAsistencia(edicion, actor)
    S->>S: exigir asistente
    S->>M: confirmarAsistencia(nickname, edicion)
    M->>DB: transacción, buscar registro propio con bloqueo
    M->>M: rechazar confirmación duplicada
    M->>DB: asistenciaConfirmada=true, commit
    P-->>W: resultado SOAP
    W-->>A: redirección y mensaje de una lectura
    A->>W: GET constancia
    W->>P: SOAP obtenerDatosConstancia(edicion), Bearer
    P->>S: obtenerDatosConstancia(edicion, actor)
    S->>S: verificar registro propio y asistencia
    S-->>W: datos de constancia por SOAP
    W-->>A: PDF generado a demanda, application/pdf
```

La generación de PDF está en `GeneradorConstanciaPdf`; la secuencia describe el código existente. Su ejecución con PDFBox queda sujeta a disponer de la dependencia real, y no se sustituye por una respuesta simulada.

## Secuencia: visitas

```mermaid
sequenceDiagram
    actor N as Navegador
    participant F as FiltroVisitasEvento
    participant W as Servlet de evento
    participant P as PortalPublicador
    participant S as Sistema
    participant DB as PostgreSQL
    N->>F: GET detalle de evento
    F->>W: chain.doFilter
    W-->>N: vista de evento
    F->>F: verificar navegación, parámetro, estado 200 y marca
    F->>P: SOAP registrarVisitaEvento
    P->>S: registrarVisitaEvento(evento, actor opcional)
    S->>DB: UPDATE visitas = visitas + 1 si no finalizado
```

El filtro solo cubre `/evento` y `/movil/eventos`, y excluye recursos auxiliares. Fallos al informar la visita se registran sin exponer credenciales ni reemplazar la página ya entregada. Swing obtiene el top cinco persistido por `ISistema`, fuera del EDT.

## Secuencia: archivo

```mermaid
sequenceDiagram
    actor O as Organizador
    participant W as ArchivarEdicionServlet
    participant P as PortalPublicador
    participant S as Sistema
    participant M as ManejadorEventos
    participant DB as PostgreSQL
    O->>W: POST edición + CSRF
    W->>P: SOAP archivarEdicion, Bearer
    P->>S: archivarEdicion(edicion, actor resuelto)
    S->>M: archivarEdicion(edicion, nickname, esAdmin)
    M->>DB: buscar edición con bloqueo
    M->>M: verificar propietario, Aceptada y fin anterior a hoy
    M->>DB: estado Archivada + fechaArchivo, commit
    P-->>W: resultado SOAP
    W-->>O: redirección y mensaje
```

Configuración privada: `~/.eventosUy/central.properties` y `web.properties`. El archivo alternativo de pruebas `eventos.web.config` se aplica a los tres clientes. La versión móvil comparte WAR, SOAP, identidad y servlets de soporte, con vistas propias en `WEB-INF/views/movil` y Bootstrap local.
