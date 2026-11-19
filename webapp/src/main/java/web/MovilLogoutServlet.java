package web;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/** Cierre de sesión móvil: revoca P03, elimina la sesión local y vuelve al inicio de sesión móvil. */
@WebServlet("/movil/logout")
public class MovilLogoutServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        var sesion = req.getSession(false);
        String credencial = SesionWeb.credencial(req);
        try {
            if (credencial != null) ContextoListener.sesiones(req).cerrar(credencial);
        } finally {
            if (sesion != null) sesion.invalidate();
        }
        resp.sendRedirect(req.getContextPath() + AccesoMovil.LOGIN);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setHeader("Allow", "POST");
        resp.sendError(405);
    }
}
