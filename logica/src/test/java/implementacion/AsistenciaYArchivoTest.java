package implementacion;

import datatypes.Actor;
import datatypes.DtAsistente;
import datatypes.DtConstanciaAsistencia;
import datatypes.DtEdicion;
import datatypes.DtEvento;
import datatypes.DtOrganizador;
import datatypes.DtRegistro;
import datatypes.DtTipoRegistro;
import datatypes.EstadoEdicion;
import excepciones.AccesoDenegadoException;
import interfaces.ISistema;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.*;

class AsistenciaYArchivoTest extends soporte.PruebaConBaseSegura {

    private final ISistema sistema = Fabrica.getInstance().getISistema();
    private final String sufijo = String.valueOf(System.nanoTime());
    private final LocalDate hoy = LocalDate.now();
    private int contador = 0;

    private DtOrganizador crearOrganizador(String prefijo) {
        String nick = prefijo + "_" + (++contador) + "_" + sufijo;
        DtOrganizador org = new DtOrganizador(
                nick, "Organizador " + contador, nick + "@mail.com",
                "Descripción", "https://ejemplo.com"
        );
        org.setContrasenia("clave1");
        org.setConfirmacionContrasenia("clave1");
        assertTrue(sistema.altaOrganizador(org));
        return org;
    }

    private DtAsistente crearAsistente(String prefijo) {
        String nick = prefijo + "_" + (++contador) + "_" + sufijo;
        DtAsistente asis = new DtAsistente(
                nick, "Ana", nick + "@mail.com",
                "Pérez", LocalDate.of(2000, 1, 1), null
        );
        asis.setContrasenia("clave1");
        asis.setConfirmacionContrasenia("clave1");
        assertTrue(sistema.altaAsistente(asis));
        return asis;
    }

    private String crearEvento(String nickOrganizador) throws Exception {
        String cat = "cat_" + (++contador) + "_" + sufijo;
        sistema.altaCategoria(cat, null, Actor.administradorLocal());
        String evento = "ev_" + (++contador) + "_" + sufijo;
        assertTrue(sistema.altaEvento(
                new DtEvento(evento, "EV" + contador, "Evento prueba", hoy, List.of(cat)),
                Actor.administradorLocal()
        ));
        return evento;
    }

    private String crearEdicion(String evento, String nickOrganizador, LocalDate inicio, LocalDate fin, boolean aceptar) {
        String edicion = "ed_" + (++contador) + "_" + sufijo;
        assertTrue(sistema.altaEdicion(
                new DtEdicion(edicion, "ED" + contador, inicio, fin, hoy, "Montevideo", "Uruguay", nickOrganizador),
                evento,
                Actor.administradorLocal()
        ));
        if (aceptar) {
            sistema.aceptarRechazarEdicion(edicion, true, Actor.administradorLocal());
        }
        return edicion;
    }

    @Test
    void confirmarAsistenciaYConstancia() throws Exception {
        DtOrganizador org = crearOrganizador("org");
        DtAsistente asis = crearAsistente("asis");
        DtAsistente otroAsis = crearAsistente("otroAsis");
        Actor actorAsis = Actor.usuarioAutenticado(asis);
        Actor actorOtro = Actor.usuarioAutenticado(otroAsis);

        String evento = crearEvento(org.getNickname());
        String edicion = crearEdicion(evento, org.getNickname(), hoy.minusDays(2), hoy.plusDays(2), true);
        String futura = crearEdicion(evento, org.getNickname(), hoy.plusDays(2), hoy.plusDays(5), true);

        assertTrue(sistema.altaTipoRegistro(
                new DtTipoRegistro("General", "Acceso general", 500, 20),
                edicion,
                Actor.administradorLocal()
        ));
        assertTrue(sistema.altaTipoRegistro(
                new DtTipoRegistro("General", "Acceso general", 500, 20),
                futura,
                Actor.administradorLocal()
        ));
        assertTrue(sistema.registroAEdicion(
                asis.getNickname(), edicion, "General", new DtRegistro(hoy, 500), null, actorAsis
        ));
        assertTrue(sistema.registroAEdicion(
                asis.getNickname(), futura, "General", new DtRegistro(hoy, 500), null, actorAsis
        ));

        // No permite confirmar antes de la fecha de inicio ni si no está registrado
        assertThrows(IllegalArgumentException.class, () -> sistema.confirmarAsistencia(futura, actorAsis));
        assertThrows(IllegalArgumentException.class, () -> sistema.confirmarAsistencia(edicion, actorOtro));

        // Antes de confirmar, no se puede emitir constancia
        assertThrows(IllegalStateException.class, () -> sistema.obtenerDatosConstancia(edicion, actorAsis));

        // Confirmar asistencia
        sistema.confirmarAsistencia(edicion, actorAsis);

        DtRegistro regActualizado = sistema.obtenerRegistrosAsistente(asis.getNickname()).stream()
                .filter(r -> edicion.equals(r.getNombreEdicion()))
                .findFirst()
                .orElseThrow();
        assertTrue(regActualizado.isAsistenciaConfirmada());

        // No permite confirmar dos veces ni descargar constancia ajena
        assertThrows(IllegalStateException.class, () -> sistema.confirmarAsistencia(edicion, actorAsis));
        assertThrows(AccesoDenegadoException.class, () -> sistema.obtenerDatosConstancia(edicion, actorOtro));

        // Emitir datos de constancia
        DtConstanciaAsistencia constancia = sistema.obtenerDatosConstancia(edicion, actorAsis);
        assertEquals("Ana", constancia.getNombreAsistente());
        assertEquals("Pérez", constancia.getApellidoAsistente());
        assertEquals(evento, constancia.getNombreEvento());
        assertEquals(edicion, constancia.getNombreEdicion());
        assertEquals("Montevideo", constancia.getCiudad());
        assertEquals(hoy, constancia.getFechaEmision());
    }

    @Test
    void confirmarAsistenciaConcurrenteEsAtomica() throws Exception {
        DtOrganizador org = crearOrganizador("orgConc");
        DtAsistente asis = crearAsistente("asisConc");
        Actor actorAsis = Actor.usuarioAutenticado(asis);

        String evento = crearEvento(org.getNickname());
        String edicion = crearEdicion(evento, org.getNickname(), hoy.minusDays(1), hoy.plusDays(1), true);
        assertTrue(sistema.altaTipoRegistro(
                new DtTipoRegistro("General", "Acceso", 100, 10), edicion, Actor.administradorLocal()));
        assertTrue(sistema.registroAEdicion(
                asis.getNickname(), edicion, "General", new DtRegistro(hoy, 100), null, actorAsis));

        CountDownLatch listo = new CountDownLatch(2);
        CountDownLatch iniciar = new CountDownLatch(1);
        List<Throwable> errores = Collections.synchronizedList(new ArrayList<>());

        try (ExecutorService pool = Executors.newFixedThreadPool(2)) {
            List<Future<?>> tareas = new ArrayList<>();
            for (int i = 0; i < 2; i++) {
                tareas.add(pool.submit(() -> {
                    listo.countDown();
                    try {
                        iniciar.await();
                        sistema.confirmarAsistencia(edicion, actorAsis);
                    } catch (Throwable t) {
                        errores.add(t);
                    }
                }));
            }
            listo.await();
            iniciar.countDown();
            for (Future<?> f : tareas) {
                f.get();
            }
        }

        assertEquals(1, errores.size(), "Exactamente uno de los intentos concurrentes debe fallar");
        assertTrue(sistema.obtenerRegistrosAsistente(asis.getNickname()).stream()
                .filter(r -> edicion.equals(r.getNombreEdicion()))
                .findFirst()
                .orElseThrow()
                .isAsistenciaConfirmada());
    }

    @Test
    void archivarEdicionFinalizadaConservaRegistrosYAplicaVisibilidad() throws Exception {
        DtOrganizador duenio = crearOrganizador("orgDuenio");
        DtOrganizador otro = crearOrganizador("orgOtro");
        DtAsistente asis = crearAsistente("asis");

        Actor actorDuenio = Actor.usuarioAutenticado(duenio);
        Actor actorOtro = Actor.usuarioAutenticado(otro);
        Actor actorAsis = Actor.usuarioAutenticado(asis);

        String evento = crearEvento(duenio.getNickname());

        String finalizada = crearEdicion(evento, duenio.getNickname(), hoy.minusDays(5), hoy.minusDays(1), true);
        String vigente = crearEdicion(evento, duenio.getNickname(), hoy.minusDays(1), hoy.plusDays(3), true);
        String ingresada = crearEdicion(evento, duenio.getNickname(), hoy.minusDays(5), hoy.minusDays(1), false);

        // Registrar y confirmar asistencia antes de archivar
        assertTrue(sistema.altaTipoRegistro(
                new DtTipoRegistro("General", "Acceso", 200, 10), finalizada, Actor.administradorLocal()));
        assertTrue(sistema.registroAEdicion(
                asis.getNickname(), finalizada, "General", new DtRegistro(hoy.minusDays(2), 200), null, actorAsis));
        sistema.confirmarAsistencia(finalizada, actorAsis);

        // No puede archivar un asistente ni otro organizador
        assertThrows(AccesoDenegadoException.class, () -> sistema.archivarEdicion(finalizada, actorAsis));
        assertThrows(AccesoDenegadoException.class, () -> sistema.archivarEdicion(finalizada, actorOtro));

        // No puede archivar una edición que no finalizó o que no fue aceptada
        assertThrows(IllegalArgumentException.class, () -> sistema.archivarEdicion(vigente, actorDuenio));
        assertThrows(IllegalStateException.class, () -> sistema.archivarEdicion(ingresada, actorDuenio));

        // Archivar edición finalizada y aceptada
        sistema.archivarEdicion(finalizada, actorDuenio);

        // Visibilidad pública excluye ARCHIVADA; consulta del organizador sí la incluye
        assertTrue(sistema.obtenerEdicionesVisibles(evento, null).stream()
                .noneMatch(e -> finalizada.equals(e.getIdNombre())));
        assertTrue(sistema.obtenerEdicionesEvento(evento, EstadoEdicion.ACEPTADA).stream()
                .noneMatch(e -> finalizada.equals(e.getIdNombre())));

        DtEdicion dtFinalizada = sistema.obtenerEdicionesOrganizador(duenio.getNickname()).stream()
                .filter(e -> finalizada.equals(e.getIdNombre()))
                .findFirst()
                .orElseThrow();
        assertEquals(EstadoEdicion.ARCHIVADA, dtFinalizada.getEstado());
        assertEquals(hoy, dtFinalizada.getFechaArchivo());

        // Conserva los registros y asistencias existentes
        DtRegistro regConservado = sistema.obtenerRegistrosEdicion(finalizada).stream()
                .filter(r -> asis.getNickname().equals(r.getNicknameAsistente()))
                .findFirst()
                .orElseThrow();
        assertTrue(regConservado.isAsistenciaConfirmada());
    }
}