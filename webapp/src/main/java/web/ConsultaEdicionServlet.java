
package web;

import datatypes.*;
import interfaces.ISistema;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/edicion")
public class ConsultaEdicionServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        ISistema sistema = ContextoListener.sistema(req);

        HttpSession sesion = req.getSession(false);
        DtUsuario usuario = null;

        if (sesion != null) {
            Object atributo = sesion.getAttribute("usuario");

            if (atributo instanceof DtUsuario) {
                usuario = (DtUsuario) atributo;
            }
        }

        req.setAttribute(
                "eventos",
                sistema.listarEventos()
        );

        String evento = req.getParameter("evento");

        if (evento != null && !evento.isBlank()) {

            List<DtEdicion> visibles = new ArrayList<>();

            for (DtEdicion edicion :
                    sistema.obtenerEdicionesEvento(evento)) {

                boolean aceptada =
                        edicion.getEstado() == EstadoEdicion.ACEPTADA;

                boolean propia =
                        usuario instanceof DtOrganizador
                                && usuario.getNickname().equals(
                                edicion.getNombreOrganizador()
                        );

                if (aceptada || propia) {
                    visibles.add(edicion);
                }
            }

            req.setAttribute("ediciones", visibles);
            req.setAttribute("eventoElegido", evento);

            String nombre = req.getParameter("nombre");

            if (nombre != null && !nombre.isBlank()) {

                for (DtEdicion edicion : visibles) {

                    if (nombre.equals(edicion.getIdNombre())) {

                        req.setAttribute(
                                "detalle",
                                edicion
                        );

                        req.setAttribute(
                                "tipos",
                                sistema.obtenerTiposRegistroEdicion(nombre)
                        );

                        break;
                    }
                }
            }
        }

        req.getRequestDispatcher(
                "/WEB-INF/views/edicion.jsp"
        ).forward(req, resp);
    }
}
