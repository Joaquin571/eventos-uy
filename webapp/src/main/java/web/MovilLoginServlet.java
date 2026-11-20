package web;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import web.remoto.ErrorCentral;

import java.io.IOException;

/**
 * Inicio de sesión móvil. Reutiliza la sesión remota P03 (ServicioSesiones) y la cookie de sesión
 * HttpOnly de Tomcat. Solo se acepta el rol ASISTENTE; si Central autentica a otro rol, la credencial
 * recién emitida se revoca y no se crea ninguna sesión local.
 */
@WebServlet("/movil/login")
public class MovilLoginServlet extends HttpServlet {
    static final String MENSAJE_ROL = "Desde el celular solo pueden iniciar sesión los asistentes.";
    static final String MENSAJE_VENCIDA = "Tu sesión venció. Iniciá sesión nuevamente.";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (AccesoMovil.esAsistente(req)) {
            resp.sendRedirect(req.getContextPath() + AccesoMovil.EVENTOS);
            return;
        }
        String motivo = req.getParameter("motivo");
        if ("rol".equals(motivo)) req.setAttribute("error", MENSAJE_ROL);
        else if ("vencida".equals(motivo)) req.setAttribute("aviso", MENSAJE_VENCIDA);
        mostrar(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String identificador = req.getParameter("identificador");
        try {
            SesionWeb.iniciarMovil(req, identificador, req.getParameter("contrasenia"));
            resp.sendRedirect(req.getContextPath() + AccesoMovil.EVENTOS);
        } catch (ErrorCentral e) {
            if (!"ACCESO_DENEGADO".equals(e.getCodigo())) throw e;
            req.setAttribute("error", MENSAJE_ROL);
            req.setAttribute("identificador", identificador);
            mostrar(req, resp);
        } catch (IllegalArgumentException e) {
            req.setAttribute("error", e.getMessage());
            req.setAttribute("identificador", identificador);
            mostrar(req, resp);
        }
    }

    private void mostrar(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.getRequestDispatcher("/WEB-INF/views/movil/login.jsp").forward(req, resp);
    }
}
