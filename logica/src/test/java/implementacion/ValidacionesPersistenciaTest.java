package implementacion;

import datatypes.*;
import interfaces.ISistema;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** Validaciones pendientes y ausencia de efectos parciales, exclusivamente en eventosuy_test. */
class ValidacionesPersistenciaTest {
    private final ISistema sistema = Fabrica.getInstance().getISistema();
    private final String sufijo = Long.toString(System.nanoTime());
    private final LocalDate hoy = LocalDate.now();

    private DtAsistente asistente(String nick, String institucion) {
        DtAsistente dt = new DtAsistente(nick, "Ana", nick + "@test.uy", "Pérez", LocalDate.of(2000, 1, 1), institucion);
        dt.setContrasenia("clave");
        dt.setConfirmacionContrasenia("clave");
        return dt;
    }

    private String crearEvento() throws Exception {
        String categoria = "Categoría validación " + sufijo;
        sistema.altaCategoria(categoria, null);
        String evento = "Evento validación " + sufijo;
        assertTrue(sistema.altaEvento(new DtEvento(evento, "VA", "Descripción", hoy, List.of(categoria))));
        return evento;
    }

    @Test
    void categoriaSinNombreNoSePersiste() {
        for (String nombre : new String[]{null, " "}) {
            assertEquals("El nombre de la categoría no puede estar vacío.", assertThrows(Exception.class,
                    () -> sistema.altaCategoria(nombre, null)).getMessage());
        }
        assertTrue(sistema.listarCategorias().stream().noneMatch(c -> c.getIdNombre() == null || c.getIdNombre().isBlank()));
    }

    @Test
    void categoriaConPadreInexistenteNoDejaUnaCategoriaHuerfana() {
        String nombre = "Hija inválida " + sufijo;
        assertEquals("La categoría padre especificada no existe.", assertThrows(Exception.class,
                () -> sistema.altaCategoria(nombre, "ausente" + sufijo)).getMessage());
        assertTrue(sistema.listarCategorias().stream().noneMatch(c -> nombre.equals(c.getIdNombre())));
        assertTrue(sistema.listarCategoriasFormateadas().stream().noneMatch(c -> c.endsWith(nombre)));
    }

    @Test
    void asistenteConInstitucionInexistenteNoReservaNicknameNiCorreo() {
        String nick = "institucionAusente" + sufijo;
        assertEquals("La institución seleccionada no existe.", assertThrows(IllegalArgumentException.class,
                () -> sistema.altaAsistente(asistente(nick, "ausente" + sufijo))).getMessage());
        assertNull(sistema.consultarUsuario(nick));
        assertFalse(sistema.existeUsuario(nick));
        assertFalse(sistema.existeCorreoElectronico(nick + "@test.uy"));
        assertTrue(sistema.altaAsistente(asistente(nick, null)));
        assertEquals(nick, sistema.autenticar(nick, "clave").getNickname());
    }

    @Test
    void modificarAsistenteSinNacimientoConservaDatosYClave() {
        String nick = "nacimientoNulo" + sufijo;
        assertTrue(sistema.altaAsistente(asistente(nick, null)));
        DtAsistente cambio = new DtAsistente(nick, "No guardar", nick + "@test.uy", "Otro apellido", null, null);
        cambio.setContrasenia("nueva");
        cambio.setConfirmacionContrasenia("nueva");
        assertEquals("La fecha de nacimiento es obligatoria.", assertThrows(IllegalArgumentException.class,
                () -> sistema.modificarAsistente(cambio)).getMessage());
        DtAsistente consultado = (DtAsistente) sistema.consultarUsuario(nick);
        assertEquals("Ana", consultado.getNombre());
        assertEquals("Pérez", consultado.getApellido());
        assertEquals(LocalDate.of(2000, 1, 1), consultado.getFechaNacimiento());
        assertEquals(nick, sistema.autenticar(nick, "clave").getNickname());
        assertThrows(IllegalArgumentException.class, () -> sistema.autenticar(nick, "nueva"));
    }

    @Test
    void edicionSinOrganizadorExistenteNoSePersiste() throws Exception {
        String evento = crearEvento();
        String nombre = "Sin organizador " + sufijo;
        DtEdicion dt = new DtEdicion(nombre, "VA", hoy.plusDays(1), hoy.plusDays(2), hoy, "Montevideo", "Uruguay", "ausente" + sufijo);
        assertEquals("El organizador indicado no existe.", assertThrows(IllegalArgumentException.class,
                () -> sistema.altaEdicion(dt, evento)).getMessage());
        assertTrue(sistema.obtenerEdicionesEvento(evento).isEmpty());
    }

    @Test
    void edicionSinFechasNoSePersisteNiReservaElNombre() throws Exception {
        String evento = crearEvento();
        String nick = "orgFechas" + sufijo;
        DtOrganizador organizador = new DtOrganizador(nick, "Organizadora", nick + "@test.uy", "Descripción", null);
        organizador.setContrasenia("clave");
        organizador.setConfirmacionContrasenia("clave");
        assertTrue(sistema.altaOrganizador(organizador));
        String nombre = "Sin fechas " + sufijo;
        for (boolean faltaInicio : new boolean[]{true, false}) {
            DtEdicion dt = new DtEdicion(nombre, "VA", faltaInicio ? null : hoy.plusDays(1),
                    faltaInicio ? hoy.plusDays(2) : null, hoy, "Montevideo", "Uruguay", nick);
            assertEquals("Las fechas de inicio y de fin son obligatorias.", assertThrows(IllegalArgumentException.class,
                    () -> sistema.altaEdicion(dt, evento)).getMessage());
            assertTrue(sistema.obtenerEdicionesEvento(evento).isEmpty());
            assertTrue(sistema.obtenerEdicionesOrganizador(nick).isEmpty());
        }
        assertTrue(sistema.altaEdicion(new DtEdicion(nombre, "VA", hoy.plusDays(1), hoy.plusDays(2), hoy,
                "Montevideo", "Uruguay", nick), evento));
        var persistida = sistema.obtenerEdicionesEvento(evento).iterator().next();
        assertEquals(nombre, persistida.getIdNombre());
        assertEquals(EstadoEdicion.INGRESADA, persistida.getEstado());
    }
}
