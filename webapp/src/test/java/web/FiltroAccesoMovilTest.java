package web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.Test;
import web.remoto.ErrorCentral;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Una sesión vencida lleva al login móvil solo en /movil; el resto del sitio sigue yendo al login de escritorio. */
class FiltroAccesoMovilTest {
    @Test void sesionVencidaEnRutasMovilesVaAlLoginMovilConAviso() throws Exception {
        for (String ruta : List.of("/movil", "/movil/login", "/movil/eventos", "/movil/edicion", "/movil/registros", "/movil/logout")) {
            var e = new Escenario(ruta);
            FiltroAcceso.responderError(e.req, e.resp, new ErrorCentral("SESION_INVALIDA", "vencida"));
            assertEquals("/test/movil/login?motivo=vencida", e.redireccion, ruta);
            assertEquals(1, e.invalidaciones, ruta);
        }
    }

    @Test void sesionVencidaEnElRestoDelSitioSigueYendoAlLoginDeEscritorio() throws Exception {
        for (String ruta : List.of("/", "/usuario", "/evento", "/edicion", "/consulta-registro", "/movilidad", "/moviles/x")) {
            var e = new Escenario(ruta);
            FiltroAcceso.responderError(e.req, e.resp, new ErrorCentral("SESION_INVALIDA", "vencida"));
            assertEquals("/test/login", e.redireccion, ruta);
            assertEquals(1, e.invalidaciones, ruta);
        }
    }

    private static <T> T doble(Class<T> tipo, InvocationHandler h) {
        return tipo.cast(Proxy.newProxyInstance(tipo.getClassLoader(), new Class<?>[]{tipo}, h));
    }

    private static final class Escenario {
        String ruta, redireccion;
        int invalidaciones;
        boolean haySesion = true;
        Escenario(String ruta) { this.ruta = ruta; }

        final HttpSession session = doble(HttpSession.class, (p, m, a) -> {
            if (!m.getName().equals("invalidate")) throw new AssertionError(m.getName());
            invalidaciones++; haySesion = false; return null;
        });
        final HttpServletRequest req = doble(HttpServletRequest.class, (p, m, a) -> switch (m.getName()) {
            case "getSession" -> haySesion ? session : null;
            case "getServletPath" -> ruta;
            case "getContextPath" -> "/test";
            default -> throw new AssertionError("Uso inesperado de la solicitud: " + m.getName());
        });
        final HttpServletResponse resp = doble(HttpServletResponse.class, (p, m, a) -> {
            if (!m.getName().equals("sendRedirect")) throw new AssertionError(m.getName());
            redireccion = (String) a[0]; return null;
        });
    }
}
