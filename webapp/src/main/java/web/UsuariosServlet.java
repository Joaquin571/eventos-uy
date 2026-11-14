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

@WebServlet("/usuarios")
public class UsuariosServlet extends HttpServlet {
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        List<DtAsistente> asistentes = new ArrayList<>();
        List<DtOrganizador> organizadores = new ArrayList<>();
        for (DtUsuario usuario : ClienteUsuarios.get().listarUsuarios()) {
            if (usuario instanceof DtAsistente a) asistentes.add(a);
            else if (usuario instanceof DtOrganizador o) organizadores.add(o);
        }
        req.setAttribute("asistentes", asistentes);
        req.setAttribute("organizadores", organizadores);
        req.getRequestDispatcher("/WEB-INF/views/usuarios.jsp").forward(req, resp);
    }
}