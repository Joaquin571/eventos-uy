package web;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/** Entrada del sitio móvil: el inicio de sesión, o los eventos si ya hay un asistente con sesión. */
@WebServlet("/movil")
public class MovilInicioServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String destino = AccesoMovil.esAsistente(req) ? AccesoMovil.EVENTOS : AccesoMovil.LOGIN;
        resp.sendRedirect(req.getContextPath() + destino);
    }
}
