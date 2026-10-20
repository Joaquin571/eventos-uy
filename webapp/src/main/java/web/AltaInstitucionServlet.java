package web;

import datatypes.DtImagen;
import datatypes.DtInstitucion;
import datatypes.TipoEntidadImagen;
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
import java.util.regex.Pattern;

@WebServlet("/alta-institucion")
@MultipartConfig
public class AltaInstitucionServlet extends HttpServlet {

    private static final Pattern URL =
            Pattern.compile("^https?://[^\\s.]+\\.[^\\s]+$", Pattern.CASE_INSENSITIVE);

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

            if (nombre.isEmpty()) {
                throw new IllegalArgumentException("El nombre es obligatorio.");
            }
            if (form.get("descripcion").isEmpty()) {
                throw new IllegalArgumentException("La descripción es obligatoria.");
            }
            if (form.get("sitioWeb").isEmpty()) {
                throw new IllegalArgumentException("El sitio web es obligatorio.");
            }
            if (!URL.matcher(form.get("sitioWeb")).matches()) {
                throw new IllegalArgumentException(
                        "Ingresá una URL válida (por ejemplo, https://ejemplo.com).");
            }

            // La imagen se valida antes de crear la institución, para no dejarla a medias
            DtImagen imagen = Subidas.leerImagen(req, "imagen");

            if (imagen != null) {
                sistema.validarImagen(imagen);
            }

            if (sistema.existeInstitucion(nombre)) {
                throw new IllegalArgumentException("Ya existe una institución con ese nombre.");
            }

            boolean alta = sistema.altaInstitucion(
                    new DtInstitucion(nombre, form.get("descripcion"), form.get("sitioWeb")));

            if (!alta) {
                throw new IllegalArgumentException("Ya existe una institución con ese nombre.");
            }

            String destino = "/alta-institucion?creada="
                    + URLEncoder.encode(nombre, StandardCharsets.UTF_8);

            if (imagen != null) {
                try {
                    sistema.asociarImagen(TipoEntidadImagen.INSTITUCION, nombre, imagen);
                } catch (IllegalArgumentException e) {
                    destino += "&imagen=error";
                }
            }

            resp.sendRedirect(req.getContextPath() + destino);

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