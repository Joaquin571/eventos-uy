package central;

import central.seguridad.SesionesRemotas;
import central.soap.EventosPublicador;
import central.soap.SesionesPublicador;
import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsServer;
import interfaces.ISistema;
import jakarta.xml.ws.Endpoint;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.security.SecureRandom;
import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Ciclo de vida del listener único y sus fachadas. La preparación TLS no inicializa JPA. */
public final class ServidorHttpsCentral implements AutoCloseable {
    private final ConfiguracionCentral configuracion;
    private final HttpsServer servidor;
    private final ExecutorService hilos;
    private final List<Endpoint> endpoints = new ArrayList<>();
    private boolean iniciado;
    private boolean cerrado;

    public ServidorHttpsCentral(ConfiguracionCentral configuracion) {
        this.configuracion = Objects.requireNonNull(configuracion);
        configuracion.validarHttps();
        var tls = configuracion.contextoTls();
        try {
            servidor = HttpsServer.create(new InetSocketAddress(configuracion.hostEscucha(), configuracion.puerto()), 0);
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo abrir la dirección y el puerto HTTPS configurados.", e);
        }
        hilos = Executors.newVirtualThreadPerTaskExecutor();
        servidor.setHttpsConfigurator(new HttpsConfigurator(tls));
        servidor.setExecutor(hilos);
    }

    public synchronized void iniciar(ISistema sistema) {
        if (iniciado || cerrado) throw new IllegalStateException("El servidor HTTPS ya fue iniciado o cerrado.");
        Objects.requireNonNull(sistema);
        iniciado = true;
        try {
            var sesiones = new SesionesRemotas(sistema, new SecureRandom(), Clock.systemUTC(),
                    configuracion.duracionSesion());
            publicar(configuracion.rutaEventos(), new EventosPublicador(sistema));
            publicar(configuracion.rutaSesiones(), new SesionesPublicador(sesiones));
            servidor.start();
        } catch (RuntimeException | Error e) {
            try { close(); } catch (RuntimeException cierre) { e.addSuppressed(cierre); }
            throw e;
        }
    }

    private void publicar(String ruta, Object fachada) {
        Endpoint endpoint = Endpoint.create(fachada);
        endpoints.add(endpoint);
        endpoint.publish(servidor.createContext(ruta));
    }

    public String urlEventos() {
        return configuracion.urlServicio(servidor.getAddress().getPort(), configuracion.rutaEventos());
    }

    public String urlSesiones() {
        return configuracion.urlServicio(servidor.getAddress().getPort(), configuracion.rutaSesiones());
    }

    @Override
    public synchronized void close() {
        if (cerrado) return;
        cerrado = true;
        RuntimeException fallo = null;
        for (var endpoint : endpoints) {
            try {
                if (endpoint.isPublished()) endpoint.stop();
            } catch (RuntimeException e) {
                if (fallo == null) fallo = new IllegalStateException("Falló el cierre de un endpoint SOAP.");
                fallo.addSuppressed(e);
            }
        }
        try {
            servidor.stop(0);
        } finally {
            hilos.shutdownNow();
        }
        if (fallo != null) throw fallo;
    }
}
