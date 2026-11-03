package web;

import datatypes.DtAsistente;
import datatypes.DtOrganizador;
import interfaces.ISistema;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.*;
import org.junit.jupiter.api.Test;

import java.lang.reflect.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/** Peticiones atravesando el filtro real; no inicia JPA ni PostgreSQL. */
class CsrfTest {
    @Test
    void getGeneraTokenAleatorioPorSesionYLoConservaEntreFormularios() throws Exception {
        Escenario e = new Escenario(); e.filtrar();
        String primero = (String) e.atributos.get("csrfToken");
        assertNotNull(primero); assertEquals(43, primero.length());
        e.filtrar(); assertEquals(primero, e.atributos.get("csrfToken"));
        Escenario otra = new Escenario(); otra.filtrar();
        assertNotEquals(primero, otra.atributos.get("csrfToken"));
        assertEquals("private, no-store", e.headers.get("Cache-Control"));
    }

    @Test
    void postValidoLlegaAlControladorYElTokenNoSeConsume() throws Exception {
        Escenario e = new Escenario(); e.prepararPost(); e.filtrar();
        assertEquals(1, e.continuaciones);
        e.filtrar(); assertEquals(2, e.continuaciones);
    }

    @Test
    void ausenteIncorrectoODuplicadoNoEjecutanControlador() throws Exception {
        for (String[] token : new String[][]{null, {"incorrecto"}, {"a", "b"}}) {
            Escenario e = new Escenario(); e.prepararPost(); e.parametros = token;
            e.filtrar(); assertEquals(403, e.estado); assertEquals(0, e.continuaciones);
        }
    }

    @Test
    void sesionExpiradaNoCreaOtraSesionNiLeeElCuerpo() throws Exception {
        Escenario e = new Escenario(); e.prepararPost(); e.sesionExiste = false;
        e.filtrar(); assertEquals(403, e.estado); assertEquals(0, e.continuaciones);
        assertFalse(e.sesionExiste); assertEquals(0, e.lecturasParametros);
    }

    @Test
    void tokenDeOtraSesionOAnteriorAlLoginNoAutoriza() throws Exception {
        Escenario e = new Escenario(); e.prepararPost(); String anterior = e.parametros[0];
        e.sesion.clear(); e.metodo = "GET"; e.ruta = "/login"; e.filtrar();
        e.metodo = "POST"; e.parametros = new String[]{anterior}; e.continuaciones = 0;
        e.filtrar(); assertEquals(403, e.estado); assertEquals(0, e.continuaciones);
    }

    @Test
    void tokenEnUrlSeRechazaAunqueCoincida() throws Exception {
        Escenario e = new Escenario(); e.prepararPost(); e.query = "%5Fcsrf=" + e.parametros[0];
        e.filtrar(); assertEquals(403, e.estado); assertEquals(0, e.continuaciones);
    }

    @Test
    void tokenValidoNoSustituyeElRol() throws Exception {
        Escenario e = new Escenario(); e.prepararPost(); e.ruta = "/alta-edicion";
        e.sesion.put("usuario", new DtAsistente("ana", "Ana", "a@test.uy", "Pérez", null, null));
        e.filtrar(); assertEquals(403, e.estado); assertEquals(0, e.continuaciones);
    }

    @Test
    void multipartUsaParametrosDelContenedorSinLeerFlujoNiPart() throws Exception {
        Escenario e = new Escenario(); e.prepararPost(); e.ruta = "/alta-edicion";
        e.sesion.put("usuario", new DtOrganizador("org", "Org", "o@test.uy", "Descripción", ""));
        e.filtrar(); assertEquals(1, e.continuaciones); assertEquals(1, e.lecturasParametros);
        // El doble falla si el filtro intenta getReader/getInputStream/getPart/getParts.
    }

    @Test
    void limiteMultipartDelContenedorSeDevuelveComo413SinEjecutarElControlador() throws Exception {
        Escenario e = new Escenario(); e.prepararPost();
        HttpServletRequest excesiva = doble(HttpServletRequest.class, (p,m,a) -> {
            if (m.getName().equals("getParameterValues")) throw new IllegalStateException("Límite multipart simulado");
            return m.invoke(e.req, a);
        });
        new FiltroAcceso().doFilter(excesiva, e.resp, (r,s) -> fail("No debe ejecutar el controlador."));
        assertEquals(413, e.estado);
    }

    @Test
    void logoutGetNoInvalidaYPostValidoSiLoHace() throws Exception {
        Escenario e = new Escenario(); e.prepararPost();
        e.sesion.put("usuario", new DtOrganizador("org", "Org", "o@test.uy", "Descripción", ""));
        new LogoutServlet().doGet(e.req, e.resp);
        assertEquals(405, e.estado); assertTrue(e.sesionExiste); assertFalse(e.sesion.isEmpty());
        e.ruta = "/logout";
        new FiltroAcceso().doFilter(e.req, e.resp, (req, resp) -> new LogoutServlet().doPost(e.req, e.resp));
        assertFalse(e.sesionExiste); assertEquals(302, e.estado);
    }

    @Test
    void recursosEstaticosGetNoCreanSesionPeroPostNoEvadeCsrf() throws Exception {
        Escenario e = new Escenario(); e.ruta = "/js/alta-edicion-fechas.js"; e.filtrar();
        assertFalse(e.sesionExiste); assertEquals(1, e.continuaciones);
        e.metodo = "POST"; e.continuaciones = 0; e.filtrar();
        assertEquals(403, e.estado); assertEquals(0, e.continuaciones);
    }

    static final class Escenario {
        String metodo = "GET", ruta = "/login", query;
        String[] parametros;
        boolean sesionExiste;
        int estado = 200, continuaciones, lecturasParametros;
        final Map<String,Object> sesion = new HashMap<>(), atributos = new HashMap<>();
        final Map<String,String> headers = new HashMap<>();
        final HttpSession session = doble(HttpSession.class, (p,m,a) -> switch (m.getName()) {
            case "getAttribute" -> sesion.get(a[0]);
            case "setAttribute" -> { sesion.put((String)a[0], a[1]); yield null; }
            case "invalidate" -> { sesion.clear(); sesionExiste = false; yield null; }
            default -> throw new AssertionError(m.getName());
        });
        final ISistema sistema = doble(ISistema.class, (p,m,a) -> {
            assertEquals("listarCategorias", m.getName()); return List.of();
        });
        final ServletContext contexto = doble(ServletContext.class, (p,m,a) -> {
            assertEquals("getAttribute", m.getName()); return sistema;
        });
        final HttpServletRequest req = doble(HttpServletRequest.class, (p,m,a) -> switch (m.getName()) {
            case "getMethod" -> metodo;
            case "getServletPath" -> ruta;
            case "getQueryString" -> query;
            case "getSession" -> {
                if (a == null || Boolean.TRUE.equals(a[0])) sesionExiste = true;
                yield sesionExiste ? session : null;
            }
            case "getParameterValues" -> { assertEquals("_csrf", a[0]); lecturasParametros++; yield parametros; }
            case "setCharacterEncoding" -> null;
            case "setAttribute" -> { atributos.put((String)a[0],a[1]); yield null; }
            case "getServletContext" -> contexto;
            case "getContextPath" -> "/test";
            default -> throw new AssertionError("No consumir cuerpo: " + m.getName());
        });
        final HttpServletResponse resp = doble(HttpServletResponse.class, (p,m,a) -> switch (m.getName()) {
            case "sendError" -> { estado = (int)a[0]; yield null; }
            case "sendRedirect" -> { estado = 302; yield null; }
            case "setHeader" -> { headers.put((String)a[0],(String)a[1]); yield null; }
            default -> throw new AssertionError(m.getName());
        });
        void filtrar() throws Exception {
            new FiltroAcceso().doFilter(req, resp, (rq, rs) -> continuaciones++);
        }
        void prepararPost() throws Exception {
            filtrar(); parametros = new String[]{(String) atributos.get("csrfToken")};
            metodo = "POST"; continuaciones = 0;
        }
    }

    static <T> T doble(Class<T> tipo, InvocationHandler h) {
        return tipo.cast(Proxy.newProxyInstance(tipo.getClassLoader(), new Class<?>[]{tipo},h));
    }
}
