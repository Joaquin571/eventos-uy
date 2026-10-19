package web;

import datatypes.DtImagen;
import datatypes.TipoEntidadImagen;
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

        DtImagen imagen = (tipo == null || id == null || id.isBlank())
                ? null
                : ContextoListener.sistema(req).obtenerImagen(tipo, id);

        if (imagen == null) {
            resp.sendRedirect(req.getContextPath() + "/img/default-avatar.png");
            return;
        }

        resp.setContentType(imagen.getTipoContenido());
        resp.setHeader("X-Content-Type-Options", "nosniff");
        resp.setHeader("Cache-Control", "no-cache");
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