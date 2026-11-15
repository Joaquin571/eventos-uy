package web;

import contratos.dto.NivelPatrocinio;
import contratos.dto.*;
import contratos.ServicioPortal;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.*;

@WebServlet("/alta-patrocinio")
public class AltaPatrocinioServlet extends HttpServlet {
    private static final String[] CAMPOS = {"evento", "edicion", "institucion", "nivel",
            "tipoRegistro", "aporte", "registrograt", "codigo"};

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        DtUsuario usuario = AccesoPatrocinios.usuarioDeSesion(req);
        if (!(usuario instanceof DtOrganizador)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        Map<String, String> form = new HashMap<>();
        // El código y los importes solo se reciben por POST.
        for (String campo : new String[]{"evento", "edicion"}) form.put(campo, texto(req, campo));
        mostrarFormulario(req, resp, usuario, form, null);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        DtUsuario usuario = AccesoPatrocinios.usuarioDeSesion(req);
        if (!(usuario instanceof DtOrganizador)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        ServicioPortal sistema = ContextoListener.central(req);
        String edicion = texto(req, "edicion");
        Map<String, String> form = new HashMap<>();
        for (String campo : CAMPOS) form.put(campo, texto(req, campo));
        try {
            String evento = form.get("evento");
            float aporte;
            int gratuitos;
            try {
                aporte = Float.parseFloat(form.get("aporte"));
                gratuitos = Integer.parseInt(form.get("registrograt"));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Ingresá un aporte numérico y una cantidad entera de registros gratuitos.");
            }
            NivelPatrocinio nivel;
            try {
                nivel = NivelPatrocinio.valueOf(form.get("nivel"));
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Seleccioná un nivel de patrocinio válido.");
            }
            DtPatrocinio dt = new DtPatrocinio(LocalDate.now(), aporte, gratuitos, form.get("codigo"),
                    nivel, form.get("institucion"), edicion, form.get("tipoRegistro"));
            sistema.altaPatrocinio(dt, evento);
            resp.sendRedirect(req.getContextPath() + "/consulta-patrocinio?edicion="
                    + URLEncoder.encode(edicion, StandardCharsets.UTF_8) + "&institucion="
                    + URLEncoder.encode(form.get("institucion"), StandardCharsets.UTF_8) + "&exito=true");
        } catch (IllegalArgumentException e) {
            mostrarFormulario(req, resp, usuario, form, e.getMessage());
        }
    }

    private void mostrarFormulario(HttpServletRequest req, HttpServletResponse resp, DtUsuario usuario,
                                   Map<String, String> form, String error) throws ServletException, IOException {
        ServicioPortal sistema = ContextoListener.central(req);
        String edicion = form.getOrDefault("edicion", "");
        DtOpcionesAltaPatrocinio opciones;
        opciones = sistema.obtenerOpcionesAltaPatrocinio(form.getOrDefault("evento", ""), edicion);
        form.put("evento", opciones.getEvento());
        req.setAttribute("form", form);
        req.setAttribute("error", error);
        req.setAttribute("eventos", opciones.getEventos());
        req.setAttribute("ediciones", opciones.getEdiciones());
        Collection<DtTipoRegistro> tipos = opciones.getTipos();
        Collection<DtInstitucion> instituciones = opciones.getInstituciones();
        req.setAttribute("tipos", tipos);
        req.setAttribute("instituciones", instituciones);
        req.setAttribute("tipoNoDisponible", !form.getOrDefault("tipoRegistro", "").isEmpty()
                && tipos.stream().noneMatch(t -> t.getIdNombre().equals(form.get("tipoRegistro"))));
        req.setAttribute("institucionNoDisponible", !form.getOrDefault("institucion", "").isEmpty()
                && instituciones.stream().noneMatch(i -> i.getNombre().equals(form.get("institucion"))));
        req.setAttribute("nivelNoDisponible", !form.getOrDefault("nivel", "").isEmpty()
                && Arrays.stream(NivelPatrocinio.values()).noneMatch(n -> n.name().equals(form.get("nivel"))));
        req.setAttribute("niveles", NivelPatrocinio.values());
        req.getRequestDispatcher("/WEB-INF/views/alta-patrocinio.jsp").forward(req, resp);
    }

    private String texto(HttpServletRequest req, String campo) {
        String valor = req.getParameter(campo);
        return valor == null ? "" : valor.trim();
    }
}
