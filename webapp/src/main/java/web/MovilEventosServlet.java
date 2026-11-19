package web;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Consulta de Edición de Evento, primera parte: eventos y, al elegir uno, sus ediciones Aceptadas.
 * La visibilidad la resuelve Central (consultarEdiciones); aquí solo se arma la vista.
 */
@WebServlet("/movil/eventos")
public class MovilEventosServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (AccesoMovil.requerirAsistente(req, resp) == null) return;
        var seleccion = ContextoListener.central(req).consultarEdiciones(AccesoEdiciones.texto(req, "evento"), "");
        req.setAttribute("eventos", seleccion.getEventos());
        req.setAttribute("eventoElegido", seleccion.getEvento());
        req.setAttribute("ediciones", seleccion.getEdiciones());
        req.getRequestDispatcher("/WEB-INF/views/movil/eventos.jsp").forward(req, resp);
    }
}
