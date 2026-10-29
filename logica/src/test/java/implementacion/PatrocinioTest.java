package implementacion;

import clases.NivelPatrocinio;
import datatypes.*;
import interfaces.ISistema;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import persistencia.BaseDeDatos;
import java.sql.DriverManager;
import java.time.LocalDate;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class PatrocinioTest {
    private final ISistema sistema = Fabrica.getInstance().getISistema();
    private final String sufijo = String.valueOf(System.nanoTime());
    private final String edicion = "Edición patrocinio " + sufijo;
    private final String institucion = "Institución " + sufijo;
    private final String codigo = "COD-" + sufijo;

    @BeforeEach
    void prepararEnBaseDePruebas() throws Exception {
        // Se confirma la base real antes de que Hibernate pueda crear o modificar tablas.
        assertEquals("eventosuy_test", System.getenv("EVENTOS_DB_NAME"), "No ejecutar contra la base habitual.");
        var props = BaseDeDatos.propiedadesJpa();
        assertTrue(props.get("jakarta.persistence.jdbc.url").endsWith("/eventosuy_test"));
        Class.forName("org.postgresql.Driver");
        try (var conexion = DriverManager.getConnection(props.get("jakarta.persistence.jdbc.url"),
                props.get("jakarta.persistence.jdbc.user"), props.get("jakarta.persistence.jdbc.password"))) {
            conexion.setReadOnly(true);
            try (var consulta = conexion.createStatement(); var resultado = consulta.executeQuery("SELECT current_database()")) {
                assertTrue(resultado.next());assertEquals("eventosuy_test", resultado.getString(1));
            }
        }
        String org = "orgPat" + sufijo;
        DtOrganizador usuario = new DtOrganizador(org, "Organizador", org + "@test.uy", "Descripción", "");
        usuario.setContrasenia("clave");usuario.setConfirmacionContrasenia("clave");
        assertTrue(sistema.altaOrganizador(usuario));
        sistema.altaCategoria("catPat" + sufijo, null);
        String evento = "Evento patrocinio " + sufijo;
        assertTrue(sistema.altaEvento(new DtEvento(evento, "EV", "Descripción", LocalDate.now(), List.of("catPat" + sufijo))));
        LocalDate hoy = LocalDate.now();
        assertTrue(sistema.altaEdicion(new DtEdicion(edicion, "ED", hoy, hoy, hoy, "Ciudad", "País", org), evento));
        assertTrue(sistema.altaTipoRegistro(new DtTipoRegistro("General", "General", 100, 50), edicion));
        assertTrue(sistema.altaInstitucion(new DtInstitucion(institucion, "Descripción", "https://ejemplo.uy")));
    }

    private DtPatrocinio datos(float aporte, int cantidad) {
        return new DtPatrocinio(LocalDate.of(1999, 1, 1), aporte, cantidad, codigo,
                NivelPatrocinio.ORO, institucion, edicion, "General");
    }

    @Test
    void altaConsultaYListadoPersistenDatosYAsignanFechaActual() {
        assertTrue(sistema.altaPatrocinio(datos(1000, 2)));
        DtPatrocinio p = sistema.consultarPatrocinio(edicion, institucion);
        assertNotNull(p);assertEquals(LocalDate.now(), p.getFecha());
        assertEquals(codigo, p.getCodigoPatrocinio());assertEquals(1000f, p.getMontoAporte());
        assertEquals(2, p.getCantRegistrosGrat());assertEquals("General", p.getNombreTipoRegistro());
        assertEquals(NivelPatrocinio.ORO, p.getNivel());
        assertEquals(edicion, sistema.consultarPatrocinio(codigo).getNombreEdicion());
        assertEquals(List.of(institucion), sistema.obtenerPatrociniosEdicion(edicion).stream().map(DtPatrocinio::getNombreInstituto).toList());
    }

    @Test
    void aceptaExactamenteElVeintePorCiento() { assertTrue(sistema.altaPatrocinio(datos(1000, 2))); }

    @Test
    void rechazaExcesoDelVeintePorCientoSinGuardar() {
        var error = assertThrows(IllegalArgumentException.class, () -> sistema.altaPatrocinio(datos(1000, 3)));
        assertTrue(error.getMessage().contains("20%"));assertNull(sistema.consultarPatrocinio(codigo));
    }

    @Test
    void rechazaCodigoRepetidoConDatosExistentes() {
        assertTrue(sistema.altaPatrocinio(datos(1000, 1)));
        var error = assertThrows(IllegalArgumentException.class, () -> sistema.altaPatrocinio(datos(1000, 1)));
        assertTrue(error.getMessage().contains("código"));
    }

    @Test
    void rechazaInstitucionRepetidaConOtroCodigo() {
        assertTrue(sistema.altaPatrocinio(datos(1000, 1)));
        DtPatrocinio otro = datos(1000, 1);otro.setCodigoPatrocinio(codigo + "-OTRO");
        var error = assertThrows(IllegalArgumentException.class, () -> sistema.altaPatrocinio(otro));
        assertTrue(error.getMessage().contains("ya posee"));assertNull(sistema.consultarPatrocinio(codigo + "-OTRO"));
    }

    @Test
    void rechazaAportesNoFinitos() {
        for (float aporte : new float[]{Float.NaN, Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY}) {
            var error = assertThrows(IllegalArgumentException.class, () -> sistema.altaPatrocinio(datos(aporte, 1)));
            assertTrue(error.getMessage().contains("aporte"));
        }
        assertNull(sistema.consultarPatrocinio(codigo));
    }

    @Test
    void ceroGratuitosEsValido() { assertTrue(sistema.altaPatrocinio(datos(1000, 0))); }

    @Test
    void consultaInexistenteDevuelveNullYListadoVacio() {
        assertNull(sistema.consultarPatrocinio(edicion, institucion));
        assertTrue(sistema.obtenerPatrociniosEdicion(edicion).isEmpty());
    }
}
