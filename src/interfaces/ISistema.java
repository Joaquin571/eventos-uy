package interfaces;

import datatypes.*;

import java.util.Collection;

public interface ISistema {

    // VALIDACIONES
    boolean existeUsuario(String nickname);

    boolean existeCorreoElectronico(String correoElectronico);


    // ALTA USUARIO
    boolean altaAsistente(DtAsistente asistente);

    boolean altaOrganizador(DtOrganizador organizador);


    // CONSULTA USUARIO
    Collection<DtUsuario> listarUsuarios();

    DtUsuario consultarUsuario(String nickname);


    // MODIFICAR USUARIO
    void modificarAsistente(
            DtAsistente asistente,
            String institucion
    );

    void modificarOrganizador(
            DtOrganizador organizador
    );
}