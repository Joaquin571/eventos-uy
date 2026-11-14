package interfaces;

import datatypes.*;
import java.util.List;

/** Contrato del bloque 2. Solo el servidor decide identidad, permisos y visibilidad. */
public interface IUsuariosWeb {
    DtSesion iniciarSesion(String identificador, String contrasenia, boolean movil);
    void cerrarSesion(String token);
    DtUsuario usuarioActual(String token);
    List<DtUsuario> listarUsuarios();
    DtPerfilUsuario consultarPerfil(String token, String nickname);
    boolean disponibleNickname(String valor);
    boolean disponibleCorreo(String valor);
    void altaUsuario(DtUsuario usuario, DtImagen imagen);
    DtUsuario modificarUsuario(String token, DtUsuario usuario, DtImagen imagen);
    List<DtInstitucion> listarInstituciones();
    DtInstitucion consultarInstitucion(String nombre);
    void altaInstitucion(String token, DtInstitucion institucion, DtImagen imagen);
    DtFormularioPatrocinio formularioPatrocinio(String token, String evento, String edicion);
    void altaPatrocinio(String token, String evento, DtPatrocinio patrocinio);
    DtConsultaPatrocinio consultarPatrocinio(String token, String evento, String edicion, String institucion);
    DtImagen imagen(String tipo, String clave);
    List<DtCategoria> listarCategorias();
}