import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.net.URI;
import java.nio.file.Path;
import java.security.KeyStore;
import java.util.Arrays;

/** Sonda de WSDL con la confianza de Java y validación de nombre/IP. No envía credenciales SOAP. */
public final class DiagnosticoHttps {
    public static void main(String[] args) throws Exception {
        if (args.length != 2) throw new IllegalArgumentException("Uso: DiagnosticoHttps URL_HTTPS_WSDL TRUSTSTORE_PKCS12");
        URI uri = URI.create(args[0]);
        if (!"https".equalsIgnoreCase(uri.getScheme()) || uri.getUserInfo() != null) {
            throw new IllegalArgumentException("Se requiere una URL HTTPS sin credenciales.");
        }
        String secreto = System.getenv("EVENTOS_TLS_PASSWORD");
        if (secreto == null || secreto.isEmpty()) throw new IllegalArgumentException("Falta EVENTOS_TLS_PASSWORD.");
        char[] clave = secreto.toCharArray();
        try {
            KeyStore almacen = KeyStore.getInstance("PKCS12");
            try (var entrada = java.nio.file.Files.newInputStream(Path.of(args[1]))) {
                almacen.load(entrada, clave);
            }
            TrustManagerFactory gestores = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            gestores.init(almacen);
            SSLContext tls = SSLContext.getInstance("TLS");
            tls.init(null, gestores.getTrustManagers(), null);
            HttpsURLConnection conexion = (HttpsURLConnection) uri.toURL().openConnection();
            conexion.setSSLSocketFactory(tls.getSocketFactory());
            conexion.setConnectTimeout(4000);
            conexion.setReadTimeout(12000);
            try {
                int estado = conexion.getResponseCode();
                if (estado != 200) throw new IllegalStateException("HTTP " + estado);
                try (var cuerpo = conexion.getInputStream()) {
                    byte[] muestra = cuerpo.readNBytes(4096);
                    if (!new String(muestra, java.nio.charset.StandardCharsets.UTF_8).contains("definitions")) {
                        throw new IllegalStateException("La respuesta no parece un WSDL.");
                    }
                }
                System.out.println("HTTPS Java, confianza PKCS12, SAN y WSDL: OK " + uri);
            } finally { conexion.disconnect(); }
        } catch (Exception error) {
            Throwable raiz = error;
            while (raiz.getCause() != null && raiz.getCause() != raiz) raiz = raiz.getCause();
            System.err.println("HTTPS Java falló: " + error.getClass().getName() + ": " + error.getMessage());
            if (raiz != error) System.err.println("Causa raíz: " + raiz.getClass().getName() + ": " + raiz.getMessage());
            System.exit(2);
        } finally { Arrays.fill(clave, '\0'); }
    }
}
