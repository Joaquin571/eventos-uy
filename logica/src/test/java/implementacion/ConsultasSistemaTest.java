package implementacion;

import datatypes.*;
import interfaces.ISistema;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/** Consultas por la interfaz pública; cada prueba crea datos propios en eventosuy_test. */
class ConsultasSistemaTest {
    private final ISistema sistema = Fabrica.getInstance().getISistema();
    private final String sufijo = Long.toString(System.nanoTime());
    private final LocalDate hoy = LocalDate.now();

    private String organizador(String prefijo) {
        String nick = prefijo + sufijo;
        DtOrganizador dt = new DtOrganizador(nick, "Organizadora", nick + "@test.uy", "Descripción", "https://organizador.test");
        dt.setContrasenia("clave");
        dt.setConfirmacionContrasenia("clave");
        assertTrue(sistema.altaOrganizador(dt));
        return nick;
    }

    @Test
    void listarUsuariosDevuelveAmbosRolesConDatosCompletosYSinClaves() {
        String org = organizador("consultaOrg");
        String institucion = "Institución " + sufijo;
        assertTrue(sistema.altaInstitucion(new DtInstitucion(institucion, "Institución", null)));
        String nick = "consultaAs" + sufijo;
        DtAsistente alta = new DtAsistente(nick, "Ana", nick + "@test.uy", "Pérez", LocalDate.of(2000, 2, 3), institucion);
        alta.setContrasenia("clave");
        alta.setConfirmacionContrasenia("clave");
        assertTrue(sistema.altaAsistente(alta));

        var usuarios = sistema.listarUsuarios();
        assertEquals(1, usuarios.stream().filter(u -> nick.equals(u.getNickname())).count());
        assertEquals(1, usuarios.stream().filter(u -> org.equals(u.getNickname())).count());
        DtAsistente asistente = assertInstanceOf(DtAsistente.class,
                usuarios.stream().filter(u -> nick.equals(u.getNickname())).findFirst().orElseThrow());
        assertEquals("Ana", asistente.getNombre());
        assertEquals(nick + "@test.uy", asistente.getCorreoElectronico());
        assertEquals("Pérez", asistente.getApellido());
        assertEquals(LocalDate.of(2000, 2, 3), asistente.getFechaNacimiento());
        assertEquals(institucion, asistente.getNombreInstitucion());
        DtOrganizador organizador = assertInstanceOf(DtOrganizador.class,
                usuarios.stream().filter(u -> org.equals(u.getNickname())).findFirst().orElseThrow());
        assertEquals("Organizadora", organizador.getNombre());
        assertEquals(org + "@test.uy", organizador.getCorreoElectronico());
        assertEquals("Descripción", organizador.getDescripcion());
        assertEquals("https://organizador.test", organizador.getSitioWeb());
        for (DtUsuario usuario : List.of(asistente, organizador)) {
            assertNull(usuario.getContrasenia());
            assertNull(usuario.getConfirmacionContrasenia());
        }
        assertTrue(sistema.existeCorreoElectronico(nick + "@test.uy"));
        assertFalse(sistema.existeCorreoElectronico("ausente" + sufijo + "@test.uy"));
    }

    @Test
    void listarInstitucionesConservaDescripcionYSitioOpcional() {
        String primera = "Consulta institución " + sufijo;
        String segunda = "Consulta sin sitio " + sufijo;
        assertTrue(sistema.altaInstitucion(new DtInstitucion(primera, "Descripción completa", "https://institucion.test")));
        assertTrue(sistema.altaInstitucion(new DtInstitucion(segunda, "Sin sitio", null)));
        var instituciones = sistema.listarInstituciones();
        var una = instituciones.stream().filter(i -> primera.equals(i.getNombre())).findFirst().orElseThrow();
        var otra = instituciones.stream().filter(i -> segunda.equals(i.getNombre())).findFirst().orElseThrow();
        assertEquals("Descripción completa", una.getDescripcion());
        assertEquals("https://institucion.test", una.getSitioWeb());
        assertEquals("Sin sitio", otra.getDescripcion());
        assertNull(otra.getSitioWeb());
        assertTrue(sistema.existeInstitucion(primera));
        assertFalse(sistema.existeInstitucion("ausente" + sufijo));
    }

    @Test
    void listarEventosIncluyeTodasLasCategoriasYLaFechaAsignadaPorLaLogica() throws Exception {
        String primera = "Categoría A " + sufijo;
        String segunda = "Categoría B " + sufijo;
        sistema.altaCategoria(primera, null);
        sistema.altaCategoria(segunda, null);
        String nombre = "Consulta evento " + sufijo;
        assertTrue(sistema.altaEvento(new DtEvento(nombre, "CE", "Descripción del evento", hoy.minusYears(1), List.of(primera, segunda))));
        var evento = sistema.listarEventos().stream().filter(e -> nombre.equals(e.getNombre())).findFirst().orElseThrow();
        assertEquals("CE", evento.getSigla());
        assertEquals("Descripción del evento", evento.getDescripcion());
        assertEquals(hoy, evento.getFechaAlta());
        assertEquals(Set.of(primera, segunda), Set.copyOf(evento.getCategorias()));
        assertEquals(2, evento.getCategorias().size());
    }

    @Test
    void categoriasConservanPadresYFormatoDeTresNivelesSinDuplicados() throws Exception {
        String raiz = "Raíz " + sufijo;
        String hija = "Hija " + sufijo;
        String nieta = "Nieta " + sufijo;
        sistema.altaCategoria(raiz, null);
        sistema.altaCategoria(hija, raiz);
        sistema.altaCategoria(nieta, hija);
        var categorias = sistema.listarCategorias().stream()
                .filter(c -> Set.of(raiz, hija, nieta).contains(c.getIdNombre())).toList();
        assertEquals(3, categorias.size());
        assertNull(categorias.stream().filter(c -> raiz.equals(c.getIdNombre())).findFirst().orElseThrow().getNombrePadre());
        assertEquals(raiz, categorias.stream().filter(c -> hija.equals(c.getIdNombre())).findFirst().orElseThrow().getNombrePadre());
        assertEquals(hija, categorias.stream().filter(c -> nieta.equals(c.getIdNombre())).findFirst().orElseThrow().getNombrePadre());
        var formateadas = sistema.listarCategoriasFormateadas().stream().filter(c -> c.endsWith(sufijo)).toList();
        assertEquals(List.of(raiz, "   - " + hija, "   -    - " + nieta), formateadas);
    }

    @Test
    void edicionesDelOrganizadorConservanDatosEstadosYAislamientoEntreUsuarios() throws Exception {
        String org = organizador("duenioConsulta");
        String otro = organizador("otroConsulta");
        String sinEdiciones = organizador("vacioConsulta");
        String categoria = "Categoría ediciones " + sufijo;
        String evento = "Evento ediciones " + sufijo;
        sistema.altaCategoria(categoria, null);
        assertTrue(sistema.altaEvento(new DtEvento(evento, "ED", "Evento", hoy, List.of(categoria))));
        for (EstadoEdicion estado : EstadoEdicion.values()) {
            String nombre = estado.name() + sufijo;
            assertTrue(sistema.altaEdicion(new DtEdicion(nombre, "ED", hoy.plusDays(10), hoy.plusDays(11),
                    hoy.minusYears(1), "Montevideo", "Uruguay", org), evento));
            if (estado != EstadoEdicion.INGRESADA) sistema.aceptarRechazarEdicion(nombre, estado == EstadoEdicion.ACEPTADA);
        }
        String ajena = "Ajena " + sufijo;
        assertTrue(sistema.altaEdicion(new DtEdicion(ajena, "AJ", hoy.plusDays(10), hoy.plusDays(11), hoy, "Salto", "Uruguay", otro), evento));
        var ediciones = sistema.obtenerEdicionesOrganizador(org);
        assertEquals(3, ediciones.size());
        assertEquals(Set.of(EstadoEdicion.values()), ediciones.stream().map(DtEdicion::getEstado).collect(Collectors.toSet()));
        for (DtEdicion edicion : ediciones) {
            assertEquals(edicion.getEstado().name() + sufijo, edicion.getIdNombre());
            assertEquals(org, edicion.getNombreOrganizador());
            assertEquals("ED", edicion.getSigla());
            assertEquals(hoy.plusDays(10), edicion.getFechaInicio());
            assertEquals(hoy.plusDays(11), edicion.getFechaFin());
            assertEquals(hoy, edicion.getFechaAlta());
            assertEquals("Montevideo", edicion.getCiudad());
            assertEquals("Uruguay", edicion.getPais());
        }
        assertEquals(List.of(ajena), sistema.obtenerEdicionesOrganizador(otro).stream().map(DtEdicion::getIdNombre).toList());
        assertTrue(sistema.obtenerEdicionesOrganizador(sinEdiciones).isEmpty());
    }
}
