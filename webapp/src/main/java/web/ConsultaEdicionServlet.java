package web;

import datatypes.*;
import interfaces.ISistema;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/edicion")
public class ConsultaEdicionServlet extends HttpServlet {
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        ISistema sistema = ContextoListener.sistema(req);
        DtUsuario usuario = AccesoEdiciones.usuario(req);
        var eventos = sistema.listarEventos();
        var seleccion = AccesoEdiciones.resolver(sistema, eventos, AccesoEdiciones.texto(req, "evento"),
                AccesoEdiciones.texto(req, "nombre"), usuario);
        if (seleccion == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        req.setAttribute("eventos", eventos);
        req.setAttribute("eventoElegido", seleccion.evento());
        req.setAttribute("ediciones", AccesoEdiciones.aceptadas(sistema, seleccion.evento()));
        DtEdicion edicion = seleccion.edicion();
        if (edicion != null) {
            String nombre = edicion.getIdNombre();
            req.setAttribute("detalle", edicion);
            req.setAttribute("fechaInicio", fecha(edicion.getFechaInicio()));
            req.setAttribute("fechaFin", fecha(edicion.getFechaFin()));
            req.setAttribute("fechaAlta", fecha(edicion.getFechaAlta()));
            req.setAttribute("organizador", sistema.consultarUsuario(edicion.getNombreOrganizador()));
            req.setAttribute("tieneImagen", sistema.obtenerImagen(TipoEntidadImagen.EDICION, nombre) != null);
            req.setAttribute("tipos", sistema.obtenerTiposRegistroEdicion(nombre));
            // El resumen conserva el contrato de la etapa anterior: solo instituciones, nunca códigos.
            List<String> patrocinadoras = new ArrayList<>();
            for (DtPatrocinio patrocinio : sistema.obtenerPatrociniosEdicion(nombre)) {
                patrocinadoras.add(patrocinio.getNombreInstituto());
            }
            req.setAttribute("patrocinadoras", patrocinadoras);
            prepararRegistros(req, sistema, usuario, edicion);
        }
        req.getRequestDispatcher("/WEB-INF/views/edicion.jsp").forward(req, resp);
    }

    private void prepararRegistros(HttpServletRequest req, ISistema sistema, DtUsuario usuario, DtEdicion edicion) {
        if (AccesoEdiciones.esPropietario(usuario, edicion)) {
            List<DetalleRegistroVista> registros = new ArrayList<>();
            for (DtRegistro registro : sistema.obtenerRegistrosEdicion(edicion.getIdNombre())) {
                registros.add(DetalleRegistroVista.de(registro, registro.getNicknameAsistente()));
            }
            req.setAttribute("esPropietario", true);
            req.setAttribute("registros", registros);
        } else if (usuario instanceof DtAsistente) {
            DtRegistro propio = null;
            for (DtRegistro registro : sistema.obtenerRegistrosAsistente(usuario.getNickname())) {
                if (edicion.getIdNombre().equals(registro.getNombreEdicion())
                        && usuario.getNickname().equals(registro.getNicknameAsistente())) {
                    propio = registro;
                    break;
                }
            }
            if (propio != null) req.setAttribute("registroPropio", DetalleRegistroVista.de(propio, usuario.getNombre()));
            req.setAttribute("puedeRegistrarse", propio == null && edicion.getEstado() == EstadoEdicion.ACEPTADA);
        }
    }

    private String fecha(LocalDate fecha) { return fecha == null ? "" : fecha.format(FECHA); }
}
