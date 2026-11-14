package web;

import datatypes.DtAsistente;
import datatypes.DtEdicion;
import datatypes.DtOrganizador;
import datatypes.DtRegistro;
import datatypes.DtUsuario;
import interfaces.ISistema;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Consulta de Registro: /consulta-registro?edicion=X (asistente u organizador).
 *
 * Asistente: lista las ediciones a las que se registró y, al elegir una, el detalle de su registro.
 * Organizador: lista sus ediciones y, al elegir una, los asistentes registrados
 * (/consulta-registro?edicion=X&asistente=nick) y el detalle del registro de cada uno.
 */
@WebServlet("/consulta-registro")
public class ConsultaRegistroServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession sesion = req.getSession(false);
        Object usuario = sesion == null ? null : sesion.getAttribute("usuario");

        if (!(usuario instanceof DtAsistente) && !(usuario instanceof DtOrganizador)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN,
                    "Esta operación requiere un asistente o un organizador.");
            return;
        }

        ISistema sistema = ContextoListener.sistema(req);
        DtUsuario actual = (DtUsuario) usuario;

        String edicion = texto(req, "edicion");
        String asistente = texto(req, "asistente");

        req.setAttribute("edicionElegida", edicion);
        req.setAttribute("asistenteElegido", asistente);
        req.setAttribute("registrado", "1".equals(req.getParameter("registrado")));

        if (actual instanceof DtOrganizador) {
            req.setAttribute("esOrganizador", true);

            if (!consultaDelOrganizador(req, resp, sistema, actual, edicion, asistente)) {
                return;
            }
        } else {
            req.setAttribute("esOrganizador", false);
            consultaDelAsistente(req, sistema, actual, edicion);
        }

        req.getRequestDispatcher("/WEB-INF/views/consulta-registro.jsp").forward(req, resp);
    }

    private void consultaDelAsistente(HttpServletRequest req, ISistema sistema,
                                      DtUsuario asistente, String edicion) {

        List<DetalleRegistroVista> propios = new ArrayList<>();

        for (DtRegistro registro : sistema.obtenerRegistrosAsistente(asistente.getNickname())) {
            propios.add(DetalleRegistroVista.de(registro, nombreCompleto(asistente)));
        }

        req.setAttribute("registros", propios);

        if (edicion.isEmpty()) {
            return;
        }

        for (DetalleRegistroVista propio : propios) {
            if (edicion.equals(propio.getNombreEdicion())) {
                req.setAttribute("detalle", propio);
                return;
            }
        }

        req.setAttribute("advertencia", "No tenés un registro en la edición «" + edicion + "».");
    }

    /** Devuelve false si ya respondió con un error (la edición no es del organizador). */
    private boolean consultaDelOrganizador(HttpServletRequest req, HttpServletResponse resp,
                                           ISistema sistema, DtUsuario organizador,
                                           String edicion, String asistente) throws IOException {

        Collection<DtEdicion> propias = sistema.obtenerEdicionesOrganizador(organizador.getNickname());

        req.setAttribute("ediciones", propias);

        if (edicion.isEmpty()) {
            return true;
        }

        boolean esPropia = false;

        for (DtEdicion propia : propias) {
            if (propia.getIdNombre().equals(edicion)) {
                esPropia = true;
                break;
            }
        }

        // Los registros de una edición solo los ve el organizador que la tiene a cargo
        if (!esPropia) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN,
                    "La edición no pertenece al organizador.");
            return false;
        }

        List<DetalleRegistroVista> registros = new ArrayList<>();

        for (DtRegistro registro : sistema.obtenerRegistrosEdicion(edicion)) {
            registros.add(DetalleRegistroVista.de(
                    registro, nombreCompleto(sistema.consultarUsuario(registro.getNicknameAsistente()))));
        }

        req.setAttribute("registros", registros);

        if (asistente.isEmpty()) {
            return true;
        }

        for (DetalleRegistroVista registro : registros) {
            if (asistente.equals(registro.getNickname())) {
                req.setAttribute("detalle", registro);
                return true;
            }
        }

        req.setAttribute("advertencia",
                "El asistente «" + asistente + "» no está registrado en esta edición.");
        return true;
    }

    private String nombreCompleto(DtUsuario usuario) {
        if (usuario == null) {
            return "";
        }

        if (usuario instanceof DtAsistente asistente && asistente.getApellido() != null) {
            return asistente.getNombre() + " " + asistente.getApellido();
        }

        return usuario.getNombre();
    }

    private String texto(HttpServletRequest req, String campo) {
        String valor = req.getParameter(campo);
        return valor == null ? "" : valor.trim();
    }
}