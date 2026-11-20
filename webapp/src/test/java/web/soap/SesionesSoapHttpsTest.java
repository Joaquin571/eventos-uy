package web.soap;

import central.seguridad.SesionesRemotas;
import central.soap.EventosPublicador;
import central.soap.SesionesPublicador;
import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsServer;
import com.sun.xml.ws.developer.JAXWSProperties;
import datatypes.DtOrganizador;
import datatypes.DtUsuario;
import interfaces.ISistema;
import jakarta.xml.ws.BindingProvider;
import jakarta.xml.ws.Endpoint;
import jakarta.xml.ws.handler.Handler;
import jakarta.xml.ws.handler.MessageContext;
import jakarta.xml.ws.handler.soap.SOAPHandler;
import jakarta.xml.ws.handler.soap.SOAPMessageContext;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import web.soap.generado.sesiones.Sesiones;
import web.soap.generado.sesiones.SesionesService;
import web.soap.generado.sesiones.SesionSoapException;
import web.soap.generado.sesiones.UsuarioSesionSoap;

import javax.net.ssl.SSLSocketFactory;
import javax.xml.namespace.QName;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SesionesSoapHttpsTest {
    private static final String CLAVE_PRUEBA = "secreto-sintetico-no-real";
    private final List<Endpoint> endpoints = new ArrayList<>();
    private final CyclicBarrier loginsConcurrentes = new CyclicBarrier(2);
    private HttpsServer servidor;
    private ExecutorService hilos;
    private SesionesRemotas registro;
    private String base;
    private SSLSocketFactory confianza;

    @BeforeAll
    void publicar(@TempDir Path temporal) throws Exception {
        ISistema sistema = (ISistema) Proxy.newProxyInstance(ISistema.class.getClassLoader(),
                new Class<?>[]{ISistema.class}, (proxy, metodo, args) -> {
                    if (metodo.getName().equals("listarEventos")) return List.of();
                    assertEquals("autenticar", metodo.getName());
                    String id = (String) args[0];
                    if (!CLAVE_PRUEBA.equals(args[1])) {
                        throw new IllegalArgumentException("Error sintético de credenciales.");
                    }
                    if (id.equals("admin")) return new DtUsuario("admin", "Admin", "a@test.uy");
                    if (id.startsWith("paralelo")) loginsConcurrentes.await(8, TimeUnit.SECONDS);
                    String nickname = id.equals("org@test.uy") ? "org" : id;
                    return new DtOrganizador(nickname, "Organización", "org@test.uy", "Descripción", "");
                });
        registro = new SesionesRemotas(sistema, new SecureRandom(), Clock.systemUTC(), Duration.ofMinutes(5));
        Path almacen = temporal.resolve("sesiones.p12");
        char[] clave = UUID.randomUUID().toString().toCharArray();
        try {
            EncabezadosSoapHttpsTest.generarCertificado(almacen, clave);
            confianza = EncabezadosSoapHttpsTest.confianzaLocal(almacen, clave);
            servidor = HttpsServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
            servidor.setHttpsConfigurator(new HttpsConfigurator(PruebaHttpsServidor.contextoTls(almacen, clave)));
        } finally {
            Arrays.fill(clave, '\0');
        }
        hilos = Executors.newVirtualThreadPerTaskExecutor();
        servidor.setExecutor(hilos);
        publicar("/eventos", new EventosPublicador(sistema));
        publicar("/sesiones", new SesionesPublicador(registro));
        publicar("/sesiones-otra", new SesionesPublicador(registro));
        servidor.start();
        base = "https://127.0.0.1:" + servidor.getAddress().getPort();
    }

    private void publicar(String ruta, Object fachada) {
        Endpoint endpoint = Endpoint.create(fachada);
        endpoints.add(endpoint);
        endpoint.publish(servidor.createContext(ruta));
    }

    @AfterAll
    void cerrar() {
        try {
            for (var endpoint : endpoints) if (endpoint.isPublished()) endpoint.stop();
        } finally {
            try {
                if (servidor != null) servidor.stop(0);
            } finally {
                if (hilos != null) hilos.shutdownNow();
            }
        }
    }

    @Test
    void loginValidoEntregaSoloResumenEnSoapYReferenciaEnHttp() throws Exception {
        var cliente = new Cliente("/sesiones");
        var usuario = cliente.iniciar("org@test.uy", CLAVE_PRUEBA);
        assertEquals("org", usuario.getNickname());
        assertEquals("Organización", usuario.getNombre());
        assertEquals("ORGANIZADOR", usuario.getRol().value());
        String credencial = cliente.credencial();
        assertTrue(credencial != null && credencial.matches("[A-Za-z0-9_-]{43}"));
        assertEquals("org", registro.requerirActor(credencial).nickname());
        assertFalse(registro.requerirActor(credencial).esAdministradorLocal());
        comprobarSinSecretos(cliente.cuerpo(), credencial, CLAVE_PRUEBA);
    }

    @Test
    void erroresDeLoginControladosCarecenDeCredencial() throws Exception {
        var cliente = new Cliente("/sesiones");
        var invalida = assertThrows(SesionSoapException.class, () -> cliente.iniciar("org", "incorrecta-sintetica"));
        assertEquals("CREDENCIALES_INVALIDAS", invalida.getFaultInfo().getCodigo());
        assertNull(cliente.credencial());
        comprobarSinSecretos(cliente.cuerpo(), "incorrecta-sintetica");
        var malformada = assertThrows(SesionSoapException.class, () -> cliente.iniciar(null, CLAVE_PRUEBA));
        assertEquals("SOLICITUD_MALFORMADA", malformada.getFaultInfo().getCodigo());
        assertNull(cliente.credencial());
    }

    @Test
    void dosSesionesYLogoutCompartenRegistroEntrePublicadores() throws Exception {
        var primera = new Cliente("/sesiones");
        var segunda = new Cliente("/sesiones");
        primera.iniciar("org", CLAVE_PRUEBA);
        segunda.iniciar("org", CLAVE_PRUEBA);
        String a = primera.credencial(), b = segunda.credencial();
        assertFalse(a.equals(b), "Los logins deben crear referencias independientes");
        var cierre = new Cliente("/sesiones-otra");
        cierre.cerrar(List.of("Bearer " + a));
        assertThrows(IllegalArgumentException.class, () -> registro.requerirActor(a));
        assertEquals("org", registro.requerirActor(b).nickname());
        assertDoesNotThrow(() -> cierre.cerrar(List.of("Bearer " + a)));
        assertThrows(IllegalArgumentException.class, () -> registro.requerirActor(a));
        assertEquals("org", registro.requerirActor(b).nickname());
        comprobarSinSecretos(cierre.cuerpo(), a, b);
    }

    @Test
    void logoutRechazaEncabezadosAusentesDuplicadosYMalformados() throws Exception {
        var login = new Cliente("/sesiones");
        login.iniciar("org", CLAVE_PRUEBA);
        String credencial = login.credencial();
        for (var encabezados : List.of(List.<String>of(), List.of("Basic " + credencial),
                List.of("Bearer"), List.of("Bearer " + credencial, "Bearer " + credencial),
                List.of("Bearer " + credencial + ", Bearer " + credencial))) {
            var cliente = new Cliente("/sesiones");
            var error = assertThrows(SesionSoapException.class, () -> cliente.cerrar(encabezados));
            assertEquals("SOLICITUD_MALFORMADA", error.getFaultInfo().getCodigo());
            comprobarSinSecretos(cliente.cuerpo(), credencial);
            assertEquals("org", registro.requerirActor(credencial).nickname());
        }
    }

    @Test
    void noSeEmitenSesionesAdministrativas() {
        var cliente = new Cliente("/sesiones");
        var error = assertThrows(SesionSoapException.class, () -> cliente.iniciar("admin", CLAVE_PRUEBA));
        assertEquals("CREDENCIALES_INVALIDAS", error.getFaultInfo().getCodigo());
        assertNull(cliente.credencial());
    }

    @Test
    void loginsSimultaneosAislanIdentidadesYEncabezados() throws Exception {
        try (var tareas = Executors.newVirtualThreadPerTaskExecutor()) {
            var a = tareas.submit(() -> loginConcurrente("paralelo-a"));
            var b = tareas.submit(() -> loginConcurrente("paralelo-b"));
            String referenciaA = a.get(15, TimeUnit.SECONDS), referenciaB = b.get(15, TimeUnit.SECONDS);
            assertFalse(referenciaA.equals(referenciaB), "Las referencias concurrentes deben ser diferentes");
        }
    }

    @Test
    void dosCierresSimultaneosRespondenCorrectamenteYDejanLaSesionRevocada() throws Exception {
        var login = new Cliente("/sesiones");
        login.iniciar("org", CLAVE_PRUEBA);
        String referencia = login.credencial();
        try (var tareas = Executors.newVirtualThreadPerTaskExecutor()) {
            var barrera = new CyclicBarrier(2);
            var a = tareas.submit(() -> cerrarSimultaneamente(referencia, barrera));
            var b = tareas.submit(() -> cerrarSimultaneamente(referencia, barrera));
            a.get(15, TimeUnit.SECONDS);
            b.get(15, TimeUnit.SECONDS);
        }
        assertThrows(IllegalArgumentException.class, () -> registro.requerirActor(referencia));
    }

    private Void cerrarSimultaneamente(String referencia, CyclicBarrier barrera) throws Exception {
        var cliente = new Cliente("/sesiones-otra");
        barrera.await(8, TimeUnit.SECONDS);
        cliente.cerrar(List.of("Bearer " + referencia));
        comprobarSinSecretos(cliente.cuerpo(), referencia);
        return null;
    }

    @Test
    void logoutDeReferenciaDesconocidaConFormatoValidoRespondeCorrectamente() throws Exception {
        String desconocida = Base64.getUrlEncoder().withoutPadding().encodeToString(new byte[32]);
        var cliente = new Cliente("/sesiones");
        cliente.cerrar(List.of("Bearer " + desconocida));
        comprobarSinSecretos(cliente.cuerpo(), desconocida);
    }

    private String loginConcurrente(String usuario) throws Exception {
        var cliente = new Cliente("/sesiones");
        assertEquals(usuario, cliente.iniciar(usuario, CLAVE_PRUEBA).getNickname());
        String referencia = cliente.credencial();
        assertEquals(usuario, registro.requerirActor(referencia).nickname());
        comprobarSinSecretos(cliente.cuerpo(), referencia, CLAVE_PRUEBA);
        return referencia;
    }

    private static void comprobarSinSecretos(String cuerpo, String... secretos) {
        for (String secreto : secretos) {
            assertFalse(cuerpo.contains(secreto), "El cuerpo SOAP no debe contener secretos");
        }
    }

    /** Un puerto nuevo y un capturador de respuesta por invocación, sin imprimir envelopes. */
    private final class Cliente {
        private final String ruta;
        private BindingProvider proveedor;
        private Captura captura;

        Cliente(String ruta) { this.ruta = ruta; }

        private Sesiones puerto(List<String> autorizacion) {
            var puerto = new SesionesService().getSesionesPort();
            proveedor = (BindingProvider) puerto;
            var contexto = proveedor.getRequestContext();
            contexto.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, base + ruta);
            contexto.put(JAXWSProperties.SSL_SOCKET_FACTORY, confianza);
            contexto.put(JAXWSProperties.CONNECT_TIMEOUT, 3000);
            contexto.put(JAXWSProperties.REQUEST_TIMEOUT, 12000);
            contexto.put(MessageContext.HTTP_REQUEST_HEADERS,
                    autorizacion.isEmpty() ? Map.of() : Map.of("Authorization", autorizacion));
            captura = new Captura();
            List<Handler> handlers = new ArrayList<>();
            handlers.add(captura);
            proveedor.getBinding().setHandlerChain(handlers);
            return puerto;
        }

        UsuarioSesionSoap iniciar(String id, String clave) throws Exception {
            return puerto(List.of()).iniciarSesion(id, clave);
        }

        void cerrar(List<String> autorizacion) throws Exception {
            puerto(autorizacion).cerrarSesion();
        }

        String credencial() {
            Object valor = proveedor.getResponseContext().get(MessageContext.HTTP_RESPONSE_HEADERS);
            if (!(valor instanceof Map<?, ?> mapa)) return null;
            for (var entrada : mapa.entrySet()) {
                if ("X-EventosUy-Session".equalsIgnoreCase(String.valueOf(entrada.getKey()))) {
                    var lista = (List<?>) entrada.getValue();
                    assertEquals(1, lista.size(), "Debe recibirse un único encabezado de sesión");
                    return (String) lista.getFirst();
                }
            }
            return null;
        }

        String cuerpo() { return captura.cuerpo; }
    }

    private static final class Captura implements SOAPHandler<SOAPMessageContext> {
        private String cuerpo = "";
        @Override public Set<QName> getHeaders() { return Set.of(); }
        @Override public void close(MessageContext contexto) { }
        @Override public boolean handleFault(SOAPMessageContext contexto) { return handleMessage(contexto); }

        @Override
        public boolean handleMessage(SOAPMessageContext contexto) {
            if (Boolean.FALSE.equals(contexto.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY))) {
                try (var bytes = new ByteArrayOutputStream()) {
                    contexto.getMessage().writeTo(bytes);
                    cuerpo = bytes.toString(StandardCharsets.UTF_8);
                } catch (Exception e) {
                    throw new IllegalStateException("No se pudo examinar la respuesta de prueba.");
                }
            }
            return true;
        }
    }
}
