package web;

import datatypes.DtEdicion;
import datatypes.DtEvento;
import datatypes.DtOrganizador;
import datatypes.DtUsuario;
import datatypes.EstadoEdicion;
import interfaces.ISistema;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

final class AccesoPatrocinios {

    private AccesoPatrocinios() {
    }

    static DtUsuario usuarioDeSesion(HttpServletRequest req) {
        HttpSession sesion = req.getSession(false);
        Object usuario = sesion == null ? null : sesion.getAttribute("usuario");
        return usuario instanceof DtUsuario ? (DtUsuario) usuario : null;
    }

    static boolean esPropietario(DtUsuario usuario, DtEdicion edicion) {
        return usuario instanceof DtOrganizador && edicion != null
                && usuario.getNickname() != null
                && usuario.getNickname().equals(edicion.getNombreOrganizador());
    }

    static boolean esEdicionPropia(ISistema sistema, DtUsuario usuario, String nombre) {
        if (!(usuario instanceof DtOrganizador) || nombre == null || nombre.isBlank()) {
            return false;
        }
        for (DtEdicion edicion : sistema.obtenerEdicionesOrganizador(usuario.getNickname())) {
            if (nombre.equals(edicion.getIdNombre()) && esPropietario(usuario, edicion)) {
                return true;
            }
        }
        return false;
    }

    static DtEdicion buscarEdicion(ISistema sistema, String nombre) {
        if (nombre == null || nombre.isBlank()) {
            return null;
        }
        for (DtEvento evento : sistema.listarEventos()) {
            for (DtEdicion edicion : sistema.obtenerEdicionesEvento(evento.getNombre())) {
                if (nombre.equals(edicion.getIdNombre())) {
                    return edicion;
                }
            }
        }
        return null;
    }

    static boolean esVisible(DtUsuario usuario, DtEdicion edicion) {
        return edicion != null && (edicion.getEstado() == EstadoEdicion.ACEPTADA
                || esPropietario(usuario, edicion));
    }
}
