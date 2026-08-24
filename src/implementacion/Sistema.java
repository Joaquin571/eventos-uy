package implementacion;

import clases.*;
import datatypes.*;
import interfaces.ISistema;
import manejadores.*;

public class Sistema implements ISistema {
    private final ManejadorUsuarios manejadorUsuarios;

    public Sistema(){manejadorUsuarios = ManejadorUsuarios.getInstance();}

    @Override
    public boolean existeUsuario(String nickname, String nombre, String correoElectronico) {
        return manejadorUsuarios.existeUsuario(nickname);
    }

    @Override
    public boolean existeCorreoElectronico(String correoElectronico) {
        return manejadorUsuarios.existeCorreo(correoElectronico);
    }

    @Override
    public void altaAsistente(DtAsistente dt) {

        Asistente asistente = new Asistente(
                dt.getNickname(),
                dt.getNombre(),
                dt.getCorreoElectronico(),
                dt.getApellido(),
                dt.getFechaNacimiento()
        );

        manejadorUsuarios.addUsuario(asistente);
    }

    @Override
    public void altaOrganizador(DtOrganizador dt) {
        Organizador organizador = new Organizador(
                dt.getNickname(),
            dt.getNombre(),
            dt.getCorreoElectronico(),
            dt.getDescripcion(),
            dt.getSitioWeb()
        );

        manejadorUsuarios.addUsuario(organizador);
    }
}
