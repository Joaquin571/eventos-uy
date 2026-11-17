package web.soap;

import central.seguridad.SesionesRemotas;
import central.soap.*;
import clases.Evento;
import com.sun.net.httpserver.*;
import datatypes.Actor;
import implementacion.Fabrica;
import interfaces.ISistema;
import jakarta.xml.ws.Endpoint;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import persistencia.BaseDeDatos;
import web.remoto.*;
import javax.net.ssl.*;
import java.net.*;
import java.nio.file.Path;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;

/** Dos publicadores HTTPS reales y PostgreSQL; cliente regenerado desde WSDL. */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class VisitasPortalHttpsTest {
    private final List<HttpsServer> servidores = new ArrayList<>();
    private final List<Endpoint> endpoints = new ArrayList<>();
    private final List<URI> portales = new ArrayList<>();
    private ExecutorService hilos;
    private SSLSocketFactory confianza;
    private URI sesiones;
    private ISistema sistema;
    @BeforeAll void iniciar(@TempDir Path temporal) throws Exception {
        PortalPersistenciaHttpsTest.verificarBase();
        BaseDeDatos.inicializar(); sistema = Fabrica.getInstance().getISistema();
        Path almacen = temporal.resolve("visitas.p12"); char[] clave = UUID.randomUUID().toString().toCharArray();
        SSLContext tls;
        try { SoporteTls.generar(almacen, clave); confianza = SoporteTls.confianza(almacen, clave); tls = SoporteTls.contexto(almacen, clave); }
        finally { Arrays.fill(clave, '\0'); }
        hilos = Executors.newVirtualThreadPerTaskExecutor();
        for (int i = 0; i < 2; i++) {
            ISistema controlador = Fabrica.getInstance().getISistema();
            var registro = new SesionesRemotas(controlador, new java.security.SecureRandom(), Clock.systemUTC(), Duration.ofMinutes(5));
            var servidor = HttpsServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
            servidores.add(servidor); servidor.setHttpsConfigurator(new HttpsConfigurator(tls)); servidor.setExecutor(hilos);
            var portal = Endpoint.create(new PortalPublicador(controlador, registro)); endpoints.add(portal); portal.publish(servidor.createContext("/portal"));
            var login = Endpoint.create(new SesionesPublicador(registro)); endpoints.add(login); login.publish(servidor.createContext("/sesiones"));
            servidor.start(); String base = "https://127.0.0.1:" + servidor.getAddress().getPort();
            portales.add(URI.create(base + "/portal")); if (i == 0) sesiones = URI.create(base + "/sesiones");
        }
    }
    @AfterAll void cerrar() {
        try { endpoints.forEach(Endpoint::stop); } finally { servidores.forEach(s -> s.stop(0)); if (hilos != null) hilos.shutdownNow(); BaseDeDatos.cerrar(); }
    }
    private ClienteCentral cliente(int indice, String token) { return new ClienteCentral(portales.get(indice), token, confianza); }
    private String crear() throws Exception {
        String nombre = "visitaSoap-" + UUID.randomUUID();
        sistema.altaCategoria(nombre, null, Actor.administradorLocal());
        assertTrue(sistema.altaEvento(new datatypes.DtEvento(nombre, "VS", "Detalle público", null, List.of(nombre)), Actor.administradorLocal()));
        return nombre;
    }
    private long visitas(String nombre) { try (var em = BaseDeDatos.getEntityManager()) { return em.find(Evento.class, nombre).getVisitas(); } }
    @Test void anonimoIncrementaPorHttpsSinQueLasConsultasCuenten() throws Exception {
        String nombre = crear();
        assertTrue(cliente(0, null).listarEventos().stream().anyMatch(e -> nombre.equals(e.getNombre())));
        assertEquals(nombre, cliente(0, null).consultarEdiciones(nombre, "").getEvento()); assertEquals(0, visitas(nombre));
        cliente(0, null).registrarVisitaEvento(nombre); assertEquals(1, visitas(nombre));
    }
    @Test void sesionP03ValidaCuentaYRevocadaNoSeDegradaAAnonimo() throws Exception {
        String nombre = crear(), nick = "visitaOrg-" + UUID.randomUUID();
        var u = new datatypes.DtOrganizador(nick, "Org", nick + "@test.uy", "Desc", "");
        u.setContrasenia("clave-prueba"); u.setConfirmacionContrasenia("clave-prueba"); assertTrue(sistema.altaOrganizador(u));
        var login = new ClienteSesionesCentral(sesiones, confianza); String token = login.iniciar(nick, "clave-prueba").credencial();
        cliente(0, token).registrarVisitaEvento(nombre); assertEquals(1, visitas(nombre)); login.cerrar(token);
        var error = assertThrows(ErrorCentral.class, () -> cliente(0, token).registrarVisitaEvento(nombre));
        assertEquals("SESION_INVALIDA", error.getCodigo()); assertEquals(1, visitas(nombre));
    }
    @Test void credencialDesconocidaOMalformadaNoPermiteIncrementar() throws Exception {
        String nombre = crear();
        for (String token : List.of("a".repeat(43), "malformado")) {
            var error = assertThrows(ErrorCentral.class, () -> cliente(0, token).registrarVisitaEvento(nombre));
            assertEquals("SESION_INVALIDA", error.getCodigo());
        }
        assertEquals(0, visitas(nombre));
    }
    @Test void inexistenteOFinalizadoNoSeCuentanYDevuelvenElMismoFault() throws Exception {
        String nombre = crear();
        try (var em = BaseDeDatos.getEntityManager()) { var tx = em.getTransaction(); tx.begin();
            em.createNativeQuery("UPDATE evento SET finalizado=TRUE WHERE nombre=:nombre").setParameter("nombre", nombre).executeUpdate(); tx.commit(); }
        for (String n : List.of(nombre, "ausente-" + UUID.randomUUID())) {
            var error = assertThrows(ErrorCentral.class, () -> cliente(0, null).registrarVisitaEvento(n)); assertEquals("NO_ENCONTRADO", error.getCodigo());
        }
        assertEquals(0, visitas(nombre));
    }
    @Test void entradasInvalidasSonFaultDeNegocioSinCambiarElContador() throws Exception {
        String nombre = crear();
        for (String valor : Arrays.asList(null, "", " ", "x".repeat(256)))
            assertThrows(IllegalArgumentException.class, () -> cliente(0, null).registrarVisitaEvento(valor));
        assertEquals(0, visitas(nombre));
    }
    @Test void dosPublicadoresConcurrentesNoPierdenVisitas() throws Exception {
        String nombre = crear();
        try (var pool = Executors.newFixedThreadPool(8)) {
            List<Future<?>> tareas = new ArrayList<>();
            for (int i = 0; i < 40; i++) { final int indice = i % 2; tareas.add(pool.submit(() -> cliente(indice, null).registrarVisitaEvento(nombre))); }
            for (var t : tareas) t.get(40, TimeUnit.SECONDS);
        }
        assertEquals(40, visitas(nombre));
    }
    @Test void consultarWsdlPorGetNoProduceEfectos() throws Exception {
        String nombre = crear();
        var conexion = (HttpsURLConnection) URI.create(portales.getFirst() + "?wsdl").toURL().openConnection();
        conexion.setSSLSocketFactory(confianza); conexion.setConnectTimeout(4000); conexion.setReadTimeout(12000);
        try (var entrada = conexion.getInputStream()) {
            assertEquals(200, conexion.getResponseCode());
            assertTrue(new String(entrada.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8).contains("registrarVisitaEvento"));
        } finally { conexion.disconnect(); }
        assertEquals(0, visitas(nombre));
    }
}
