package web;

import datatypes.DtEvento;
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

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String categoria = req.getParameter("categoria");
        Collection<DtEvento> eventos = new ArrayList<>();

        for (DtEvento evento : ContextoListener.sistema(req).listarEventos()) {
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