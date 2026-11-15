package web;

import contratos.dto.*;
import contratos.ServicioPortal;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.*;

@WebServlet("/consulta-patrocinio")
public class ConsultaPatrocinioServlet extends HttpServlet {
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        ServicioPortal sistema = ContextoListener.central(req);
        DtUsuario usuario = AccesoPatrocinios.usuarioDeSesion(req);
        String edicion = texto(req, "edicion");
        String institucion = texto(req, "institucion");
        String evento = texto(req, "evento");
        DtConsultaPatrocinios consulta = sistema.consultarPatrocinios(evento, edicion, institucion);
        if (consulta == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        req.setAttribute("patrocinadoras", consulta.getPatrocinadoras());
        DtPatrocinio patrocinio = consulta.getPatrocinio();
        if (patrocinio != null) {
            req.setAttribute("patrocinio", patrocinio);
            req.setAttribute("esOrganizadorDuenio", consulta.isPuedeVerCodigo());
            req.setAttribute("fechaAlta", patrocinio.getFecha() == null ? "" : patrocinio.getFecha().format(FECHA));
            req.setAttribute("aporte", String.format(Locale.forLanguageTag("es-UY"), "%.2f", patrocinio.getMontoAporte()));
        }
        req.setAttribute("eventos", consulta.getEventos());
        req.setAttribute("ediciones", consulta.getEdiciones());
        req.setAttribute("eventoElegido", consulta.getEvento());
        req.setAttribute("edicionElegida", edicion);
        req.setAttribute("institucionElegida", institucion);
        req.getRequestDispatcher("/WEB-INF/views/consulta-patrocinio.jsp").forward(req, resp);
    }

    private String texto(HttpServletRequest req, String campo) {
        String valor = req.getParameter(campo);
        return valor == null ? "" : valor.trim();
    }
}
