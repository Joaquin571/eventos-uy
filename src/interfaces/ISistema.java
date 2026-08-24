package interfaces;

import datatypes.*;
import java.util.List;

public interface ISistema {
    boolean existeUsuario(String nickname, String nombre, String correoElectronico);
    boolean existeCorreoElectronico(String correoElectronico);
    void altaAsistente(DtAsistente asistente);
    void altaOrganizador(DtOrganizador organizador);

    List<DtUsuario> listarUsuarios();
    DtUsuario obtenerInformacionUsuario(String nickname);
}
