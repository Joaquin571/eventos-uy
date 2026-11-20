package web;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@WebServlet("/archivar-edicion")
public class ArchivarEdicionServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String edicion = AccesoEdiciones.texto(req, "edicion");
        String destino = "/edicion?nombre=" + URLEncoder.encode(edicion, StandardCharsets.UTF_8);

        try {
            ContextoListener.central(req).archivarEdicion(edicion);
            MensajesFlash.guardar(req, "/edicion", "La edición fue archivada correctamente.", null);
        } catch (IllegalArgumentException e) {
            MensajesFlash.guardar(req, "/edicion", null, e.getMessage());
        }
        resp.sendRedirect(req.getContextPath() + destino);
    }
}