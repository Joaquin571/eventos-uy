package web;

import datatypes.*;
import interfaces.ISistema;
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
        ISistema sistema = ContextoListener.sistema(req);
        DtUsuario usuario = AccesoPatrocinios.usuarioDeSesion(req);
        String edicion = texto(req, "edicion");
        String institucion = texto(req, "institucion");
        String evento = texto(req, "evento");
        Collection<DtEvento> eventos = sistema.listarEventos();
        DtEdicion detalle = null;
        if (!edicion.isEmpty()) {
            detalle = AccesoPatrocinios.buscarEdicion(sistema, edicion);
            if (!AccesoPatrocinios.esVisible(usuario, detalle)) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            // El enlace directo se resuelve por edición; no confía en el evento de la URL.
            for (DtEvento candidato : eventos) {
                if (sistema.obtenerEdicionesEvento(candidato.getNombre()).stream()
                        .anyMatch(e -> edicion.equals(e.getIdNombre()))) {
                    evento = candidato.getNombre();
                    break;
                }
            }
            // El selector solo necesita nombres, nunca códigos ni DTO privados.
            List<String> patrocinadoras = new ArrayList<>();
            for (DtPatrocinio p : sistema.obtenerPatrociniosEdicion(edicion)) {
                patrocinadoras.add(p.getNombreInstituto());
            }
            req.setAttribute("patrocinadoras", patrocinadoras);
            if (!institucion.isEmpty()) {
                DtPatrocinio patrocinio = sistema.consultarPatrocinio(edicion, institucion);
                if (patrocinio == null) {
                    resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                    return;
                }
                boolean esDuenio = AccesoPatrocinios.esPropietario(usuario, detalle);
                DtPatrocinio visible = new DtPatrocinio(patrocinio.getFecha(), patrocinio.getMontoAporte(),
                        patrocinio.getCantRegistrosGrat(), esDuenio ? patrocinio.getCodigoPatrocinio() : null,
                        patrocinio.getNivel(), patrocinio.getNombreInstituto(),
                        patrocinio.getNombreEdicion(), patrocinio.getNombreTipoRegistro());
                req.setAttribute("patrocinio", visible);
                req.setAttribute("esOrganizadorDuenio", esDuenio);
                req.setAttribute("fechaAlta", patrocinio.getFecha() == null ? "" : patrocinio.getFecha().format(FECHA));
                req.setAttribute("aporte", String.format(Locale.forLanguageTag("es-UY"), "%.2f", patrocinio.getMontoAporte()));
            }
        }
        List<DtEdicion> aceptadas = new ArrayList<>();
        if (!evento.isEmpty()) {
            for (DtEdicion candidata : sistema.obtenerEdicionesEvento(evento)) {
                if (candidata.getEstado() == EstadoEdicion.ACEPTADA) aceptadas.add(candidata);
            }
        }
        // Conserva únicamente la edición privada ya autorizada del dueño, sin listar otras ocultas.
        if (detalle != null && detalle.getEstado() != EstadoEdicion.ACEPTADA) aceptadas.add(detalle);
        req.setAttribute("eventos", eventos);
        req.setAttribute("ediciones", aceptadas);
        req.setAttribute("eventoElegido", evento);
        req.setAttribute("edicionElegida", edicion);
        req.setAttribute("institucionElegida", institucion);
        req.getRequestDispatcher("/WEB-INF/views/consulta-patrocinio.jsp").forward(req, resp);
    }

    private String texto(HttpServletRequest req, String campo) {
        String valor = req.getParameter(campo);
        return valor == null ? "" : valor.trim();
    }
}
