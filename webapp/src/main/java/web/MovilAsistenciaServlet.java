package web;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Registro de Asistencia a Edición (celular). Solo POST con CSRF: invoca confirmarAsistencia en Central, que
 * resuelve al asistente con la credencial P03 y valida las reglas de negocio. El resultado se muestra como
 * mensaje de una sola lectura en /movil/registros (patrón POST-redirect-GET).
 */
@WebServlet("/movil/asistencia")
public class MovilAsistenciaServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (AccesoMovil.requerirAsistente(req, resp) == null) return;
        String edicion = AccesoEdiciones.texto(req, "edicion");
        boolean modoAsistencia = "asistencia".equals(req.getParameter("modo"));
        StringBuilder consulta = new StringBuilder();
        if (!edicion.isEmpty()) consulta.append("edicion=").append(URLEncoder.encode(edicion, StandardCharsets.UTF_8));
        if (modoAsistencia) consulta.append(consulta.length() == 0 ? "" : "&").append("modo=asistencia");
        String destino = req.getContextPath() + MovilRegistrosServlet.PANTALLA + (consulta.length() == 0 ? "" : "?" + consulta);
        if (edicion.isEmpty()) {
            MensajesFlash.guardar(req, MovilRegistrosServlet.PANTALLA, null, "Elegí un registro para confirmar la asistencia.");
        } else {
            try {
                ContextoListener.central(req).confirmarAsistencia(edicion);
                MensajesFlash.guardar(req, MovilRegistrosServlet.PANTALLA, "Asistencia registrada correctamente.", null);
            } catch (IllegalArgumentException e) {
                MensajesFlash.guardar(req, MovilRegistrosServlet.PANTALLA, null, e.getMessage());
            }
        }
        resp.sendRedirect(destino);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setHeader("Allow", "POST");
        resp.sendError(405);
    }
}
