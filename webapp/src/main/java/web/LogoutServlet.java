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

@WebServlet("/logout")
public class LogoutServlet extends HttpServlet {
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession sesion = req.getSession(false);
        boolean movil = sesion != null && Boolean.TRUE.equals(sesion.getAttribute("modoMovil"));
        try {
            String token = ClienteUsuarios.token(req);
            if (token != null) ClienteUsuarios.get().cerrarSesion(token);
        } catch (ErrorRemotoUsuarios e) {
            getServletContext().log("No se pudo notificar el cierre de sesión al central.", e);
        } finally {
            if (sesion != null) sesion.invalidate();
        }
        resp.sendRedirect(req.getContextPath() + (movil ? "/login?movil=true" : "/usuarios"));
    }
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setHeader("Allow", "POST");
        resp.sendError(405);
    }
}