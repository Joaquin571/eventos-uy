package web;

import contratos.dto.DtEdicion;
import contratos.dto.DtEvento;
import contratos.dto.EstadoEdicion;
import contratos.dto.TipoEntidadImagen;
import contratos.ServicioPortal;
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
        if (nombre == null || nombre.isBlank()) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        ServicioPortal sistema = ContextoListener.central(req);

        var seleccion=sistema.consultarEdiciones(nombre, "");
        DtEvento evento=seleccion.getEventos().stream().filter(e->e.getNombre().equals(seleccion.getEvento())).findFirst().orElseThrow();
        req.setAttribute("evento", evento);
        req.setAttribute("fechaAlta", evento.getFechaAlta() == null
                ? "" : evento.getFechaAlta().format(FECHA));
        req.setAttribute("ediciones",
                seleccion.getEdiciones());
        req.setAttribute("tieneImagen",
                sistema.obtenerImagen(TipoEntidadImagen.EVENTO, evento.getNombre()) != null);

        req.getRequestDispatcher("/WEB-INF/views/evento.jsp").forward(req, resp);
    }
}
