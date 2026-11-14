package web;

import datatypes.DtEdicion;
import datatypes.DtEvento;
import datatypes.EstadoEdicion;
import datatypes.TipoEntidadImagen;
import interfaces.ISistema;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.format.DateTimeFormatter;

/**
 * Consulta de Evento: /evento?nombre=X
 * Visible para visitante, asistente y organizador.
 */
@WebServlet("/evento")
public class EventoServlet extends HttpServlet {

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String nombre = req.getParameter("nombre");
        ISistema sistema = ContextoListener.sistema(req);

        // No hay "consultarEvento" en ISistema: se busca por nombre en la lista.
        DtEvento evento = null;
        if (nombre != null && !nombre.isBlank()) {
            for (DtEvento candidato : sistema.listarEventos()) {
                if (candidato.getNombre().equals(nombre)) {
                    evento = candidato;
                    break;
                }
            }
        }

        if (evento == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "El evento no existe.");
            return;
        }

        // Solo las ediciones Aceptada son públicas.
        req.setAttribute("evento", evento);
        req.setAttribute("fechaAlta", evento.getFechaAlta() == null
                ? "" : evento.getFechaAlta().format(FECHA));
        req.setAttribute("ediciones",
                sistema.obtenerEdicionesEvento(evento.getNombre(), EstadoEdicion.ACEPTADA));
        req.setAttribute("tieneImagen",
                sistema.obtenerImagen(TipoEntidadImagen.EVENTO, evento.getNombre()) != null);

        req.getRequestDispatcher("/WEB-INF/views/evento.jsp").forward(req, resp);
    }
}
