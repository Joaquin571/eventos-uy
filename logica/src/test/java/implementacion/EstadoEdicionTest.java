package implementacion;

import datatypes.DtAsistente;
import datatypes.DtEdicion;
import datatypes.DtEvento;
import datatypes.DtOrganizador;
import datatypes.DtRegistro;
import datatypes.DtTipoRegistro;
import datatypes.EstadoEdicion;
import interfaces.ISistema;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Estados de la edición (sección 4 y 7.5): nace Ingresada, solo pasa a Aceptada o a
 * Rechazada y no vuelve. Necesitan la base de pruebas (eventosuy_test).
 */
class EstadoEdicionTest {

    private final ISistema sistema = Fabrica.getInstance().getISistema();
    private final String sufijo = String.valueOf(System.nanoTime());
    private final LocalDate hoy = LocalDate.now();

    private String organizador;
    private String evento;
    private int contador = 0;

    // ------------------------------------------------------------ preparación

    private void prepararEvento() throws Exception {
        if (evento != null) {
            return;
        }

        organizador = "org" + sufijo;

        DtOrganizador dtOrganizador = new DtOrganizador(organizador, "Organizadora",
                organizador + "@mail.com", "Descripción general", "https://ejemplo.com");
        dtOrganizador.setContrasenia("clave1");
        dtOrganizador.setConfirmacionContrasenia("clave1");
        assertTrue(sistema.altaOrganizador(dtOrganizador));

        String categoria = "cat" + sufijo;
        sistema.altaCategoria(categoria, null);

        evento = "evento" + sufijo;
        assertTrue(sistema.altaEvento(new DtEvento(evento, "EV" + sufijo,
                "Evento de prueba", hoy, List.of(categoria))));
    }

    /** Crea una edición nueva, que queda Ingresada. */
    private String crearEdicion() throws Exception {
        prepararEvento();

        String edicion = "edicion" + (++contador) + "_" + sufijo;

        assertTrue(sistema.altaEdicion(new DtEdicion(edicion, "ED" + contador,
                hoy.plusDays(10), hoy.plusDays(12), hoy, "Montevideo", "Uruguay", organizador), evento));

        return edicion;
    }

    private List<String> nombres(EstadoEdicion estado) {
        List<String> nombres = new ArrayList<>();

        Collection<DtEdicion> ediciones = sistema.obtenerEdicionesEvento(evento, estado);
        for (DtEdicion edicion : ediciones) {
            nombres.add(edicion.getIdNombre());
        }
        return nombres;
    }

    // ------------------------------------------------------------ transiciones

    @Test
    void laEdicionNuevaQuedaIngresada() throws Exception {
        String edicion = crearEdicion();

        assertTrue(nombres(EstadoEdicion.INGRESADA).contains(edicion));
        assertFalse(nombres(EstadoEdicion.ACEPTADA).contains(edicion));
        assertFalse(nombres(EstadoEdicion.RECHAZADA).contains(edicion));
    }

    @Test
    void aceptarLaPasaDeIngresadaAAceptada() throws Exception {
        String edicion = crearEdicion();

        sistema.aceptarRechazarEdicion(edicion, true);

        assertTrue(nombres(EstadoEdicion.ACEPTADA).contains(edicion));
        assertFalse(nombres(EstadoEdicion.INGRESADA).contains(edicion));
    }

    @Test
    void rechazarLaPasaDeIngresadaARechazada() throws Exception {
        String edicion = crearEdicion();

        sistema.aceptarRechazarEdicion(edicion, false);

        assertTrue(nombres(EstadoEdicion.RECHAZADA).contains(edicion));
        assertFalse(nombres(EstadoEdicion.INGRESADA).contains(edicion));
    }

    @Test
    void unaEdicionAceptadaNoPuedeCambiarDeEstado() throws Exception {
        String edicion = crearEdicion();
        sistema.aceptarRechazarEdicion(edicion, true);

        assertThrows(IllegalStateException.class, () -> sistema.aceptarRechazarEdicion(edicion, true));
        assertThrows(IllegalStateException.class, () -> sistema.aceptarRechazarEdicion(edicion, false));

        assertTrue(nombres(EstadoEdicion.ACEPTADA).contains(edicion));
    }

    @Test
    void unaEdicionRechazadaNoPuedeCambiarDeEstado() throws Exception {
        String edicion = crearEdicion();
        sistema.aceptarRechazarEdicion(edicion, false);

        assertThrows(IllegalStateException.class, () -> sistema.aceptarRechazarEdicion(edicion, true));
        assertThrows(IllegalStateException.class, () -> sistema.aceptarRechazarEdicion(edicion, false));

        assertTrue(nombres(EstadoEdicion.RECHAZADA).contains(edicion));
    }

    @Test
    void aceptarUnaEdicionInexistenteLanzaError() {
        assertThrows(IllegalArgumentException.class,
                () -> sistema.aceptarRechazarEdicion("noexiste" + sufijo, true));
    }

    @Test
    void aceptarSinIndicarLaEdicionLanzaError() {
        assertThrows(IllegalArgumentException.class, () -> sistema.aceptarRechazarEdicion(null, true));
        assertThrows(IllegalArgumentException.class, () -> sistema.aceptarRechazarEdicion("  ", true));
    }

    // ----------------------------------------------------------- listado público

    @Test
    void elListadoPorEstadoSoloDevuelveLasEdicionesDeEseEstado() throws Exception {
        String ingresada = crearEdicion();
        String aceptada = crearEdicion();
        String rechazada = crearEdicion();

        sistema.aceptarRechazarEdicion(aceptada, true);
        sistema.aceptarRechazarEdicion(rechazada, false);

        assertEquals(List.of(ingresada), nombres(EstadoEdicion.INGRESADA));
        assertEquals(List.of(aceptada), nombres(EstadoEdicion.ACEPTADA));
        assertEquals(List.of(rechazada), nombres(EstadoEdicion.RECHAZADA));
    }

    @Test
    void soloSePuedeRegistrarEnUnaEdicionAceptada() throws Exception {
        String edicion = crearEdicion();

        assertTrue(sistema.altaTipoRegistro(new DtTipoRegistro("General", "Tipo general", 100, 10), edicion));

        String asistente = "asis" + sufijo;
        DtAsistente dt = new DtAsistente(asistente, "Ana", asistente + "@mail.com",
                "Pérez", LocalDate.of(2000, 1, 1), null);
        dt.setContrasenia("clave1");
        dt.setConfirmacionContrasenia("clave1");
        assertTrue(sistema.altaAsistente(dt));

        // Ingresada: no se admite el registro
        assertThrows(IllegalArgumentException.class, () -> sistema.registroAEdicion(
                asistente, edicion, "General", new DtRegistro(hoy, 100), null));

        // Una vez aceptada, sí
        sistema.aceptarRechazarEdicion(edicion, true);

        assertTrue(sistema.registroAEdicion(
                asistente, edicion, "General", new DtRegistro(hoy, 100), null));
    }
}