package web;

import contratos.dto.DtAsistente;
import contratos.dto.DtOrganizador;
import contratos.dto.DtUsuario;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Guardia del sitio móvil. Solo deja pasar a asistentes con credencial P03 vigente.
 * El rol sale de la sesión que FiltroAcceso refresca contra Central en cada solicitud;
 * no se guarda ningún dato de rol propio del sitio móvil.
 */
final class AccesoMovil {
    static final String LOGIN = "/movil/login";
    static final String EVENTOS = "/movil/eventos";

    private AccesoMovil() { }

    static boolean esAsistente(HttpServletRequest req) {
        return SesionWeb.usuario(req) instanceof DtAsistente && SesionWeb.credencial(req) != null;
    }

    /** Devuelve el asistente con sesión; si no hay, redirige al login móvil y devuelve null. */
    static DtAsistente requerirAsistente(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        DtUsuario usuario = SesionWeb.usuario(req);
        if (usuario instanceof DtAsistente asistente && SesionWeb.credencial(req) != null) return asistente;
        String motivo = usuario instanceof DtOrganizador ? "?motivo=rol" : "";
        resp.sendRedirect(req.getContextPath() + LOGIN + motivo);
        return null;
    }
}
