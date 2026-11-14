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

@WebServlet("/consulta-institucion")
public class ConsultaInstitucionServlet extends HttpServlet {
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setAttribute("instituciones", ClienteUsuarios.get().listarInstituciones());
        String nombre = FormularioUsuario.parametro(req, "nombre");
        if (!nombre.isEmpty()) req.setAttribute("institucion", ClienteUsuarios.get().consultarInstitucion(nombre));
        req.getRequestDispatcher("/WEB-INF/views/consulta-institucion.jsp").forward(req, resp);
    }
}