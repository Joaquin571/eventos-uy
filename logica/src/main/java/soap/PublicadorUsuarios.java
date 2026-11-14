package soap;

import jakarta.xml.ws.Endpoint;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/** Punto de integración del integrante 2: invocar publicar() una sola vez al arrancar el central. */
public final class PublicadorUsuarios {
    private static Endpoint endpoint;
    private PublicadorUsuarios() { }

    public static synchronized void publicar() {
        if (endpoint != null && endpoint.isPublished()) return;
        Properties configuracion = cargar();
        String direccion = "http://" + configuracion.getProperty("host") + ":"
                + configuracion.getProperty("puerto") + configuracion.getProperty("ruta");
        endpoint = Endpoint.publish(direccion, new UsuariosEndpoint());
        Runtime.getRuntime().addShutdownHook(new Thread(PublicadorUsuarios::detener));
        System.out.println("Servicio de usuarios publicado: " + direccion + "?wsdl");
    }
    public static synchronized void detener() {
        if (endpoint != null) { endpoint.stop(); endpoint = null; }
    }
    private static Properties cargar() {
        try {
            Path archivo = Path.of(System.getProperty("user.home"), ".eventosUy", "usuarios-central.properties");
            if (!Files.exists(archivo)) {
                Files.createDirectories(archivo.getParent());
                try (InputStream plantilla = PublicadorUsuarios.class.getResourceAsStream("/usuarios-central.properties")) {
                    if (plantilla == null) throw new IOException("No se encontró la plantilla de configuración.");
                    Files.copy(plantilla, archivo);
                }
            }
            Properties propiedades = new Properties();
            try (InputStream entrada = Files.newInputStream(archivo)) { propiedades.load(entrada); }
            for (String clave : new String[]{"host", "puerto", "ruta"}) {
                if (propiedades.getProperty(clave, "").isBlank()) throw new IOException("Falta la propiedad " + clave);
            }
            int puerto = Integer.parseInt(propiedades.getProperty("puerto"));
            if (puerto < 1 || puerto > 65535 || !propiedades.getProperty("ruta").startsWith("/")) {
                throw new IOException("Puerto o ruta inválidos.");
            }
            return propiedades;
        } catch (IOException | NumberFormatException e) {
            throw new IllegalStateException("Revisá ~/.eventosUy/usuarios-central.properties.", e);
        }
    }
}