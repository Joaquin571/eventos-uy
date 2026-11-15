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

@WebServlet("/consulta-institucion")
public class ConsultaInstitucionServlet extends HttpServlet {
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setAttribute("instituciones", ContextoListener.central(req).listarInstituciones());
        String nombre = AccesoEdiciones.texto(req, "nombre");
        if (!nombre.isEmpty()) req.setAttribute("institucion", ContextoListener.central(req).consultarInstitucion(nombre));
        req.getRequestDispatcher("/WEB-INF/views/consulta-institucion.jsp").forward(req, resp);
    }
}