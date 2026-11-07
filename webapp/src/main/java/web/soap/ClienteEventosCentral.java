package web.soap;

import jakarta.xml.ws.BindingProvider;
import jakarta.xml.ws.WebServiceException;
import web.soap.generado.EventoSoap;
import web.soap.generado.EventosService;
import web.soap.generado.ConsultaException_Exception;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Properties;
import java.util.function.Supplier;

/** Solo la portada utiliza este cliente durante el primer corte vertical. */
public final class ClienteEventosCentral {
    private static final class Servicio {
        private static final EventosService INSTANCIA = new EventosService();
    }

    private final Supplier<String> direccion;

    public ClienteEventosCentral() { this.direccion = ClienteEventosCentral::leerDireccion; }
    public ClienteEventosCentral(URI direccion) { this.direccion = direccion::toString; }

    public List<EventoSoap> listarEventos() {
        var puerto = Servicio.INSTANCIA.getEventosPort();
        var contexto = ((BindingProvider) puerto).getRequestContext();
        contexto.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, direccion.get());
        contexto.put("com.sun.xml.ws.connect.timeout", 3000);
        contexto.put("com.sun.xml.ws.request.timeout", 10000);
        try {
            return puerto.listarEventos();
        } catch (ConsultaException_Exception error) {
            // No propagar faultString ni detalles técnicos a la vista.
            throw new WebServiceException("No se pudo consultar el Servidor Central.", error);
        }
    }

    private static String leerDireccion() {
        Path archivo = Path.of(System.getProperty("user.home"), ".eventosUy", "web.properties");
        Properties propiedades = new Properties();
        try (var lector = Files.newBufferedReader(archivo)) {
            propiedades.load(lector);
            String host = requerida(propiedades, "central.host");
            int puerto = Integer.parseInt(requerida(propiedades, "central.puerto"));
            String ruta = requerida(propiedades, "central.ruta");
            if (puerto < 1 || puerto > 65535 || !ruta.startsWith("/") || ruta.contains("?") || ruta.contains("#"))
                throw new IllegalArgumentException();
            return new URI("http", null, host, puerto, ruta, null, null).toString();
        } catch (Exception error) {
            throw new WebServiceException("Revisá ~/.eventosUy/web.properties.", error);
        }
    }

    private static String requerida(Properties propiedades, String clave) {
        String valor = propiedades.getProperty(clave);
        if (valor == null || valor.isBlank()) throw new IllegalArgumentException("Falta " + clave);
        return valor.strip();
    }
}
