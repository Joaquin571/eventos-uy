package web.soap;

import central.soap.EventosPublicador;
import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsServer;
import datatypes.DtEvento;
import interfaces.ISistema;
import jakarta.xml.ws.Endpoint;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/** Publicador aislado para verificar el transporte TLS con Metro y sin JPA. */
public final class PruebaHttpsServidor {
    private PruebaHttpsServidor() { }

    public static void main(String[] args) throws Exception {
        String host = variable("SOAP_HTTPS_BIND");
        int puerto = Integer.parseInt(variable("SOAP_HTTPS_PORT"));
        String ruta = variable("SOAP_HTTPS_PATH");
        if (puerto < 0 || puerto > 65535 || !ruta.startsWith("/")
                || ruta.contains("?") || ruta.contains("#")) {
            throw new IllegalArgumentException("Puerto o ruta SOAP inválidos.");
        }

        SSLContext tls = contextoTls();
        Endpoint endpoint = Endpoint.create(new EventosPublicador(sistemaDePrueba()));
        HttpsServer servidor = HttpsServer.create(new InetSocketAddress(host, puerto), 0);
        var cerrado = new AtomicBoolean();
        Runnable cerrar = () -> {
            if (cerrado.compareAndSet(false, true)) {
                try {
                    if (endpoint.isPublished()) endpoint.stop();
                } finally {
                    servidor.stop(0);
                }
            }
        };
        Thread alSalir = new Thread(cerrar, "cerrar-prueba-soap-https");
        Runtime.getRuntime().addShutdownHook(alSalir);
        try {
            servidor.setHttpsConfigurator(new HttpsConfigurator(tls));
            endpoint.publish(servidor.createContext(ruta));
            servidor.start();
            URI url = new URI("https", null, host, servidor.getAddress().getPort(), ruta, null, null);
            System.out.println("SOAP_HTTPS_LISTO " + url);
            System.out.println("Presioná Enter para cerrar la prueba.");
            System.in.read();
        } finally {
            cerrar.run();
            Runtime.getRuntime().removeShutdownHook(alSalir);
        }
    }

    private static SSLContext contextoTls() throws Exception {
        char[] clave = variable("SOAP_HTTPS_KEYSTORE_PASSWORD").toCharArray();
        try {
            return contextoTls(Path.of(variable("SOAP_HTTPS_KEYSTORE")), clave);
        } finally {
            Arrays.fill(clave, '\0');
        }
    }

    static SSLContext contextoTls(Path archivo, char[] clave) throws Exception {
        var almacen = KeyStore.getInstance("PKCS12");
        try (var entrada = Files.newInputStream(archivo)) {
            almacen.load(entrada, clave);
        }
        var gestor = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        gestor.init(almacen, clave);
        var tls = SSLContext.getInstance("TLS");
        tls.init(gestor.getKeyManagers(), null, null);
        return tls;
    }

    private static ISistema sistemaDePrueba() {
        return (ISistema) Proxy.newProxyInstance(ISistema.class.getClassLoader(),
                new Class<?>[]{ISistema.class}, (proxy, metodo, args) -> {
                    if (!metodo.getName().equals("listarEventos")) {
                        throw new UnsupportedOperationException(metodo.getName());
                    }
                    return List.of(new DtEvento("Encuentro HTTPS ñ", "TLS", "Prueba SOAP sobre TLS",
                            LocalDate.of(2026, 11, 5), List.of("Arte")));
                });
    }

    static String variable(String nombre) {
        String valor = System.getenv(nombre);
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("Falta configurar " + nombre);
        }
        return valor;
    }
}
