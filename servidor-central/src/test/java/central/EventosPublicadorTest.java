package central;

import central.soap.EventosPublicador;
import com.sun.net.httpserver.HttpServer;
import datatypes.DtEvento;
import interfaces.ISistema;
import jakarta.xml.ws.Endpoint;
import org.junit.jupiter.api.Test;
import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.*;
import java.time.LocalDate;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

/** Transporte SOAP real; la fachada recibe un doble de ISistema y no abre JPA. */
class EventosPublicadorTest {
    @Test void publicaWsdlYDevuelveDatosConTildesFechaYCategorias() throws Exception {
        verificar(false);
    }

    @Test void falloInternoProduceFaultDeclaradoSinDetallesDePersistencia() throws Exception {
        verificar(true);
    }

    private void verificar(boolean fallar) throws Exception {
        ISistema sistema = (ISistema) Proxy.newProxyInstance(ISistema.class.getClassLoader(),
                new Class<?>[]{ISistema.class}, (objeto, metodo, argumentos) -> {
                    assertEquals("listarEventos", metodo.getName());
                    if (fallar) throw new IllegalStateException("JDBC dato-interno-no-publicar");
                    return List.of(new DtEvento("Día de tecnología", "TEC", "Descripción & pruebas", LocalDate.of(2026, 11, 5),
                            List.of("Tecnología", "Educación")));
                });
        HttpServer servidor = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        Endpoint endpoint = Endpoint.create(new EventosPublicador(sistema));
        endpoint.publish(servidor.createContext("/eventos"));
        servidor.start();
        try {
            URI uri = URI.create("http://127.0.0.1:" + servidor.getAddress().getPort() + "/eventos");
            HttpClient cliente = HttpClient.newHttpClient();
            var wsdl = cliente.send(HttpRequest.newBuilder(URI.create(uri + "?wsdl")).GET().build(), HttpResponse.BodyHandlers.ofString());
            assertEquals(200, wsdl.statusCode());
            assertTrue(wsdl.body().contains("listarEventos"));
            assertTrue(wsdl.body().contains("ConsultaException"));
            String xml = "<s:Envelope xmlns:s='http://schemas.xmlsoap.org/soap/envelope/'><s:Body>"
                    + "<e:listarEventos xmlns:e='https://eventos.uy/soap/eventos'/></s:Body></s:Envelope>";
            var respuesta = cliente.send(HttpRequest.newBuilder(uri).header("Content-Type", "text/xml; charset=UTF-8")
                    .POST(HttpRequest.BodyPublishers.ofString(xml)).build(), HttpResponse.BodyHandlers.ofString());
            assertEquals(fallar ? 500 : 200, respuesta.statusCode());
            if (fallar) {
                assertTrue(respuesta.body().contains("ConsultaFault"));
                assertFalse(respuesta.body().contains("JDBC"));
                assertFalse(respuesta.body().contains("dato-interno-no-publicar"));
            } else {
                assertTrue(respuesta.body().contains("Día de tecnología"));
                assertTrue(respuesta.body().contains("2026-11-05"));
                assertTrue(respuesta.body().contains("Educación"));
                assertTrue(respuesta.body().contains("Descripción &amp; pruebas"));
            }
        } finally {
            endpoint.stop(); servidor.stop(0);
        }
    }
}
