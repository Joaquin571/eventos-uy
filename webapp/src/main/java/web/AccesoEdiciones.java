package web;

import datatypes.*;
import interfaces.ISistema;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.Collection;
import java.util.List;

final class AccesoEdiciones {
    private AccesoEdiciones() { }

    record Seleccion(String evento, DtEdicion edicion) { }

    static DtUsuario usuario(HttpServletRequest req) {
        HttpSession sesion = req.getSession(false);
        Object usuario = sesion == null ? null : sesion.getAttribute("usuario");
        return usuario instanceof DtUsuario ? (DtUsuario) usuario : null;
    }

    static boolean esPropietario(DtUsuario usuario, DtEdicion edicion) {
        return usuario instanceof DtOrganizador && edicion != null
                && usuario.getNickname() != null
                && usuario.getNickname().equals(edicion.getNombreOrganizador());
    }

    static boolean esVisible(DtUsuario usuario, DtEdicion edicion) {
        return edicion != null && (edicion.getEstado() == EstadoEdicion.ACEPTADA
                || esPropietario(usuario, edicion));
    }

    // Una combinación incompatible o no visible se trata igual que un recurso inexistente.
    static Seleccion resolver(ISistema sistema, Collection<DtEvento> eventos,
                              String evento, String nombre, DtUsuario usuario) {
        if (!evento.isEmpty() && eventos.stream().noneMatch(e -> evento.equals(e.getNombre()))) return null;
        if (nombre.isEmpty()) return new Seleccion(evento, null);
        for (DtEvento candidato : eventos) {
            if (!evento.isEmpty() && !evento.equals(candidato.getNombre())) continue;
            for (DtEdicion edicion : sistema.obtenerEdicionesEvento(candidato.getNombre())) {
                if (nombre.equals(edicion.getIdNombre())) {
                    return esVisible(usuario, edicion) ? new Seleccion(candidato.getNombre(), edicion) : null;
                }
            }
        }
        return null;
    }

    static List<DtEdicion> aceptadas(ISistema sistema, String evento) {
        return evento.isEmpty() ? List.of() : sistema.obtenerEdicionesEvento(evento).stream()
                .filter(e -> e.getEstado() == EstadoEdicion.ACEPTADA).toList();
    }

    static String texto(HttpServletRequest req, String campo) {
        String valor = req.getParameter(campo);
        return valor == null || valor.isBlank() ? "" : valor;
    }
}
