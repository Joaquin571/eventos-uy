package implementacion;

import clases.Evento;
import datatypes.*;
import excepciones.NoEncontradoException;
import org.junit.jupiter.api.Test;
import persistencia.BaseDeDatos;
import soporte.PruebaConBaseSegura;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

class VisitasFinalizacionConcurrenciaTest extends PruebaConBaseSegura {
    @Test void visitaYFinalizacionConservanExactamenteLosIncrementosConfirmados() throws Exception {
        var sistema = Fabrica.getInstance().getISistema();
        Actor admin = Actor.administradorLocal();
        String nombre = "carrera-visitas-finalizacion-" + UUID.randomUUID();
        String categoria = "categoria-" + nombre;
        sistema.altaCategoria(categoria, null, admin);
        assertTrue(sistema.altaEvento(new DtEvento(nombre, "CV", "Prueba de concurrencia", null,
                List.of(categoria)), admin));
        Actor organizador = Actor.usuarioAutenticado(new DtOrganizador(
                "organizador-prueba", "Organizador", "org@test.invalid", "Prueba", ""));

        int intentos = 60;
        try (var pool = Executors.newFixedThreadPool(12)) {
            var inicio = new CountDownLatch(1);
            List<Future<Boolean>> visitas = new java.util.ArrayList<>();
            for (int i = 0; i < intentos; i++) visitas.add(pool.submit(() -> {
                inicio.await();
                try {
                    sistema.registrarVisitaEvento(nombre, null);
                    return true;
                } catch (NoEncontradoException cerrado) {
                    return false;
                }
            }));
            Future<?> cierre = pool.submit(() -> {
                inicio.await();
                sistema.finalizarEvento(nombre, organizador);
                return null;
            });
            inicio.countDown();
            cierre.get(30, TimeUnit.SECONDS);
            long confirmadas = 0;
            for (Future<Boolean> visita : visitas)
                if (visita.get(30, TimeUnit.SECONDS)) confirmadas++;

            try (var em = BaseDeDatos.getEntityManager()) {
                Evento guardado = em.find(Evento.class, nombre);
                assertTrue(guardado.isFinalizado());
                assertEquals(confirmadas, guardado.getVisitas());
            }
            assertThrows(NoEncontradoException.class, () -> sistema.registrarVisitaEvento(nombre, null));
            var ranking = sistema.obtenerEventosMasVisitados(admin);
            if (ranking.stream().anyMatch(e -> e.getNombre().equals(nombre))) {
                var entrada = ranking.stream().filter(e -> e.getNombre().equals(nombre)).findFirst().orElseThrow();
                assertTrue(entrada.isFinalizado());
                assertEquals(confirmadas, entrada.getVisitas());
            }
        }
    }
}
