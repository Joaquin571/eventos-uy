package web;

import clases.NivelPatrocinio;
import datatypes.*;
import interfaces.ISistema;
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
        ISistema sistema = ContextoListener.sistema(req);
        String edicion = texto(req, "edicion");
        if (!AccesoPatrocinios.esEdicionPropia(sistema, usuario, edicion)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        Map<String, String> form = new HashMap<>();
        for (String campo : CAMPOS) form.put(campo, texto(req, campo));
        try {
            String evento = form.get("evento");
            if (!evento.isEmpty() && sistema.obtenerEdicionesEvento(evento).stream()
                    .noneMatch(e -> edicion.equals(e.getIdNombre()))) {
                throw new IllegalArgumentException("La edición no pertenece al evento seleccionado.");
            }
            for (String campo : new String[]{"institucion", "nivel", "tipoRegistro", "aporte", "registrograt", "codigo"}) {
                if (form.get(campo).isBlank()) throw new IllegalArgumentException("Todos los campos del patrocinio son obligatorios.");
            }
            float aporte;
            int gratuitos;
            try {
                aporte = Float.parseFloat(form.get("aporte"));
                gratuitos = Integer.parseInt(form.get("registrograt"));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Ingresá un aporte numérico y una cantidad entera de registros gratuitos.");
            }
            if (!Float.isFinite(aporte) || aporte <= 0) {
                throw new IllegalArgumentException("El aporte económico debe ser un número finito mayor que cero.");
            }
            if (gratuitos < 0) throw new IllegalArgumentException("La cantidad de registros gratuitos no puede ser negativa.");
            NivelPatrocinio nivel;
            try {
                nivel = NivelPatrocinio.valueOf(form.get("nivel"));
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Seleccioná un nivel de patrocinio válido.");
            }
            if (sistema.consultarTipoRegistro(edicion, form.get("tipoRegistro")) == null) {
                throw new IllegalArgumentException("El tipo de registro no pertenece a la edición seleccionada.");
            }
            DtPatrocinio dt = new DtPatrocinio(LocalDate.now(), aporte, gratuitos, form.get("codigo"),
                    nivel, form.get("institucion"), edicion, form.get("tipoRegistro"));
            if (!sistema.altaPatrocinio(dt)) throw new IllegalArgumentException("No se pudo registrar el patrocinio.");
            resp.sendRedirect(req.getContextPath() + "/consulta-patrocinio?edicion="
                    + URLEncoder.encode(edicion, StandardCharsets.UTF_8) + "&institucion="
                    + URLEncoder.encode(form.get("institucion"), StandardCharsets.UTF_8) + "&exito=true");
        } catch (IllegalArgumentException e) {
            mostrarFormulario(req, resp, usuario, form, e.getMessage());
        }
    }

    private void mostrarFormulario(HttpServletRequest req, HttpServletResponse resp, DtUsuario usuario,
                                   Map<String, String> form, String error) throws ServletException, IOException {
        ISistema sistema = ContextoListener.sistema(req);
        String edicion = form.getOrDefault("edicion", "");
        if (!edicion.isEmpty() && !AccesoPatrocinios.esEdicionPropia(sistema, usuario, edicion)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        Collection<DtEvento> eventos = sistema.listarEventos();
        String evento = form.getOrDefault("evento", "");
        // Resuelve enlaces directos y reconstruye una selección incompatible tras un error.
        if (!edicion.isEmpty()) {
            for (DtEvento candidato : eventos) {
                if (sistema.obtenerEdicionesEvento(candidato.getNombre()).stream()
                        .anyMatch(e -> edicion.equals(e.getIdNombre()))) {
                    evento = candidato.getNombre();
                    break;
                }
            }
        }
        form.put("evento", evento);
        List<DtEdicion> propias = new ArrayList<>();
        if (!evento.isEmpty()) {
            for (DtEdicion candidata : sistema.obtenerEdicionesEvento(evento)) {
                if (AccesoPatrocinios.esPropietario(usuario, candidata)) propias.add(candidata);
            }
        }
        req.setAttribute("form", form);
        req.setAttribute("error", error);
        req.setAttribute("eventos", eventos);
        req.setAttribute("ediciones", propias);
        Collection<DtTipoRegistro> tipos = edicion.isEmpty() ? List.of() : sistema.obtenerTiposRegistroEdicion(edicion);
        Collection<DtInstitucion> instituciones = sistema.listarInstituciones();
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
