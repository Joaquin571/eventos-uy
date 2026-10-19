package web;

import datatypes.DtUsuario;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String identificador = req.getParameter("identificador");
        String contrasenia = req.getParameter("contrasenia");

        try {
            DtUsuario usuario = ContextoListener.sistema(req)
                    .autenticar(identificador, contrasenia);

            // Sesión nueva al iniciar (evita reutilizar un identificador viejo)
            HttpSession anterior = req.getSession(false);
            if (anterior != null) {
                anterior.invalidate();
            }

            req.getSession(true).setAttribute("usuario", usuario);
            resp.sendRedirect(req.getContextPath() + "/");

        } catch (IllegalArgumentException e) {
            req.setAttribute("error", e.getMessage());
            req.setAttribute("identificador", identificador);
            doGet(req, resp);
        }
    }
}