package web;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
@WebServlet(urlPatterns={"","/inicio","/eventos"})
public class InicioServlet extends HttpServlet {
    protected void doGet(HttpServletRequest req,HttpServletResponse resp)throws ServletException,IOException {
        String categoria=req.getParameter("categoria");
        req.setAttribute("eventos",ContextoListener.central(req).listarEventosPorCategoria(categoria));
        req.setAttribute("categoriaSeleccionada",categoria);
        req.getRequestDispatcher("/WEB-INF/views/inicio.jsp").forward(req,resp);
    }
}
