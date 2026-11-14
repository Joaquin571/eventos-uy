package web;

import datatypes.*;
import web.remoto.ClienteUsuarios;
import web.remoto.ErrorRemotoUsuarios;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@WebServlet("/alta-patrocinio")
public class AltaPatrocinioServlet extends HttpServlet {
    private static final String[] CAMPOS = {"evento", "edicion", "institucion", "nivel", "tipoRegistro", "aporte", "registrograt", "codigo"};
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Map<String, String> form = new HashMap<>();
        for (String campo : new String[]{"evento", "edicion"}) form.put(campo, FormularioUsuario.parametro(req, campo));
        mostrar(req, resp, form, null);
    }
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Map<String, String> form = new HashMap<>();
        for (String campo : CAMPOS) form.put(campo, FormularioUsuario.parametro(req, campo));
        try {
            float aporte;
            int gratuitos;
            clases.NivelPatrocinio nivel;
            try {
                aporte = Float.parseFloat(form.get("aporte"));
                gratuitos = Integer.parseInt(form.get("registrograt"));
                nivel = clases.NivelPatrocinio.valueOf(form.get("nivel"));
            } catch (IllegalArgumentException e) { throw new IllegalArgumentException("Revisá el aporte, los registros gratuitos y el nivel seleccionado."); }
            DtPatrocinio datos = new DtPatrocinio(null, aporte, gratuitos, form.get("codigo"), nivel,
                    form.get("institucion"), form.get("edicion"), form.get("tipoRegistro"));
            ClienteUsuarios.get().altaPatrocinio(ClienteUsuarios.token(req), form.get("evento"), datos);
            resp.sendRedirect(req.getContextPath() + "/consulta-patrocinio?edicion="
                    + URLEncoder.encode(form.get("edicion"), StandardCharsets.UTF_8) + "&institucion="
                    + URLEncoder.encode(form.get("institucion"), StandardCharsets.UTF_8) + "&exito=true");
        } catch (IllegalArgumentException e) {
            FormularioUsuario.verificarError(e);
            mostrar(req, resp, form, e.getMessage());
        }
    }
    private void mostrar(HttpServletRequest req, HttpServletResponse resp, Map<String, String> form, String error) throws ServletException, IOException {
        DtFormularioPatrocinio opciones = ClienteUsuarios.get().formularioPatrocinio(ClienteUsuarios.token(req), form.get("evento"), form.get("edicion"));
        form.put("evento", opciones.getEventoElegido());
        req.setAttribute("form", form);
        req.setAttribute("error", error);
        req.setAttribute("eventos", opciones.getEventos());
        req.setAttribute("ediciones", opciones.getEdiciones());
        req.setAttribute("tipos", opciones.getTipos());
        req.setAttribute("instituciones", opciones.getInstituciones());
        req.setAttribute("niveles", clases.NivelPatrocinio.values());
        req.setAttribute("tipoNoDisponible", !form.getOrDefault("tipoRegistro", "").isEmpty()
                && opciones.getTipos().stream().noneMatch(t -> t.getIdNombre().equals(form.get("tipoRegistro"))));
        req.setAttribute("institucionNoDisponible", !form.getOrDefault("institucion", "").isEmpty()
                && opciones.getInstituciones().stream().noneMatch(i -> i.getNombre().equals(form.get("institucion"))));
        req.setAttribute("nivelNoDisponible", !form.getOrDefault("nivel", "").isEmpty()
                && Arrays.stream(clases.NivelPatrocinio.values()).noneMatch(n -> n.name().equals(form.get("nivel"))));
        req.getRequestDispatcher("/WEB-INF/views/alta-patrocinio.jsp").forward(req, resp);
    }
}