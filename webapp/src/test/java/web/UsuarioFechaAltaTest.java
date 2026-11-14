package web;

import datatypes.DtOrganizador;
import interfaces.ISistema;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/** Prueba de controlador con dobles: no inicia Tomcat, JPA ni PostgreSQL. */
class UsuarioFechaAltaTest {
    @Test
    void perfilFormateaFechaPersistidaSinDependerDeLaFechaDelNavegador() throws Exception {
        assertEquals("02/11/2026", consultar(LocalDate.of(2026, 11, 2)).get("fechaAlta"));
    }

    @Test
    void perfilAnteriorSinFechaNoInventaUnaFechaDeAlta() throws Exception {
        assertEquals("No registrada", consultar(null).get("fechaAlta"));
    }

    private Map<String, Object> consultar(LocalDate fecha) throws Exception {
        DtOrganizador perfil = new DtOrganizador("organizador", "Nombre", "org@test.uy",
                "Descripción", "", fecha);
        Map<String, Object> atributos = new HashMap<>();
        ISistema sistema = doble(ISistema.class, (p, m, a) -> switch (m.getName()) {
            case "consultarUsuario" -> { assertEquals("organizador", a[0]); yield perfil; }
            case "obtenerEdicionesOrganizador" -> List.of();
            default -> throw new AssertionError("Caso de uso inesperado: " + m.getName());
        });
        ServletContext contexto = doble(ServletContext.class, (p, m, a) -> {
            assertEquals("getAttribute", m.getName()); assertEquals("sistema", a[0]);
            return sistema;
        });
        RequestDispatcher vista = doble(RequestDispatcher.class, (p, m, a) -> {
            assertEquals("forward", m.getName()); return null;
        });
        HttpServletRequest req = doble(HttpServletRequest.class, (p, m, a) -> switch (m.getName()) {
            case "getParameter" -> { assertEquals("nick", a[0]); yield "organizador"; }
            case "getServletContext" -> contexto;
            case "getSession" -> null;
            case "setAttribute" -> { atributos.put((String) a[0], a[1]); yield null; }
            case "getRequestDispatcher" -> {
                assertEquals("/WEB-INF/views/usuario.jsp", a[0]); yield vista;
            }
            default -> throw new AssertionError(m.getName());
        });
        HttpServletResponse resp = doble(HttpServletResponse.class, (p, m, a) -> {
            throw new AssertionError("No debe responder con error o redirect: " + m.getName());
        });
        new UsuarioServlet().doGet(req, resp);
        assertSame(perfil, atributos.get("perfil"));
        assertEquals(false, atributos.get("esPropio"));
        return atributos;
    }

    private static <T> T doble(Class<T> tipo, InvocationHandler invocacion) {
        return tipo.cast(Proxy.newProxyInstance(tipo.getClassLoader(), new Class<?>[]{tipo}, invocacion));
    }
}
