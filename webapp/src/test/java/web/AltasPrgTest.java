package web;

import datatypes.*;
import interfaces.ISistema;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.*;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/** Prueba filtro + controlador con tokens reales de sesión y lógica simulada. */
class AltasPrgTest {
    @Test
    void altaEdicionRedirigeYRefrescarElResultadoNoRepiteElAlta() throws Exception {
        comprobarPrg("/alta-edicion");
    }

    @Test
    void altaTipoRedirigeYRefrescarElResultadoNoRepiteElAlta() throws Exception {
        comprobarPrg("/alta-tipo-registro");
    }

    private void comprobarPrg(String ruta) throws Exception {
        Escenario e = new Escenario(ruta); e.formulario(); e.enviar();
        assertEquals(302, e.estado); assertEquals("/test" + ruta, e.redirect);
        assertFalse(e.forward); assertEquals(1, e.altas);
        e.formulario(); assertNotNull(e.atributos.get("exito"));
        e.formulario(); assertNull(e.atributos.get("exito")); assertEquals(1, e.altas);
    }

    @Test
    void errorDeFechasConservaTextoYSeleccionSinCrearEdicion() throws Exception {
        Escenario e = new Escenario("/alta-edicion"); e.formulario();
        e.parametros.put("fechaFin", "2026-11-01"); e.parametros.put("nombre", "Texto <con marcas>");
        e.enviar(); assertEquals(200, e.estado); assertTrue(e.forward); assertEquals(0, e.altas);
        assertEquals("Texto <con marcas>", e.form().get("nombre"));
        assertEquals("Evento", e.form().get("evento"));
        assertEquals("2026-11-01", e.form().get("fechaFin"));
        assertNotNull(e.atributos.get("error"));
    }

    @Test
    void errorNumericoConservaTodosLosCamposDelTipo() throws Exception {
        Escenario e = new Escenario("/alta-tipo-registro"); e.formulario();
        e.parametros.put("costo", "no-numero"); e.parametros.put("descripcion", "Descripción <guardada>");
        e.enviar(); assertTrue(e.forward); assertEquals(0, e.altas);
        assertEquals("no-numero", e.form().get("costo"));
        assertEquals("Descripción <guardada>", e.form().get("descripcion"));
        assertEquals("Edicion", e.form().get("edicion")); assertNotNull(e.atributos.get("error"));
    }

    @Test
    void errorDeNegocioConservaElFormularioYElMensajeOriginal() throws Exception {
        for (String ruta : List.of("/alta-edicion", "/alta-tipo-registro")) {
            Escenario e = new Escenario(ruta); e.formulario(); e.errorAlta = true;
            e.enviar(); assertTrue(e.forward); assertNull(e.redirect);
            assertEquals("Ya existe ese nombre.", e.atributos.get("error"));
            assertEquals("Nombre", e.form().get("nombre"));
        }
    }

    @Test
    void imagenFallidaMantieneRedirectExitoYAdvertenciaSinRepetirAlta() throws Exception {
        Escenario e = new Escenario("/alta-edicion"); e.formulario(); e.imagenFallida = true;
        e.enviar(); assertEquals(302, e.estado); assertEquals(1, e.altas);
        e.formulario(); assertNotNull(e.atributos.get("exito")); assertNotNull(e.atributos.get("advertencia"));
        e.formulario(); assertNull(e.atributos.get("advertencia")); assertEquals(1, e.altas);
    }

    @Test
    void tokenAusenteImpideLasDosAltasAntesDeInvocarLaLogica() throws Exception {
        for (String ruta : List.of("/alta-edicion", "/alta-tipo-registro")) {
            Escenario e = new Escenario(ruta); e.formulario(); e.parametros.remove("_csrf");
            e.enviar(); assertEquals(403, e.estado); assertEquals(0, e.altas); assertFalse(e.forward);
        }
    }

    @Test
    void tokenValidoNoPermiteCrearUnTipoEnEdicionAjena() throws Exception {
        Escenario e = new Escenario("/alta-tipo-registro"); e.formulario(); e.parametros.put("edicion", "Ajena");
        e.enviar(); assertEquals(0, e.altas); assertTrue(e.forward);
        assertEquals("La edición no pertenece al organizador.", e.atributos.get("error"));
    }

    static final class Escenario {
        String ruta;
        String metodo = "GET", redirect;
        int estado = 200, altas;
        boolean forward, errorAlta, imagenFallida;
        final Map<String,String> parametros = new HashMap<>();
        final Map<String,Object> atributos = new HashMap<>(), sesion = new HashMap<>();
        final DtEdicion edicion = new DtEdicion("Edicion", "ED", LocalDate.now(), LocalDate.now(),
                LocalDate.now(), "Ciudad", "País", "org");
        final HttpSession session = CsrfTest.doble(HttpSession.class, (p,m,a) -> switch (m.getName()) {
            case "getAttribute" -> sesion.get(a[0]);
            case "setAttribute" -> { sesion.put((String)a[0],a[1]); yield null; }
            case "removeAttribute" -> { sesion.remove(a[0]); yield null; }
            default -> throw new AssertionError(m.getName());
        });
        final ISistema sistema = CsrfTest.doble(ISistema.class, (p,m,a) -> switch (m.getName()) {
            case "listarCategorias" -> List.of();
            case "listarEventos" -> List.of(new DtEvento("Evento", "EV", "Descripción", null, List.of()));
            case "obtenerEdicionesOrganizador" -> List.of(edicion);
            case "altaEdicion", "altaTipoRegistro" -> {
                if (errorAlta) throw new IllegalArgumentException("Ya existe ese nombre.");
                altas++; yield true;
            }
            case "validarImagen" -> null;
            case "asociarImagen" -> { throw new IllegalArgumentException("Imagen no guardada."); }
            default -> throw new AssertionError(m.getName());
        });
        final ServletContext contexto = CsrfTest.doble(ServletContext.class, (p,m,a) -> sistema);
        final RequestDispatcher dispatcher = CsrfTest.doble(RequestDispatcher.class, (p,m,a) -> { forward = true; return null; });
        final Part imagen = CsrfTest.doble(Part.class, (p,m,a) -> switch (m.getName()) {
            case "getSize" -> (long)68;
            case "getContentType" -> "image/png";
            case "getInputStream" -> new ByteArrayInputStream(java.util.Base64.getDecoder().decode("iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAwMCAO+ip1sAAAAASUVORK5CYII="));
            default -> throw new AssertionError(m.getName());
        });
        final HttpServletRequest req = CsrfTest.doble(HttpServletRequest.class, (p,m,a) -> switch (m.getName()) {
            case "getMethod" -> metodo;
            case "getServletPath" -> ruta;
            case "getQueryString" -> null;
            case "getSession" -> session;
            case "getParameter" -> parametros.get(a[0]);
            case "getParameterValues" -> parametros.containsKey(a[0]) ? new String[]{parametros.get(a[0])} : null;
            case "getAttribute" -> atributos.get(a[0]);
            case "setAttribute" -> { atributos.put((String)a[0],a[1]); yield null; }
            case "setCharacterEncoding" -> null;
            case "getServletContext" -> contexto;
            case "getRequestDispatcher" -> dispatcher;
            case "getContextPath" -> "/test";
            case "getPart" -> imagenFallida ? imagen : null;
            default -> throw new AssertionError(m.getName());
        });
        final HttpServletResponse resp = CsrfTest.doble(HttpServletResponse.class, (p,m,a) -> switch (m.getName()) {
            case "sendError" -> { estado = (int)a[0]; yield null; }
            case "sendRedirect" -> { redirect = (String)a[0]; estado = 302; yield null; }
            case "setHeader" -> null;
            default -> throw new AssertionError(m.getName());
        });
        Escenario(String ruta) {
            this.ruta = ruta;
            sesion.put("usuario", new DtOrganizador("org", "Organización", "o@test.uy", "Descripción", ""));
            parametros.putAll(Map.of("evento","Evento","edicion","Edicion","nombre","Nombre","sigla","ED",
                    "ciudad","Ciudad","pais","País","fechaInicio","2026-11-02","fechaFin","2026-11-03"));
            parametros.putAll(Map.of("descripcion","Descripción","costo","100","cupo","10"));
        }
        void formulario() throws Exception {
            metodo = "GET"; solicitar(); parametros.put("_csrf", (String)atributos.get("csrfToken"));
        }
        void enviar() throws Exception { metodo = "POST"; solicitar(); }
        void solicitar() throws Exception {
            atributos.clear(); estado = 200; forward = false; redirect = null;
            new FiltroAcceso().doFilter(req,resp,(r,s) -> {
                if (ruta.equals("/alta-edicion")) {
                    var servlet = new AltaEdicionServlet();
                    if (metodo.equals("GET")) servlet.doGet(req,resp); else servlet.doPost(req,resp);
                } else {
                    var servlet = new AltaTipoRegistroServlet();
                    if (metodo.equals("GET")) servlet.doGet(req,resp); else servlet.doPost(req,resp);
                }
            });
        }
        @SuppressWarnings("unchecked") Map<String,String> form() { return (Map<String,String>)atributos.get("form"); }
    }
}
