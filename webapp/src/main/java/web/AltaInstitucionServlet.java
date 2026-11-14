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

@WebServlet("/alta-institucion")
@MultipartConfig(maxFileSize = 2L * 1024 * 1024, maxRequestSize = 3L * 1024 * 1024)
public class AltaInstitucionServlet extends HttpServlet {
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        mostrar(req, resp, new HashMap<>(), null);
    }
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Map<String, String> form = new HashMap<>();
        for (String campo : new String[]{"nombre", "descripcion", "sitioWeb"}) form.put(campo, FormularioUsuario.parametro(req, campo));
        try {
            ClienteUsuarios.get().altaInstitucion(ClienteUsuarios.token(req),
                    new DtInstitucion(form.get("nombre"), form.get("descripcion"), form.get("sitioWeb")), Subidas.leerImagen(req, "imagen"));
            resp.sendRedirect(req.getContextPath() + "/alta-institucion?creada=" + URLEncoder.encode(form.get("nombre"), StandardCharsets.UTF_8));
        } catch (IllegalArgumentException e) {
            FormularioUsuario.verificarError(e);
            mostrar(req, resp, form, e.getMessage());
        }
    }
    private void mostrar(HttpServletRequest req, HttpServletResponse resp, Map<String, String> form, String error) throws ServletException, IOException {
        req.setAttribute("form", form);
        req.setAttribute("error", error);
        req.getRequestDispatcher("/WEB-INF/views/alta-institucion.jsp").forward(req, resp);
    }
}