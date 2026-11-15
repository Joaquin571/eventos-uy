package web;

import contratos.dto.DtEvento;
import contratos.dto.DtImagen;
import contratos.dto.TipoEntidadImagen;
import contratos.ServicioPortal;
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
@MultipartConfig(maxFileSize = 2L * 1024 * 1024, maxRequestSize = 3L * 1024 * 1024)
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

        ServicioPortal sistema = ContextoListener.central(req);

        try {
            String nombre = form.get("nombre");

            DtImagen imagen = Subidas.leerImagen(req, "imagen");
            DtEvento dt = new DtEvento(nombre, form.get("sigla"), form.get("descripcion"), null, categorias);
            sistema.altaEvento(dt, imagen);
            String destino = "/alta-evento?creada=" + URLEncoder.encode(nombre, StandardCharsets.UTF_8);
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
        req.setAttribute("categorias", ContextoListener.central(req).listarCategorias());
        req.setAttribute("error", error);
        req.getRequestDispatcher("/WEB-INF/views/alta-evento.jsp").forward(req, resp);
    }
}
