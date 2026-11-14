package usuarios;

import datatypes.*;
import interfaces.ISistema;
import interfaces.IUsuariosWeb;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/** Casos de uso web del integrante 2. La fachada SOAP solamente delega aquí. */
public class ControladorUsuariosWeb implements IUsuariosWeb {
    private final ISistema sistema;
    private final SesionesUsuarios sesiones;
    private static final Pattern CORREO = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    private static final Pattern URL = Pattern.compile("^https?://[^\\s.]+\\.[^\\s]+$", Pattern.CASE_INSENSITIVE);

    public ControladorUsuariosWeb(ISistema sistema, SesionesUsuarios sesiones) {
        this.sistema = sistema;
        this.sesiones = sesiones;
    }

    public DtSesion iniciarSesion(String identificador, String contrasenia, boolean movil) {
        DtUsuario usuario = sistema.autenticar(identificador, contrasenia);
        if (movil && !(usuario instanceof DtAsistente)) {
            throw new IllegalArgumentException("Desde el sitio móvil solo pueden ingresar asistentes.");
        }
        DtSesion respuesta = new DtSesion();
        respuesta.setToken(sesiones.crear(usuario.getNickname()));
        respuesta.setUsuario(usuario);
        return respuesta;
    }

    public void cerrarSesion(String token) { sesiones.cerrar(token); }

    public DtUsuario usuarioActual(String token) {
        DtUsuario usuario = sistema.consultarUsuario(sesiones.validar(token));
        if (usuario == null) {
            sesiones.cerrar(token);
            throw new ErrorAccesoUsuarios("SESION", "El usuario de la sesión ya no existe.");
        }
        return usuario;
    }

    private DtUsuario opcional(String token) {
        return token == null || token.isBlank() ? null : usuarioActual(token);
    }

    private DtUsuario organizador(String token) {
        DtUsuario usuario = usuarioActual(token);
        if (!(usuario instanceof DtOrganizador)) {
            throw new ErrorAccesoUsuarios("PERMISO", "Esta operación requiere un organizador.");
        }
        return usuario;
    }

    public List<DtUsuario> listarUsuarios() { return new ArrayList<>(sistema.listarUsuarios()); }

    public DtPerfilUsuario consultarPerfil(String token, String nickname) {
        DtUsuario actual = opcional(token);
        if (nickname == null || nickname.isBlank()) throw noEncontrado();
        DtUsuario perfil = sistema.consultarUsuario(nickname);
        if (perfil == null) throw noEncontrado();
        boolean propio = actual != null && actual.getNickname().equals(perfil.getNickname());
        DtPerfilUsuario respuesta = new DtPerfilUsuario();
        respuesta.setUsuario(perfil);
        respuesta.setPropio(propio);
        if (perfil instanceof DtAsistente && propio) {
            respuesta.setRegistros(new ArrayList<>(sistema.obtenerRegistrosAsistente(perfil.getNickname())));
        }
        if (perfil instanceof DtOrganizador) {
            for (DtEdicion edicion : sistema.obtenerEdicionesOrganizador(perfil.getNickname())) {
                if (propio || edicion.getEstado() == EstadoEdicion.ACEPTADA) respuesta.getEdiciones().add(edicion);
            }
        }
        return respuesta;
    }

    public boolean disponibleNickname(String valor) {
        return textoValido(valor) && !sistema.existeUsuario(valor);
    }
    public boolean disponibleCorreo(String valor) {
        return textoValido(valor) && CORREO.matcher(valor).matches() && !sistema.existeCorreoElectronico(valor);
    }
    private boolean textoValido(String valor) {
        return valor != null && !valor.isBlank() && valor.indexOf('\0') < 0
                && valor.equals(valor.trim()) && valor.codePointCount(0, valor.length()) <= 255;
    }

    public void altaUsuario(DtUsuario usuario, DtImagen imagen) {
        boolean creado;
        if (usuario instanceof DtAsistente asistente) creado = sistema.altaAsistente(asistente, imagen);
        else if (usuario instanceof DtOrganizador organizador) creado = sistema.altaOrganizador(organizador, imagen);
        else throw new IllegalArgumentException("Seleccioná un tipo de usuario válido.");
        if (!creado) throw new IllegalArgumentException("El nickname o el correo ya están registrados.");
    }

    public DtUsuario modificarUsuario(String token, DtUsuario datos, DtImagen imagen) {
        DtUsuario actual = usuarioActual(token);
        if (datos == null || !actual.getNickname().equals(datos.getNickname())) {
            throw new ErrorAccesoUsuarios("PERMISO", "Solo podés modificar tu propio perfil.");
        }
        if (actual instanceof DtAsistente && datos instanceof DtAsistente asistente) {
            sistema.modificarAsistente(asistente, imagen);
        } else if (actual instanceof DtOrganizador && datos instanceof DtOrganizador organizador) {
            sistema.modificarOrganizador(organizador, imagen);
        } else throw new IllegalArgumentException("No se puede cambiar el tipo de usuario.");
        return sistema.consultarUsuario(actual.getNickname());
    }

    public List<DtInstitucion> listarInstituciones() { return new ArrayList<>(sistema.listarInstituciones()); }
    public DtInstitucion consultarInstitucion(String nombre) {
        return sistema.listarInstituciones().stream().filter(i -> i.getNombre().equals(nombre))
                .findFirst().orElseThrow(this::noEncontrado);
    }
    public void altaInstitucion(String token, DtInstitucion datos, DtImagen imagen) {
        organizador(token);
        if (datos == null || datos.getSitioWeb() == null || !URL.matcher(datos.getSitioWeb()).matches()) {
            throw new IllegalArgumentException("Ingresá una URL válida para la institución (https://ejemplo.com).");
        }
        if (!sistema.altaInstitucion(datos, imagen)) throw new IllegalArgumentException("La institución ya existe.");
    }

    private DtEdicion buscarEdicion(String nombre) {
        if (nombre == null || nombre.isBlank()) throw noEncontrado();
        for (DtEvento evento : sistema.listarEventos()) {
            for (DtEdicion edicion : sistema.obtenerEdicionesEvento(evento.getNombre())) {
                if (nombre.equals(edicion.getIdNombre())) return edicion;
            }
        }
        throw noEncontrado();
    }
    private boolean propio(DtUsuario usuario, DtEdicion edicion) {
        return usuario instanceof DtOrganizador && usuario.getNickname().equals(edicion.getNombreOrganizador());
    }
    private void exigirPropia(DtUsuario usuario, DtEdicion edicion) {
        if (!propio(usuario, edicion)) throw new ErrorAccesoUsuarios("PERMISO", "La edición pertenece a otro organizador.");
    }
    private String eventoDe(String nombreEdicion) {
        for (DtEvento evento : sistema.listarEventos()) {
            if (sistema.obtenerEdicionesEvento(evento.getNombre()).stream()
                    .anyMatch(e -> nombreEdicion.equals(e.getIdNombre()))) return evento.getNombre();
        }
        throw noEncontrado();
    }

    public DtFormularioPatrocinio formularioPatrocinio(String token, String evento, String edicion) {
        DtUsuario usuario = organizador(token);
        DtFormularioPatrocinio respuesta = new DtFormularioPatrocinio();
        respuesta.setEventos(new ArrayList<>(sistema.listarEventos()));
        if (edicion != null && !edicion.isBlank()) {
            exigirPropia(usuario, buscarEdicion(edicion));
            evento = eventoDe(edicion);
            respuesta.setTipos(new ArrayList<>(sistema.obtenerTiposRegistroEdicion(edicion)));
        }
        respuesta.setEventoElegido(evento == null ? "" : evento);
        if (evento != null && !evento.isBlank()) {
            for (DtEdicion candidata : sistema.obtenerEdicionesEvento(evento)) {
                if (propio(usuario, candidata)) respuesta.getEdiciones().add(candidata);
            }
        }
        respuesta.setInstituciones(listarInstituciones());
        return respuesta;
    }

    public void altaPatrocinio(String token, String evento, DtPatrocinio datos) {
        DtUsuario usuario = organizador(token);
        if (datos == null) throw new IllegalArgumentException("Los datos del patrocinio son obligatorios.");
        exigirPropia(usuario, buscarEdicion(datos.getNombreEdicion()));
        if (evento != null && !evento.isBlank() && !evento.equals(eventoDe(datos.getNombreEdicion()))) {
            throw new IllegalArgumentException("La edición no pertenece al evento seleccionado.");
        }
        // La fecha y las reglas del aporte, código, tipo, institución y 20 % las determina Sistema.
        if (!sistema.altaPatrocinio(datos)) throw new IllegalArgumentException("No se pudo crear el patrocinio.");
    }

    public DtConsultaPatrocinio consultarPatrocinio(String token, String evento, String edicion, String institucion) {
        DtUsuario usuario = opcional(token);
        DtConsultaPatrocinio respuesta = new DtConsultaPatrocinio();
        respuesta.setEventos(new ArrayList<>(sistema.listarEventos()));
        DtEdicion detalle = null;
        boolean duenio = false;
        if (edicion != null && !edicion.isBlank()) {
            detalle = buscarEdicion(edicion);
            duenio = propio(usuario, detalle);
            if (!duenio && detalle.getEstado() != EstadoEdicion.ACEPTADA) throw noEncontrado();
            evento = eventoDe(edicion);
            for (DtPatrocinio p : sistema.obtenerPatrociniosEdicion(edicion)) {
                respuesta.getPatrocinadoras().add(p.getNombreInstituto());
            }
            if (institucion != null && !institucion.isBlank()) {
                DtPatrocinio patrocinio = sistema.consultarPatrocinio(edicion, institucion);
                if (patrocinio == null) throw noEncontrado();
                // El código privado se elimina ANTES de construir la respuesta SOAP.
                if (!duenio) patrocinio.setCodigoPatrocinio(null);
                respuesta.setPatrocinio(patrocinio);
            }
        }
        respuesta.setOrganizadorDuenio(duenio);
        respuesta.setEventoElegido(evento == null ? "" : evento);
        if (evento != null && !evento.isBlank()) {
            for (DtEdicion candidata : sistema.obtenerEdicionesEvento(evento)) {
                if (candidata.getEstado() == EstadoEdicion.ACEPTADA) respuesta.getEdiciones().add(candidata);
            }
        }
        if (detalle != null && detalle.getEstado() != EstadoEdicion.ACEPTADA) respuesta.getEdiciones().add(detalle);
        return respuesta;
    }

    public DtImagen imagen(String tipo, String clave) {
        TipoEntidadImagen entidad;
        if ("usuario".equals(tipo)) entidad = TipoEntidadImagen.USUARIO;
        else if ("institucion".equals(tipo)) entidad = TipoEntidadImagen.INSTITUCION;
        else throw new IllegalArgumentException("Este servicio solo publica imágenes de usuarios e instituciones.");
        if (clave == null || clave.isBlank()) return null;
        return sistema.obtenerImagen(entidad, clave);
    }
    public List<DtCategoria> listarCategorias() { return new ArrayList<>(sistema.listarCategorias()); }
    private ErrorAccesoUsuarios noEncontrado() { return new ErrorAccesoUsuarios("NO_ENCONTRADO", "No se encontró la información solicitada."); }
}
