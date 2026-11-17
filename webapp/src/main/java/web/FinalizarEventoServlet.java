package web;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;

@WebServlet("/finalizar-evento")
public class FinalizarEventoServlet extends HttpServlet {
    @Override protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        MensajesFlash.mostrar(req, "/finalizar-evento");
        req.setAttribute("eventos", ContextoListener.central(req).listarEventos());
        req.getRequestDispatcher("/WEB-INF/views/finalizar-evento.jsp").forward(req, resp);
    }
    @Override protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            ContextoListener.central(req).finalizarEvento(req.getParameter("evento"));
            MensajesFlash.guardar(req,"/finalizar-evento","Evento finalizado correctamente.",null);
            resp.sendRedirect(req.getContextPath()+"/finalizar-evento");
        } catch (IllegalArgumentException e) {
            req.setAttribute("error",e.getMessage());
            doGet(req,resp);
        }
    }
}
