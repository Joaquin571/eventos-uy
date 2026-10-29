package web;

import datatypes.*;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import web.PatrociniosAutorizacionTest.Escenario;

class PatrociniosFlujoTest {
    private Escenario propio() {
        Escenario e = new Escenario(EstadoEdicion.ACEPTADA);
        e.autenticar(new DtOrganizador("duenio", "Dueño", "d@ejemplo.uy", "Descripción", ""));
        return e;
    }

    @Test
    void cargaSoloEdicionesPropiasDelEventoEnTodosLosEstados() throws Exception {
        Escenario e = propio();
        for (EstadoEdicion estado : EstadoEdicion.values()) {
            DtEdicion otra = edicion("Propia " + estado, "duenio", estado);
            e.ediciones.add(otra);
        }
        e.ediciones.add(edicion("Oculta ajena", "ajeno", EstadoEdicion.INGRESADA));
        e.parametros.clear(); e.parametros.put("evento", "Evento");
        new AltaPatrocinioServlet().doGet(e.request, e.response);
        List<DtEdicion> opciones = ediciones(e);
        assertEquals(4, opciones.size());
        assertTrue(opciones.stream().allMatch(d -> d.getNombreOrganizador().equals("duenio")));
        assertEquals(Set.of(EstadoEdicion.INGRESADA, EstadoEdicion.ACEPTADA, EstadoEdicion.RECHAZADA),
                new HashSet<>(opciones.stream().map(DtEdicion::getEstado).toList()));
        assertTrue(((Collection<?>) e.atributos.get("tipos")).isEmpty());
    }

    @Test
    void cargaSoloTiposDeLaEdicionSeleccionadaYResuelveElEvento() throws Exception {
        Escenario e = propio();
        e.tiposPorEdicion.put("Otra", List.of(new DtTipoRegistro("Privado", "", 1, 1)));
        new AltaPatrocinioServlet().doGet(e.request, e.response);
        assertEquals("Evento", form(e).get("evento"));
        List<DtTipoRegistro> tipos = (List<DtTipoRegistro>) e.atributos.get("tipos");
        assertEquals(List.of("General"), tipos.stream().map(DtTipoRegistro::getIdNombre).toList());
        assertFalse(form(e).containsKey("codigo"), "Los valores de alta no se leen por GET.");
    }

    @Test
    void cargarTiposDeEdicionAjenaDevuelve403() throws Exception {
        Escenario e = propio();
        e.ediciones.add(edicion("Ajena", "ajeno", EstadoEdicion.ACEPTADA));
        e.parametros.put("edicion", "Ajena");
        new AltaPatrocinioServlet().doGet(e.request, e.response);
        assertEquals(403, e.estado);
        assertFalse(e.atributos.containsKey("tipos"));
    }

    @Test
    void eventoSinEdicionesPropiasYEdicionSinTiposSeRepresentanVacios() throws Exception {
        Escenario e = propio();
        e.parametros.clear();e.parametros.put("evento", "Sin ediciones");
        new AltaPatrocinioServlet().doGet(e.request, e.response);
        assertTrue(ediciones(e).isEmpty());
        Escenario sinTipos = propio();sinTipos.tiposPorEdicion.clear();
        new AltaPatrocinioServlet().doGet(sinTipos.request, sinTipos.response);
        assertTrue(((Collection<?>) sinTipos.atributos.get("tipos")).isEmpty());
    }

    @Test
    void erroresNumericosConservanTodosLosCamposYNoCrean() throws Exception {
        for (String valor : List.of("NaN", "Infinity", "-Infinity", "0", "-1", "texto")) {
            Escenario e = propio();e.parametros.put("aporte", valor);
            new AltaPatrocinioServlet().doPost(e.request, e.response);
            assertEquals(0, e.altas);assertTrue(e.reenviado);
            assertEquals(valor, form(e).get("aporte"));
            assertEquals("NUEVO", form(e).get("codigo"));
            assertEquals("UTEC", form(e).get("institucion"));
            assertNotNull(e.atributos.get("error"));
        }
    }

    @Test
    void gratuitosInvalidosSeRechazanYSeConservan() throws Exception {
        for (String valor : List.of("-1", "1.5", "2147483648", "texto")) {
            Escenario e = propio();e.parametros.put("registrograt", valor);
            new AltaPatrocinioServlet().doPost(e.request, e.response);
            assertEquals(0, e.altas);assertEquals(valor, form(e).get("registrograt"));
        }
    }

    @Test
    void nivelInvalidoYCamposObligatoriosProducenMensajes() throws Exception {
        for (String campo : List.of("institucion", "nivel", "tipoRegistro", "aporte", "registrograt", "codigo")) {
            Escenario e = propio();e.parametros.remove(campo);
            new AltaPatrocinioServlet().doPost(e.request, e.response);
            assertEquals(0, e.altas);assertNotNull(e.atributos.get("error"));
        }
        Escenario e = propio();e.parametros.put("nivel", "DIAMANTE");
        new AltaPatrocinioServlet().doPost(e.request, e.response);
        assertTrue(e.atributos.get("error").toString().contains("nivel"));
    }

    @Test
    void tipoAjenoNoSeAceptaAunqueExistaEnOtraEdicion() throws Exception {
        Escenario e = propio();e.parametros.put("tipoRegistro", "VIP");
        e.tiposPorEdicion.put("Otra", List.of(new DtTipoRegistro("VIP", "", 100, 10)));
        new AltaPatrocinioServlet().doPost(e.request, e.response);
        assertEquals(0, e.altas);assertNotNull(e.atributos.get("error"));
    }

    @Test
    void eventoIncompatibleNoPermiteCrear() throws Exception {
        Escenario e = propio();e.parametros.put("evento", "Otro evento");
        new AltaPatrocinioServlet().doPost(e.request, e.response);
        assertEquals(0, e.altas);assertNotNull(e.atributos.get("error"));
        assertEquals("Edicion", form(e).get("edicion"));
    }

    @Test
    void erroresDeNegocioSeMuestranSinPerderElFormulario() throws Exception {
        for (String mensaje : List.of("La institución ya patrocina la edición.", "El código ya existe.", "Supera el 20 % del aporte.")) {
            Escenario e = propio();e.errorAlta = mensaje;
            new AltaPatrocinioServlet().doPost(e.request, e.response);
            assertEquals(mensaje, e.atributos.get("error"));
            assertEquals("NUEVO", form(e).get("codigo"));
            assertEquals("General", form(e).get("tipoRegistro"));
            assertEquals("5000", form(e).get("aporte"));
        }
    }

    @Test
    void ceroGratuitosYLimiteExactoLleganALaLogica() throws Exception {
        for (String cantidad : List.of("0", "10")) {
            Escenario e = propio();e.parametros.put("registrograt", cantidad);
            new AltaPatrocinioServlet().doPost(e.request, e.response);
            assertEquals(1, e.altas);assertEquals(302, e.estado);
        }
    }

    @Test
    void redireccionCodificaNombresSinExponerElCodigo() throws Exception {
        Escenario e = propio();e.ediciones.clear();
        String nombre = "Edición & música";
        e.ediciones.add(edicion(nombre, "duenio", EstadoEdicion.INGRESADA));
        e.tiposPorEdicion.put(nombre, List.of(new DtTipoRegistro("General", "", 1, 10)));
        e.parametros.put("edicion", nombre);e.parametros.put("institucion", "UTEC & ANTEL");
        new AltaPatrocinioServlet().doPost(e.request, e.response);
        assertTrue(e.redireccion.contains("Edici%C3%B3n+%26+m%C3%BAsica"));
        assertTrue(e.redireccion.contains("UTEC+%26+ANTEL"));
        assertFalse(e.redireccion.contains("NUEVO"));
    }

    @Test
    void consultaListaSoloAceptadasSinPublicarEdicionesOcultas() throws Exception {
        Escenario e = propio();e.parametros.clear();e.parametros.put("evento", "Evento");
        e.ediciones.add(edicion("Oculta propia", "duenio", EstadoEdicion.INGRESADA));
        e.ediciones.add(edicion("Oculta ajena", "ajeno", EstadoEdicion.RECHAZADA));
        new ConsultaPatrocinioServlet().doGet(e.request, e.response);
        assertEquals(List.of("Edicion"), ediciones(e).stream().map(DtEdicion::getIdNombre).toList());
    }

    @Test
    void selectorDePatrociniosSoloRecibeInstitucionesYFechaUsaDatoPersistido() throws Exception {
        Escenario e = propio();e.tieneSesion = false;
        e.patrocinio.setFecha(LocalDate.of(2026, 3, 7));
        new ConsultaPatrocinioServlet().doGet(e.request, e.response);
        assertEquals(List.of("UTEC"), e.atributos.get("patrocinadoras"));
        assertEquals("07/03/2026", e.atributos.get("fechaAlta"));
        assertNull(((DtPatrocinio)e.atributos.get("patrocinio")).getCodigoPatrocinio());
        assertEquals("Evento", e.atributos.get("eventoElegido"));
    }

    @Test
    void consultaSinPatrociniosNoEntregaDatosPrivados() throws Exception {
        Escenario e = propio();e.existePatrocinio = false;e.parametros.remove("institucion");
        new ConsultaPatrocinioServlet().doGet(e.request, e.response);
        assertEquals(List.of(), e.atributos.get("patrocinadoras"));assertEquals(200, e.estado);
    }

    @Test
    void edicionEnlazaPatrociniosSinEntregarCodigos() throws Exception {
        Escenario e = propio();e.parametros.put("nombre", "Edicion");
        new ConsultaEdicionServlet().doGet(e.request, e.response);
        assertEquals(List.of("UTEC"), e.atributos.get("patrocinadoras"));
        assertFalse(e.atributos.containsKey("patrocinios"));
    }

    private static DtEdicion edicion(String nombre, String organizador, EstadoEdicion estado) {
        LocalDate hoy = LocalDate.now();
        DtEdicion d = new DtEdicion(nombre, "ED", hoy, hoy, hoy, "Ciudad", "País", organizador);
        d.setEstado(estado);return d;
    }
    private static Map<String,String> form(Escenario e) { return (Map<String,String>) e.atributos.get("form"); }
    private static List<DtEdicion> ediciones(Escenario e) { return (List<DtEdicion>) e.atributos.get("ediciones"); }
}
