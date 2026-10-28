package web;

import clases.NivelPatrocinio;
import datatypes.DtPatrocinio;
import interfaces.ISistema;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.LocalDate;

@WebServlet("/alta-patrocinio")
public class AltaPatrocinioServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        ISistema sistema = ContextoListener.sistema(req);
        req.setAttribute("eventos", sistema.listarEventos());
        req.setAttribute("instituciones", sistema.listarInstituciones());
        req.getRequestDispatcher("/WEB-INF/views/alta-patrocinio.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        ISistema sistema = ContextoListener.sistema(req);

        try {
            String edicion = req.getParameter("edicion");
            String institucion = req.getParameter("institucion");
            String nivelStr = req.getParameter("nivel");
            String tipoRegistro = req.getParameter("tipoRegistro");
            String aporteStr = req.getParameter("aporte");
            String gratuitosStr = req.getParameter("registrograt");
            String codigo = req.getParameter("codigo");

            if (aporteStr == null || aporteStr.isBlank() || gratuitosStr == null || gratuitosStr.isBlank()) {
                throw new IllegalArgumentException("Todos los campos marcados son obligatorios.");
            }

            float aporte = Float.parseFloat(aporteStr);
            int gratuitos = Integer.parseInt(gratuitosStr);
            NivelPatrocinio nivel = NivelPatrocinio.valueOf(nivelStr.toUpperCase());

            DtPatrocinio dt = new DtPatrocinio(
                    LocalDate.now(),
                    aporte,
                    gratuitos,
                    codigo,
                    nivel,
                    institucion,
                    edicion,
                    tipoRegistro
            );
            sistema.altaPatrocinio(dt);
            resp.sendRedirect(req.getContextPath() + "/consulta-patrocinio?edicion=" + edicion + "&institucion=" + institucion + "&exito=true");

        } catch (IllegalArgumentException e) {
            req.setAttribute("error", e.getMessage());
            doGet(req, resp);
        }
    }
}