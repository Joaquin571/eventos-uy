package interfaces;

import datatypes.*;
import java.util.Collection;

public interface ISistema {

    // USUARIOS
    boolean existeUsuario(String nickname);
    boolean existeCorreoElectronico(String correoElectronico);
    boolean altaAsistente(DtAsistente asistente);
    /** Alta atómica de datos, contraseña e imagen opcional. */
    boolean altaAsistente(DtAsistente asistente, DtImagen imagen);
    boolean altaOrganizador(DtOrganizador organizador);
    /** Alta atómica de datos, contraseña e imagen opcional. */
    boolean altaOrganizador(DtOrganizador organizador, DtImagen imagen);
    void modificarAsistente(DtAsistente asistente, Actor actor);
    void modificarAsistente(DtAsistente asistente, DtImagen imagen, Actor actor);
    void modificarOrganizador(DtOrganizador organizador, Actor actor);
    void modificarOrganizador(DtOrganizador organizador, DtImagen imagen, Actor actor);
    Collection<DtUsuario> listarUsuarios();
    DtUsuario consultarUsuario(String nickname);
    Collection<DtEdicion> obtenerEdicionesOrganizador(String nickname);
    DtUsuario autenticar(String nicknameOCorreo, String contrasenia);
    void cambiarContrasenia(String nickname, String nueva, String confirmacion, Actor actor);
    DtPatrocinio consultarPatrocinio(String nombreEdicion, String nombreInstitucion, Actor actor);


    // INSTITUCIONES
    boolean existeInstitucion(String nombre);
    boolean altaInstitucion(DtInstitucion institucion, Actor actor);
    boolean altaInstitucion(DtInstitucion institucion, DtImagen imagen, Actor actor);
    Collection<DtInstitucion> listarInstituciones();

    // EVENTOS
    boolean altaEvento(DtEvento dt, Actor actor) throws Exception;
    Collection<DtEvento> listarEventos();
    Collection<DtEvento> listarEventosPublicos();
    void finalizarEvento(String evento, Actor actor);
    Collection<DtEvento> listarEventosPorCategoria(String categoria);

    // EDICIONES
    boolean altaEdicion(DtEdicion dtEdicion, String nombreEvento, Actor actor);
    Collection<DtEdicion> obtenerEdicionesEvento(String nombreEvento);
    void aceptarRechazarEdicion(String nombreEdicion, boolean aceptar, Actor actor);
    Collection<DtEdicion> obtenerEdicionesEvento(String nombreEvento, EstadoEdicion estado);

    // CATEGORÍAS
    void altaCategoria(String nombre, String nombrePadre, Actor actor) throws Exception;
    Collection<String> listarCategoriasFormateadas();
    Collection<DtCategoria> listarCategorias();

    // TIPOS DE REGISTRO
    boolean altaTipoRegistro(DtTipoRegistro dtTipoRegistro, String nombreEdicion, Actor actor);
    Collection<DtTipoRegistro> obtenerTiposRegistroEdicion(String nombreEdicion);
    DtTipoRegistro consultarTipoRegistro(String nombreEdicion, String nombreTipoRegistro);

    // REGISTROS

    boolean registroAEdicion(
            String nicknameAsistente,
            String nombreEdicion,
            String nombreTipoRegistro,
            DtRegistro dt, Actor actor
    );

    boolean registroAEdicion(
            String nicknameAsistente,
            String nombreEdicion,
            String nombreTipoRegistro,
            DtRegistro dt,
            String codigoPatrocinio, Actor actor
    );

    Collection<DtRegistro> obtenerRegistrosAsistente(String nickname);
    Collection<DtRegistro> obtenerRegistrosEdicion(String nombreEdicion);

    // PATROCINIOS
    boolean altaPatrocinio(DtPatrocinio patrocinio, String nombreEvento, Actor actor);
    DtOpcionesAltaPatrocinio obtenerOpcionesAltaPatrocinio(String nombreEvento, String nombreEdicion, Actor actor);
    /** En consultas de patrocinios, actor null representa un visitante anónimo. */
    Collection<DtPatrocinio> obtenerPatrociniosEdicion(String nombreEdicion, Actor actor);
    DtPatrocinio consultarPatrocinio(String codigo, Actor actor);
    Collection<String> obtenerInstitucionesPatrocinadoras(String nombreEdicion, Actor actor);
    DtConsultaPatrocinios consultarPatrocinios(String evento, String edicion, String institucion, Actor actor);

    //IMAGEN
    void asociarImagen(TipoEntidadImagen tipo, String clave, DtImagen imagen, Actor actor);
    DtImagen obtenerImagen(TipoEntidadImagen tipo, String clave);
    void validarImagen(DtImagen imagen);

    DtPerfilUsuario consultarPerfil(String nickname, Actor actor);
    boolean disponibleNickname(String valor);
    boolean disponibleCorreo(String valor);
    DtInstitucion consultarInstitucion(String nombre);
    java.util.List<DtEdicion> obtenerEdicionesVisibles(String evento, Actor actor);
    DtSeleccionEdicion consultarEdiciones(String evento, String edicion, Actor actor);
    DtDetalleEdicion consultarDetalleEdicion(String nombre, Actor actor);
    DtTipoRegistro consultarTipoRegistroVisible(String edicion, String tipo, Actor actor);
    DtImagen obtenerImagenVisible(TipoEntidadImagen tipo, String clave, Actor actor);
    DtConsultaRegistros consultarRegistros(String edicion, String asistente, Actor actor);
    DtFormularioRegistro formularioRegistro(String evento, String edicion, String tipo, Actor actor);
    boolean registrarAutorizado(String edicion, String tipo, String modalidad, String codigo, Actor actor);
    boolean altaEvento(DtEvento dt, DtImagen imagen, Actor actor) throws Exception;
    boolean altaEdicion(DtEdicion dt, String nombreEvento, DtImagen imagen, Actor actor);
}
