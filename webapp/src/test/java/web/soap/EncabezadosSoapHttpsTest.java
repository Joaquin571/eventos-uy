package web.soap;

import central.soap.EventoSoap;
import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsServer;
import com.sun.xml.ws.developer.JAXWSProperties;
import jakarta.annotation.Resource;
import jakarta.jws.WebMethod;
import jakarta.jws.WebService;
import jakarta.xml.ws.BindingProvider;
import jakarta.xml.ws.Endpoint;
import jakarta.xml.ws.WebServiceContext;
import jakarta.xml.ws.handler.MessageContext;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.io.TempDir;
import web.soap.generado.EventosService;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class EncabezadosSoapHttpsTest {
    private static final String RESULTADO = "X-Prueba-Resultado";
    private static final String CONCURRENCIA = "X-Prueba-Concurrencia";
    private static final String REFERENCIA_A = "A".repeat(43);
    private static final String REFERENCIA_B = "B".repeat(42) + "A";

    private HttpsServer servidor;
    private ExecutorService hilosServidor;
    private Endpoint endpoint;
    private String direccion;
    private SSLSocketFactory confianza;

    @BeforeAll
    void publicar(@TempDir Path temporal) throws Exception {
        Path almacen = temporal.resolve("servidor-prueba.p12");
        char[] clave = UUID.randomUUID().toString().toCharArray();
        try {
            generarCertificado(almacen, clave);
            confianza = confianzaLocal(almacen, clave);
            servidor = HttpsServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
            servidor.setHttpsConfigurator(new HttpsConfigurator(PruebaHttpsServidor.contextoTls(almacen, clave)));
        } finally {
            Arrays.fill(clave, '\0');
        }
        hilosServidor = Executors.newVirtualThreadPerTaskExecutor();
        servidor.setExecutor(hilosServidor);
        endpoint = Endpoint.create(new Sonda());
        endpoint.publish(servidor.createContext("/sonda"));
        servidor.start();
        direccion = "https://127.0.0.1:" + servidor.getAddress().getPort() + "/sonda";
    }

    @AfterAll
    void cerrar() {
        try {
            if (endpoint != null && endpoint.isPublished()) endpoint.stop();
        } finally {
            try {
                if (servidor != null) servidor.stop(0);
            } finally {
                if (hilosServidor != null) hilosServidor.shutdownNow();
            }
        }
    }

    @Test
    void clienteGeneradoEnviaDosReferenciasYLeeElEncabezadoDeRespuesta() throws Exception {
        assertEquals("referencia-a", invocar(List.of("Bearer " + REFERENCIA_A), false));
        assertEquals("referencia-b", invocar(List.of("Bearer " + REFERENCIA_B), false));
        assertEquals("ausente", invocar(List.of(), false));
    }

    @Test
    void distingueAutorizacionDuplicada() throws Exception {
        assertEquals("duplicado", invocar(List.of("Bearer " + REFERENCIA_A, "Bearer " + REFERENCIA_B), false));
    }

    @Test
    void distingueEsquemaFormatoYValoresAgrupados() throws Exception {
        assertEquals("malformado", invocar(List.of("Basic " + REFERENCIA_A), false));
        assertEquals("malformado", invocar(List.of("Bearer"), false));
        assertEquals("malformado", invocar(List.of("Bearer " + REFERENCIA_A + ", Bearer " + REFERENCIA_B), false));
    }

    @Test
    void lasPeticionesSuperpuestasMantienenSusEncabezadosSeparados() throws Exception {
        try (var clientes = Executors.newVirtualThreadPerTaskExecutor()) {
            var primera = clientes.submit(() -> invocar(List.of("Bearer " + REFERENCIA_A), true));
            var segunda = clientes.submit(() -> invocar(List.of("Bearer " + REFERENCIA_B), true));
            assertEquals("referencia-a", primera.get(15, TimeUnit.SECONDS));
            assertEquals("referencia-b", segunda.get(15, TimeUnit.SECONDS));
        }
    }

    private String invocar(List<String> autorizacion, boolean concurrente) throws Exception {
        var puerto = new EventosService().getEventosPort();
        var proveedor = (BindingProvider) puerto;
        var contexto = proveedor.getRequestContext();
        contexto.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, direccion);
        contexto.put(JAXWSProperties.SSL_SOCKET_FACTORY, confianza);
        contexto.put(JAXWSProperties.CONNECT_TIMEOUT, 3000);
        contexto.put(JAXWSProperties.REQUEST_TIMEOUT, 12000);
        Map<String, List<String>> encabezados = new HashMap<>();
        if (!autorizacion.isEmpty()) encabezados.put("Authorization", autorizacion);
        if (concurrente) encabezados.put(CONCURRENCIA, List.of("si"));
        contexto.put(MessageContext.HTTP_REQUEST_HEADERS, encabezados);
        assertTrue(puerto.listarEventos().isEmpty());
        var respuesta = encabezados(proveedor.getResponseContext().get(MessageContext.HTTP_RESPONSE_HEADERS), RESULTADO);
        assertEquals(1, respuesta.size(), "Debe haber un único encabezado de resultado");
        return respuesta.getFirst();
    }

    private static List<String> encabezados(Object propiedad, String nombre) {
        if (!(propiedad instanceof Map<?, ?> mapa)) return List.of();
        return mapa.entrySet().stream()
                .filter(entrada -> entrada.getKey() instanceof String clave && nombre.equalsIgnoreCase(clave))
                .flatMap(entrada -> ((List<?>) entrada.getValue()).stream())
                .map(String.class::cast)
                .toList();
    }

    static void generarCertificado(Path almacen, char[] clave) throws Exception {
        String binario = System.getProperty("os.name").startsWith("Windows") ? "keytool.exe" : "keytool";
        var proceso = new ProcessBuilder(Path.of(System.getProperty("java.home"), "bin", binario).toString(),
                "-genkeypair", "-alias", "sonda", "-keyalg", "RSA", "-keysize", "2048",
                "-validity", "2", "-dname", "CN=Sonda HTTPS", "-ext", "SAN=IP:127.0.0.1",
                "-storetype", "PKCS12", "-keystore", almacen.toString(),
                "-storepass:env", "SOAP_SONDA_CLAVE")
                .redirectOutput(ProcessBuilder.Redirect.DISCARD)
                .redirectError(ProcessBuilder.Redirect.DISCARD);
        proceso.environment().put("SOAP_SONDA_CLAVE", new String(clave));
        Process keytool = proceso.start();
        try {
            assertTrue(keytool.waitFor(20, TimeUnit.SECONDS), "keytool no terminó a tiempo");
            assertEquals(0, keytool.exitValue(), "keytool no pudo generar el certificado temporal");
        } finally {
            if (keytool.isAlive()) keytool.destroyForcibly();
        }
    }

    static SSLSocketFactory confianzaLocal(Path archivo, char[] clave) throws Exception {
        var origen = KeyStore.getInstance("PKCS12");
        try (var entrada = Files.newInputStream(archivo)) {
            origen.load(entrada, clave);
        }
        var publico = KeyStore.getInstance("PKCS12");
        publico.load(null, null);
        publico.setCertificateEntry("sonda", origen.getCertificate("sonda"));
        var gestor = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        gestor.init(publico);
        var tls = SSLContext.getInstance("TLS");
        tls.init(null, gestor.getTrustManagers(), null);
        return tls.getSocketFactory();
    }

    // Reutiliza la firma del WSDL existente solo para ejercitar el cliente generado.
    @WebService(name = "EventosPublicos", serviceName = "EventosService", portName = "EventosPort",
            targetNamespace = "https://eventos.uy/soap/eventos")
    public static class Sonda {
        private final CyclicBarrier barrera = new CyclicBarrier(2);
        @Resource
        private WebServiceContext servicio;

        @WebMethod
        public List<EventoSoap> listarEventos() {
            var contexto = servicio.getMessageContext();
            if (!encabezados(contexto.get(MessageContext.HTTP_REQUEST_HEADERS), CONCURRENCIA).isEmpty()) {
                try {
                    barrera.await(8, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException("Sonda interrumpida.", e);
                } catch (java.util.concurrent.BrokenBarrierException | java.util.concurrent.TimeoutException e) {
                    throw new IllegalStateException("Las peticiones de prueba no coincidieron.", e);
                }
            }
            var autorizacion = encabezados(servicio.getMessageContext().get(MessageContext.HTTP_REQUEST_HEADERS),
                    "Authorization");
            String resultado = switch (autorizacion.size()) {
                case 0 -> "ausente";
                case 1 -> clasificar(autorizacion.getFirst());
                default -> "duplicado";
            };
            contexto.put(MessageContext.HTTP_RESPONSE_HEADERS, Map.of(RESULTADO, List.of(resultado)));
            return List.of();
        }

        private String clasificar(String valor) {
            if (("Bearer " + REFERENCIA_A).equals(valor)) return "referencia-a";
            if (("Bearer " + REFERENCIA_B).equals(valor)) return "referencia-b";
            return "malformado";
        }
    }
}
