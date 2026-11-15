package web;

import datatypes.DtUsuario;
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

}
