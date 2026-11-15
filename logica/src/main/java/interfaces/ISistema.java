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
    DtPatrocinio consultarPatrocinio(String nombreEdicion, String nombreInstitucion);


    // INSTITUCIONES
    boolean existeInstitucion(String nombre);
    boolean altaInstitucion(DtInstitucion institucion);
    Collection<DtInstitucion> listarInstituciones();

    // EVENTOS
    boolean altaEvento(DtEvento dt) throws Exception;
    Collection<DtEvento> listarEventos();

    // EDICIONES
    boolean altaEdicion(DtEdicion dtEdicion, String nombreEvento);
    Collection<DtEdicion> obtenerEdicionesEvento(String nombreEvento);
    void aceptarRechazarEdicion(String nombreEdicion, boolean aceptar);
    Collection<DtEdicion> obtenerEdicionesEvento(String nombreEvento, EstadoEdicion estado);

    // CATEGORÍAS
    void altaCategoria(String nombre, String nombrePadre) throws Exception;
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
            DtRegistro dt
    );

    boolean registroAEdicion(
            String nicknameAsistente,
            String nombreEdicion,
            String nombreTipoRegistro,
            DtRegistro dt,
            String codigoPatrocinio
    );

    Collection<DtRegistro> obtenerRegistrosAsistente(String nickname);
    Collection<DtRegistro> obtenerRegistrosEdicion(String nombreEdicion);

    // PATROCINIOS
    boolean altaPatrocinio(DtPatrocinio patrocinio, String nombreEvento, Actor actor);
    DtOpcionesAltaPatrocinio obtenerOpcionesAltaPatrocinio(String nombreEvento, String nombreEdicion, Actor actor);
    Collection<DtPatrocinio> obtenerPatrociniosEdicion(String nombreEdicion);
    DtPatrocinio consultarPatrocinio(String codigo);

    //IMAGEN
    void asociarImagen(TipoEntidadImagen tipo, String clave, DtImagen imagen);
    DtImagen obtenerImagen(TipoEntidadImagen tipo, String clave);
    void validarImagen(DtImagen imagen);
}