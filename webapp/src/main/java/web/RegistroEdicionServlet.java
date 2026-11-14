package web;
 
import datatypes.DtAsistente;
import datatypes.DtEdicion;
import datatypes.DtEvento;
import datatypes.DtRegistro;
import datatypes.DtTipoRegistro;
import datatypes.DtUsuario;
import datatypes.EstadoEdicion;
import interfaces.ISistema;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
 
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
 
/**
 * Registro a Edición de Evento: /registro-edicion?edicion=X (solo asistente).
 *
 * El asistente elige un evento, una edición Aceptada y un tipo de registro, y confirma
 * como registro general o con código de patrocinio. Si la lógica rechaza el registro
 * (cupo, ya registrado, código), se muestra el motivo y el asistente puede cambiar la
 * selección o cancelar.
 */
@WebServlet("/registro-edicion")
public class RegistroEdicionServlet extends HttpServlet {
 
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
 
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
 
        if (asistenteDeLaSesion(req, resp) == null) {
            return;
        }
 
        mostrarFormulario(req, resp,
                texto(req, "evento"), texto(req, "edicion"), texto(req, "tipo"),
                "general", "", null);
    }
 
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
 
        DtUsuario asistente = asistenteDeLaSesion(req, resp);
        if (asistente == null) {
            return;
        }
 
        ISistema sistema = ContextoListener.sistema(req);
 
        String evento = texto(req, "evento");
        String edicion = texto(req, "edicion");
        String tipo = texto(req, "tipo");
        String modalidad = "codigo".equals(req.getParameter("modalidad")) ? "codigo" : "general";
        String codigo = texto(req, "codigo");
 
        try {
            if (edicion.isEmpty()) {
                throw new IllegalArgumentException("Debe elegir una edición.");
            }
            if (tipo.isEmpty()) {
                throw new IllegalArgumentException("Debe elegir un tipo de registro.");
            }
            if (modalidad.equals("codigo") && codigo.isEmpty()) {
                throw new IllegalArgumentException("Ingresá el código de patrocinio.");
            }
 
            DtTipoRegistro dtTipo = sistema.consultarTipoRegistro(edicion, tipo);
 
            if (dtTipo == null) {
                throw new IllegalArgumentException(
                        "El tipo de registro elegido no existe en esa edición.");
            }
 
            // Si el registro es general se ignora cualquier código que haya quedado escrito.
            String codigoUsado = modalidad.equals("codigo") ? codigo : null;
 
            // La lógica fija el costo real (el del tipo, o cero con código válido)
            DtRegistro dt = new DtRegistro(LocalDate.now(), dtTipo.getCosto(), tipo, edicion);
 
            boolean registrado = sistema.registroAEdicion(
                    asistente.getNickname(), edicion, tipo, dt, codigoUsado);
 
            if (!registrado) {
                throw new IllegalArgumentException("No se pudo completar el registro.");
            }
 
            resp.sendRedirect(req.getContextPath() + "/consulta-registro?registrado=1&edicion="
                    + URLEncoder.encode(edicion, StandardCharsets.UTF_8));
 
        } catch (IllegalArgumentException e) {
            mostrarFormulario(req, resp, evento, edicion, tipo, modalidad, codigo, e.getMessage());
 
        } catch (RuntimeException e) {
            // Falla inesperada (por ejemplo, la base de datos): se registra y no se muestra el detalle
            getServletContext().log("Error al registrar a " + asistente.getNickname()
                    + " en la edición " + edicion, e);
            mostrarFormulario(req, resp, evento, edicion, tipo, modalidad, codigo,
                    "No se pudo completar el registro. Intentá nuevamente.");
        }
    }
 
    /** Prepara los datos de la página según lo que el asistente fue eligiendo. */
    private void mostrarFormulario(HttpServletRequest req, HttpServletResponse resp,
                                   String evento, String edicion, String tipo,
                                   String modalidad, String codigo, String error)
            throws ServletException, IOException {
 
        ISistema sistema = ContextoListener.sistema(req);
        Collection<DtEvento> eventos = sistema.listarEventos();
 
        // Si se llega solo con la edición (desde su consulta), se busca a qué evento pertenece
        if (evento.isEmpty() && !edicion.isEmpty()) {
            evento = eventoDeLaEdicion(sistema, eventos, edicion);
        }
 
        String advertencia = null;
 
        if (!evento.isEmpty()) {
            Collection<DtEdicion> aceptadas =
                    sistema.obtenerEdicionesEvento(evento, EstadoEdicion.ACEPTADA);
 
            req.setAttribute("ediciones", aceptadas);
 
            if (!edicion.isEmpty()) {
                DtEdicion elegida = null;
 
                for (DtEdicion candidata : aceptadas) {
                    if (candidata.getIdNombre().equals(edicion)) {
                        elegida = candidata;
                        break;
                    }
                }
 
                if (elegida == null) {
                    advertencia = "La edición indicada no está disponible para registrarse.";
                    edicion = "";
                    tipo = "";
                } else {
                    Map<String, String> costos = new LinkedHashMap<>();
                    Collection<DtTipoRegistro> tipos = sistema.obtenerTiposRegistroEdicion(edicion);
 
                    for (DtTipoRegistro t : tipos) {
                        costos.put(t.getIdNombre(), DetalleRegistroVista.formatearCosto(t.getCosto()));
                    }
 
                    req.setAttribute("detalle", elegida);
                    req.setAttribute("tipos", tipos);
                    req.setAttribute("costos", costos);
                    req.setAttribute("fechaInicio", formatearFecha(elegida.getFechaInicio()));
                    req.setAttribute("fechaFin", formatearFecha(elegida.getFechaFin()));
                }
            }
        } else {
            edicion = "";
            tipo = "";
        }
 
        req.setAttribute("eventos", eventos);
        req.setAttribute("eventoElegido", evento);
        req.setAttribute("edicionElegida", edicion);
        req.setAttribute("tipoElegido", tipo);
        req.setAttribute("modalidad", modalidad);
        req.setAttribute("codigo", codigo);
        req.setAttribute("error", error);
        req.setAttribute("advertencia", advertencia);
 
        req.getRequestDispatcher("/WEB-INF/views/registro-edicion.jsp").forward(req, resp);
    }
 
    private String eventoDeLaEdicion(ISistema sistema, Collection<DtEvento> eventos, String edicion) {
        for (DtEvento evento : eventos) {
            for (DtEdicion candidata :
                    sistema.obtenerEdicionesEvento(evento.getNombre(), EstadoEdicion.ACEPTADA)) {
                if (candidata.getIdNombre().equals(edicion)) {
                    return evento.getNombre();
                }
            }
        }
        return "";
    }
 
    /** Devuelve al asistente de la sesión; si no hay uno, responde 403 y devuelve null. */
    private DtUsuario asistenteDeLaSesion(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
 
        HttpSession sesion = req.getSession(false);
        Object usuario = sesion == null ? null : sesion.getAttribute("usuario");
 
        if (!(usuario instanceof DtAsistente)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN,
                    "Esta operación requiere un asistente.");
            return null;
        }
 
        return (DtUsuario) usuario;
    }
 
    private String texto(HttpServletRequest req, String campo) {
        String valor = req.getParameter(campo);
        return valor == null ? "" : valor.trim();
    }
 
    private String formatearFecha(LocalDate fecha) {
        return fecha == null ? "" : fecha.format(FECHA);
    }
}