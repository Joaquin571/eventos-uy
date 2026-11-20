package web;

import contratos.dto.DtImagen;
import contratos.dto.TipoEntidadImagen;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/imagen")
public class ImagenServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {

        TipoEntidadImagen tipo = tipoDe(req.getParameter("tipo"));
        String id = req.getParameter("id");

        resp.setHeader("Cache-Control", "private, no-store");
        DtImagen imagen = (tipo == null)
                ? null
                : ContextoListener.central(req).obtenerImagen(tipo, id);

        if (imagen == null) {
            resp.sendRedirect(req.getContextPath() + "/img/default-avatar.png");
            return;
        }

        resp.setContentType(imagen.getTipoContenido());
        if (tipo != TipoEntidadImagen.EDICION) resp.setHeader("Cache-Control", "no-cache");
        resp.setHeader("X-Content-Type-Options", "nosniff");

        resp.setContentLength(imagen.getContenido().length);
        resp.getOutputStream().write(imagen.getContenido());
    }

    private TipoEntidadImagen tipoDe(String valor) {
        if (valor == null) {
            return null;
        }

        return switch (valor.toLowerCase()) {
            case "usuario" -> TipoEntidadImagen.USUARIO;
            case "evento" -> TipoEntidadImagen.EVENTO;
            case "edicion" -> TipoEntidadImagen.EDICION;
            case "institucion" -> TipoEntidadImagen.INSTITUCION;
            default -> null;
        };
    }
}
