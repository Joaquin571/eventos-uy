package web;

import datatypes.DtImagen;
import web.remoto.ClienteUsuarios;
import web.remoto.ErrorRemotoUsuarios;
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

        if (tipo == TipoEntidadImagen.EDICION) {
            resp.setHeader("Cache-Control", "private, no-store");
            var sistema = ContextoListener.sistema(req);
            var seleccion = AccesoEdiciones.resolver(sistema, sistema.listarEventos(), "",
                    id == null ? "" : id, AccesoEdiciones.usuario(req));
            if (seleccion == null || seleccion.edicion() == null) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
        }

        DtImagen imagen;
        try {
            imagen = (tipo == null || id == null || id.isBlank()) ? null
                    : (tipo == TipoEntidadImagen.USUARIO || tipo == TipoEntidadImagen.INSTITUCION)
                    ? ClienteUsuarios.get().imagen(tipo == TipoEntidadImagen.USUARIO ? "usuario" : "institucion", id)
                    : ContextoListener.sistema(req).obtenerImagen(tipo, id);
        } catch (ErrorRemotoUsuarios e) {
            resp.sendError(503, "La imagen no está disponible temporalmente.");
            return;
        }

        if (imagen == null) {
            resp.sendRedirect(req.getContextPath() + "/img/default-avatar.png");
            return;
        }

        resp.setContentType(imagen.getTipoContenido());
        resp.setHeader("X-Content-Type-Options", "nosniff");
        if (tipo != TipoEntidadImagen.EDICION) resp.setHeader("Cache-Control", "no-cache");
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