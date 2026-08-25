package interfaces;

import clases.Usuario;
import datatypes.*;

import java.util.Collection;
import java.util.Set;

public interface ISistema {
    boolean existeUsuario(String nickname, String nombre, String correoElectronico);
    boolean existeCorreoElectronico(String correoElectronico);
    void altaAsistente(DtAsistente asistente);
    void altaOrganizador(DtOrganizador organizador);
    DtUsuario consultarUsuario(String nickname);
    void modificarAsistente(DtAsistente asistente, String institucion);
    void modificarOrganizador(DtOrganizador organizador);
    Collection<DtUsuario> listarUsuarios();
}
