package web;

import clases.NivelPatrocinio;
import datatypes.*;
import interfaces.ISistema;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.*;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class PatrociniosAutorizacionTest {
    @Test
    void propietarioCreaEnLosTresEstadosUsandoSoloSuSesion() throws Exception {
        for (EstadoEdicion estado : EstadoEdicion.values()) {
            Escenario e = new Escenario(estado);
            e.autenticar(organizador("duenio"));
            e.parametros.put("organizador", "ajeno");
            new AltaPatrocinioServlet().doPost(e.request, e.response);
            assertEquals(1, e.altas);
            assertEquals(302, e.estado);
            assertEquals("Edicion", e.creado.getNombreEdicion());
        }
    }

    @Test
    void organizadorAjenoNoPuedeCrearAunqueManipuleLaIdentidad() throws Exception {
        for (EstadoEdicion estado : EstadoEdicion.values()) {
            Escenario e = new Escenario(estado);
            e.autenticar(organizador("ajeno"));
            e.parametros.put("organizador", "duenio");
            e.parametros.put("nickname", "duenio");
            e.parametros.put("usuario", "duenio");
            e.sesion.put("usuarioLogueado", organizador("duenio"));
            new AltaPatrocinioServlet().doPost(e.request, e.response);
            assertAltaRechazada(e);
        }
    }

    @Test
    void visitanteYAsistenteNoPuedenCrear() throws Exception {
        for (DtUsuario usuario : new DtUsuario[]{null, asistente("asistente"), asistente("duenio")}) {
            Escenario e = new Escenario(EstadoEdicion.ACEPTADA);
            if (usuario != null) e.autenticar(usuario);
            new AltaPatrocinioServlet().doPost(e.request, e.response);
            assertAltaRechazada(e);
        }
    }

    @Test
    void elAtributoDeSesionAntiguoNoAutorizaElAlta() throws Exception {
        Escenario e = new Escenario(EstadoEdicion.ACEPTADA);
        e.tieneSesion = true;
        e.sesion.put("usuarioLogueado", organizador("duenio"));
        new AltaPatrocinioServlet().doPost(e.request, e.response);
        assertAltaRechazada(e);
    }

    @Test
    void altaRechazaEdicionInexistenteVaciaOConOtraCapitalizacion() throws Exception {
        for (String nombre : new String[]{"Inexistente", "EDICION", "", " "}) {
            Escenario e = new Escenario(EstadoEdicion.ACEPTADA);
            e.autenticar(organizador("duenio"));
            e.parametros.put("edicion", nombre);
            new AltaPatrocinioServlet().doPost(e.request, e.response);
            assertAltaRechazada(e);
        }
    }

    @Test
    void elFormularioExigeOrganizador() throws Exception {
        Escenario e = new Escenario(EstadoEdicion.ACEPTADA);
        new AltaPatrocinioServlet().doGet(e.request, e.response);
        assertAltaRechazada(e);
    }

    @Test
    void tercerosConsultanAceptadasSinRecibirElCodigo() throws Exception {
        for (DtUsuario usuario : new DtUsuario[]{null, asistente("asistente"),
                organizador("ajeno"), asistente("duenio")}) {
            Escenario e = new Escenario(EstadoEdicion.ACEPTADA);
            if (usuario != null) e.autenticar(usuario);
            new ConsultaPatrocinioServlet().doGet(e.request, e.response);
            assertPublicoSinCodigo(e);
        }
    }

    @Test
    void propietarioDeSesionVeElCodigoEnLosTresEstados() throws Exception {
        for (EstadoEdicion estado : EstadoEdicion.values()) {
            Escenario e = new Escenario(estado);
            e.autenticar(organizador("duenio"));
            new ConsultaPatrocinioServlet().doGet(e.request, e.response);
            assertEquals(200, e.estado);
            assertTrue(e.reenviado);
            assertEquals(true, e.atributos.get("esOrganizadorDuenio"));
            assertEquals("SECRETO-UTEC", ((DtPatrocinio) e.atributos.get("patrocinio")).getCodigoPatrocinio());
        }
    }

    @Test
    void tercerosNoPuedenConsultarEdicionesOcultas() throws Exception {
        for (EstadoEdicion estado : new EstadoEdicion[]{EstadoEdicion.INGRESADA, EstadoEdicion.RECHAZADA}) {
            for (DtUsuario usuario : new DtUsuario[]{null, asistente("asistente"), organizador("ajeno")}) {
                Escenario e = new Escenario(estado);
                if (usuario != null) e.autenticar(usuario);
                new ConsultaPatrocinioServlet().doGet(e.request, e.response);
                assertOculto(e);
                assertEquals(0, e.consultas);
            }
        }
    }

    @Test
    void parametrosYElAtributoAntiguoNoSuplantanAlDuenioEnConsulta() throws Exception {
        Escenario e = new Escenario(EstadoEdicion.ACEPTADA);
        e.autenticar(organizador("ajeno"));
        e.sesion.put("usuarioLogueado", organizador("duenio"));
        e.parametros.put("usuario", "duenio");
        e.parametros.put("organizador", "duenio");
        e.parametros.put("esOrganizadorDuenio", "true");
        new ConsultaPatrocinioServlet().doGet(e.request, e.response);
        assertPublicoSinCodigo(e);
    }

    @Test
    void elAtributoAntiguoSoloNoAutorizaUnaEdicionOculta() throws Exception {
        Escenario e = new Escenario(EstadoEdicion.INGRESADA);
        e.tieneSesion = true;
        e.sesion.put("usuarioLogueado", organizador("duenio"));
        new ConsultaPatrocinioServlet().doGet(e.request, e.response);
        assertOculto(e);
        assertEquals(0, e.consultas);
    }

    @Test
    void consultaDeEdicionInexistenteOConOtraCapitalizacionDevuelve404() throws Exception {
        for (String nombre : new String[]{"Inexistente", "EDICION"}) {
            Escenario e = new Escenario(EstadoEdicion.ACEPTADA);
            e.autenticar(organizador("duenio"));
            e.parametros.put("edicion", nombre);
            new ConsultaPatrocinioServlet().doGet(e.request, e.response);
            assertOculto(e);
            assertEquals(0, e.consultas);
        }
    }

    @Test
    void edicionOcultaTambienSeProtegeSinInstitucion() throws Exception {
        Escenario e = new Escenario(EstadoEdicion.RECHAZADA);
        e.parametros.remove("institucion");
        new ConsultaPatrocinioServlet().doGet(e.request, e.response);
        assertOculto(e);
    }

    @Test
    void patrocinioInexistenteDevuelve404() throws Exception {
        Escenario e = new Escenario(EstadoEdicion.ACEPTADA);
        e.existePatrocinio = false;
        new ConsultaPatrocinioServlet().doGet(e.request, e.response);
        assertOculto(e);
    }

    @Test
    void consultaSinSeleccionSigueSiendoPublica() throws Exception {
        Escenario e = new Escenario(EstadoEdicion.ACEPTADA);
        e.parametros.clear();
        new ConsultaPatrocinioServlet().doGet(e.request, e.response);
        assertEquals(200, e.estado);
        assertTrue(e.reenviado);
        assertEquals(0, e.consultas);
    }

    private static void assertAltaRechazada(Escenario e) {
        assertEquals(403, e.estado);
        assertEquals(0, e.altas);
        assertFalse(e.reenviado);
    }

    private static void assertOculto(Escenario e) {
        assertEquals(404, e.estado);
        assertFalse(e.reenviado);
        assertFalse(e.atributos.containsKey("patrocinio"));
    }

    private static void assertPublicoSinCodigo(Escenario e) {
        assertEquals(200, e.estado);
        assertTrue(e.reenviado);
        assertEquals(false, e.atributos.get("esOrganizadorDuenio"));
        DtPatrocinio visible = (DtPatrocinio) e.atributos.get("patrocinio");
        assertNotNull(visible);
        assertNull(visible.getCodigoPatrocinio());
        assertEquals("UTEC", visible.getNombreInstituto());
        assertEquals("General", visible.getNombreTipoRegistro());
        assertEquals(5000f, visible.getMontoAporte());
        assertEquals("SECRETO-UTEC", e.patrocinio.getCodigoPatrocinio(), "El DTO original debe conservarse.");
    }

    private static DtOrganizador organizador(String nick) {
        return new DtOrganizador(nick, nick, nick + "@ejemplo.uy", "Descripci?n", "");
    }

    private static DtAsistente asistente(String nick) {
        return new DtAsistente(nick, nick, nick + "@ejemplo.uy", "Apellido", LocalDate.of(2000, 1, 1), null);
    }

    // Dobles estrictos de las interfaces existentes; no necesitan base ni bibliotecas de mocks.
    static final class Escenario {
        final Map<String, String> parametros = new HashMap<>();
        final Map<String, Object> atributos = new HashMap<>();
        final Map<String, Object> sesion = new HashMap<>();
        final List<DtEdicion> ediciones = new ArrayList<>();
        final List<DtEvento> eventos = new ArrayList<>(List.of(new DtEvento("Evento", "EV", "Descripción", LocalDate.now(), List.of())));
        final Map<String, List<DtEdicion>> edicionesPorEvento = new HashMap<>();
        final Map<String, List<DtTipoRegistro>> tiposPorEdicion = new HashMap<>();
        String errorAlta;
        String redireccion;
        boolean tieneSesion;
        int estado = 200;
        int altas;
        int consultas;
        boolean reenviado;
        DtPatrocinio creado;
        boolean existePatrocinio = true;
        final DtPatrocinio patrocinio = new DtPatrocinio(LocalDate.now(), 5000, 1,
                "SECRETO-UTEC", NivelPatrocinio.ORO, "UTEC", "Edicion", "General");
        final ISistema sistema = doble(ISistema.class, (p, m, a) -> switch (m.getName()) {
            case "listarEventos" -> eventos;
            case "consultarUsuario" -> organizador((String) a[0]);
            case "obtenerImagen" -> null;
            case "obtenerRegistrosEdicion" -> List.of();
            case "obtenerEdicionesEvento" -> edicionesPorEvento.getOrDefault(a[0], List.of());
            case "obtenerEdicionesOrganizador" -> ediciones.stream()
                    .filter(e -> Objects.equals(e.getNombreOrganizador(), a[0])).toList();
            case "listarInstituciones" -> List.of(new DtInstitucion("UTEC", "Institución", "https://utec.edu.uy"));
            case "obtenerTiposRegistroEdicion" -> tiposPorEdicion.getOrDefault(a[0], List.of());
            case "consultarTipoRegistro" -> tiposPorEdicion.getOrDefault(a[0], List.of()).stream()
                    .filter(t -> t.getIdNombre().equals(a[1])).findFirst().orElse(null);
            case "obtenerPatrociniosEdicion" -> existePatrocinio ? List.of(patrocinio) : List.of();
            case "altaPatrocinio" -> {
                if (errorAlta != null) throw new IllegalArgumentException(errorAlta);
                altas++; creado = (DtPatrocinio) a[0]; yield true;
            }
            case "consultarPatrocinio" -> { consultas++; yield existePatrocinio ? patrocinio : null; }
            default -> throw new AssertionError("Llamada inesperada a ISistema: " + m.getName());
        });
        final ServletContext contexto = doble(ServletContext.class, (p, m, a) -> {
            if (m.getName().equals("getAttribute") && a[0].equals("sistema")) return sistema;
            throw new AssertionError("Llamada inesperada al contexto: " + m.getName());
        });
        final HttpSession httpSesion = doble(HttpSession.class, (p, m, a) -> {
            if (m.getName().equals("getAttribute")) return sesion.get(a[0]);
            throw new AssertionError("Llamada inesperada a la sesi?n: " + m.getName());
        });
        final RequestDispatcher despachador = doble(RequestDispatcher.class, (p, m, a) -> {
            if (m.getName().equals("forward")) { reenviado = true; return null; }
            throw new AssertionError("Llamada inesperada al despachador: " + m.getName());
        });
        final HttpServletRequest request = doble(HttpServletRequest.class, (p, m, a) -> switch (m.getName()) {
            case "getSession" -> {
                if (a == null || a.length != 1 || !Boolean.FALSE.equals(a[0]))
                    throw new AssertionError("No se debe crear una sesi?n para autorizar.");
                yield tieneSesion ? httpSesion : null;
            }
            case "getServletContext" -> contexto;
            case "getParameter" -> parametros.get(a[0]);
            case "setAttribute" -> { atributos.put((String) a[0], a[1]); yield null; }
            case "getAttribute" -> atributos.get(a[0]);
            case "getContextPath" -> "/eventos";
            case "getRequestDispatcher" -> despachador;
            default -> throw new AssertionError("Llamada inesperada a la petici?n: " + m.getName());
        });
        final HttpServletResponse response = doble(HttpServletResponse.class, (p, m, a) -> switch (m.getName()) {
            case "sendError" -> { estado = (Integer) a[0]; yield null; }
            case "sendRedirect" -> { estado = 302; redireccion = (String) a[0]; yield null; }
            default -> throw new AssertionError("Llamada inesperada a la respuesta: " + m.getName());
        });

        Escenario(EstadoEdicion estado) {
            LocalDate hoy = LocalDate.now();
            DtEdicion edicion = new DtEdicion("Edicion", "ED", hoy, hoy, hoy, "Montevideo", "Uruguay", "duenio");
            edicion.setEstado(estado);
            ediciones.add(edicion);
            edicionesPorEvento.put("Evento", ediciones);
            tiposPorEdicion.put("Edicion", List.of(new DtTipoRegistro("General", "General", 100, 50)));
            parametros.putAll(Map.of("edicion", "Edicion", "institucion", "UTEC", "nivel", "ORO",
                    "tipoRegistro", "General", "aporte", "5000", "registrograt", "1", "codigo", "NUEVO"));
        }

        void autenticar(DtUsuario usuario) {
            tieneSesion = true;
            sesion.put("usuario", usuario);
        }
    }

    private static <T> T doble(Class<T> tipo, InvocationHandler manejador) {
        return tipo.cast(Proxy.newProxyInstance(tipo.getClassLoader(), new Class<?>[]{tipo}, manejador));
    }
}
