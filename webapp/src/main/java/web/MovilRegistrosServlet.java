package web;

import contratos.dto.DtRegistro;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Consulta de Registro y Registro de Asistencia móvil: las ediciones a las que el asistente se registró y el
 * detalle del elegido, con su estado de asistencia. Siempre consulta los registros propios (el asistente se
 * resuelve en Central con la credencial P03). El parámetro modo=asistencia solo cambia el título y el resaltado
 * del menú; la acción de confirmar vive en MovilAsistenciaServlet.
 */
@WebServlet("/movil/registros")
public class MovilRegistrosServlet extends HttpServlet {
    static final String PANTALLA = "/movil/registros";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        var asistente = AccesoMovil.requerirAsistente(req, resp);
        if (asistente == null) return;
        MensajesFlash.mostrar(req, PANTALLA);
        String edicion = AccesoEdiciones.texto(req, "edicion");
        var central = ContextoListener.central(req);
        var consulta = central.consultarRegistros(edicion, "");
        req.setAttribute("modoAsistencia", "asistencia".equals(req.getParameter("modo")));
        req.setAttribute("edicionElegida", edicion);
        req.setAttribute("registros", consulta.getRegistros().stream().map(r -> vista(r, asistente.getNombre())).toList());
        var detalle = consulta.getDetalle();
        if (detalle != null) {
            req.setAttribute("detalle", vista(detalle, asistente.getNombre()));
            if (!detalle.isAsistenciaConfirmada()) req.setAttribute("puedeConfirmarAsistencia", true);
        }
        req.getRequestDispatcher("/WEB-INF/views/movil/registros.jsp").forward(req, resp);
    }

    private DetalleRegistroVista vista(DtRegistro registro, String nombre) {
        return DetalleRegistroVista.de(registro, nombre);
    }
}
