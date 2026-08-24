package interfaces;

import datatypes.*;

public interface ISistema {
    boolean existeUsuario(String nickname, String nombre, String correoElectronico);
    boolean existeCorreoElectronico(String correoElectronico);
    void altaAsistente(DtAsistente asistente);
    void altaOrganizador(DtOrganizador organizador);
}
