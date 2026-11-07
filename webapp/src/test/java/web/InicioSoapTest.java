package web;

import central.soap.EventosPublicador;
import com.sun.net.httpserver.HttpServer;
import datatypes.DtEvento;
import interfaces.ISistema;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.*;
import jakarta.xml.ws.Endpoint;
import org.junit.jupiter.api.*;
import web.soap.ClienteEventosCentral;
import web.soap.generado.EventoSoap;
import java.lang.reflect.*;
import java.net.URI;
import java.net.InetSocketAddress;
import java.time.LocalDate;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

/** Cliente wsimport -> SOAP real -> fachada; Servlet con request/response simulados. Sin JPA. */
class InicioSoapTest {
    HttpServer servidor;
    Endpoint endpoint;
    URI direccion;
    boolean fallar;

    @BeforeEach void publicar() throws Exception {
        ISistema sistema = doble(ISistema.class, (objeto, metodo, argumentos) -> {
            assertEquals("listarEventos", metodo.getName());
            if (fallar) throw new IllegalStateException("Hibernate: detalle reservado");
            return List.of(new DtEvento("Encuentro ñ", "EN", "Descripción", LocalDate.of(2026, 11, 5), List.of("Arte")),
                    new DtEvento("Otro evento", "OT", "Otra descripción", null, List.of("Ciencia")));
        });
        servidor = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        endpoint = Endpoint.create(new EventosPublicador(sistema));
        endpoint.publish(servidor.createContext("/consulta"));
        servidor.start();
        direccion = URI.create("http://127.0.0.1:" + servidor.getAddress().getPort() + "/consulta");
    }

    @AfterEach void cerrar() { endpoint.stop(); servidor.stop(0); }

    @Test void clienteGeneradoRecuperaTodosLosCamposConEndpointSustituido() {
        var datos = new ClienteEventosCentral(direccion).listarEventos();
        assertEquals(2, datos.size());
        assertEquals("Encuentro ñ", datos.get(0).getNombre());
        assertEquals("EN", datos.get(0).getSigla());
        assertEquals("Descripción", datos.get(0).getDescripcion());
        assertEquals("2026-11-05", datos.get(0).getFechaAlta());
        assertEquals(List.of("Arte"), datos.get(0).getCategorias());
        assertNull(datos.get(1).getFechaAlta());
    }

    @Test void portadaSinFiltroConservaTodosLosResultados() throws Exception { comprobar(null, 2, false); }
    @Test void portadaMantieneFiltroDeCategoria() throws Exception { comprobar("Arte", 1, false); }
    @Test void categoriaDesconocidaProduceListaVaciaSinError() throws Exception { comprobar("Inexistente", 0, false); }
    @Test void falloRemotoDevuelvePaginaPropia503() throws Exception { fallar = true; comprobar(null, 0, true); }
    @Test void centralApagadoNoUsaLogicaLocalComoRespaldo() throws Exception {
        endpoint.stop(); servidor.stop(0); comprobar(null, 0, true);
    }

    private void comprobar(String categoria, int cantidad, boolean error) throws Exception {
        Map<String, Object> atributos = new HashMap<>();
        List<String> vistas = new ArrayList<>();
        List<Integer> estados = new ArrayList<>();
        HttpServletRequest req = doble(HttpServletRequest.class, (objeto, metodo, argumentos) -> switch (metodo.getName()) {
            case "getParameter" -> categoria;
            case "setAttribute" -> { atributos.put((String) argumentos[0], argumentos[1]); yield null; }
            case "getRequestDispatcher" -> {
                vistas.add((String) argumentos[0]);
                yield doble(RequestDispatcher.class, (o, m, a) -> { assertEquals("forward", m.getName()); return null; });
            }
            default -> throw new AssertionError("No debe acceder a lógica local/sesión: " + metodo.getName());
        });
        HttpServletResponse resp = doble(HttpServletResponse.class, (objeto, metodo, argumentos) -> {
            assertEquals("setStatus", metodo.getName()); estados.add((Integer) argumentos[0]); return null;
        });
        new InicioServlet(new ClienteEventosCentral(direccion)).doGet(req, resp);
        assertEquals(List.of(error ? "/WEB-INF/views/servicio-no-disponible.jsp" : "/WEB-INF/views/inicio.jsp"), vistas);
        if (error) {
            assertEquals(List.of(503), estados);
            assertFalse(atributos.containsKey("eventos"));
        } else {
            assertTrue(estados.isEmpty());
            Collection<?> datos = (Collection<?>) atributos.get("eventos");
            assertEquals(cantidad, datos.size());
            assertEquals(categoria, atributos.get("categoriaSeleccionada"));
            if (cantidad == 1) assertEquals("Encuentro ñ", ((EventoSoap) datos.iterator().next()).getNombre());
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> T doble(Class<T> tipo, InvocationHandler comportamiento) {
        return (T) Proxy.newProxyInstance(tipo.getClassLoader(), new Class<?>[]{tipo}, comportamiento);
    }
}
