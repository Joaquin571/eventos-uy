package web;

import web.soap.ClienteEventosCentral;
import web.soap.generado.EventoSoap;
import jakarta.xml.ws.WebServiceException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

@WebServlet(urlPatterns = {"", "/inicio"})
public class InicioServlet extends HttpServlet {
    private final ClienteEventosCentral cliente;

    public InicioServlet() { this(new ClienteEventosCentral()); }
    InicioServlet(ClienteEventosCentral cliente) { this.cliente = cliente; }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String categoria = req.getParameter("categoria");
        Collection<EventoSoap> eventos = new ArrayList<>();

        Collection<EventoSoap> disponibles;
        try {
            disponibles = cliente.listarEventos();
        } catch (WebServiceException error) {
            resp.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            req.getRequestDispatcher("/WEB-INF/views/servicio-no-disponible.jsp").forward(req, resp);
            return;
        }

        for (EventoSoap evento : disponibles) {
            boolean sinFiltro = categoria == null || categoria.isBlank();
            boolean coincide = evento.getCategorias() != null
                    && evento.getCategorias().contains(categoria);

            if (sinFiltro || coincide) {
                eventos.add(evento);
            }
        }

        req.setAttribute("eventos", eventos);
        req.setAttribute("categoriaSeleccionada", categoria);
        req.getRequestDispatcher("/WEB-INF/views/inicio.jsp").forward(req, resp);
    }
}
