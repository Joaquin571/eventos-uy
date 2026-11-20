package web;

import jakarta.servlet.DispatcherType;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.xml.ws.WebServiceException;
import org.junit.jupiter.api.Test;
import web.remoto.ErrorCentral;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/** Filtro de visitas con un informante de prueba; no ejercita SOAP (pendiente del contrato de González). */
class FiltroVisitasEventoTest {
    private static final List<String> SOLO_PAGINA = List.of("pagina");

    @Test void detalleValidoCuentaUnaVisitaDespuesDeAtenderLaPagina() throws Exception {
        var e = detalle(); e.ejecutar();
        assertEquals(List.of("pagina", "flush", "visita:Maraton"), e.orden);
    }

    @Test void rutaMovilDeEventoCuentaSoloConNombreValido() throws Exception {
        var conNombre = new Escenario("/movil/eventos"); conNombre.parametros.put("evento", " Maraton "); conNombre.ejecutar();
        assertEquals(List.of("pagina", "flush", "visita:Maraton"), conNombre.orden);
        var listado = new Escenario("/movil/eventos"); listado.ejecutar();
        assertEquals(SOLO_PAGINA, listado.orden);
    }

    @Test void recursosEstaticosYRutasAjenasNoCuentan() throws Exception {
        for (String ruta : List.of("/css/movil.css", "/js/validacion.js", "/vendor/bootstrap/css/bootstrap.min.css",
                "/imagen", "/favicon.ico", "/edicion", "/movil/edicion", "/usuario", "/usuarios", "/alta-evento", "/login")) {
            var e = new Escenario(ruta); e.parametros.putAll(Map.of("nombre", "Maraton", "evento", "Maraton")); e.ejecutar();
            assertEquals(SOLO_PAGINA, e.orden, ruta);
        }
    }

    @Test void solicitudesAuxiliaresNoCuentan() throws Exception {
        List<Map<String, String>> auxiliares = List.of(
                Map.of("X-Requested-With", "XMLHttpRequest"), Map.of("Sec-Fetch-Mode", "cors"),
                Map.of("Sec-Fetch-Dest", "empty"), Map.of("Sec-Fetch-Mode", "navigate", "Sec-Fetch-Dest", "iframe"),
                Map.of("Sec-Purpose", "prefetch"), Map.of("Purpose", "prefetch"));
        for (var cabeceras : auxiliares) {
            var e = detalle(); e.cabeceras.putAll(cabeceras); e.ejecutar();
            assertEquals(SOLO_PAGINA, e.orden, cabeceras.toString());
        }
        var navegacion = detalle(); navegacion.cabeceras.putAll(Map.of("Sec-Fetch-Mode", "navigate", "Sec-Fetch-Dest", "document"));
        navegacion.ejecutar();
        assertEquals(1, navegacion.visitas());
    }

    @Test void metodosQueNoSonGetNoCuentan() throws Exception {
        for (String metodo : List.of("POST", "HEAD", "PUT", "DELETE")) {
            var e = detalle(); e.metodo = metodo; e.ejecutar();
            assertEquals(SOLO_PAGINA, e.orden, metodo);
        }
    }

    @Test void forwardsEInclusionesInternasNoCuentan() throws Exception {
        for (DispatcherType tipo : List.of(DispatcherType.FORWARD, DispatcherType.INCLUDE, DispatcherType.ERROR, DispatcherType.ASYNC)) {
            var e = detalle(); e.despacho = tipo; e.ejecutar();
            assertEquals(SOLO_PAGINA, e.orden, tipo.toString());
        }
    }

    @Test void eventoInexistenteNoVisibleOConErrorNoCuenta() throws Exception {
        for (int estado : List.of(404, 403, 302, 500)) {
            var e = detalle(); e.estadoPagina = estado; e.ejecutar();
            assertEquals(SOLO_PAGINA, e.orden, "estado " + estado);
        }
    }

    @Test void unaMismaNavegacionQueRecorreElFiltroDosVecesCuentaUnaSolaVisita() throws Exception {
        var e = detalle(); var filtro = e.filtro();
        filtro.doFilter(e.req, e.resp, (rq, rs) -> filtro.doFilter(rq, rs, (a, b) -> e.orden.add("pagina")));
        assertEquals(1, e.visitas());
    }

    @Test void fallosDelServicioDeVisitasNoRompenLaPagina() throws Exception {
        List<RuntimeException> fallos = List.of(new WebServiceException("Central no disponible"),
                new ErrorCentral("NO_ENCONTRADO", "no existe"), new ErrorCentral("SESION_INVALIDA", "vencida"),
                new IllegalArgumentException("entrada inválida"), new IllegalStateException("tiempo agotado"));
        for (RuntimeException fallo : fallos) {
            var e = detalle(); e.fallo = fallo;
            assertDoesNotThrow(e::ejecutar, fallo.getClass().getSimpleName());
            assertEquals(1, e.visitas(), "se intentó informar una vez"); assertEquals(200, e.estado);
        }
    }

    @Test void nombresInvalidosNoLlegaranACentral() throws Exception {
        for (String valor : Arrays.asList(null, "", "   ", "x".repeat(256))) {
            var e = new Escenario("/evento"); if (valor != null) e.parametros.put("nombre", valor); e.ejecutar();
            assertEquals(SOLO_PAGINA, e.orden, String.valueOf(valor));
        }
        var limite = new Escenario("/evento"); limite.parametros.put("nombre", "x".repeat(255)); limite.ejecutar();
        assertEquals(1, limite.visitas());
    }

    @Test void siLaPaginaFallaLaExcepcionSePropagaYNoSeCuenta() {
        var e = detalle(); e.falloPagina = new IllegalStateException("error de página");
        assertThrows(IllegalStateException.class, e::ejecutar);
        assertEquals(0, e.visitas());
    }

    private static Escenario detalle() { var e = new Escenario("/evento"); e.parametros.put("nombre", "Maraton"); return e; }

    private static <T> T doble(Class<T> tipo, InvocationHandler h) {
        return tipo.cast(Proxy.newProxyInstance(tipo.getClassLoader(), new Class<?>[]{tipo}, h));
    }

    private static final class Escenario {
        String ruta;
        String metodo = "GET";
        DispatcherType despacho = DispatcherType.REQUEST;
        int estadoPagina = 200, estado = 200;
        RuntimeException fallo, falloPagina;
        final Map<String, String> parametros = new HashMap<>(), cabeceras = new HashMap<>();
        final Map<String, Object> atributos = new HashMap<>();
        final List<String> orden = new ArrayList<>();

        Escenario(String ruta) { this.ruta = ruta; }

        final HttpServletRequest req = doble(HttpServletRequest.class, (p, m, a) -> switch (m.getName()) {
            case "getDispatcherType" -> despacho;
            case "getMethod" -> metodo;
            case "getServletPath" -> ruta;
            case "getParameter" -> parametros.get(a[0]);
            case "getHeader" -> cabeceras.get(a[0]);
            case "getAttribute" -> atributos.get(a[0]);
            case "setAttribute" -> { atributos.put((String) a[0], a[1]); yield null; }
            default -> throw new AssertionError("Uso inesperado de la solicitud: " + m.getName());
        });

        final HttpServletResponse resp = doble(HttpServletResponse.class, (p, m, a) -> switch (m.getName()) {
            case "getStatus" -> estado;
            case "flushBuffer" -> { orden.add("flush"); yield null; }
            default -> throw new AssertionError("Uso inesperado de la respuesta: " + m.getName());
        });

        FiltroVisitasEvento filtro() {
            return new FiltroVisitasEvento((r, evento) -> {
                orden.add("visita:" + evento);
                if (fallo != null) throw fallo;
            });
        }

        void ejecutar() throws Exception {
            FilterChain pagina = (rq, rs) -> {
                orden.add("pagina"); estado = estadoPagina;
                if (falloPagina != null) throw falloPagina;
            };
            filtro().doFilter(req, resp, pagina);
        }

        long visitas() { return orden.stream().filter(s -> s.startsWith("visita:")).count(); }
    }
}
