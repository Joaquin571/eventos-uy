package implementacion;

import clases.NivelPatrocinio;
import datatypes.*;
import interfaces.ISistema;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** Sistema y persistencia reales; la superclase verifica current_database() antes de preparar datos. */
class ConsultaPatrociniosVisibilidadTest extends soporte.PruebaConBaseSegura {
    private final ISistema sistema = Fabrica.getInstance().getISistema();
    private final String sufijo = Long.toString(System.nanoTime());
    private final String evento = "Evento consulta patrocinio " + sufijo;
    private final String institucion = "Institución consulta " + sufijo;
    private Actor propietario;
    private Actor ajeno;
    private Actor asistente;

    private String edicion(EstadoEdicion estado) { return "Edición " + estado + sufijo; }
    private String codigo(EstadoEdicion estado) { return "RESERVADO-" + estado + sufijo; }

    @BeforeEach
    void preparar() throws Exception {
        propietario = organizador("duenio");
        ajeno = organizador("ajeno");
        String nick = "asistente" + sufijo;
        DtAsistente usuario = new DtAsistente(nick, "Asistente", nick + "@test.uy", "Apellido", LocalDate.of(2000, 1, 1), null);
        usuario.setContrasenia("clave"); usuario.setConfirmacionContrasenia("clave");
        assertTrue(sistema.altaAsistente(usuario));
        asistente = Actor.usuarioAutenticado(sistema.autenticar(nick, "clave"));
        String categoria = "categoriaConsulta" + sufijo;
        sistema.altaCategoria(categoria, null);
        assertTrue(sistema.altaEvento(new DtEvento(evento, "EV", "Descripción", null, List.of(categoria))));
        assertTrue(sistema.altaInstitucion(new DtInstitucion(institucion, "Descripción", "https://test.uy"), datatypes.Actor.administradorLocal()));
        LocalDate hoy = LocalDate.now();
        for (EstadoEdicion estado : EstadoEdicion.values()) {
            String nombre = edicion(estado);
            assertTrue(sistema.altaEdicion(new DtEdicion(nombre, "ED", hoy, hoy, hoy, "Ciudad", "País", propietario.nickname()), evento));
            if (estado != EstadoEdicion.INGRESADA) sistema.aceptarRechazarEdicion(nombre, estado == EstadoEdicion.ACEPTADA);
            assertTrue(sistema.altaTipoRegistro(new DtTipoRegistro("General", "Descripción", 100, 50), nombre, propietario));
            assertTrue(sistema.altaPatrocinio(new DtPatrocinio(null, 1000, 1, codigo(estado), NivelPatrocinio.ORO,
                    institucion, nombre, "General"), evento, propietario));
        }
    }

    private Actor organizador(String prefijo) {
        String nick = prefijo + sufijo;
        DtOrganizador usuario = new DtOrganizador(nick, "Organizador", nick + "@test.uy", "Descripción", "");
        usuario.setContrasenia("clave"); usuario.setConfirmacionContrasenia("clave");
        assertTrue(sistema.altaOrganizador(usuario));
        return Actor.usuarioAutenticado(sistema.autenticar(nick, "clave"));
    }

    @Test
    void tercerosVenAceptadasSinCodigoEnNingunaOperacionDeConsulta() {
        String nombre = edicion(EstadoEdicion.ACEPTADA);
        for (Actor actor : new Actor[]{null, asistente, ajeno}) {
            for (DtPatrocinio dto : List.of(sistema.consultarPatrocinio(nombre, institucion, actor),
                    sistema.consultarPatrocinio(codigo(EstadoEdicion.ACEPTADA), actor),
                    sistema.obtenerPatrociniosEdicion(nombre, actor).iterator().next())) {
                assertNull(dto.getCodigoPatrocinio());
                assertEquals(institucion, dto.getNombreInstituto());
                assertEquals(1000f, dto.getMontoAporte());
                assertEquals(1, dto.getCantRegistrosGrat());
                assertEquals(NivelPatrocinio.ORO, dto.getNivel());
            }
            var consulta = sistema.consultarPatrocinios(evento, nombre, institucion, actor);
            assertNotNull(consulta); assertFalse(consulta.puedeVerCodigo());
            assertNull(consulta.patrocinio().getCodigoPatrocinio());
        }
    }

    @Test
    void propietarioConoceCodigoEnLosTresEstados() {
        comprobarPrivilegiado(propietario);
    }

    @Test
    void administradorLocalConservaConsultaCompletaEnLosTresEstados() {
        comprobarPrivilegiado(Actor.administradorLocal());
    }

    private void comprobarPrivilegiado(Actor actor) {
        for (EstadoEdicion estado : EstadoEdicion.values()) {
            String nombre = edicion(estado);
            assertEquals(codigo(estado), sistema.consultarPatrocinio(nombre, institucion, actor).getCodigoPatrocinio());
            assertEquals(codigo(estado), sistema.consultarPatrocinio(codigo(estado), actor).getCodigoPatrocinio());
            assertEquals(codigo(estado), sistema.obtenerPatrociniosEdicion(nombre, actor).iterator().next().getCodigoPatrocinio());
            var consulta = sistema.consultarPatrocinios("", nombre, institucion, actor);
            assertTrue(consulta.puedeVerCodigo());
            assertEquals(codigo(estado), consulta.patrocinio().getCodigoPatrocinio());
        }
    }

    @Test
    void tercerosNoRecibenDatosDeIngresadasORechazadasAunqueConozcanElCodigo() {
        for (EstadoEdicion estado : new EstadoEdicion[]{EstadoEdicion.INGRESADA, EstadoEdicion.RECHAZADA}) {
            String nombre = edicion(estado);
            for (Actor actor : new Actor[]{null, asistente, ajeno}) {
                assertNull(sistema.consultarPatrocinio(nombre, institucion, actor));
                assertNull(sistema.consultarPatrocinio(codigo(estado), actor));
                assertTrue(sistema.obtenerPatrociniosEdicion(nombre, actor).isEmpty());
                assertTrue(sistema.obtenerInstitucionesPatrocinadoras(nombre, actor).isEmpty());
                assertNull(sistema.consultarPatrocinios(evento, nombre, institucion, actor));
                assertNull(sistema.consultarPatrocinios(evento, nombre, "", actor));
            }
        }
    }

    @Test
    void selectorListaAceptadasYConservaSoloLaPrivadaSeleccionadaDelPropietario() {
        var sinSeleccion = sistema.consultarPatrocinios(evento, "", "", propietario);
        assertEquals(List.of(edicion(EstadoEdicion.ACEPTADA)), sinSeleccion.ediciones().stream().map(DtEdicion::getIdNombre).toList());
        String privada = edicion(EstadoEdicion.INGRESADA);
        var seleccion = sistema.consultarPatrocinios("Evento de URL manipulado", privada, institucion, propietario);
        assertEquals(evento, seleccion.evento());
        assertEquals(2, seleccion.ediciones().size());
        assertFalse(seleccion.ediciones().stream().anyMatch(e -> edicion(EstadoEdicion.RECHAZADA).equals(e.getIdNombre())));
        assertNull(sistema.consultarPatrocinios("Evento de URL manipulado", privada, institucion, ajeno));
    }

    @Test
    void clavesAusentesInexistentesOConOtraCapitalizacionNoDevuelvenPatrociniosAjenos() {
        Actor admin = Actor.administradorLocal();
        String nombre = edicion(EstadoEdicion.ACEPTADA);
        assertNull(sistema.consultarPatrocinio(nombre, "No existe", admin));
        assertNull(sistema.consultarPatrocinio(nombre.toUpperCase(java.util.Locale.ROOT), institucion, admin));
        assertNull(sistema.consultarPatrocinio("No existe", admin));
        assertNull(sistema.consultarPatrocinio((String) null, admin));
        assertNull(sistema.consultarPatrocinio(nombre, null, admin));
        assertTrue(sistema.obtenerPatrociniosEdicion(null, admin).isEmpty());
        assertNull(sistema.consultarPatrocinios(evento, nombre, "No existe", admin));
        assertNull(sistema.consultarPatrocinios(evento, "No existe", "", admin));
    }

    @Test
    void resumenNuncaTransfiereCodigosYModificarDtoNoAlteraDatosNiPermisos() {
        String nombre = edicion(EstadoEdicion.ACEPTADA);
        assertEquals(List.of(institucion), sistema.obtenerInstitucionesPatrocinadoras(nombre, propietario));
        assertEquals(List.of(institucion), sistema.consultarPatrocinios(evento, nombre, "", propietario).patrocinadoras());
        DtPatrocinio publico = sistema.consultarPatrocinio(nombre, institucion, null);
        publico.setCodigoPatrocinio("inyectado"); publico.setMontoAporte(0);
        assertNull(sistema.consultarPatrocinio(nombre, institucion, null).getCodigoPatrocinio());
        DtPatrocinio completo = sistema.consultarPatrocinio(nombre, institucion, propietario);
        assertEquals(codigo(EstadoEdicion.ACEPTADA), completo.getCodigoPatrocinio());
        assertEquals(1000f, completo.getMontoAporte());
        assertEquals(1, sistema.obtenerPatrociniosEdicion(nombre, propietario).size());
    }

    @Test
    void noQuedanFirmasPublicasDeConsultaSinActor() {
        assertThrows(NoSuchMethodException.class, () -> ISistema.class.getMethod("consultarPatrocinio", String.class));
        assertThrows(NoSuchMethodException.class, () -> ISistema.class.getMethod("consultarPatrocinio", String.class, String.class));
        assertThrows(NoSuchMethodException.class, () -> ISistema.class.getMethod("obtenerPatrociniosEdicion", String.class));
    }
}
