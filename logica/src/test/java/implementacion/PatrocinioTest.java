package implementacion;

import clases.NivelPatrocinio;
import datatypes.*;
import interfaces.ISistema;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class PatrocinioTest extends soporte.PruebaConBaseSegura {
    private final ISistema sistema = Fabrica.getInstance().getISistema();
    private final String sufijo = String.valueOf(System.nanoTime());
    private final String edicion = "Edición patrocinio " + sufijo;
    private final String institucion = "Institución " + sufijo;
    private final String codigo = "COD-" + sufijo;
    private final String org = "orgPat" + sufijo;
    private final String evento = "Evento patrocinio " + sufijo;

    @BeforeEach
    void prepararEnBaseDePruebas() throws Exception {
        DtOrganizador usuario = new DtOrganizador(org, "Organizador", org + "@test.uy", "Descripción", "");
        usuario.setContrasenia("clave");usuario.setConfirmacionContrasenia("clave");
        assertTrue(sistema.altaOrganizador(usuario));
        sistema.altaCategoria("catPat" + sufijo, null, datatypes.Actor.administradorLocal());
        assertTrue(sistema.altaEvento(new DtEvento(evento, "EV", "Descripción", LocalDate.now(), List.of("catPat" + sufijo)), datatypes.Actor.administradorLocal()));
        LocalDate hoy = LocalDate.now();
        assertTrue(sistema.altaEdicion(new DtEdicion(edicion, "ED", hoy, hoy, hoy, "Ciudad", "País", org), evento, datatypes.Actor.administradorLocal()));
        assertTrue(sistema.altaTipoRegistro(new DtTipoRegistro("General", "General", 100, 50), edicion, Actor.administradorLocal()));
        assertTrue(sistema.altaInstitucion(new DtInstitucion(institucion, "Descripción", "https://ejemplo.uy"), datatypes.Actor.administradorLocal()));
    }

    private DtPatrocinio datos(float aporte, int cantidad) {
        return new DtPatrocinio(LocalDate.of(1999, 1, 1), aporte, cantidad, codigo,
                NivelPatrocinio.ORO, institucion, edicion, "General");
    }

    @Test
    void propietarioAutenticadoCreaConLaMismaOperacionQueSwing() {
        Actor propietario = Actor.usuarioAutenticado(sistema.autenticar(org, "clave"));
        assertTrue(sistema.altaPatrocinio(datos(1000, 1), evento, propietario));
        assertEquals(codigo, sistema.consultarPatrocinio(edicion, institucion, datatypes.Actor.administradorLocal()).getCodigoPatrocinio());
    }

    @Test
    void propietarioPuedePatrocinarSusEdicionesEnLosTresEstados() {
        Actor propietario = Actor.usuarioAutenticado(sistema.autenticar(org, "clave"));
        for (EstadoEdicion estado : List.of(EstadoEdicion.INGRESADA, EstadoEdicion.ACEPTADA, EstadoEdicion.RECHAZADA)) {
            String nombre = edicion + estado;
            LocalDate hoy = LocalDate.now();
            assertTrue(sistema.altaEdicion(new DtEdicion(nombre, "ED", hoy, hoy, hoy, "Ciudad", "País", org), evento, datatypes.Actor.administradorLocal()));
            if (estado != EstadoEdicion.INGRESADA) {
                sistema.aceptarRechazarEdicion(nombre, estado == EstadoEdicion.ACEPTADA, datatypes.Actor.administradorLocal());
            }
            assertTrue(sistema.altaTipoRegistro(new DtTipoRegistro("General", "Descripción", 100, 50), nombre, propietario));
            DtPatrocinio nuevo = datos(1000, 1);
            nuevo.setNombreEdicion(nombre); nuevo.setCodigoPatrocinio(codigo + estado);
            assertTrue(sistema.altaPatrocinio(nuevo, evento, propietario));
            var opciones = sistema.obtenerOpcionesAltaPatrocinio(evento, nombre, propietario);
            assertTrue(opciones.ediciones().stream().anyMatch(e -> nombre.equals(e.getIdNombre()) && e.getEstado() == estado));
        }
    }

    @Test
    void ajenoAsistenteYActorAusenteNoInsertanNiModificanElPatrocinioExistente() {
        var ajeno = new DtOrganizador("ajeno" + sufijo, "Ajeno", "ajeno" + sufijo + "@test.uy", "Descripción", "");
        ajeno.setContrasenia("clave"); ajeno.setConfirmacionContrasenia("clave");
        assertTrue(sistema.altaOrganizador(ajeno));
        var asistente = new DtAsistente("as" + sufijo, "Asistente", "as" + sufijo + "@test.uy", "Apellido",
                LocalDate.of(2000, 1, 1), null);
        asistente.setContrasenia("clave"); asistente.setConfirmacionContrasenia("clave");
        assertTrue(sistema.altaAsistente(asistente));
        Actor[] actores = {null, Actor.usuarioAutenticado(sistema.autenticar(ajeno.getNickname(), "clave")),
                Actor.usuarioAutenticado(sistema.autenticar(asistente.getNickname(), "clave"))};
        assertTrue(sistema.altaPatrocinio(datos(1000, 1), evento, Actor.administradorLocal()));
        for (Actor actor : actores) {
            // El código repetido no debe ocultar la denegación de propiedad.
            assertThrows(excepciones.AccesoDenegadoException.class,
                    () -> sistema.altaPatrocinio(datos(1000, 1), evento, actor));
            DtPatrocinio nuevo = datos(1000, 1); nuevo.setCodigoPatrocinio(codigo + "-denegado");
            assertThrows(excepciones.AccesoDenegadoException.class,
                    () -> sistema.altaPatrocinio(nuevo, evento, actor));
            assertNull(sistema.consultarPatrocinio(nuevo.getCodigoPatrocinio(), datatypes.Actor.administradorLocal()));
            assertEquals(1, sistema.obtenerPatrociniosEdicion(edicion, datatypes.Actor.administradorLocal()).size());
            assertEquals(1000f, sistema.consultarPatrocinio(codigo, datatypes.Actor.administradorLocal()).getMontoAporte());
            assertEquals(1, sistema.consultarPatrocinio(codigo, datatypes.Actor.administradorLocal()).getCantRegistrosGrat());
        }
    }

    @Test
    void erroresFuncionalesDelPropietarioReviertenLaUnidadDeEscritura() {
        Actor propietario = Actor.usuarioAutenticado(sistema.autenticar(org, "clave"));
        assertThrows(IllegalArgumentException.class,
                () -> sistema.altaPatrocinio(datos(1000, 1), "Evento incompatible", propietario));
        for (String campo : List.of("institucion", "tipo", "aporte", "gratuitos")) {
            DtPatrocinio invalido = datos(1000, 1);
            switch (campo) {
                case "institucion" -> invalido.setNombreInstituto("Inexistente" + sufijo);
                case "tipo" -> invalido.setNombreTipoRegistro("Inexistente" + sufijo);
                case "aporte" -> invalido.setMontoAporte(Float.NaN);
                case "gratuitos" -> invalido.setCantRegistrosGrat(-1);
            }
            assertThrows(IllegalArgumentException.class, () -> sistema.altaPatrocinio(invalido, evento, propietario));
            assertNull(sistema.consultarPatrocinio(codigo, datatypes.Actor.administradorLocal()));
            assertTrue(sistema.obtenerPatrociniosEdicion(edicion, datatypes.Actor.administradorLocal()).isEmpty());
        }
        // Una operación válida posterior demuestra también que la transacción rechazada no quedó abierta.
        assertTrue(sistema.altaPatrocinio(datos(1000, 1), evento, propietario));
    }

    @Test
    void opcionesDelFormularioSoloIncluyenEdicionesPropiasYNoPermitenConsultarTiposAjenos() {
        Actor propietario = Actor.usuarioAutenticado(sistema.autenticar(org, "clave"));
        var opciones = sistema.obtenerOpcionesAltaPatrocinio("", edicion, propietario);
        assertEquals(evento, opciones.evento());
        assertTrue(opciones.ediciones().stream().allMatch(e -> org.equals(e.getNombreOrganizador())));
        assertEquals(List.of("General"), opciones.tipos().stream().map(DtTipoRegistro::getIdNombre).toList());
        Actor ajeno = Actor.usuarioAutenticado(new DtOrganizador("otro", "Otro", "otro@test.uy", "", ""));
        assertThrows(excepciones.AccesoDenegadoException.class,
                () -> sistema.obtenerOpcionesAltaPatrocinio(evento, edicion, ajeno));
        assertThrows(excepciones.AccesoDenegadoException.class,
                () -> sistema.obtenerOpcionesAltaPatrocinio(evento, "inexistente", propietario));
    }

    @Test
    void altaConsultaYListadoPersistenDatosYAsignanFechaActual() {
        assertTrue(sistema.altaPatrocinio(datos(1000, 2), null, datatypes.Actor.administradorLocal()));
        DtPatrocinio p = sistema.consultarPatrocinio(edicion, institucion, datatypes.Actor.administradorLocal());
        assertNotNull(p);assertEquals(LocalDate.now(), p.getFecha());
        assertEquals(codigo, p.getCodigoPatrocinio());assertEquals(1000f, p.getMontoAporte());
        assertEquals(2, p.getCantRegistrosGrat());assertEquals("General", p.getNombreTipoRegistro());
        assertEquals(NivelPatrocinio.ORO, p.getNivel());
        assertEquals(edicion, sistema.consultarPatrocinio(codigo, datatypes.Actor.administradorLocal()).getNombreEdicion());
        assertEquals(List.of(institucion), sistema.obtenerPatrociniosEdicion(edicion, datatypes.Actor.administradorLocal()).stream().map(DtPatrocinio::getNombreInstituto).toList());
    }

    @Test
    void aceptaExactamenteElVeintePorCiento() { assertTrue(sistema.altaPatrocinio(datos(1000, 2), null, datatypes.Actor.administradorLocal())); }

    @Test
    void rechazaExcesoDelVeintePorCientoSinGuardar() {
        var error = assertThrows(IllegalArgumentException.class, () -> sistema.altaPatrocinio(datos(1000, 3), null, datatypes.Actor.administradorLocal()));
        assertTrue(error.getMessage().contains("20%"));assertNull(sistema.consultarPatrocinio(codigo, datatypes.Actor.administradorLocal()));
    }

    @Test
    void rechazaCodigoRepetidoConDatosExistentes() {
        assertTrue(sistema.altaPatrocinio(datos(1000, 1), null, datatypes.Actor.administradorLocal()));
        var error = assertThrows(IllegalArgumentException.class, () -> sistema.altaPatrocinio(datos(1000, 1), null, datatypes.Actor.administradorLocal()));
        assertTrue(error.getMessage().contains("código"));
    }

    @Test
    void rechazaInstitucionRepetidaConOtroCodigo() {
        assertTrue(sistema.altaPatrocinio(datos(1000, 1), null, datatypes.Actor.administradorLocal()));
        DtPatrocinio otro = datos(1000, 1);otro.setCodigoPatrocinio(codigo + "-OTRO");
        var error = assertThrows(IllegalArgumentException.class, () -> sistema.altaPatrocinio(otro, null, datatypes.Actor.administradorLocal()));
        assertTrue(error.getMessage().contains("ya posee"));assertNull(sistema.consultarPatrocinio(codigo + "-OTRO", datatypes.Actor.administradorLocal()));
    }

    @Test
    void rechazaAportesNoFinitos() {
        for (float aporte : new float[]{Float.NaN, Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY}) {
            var error = assertThrows(IllegalArgumentException.class, () -> sistema.altaPatrocinio(datos(aporte, 1), null, datatypes.Actor.administradorLocal()));
            assertTrue(error.getMessage().contains("aporte"));
        }
        assertNull(sistema.consultarPatrocinio(codigo, datatypes.Actor.administradorLocal()));
    }

    @Test
    void ceroGratuitosEsValido() { assertTrue(sistema.altaPatrocinio(datos(1000, 0), null, datatypes.Actor.administradorLocal())); }

    @Test
    void consultaInexistenteDevuelveNullYListadoVacio() {
        assertNull(sistema.consultarPatrocinio(edicion, institucion, datatypes.Actor.administradorLocal()));
        assertTrue(sistema.obtenerPatrociniosEdicion(edicion, datatypes.Actor.administradorLocal()).isEmpty());
    }
}
