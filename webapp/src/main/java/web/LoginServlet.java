package web;

import datatypes.*;
import web.remoto.ClienteUsuarios;
import web.remoto.ErrorRemotoUsuarios;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if ("true".equals(req.getParameter("movil"))) req.getSession().setAttribute("modoMovil", true);
        req.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(req, resp);
    }
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        boolean movil = Boolean.TRUE.equals(req.getSession().getAttribute("modoMovil"));
        try {
            DtSesion ingreso = ClienteUsuarios.get().iniciarSesion(req.getParameter("identificador"), req.getParameter("contrasenia"), movil);
            req.getSession().invalidate();
            HttpSession sesion = req.getSession(true);
            sesion.setAttribute("usuario", ingreso.getUsuario());
            sesion.setAttribute("tokenUsuarios", ingreso.getToken());
            sesion.setAttribute("modoMovil", movil);
            sesion.setMaxInactiveInterval(30 * 60);
            resp.sendRedirect(req.getContextPath() + "/usuario?nick=" + URLEncoder.encode(ingreso.getUsuario().getNickname(), StandardCharsets.UTF_8));
        } catch (IllegalArgumentException e) {
            FormularioUsuario.verificarError(e);
            req.setAttribute("error", e.getMessage());
            req.setAttribute("identificador", req.getParameter("identificador"));
            doGet(req, resp);
        }
    }
}