package web;

import contratos.ServicioPortal;
import contratos.dto.*;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/** Video y asistencia móviles con un doble local de Central (no sustituye la prueba contra SOAP real). */
class MovilVideoYAsistenciaTest {
    private static final String EMBED = "https://www.youtube-nocookie.com/embed/abc123";

    @Test void edicionConVideoNormalizadoPublicaLaDireccionEmbebida() throws Exception {
        var e = conSesion(); e.prepararEdicion(EMBED);
        new MovilEdicionServlet().doGet(e.req, e.resp);
        assertEquals(EMBED, e.atributos.get("urlVideoEmbed"));
    }

    @Test void videoQueNoEsDelReproductorEsperadoNoSePublica() throws Exception {
        for (String valor : Arrays.asList(null, "", "  ", "https://evil.example/embed/x", "javascript:alert(1)",
                "http://www.youtube-nocookie.com/embed/x", "https://www.youtube.com/watch?v=x")) {
            var e = conSesion(); e.prepararEdicion(valor);
            new MovilEdicionServlet().doGet(e.req, e.resp);
            assertFalse(e.atributos.containsKey("urlVideoEmbed"), String.valueOf(valor));
        }
    }

    @Test void confirmarAsistenciaInvocaCentralYDejaMensajeDeExito() throws Exception {
        var e = conSesion(); e.parametros.put("edicion", "Maraton2026");
        new MovilAsistenciaServlet().doPost(e.req, e.resp);
        assertEquals(List.of("confirmarAsistencia[Maraton2026]"), e.llamadas);
        assertEquals("/test/movil/registros?edicion=Maraton2026", e.redireccion);
        MensajesFlash.mostrar(e.req, "/movil/registros");
        assertEquals("Asistencia registrada correctamente.", e.atributos.get("exito"));
        assertNull(e.atributos.get("advertencia"));
    }

    @Test void elModoAsistenciaSeConservaEnLaRedireccion() throws Exception {
        var e = conSesion(); e.parametros.putAll(Map.of("edicion", "Maraton 2026", "modo", "asistencia"));
        new MovilAsistenciaServlet().doPost(e.req, e.resp);
        assertEquals("/test/movil/registros?edicion=Maraton+2026&modo=asistencia", e.redireccion);
    }

    @Test void unErrorDeNegocioDeCentralSeMuestraComoAdvertenciaSinRomperLaPagina() throws Exception {
        var e = conSesion(); e.parametros.put("edicion", "Maraton2026");
        e.falloConfirmar = new IllegalArgumentException("La asistencia ya estaba registrada.");
        assertDoesNotThrow(() -> new MovilAsistenciaServlet().doPost(e.req, e.resp));
        assertEquals("/test/movil/registros?edicion=Maraton2026", e.redireccion);
        MensajesFlash.mostrar(e.req, "/movil/registros");
        assertEquals("La asistencia ya estaba registrada.", e.atributos.get("advertencia"));
        assertNull(e.atributos.get("exito"));
    }

    @Test void sinEdicionNoSeLlamaACentral() throws Exception {
        var e = conSesion();
        new MovilAsistenciaServlet().doPost(e.req, e.resp);
        assertTrue(e.llamadas.isEmpty()); assertEquals("/test/movil/registros", e.redireccion);
        MensajesFlash.mostrar(e.req, "/movil/registros");
        assertEquals("Elegí un registro para confirmar la asistencia.", e.atributos.get("advertencia"));
    }

    @Test void visitanteYOrganizadorNoConfirmanAsistencia() throws Exception {
        var visitante = new Escenario(); visitante.parametros.put("edicion", "Maraton2026");
        new MovilAsistenciaServlet().doPost(visitante.req, visitante.resp);
        assertEquals("/test/movil/login", visitante.redireccion); assertTrue(visitante.llamadas.isEmpty());
        var organizador = new Escenario(); organizador.usuario = MovilAccesoTest.organizador("org");
        organizador.parametros.put("edicion", "Maraton2026");
        new MovilAsistenciaServlet().doPost(organizador.req, organizador.resp);
        assertEquals("/test/movil/login?motivo=rol", organizador.redireccion); assertTrue(organizador.llamadas.isEmpty());
    }

    @Test void confirmarPorGetNoEstaPermitido() throws Exception {
        var e = conSesion(); e.parametros.put("edicion", "Maraton2026");
        new MovilAsistenciaServlet().doGet(e.req, e.resp);
        assertEquals(405, e.estado); assertTrue(e.llamadas.isEmpty());
    }

    @Test void elBotonSeOfreceParaRegistroPropioSinRestriccionesDeFechaOEstado() throws Exception {
        record Caso(EstadoEdicion estado, LocalDate inicio, boolean esperado) { }
        LocalDate hoy = LocalDate.now();
        for (var c : List.of(new Caso(EstadoEdicion.ACEPTADA, hoy.minusDays(1), true), new Caso(EstadoEdicion.ACEPTADA, hoy, true),
                new Caso(EstadoEdicion.ACEPTADA, hoy.plusDays(1), true), new Caso(EstadoEdicion.ARCHIVADA, hoy.minusDays(5), true),
                new Caso(EstadoEdicion.INGRESADA, hoy.minusDays(5), true), new Caso(EstadoEdicion.ACEPTADA, null, true))) {
            var e = conSesion(); e.parametros.put("edicion", "Maraton2026"); e.prepararRegistro(false);
            var ed = new DtEdicion("Maraton2026", "ED", c.inicio(), c.inicio() == null ? null : c.inicio().plusDays(2),
                    LocalDate.of(2026, 2, 1), "Ciudad", "País", "duenio");
            ed.setEstado(c.estado());
            e.detalleEdicion.setEdicion(ed);
            new MovilRegistrosServlet().doGet(e.req, e.resp);
            assertEquals(c.esperado(), e.atributos.get("puedeConfirmarAsistencia"), c.toString());
            assertEquals(List.of("consultarRegistros[Maraton2026, ]"), e.llamadas);
        }
    }

    @Test void conAsistenciaYaRegistradaNoSeOfreceNiSeConsultaLaEdicion() throws Exception {
        var e = conSesion(); e.parametros.put("edicion", "Maraton2026"); e.prepararRegistro(true);
        new MovilRegistrosServlet().doGet(e.req, e.resp);
        assertFalse(e.atributos.containsKey("puedeConfirmarAsistencia"));
        assertEquals(List.of("consultarRegistros[Maraton2026, ]"), e.llamadas);
        assertTrue(((DetalleRegistroVista) e.atributos.get("detalle")).isAsistenciaConfirmada());
    }

    @Test void registroPropioArchivadoSinAsistenciaSePuedeConsultarEnAmbosSitios() throws Exception {
        for (boolean movil : List.of(true, false)) {
            var e = conSesion(); e.parametros.put("edicion", "Maraton2026"); e.prepararRegistro(false);
            e.falloDetalle = new web.remoto.ErrorCentral("NO_ENCONTRADO", "Edición no visible.");
            if (movil) new MovilRegistrosServlet().doGet(e.req, e.resp);
            else new ConsultaRegistroServlet().doGet(e.req, e.resp);
            assertEquals(200, e.estado);
            assertNotNull(e.atributos.get("detalle"));
            assertEquals(Boolean.TRUE, e.atributos.get("puedeConfirmarAsistencia"));
        }
    }

    @Test void consultaDeRegistroNoOcultaFallasRealesDeCentral() {
        for (boolean movil : List.of(true, false)) {
            var e = conSesion(); e.parametros.put("edicion", "Maraton2026"); e.prepararRegistro(false);
            e.falloRegistros = new web.remoto.ErrorCentral("SERVICIO_NO_DISPONIBLE", "Central no disponible.");
            assertSame(e.falloRegistros, assertThrows(web.remoto.ErrorCentral.class, () -> {
                if (movil) new MovilRegistrosServlet().doGet(e.req, e.resp);
                else new ConsultaRegistroServlet().doGet(e.req, e.resp);
            }));
        }
    }

    @Test void elModoAsistenciaYLosMensajesLlegaranALaVista() throws Exception {
        var e = conSesion(); e.parametros.put("modo", "asistencia"); e.prepararRegistro(true);
        MensajesFlash.guardar(e.req, "/movil/registros", "Hecho", null);
        new MovilRegistrosServlet().doGet(e.req, e.resp);
        assertEquals(Boolean.TRUE, e.atributos.get("modoAsistencia"));
        assertEquals("Hecho", e.atributos.get("exito"));
        var sinModo = conSesion(); sinModo.prepararRegistro(true); new MovilRegistrosServlet().doGet(sinModo.req, sinModo.resp);
        assertEquals(Boolean.FALSE, sinModo.atributos.get("modoAsistencia"));
    }

    private static Escenario conSesion() { var e = new Escenario(); e.usuario = MovilAccesoTest.asistente("ana"); return e; }

    private static <T> T doble(Class<T> tipo, InvocationHandler h) {
        return tipo.cast(Proxy.newProxyInstance(tipo.getClassLoader(), new Class<?>[]{tipo}, h));
    }

    private static final class Escenario {
        DtUsuario usuario;
        int estado = 200;
        String redireccion;
        RuntimeException falloConfirmar;
        RuntimeException falloDetalle;
        RuntimeException falloRegistros;
        final Map<String, String> parametros = new HashMap<>();
        final Map<String, Object> atributos = new HashMap<>(), sesion = new HashMap<>();
        final List<String> llamadas = new ArrayList<>();
        final DtSeleccionEdicion seleccion = new DtSeleccionEdicion();
        final DtDetalleEdicion detalleEdicion = new DtDetalleEdicion();
        final DtConsultaRegistros registros = new DtConsultaRegistros();

        void prepararEdicion(String video) {
            var ed = MovilAccesoTest.edicion("Maraton2026"); ed.setVideoYoutube(video);
            seleccion.setEvento("Evento"); seleccion.setEdicion(ed); detalleEdicion.setEdicion(ed);
            parametros.putAll(Map.of("evento", "Evento", "nombre", "Maraton2026"));
        }

        void prepararRegistro(boolean asistio) {
            var r = new DtRegistro(LocalDate.of(2026, 3, 1), 1200, "Corredor", "Maraton2026", "ana", false);
            r.setAsistenciaConfirmada(asistio);
            registros.setRegistros(List.of(r)); registros.setDetalle(r);
        }

        final ServicioPortal portal = doble(ServicioPortal.class, (p, m, a) -> {
            llamadas.add(m.getName() + Arrays.toString(a));
            return switch (m.getName()) {
                case "consultarEdiciones" -> seleccion;
                case "consultarDetalleEdicion" -> { if (falloDetalle != null) throw falloDetalle; yield detalleEdicion; }
                case "consultarRegistros" -> { if (falloRegistros != null) throw falloRegistros; yield registros; }
                case "confirmarAsistencia" -> { if (falloConfirmar != null) throw falloConfirmar; yield Boolean.TRUE; }
                default -> throw new AssertionError("Consulta inesperada: " + m.getName());
            };
        });
        final ServletContext contexto = doble(ServletContext.class, (p, m, a) -> {
            if (!m.getName().equals("getAttribute")) throw new AssertionError(m.getName());
            Object valor = "central".equals(a[0]) ? portal : null;
            return valor;
        });
        final HttpSession session = doble(HttpSession.class, (p, m, a) -> switch (m.getName()) {
            case "getAttribute" -> sesion.get(a[0]);
            case "setAttribute" -> { sesion.put((String) a[0], a[1]); yield null; }
            case "removeAttribute" -> { sesion.remove(a[0]); yield null; }
            default -> throw new AssertionError(m.getName());
        });
        final RequestDispatcher dispatcher = doble(RequestDispatcher.class, (p, m, a) -> null);
        final HttpServletRequest req = doble(HttpServletRequest.class, (p, m, a) -> switch (m.getName()) {
            case "getSession" -> {
                if (usuario != null) { sesion.put("usuario", usuario); sesion.put(SesionWeb.CREDENCIAL, "token-sintetico"); }
                yield session;
            }
            case "getParameter" -> parametros.get(a[0]);
            case "getServletContext" -> contexto;
            case "setAttribute" -> { atributos.put((String) a[0], a[1]); yield null; }
            case "getRequestDispatcher" -> dispatcher;
            case "getContextPath" -> "/test";
            case "getMethod" -> "GET";
            default -> throw new AssertionError("Uso inesperado de la solicitud: " + m.getName());
        });
        final HttpServletResponse resp = doble(HttpServletResponse.class, (p, m, a) -> switch (m.getName()) {
            case "sendRedirect" -> { estado = 302; redireccion = (String) a[0]; yield null; }
            case "sendError" -> { estado = (Integer) a[0]; yield null; }
            case "setHeader" -> null;
            default -> throw new AssertionError(m.getName());
        });
    }
}
