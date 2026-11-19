package web;

import contratos.dto.DtRegistro;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Consulta de Registro móvil: las ediciones a las que el asistente se registró y el detalle del elegido.
 * Siempre consulta los registros propios (el asistente se resuelve en Central con la credencial P03).
 *
 * PENDIENTE (Adrián): mostrar "asistencia registrada" y el registro de asistencia, cuando el contrato exista.
 */
@WebServlet("/movil/registros")
public class MovilRegistrosServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        var asistente = AccesoMovil.requerirAsistente(req, resp);
        if (asistente == null) return;
        String edicion = AccesoEdiciones.texto(req, "edicion");
        var consulta = ContextoListener.central(req).consultarRegistros(edicion, "");
        req.setAttribute("edicionElegida", edicion);
        req.setAttribute("registros", consulta.getRegistros().stream().map(r -> vista(r, asistente.getNombre())).toList());
        if (consulta.getDetalle() != null) req.setAttribute("detalle", vista(consulta.getDetalle(), asistente.getNombre()));
        req.getRequestDispatcher("/WEB-INF/views/movil/registros.jsp").forward(req, resp);
    }

    private DetalleRegistroVista vista(DtRegistro registro, String nombre) {
        return DetalleRegistroVista.de(registro, nombre);
    }
}
