package implementacion;

import clases.*;
import datatypes.*;
import interfaces.ISistema;
import manejadores.*;

import java.util.ArrayList;
import java.util.List;

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

    @Override
    public List<DtUsuario> listarUsuarios(){
        List<DtUsuario> lista = new ArrayList<>();
        for(Usuario u : manejadorUsuarios.listarUsuarios()){
            if(u instanceof Asistente){
                Asistente a = (Asistente) u;
                lista.add(new DtAsistente(a.getNickname(), a.getNombre(), a.getCorreoElectronico(), a.getApellido(), a.getFechaNacimiento()));

            }else if (u instanceof Organizador){
                Organizador o = (Organizador) u;
                lista.add(new DtOrganizador(o.getNickname(), o.getNombre(), o.getCorreoElectronico(), o.getDescripcion(), o.getSitioWeb()));
            }
        }
        return lista;
    }

    @Override
    public DtUsuario obtenerInformacionUsuario(String nickname){
        Usuario u = manejadorUsuarios.obtenerUsuario(nickname);
        if(u == null)
            return null;
        if(u instanceof Asistente){
            Asistente a = (Asistente) u;
            return new DtAsistente(a.getNickname(), a.getNombre(), a.getCorreoElectronico(), a.getApellido(), a.getFechaNacimiento());

        }else if(u instanceof Organizador){
            Organizador o = (Organizador) u;
            return new DtOrganizador(o.getNickname(), o.getNombre(), o.getCorreoElectronico(), o.getDescripcion(), o.getSitioWeb());
        }
        return null;
    }
}

