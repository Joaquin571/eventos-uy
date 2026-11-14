package implementacion;

import datatypes.*;
import interfaces.ISistema;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;

/** H01/H03: resultados recuperados desde operaciones de persistencia independientes. */
class EtapaUnoTest extends soporte.PruebaConBaseSegura {
    private final ISistema sistema = Fabrica.getInstance().getISistema();
    private final String sufijo = Long.toString(System.nanoTime());

    private DtAsistente asistente(String nick) {
        DtAsistente dt = new DtAsistente(nick, "Ana", nick + "@test.uy", "Pérez",
                LocalDate.of(2000, 1, 1), null, LocalDate.of(1900, 1, 1));
        dt.setContrasenia("clave"); dt.setConfirmacionContrasenia("clave");
        return dt;
    }

    private DtOrganizador organizador(String nick) {
        DtOrganizador dt = new DtOrganizador(nick, "Organización", nick + "@test.uy",
                "Descripción", "", LocalDate.of(1900, 1, 1));
        dt.setContrasenia("clave"); dt.setConfirmacionContrasenia("clave");
        return dt;
    }

    @Test
    void fechaDeAmbosRolesLaAsignaLaLogicaYSeRecuperaSinClaves() {
        LocalDate antes = LocalDate.now();
        String as = "fechaAs" + sufijo, org = "fechaOrg" + sufijo;
        assertTrue(sistema.altaAsistente(asistente(as)));
        assertTrue(sistema.altaOrganizador(organizador(org)));
        LocalDate despues = LocalDate.now();
        for (String nick : List.of(as, org)) {
            DtUsuario consultado = sistema.consultarUsuario(nick);
            assertNotNull(consultado.getFechaAlta());
            assertFalse(consultado.getFechaAlta().isBefore(antes));
            assertFalse(consultado.getFechaAlta().isAfter(despues));
            DtUsuario listado = sistema.listarUsuarios().stream()
                    .filter(u -> nick.equals(u.getNickname())).findFirst().orElseThrow();
            assertEquals(consultado.getFechaAlta(), listado.getFechaAlta());
            assertEquals(consultado.getFechaAlta(), sistema.autenticar(nick, "clave").getFechaAlta());
            assertNull(listado.getContrasenia());
            assertNull(consultado.getConfirmacionContrasenia());
        }
    }

    @Test
    void modificarAmbosRolesConservaFechaPersistidaAunqueElDtoTraigaOtra() {
        String as = "conservaAs" + sufijo, org = "conservaOrg" + sufijo;
        assertTrue(sistema.altaAsistente(asistente(as)));
        assertTrue(sistema.altaOrganizador(organizador(org)));
        LocalDate fechaAs = sistema.consultarUsuario(as).getFechaAlta();
        LocalDate fechaOrg = sistema.consultarUsuario(org).getFechaAlta();
        DtAsistente cambioAs = asistente(as); cambioAs.setNombre("Nombre nuevo");
        DtOrganizador cambioOrg = organizador(org); cambioOrg.setNombre("Organización nueva");
        sistema.modificarAsistente(cambioAs);
        sistema.modificarOrganizador(cambioOrg);
        assertEquals(fechaAs, sistema.consultarUsuario(as).getFechaAlta());
        assertEquals(fechaOrg, sistema.consultarUsuario(org).getFechaAlta());
        assertEquals("Nombre nuevo", sistema.consultarUsuario(as).getNombre());
        assertEquals("Organización nueva", sistema.consultarUsuario(org).getNombre());
    }

    @Test
    void asistenteInvalidoNoReservaNicknameNiCorreo() {
        String nick = "asInvalido" + sufijo;
        for (Consumer<DtAsistente> invalidar : List.<Consumer<DtAsistente>>of(
                d -> d.setApellido(null), d -> d.setApellido(" "),
                d -> d.setFechaNacimiento(null), d -> d.setFechaNacimiento(LocalDate.now().plusDays(1)))) {
            DtAsistente dt = asistente(nick); invalidar.accept(dt);
            assertThrows(IllegalArgumentException.class, () -> sistema.altaAsistente(dt));
            assertNull(sistema.consultarUsuario(nick));
            assertFalse(sistema.existeCorreoElectronico(dt.getCorreoElectronico()));
        }
        assertTrue(sistema.altaAsistente(asistente(nick)));
    }

    @Test
    void nacimientoHoySeAdmiteSinInventarUnaEdadMinimaYSePuedeConservarAlModificar() {
        String nick = "naceHoy" + sufijo;
        DtAsistente dt = asistente(nick); dt.setFechaNacimiento(LocalDate.now());
        assertTrue(sistema.altaAsistente(dt));
        dt.setNombre("Nombre actualizado"); sistema.modificarAsistente(dt);
        DtAsistente resultado = (DtAsistente) sistema.consultarUsuario(nick);
        assertEquals(dt.getFechaNacimiento(), resultado.getFechaNacimiento());
        assertEquals(dt.getNombre(), resultado.getNombre());
    }

    @Test
    void organizadorSinDescripcionNoSeGuardaYSitioWebSigueSiendoOpcional() {
        String nick = "orgInvalido" + sufijo;
        for (String descripcion : new String[]{null, " "}) {
            DtOrganizador dt = organizador(nick); dt.setDescripcion(descripcion);
            assertThrows(IllegalArgumentException.class, () -> sistema.altaOrganizador(dt));
            assertNull(sistema.consultarUsuario(nick));
            assertFalse(sistema.existeCorreoElectronico(dt.getCorreoElectronico()));
        }
        assertTrue(sistema.altaOrganizador(organizador(nick)));
        assertEquals("", ((DtOrganizador) sistema.consultarUsuario(nick)).getSitioWeb());
    }

    @Test
    void institucionExigeDatosBasicosSinReservarNombreTrasError() {
        String nombre = "Institución validada " + sufijo;
        assertThrows(IllegalArgumentException.class, () -> sistema.altaInstitucion(null));
        for (String invalido : new String[]{null, " "}) {
            assertThrows(IllegalArgumentException.class,
                    () -> sistema.altaInstitucion(new DtInstitucion(invalido, "Descripción", "")));
            assertThrows(IllegalArgumentException.class,
                    () -> sistema.altaInstitucion(new DtInstitucion(nombre, invalido, "")));
        }
        assertFalse(sistema.existeInstitucion(nombre));
        assertTrue(sistema.altaInstitucion(new DtInstitucion(nombre, "Descripción", "")));
        assertEquals("Descripción", sistema.listarInstituciones().stream()
                .filter(i -> nombre.equals(i.getNombre())).findFirst().orElseThrow().getDescripcion());
    }

    private String evento() throws Exception {
        String categoria = "Categoría H03 " + sufijo;
        sistema.altaCategoria(categoria, null);
        String nombre = "Evento H03 " + sufijo;
        assertTrue(sistema.altaEvento(new DtEvento(nombre, "EV", "Descripción", null, List.of(categoria))));
        return nombre;
    }

    @Test
    void eventoInvalidoNoSePersisteNiReservaSuNombre() throws Exception {
        assertThrows(IllegalArgumentException.class, () -> sistema.altaEvento(null));
        String cat = "Categoría evento inválido " + sufijo;
        sistema.altaCategoria(cat, null);
        String nombre = "Evento inválido " + sufijo;
        for (DtEvento dt : List.of(
                new DtEvento(" ", "EV", "Descripción", null, List.of(cat)),
                new DtEvento(nombre, null, "Descripción", null, List.of(cat)),
                new DtEvento(nombre, "EV", " ", null, List.of(cat)),
                new DtEvento(nombre, "EV", "Descripción", null, java.util.Arrays.asList((String) null)))) {
            assertThrows(IllegalArgumentException.class, () -> sistema.altaEvento(dt));
        }
        assertTrue(sistema.listarEventos().stream().noneMatch(e -> nombre.equals(e.getNombre())));
        assertTrue(sistema.altaEvento(new DtEvento(nombre, "EV", "Descripción", null, List.of(cat))));
    }

    @Test
    void edicionExigeUbicacionYSiglaSinReservarNombre() throws Exception {
        String evento = evento(), org = "orgEd" + sufijo, ed = "Edición H03 " + sufijo;
        assertTrue(sistema.altaOrganizador(organizador(org)));
        for (int campo = 0; campo < 4; campo++) {
            DtEdicion dt = new DtEdicion(ed, campo == 0 ? " " : "ED", LocalDate.now(), LocalDate.now(),
                    null, campo == 1 ? null : "Ciudad", campo == 2 ? " " : "País", campo == 3 ? null : org);
            assertThrows(IllegalArgumentException.class, () -> sistema.altaEdicion(dt, evento));
            assertTrue(sistema.obtenerEdicionesEvento(evento).isEmpty());
        }
        assertTrue(sistema.altaEdicion(new DtEdicion(ed, "ED", LocalDate.now(), LocalDate.now(),
                null, "Ciudad", "País", org), evento));
        assertEquals(EstadoEdicion.INGRESADA, sistema.obtenerEdicionesEvento(evento).iterator().next().getEstado());
    }

    @Test
    void costosNoFinitosYDescripcionInvalidaNoDejanTiposYCeroSigueSiendoValido() throws Exception {
        String evento = evento(), org = "orgTipoFinito" + sufijo, ed = "Edición finita " + sufijo;
        assertTrue(sistema.altaOrganizador(organizador(org)));
        assertTrue(sistema.altaEdicion(new DtEdicion(ed, "ED", LocalDate.now(), LocalDate.now(),
                null, "Ciudad", "País", org), evento));
        for (float costo : new float[]{Float.NaN, Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY}) {
            assertThrows(IllegalArgumentException.class,
                    () -> sistema.altaTipoRegistro(new DtTipoRegistro("General", "Descripción", costo, 1), ed));
            assertTrue(sistema.obtenerTiposRegistroEdicion(ed).isEmpty());
        }
        assertThrows(IllegalArgumentException.class,
                () -> sistema.altaTipoRegistro(new DtTipoRegistro("General", " ", 0, 1), ed));
        assertTrue(sistema.obtenerTiposRegistroEdicion(ed).isEmpty());
        assertTrue(sistema.altaTipoRegistro(new DtTipoRegistro("General", "Descripción", 0, 1), ed));
        assertEquals(0f, sistema.consultarTipoRegistro(ed, "General").getCosto());
        assertEquals(1, sistema.consultarTipoRegistro(ed, "General").getCupo());
    }
}
