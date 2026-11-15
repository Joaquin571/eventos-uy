package central;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.time.Duration;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;

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
        return urlServicio(puerto(), rutaEventos());
    }

    public String urlSesiones() {
        return urlServicio(puerto(), rutaSesiones());
    }

    String urlServicio(int puerto, String ruta) {
        try {
            URI url = new URI("https", null, requerida("central.host"), puerto, ruta, null, null);
            if (url.getHost() == null) throw new IllegalArgumentException();
            return url.toString();
        } catch (IllegalArgumentException | URISyntaxException error) {
            throw new IllegalStateException("Revisá host, puerto y ruta SOAP en central.properties.", error);
        }
    }

    public int puerto() {
        try {
            int puerto = Integer.parseInt(requerida("central.puerto"));
            if (puerto < 0 || puerto > 65535) throw new NumberFormatException();
            return puerto;
        } catch (NumberFormatException e) {
            throw new IllegalStateException("central.puerto debe estar entre 0 y 65535.");
        }
    }

    public String hostEscucha() {
        return requerida("central.escucha");
    }

    public String rutaPortal() {
        String valor=propiedades.getProperty("central.ruta.portal","/portal");
        if(!valor.matches("/[A-Za-z0-9_-]+(?:/[A-Za-z0-9_-]+)*")) throw new IllegalStateException("Revisá central.ruta.portal.");
        return valor;
    }

    public String rutaEventos() { return ruta("central.ruta"); }
    public String rutaSesiones() { return ruta("central.ruta.sesiones"); }

    private String ruta(String propiedad) {
        String ruta = requerida(propiedad);
        if (!ruta.matches("/[A-Za-z0-9_-]+(?:/[A-Za-z0-9_-]+)*")) {
            throw new IllegalStateException("Revisá la ruta de " + propiedad + ".");
        }
        return ruta;
    }

    public Duration duracionSesion() {
        try {
            long segundos = Long.parseLong(requerida("sesiones.duracion.segundos"));
            if (segundos <= 0) throw new NumberFormatException();
            return Duration.ofSeconds(segundos);
        } catch (NumberFormatException e) {
            throw new IllegalStateException("sesiones.duracion.segundos debe ser un entero positivo.");
        }
    }

    public void validarHttps() {
        urlServicio();
        urlSesiones();
        hostEscucha();
        duracionSesion();
        String portal=rutaPortal();
        for(String otra:new String[]{rutaEventos(),rutaSesiones()})
            if(portal.equals(otra) || portal.startsWith(otra+"/") || otra.startsWith(portal+"/"))
                throw new IllegalStateException("La ruta del portal debe ser independiente.");
        String eventos = rutaEventos(), sesiones = rutaSesiones();
        if (eventos.equals(sesiones) || eventos.startsWith(sesiones + "/") || sesiones.startsWith(eventos + "/")) {
            throw new IllegalStateException("Las rutas de eventos y sesiones deben ser independientes.");
        }
    }

    public SSLContext contextoTls() {
        char[] claveAlmacen = clave("tls.almacen.password");
        char[] clavePrivada = null;
        try {
            clavePrivada = clave("tls.clave.password");
            Path archivo = Path.of(requerida("tls.almacen"));
            if (!archivo.isAbsolute()) {
                throw new IllegalStateException("tls.almacen debe ser una ruta absoluta.");
            }
            var almacen = KeyStore.getInstance("PKCS12");
            try (var entrada = Files.newInputStream(archivo)) {
                almacen.load(entrada, claveAlmacen);
            }
            int claves = 0;
            for (var alias = almacen.aliases(); alias.hasMoreElements();) {
                String nombre = alias.nextElement();
                if (!almacen.isKeyEntry(nombre)) continue;
                claves++;
                var cadena = almacen.getCertificateChain(nombre);
                if (cadena == null || cadena.length == 0) {
                    throw new IllegalStateException("El almacén TLS requiere una cadena de certificados.");
                }
                for (var certificado : cadena) ((X509Certificate) certificado).checkValidity();
            }
            if (claves != 1) throw new IllegalStateException("El PKCS#12 debe contener una única clave privada.");
            var gestor = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            gestor.init(almacen, clavePrivada);
            var contexto = SSLContext.getInstance("TLS");
            contexto.init(gestor.getKeyManagers(), null, null);
            return contexto;
        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo cargar TLS. Revisá el PKCS#12, sus claves y certificados.");
        } finally {
            Arrays.fill(claveAlmacen, '\0');
            if (clavePrivada != null) Arrays.fill(clavePrivada, '\0');
        }
    }

    private char[] clave(String propiedad) {
        String clave = propiedades.getProperty(propiedad);
        if (clave == null || clave.isEmpty()) throw new IllegalStateException("Falta configurar " + propiedad);
        return clave.toCharArray();
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
