package central;

import central.soap.EventosPublicador;
import jakarta.xml.ws.Endpoint;
import persistencia.BaseDeDatos;
import java.nio.file.Path;

public final class Main {
    private Main() { }

    public static void main(String[] args) {
        ConfiguracionCentral configuracion = new ConfiguracionCentral(
                Path.of(System.getProperty("user.home"), ".eventosUy", "central.properties"));
        String url = configuracion.urlServicio();
        BaseDeDatos.inicializar(configuracion.propiedadesJpa());
        Endpoint endpoint;
        try {
            endpoint = Endpoint.publish(url, new EventosPublicador());
        } catch (RuntimeException error) {
            BaseDeDatos.cerrar();
            throw error;
        }
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            endpoint.stop();
            BaseDeDatos.cerrar();
        }));
        System.out.println("Servicio de eventos publicado. WSDL: " + url + "?wsdl");
        swing.principal.main(args);
    }
}
