package web;




import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@WebServlet("/disponibilidad-usuario")
public class DisponibilidadUsuarioServlet extends HttpServlet {
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");
        resp.setHeader("Cache-Control", "no-store");
        String campo = req.getParameter("campo");
        String valor = req.getParameter("valor");
        boolean disponible;
        if ("nickname".equals(campo)) disponible = ContextoListener.central(req).disponibleNickname(valor);
        else if ("correo".equals(campo)) disponible = ContextoListener.central(req).disponibleCorreo(valor);
        else {
            resp.setStatus(400);
            resp.getWriter().write("{\"error\":\"Campo inválido\"}");
            return;
        }
        resp.getWriter().write("{\"disponible\":" + disponible + "}");
    }
}