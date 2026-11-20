package web;

import contratos.ServicioPortal;
import contratos.dto.*;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.Test;
import web.remoto.ClienteSesionesCentral;
import web.remoto.ServicioSesiones;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/** Sitio móvil: acceso solo de asistentes sobre P03 y consultas con dobles locales de Central (no sustituyen SOAP real). */
class MovilAccesoTest {
    private static final List<String> RUTAS_PROTEGIDAS = List.of("eventos", "edicion", "registros");

    @Test void loginDeAsistenteCreaSesionConCredencialP03YVaAEventos() throws Exception {
        var e = new Escenario(); e.parametros.putAll(Map.of("identificador", "id", "contrasenia", "clave"));
        new MovilLoginServlet().doPost(e.req, e.resp);
        assertEquals(302, e.estado); assertEquals("/test/movil/eventos", e.redireccion);
        assertEquals("token-sintetico", e.sesion.get(SesionWeb.CREDENCIAL));
        assertInstanceOf(DtAsistente.class, e.sesion.get("usuario"));
        assertEquals(0, e.revocaciones); assertFalse(e.atributos.toString().contains("token-sintetico"));
    }

    @Test void organizadorQueSeAutenticaEsRechazadoYSuCredencialNuevaSeRevoca() throws Exception {
        var e = new Escenario(); e.rolLogin = "ORGANIZADOR"; e.parametros.putAll(Map.of("identificador", "org", "contrasenia", "clave"));
        new MovilLoginServlet().doPost(e.req, e.resp);
        assertEquals(1, e.revocaciones); assertTrue(e.sesion.isEmpty()); assertTrue(e.reenviado);
        assertEquals(MovilLoginServlet.MENSAJE_ROL, e.atributos.get("error"));
        assertEquals("/WEB-INF/views/movil/login.jsp", e.reenvio); assertEquals(0, e.invalidaciones);
    }

    @Test void rechazoDeOrganizadorNoTocaSuSesionDeEscritorio() throws Exception {
        var e = new Escenario(); e.conSesion(organizador("org"), "token-escritorio"); e.rolLogin = "ORGANIZADOR";
        new MovilLoginServlet().doPost(e.req, e.resp);
        assertEquals("token-escritorio", e.sesion.get(SesionWeb.CREDENCIAL)); assertEquals(0, e.invalidaciones);
        assertEquals(1, e.revocaciones); // se revocó token-sintetico, la credencial nueva, no la de escritorio
    }

    @Test void credencialesInvalidasMuestranMensajeYNoCreanSesion() throws Exception {
        var e = new Escenario(); e.falloLogin = true; e.parametros.putAll(Map.of("identificador", "id", "contrasenia", "clave"));
        new MovilLoginServlet().doPost(e.req, e.resp);
        assertTrue(e.sesion.isEmpty()); assertTrue(e.reenviado);
        assertEquals("Credenciales inválidas.", e.atributos.get("error")); assertEquals("id", e.atributos.get("identificador"));
    }

    @Test void loginMuestraAvisoDeSesionVencidaYDeRolNoPermitido() throws Exception {
        var vencida = new Escenario(); vencida.parametros.put("motivo", "vencida");
        new MovilLoginServlet().doGet(vencida.req, vencida.resp);
        assertEquals(MovilLoginServlet.MENSAJE_VENCIDA, vencida.atributos.get("aviso"));
        var rol = new Escenario(); rol.parametros.put("motivo", "rol");
        new MovilLoginServlet().doGet(rol.req, rol.resp);
        assertEquals(MovilLoginServlet.MENSAJE_ROL, rol.atributos.get("error"));
    }

    @Test void organizadorConSesionDeEscritorioNoAccedeAlSitioMovil() throws Exception {
        for (String ruta : RUTAS_PROTEGIDAS) {
            var e = new Escenario(); e.conSesion(organizador("org"), "token-escritorio");
            e.parametros.put("nombre", "Maraton2026");
            llamar(ruta, e);
            assertEquals(302, e.estado, ruta); assertEquals("/test/movil/login?motivo=rol", e.redireccion, ruta);
            assertFalse(e.reenviado, ruta); assertEquals(0, e.consultasCentral, ruta);
        }
    }

    @Test void visitanteSinSesionVaAlLoginMovilSinConsultarCentral() throws Exception {
        for (String ruta : RUTAS_PROTEGIDAS) {
            var e = new Escenario(); llamar(ruta, e);
            assertEquals("/test/movil/login", e.redireccion, ruta); assertEquals(0, e.consultasCentral, ruta);
        }
        var inicio = new Escenario(); new MovilInicioServlet().doGet(inicio.req, inicio.resp);
        assertEquals("/test/movil/login", inicio.redireccion);
    }

    @Test void entradaConSesionDeAsistenteVaAEventos() throws Exception {
        var e = new Escenario(); e.conSesion(asistente("ana"), "token-sintetico");
        new MovilInicioServlet().doGet(e.req, e.resp);
        assertEquals("/test/movil/eventos", e.redireccion);
    }

    @Test void eventosListaLosQueDevuelveCentralYLimpiaElParametro() throws Exception {
        var e = new Escenario(); e.conSesion(asistente("ana"), "token-sintetico"); e.parametros.put("evento", " Evento ");
        e.seleccion.setEventos(List.of(new DtEvento("Evento", "EV", "Desc", FECHA, List.of())));
        e.seleccion.setEvento("Evento"); e.seleccion.setEdiciones(List.of(edicion("Maraton2026")));
        new MovilEventosServlet().doGet(e.req, e.resp);
        assertTrue(e.reenviado); assertEquals("consultarEdiciones[Evento, ]", e.llamada);
        assertEquals(1, ((List<?>) e.atributos.get("eventos")).size()); assertEquals(1, ((List<?>) e.atributos.get("ediciones")).size());
    }

    @Test void detalleDeEdicionMuestraFechasYMiRegistroSinVideoSimulado() throws Exception {
        var e = new Escenario(); e.conSesion(asistente("ana"), "token-sintetico");
        e.parametros.putAll(Map.of("evento", "Evento", "nombre", "Maraton2026"));
        var ed = edicion("Maraton2026"); e.seleccion.setEvento("Evento"); e.seleccion.setEdicion(ed);
        e.detalle.setEdicion(ed); e.detalle.setRegistroPropio(new DtRegistro(FECHA, 1200, "Corredor", "Maraton2026", "ana", true));
        new MovilEdicionServlet().doGet(e.req, e.resp);
        assertTrue(e.reenviado); assertEquals("07/03/2026", e.atributos.get("fechaInicio")); assertEquals("09/03/2026", e.atributos.get("fechaFin"));
        var propio = (DetalleRegistroVista) e.atributos.get("registroPropio");
        assertEquals("Corredor", propio.getNombreTipoRegistro()); assertEquals("Sí", propio.getUsoCodigo());
        assertFalse(e.atributos.containsKey("urlVideoEmbed"));
    }

    @Test void edicionSinNombreVuelveAEventosYSinResultadoEs404() throws Exception {
        var sinNombre = new Escenario(); sinNombre.conSesion(asistente("ana"), "token-sintetico");
        new MovilEdicionServlet().doGet(sinNombre.req, sinNombre.resp);
        assertEquals("/test/movil/eventos", sinNombre.redireccion); assertEquals(0, sinNombre.consultasCentral);
        var ausente = new Escenario(); ausente.conSesion(asistente("ana"), "token-sintetico"); ausente.parametros.put("nombre", "NoExiste");
        new MovilEdicionServlet().doGet(ausente.req, ausente.resp);
        assertEquals(404, ausente.estado); assertFalse(ausente.reenviado);
    }

    @Test void registrosSiempreConsultanLosPropiosAunqueSeManipuleElParametro() throws Exception {
        var e = new Escenario(); e.conSesion(asistente("ana"), "token-sintetico");
        e.parametros.putAll(Map.of("edicion", "Maraton2026", "asistente", "otro"));
        var registro = new DtRegistro(FECHA, 1200, "Corredor", "Maraton2026", "ana", false);
        e.registros.setRegistros(List.of(registro)); e.registros.setDetalle(registro);
        new MovilRegistrosServlet().doGet(e.req, e.resp);
        assertEquals("consultarRegistros[Maraton2026, ]", e.llamada); assertTrue(e.reenviado);
        assertEquals("No", ((DetalleRegistroVista) e.atributos.get("detalle")).getUsoCodigo());
    }

    @Test void logoutRevocaP03EliminaLaSesionYVuelveAlLoginMovil() throws Exception {
        var e = new Escenario(); e.conSesion(asistente("ana"), "token-sintetico");
        new MovilLogoutServlet().doPost(e.req, e.resp);
        assertEquals(1, e.revocaciones); assertEquals(1, e.invalidaciones); assertTrue(e.sesion.isEmpty());
        assertEquals("/test/movil/login", e.redireccion);
        var get = new Escenario(); new MovilLogoutServlet().doGet(get.req, get.resp);
        assertEquals(405, get.estado);
    }

    private static void llamar(String ruta, Escenario e) throws Exception {
        switch (ruta) {
            case "eventos" -> new MovilEventosServlet().doGet(e.req, e.resp);
            case "edicion" -> new MovilEdicionServlet().doGet(e.req, e.resp);
            default -> new MovilRegistrosServlet().doGet(e.req, e.resp);
        }
    }

    private static final LocalDate FECHA = LocalDate.of(2026, 3, 7);
    static DtOrganizador organizador(String nick) { return new DtOrganizador(nick, nick, nick + "@test.uy", "Descripción", ""); }
    static DtAsistente asistente(String nick) { return new DtAsistente(nick, nick, nick + "@test.uy", "Apellido", LocalDate.of(2000, 1, 1), null); }
    static DtEdicion edicion(String nombre) {
        DtEdicion d = new DtEdicion(nombre, "ED", FECHA, FECHA.plusDays(2), LocalDate.of(2026, 2, 1), "Ciudad", "País", "duenio");
        d.setEstado(EstadoEdicion.ACEPTADA); return d;
    }
    static <T> T doble(Class<T> tipo, InvocationHandler h) { return tipo.cast(Proxy.newProxyInstance(tipo.getClassLoader(), new Class<?>[]{tipo}, h)); }

    private static final class Escenario {
        final Map<String, Object> sesion = new HashMap<>(), atributos = new HashMap<>();
        final Map<String, String> parametros = new HashMap<>();
        int invalidaciones, revocaciones, estado = 200, consultasCentral;
        boolean haySesion, falloLogin, reenviado;
        String rolLogin = "ASISTENTE", redireccion, reenvio, llamada;
        final DtSeleccionEdicion seleccion = new DtSeleccionEdicion();
        final DtDetalleEdicion detalle = new DtDetalleEdicion();
        final DtConsultaRegistros registros = new DtConsultaRegistros();

        final ServicioSesiones remoto = new ServicioSesiones() {
            public ClienteSesionesCentral.Inicio iniciar(String id, String clave) {
                if (falloLogin) throw new IllegalArgumentException("Credenciales inválidas.");
                return new ClienteSesionesCentral.Inicio("nick", "Nombre", rolLogin, "token-sintetico");
            }
            public ClienteSesionesCentral.Inicio iniciarMovil(String id, String clave) {
                return iniciar(id, clave);
            }
            public void cerrar(String credencial) { assertEquals("token-sintetico", credencial); revocaciones++; }
        };
        final ServicioPortal portal = doble(ServicioPortal.class, (p, m, a) -> {
            consultasCentral++; llamada = m.getName() + Arrays.toString(a);
            return switch (m.getName()) {
                case "consultarEdiciones" -> seleccion;
                case "consultarDetalleEdicion" -> detalle;
                case "consultarRegistros" -> registros;
                default -> throw new AssertionError("Consulta inesperada: " + m.getName());
            };
        });
        final ServletContext contexto = doble(ServletContext.class, (p, m, a) -> {
            if (!m.getName().equals("getAttribute")) throw new AssertionError(m.getName());
            Object valor = "sesionesCentral".equals(a[0]) ? remoto : "central".equals(a[0]) ? portal : null;
            return valor;
        });
        final HttpSession session = doble(HttpSession.class, (p, m, a) -> switch (m.getName()) {
            case "getAttribute" -> sesion.get(a[0]);
            case "setAttribute" -> { sesion.put((String) a[0], a[1]); yield null; }
            case "invalidate" -> { invalidaciones++; sesion.clear(); haySesion = false; yield null; }
            default -> throw new AssertionError(m.getName());
        });
        final RequestDispatcher dispatcher = doble(RequestDispatcher.class, (p, m, a) -> {
            if (!m.getName().equals("forward")) throw new AssertionError(m.getName());
            reenviado = true; return null;
        });
        final HttpServletRequest req = doble(HttpServletRequest.class, (p, m, a) -> switch (m.getName()) {
            case "getSession" -> {
                if (a != null && a.length == 1 && Boolean.TRUE.equals(a[0])) haySesion = true;
                yield haySesion ? session : null;
            }
            case "getParameter" -> parametros.get(a[0]);
            case "getServletContext" -> contexto;
            case "setAttribute" -> { atributos.put((String) a[0], a[1]); yield null; }
            case "getRequestDispatcher" -> { reenvio = (String) a[0]; yield dispatcher; }
            case "getContextPath" -> "/test";
            default -> throw new AssertionError(m.getName());
        });
        final HttpServletResponse resp = doble(HttpServletResponse.class, (p, m, a) -> switch (m.getName()) {
            case "sendRedirect" -> { estado = 302; redireccion = (String) a[0]; yield null; }
            case "sendError" -> { estado = (Integer) a[0]; yield null; }
            case "setHeader" -> null;
            default -> throw new AssertionError(m.getName());
        });

        void conSesion(DtUsuario usuario, String credencial) {
            sesion.put("usuario", usuario); sesion.put(SesionWeb.CREDENCIAL, credencial); haySesion = true;
        }
    }
}
