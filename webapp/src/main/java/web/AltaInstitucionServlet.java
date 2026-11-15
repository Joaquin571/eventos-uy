package web;

import datatypes.DtImagen;
import datatypes.DtInstitucion;
import interfaces.ISistema;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@WebServlet("/alta-institucion")
@MultipartConfig(maxFileSize = 2L * 1024 * 1024, maxRequestSize = 3L * 1024 * 1024)
public class AltaInstitucionServlet extends HttpServlet {

    private static final String[] CAMPOS = {"nombre", "descripcion", "sitioWeb"};

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        mostrarFormulario(req, resp, new HashMap<>(), null);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        Map<String, String> form = new HashMap<>();
        for (String campo : CAMPOS) {
            String valor = req.getParameter(campo);
            form.put(campo, valor == null ? "" : valor.trim());
        }

        ISistema sistema = ContextoListener.sistema(req);

        try {
            String nombre = form.get("nombre");

            DtImagen imagen = Subidas.leerImagen(req, "imagen");
            var sesion = req.getSession(false);
            var usuario = sesion == null ? null : (datatypes.DtUsuario) sesion.getAttribute("usuario");
            boolean alta = sistema.altaInstitucion(
                    new DtInstitucion(nombre, form.get("descripcion"), form.get("sitioWeb")), imagen,
                    usuario == null ? null : datatypes.Actor.usuarioAutenticado(usuario));
            if (!alta) throw new IllegalArgumentException("Ya existe una instituci?n con ese nombre.");

            String destino = "/alta-institucion?creada="
                    + URLEncoder.encode(nombre, StandardCharsets.UTF_8);

            resp.sendRedirect(req.getContextPath() + destino);

        } catch (excepciones.AccesoDenegadoException e) {
            resp.sendError(403);
        } catch (IllegalArgumentException e) {
            mostrarFormulario(req, resp, form, e.getMessage());
        }
    }

    private void mostrarFormulario(HttpServletRequest req, HttpServletResponse resp,
                                   Map<String, String> form, String error)
            throws ServletException, IOException {
        req.setAttribute("form", form);
        req.setAttribute("error", error);
        req.getRequestDispatcher("/WEB-INF/views/alta-institucion.jsp").forward(req, resp);
    }
}