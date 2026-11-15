package web;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.xml.ws.WebServiceException;
import web.soap.ClienteEventosCentral;
import java.io.IOException;

/** Consulta pública de disponibilidad, sin exponer datos personales ni detalles internos. */
@WebServlet("/disponibilidad-usuario")
public class DisponibilidadUsuarioServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");
        resp.setHeader("Cache-Control", "no-store");
        String tipo = req.getParameter("tipo");
        String valor = req.getParameter("valor");
        if (valor == null || valor.isBlank() || valor.length() > 254 ||
                !("nickname".equals(tipo) || "correo".equals(tipo))) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\":\"Consulta inválida\"}");
            return;
        }
        try {
            ClienteEventosCentral cliente = new ClienteEventosCentral();
            boolean disponible = "nickname".equals(tipo)
                    ? cliente.nicknameDisponible(valor)
                    : cliente.correoDisponible(valor);
            resp.getWriter().write("{\"disponible\":" + disponible + "}");
        } catch (WebServiceException error) {
            resp.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            resp.getWriter().write("{\"error\":\"Servicio no disponible\"}");
        }
    }
}
