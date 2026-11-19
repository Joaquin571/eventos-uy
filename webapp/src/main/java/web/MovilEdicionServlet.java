package web;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Consulta de Edición de Evento, detalle móvil. Usa las mismas operaciones SOAP que el sitio de escritorio
 * (consultarEdiciones y consultarDetalleEdicion). Si el asistente está registrado, muestra su registro.
 *
 * PENDIENTE (Emmanuel): el video. Cuando DtEdicion exponga el video, este servlet debe publicar el atributo
 * "urlVideoEmbed" con la dirección embebida; fragmentos/video.jsp ya lo muestra en 16:9 si existe.
 */
@WebServlet("/movil/edicion")
public class MovilEdicionServlet extends HttpServlet {
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        var asistente = AccesoMovil.requerirAsistente(req, resp);
        if (asistente == null) return;
        String nombre = AccesoEdiciones.texto(req, "nombre");
        if (nombre.isEmpty()) {
            resp.sendRedirect(req.getContextPath() + AccesoMovil.EVENTOS);
            return;
        }
        var central = ContextoListener.central(req);
        var seleccion = central.consultarEdiciones(AccesoEdiciones.texto(req, "evento"), nombre);
        if (seleccion.getEdicion() == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        var detalle = central.consultarDetalleEdicion(seleccion.getEdicion().getIdNombre());
        var edicion = detalle.getEdicion();
        req.setAttribute("eventoElegido", seleccion.getEvento());
        req.setAttribute("detalle", edicion);
        req.setAttribute("fechaInicio", fecha(edicion.getFechaInicio()));
        req.setAttribute("fechaFin", fecha(edicion.getFechaFin()));
        req.setAttribute("organizador", detalle.getOrganizador());
        req.setAttribute("tieneImagen", detalle.isTieneImagen());
        req.setAttribute("tipos", detalle.getTipos());
        req.setAttribute("patrocinadoras", detalle.getPatrocinadoras());
        if (detalle.getRegistroPropio() != null) {
            req.setAttribute("registroPropio", DetalleRegistroVista.de(detalle.getRegistroPropio(), asistente.getNombre()));
        }
        req.getRequestDispatcher("/WEB-INF/views/movil/edicion.jsp").forward(req, resp);
    }

    private String fecha(LocalDate valor) {
        return valor == null ? "" : valor.format(FECHA);
    }
}
