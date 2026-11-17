package implementacion;

import clases.Evento;
import datatypes.*;
import excepciones.NoEncontradoException;
import interfaces.ISistema;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import persistencia.BaseDeDatos;
import soporte.PruebaConBaseSegura;
import soporte.ImagenesPrueba;
import java.nio.file.*;
import java.sql.DriverManager;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;

class VisitasEventoTest extends PruebaConBaseSegura {
    private final ISistema sistema = Fabrica.getInstance().getISistema();
    private final Actor admin = Actor.administradorLocal();
    private String crear() throws Exception {
        String nombre = "visita-" + UUID.randomUUID();
        String categoria = "cat-" + nombre;
        sistema.altaCategoria(categoria, null, admin);
        assertTrue(sistema.altaEvento(new DtEvento(nombre, "VI", "Original", null, List.of(categoria)), admin));
        return nombre;
    }
    private long visitas(String nombre) {
        try (var em = BaseDeDatos.getEntityManager()) { return em.find(Evento.class, nombre).getVisitas(); }
    }
    private void finalizado(String nombre) {
        try (var em = BaseDeDatos.getEntityManager()) {
            var tx = em.getTransaction(); tx.begin();
            em.createNativeQuery("UPDATE evento SET finalizado = TRUE WHERE nombre = :nombre").setParameter("nombre", nombre).executeUpdate();
            tx.commit();
        }
    }
    @Test void contadorEmpiezaEnCeroYVisitanteAnonimoIncrementa() throws Exception {
        String nombre = crear(); assertEquals(0, visitas(nombre));
        sistema.registrarVisitaEvento(nombre, null); assertEquals(1, visitas(nombre));
    }
    @Test void identidadAutenticadaNoEsNecesariaNiCambiaLaContabilidad() throws Exception {
        String nombre = crear();
        Actor actor = Actor.usuarioAutenticado(new DtOrganizador("org", "Org", "org@test.uy", "Desc", ""));
        sistema.registrarVisitaEvento(nombre, actor); assertEquals(1, visitas(nombre));
    }
    @Test void inexistenteNoCreaEventoNiCuentaOtro() throws Exception {
        String nombre = crear();
        assertThrows(NoEncontradoException.class, () -> sistema.registrarVisitaEvento("inexistente-" + UUID.randomUUID(), null));
        assertEquals(0, visitas(nombre));
    }
    @Test void nombreInvalidoSeRechazaSinCambios() throws Exception {
        String nombre = crear();
        for (String valor : Arrays.asList(null, "", "  ", "x".repeat(256), "x\0y"))
            assertThrows(IllegalArgumentException.class, () -> sistema.registrarVisitaEvento(valor, null));
        assertThrows(NoEncontradoException.class, () -> sistema.registrarVisitaEvento("' OR 1=1 --", null));
        assertEquals(0, visitas(nombre));
    }
    @Test void noPublicoNoCuentaAunqueElActorSeaAdministrador() throws Exception {
        String nombre = crear(); finalizado(nombre);
        assertThrows(NoEncontradoException.class, () -> sistema.registrarVisitaEvento(nombre, null));
        assertThrows(NoEncontradoException.class, () -> sistema.registrarVisitaEvento(nombre, admin));
        assertEquals(0, visitas(nombre));
        assertTrue(sistema.listarEventos().stream().anyMatch(e -> nombre.equals(e.getNombre())), "La consulta administrativa no se altera.");
    }
    @Test void consultasExistentesNoRegistranVisitasImplicitamente() throws Exception {
        String nombre = crear();
        String categoria = sistema.listarEventos().stream().filter(e -> nombre.equals(e.getNombre())).findFirst().orElseThrow().getCategorias().iterator().next();
        assertTrue(sistema.listarEventosPorCategoria(categoria).stream().anyMatch(e -> nombre.equals(e.getNombre())));
        assertEquals(nombre, sistema.consultarEdiciones(nombre, "", null).evento());
        sistema.obtenerImagenVisible(TipoEntidadImagen.EVENTO, nombre, null);
        assertEquals(0, visitas(nombre));
    }
    @Test void incrementosConcurrentesNoSePierdenNiAfectanOtroEvento() throws Exception {
        String nombre = crear(), otro = crear(); int cantidad = 120;
        try (var pool = Executors.newFixedThreadPool(12)) {
            var inicio = new CountDownLatch(1); List<Future<?>> tareas = new ArrayList<>();
            for (int i = 0; i < cantidad; i++) tareas.add(pool.submit(() -> {
                inicio.await(); Fabrica.getInstance().getISistema().registrarVisitaEvento(nombre, null); return null;
            }));
            inicio.countDown(); for (var t : tareas) t.get(30, TimeUnit.SECONDS);
        }
        assertEquals(cantidad, visitas(nombre)); assertEquals(0, visitas(otro));
    }
    @Test void unaEntidadObsoletaNoRestauraElContadorAlActualizarOtrosDatos() throws Exception {
        String nombre = crear();
        try (var em = BaseDeDatos.getEntityManager()) {
            Evento obsoleto = em.find(Evento.class, nombre); assertEquals(0, obsoleto.getVisitas());
            sistema.registrarVisitaEvento(nombre, null); sistema.registrarVisitaEvento(nombre, null);
            var tx = em.getTransaction(); tx.begin(); obsoleto.setDescripcion("Actualizado"); tx.commit();
        }
        assertEquals(2, visitas(nombre));
        assertEquals("Actualizado", sistema.listarEventos().stream().filter(e -> nombre.equals(e.getNombre())).findFirst().orElseThrow().getDescripcion());
    }
    @Test void carrerasConImagenConservanBytesYTodosLosIncrementos() throws Exception {
        String nombre = crear(); var imagen = new DtImagen("image/png", ImagenesPrueba.imagen("png"));
        try (var pool = Executors.newFixedThreadPool(6)) {
            List<Future<?>> tareas = new ArrayList<>();
            for (int i = 0; i < 40; i++) tareas.add(pool.submit(() -> sistema.registrarVisitaEvento(nombre, null)));
            tareas.add(pool.submit(() -> sistema.asociarImagen(TipoEntidadImagen.EVENTO, nombre, imagen, admin)));
            for (var t : tareas) t.get(30, TimeUnit.SECONDS);
        }
        assertEquals(40, visitas(nombre)); assertArrayEquals(imagen.getContenido(), sistema.obtenerImagen(TipoEntidadImagen.EVENTO, nombre).getContenido());
    }
    @Test void cambioDeVisibilidadConcurrenteSeCompruebaEnElMismoUpdate() throws Exception {
        String nombre = crear();
        try (var em = BaseDeDatos.getEntityManager(); var pool = Executors.newSingleThreadExecutor()) {
            var tx = em.getTransaction(); tx.begin();
            try {
                em.createNativeQuery("UPDATE evento SET finalizado = TRUE WHERE nombre = :nombre").setParameter("nombre", nombre).executeUpdate();
                var tarea = pool.submit(() -> sistema.registrarVisitaEvento(nombre, null));
                long limite = System.nanoTime() + TimeUnit.SECONDS.toNanos(8); boolean bloqueada = false;
                while (System.nanoTime() < limite) {
                    em.createNativeQuery("SELECT pg_stat_clear_snapshot()").getSingleResult();
                    Number n = (Number) em.createNativeQuery("SELECT count(*) FROM pg_stat_activity WHERE datname=current_database() AND wait_event_type='Lock' AND query ILIKE '%SET visitas = visitas + 1%'").getSingleResult();
                    if (n.intValue() > 0) { bloqueada = true; break; } Thread.sleep(20);
                }
                assertTrue(bloqueada, "El incremento debe esperar el estado de la fila.");
                tx.commit(); assertInstanceOf(NoEncontradoException.class, assertThrows(ExecutionException.class, () -> tarea.get(10, TimeUnit.SECONDS)).getCause());
            } finally { if (tx.isActive()) tx.rollback(); }
        }
        assertEquals(0, visitas(nombre));
    }
    @Test void conservaVisitasTrasCerrarYReabrirLaFactory() throws Exception {
        String nombre = crear(); sistema.registrarVisitaEvento(nombre, null);
        BaseDeDatos.cerrar(); soporte.ProteccionBasePruebas.verificar();
        BaseDeDatos.inicializar(BaseDeDatos.propiedadesJpa());
        assertEquals(1, visitas(nombre));
        Fabrica.getInstance().getISistema().registrarVisitaEvento(nombre, null); assertEquals(2, visitas(nombre));
    }
    @Test void dosProcesosCentralIndependientesUsanElMismoContador(@TempDir Path temporal) throws Exception {
        String nombre = crear(); List<Process> procesos = new ArrayList<>();
        try {
            for (int i = 0; i < 2; i++) {
                procesos.add(new ProcessBuilder(Path.of(System.getProperty("java.home"), "bin", "java.exe").toString(),
                        "-cp", System.getProperty("surefire.test.class.path", System.getProperty("java.class.path")),
                        "soporte.RegistrarVisitasProceso", nombre, "20", temporal.resolve("iniciar").toString(), temporal.resolve("listo-" + i).toString())
                        .redirectErrorStream(true).redirectOutput(temporal.resolve("central-" + i + ".log").toFile()).start());
            }
            long limite = System.nanoTime() + TimeUnit.SECONDS.toNanos(30);
            while (!java.nio.file.Files.exists(temporal.resolve("listo-0")) || !java.nio.file.Files.exists(temporal.resolve("listo-1"))) {
                assertTrue(procesos.stream().allMatch(Process::isAlive), "Los dos procesos deben inicializarse antes de incrementar.");
                assertTrue(System.nanoTime() < limite, "Los procesos no alcanzaron la barrera.");
                Thread.sleep(20);
            }
            Files.writeString(temporal.resolve("iniciar"), "comenzar");
            for (Process proceso : procesos) { assertTrue(proceso.waitFor(45, TimeUnit.SECONDS)); assertEquals(0, proceso.exitValue()); }
        } finally {
            for (Process proceso : procesos) if (proceso.isAlive()) { proceso.destroyForcibly(); proceso.waitFor(10, TimeUnit.SECONDS); }
        }
        assertEquals(40, visitas(nombre));
    }
    @Test void migracionRepetidaConservaEventosYContadores() throws Exception {
        String nombre = crear(); sistema.registrarVisitaEvento(nombre, null);
        String sql;
        try (var entrada = getClass().getResourceAsStream("/db/migration/V20261116_01__visitas_evento.sql")) { assertNotNull(entrada); sql = new String(entrada.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8); }
        var p = BaseDeDatos.propiedadesJpa();
        try (var c = DriverManager.getConnection(p.get("jakarta.persistence.jdbc.url"), p.get("jakarta.persistence.jdbc.user"), p.get("jakarta.persistence.jdbc.password")); var q = c.createStatement()) {
            q.execute(sql); q.execute(sql);
        }
        assertEquals(1, visitas(nombre));
    }
    @Test void falloDelUpdateRevierteSinModificarDatosDelEvento() throws Exception {
        String nombre = crear();
        try (var em = BaseDeDatos.getEntityManager()) {
            var tx = em.getTransaction(); tx.begin();
            em.createNativeQuery("UPDATE evento SET visitas = 9223372036854775807 WHERE nombre = :nombre").setParameter("nombre", nombre).executeUpdate(); tx.commit();
        }
        assertThrows(RuntimeException.class, () -> sistema.registrarVisitaEvento(nombre, null));
        assertEquals(Long.MAX_VALUE, visitas(nombre));
        assertEquals("Original", sistema.listarEventos().stream().filter(e -> nombre.equals(e.getNombre())).findFirst().orElseThrow().getDescripcion());
    }
}
