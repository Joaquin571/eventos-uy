package interfaces;

import datatypes.*;

import java.util.Collection;

public interface ISistema {

    // VALIDACIONES USUARIO
    boolean existeUsuario(String nickname);
    boolean existeCorreoElectronico(String correoElectronico);

    // ALTA USUARIO
    boolean altaAsistente(DtAsistente asistente);
    boolean altaOrganizador(DtOrganizador organizador);

    // CONSULTA USUARIO
    Collection<DtUsuario> listarUsuarios();
    DtUsuario consultarUsuario(String nickname);

    // MODIFICAR USUARIO
    void modificarAsistente(DtAsistente asistente);
    void modificarOrganizador(DtOrganizador organizador);

    // INSTITUCIONES
    boolean existeInstitucion(String nombre);
    boolean altaInstitucion(DtInstitucion institucion);
    Collection<DtInstitucion> listarInstituciones();

    //ALTA EVENTO
    Collection<String> listarCategorias();
    boolean existeEvento(String nombre);
    boolean altaEvento(DtEvento dt) throws Exception;

    // PATROCINIOS
    boolean altaPatrocinio(DtPatrocinio patrocinio);
    Collection<DtPatrocinio> listarPatrocinios();
    DtPatrocinio consultarPatrocinio(String codigo);
}

