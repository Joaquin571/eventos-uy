package central;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/** Configuración obligatoria; no utiliza credenciales ni destinos de respaldo. */
public final class ConfiguracionCentral {
    private final Properties propiedades = new Properties();

    public ConfiguracionCentral(Path archivo) {
        try (var lector = Files.newBufferedReader(archivo)) {
            propiedades.load(lector);
        } catch (IOException error) {
            throw new IllegalStateException("No se pudo leer la configuración del Servidor Central.", error);
        }
    }

    private String requerida(String clave) {
        String valor = propiedades.getProperty(clave);
        if (valor == null || valor.isBlank()) throw new IllegalStateException("Falta configurar " + clave);
        return valor.strip();
    }

    public String urlServicio() {
        try {
            int puerto = Integer.parseInt(requerida("central.puerto"));
            String ruta = requerida("central.ruta");
            if (puerto < 1 || puerto > 65535 || !ruta.startsWith("/") || ruta.contains("?") || ruta.contains("#"))
                throw new IllegalArgumentException();
            return new URI("http", null, requerida("central.host"), puerto, ruta, null, null).toString();
        } catch (IllegalArgumentException | URISyntaxException error) {
            throw new IllegalStateException("Revisá host, puerto y ruta SOAP en central.properties.", error);
        }
    }

    public Map<String, String> propiedadesJpa() {
        String url = requerida("db.url");
        if (!url.startsWith("jdbc:postgresql://")) throw new IllegalStateException("db.url debe ser PostgreSQL.");
        Map<String, String> resultado = new HashMap<>();
        resultado.put("jakarta.persistence.jdbc.driver", "org.postgresql.Driver");
        resultado.put("jakarta.persistence.jdbc.url", url);
        resultado.put("jakarta.persistence.jdbc.user", requerida("db.usuario"));
        String clave = propiedades.getProperty("db.password");
        if (clave == null) throw new IllegalStateException("Falta configurar db.password");
        resultado.put("jakarta.persistence.jdbc.password", clave);
        // Esta fase no crea ni migra tablas. Requiere un esquema ya existente.
        resultado.put("hibernate.hbm2ddl.auto", "validate");
        return resultado;
    }
}
