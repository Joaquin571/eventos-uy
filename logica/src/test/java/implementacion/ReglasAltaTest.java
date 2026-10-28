package implementacion;

import clases.NivelPatrocinio;
import datatypes.DtEdicion;
import datatypes.DtEvento;
import datatypes.DtInstitucion;
import datatypes.DtOrganizador;
import datatypes.DtPatrocinio;
import datatypes.DtTipoRegistro;
import interfaces.ISistema;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Reglas de las altas de eventos, ediciones, tipos de registro, instituciones y patrocinios
 * (secciones 6.2 y 7.5). Necesitan la base de pruebas (eventosuy_test).
 */
class ReglasAltaTest {

    private final ISistema sistema = Fabrica.getInstance().getISistema();
    private final String sufijo = String.valueOf(System.nanoTime());
    private final LocalDate hoy = LocalDate.now();

    private String organizador;
    private String categoria;
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

        categoria = "cat" + sufijo;
        sistema.altaCategoria(categoria, null);

        evento = "evento" + sufijo;
        assertTrue(sistema.altaEvento(new DtEvento(evento, "EV" + sufijo,
                "Evento de prueba", hoy, List.of(categoria))));
    }

    private DtEdicion dtEdicion(String nombre, LocalDate inicio, LocalDate fin) {
        return new DtEdicion(nombre, "ED", inicio, fin, hoy, "Montevideo", "Uruguay", organizador);
    }

    private String crearEdicion() throws Exception {
        prepararEvento();

        String edicion = "edicion" + (++contador) + "_" + sufijo;
        assertTrue(sistema.altaEdicion(dtEdicion(edicion, hoy.plusDays(10), hoy.plusDays(12)), evento));
        return edicion;
    }

    private String crearInstitucion(String prefijo) {
        String nombre = prefijo + sufijo;
        assertTrue(sistema.altaInstitucion(new DtInstitucion(nombre, "Institución de prueba", "https://ejemplo.com")));
        return nombre;
    }

    private DtPatrocinio patrocinio(String codigo, String institucion, String edicion, String tipo,
                                    float aporte, int gratuitos) {
        return new DtPatrocinio(hoy, aporte, gratuitos, codigo, NivelPatrocinio.ORO,
                institucion, edicion, tipo);
    }

    // ----------------------------------------------------------------- eventos

    @Test
    void unEventoConNombreRepetidoSeRechaza() throws Exception {
        prepararEvento();

        assertThrows(Exception.class, () -> sistema.altaEvento(new DtEvento(evento, "OTRA",
                "Otro evento", hoy, List.of(categoria))));
    }

    @Test
    void unEventoExigeAlMenosUnaCategoria() throws Exception {
        prepararEvento();

        assertThrows(Exception.class, () -> sistema.altaEvento(new DtEvento("sinCat" + sufijo,
                "SC", "Sin categorías", hoy, List.of())));
    }

    @Test
    void unEventoNoAdmiteUnaCategoriaInexistente() throws Exception {
        prepararEvento();

        assertThrows(Exception.class, () -> sistema.altaEvento(new DtEvento("catMala" + sufijo,
                "CM", "Categoría inexistente", hoy, List.of("noexiste" + sufijo))));
    }

    @Test
    void unaCategoriaRepetidaSeRechaza() throws Exception {
        prepararEvento();

        assertThrows(Exception.class, () -> sistema.altaCategoria(categoria, null));
    }

    // ---------------------------------------------------------------- ediciones

    @Test
    void unaEdicionConNombreRepetidoSeRechaza() throws Exception {
        String edicion = crearEdicion();

        assertThrows(IllegalArgumentException.class, () -> sistema.altaEdicion(
                dtEdicion(edicion, hoy.plusDays(20), hoy.plusDays(22)), evento));
    }

    @Test
    void unaEdicionDeUnEventoInexistenteSeRechaza() throws Exception {
        prepararEvento();

        assertThrows(IllegalArgumentException.class, () -> sistema.altaEdicion(
                dtEdicion("sinEvento" + sufijo, hoy.plusDays(10), hoy.plusDays(12)), "noexiste" + sufijo));
    }

    @Test
    void unaEdicionConElFinAnteriorAlInicioSeRechaza() throws Exception {
        prepararEvento();

        assertThrows(IllegalArgumentException.class, () -> sistema.altaEdicion(
                dtEdicion("fechas" + sufijo, hoy.plusDays(12), hoy.plusDays(10)), evento));
    }

    @Test
    void unaEdicionAdmiteInicioYFinElMismoDia() throws Exception {
        prepararEvento();

        assertTrue(sistema.altaEdicion(
                dtEdicion("mismoDia" + sufijo, hoy.plusDays(10), hoy.plusDays(10)), evento));
    }

    // -------------------------------------------------------- tipos de registro

    @Test
    void unTipoDeRegistroRepetidoEnLaEdicionSeRechaza() throws Exception {
        String edicion = crearEdicion();

        assertTrue(sistema.altaTipoRegistro(new DtTipoRegistro("General", "Tipo general", 100, 10), edicion));

        assertThrows(IllegalArgumentException.class, () -> sistema.altaTipoRegistro(
                new DtTipoRegistro("General", "Otro", 200, 20), edicion));
    }

    @Test
    void elMismoNombreDeTipoSePuedeUsarEnOtraEdicion() throws Exception {
        String primera = crearEdicion();
        String segunda = crearEdicion();

        assertTrue(sistema.altaTipoRegistro(new DtTipoRegistro("General", "Tipo general", 100, 10), primera));
        assertTrue(sistema.altaTipoRegistro(new DtTipoRegistro("General", "Tipo general", 100, 10), segunda));
    }

    @Test
    void unTipoDeRegistroExigeEdicionExistenteCostoYCupoValidos() throws Exception {
        String edicion = crearEdicion();

        assertThrows(IllegalArgumentException.class, () -> sistema.altaTipoRegistro(
                new DtTipoRegistro("General", "Tipo", 100, 10), "noexiste" + sufijo));
        assertThrows(IllegalArgumentException.class, () -> sistema.altaTipoRegistro(
                new DtTipoRegistro("Negativo", "Tipo", -1, 10), edicion));
        assertThrows(IllegalArgumentException.class, () -> sistema.altaTipoRegistro(
                new DtTipoRegistro("SinCupo", "Tipo", 100, 0), edicion));
    }

    // ------------------------------------------------------------ instituciones

    @Test
    void unaInstitucionConNombreRepetidoSeRechaza() {
        String nombre = crearInstitucion("inst");

        assertFalse(sistema.altaInstitucion(new DtInstitucion(nombre, "Otra descripción", "https://otra.com")));
    }

    // -------------------------------------------------------------- patrocinios

    @Test
    void unPatrocinioValidoSeRegistra() throws Exception {
        String edicion = crearEdicion();
        String institucion = crearInstitucion("instOk");
        assertTrue(sistema.altaTipoRegistro(new DtTipoRegistro("General", "Tipo", 1000, 50), edicion));

        // 2 gratuitos de $1000 = $2000, menos del 20 % de $100000
        assertTrue(sistema.altaPatrocinio(patrocinio("COD-OK-" + sufijo, institucion, edicion, "General", 100_000f, 2)));
    }

    @Test
    void elCodigoDePatrocinioEsUnicoEnLaPlataforma() throws Exception {
        String edicion = crearEdicion();
        String primera = crearInstitucion("instA");
        String segunda = crearInstitucion("instB");
        assertTrue(sistema.altaTipoRegistro(new DtTipoRegistro("General", "Tipo", 1000, 50), edicion));

        String codigo = "COD-UNICO-" + sufijo;
        assertTrue(sistema.altaPatrocinio(patrocinio(codigo, primera, edicion, "General", 100_000f, 2)));

        assertThrows(IllegalArgumentException.class, () -> sistema.altaPatrocinio(
                patrocinio(codigo, segunda, edicion, "General", 100_000f, 2)));
    }

    @Test
    void unaInstitucionNoPuedePatrocinarDosVecesLaMismaEdicion() throws Exception {
        String edicion = crearEdicion();
        String institucion = crearInstitucion("instDos");
        assertTrue(sistema.altaTipoRegistro(new DtTipoRegistro("General", "Tipo", 1000, 50), edicion));

        assertTrue(sistema.altaPatrocinio(patrocinio("COD-1-" + sufijo, institucion, edicion, "General", 100_000f, 2)));

        assertThrows(IllegalArgumentException.class, () -> sistema.altaPatrocinio(
                patrocinio("COD-2-" + sufijo, institucion, edicion, "General", 100_000f, 2)));
    }

    @Test
    void elCostoDeLosRegistrosGratuitosNoPuedeSuperarElVeintePorCientoDelAporte() throws Exception {
        String edicion = crearEdicion();
        String institucion = crearInstitucion("instTope");
        assertTrue(sistema.altaTipoRegistro(new DtTipoRegistro("General", "Tipo", 1000, 50), edicion));

        // 3 gratuitos de $1000 = $3000, más del 20 % de $10000 ($2000)
        assertThrows(IllegalArgumentException.class, () -> sistema.altaPatrocinio(
                patrocinio("COD-TOPE-" + sufijo, institucion, edicion, "General", 10_000f, 3)));
    }

    @Test
    void unPatrocinioExigeEdicionTipoEInstitucionExistentes() throws Exception {
        String edicion = crearEdicion();
        String institucion = crearInstitucion("instExiste");
        assertTrue(sistema.altaTipoRegistro(new DtTipoRegistro("General", "Tipo", 1000, 50), edicion));

        assertThrows(IllegalArgumentException.class, () -> sistema.altaPatrocinio(
                patrocinio("COD-A-" + sufijo, "noexiste" + sufijo, edicion, "General", 100_000f, 1)));
        assertThrows(IllegalArgumentException.class, () -> sistema.altaPatrocinio(
                patrocinio("COD-B-" + sufijo, institucion, "noexiste" + sufijo, "General", 100_000f, 1)));
        assertThrows(IllegalArgumentException.class, () -> sistema.altaPatrocinio(
                patrocinio("COD-C-" + sufijo, institucion, edicion, "NoExiste", 100_000f, 1)));
    }
}