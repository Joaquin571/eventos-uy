package web;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@WebServlet("/confirmar-asistencia")
public class ConfirmarAsistenciaServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String edicion = AccesoEdiciones.texto(req, "edicion");
        String origen = AccesoEdiciones.texto(req, "origen");
        String destino = "edicion".equals(origen)
                ? "/edicion?nombre=" + URLEncoder.encode(edicion, StandardCharsets.UTF_8)
                : "/consulta-registro?edicion=" + URLEncoder.encode(edicion, StandardCharsets.UTF_8);
        String pantalla = "edicion".equals(origen) ? "/edicion" : "/consulta-registro";

        try {
            ContextoListener.central(req).confirmarAsistencia(edicion);
            MensajesFlash.guardar(req, pantalla, "Asistencia confirmada correctamente.", null);
        } catch (IllegalArgumentException e) {
            MensajesFlash.guardar(req, pantalla, null, e.getMessage());
        }
        resp.sendRedirect(req.getContextPath() + destino);
    }
}