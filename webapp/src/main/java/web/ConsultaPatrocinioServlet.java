package web;

import datatypes.DtEdicion;
import datatypes.DtPatrocinio;
import datatypes.DtUsuario;
import interfaces.ISistema;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Collection;

@WebServlet("/consulta-patrocinio")
public class ConsultaPatrocinioServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        ISistema sistema = ContextoListener.sistema(req);

        String edicion = req.getParameter("edicion");
        String institucion = req.getParameter("institucion");

        req.setAttribute("eventos", sistema.listarEventos());
        req.setAttribute("instituciones", sistema.listarInstituciones());

        if (edicion != null && !edicion.isBlank() && institucion != null && !institucion.isBlank()) {
            DtPatrocinio patrocinio = sistema.consultarPatrocinio(edicion, institucion);

            if (patrocinio != null) {
                DtUsuario usuarioLogueado = (DtUsuario) req.getSession().getAttribute("usuarioLogueado");
                boolean esOrganizadorDuenio = false;

                if (usuarioLogueado != null) {
                    Collection<DtEdicion> edicionesOrg = sistema.obtenerEdicionesOrganizador(usuarioLogueado.getNickname());
                    if (edicionesOrg != null) {
                        esOrganizadorDuenio = edicionesOrg.stream()
                                .anyMatch(e -> e.getIdNombre().equalsIgnoreCase(edicion));
                    }
                }

                req.setAttribute("patrocinio", patrocinio);
                req.setAttribute("esOrganizadorDuenio", esOrganizadorDuenio);
            } else {
                req.setAttribute("error", "No se encontró ningún patrocinio para la edición e institución seleccionadas.");
            }
        }

        req.getRequestDispatcher("/WEB-INF/views/consulta-patrocinio.jsp").forward(req, resp);
    }
}