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

@WebServlet("/consulta-patrocinio")
public class ConsultaPatrocinioServlet extends HttpServlet {
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String edicion = FormularioUsuario.parametro(req, "edicion");
        String institucion = FormularioUsuario.parametro(req, "institucion");
        DtConsultaPatrocinio consulta = ClienteUsuarios.get().consultarPatrocinio(ClienteUsuarios.token(req),
                FormularioUsuario.parametro(req, "evento"), edicion, institucion);
        req.setAttribute("eventos", consulta.getEventos());
        req.setAttribute("ediciones", consulta.getEdiciones());
        req.setAttribute("patrocinadoras", consulta.getPatrocinadoras());
        req.setAttribute("patrocinio", consulta.getPatrocinio());
        req.setAttribute("esOrganizadorDuenio", consulta.isOrganizadorDuenio());
        req.setAttribute("eventoElegido", consulta.getEventoElegido());
        req.setAttribute("edicionElegida", edicion);
        req.setAttribute("institucionElegida", institucion);
        DtPatrocinio p = consulta.getPatrocinio();
        if (p != null) {
            req.setAttribute("fechaAlta", p.getFecha() == null ? "" : p.getFecha().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            req.setAttribute("aporte", String.format(Locale.forLanguageTag("es-UY"), "%.2f", p.getMontoAporte()));
        }
        req.getRequestDispatcher("/WEB-INF/views/consulta-patrocinio.jsp").forward(req, resp);
    }
}