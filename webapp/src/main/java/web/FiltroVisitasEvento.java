package web;

import jakarta.servlet.DispatcherType;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import web.remoto.ErrorCentral;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Cuenta las consultas de evento (escritorio y celular) sin modificar los servlets existentes.
 *
 * Una visita se informa solo si la solicitud es una navegación GET de tipo REQUEST a una ruta de detalle de evento,
 * con nombre válido, y la página se atendió con 200. El conteo se hace después de atender la página, de modo que:
 * un evento inexistente o no visible (404), una redirección o un error no suman, y un fallo del servicio de
 * visitas nunca afecta lo que ve el usuario. La existencia y visibilidad del evento las vuelve a validar Central.
 *
 * Informa la visita a Central mediante el contrato SOAP registrarVisitaEvento.
 */
@WebFilter(urlPatterns = {"/evento", "/movil/eventos"}, dispatcherTypes = DispatcherType.REQUEST)
public class FiltroVisitasEvento implements Filter {
    static final String MARCA = FiltroVisitasEvento.class.getName() + ".contada";
    static final int LARGO_MAXIMO = 255;
    /** Ruta de detalle de evento -> parámetro que contiene el nombre del evento. */
    private static final Map<String, String> RUTAS = Map.of("/evento", "nombre", "/movil/eventos", "evento");
    private static final Logger LOG = Logger.getLogger(FiltroVisitasEvento.class.getName());

    private final InformanteVisitas informante;

    FiltroVisitasEvento(InformanteVisitas informante) {
        this.informante = Objects.requireNonNull(informante);
    }

    public FiltroVisitasEvento() {
        this((req, evento) -> ContextoListener.central(req).registrarVisitaEvento(evento));
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (!(request instanceof HttpServletRequest req) || !(response instanceof HttpServletResponse resp)) {
            chain.doFilter(request, response);
            return;
        }
        String evento = candidata(req);
        chain.doFilter(req, resp);
        if (evento == null || resp.getStatus() != HttpServletResponse.SC_OK) return;
        if (req.getAttribute(MARCA) != null) return;
        req.setAttribute(MARCA, Boolean.TRUE);
        entregarPagina(resp);
        try {
            informante.registrar(req, evento);
        } catch (Exception e) {
            String codigo = e instanceof ErrorCentral central ? " (" + central.getCodigo() + ")" : "";
            LOG.log(Level.WARNING, "No se pudo registrar la visita del evento: {0}{1}",
                    new Object[]{e.getClass().getSimpleName(), codigo});
        }
    }

    /** Nombre del evento si la solicitud puede contar como visita; null si no corresponde. */
    private String candidata(HttpServletRequest req) {
        if (req.getDispatcherType() != DispatcherType.REQUEST) return null;
        if (!"GET".equals(req.getMethod())) return null;
        String parametro = RUTAS.get(req.getServletPath());
        if (parametro == null || esAuxiliar(req)) return null;
        String valor = req.getParameter(parametro);
        if (valor == null) return null;
        valor = valor.trim();
        return valor.isEmpty() || valor.length() > LARGO_MAXIMO ? null : valor;
    }

    /** AJAX, fetch, iframes y precargas no son navegaciones del usuario. */
    private boolean esAuxiliar(HttpServletRequest req) {
        if ("XMLHttpRequest".equalsIgnoreCase(req.getHeader("X-Requested-With"))) return true;
        String modo = req.getHeader("Sec-Fetch-Mode");
        if (modo != null && !"navigate".equalsIgnoreCase(modo)) return true;
        String destino = req.getHeader("Sec-Fetch-Dest");
        if (destino != null && !"document".equalsIgnoreCase(destino)) return true;
        String proposito = req.getHeader("Sec-Purpose") != null ? req.getHeader("Sec-Purpose") : req.getHeader("Purpose");
        return proposito != null && proposito.toLowerCase().contains("prefetch");
    }

    /** Envía la página al cliente antes de esperar a Central, para que un servicio lento no demore la vista. */
    private void entregarPagina(HttpServletResponse resp) {
        try {
            resp.flushBuffer();
        } catch (IOException | IllegalStateException clienteCortoOYaCerrada) {
            // La visita igual se informa: la página ya se generó.
        }
    }
}
