package implementacion;

import clases.NivelPatrocinio;
import datatypes.DtAsistente;
import datatypes.DtEdicion;
import datatypes.DtEvento;
import datatypes.DtInstitucion;
import datatypes.DtOrganizador;
import datatypes.DtPatrocinio;
import datatypes.DtRegistro;
import datatypes.DtTipoRegistro;
import interfaces.ISistema;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Registro a edición (sección 7.5): general, con código de patrocinio válido, código de otra
 * institución, de otro tipo o de otra edición, código sin usos, cupo agotado y asistente ya
 * registrado. Necesitan la base de pruebas (eventosuy_test).
 */
class RegistroTest {

    private final ISistema sistema = Fabrica.getInstance().getISistema();
    private final String sufijo = String.valueOf(System.nanoTime());
    private final LocalDate hoy = LocalDate.now();

    // ------------------------------------------------------------ preparación

    private String organizador;
    private String evento;
    private int contadorEdiciones = 0;

    /** Crea (una sola vez por prueba) el organizador y el evento que usan las ediciones. */
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

    /** Edición del evento, ya Aceptada, con un tipo de registro. Devuelve el nombre de la edición. */
    private String crearEdicion(String tipo, float costo, int cupo) throws Exception {
        prepararEvento();

        String edicion = "edicion" + (++contadorEdiciones) + "_" + sufijo;

        assertTrue(sistema.altaEdicion(new DtEdicion(edicion, "ED" + contadorEdiciones,
                hoy.plusDays(10), hoy.plusDays(12), hoy, "Montevideo", "Uruguay", organizador), evento));
        sistema.aceptarRechazarEdicion(edicion, true);

        agregarTipo(edicion, tipo, costo, cupo);

        return edicion;
    }

    private void agregarTipo(String edicion, String tipo, float costo, int cupo) {
        assertTrue(sistema.altaTipoRegistro(
                new DtTipoRegistro(tipo, "Tipo " + tipo, costo, cupo), edicion));
    }

    private String crearInstitucion(String prefijo) {
        String nombre = prefijo + sufijo;
        assertTrue(sistema.altaInstitucion(
                new DtInstitucion(nombre, "Institución de prueba", "https://ejemplo.com")));
        return nombre;
    }

    /** Asistente con la institución indicada (null = sin institución). */
    private String crearAsistente(String prefijo, String institucion) {
        String nick = prefijo + sufijo;

        DtAsistente dt = new DtAsistente(nick, "Ana", nick + "@mail.com",
                "Pérez", LocalDate.of(2000, 1, 1), institucion);
        dt.setContrasenia("clave1");
        dt.setConfirmacionContrasenia("clave1");

        assertTrue(sistema.altaAsistente(dt));
        return nick;
    }

    /** Patrocinio de la institución para (edición, tipo). El aporte es alto para cumplir el tope del 20 %. */
    private void crearPatrocinio(String codigo, String institucion, String edicion, String tipo,
                                 int gratuitos) {
        assertTrue(sistema.altaPatrocinio(new DtPatrocinio(hoy, 1_000_000f, gratuitos, codigo,
                NivelPatrocinio.ORO, institucion, edicion, tipo)));
    }

    private boolean registrar(String asistente, String edicion, String tipo, String codigo) {
        return sistema.registroAEdicion(asistente, edicion, tipo, new DtRegistro(hoy, 0), codigo);
    }

    /** Ejecuta la acción, comprueba que la lógica la rechaza y devuelve el motivo. */
    private String motivoDelRechazo(org.junit.jupiter.api.function.Executable accion) {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, accion);
        assertNotNull(e.getMessage());
        return e.getMessage();
    }

    private DtRegistro registroDe(String asistente, String edicion) {
        for (DtRegistro registro : sistema.obtenerRegistrosEdicion(edicion)) {
            if (asistente.equals(registro.getNicknameAsistente())) {
                return registro;
            }
        }
        return null;
    }

    // ----------------------------------------------------------- registro general

    @Test
    void registroGeneralCobraElCostoDelTipo() throws Exception {
        String edicion = crearEdicion("General", 1500, 10);
        String asistente = crearAsistente("gen", null);

        assertTrue(registrar(asistente, edicion, "General", null));

        DtRegistro registro = registroDe(asistente, edicion);

        assertNotNull(registro);
        assertEquals(1500f, registro.getCosto());
        assertEquals("General", registro.getNombreTipoRegistro());
        assertEquals(edicion, registro.getNombreEdicion());
        assertEquals(hoy, registro.getFechaRegistro());
        assertFalse(registro.isUsoCodigo());
    }

    @Test
    void elCostoLoFijaLaLogicaAunqueElDtTraigaOtro() throws Exception {
        String edicion = crearEdicion("General", 900, 10);
        String asistente = crearAsistente("costo", null);

        assertTrue(sistema.registroAEdicion(asistente, edicion, "General",
                new DtRegistro(hoy, 1), null));

        assertEquals(900f, registroDe(asistente, edicion).getCosto());
    }

    @Test
    void unCodigoEnBlancoEsUnRegistroGeneral() throws Exception {
        String edicion = crearEdicion("General", 700, 10);
        String asistente = crearAsistente("blanco", null);

        assertTrue(registrar(asistente, edicion, "General", "   "));

        DtRegistro registro = registroDe(asistente, edicion);
        assertEquals(700f, registro.getCosto());
        assertFalse(registro.isUsoCodigo());
    }

    // ----------------------------------------------------- código de patrocinio válido

    @Test
    void conCodigoValidoElRegistroEsGratis() throws Exception {
        String institucion = crearInstitucion("instA");
        String edicion = crearEdicion("General", 1500, 10);
        String asistente = crearAsistente("conCodigo", institucion);
        crearPatrocinio("COD" + sufijo, institucion, edicion, "General", 2);

        assertTrue(registrar(asistente, edicion, "General", "COD" + sufijo));

        DtRegistro registro = registroDe(asistente, edicion);

        assertEquals(0f, registro.getCosto());
        assertTrue(registro.isUsoCodigo());
        assertEquals(asistente, registro.getNicknameAsistente());
    }

    @Test
    void elCodigoSeAdmiteConEspaciosAlrededor() throws Exception {
        String institucion = crearInstitucion("instEsp");
        String edicion = crearEdicion("General", 1500, 10);
        String asistente = crearAsistente("espacios", institucion);
        crearPatrocinio("ESP" + sufijo, institucion, edicion, "General", 1);

        assertTrue(registrar(asistente, edicion, "General", "  ESP" + sufijo + "  "));
        assertTrue(registroDe(asistente, edicion).isUsoCodigo());
    }

    @Test
    void cadaUsoDelCodigoDescuentaUnRegistroGratuito() throws Exception {
        String institucion = crearInstitucion("instUsos");
        String edicion = crearEdicion("General", 1500, 10);
        String primero = crearAsistente("uso1", institucion);
        String segundo = crearAsistente("uso2", institucion);
        crearPatrocinio("USOS" + sufijo, institucion, edicion, "General", 2);

        assertTrue(registrar(primero, edicion, "General", "USOS" + sufijo));
        assertTrue(registrar(segundo, edicion, "General", "USOS" + sufijo));

        assertTrue(registroDe(primero, edicion).isUsoCodigo());
        assertTrue(registroDe(segundo, edicion).isUsoCodigo());
    }

    // ------------------------------------------------------- código que no corresponde

    @Test
    void elCodigoDeOtraInstitucionSeRechaza() throws Exception {
        String patrocinadora = crearInstitucion("instP");
        String otra = crearInstitucion("instO");
        String edicion = crearEdicion("General", 1500, 10);
        String asistente = crearAsistente("otraInst", otra);
        crearPatrocinio("INST" + sufijo, patrocinadora, edicion, "General", 2);

        String motivo = motivoDelRechazo(() -> registrar(asistente, edicion, "General", "INST" + sufijo));

        assertTrue(motivo.contains("institución"));
        assertNull(registroDe(asistente, edicion));
    }

    @Test
    void elCodigoDeOtroTipoDeRegistroSeRechaza() throws Exception {
        String institucion = crearInstitucion("instT");
        String edicion = crearEdicion("General", 1500, 10);
        agregarTipo(edicion, "VIP", 3000, 10);
        String asistente = crearAsistente("otroTipo", institucion);
        crearPatrocinio("TIPO" + sufijo, institucion, edicion, "General", 2);

        String motivo = motivoDelRechazo(() -> registrar(asistente, edicion, "VIP", "TIPO" + sufijo));

        assertTrue(motivo.contains("tipo de registro"));
        assertNull(registroDe(asistente, edicion));
    }

    @Test
    void elCodigoDeOtraEdicionSeRechaza() throws Exception {
        String institucion = crearInstitucion("instE");
        String edicionConCodigo = crearEdicion("General", 1500, 10);
        String edicionElegida = crearEdicion("General", 1500, 10);
        String asistente = crearAsistente("otraEd", institucion);
        crearPatrocinio("EDIC" + sufijo, institucion, edicionConCodigo, "General", 2);

        String motivo = motivoDelRechazo(() ->
                registrar(asistente, edicionElegida, "General", "EDIC" + sufijo));

        assertTrue(motivo.contains("edición"));
        assertNull(registroDe(asistente, edicionElegida));
    }

    @Test
    void unCodigoInexistenteSeRechaza() throws Exception {
        String institucion = crearInstitucion("instI");
        String edicion = crearEdicion("General", 1500, 10);
        String asistente = crearAsistente("inexistente", institucion);

        String motivo = motivoDelRechazo(() -> registrar(asistente, edicion, "General", "NOEXISTE" + sufijo));

        assertTrue(motivo.contains("no existe"));
        assertNull(registroDe(asistente, edicion));
    }

    @Test
    void unAsistenteSinInstitucionSoloPuedeRegistrarseDeFormaGeneral() throws Exception {
        String institucion = crearInstitucion("instS");
        String edicion = crearEdicion("General", 1500, 10);
        String asistente = crearAsistente("sinInst", null);
        crearPatrocinio("SIN" + sufijo, institucion, edicion, "General", 2);

        String motivo = motivoDelRechazo(() -> registrar(asistente, edicion, "General", "SIN" + sufijo));

        assertTrue(motivo.contains("sin institución"));
        assertNull(registroDe(asistente, edicion));

        // De forma general sí puede registrarse
        assertTrue(registrar(asistente, edicion, "General", null));
        assertEquals(1500f, registroDe(asistente, edicion).getCosto());
    }

    // ------------------------------------------------------------ código sin usos

    @Test
    void elCodigoSinUsosDisponiblesSeRechaza() throws Exception {
        String institucion = crearInstitucion("instU");
        String edicion = crearEdicion("General", 1500, 10);
        String primero = crearAsistente("sinUsos1", institucion);
        String segundo = crearAsistente("sinUsos2", institucion);
        crearPatrocinio("UNO" + sufijo, institucion, edicion, "General", 1);

        assertTrue(registrar(primero, edicion, "General", "UNO" + sufijo));

        String motivo = motivoDelRechazo(() -> registrar(segundo, edicion, "General", "UNO" + sufijo));

        assertTrue(motivo.contains("agotaron"));
        assertNull(registroDe(segundo, edicion));
    }

    @Test
    void conCodigoAgotadoElAsistenteSigueTeniendoElRegistroGeneral() throws Exception {
        String institucion = crearInstitucion("instG");
        String edicion = crearEdicion("General", 1500, 10);
        String primero = crearAsistente("agotado1", institucion);
        String segundo = crearAsistente("agotado2", institucion);
        crearPatrocinio("AGO" + sufijo, institucion, edicion, "General", 1);

        assertTrue(registrar(primero, edicion, "General", "AGO" + sufijo));
        motivoDelRechazo(() -> registrar(segundo, edicion, "General", "AGO" + sufijo));

        // Tras el rechazo puede cambiar la selección y registrarse de forma general
        assertTrue(registrar(segundo, edicion, "General", null));
        assertEquals(1500f, registroDe(segundo, edicion).getCosto());
    }

    // ---------------------------------------------------------------- cupo agotado

    @Test
    void elCupoAgotadoSeRechaza() throws Exception {
        String edicion = crearEdicion("Unico", 500, 1);
        String primero = crearAsistente("cupo1", null);
        String segundo = crearAsistente("cupo2", null);

        assertTrue(registrar(primero, edicion, "Unico", null));

        String motivo = motivoDelRechazo(() -> registrar(segundo, edicion, "Unico", null));

        assertTrue(motivo.contains("cupo"));
        assertNull(registroDe(segundo, edicion));
    }

    @Test
    void elCupoAgotadoTambienRigeParaLosRegistrosConCodigo() throws Exception {
        String institucion = crearInstitucion("instC");
        String edicion = crearEdicion("Unico", 500, 1);
        String primero = crearAsistente("cupoC1", null);
        String segundo = crearAsistente("cupoC2", institucion);
        crearPatrocinio("CUPO" + sufijo, institucion, edicion, "Unico", 3);

        assertTrue(registrar(primero, edicion, "Unico", null));

        String motivo = motivoDelRechazo(() -> registrar(segundo, edicion, "Unico", "CUPO" + sufijo));

        assertTrue(motivo.contains("cupo"));
    }

    // ------------------------------------------------------ asistente ya registrado

    @Test
    void unAsistenteNoPuedeRegistrarseDosVecesEnLaMismaEdicion() throws Exception {
        String edicion = crearEdicion("General", 1500, 10);
        agregarTipo(edicion, "VIP", 3000, 10);
        String asistente = crearAsistente("repetido", null);

        assertTrue(registrar(asistente, edicion, "General", null));

        // Ni con el mismo tipo ni con otro
        assertTrue(motivoDelRechazo(() -> registrar(asistente, edicion, "General", null))
                .contains("ya está registrado"));
        assertTrue(motivoDelRechazo(() -> registrar(asistente, edicion, "VIP", null))
                .contains("ya está registrado"));

        assertEquals(1, sistema.obtenerRegistrosEdicion(edicion).size());
    }

    @Test
    void repetirElRegistroConElMismoCodigoDiceQueYaEstaRegistrado() throws Exception {
        String institucion = crearInstitucion("instR");
        String edicion = crearEdicion("General", 1500, 10);
        String asistente = crearAsistente("repiteCodigo", institucion);
        crearPatrocinio("REP" + sufijo, institucion, edicion, "General", 1);

        assertTrue(registrar(asistente, edicion, "General", "REP" + sufijo));

        // El código ya no tiene usos, pero el motivo real es que ya está registrado
        String motivo = motivoDelRechazo(() -> registrar(asistente, edicion, "General", "REP" + sufijo));

        assertTrue(motivo.contains("ya está registrado"));
    }

    @Test
    void elMismoAsistentePuedeRegistrarseEnOtraEdicion() throws Exception {
        String primera = crearEdicion("General", 1500, 10);
        String segunda = crearEdicion("General", 1500, 10);
        String asistente = crearAsistente("variasEd", null);

        assertTrue(registrar(asistente, primera, "General", null));
        assertTrue(registrar(asistente, segunda, "General", null));
    }

    // -------------------------------------------------------------- consultas

    @Test
    void obtenerRegistrosEdicionIncluyeAlAsistenteYSiUsoCodigo() throws Exception {
        String institucion = crearInstitucion("instQ");
        String edicion = crearEdicion("General", 1500, 10);
        String conCodigo = crearAsistente("qCodigo", institucion);
        String general = crearAsistente("qGeneral", null);
        crearPatrocinio("QRY" + sufijo, institucion, edicion, "General", 1);

        assertTrue(registrar(conCodigo, edicion, "General", "QRY" + sufijo));
        assertTrue(registrar(general, edicion, "General", null));

        Collection<DtRegistro> registros = sistema.obtenerRegistrosEdicion(edicion);

        assertEquals(2, registros.size());
        assertTrue(registroDe(conCodigo, edicion).isUsoCodigo());
        assertFalse(registroDe(general, edicion).isUsoCodigo());
    }

    @Test
    void obtenerRegistrosAsistenteIncluyeAlAsistenteYSiUsoCodigo() throws Exception {
        String institucion = crearInstitucion("instM");
        String edicion = crearEdicion("General", 1500, 10);
        String asistente = crearAsistente("misRegistros", institucion);
        crearPatrocinio("MIS" + sufijo, institucion, edicion, "General", 1);

        assertTrue(registrar(asistente, edicion, "General", "MIS" + sufijo));

        Collection<DtRegistro> registros = sistema.obtenerRegistrosAsistente(asistente);

        assertEquals(1, registros.size());

        DtRegistro registro = registros.iterator().next();

        assertEquals(asistente, registro.getNicknameAsistente());
        assertEquals(edicion, registro.getNombreEdicion());
        assertTrue(registro.isUsoCodigo());
    }

    @Test
    void laConsultaDeUnAsistenteSinRegistrosEstaVacia() {
        String asistente = crearAsistente("sinRegistros", null);

        assertTrue(sistema.obtenerRegistrosAsistente(asistente).isEmpty());
    }
}