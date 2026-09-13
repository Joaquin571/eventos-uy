package interfaces;

import datatypes.*;
import java.util.Collection;

public interface ISistema {

    // USUARIOS
    boolean existeUsuario(String nickname);
    boolean existeCorreoElectronico(String correoElectronico);
    boolean altaAsistente(DtAsistente asistente);
    boolean altaOrganizador(DtOrganizador organizador);
    void modificarAsistente(DtAsistente asistente);
    void modificarOrganizador(DtOrganizador organizador);
    Collection<DtUsuario> listarUsuarios();
    DtUsuario consultarUsuario(String nickname);
    Collection<DtEdicion> obtenerEdicionesOrganizador(String nickname);

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

    // CATEGORÍAS
    void altaCategoria(String nombre, String nombrePadre) throws Exception;
    Collection<String> listarCategoriasFormateadas();

    // TIPOS DE REGISTRO
    boolean altaTipoRegistro(DtTipoRegistro dtTipoRegistro, String nombreEdicion);
    Collection<DtTipoRegistro> obtenerTiposRegistroEdicion(String nombreEdicion);
    DtTipoRegistro consultarTipoRegistro(String nombreEdicion, String nombreTipoRegistro);

    // REGISTROS
    boolean registroAEdicion(String nicknameAsistente, String nombreEdicion, String nombreTipoRegistro, DtRegistro dtRegistro);
    Collection<DtRegistro> obtenerRegistrosAsistente(String nickname);
    Collection<DtRegistro> obtenerRegistrosEdicion(String nombreEdicion);

    // PATROCINIOS
    boolean altaPatrocinio(DtPatrocinio patrocinio);
    Collection<DtPatrocinio> obtenerPatrociniosEdicion(String nombreEdicion);
    DtPatrocinio consultarPatrocinio(String codigo);
}