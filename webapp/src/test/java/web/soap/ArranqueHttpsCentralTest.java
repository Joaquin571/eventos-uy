package web.soap;

import central.ConfiguracionCentral;
import central.ServidorHttpsCentral;
import com.sun.xml.ws.developer.JAXWSProperties;
import datatypes.DtOrganizador;
import interfaces.ISistema;
import jakarta.xml.ws.BindingProvider;
import jakarta.xml.ws.handler.MessageContext;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import web.soap.generado.EventosService;
import web.soap.generado.sesiones.SesionesService;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ArranqueHttpsCentralTest {
    private Path temporal;
    private Path almacen;
    private String clave;
    private SSLContext confianza;

    @BeforeAll
    void prepararTls(@TempDir Path temporal) throws Exception {
        this.temporal = temporal;
        almacen = temporal.resolve("central.p12");
        clave = UUID.randomUUID().toString();
        char[] caracteres = clave.toCharArray();
        try {
            EncabezadosSoapHttpsTest.generarCertificado(almacen, caracteres);
            var origen = KeyStore.getInstance("PKCS12");
            try (var entrada = Files.newInputStream(almacen)) { origen.load(entrada, caracteres); }
            var publico = KeyStore.getInstance("PKCS12");
            publico.load(null, null);
            publico.setCertificateEntry("central", origen.getCertificate("sonda"));
            var gestor = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            gestor.init(publico);
            confianza = SSLContext.getInstance("TLS");
            confianza.init(null, gestor.getTrustManagers(), null);
        } finally {
            Arrays.fill(caracteres, '\0');
        }
    }

    private Properties propiedades() {
        var p = new Properties();
        p.setProperty("central.host", "127.0.0.1");
        p.setProperty("central.escucha", "127.0.0.1");
        p.setProperty("central.puerto", "0");
        p.setProperty("central.ruta", "/eventos");
        p.setProperty("central.ruta.sesiones", "/sesiones");
        p.setProperty("sesiones.duracion.segundos", "60");
        p.setProperty("tls.almacen", almacen.toString());
        p.setProperty("tls.almacen.password", clave);
        p.setProperty("tls.clave.password", clave);
        return p; // Deliberadamente sin propiedades JDBC.
    }

    private ConfiguracionCentral configurar(Properties propiedades) throws Exception {
        Path archivo = Files.createTempFile(temporal, "central-", ".properties");
        try (var salida = Files.newBufferedWriter(archivo)) { propiedades.store(salida, null); }
        return new ConfiguracionCentral(archivo);
    }

    private ISistema sistema() {
        return (ISistema) Proxy.newProxyInstance(ISistema.class.getClassLoader(), new Class<?>[]{ISistema.class},
                (proxy, metodo, args) -> {
                    if (metodo.getName().equals("listarEventos")) return List.of();
                    assertEquals("autenticar", metodo.getName(), "El arranque no requiere otros casos de uso.");
                    if (!"clave-sintetica".equals(args[1])) throw new IllegalArgumentException("Credenciales inválidas.");
                    return new DtOrganizador("org", "Organizador", "org@test.uy", "Descripción", "");
                });
    }

    private void configurarPuerto(Object puerto, String url, SSLSocketFactory sockets) {
        var contexto = ((BindingProvider) puerto).getRequestContext();
        contexto.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, url);
        contexto.put(JAXWSProperties.SSL_SOCKET_FACTORY, sockets);
        contexto.put(JAXWSProperties.CONNECT_TIMEOUT, 2000);
        contexto.put(JAXWSProperties.REQUEST_TIMEOUT, 5000);
    }

    @Test
    void arrancaAmbasRutasHaceLoginLogoutYLiberacionDelPuerto() throws Exception {
        String url;
        try (var central = new ServidorHttpsCentral(configurar(propiedades()))) {
            central.iniciar(sistema());
            url = central.urlEventos();
            try (var http = HttpClient.newBuilder().sslContext(confianza).build()) {
                for (String ruta : List.of(central.urlEventos(), central.urlSesiones())) {
                    var respuesta = http.send(HttpRequest.newBuilder(URI.create(ruta + "?wsdl")).GET().build(),
                            HttpResponse.BodyHandlers.ofString());
                    assertEquals(200, respuesta.statusCode());
                    assertTrue(respuesta.body().contains("definitions"));
                    assertTrue(respuesta.body().contains("https://"));
                }
            }
            var eventos = new EventosService().getEventosPort();
            configurarPuerto(eventos, url, confianza.getSocketFactory());
            assertTrue(eventos.listarEventos().isEmpty());
            var login = new SesionesService().getSesionesPort();
            configurarPuerto(login, central.urlSesiones(), confianza.getSocketFactory());
            assertEquals("org", login.iniciarSesion("org", "clave-sintetica").getNickname());
            Object encabezados = ((BindingProvider) login).getResponseContext()
                    .get(MessageContext.HTTP_RESPONSE_HEADERS);
            String credencial = ((Map<?, ?>) encabezados).entrySet().stream()
                    .filter(e -> "X-EventosUy-Session".equalsIgnoreCase(String.valueOf(e.getKey())))
                    .map(e -> (String) ((List<?>) e.getValue()).getFirst()).findFirst().orElseThrow();
            var logout = new SesionesService().getSesionesPort();
            configurarPuerto(logout, central.urlSesiones(), confianza.getSocketFactory());
            ((BindingProvider) logout).getRequestContext().put(MessageContext.HTTP_REQUEST_HEADERS,
                    Map.of("Authorization", List.of("Bearer " + credencial)));
            assertDoesNotThrow(logout::cerrarSesion);
            assertDoesNotThrow(logout::cerrarSesion);
            assertThrows(IllegalStateException.class, () -> central.iniciar(sistema()));
        }
        try (var socket = new ServerSocket()) {
            socket.bind(new InetSocketAddress("127.0.0.1", URI.create(url).getPort()));
        }
    }

    @Test
    void certificadoAjenoYNombreIncorrectoSonRechazados() throws Exception {
        try (var central = new ServidorHttpsCentral(configurar(propiedades()))) {
            central.iniciar(sistema());
            try (var sinConfianza = HttpClient.newBuilder().sslContext(SSLContext.getDefault()).build();
                 var nombreIncorrecto = HttpClient.newBuilder().sslContext(confianza).build()) {
                assertThrows(SSLHandshakeException.class, () -> sinConfianza.send(
                        HttpRequest.newBuilder(URI.create(central.urlEventos() + "?wsdl")).GET().build(),
                        HttpResponse.BodyHandlers.discarding()));
                assertThrows(SSLHandshakeException.class, () -> nombreIncorrecto.send(
                        HttpRequest.newBuilder(URI.create(central.urlEventos().replace("127.0.0.1", "localhost")
                                + "?wsdl")).GET().build(), HttpResponse.BodyHandlers.discarding()));
            }
        }
    }

    @Test
    void tlsAusenteOInvalidoFallaAntesDeRecibirUnControlador() throws Exception {
        for (String campo : List.of("tls.almacen", "tls.almacen.password", "tls.clave.password")) {
            var p = propiedades();
            p.remove(campo);
            var config = configurar(p);
            assertThrows(IllegalStateException.class, () -> new ServidorHttpsCentral(config));
        }
        for (String campo : List.of("tls.almacen.password", "tls.clave.password")) {
            var p = propiedades();
            p.setProperty(campo, "incorrecta-sintetica");
            var config = configurar(p);
            var error = assertThrows(IllegalStateException.class, () -> new ServidorHttpsCentral(config));
            assertFalse(error.getMessage().contains("incorrecta-sintetica"));
        }
        Path invalido = temporal.resolve("invalido.p12");
        Files.writeString(invalido, "Esto no es un PKCS#12.");
        var p = propiedades();
        p.setProperty("tls.almacen", invalido.toString());
        var config = configurar(p);
        assertThrows(IllegalStateException.class, () -> new ServidorHttpsCentral(config));
    }

    @Test
    void rutasSuperpuestasSonRechazadasYSePuedeCerrarSinIniciar() throws Exception {
        var p = propiedades();
        p.setProperty("central.ruta.sesiones", "/eventos/sesiones");
        var invalida = configurar(p);
        assertThrows(IllegalStateException.class, () -> new ServidorHttpsCentral(invalida));
        var central = new ServidorHttpsCentral(configurar(propiedades()));
        central.close();
        assertDoesNotThrow(central::close);
        assertThrows(IllegalStateException.class, () -> central.iniciar(sistema()));
    }
}
