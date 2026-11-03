package implementacion;

import clases.Edicion;
import datatypes.*;
import interfaces.ISistema;
import jakarta.persistence.LockModeType;
import org.junit.jupiter.api.Test;
import persistencia.BaseDeDatos;
import soporte.PruebaConBaseSegura;

import java.sql.DriverManager;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

/** Transacciones y conexiones reales; no usa sleeps para suponer quién ganó. */
class EstadoEdicionConcurrenciaTest extends PruebaConBaseSegura {
    private final ISistema sistema = Fabrica.getInstance().getISistema();
    private final String sufijo = Long.toString(System.nanoTime());
    private final String evento = "Evento concurrente " + sufijo;
    private final String edicion = "Edición concurrente " + sufijo;

    private void preparar() throws Exception {
        String org = "orgConcurrente" + sufijo, categoria = "Categoría concurrente " + sufijo;
        DtOrganizador usuario = new DtOrganizador(org, "Organización", org + "@test.uy", "Descripción", "");
        usuario.setContrasenia("clave"); usuario.setConfirmacionContrasenia("clave");
        assertTrue(sistema.altaOrganizador(usuario));
        sistema.altaCategoria(categoria, null);
        assertTrue(sistema.altaEvento(new DtEvento(evento, "EV", "Descripción", null, List.of(categoria))));
        assertTrue(sistema.altaEdicion(new DtEdicion(edicion, "ED", LocalDate.now(), LocalDate.now(),
                null, "Ciudad", "País", org), evento));
    }

    @Test
    void rechazoEsperaElCommitDeAceptacionYNoLoSobrescribe() throws Exception {
        comprobarEspera(true);
    }

    @Test
    void aceptacionEsperaElCommitDeRechazoYNoLoSobrescribe() throws Exception {
        comprobarEspera(false);
    }

    private void comprobarEspera(boolean primeraAcepta) throws Exception {
        preparar();
        ExecutorService ejecutor = Executors.newSingleThreadExecutor();
        try (var em = BaseDeDatos.getEntityManager()) {
            var tx = em.getTransaction();
            try {
                tx.begin();
                Edicion bloqueada = em.find(Edicion.class, edicion, LockModeType.PESSIMISTIC_WRITE);
                int pid = ((Number) em.createNativeQuery("SELECT pg_backend_pid()").getSingleResult()).intValue();
                if (primeraAcepta) bloqueada.aceptar(); else bloqueada.rechazar();
                Future<?> segunda = ejecutor.submit(() ->
                        Fabrica.getInstance().getISistema().aceptarRechazarEdicion(edicion, !primeraAcepta));

                esperarBloqueoReal(pid, segunda);
                assertFalse(segunda.isDone(), "La segunda decisión debe esperar al primer commit.");
                tx.commit();

                ExecutionException error = assertThrows(ExecutionException.class,
                        () -> segunda.get(10, TimeUnit.SECONDS));
                assertInstanceOf(IllegalStateException.class, error.getCause());
                assertEquals(primeraAcepta ? EstadoEdicion.ACEPTADA : EstadoEdicion.RECHAZADA, estadoPersistido());
            } finally {
                if (tx.isActive()) tx.rollback();
            }
        } finally {
            ejecutor.shutdownNow();
            assertTrue(ejecutor.awaitTermination(15, TimeUnit.SECONDS), "No deben quedar transacciones de prueba activas.");
        }
    }

    private void esperarBloqueoReal(int pidPrimera, Future<?> segunda) throws Exception {
        var p = BaseDeDatos.propiedadesJpa();
        try (var c = DriverManager.getConnection(p.get("jakarta.persistence.jdbc.url"),
                p.get("jakarta.persistence.jdbc.user"), p.get("jakarta.persistence.jdbc.password"))) {
            c.setReadOnly(true);
            try (var consulta = c.prepareStatement("""
                    SELECT EXISTS (
                        SELECT 1 FROM pg_stat_activity a
                        WHERE a.datname = current_database()
                          AND ? = ANY(pg_blocking_pids(a.pid))
                    )
                    """)) {
                consulta.setQueryTimeout(3); consulta.setInt(1, pidPrimera);
                long limite = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
                while (System.nanoTime() < limite) {
                    try (var r = consulta.executeQuery()) {
                        assertTrue(r.next());
                        if (r.getBoolean(1)) return;
                    }
                    assertFalse(segunda.isDone(), "La operación terminó sin esperar al bloqueo de la edición.");
                    Thread.sleep(25); // Solo intervalo de consulta; el criterio es pg_blocking_pids.
                }
                fail("PostgreSQL no mostró la segunda transacción esperando a la primera.");
            }
        }
    }

    @Test
    void dosCasosDeUsoSimultaneosConfirmanUnaSolaDecision() throws Exception {
        preparar();
        ExecutorService ejecutor = Executors.newFixedThreadPool(2);
        CyclicBarrier inicio = new CyclicBarrier(2);
        try {
            Future<Boolean> aceptar = ejecutor.submit(() -> decidir(inicio, true));
            Future<Boolean> rechazar = ejecutor.submit(() -> decidir(inicio, false));
            boolean acepto = aceptar.get(15, TimeUnit.SECONDS);
            boolean rechazo = rechazar.get(15, TimeUnit.SECONDS);
            assertNotEquals(acepto, rechazo, "Una sola operación debe confirmar; la otra debe rechazar la transición.");
            assertEquals(acepto ? EstadoEdicion.ACEPTADA : EstadoEdicion.RECHAZADA, estadoPersistido());
            assertThrows(IllegalStateException.class, () -> sistema.aceptarRechazarEdicion(edicion, !acepto));
            assertEquals(acepto ? EstadoEdicion.ACEPTADA : EstadoEdicion.RECHAZADA, estadoPersistido());
        } finally {
            ejecutor.shutdownNow();
            assertTrue(ejecutor.awaitTermination(15, TimeUnit.SECONDS));
        }
    }

    private boolean decidir(CyclicBarrier inicio, boolean aceptar) throws Exception {
        inicio.await(10, TimeUnit.SECONDS);
        try {
            Fabrica.getInstance().getISistema().aceptarRechazarEdicion(edicion, aceptar);
            return true;
        } catch (IllegalStateException esperada) {
            assertEquals("Solo se puede aceptar o rechazar una edición en estado Ingresada.", esperada.getMessage());
            return false;
        }
    }

    private EstadoEdicion estadoPersistido() {
        return sistema.obtenerEdicionesEvento(evento).stream()
                .filter(e -> edicion.equals(e.getIdNombre())).findFirst().orElseThrow().getEstado();
    }
}
