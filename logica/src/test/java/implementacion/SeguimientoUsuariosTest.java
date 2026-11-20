package implementacion;

import datatypes.*;
import excepciones.*;
import interfaces.ISistema;
import org.junit.jupiter.api.*;
import persistencia.BaseDeDatos;
import soporte.PruebaConBaseSegura;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;

class SeguimientoUsuariosTest extends PruebaConBaseSegura {
    private final ISistema sistema = Fabrica.getInstance().getISistema();
    private Actor origen, destino;

    private Actor crear(String prefijo) {
        String nick = prefijo + UUID.randomUUID();
        var datos = new DtAsistente(nick, "Nombre", nick + "@test.uy", "Apellido", LocalDate.of(2000, 1, 1), null);
        datos.setContrasenia("clave"); datos.setConfirmacionContrasenia("clave");
        assertTrue(sistema.altaAsistente(datos));
        return Actor.usuarioAutenticado(sistema.autenticar(nick, "clave"));
    }
    @BeforeEach void preparar() { origen = crear("seg"); destino = crear("dest"); }

    @Test void seguirConsultarYDejarDeSeguirSonIdempotentes() {
        sistema.seguirUsuario(destino.nickname(), origen);
        sistema.seguirUsuario(destino.nickname(), origen);
        var perfil = sistema.consultarRelaciones(destino.nickname(), origen);
        assertEquals(List.of(origen.nickname()), perfil.seguidores());
        assertTrue(perfil.siguiendo());
        assertEquals(List.of(destino.nickname()), sistema.consultarRelaciones(origen.nickname(), null).seguidos());
        assertFalse(sistema.consultarRelaciones(destino.nickname(), null).siguiendo());
        sistema.dejarDeSeguirUsuario(destino.nickname(), origen);
        sistema.dejarDeSeguirUsuario(destino.nickname(), origen);
        assertTrue(sistema.consultarRelaciones(destino.nickname(), origen).seguidores().isEmpty());
    }
    @Test void autorizacionYDestino() {
        for (Actor actor : new Actor[]{null, Actor.administradorLocal()}) {
            assertThrows(AccesoDenegadoException.class, () -> sistema.seguirUsuario(destino.nickname(), actor));
            assertThrows(AccesoDenegadoException.class, () -> sistema.dejarDeSeguirUsuario(destino.nickname(), actor));
        }
        assertThrows(IllegalArgumentException.class, () -> sistema.seguirUsuario(origen.nickname(), origen));
        assertThrows(NoEncontradoException.class, () -> sistema.seguirUsuario("inexistente", origen));
        assertThrows(NoEncontradoException.class, () -> sistema.consultarRelaciones(null, origen));
    }
    @Test void solicitudesConcurrentesNoDuplican() throws Exception {
        try (var pool = Executors.newFixedThreadPool(6)) {
            var inicio = new CyclicBarrier(6);
            List<Future<?>> futuros = new ArrayList<>();
            for (int i=0; i<6; i++) futuros.add(pool.submit(() -> {
                inicio.await(10, TimeUnit.SECONDS);
                sistema.seguirUsuario(destino.nickname(), origen);
                return null;
            }));
            for (var f : futuros) f.get(30, TimeUnit.SECONDS);
        }
        assertEquals(List.of(origen.nickname()), sistema.consultarRelaciones(destino.nickname(), origen).seguidores());
    }
    @Test void persisteAlReiniciarContexto() {
        sistema.seguirUsuario(destino.nickname(), origen);
        BaseDeDatos.cerrar();
        BaseDeDatos.inicializar();
        assertTrue(sistema.consultarRelaciones(destino.nickname(), origen).siguiendo());
        assertEquals(List.of(destino.nickname()), sistema.consultarRelaciones(origen.nickname(), origen).seguidos());
        sistema.dejarDeSeguirUsuario(destino.nickname(), origen);
        BaseDeDatos.cerrar();
        BaseDeDatos.inicializar();
        assertFalse(sistema.consultarRelaciones(destino.nickname(), origen).siguiendo());
        assertTrue(sistema.consultarRelaciones(origen.nickname(), origen).seguidos().isEmpty());
    }

    @Test void restriccionesPersistentesRechazanSeguimientoPropioYDuplicadoDirecto() {
        sistema.seguirUsuario(destino.nickname(), origen);
        for (String seguido : List.of(destino.nickname(), origen.nickname())) {
            var em = BaseDeDatos.getEntityManager();
            var tx = em.getTransaction();
            try {
                tx.begin();
                assertThrows(RuntimeException.class, () -> em.createNativeQuery(
                        "INSERT INTO seguimiento_usuario (seguidor, seguido) VALUES (:origen, :destino)")
                        .setParameter("origen", origen.nickname()).setParameter("destino", seguido).executeUpdate());
            } finally {
                if (tx.isActive()) tx.rollback();
                em.close();
            }
        }
        assertEquals(List.of(origen.nickname()), sistema.consultarRelaciones(destino.nickname(), null).seguidores());
    }

    @Test void identidadInexistenteNoPuedeModificarNiRevelarExistenciaDelDestino() {
        var inexistente = Actor.usuarioAutenticado(new DtAsistente("no-existe-" + UUID.randomUUID(), "", "", "", null, null));
        for (String nick : List.of(destino.nickname(), "no-existe")) {
            assertThrows(AccesoDenegadoException.class, () -> sistema.seguirUsuario(nick, inexistente));
            assertThrows(AccesoDenegadoException.class, () -> sistema.dejarDeSeguirUsuario(nick, inexistente));
        }
        assertTrue(sistema.consultarRelaciones(destino.nickname(), null).seguidores().isEmpty());
    }
}
