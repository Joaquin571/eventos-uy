package web;

import datatypes.*;
import interfaces.ISistema;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;

@WebServlet("/tipo-registro")
public class ConsultaTipoRegistroServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        ISistema sistema = ContextoListener.sistema(req);
        DtUsuario usuario = AccesoEdiciones.usuario(req);
        String nombre = AccesoEdiciones.texto(req, "nombre");
        String edicion = AccesoEdiciones.texto(req, "edicion");
        if (!nombre.isEmpty() && edicion.isEmpty()) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        var eventos = sistema.listarEventos();
        var seleccion = AccesoEdiciones.resolver(sistema, eventos, AccesoEdiciones.texto(req, "evento"), edicion, usuario);
        if (seleccion == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        req.setAttribute("eventos", eventos);
        req.setAttribute("eventoElegido", seleccion.evento());
        req.setAttribute("ediciones", AccesoEdiciones.aceptadas(sistema, seleccion.evento()));
        if (seleccion.edicion() != null) {
            req.setAttribute("edicionElegida", seleccion.edicion());
            req.setAttribute("tipos", sistema.obtenerTiposRegistroEdicion(edicion));
            if (!nombre.isEmpty()) {
                DtTipoRegistro tipo = sistema.consultarTipoRegistro(edicion, nombre);
                if (tipo == null || !nombre.equals(tipo.getIdNombre())) {
                    resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                    return;
                }
                req.setAttribute("detalle", tipo);
                req.setAttribute("costo", DetalleRegistroVista.formatearCosto(tipo.getCosto()));
            }
        }
        req.getRequestDispatcher("/WEB-INF/views/tipo-registro.jsp").forward(req, resp);
    }
}
