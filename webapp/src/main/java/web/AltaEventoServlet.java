package web;

import datatypes.DtEvento;
import datatypes.DtImagen;
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
import java.time.LocalDate;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Alta de Evento: /alta-evento (solo organizador, lo controla FiltroAcceso).
 */
@WebServlet("/alta-evento")
@MultipartConfig
public class AltaEventoServlet extends HttpServlet {

    private static final String[] CAMPOS = {"nombre", "sigla", "descripcion"};

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        mostrarFormulario(req, resp, new HashMap<>(), new LinkedHashSet<>(), null);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        Map<String, String> form = new HashMap<>();
        for (String campo : CAMPOS) {
            String valor = req.getParameter(campo);
            form.put(campo, valor == null ? "" : valor.trim());
        }

        Set<String> categorias = new LinkedHashSet<>();
        String[] marcadas = req.getParameterValues("categorias");
        if (marcadas != null) {
            for (String categoria : marcadas) {
                categorias.add(categoria);
            }
        }

        ISistema sistema = ContextoListener.sistema(req);

        try {
            String nombre = form.get("nombre");

            if (nombre.isEmpty()) {
                throw new IllegalArgumentException("El nombre es obligatorio.");
            }
            if (form.get("sigla").isEmpty()) {
                throw new IllegalArgumentException("La sigla es obligatoria.");
            }
            if (form.get("descripcion").isEmpty()) {
                throw new IllegalArgumentException("La descripción es obligatoria.");
            }
            if (categorias.isEmpty()) {
                throw new IllegalArgumentException("Elegí al menos una categoría.");
            }

            // La imagen se valida antes del alta, para no dejar el evento a medias
            DtImagen imagen = Subidas.leerImagen(req, "imagen");
            if (imagen != null) {
                sistema.validarImagen(imagen);
            }

            // La fecha de alta la asigna el sistema con la fecha del día
            DtEvento dt = new DtEvento(
                    nombre, form.get("sigla"), form.get("descripcion"),
                    LocalDate.now(), categorias);

            try {
                if (!sistema.altaEvento(dt)) {
                    throw new IllegalArgumentException("No se pudo dar de alta el evento.");
                }
            } catch (IllegalArgumentException e) {
                throw e;
            } catch (Exception e) {
                // La lógica informa con mensaje claro (nombre repetido, categoría inexistente)
                throw new IllegalArgumentException(e.getMessage());
            }

            String destino = "/alta-evento?creada=" + URLEncoder.encode(nombre, StandardCharsets.UTF_8);

            if (imagen != null) {
                try {
                    sistema.asociarImagen(TipoEntidadImagen.EVENTO, nombre, imagen);
                } catch (IllegalArgumentException e) {
                    destino += "&imagen=error";
                }
            }

            resp.sendRedirect(req.getContextPath() + destino);

        } catch (IllegalArgumentException e) {
            mostrarFormulario(req, resp, form, categorias, e.getMessage());
        }
    }

    private void mostrarFormulario(HttpServletRequest req, HttpServletResponse resp,
                                   Map<String, String> form, Set<String> seleccionadas, String error)
            throws ServletException, IOException {
        req.setAttribute("form", form);
        req.setAttribute("seleccionadas", seleccionadas);
        req.setAttribute("categorias", ContextoListener.sistema(req).listarCategorias());
        req.setAttribute("error", error);
        req.getRequestDispatcher("/WEB-INF/views/alta-evento.jsp").forward(req, resp);
    }
}
