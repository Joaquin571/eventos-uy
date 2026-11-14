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

@WebServlet("/alta-usuario")
@MultipartConfig(maxFileSize = 2L * 1024 * 1024, maxRequestSize = 3L * 1024 * 1024)
public class AltaUsuarioServlet extends HttpServlet {
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        mostrar(req, resp, new HashMap<>(), null);
    }
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Map<String, String> form = FormularioUsuario.leer(req);
        try {
            ClienteUsuarios.get().altaUsuario(FormularioUsuario.datos(req, form, null), Subidas.leerImagen(req, "imagen"));
            resp.sendRedirect(req.getContextPath() + "/login?registrado=ok");
        } catch (IllegalArgumentException e) {
            FormularioUsuario.verificarError(e);
            mostrar(req, resp, form, e.getMessage());
        }
    }
    private void mostrar(HttpServletRequest req, HttpServletResponse resp, Map<String, String> form, String error)
            throws ServletException, IOException {
        req.setAttribute("form", form);
        req.setAttribute("error", error);
        req.setAttribute("instituciones", ClienteUsuarios.get().listarInstituciones());
        req.getRequestDispatcher("/WEB-INF/views/alta-usuario.jsp").forward(req, resp);
    }
}