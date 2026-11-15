package web;

import datatypes.*;
import excepciones.AccesoDenegadoException;
import interfaces.ISistema;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.*;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/** Adaptación HTTP: la política y la transacción reales se prueban en lógica. */
class ModificarPerfilAtomicoTest {
    @Test void transmiteImagenYActorDeSesionEnUnaSolaOperacionParaAmbosSubtipos() throws Exception {
        for (boolean org : new boolean[]{false, true}) {
            Escenario e = new Escenario(org); e.conImagen = true;
            new ModificarUsuarioServlet().doPost(e.request, e.response);
            assertEquals(1, e.modificaciones); assertEquals(302, e.estado);
            assertEquals("propio", e.datos.getNickname()); assertEquals("propio@test.uy", e.datos.getCorreoElectronico());
            assertEquals("propio", e.actor.nickname()); assertEquals(org, e.actor.esOrganizador());
            assertFalse(e.actor.esAdministradorLocal());
            assertArrayEquals(new byte[]{1, 2, 3}, e.imagen.getContenido());
            assertEquals("/test/usuario?nick=propio", e.redirect);
        }
    }

    @Test void ausenciaDeArchivoSeTransmiteComoImagenNula() throws Exception {
        Escenario e = new Escenario(false);
        new ModificarUsuarioServlet().doPost(e.request, e.response);
        assertEquals(1, e.modificaciones); assertNull(e.imagen); assertEquals(302, e.estado);
    }

    @Test void denegacionDeSistemaSePresentaComo403SinRedireccionDeExito() throws Exception {
        Escenario e = new Escenario(true); e.error = new AccesoDenegadoException("Denegado");
        new ModificarUsuarioServlet().doPost(e.request, e.response);
        assertEquals(403, e.estado); assertNull(e.redirect); assertFalse(e.forward);
    }

    @Test void errorDeOperacionConservaFormularioSinExitoParcial() throws Exception {
        Escenario e = new Escenario(false); e.error = new IllegalArgumentException("No se guardó el perfil.");
        new ModificarUsuarioServlet().doPost(e.request, e.response);
        assertTrue(e.forward); assertNull(e.redirect);
        assertEquals("No se guardó el perfil.", e.atributos.get("error"));
        assertEquals("Nuevo", ((Map<?, ?>) e.atributos.get("form")).get("nombre"));
        assertSame(e.perfil, e.sesion.get("usuario"));
    }

    private static final class Escenario {
        DtUsuario perfil;
        final Map<String, Object> sesion = new HashMap<>(), atributos = new HashMap<>();
        final Map<String, String> parametros = new HashMap<>(Map.of(
                "nombre", "Nuevo", "apellido", "Nuevo apellido", "fechaNac", "2000-01-01",
                "descripcion", "Nueva descripción", "sitioWeb", "https://test.uy",
                "nickname", "ajeno", "rol", "administrador", "contrasenia", "nueva", "confirmacion", "nueva"));
        boolean conImagen, forward;
        int estado = 200, modificaciones;
        String redirect;
        DtUsuario datos;
        DtImagen imagen;
        Actor actor;
        IllegalArgumentException error;
        final ISistema sistema = doble(ISistema.class, (p, m, a) -> switch (m.getName()) {
            case "consultarUsuario" -> perfil;
            case "listarInstituciones" -> List.of();
            case "modificarAsistente", "modificarOrganizador" -> {
                assertEquals(3, a.length);
                modificaciones++; datos = (DtUsuario) a[0]; imagen = (DtImagen) a[1]; actor = (Actor) a[2];
                if (error != null) throw error;
                yield null;
            }
            default -> throw new AssertionError("Llamada inesperada o escritura separada: " + m.getName());
        });
        final ServletContext contexto = doble(ServletContext.class, (p, m, a) -> {
            if (m.getName().equals("getAttribute")) return sistema;
            throw new AssertionError(m.getName());
        });
        final HttpSession session = doble(HttpSession.class, (p, m, a) -> switch (m.getName()) {
            case "getAttribute" -> sesion.get(a[0]);
            case "setAttribute" -> { sesion.put((String) a[0], a[1]); yield null; }
            case "removeAttribute" -> { sesion.remove(a[0]); yield null; }
            default -> throw new AssertionError(m.getName());
        });
        final RequestDispatcher dispatcher = doble(RequestDispatcher.class, (p, m, a) -> { forward = true; return null; });
        final Part parte = doble(Part.class, (p, m, a) -> switch (m.getName()) {
            case "getSize" -> 3L;
            case "getContentType" -> "image/png";
            case "getInputStream" -> new ByteArrayInputStream(new byte[]{1, 2, 3});
            default -> throw new AssertionError(m.getName());
        });
        final HttpServletRequest request = doble(HttpServletRequest.class, (p, m, a) -> switch (m.getName()) {
            case "getServletContext" -> contexto;
            case "getSession" -> session;
            case "getParameter" -> parametros.get(a[0]);
            case "getPart" -> conImagen ? parte : null;
            case "getContextPath" -> "/test";
            case "getAttribute" -> atributos.get(a[0]);
            case "setAttribute" -> { atributos.put((String) a[0], a[1]); yield null; }
            case "getRequestDispatcher" -> dispatcher;
            default -> throw new AssertionError(m.getName());
        });
        final HttpServletResponse response = doble(HttpServletResponse.class, (p, m, a) -> {
            switch (m.getName()) {
                case "sendRedirect" -> { estado = 302; redirect = (String) a[0]; }
                case "sendError" -> estado = (Integer) a[0];
                default -> throw new AssertionError(m.getName());
            }
            return null;
        });

        Escenario(boolean org) {
            perfil = org ? new DtOrganizador("propio", "Propio", "propio@test.uy", "Descripción", "")
                    : new DtAsistente("propio", "Propio", "propio@test.uy", "Apellido", LocalDate.of(2000, 1, 1), null);
            sesion.put("usuario", perfil);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> T doble(Class<T> tipo, InvocationHandler handler) {
        return (T) Proxy.newProxyInstance(tipo.getClassLoader(), new Class<?>[]{tipo}, handler);
    }
}
