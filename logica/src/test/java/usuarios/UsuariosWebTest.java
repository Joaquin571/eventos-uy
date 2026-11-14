package usuarios;

import clases.NivelPatrocinio;
import datatypes.*;
import implementacion.Fabrica;
import interfaces.ISistema;
import org.junit.jupiter.api.Test;
import soporte.PruebaConBaseSegura;
import java.time.*;
import java.util.List;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class UsuariosWebTest extends PruebaConBaseSegura {
    private final ISistema sistema = Fabrica.getInstance().getISistema();
    private final ControladorUsuariosWeb controlador = new ControladorUsuariosWeb(sistema,
            new SesionesUsuarios(Clock.systemUTC(), Duration.ofMinutes(30)));
    private final String sufijo = UUID.randomUUID().toString().replace("-", "");

    private DtAsistente asistente(String prefijo) {
        String nick = prefijo + sufijo;
        DtAsistente dt = new DtAsistente(nick, "Ana", nick + "@test.uy", "Pérez", LocalDate.of(2000, 1, 1), null);
        dt.setContrasenia("clave"); dt.setConfirmacionContrasenia("clave");
        controlador.altaUsuario(dt, null);
        return dt;
    }
    private DtOrganizador organizador(String prefijo) {
        String nick = prefijo + sufijo;
        DtOrganizador dt = new DtOrganizador(nick, "Organizador", nick + "@test.uy", "Descripción", "");
        dt.setContrasenia("clave"); dt.setConfirmacionContrasenia("clave");
        controlador.altaUsuario(dt, null);
        return dt;
    }
    private String entrar(DtUsuario dt) { return controlador.iniciarSesion(dt.getNickname(), "clave", false).getToken(); }
    private String edicion(DtUsuario organizador, String evento, EstadoEdicion estado) {
        String nombre = "ed" + organizador.getNickname();
        DtEdicion dt = new DtEdicion(nombre, "ED", LocalDate.now().plusDays(1), LocalDate.now().plusDays(2),
                null, "San José", "Uruguay", organizador.getNickname());
        assertTrue(sistema.altaEdicion(dt, evento));
        if (estado == EstadoEdicion.ACEPTADA) sistema.aceptarRechazarEdicion(nombre, true);
        return nombre;
    }
    private String evento() throws Exception {
        String categoria = "cat" + sufijo;
        sistema.altaCategoria(categoria, null);
        String nombre = "evento" + sufijo;
        assertTrue(sistema.altaEvento(new DtEvento(nombre, "EV", "Evento", null, List.of(categoria))));
        return nombre;
    }

    @Test void disponibilidadYValidacionDefinitivaDeDuplicados() {
        DtAsistente ana = asistente("ana");
        assertFalse(controlador.disponibleNickname(ana.getNickname()));
        assertFalse(controlador.disponibleCorreo(ana.getCorreoElectronico().toUpperCase()));
        assertTrue(controlador.disponibleNickname("libre" + sufijo));
        assertFalse(controlador.disponibleCorreo("invalido"));
        assertFalse(controlador.disponibleNickname(" "));
        assertFalse(controlador.disponibleNickname("x".repeat(256)));
        assertThrows(IllegalArgumentException.class, () -> controlador.altaUsuario(ana, null));
    }
    @Test void autenticaPorCorreoYNoDevuelveContrasenias() {
        DtAsistente ana = asistente("ana");
        DtSesion ingreso = controlador.iniciarSesion(ana.getCorreoElectronico().toUpperCase(), "clave", true);
        assertEquals(ana.getNickname(), ingreso.getUsuario().getNickname());
        assertNull(ingreso.getUsuario().getContrasenia());
        assertNull(controlador.listarUsuarios().stream().filter(u -> u.getNickname().equals(ana.getNickname())).findFirst().orElseThrow().getConfirmacionContrasenia());
        controlador.cerrarSesion(ingreso.getToken());
        assertThrows(ErrorAccesoUsuarios.class, () -> controlador.usuarioActual(ingreso.getToken()));
        assertThrows(IllegalArgumentException.class, () -> controlador.iniciarSesion(ana.getNickname(), "otra", false));
    }
    @Test void organizadorNoPuedeIngresarAlModoMovil() {
        DtOrganizador org = organizador("org");
        assertThrows(IllegalArgumentException.class, () -> controlador.iniciarSesion(org.getNickname(), "clave", true));
        assertNotNull(controlador.iniciarSesion(org.getNickname(), "clave", false).getToken());
    }
    @Test void noModificaPerfilAjenoNiCambiaIdentidadOTipo() {
        DtAsistente ana = asistente("ana"), bob = asistente("bob");
        String token = entrar(ana);
        bob.setNombre("No guardar");
        assertThrows(ErrorAccesoUsuarios.class, () -> controlador.modificarUsuario(token, bob, null));
        assertEquals("Ana", sistema.consultarUsuario(bob.getNickname()).getNombre());
        assertThrows(ErrorAccesoUsuarios.class, () -> controlador.modificarUsuario(ana.getNickname(), ana, null));
        DtOrganizador falso = new DtOrganizador(ana.getNickname(), "Nombre", "otro@test.uy", "Descripción", "");
        assertThrows(IllegalArgumentException.class, () -> controlador.modificarUsuario(token, falso, null));
        ana.setCorreoElectronico("no-cambiar@test.uy"); ana.setNombre("Nombre nuevo");
        controlador.modificarUsuario(token, ana, null);
        assertNotEquals(ana.getCorreoElectronico(), sistema.consultarUsuario(ana.getNickname()).getCorreoElectronico());
        assertEquals("Nombre nuevo", sistema.consultarUsuario(ana.getNickname()).getNombre());
    }
    @Test void institucionRequiereOrganizadorYValidaURL() {
        DtAsistente ana = asistente("ana");
        DtOrganizador org = organizador("org");
        DtInstitucion dt = new DtInstitucion("inst" + sufijo, "Institución", "https://utec.test");
        assertThrows(ErrorAccesoUsuarios.class, () -> controlador.altaInstitucion(entrar(ana), dt, null));
        controlador.altaInstitucion(entrar(org), dt, null);
        assertEquals(dt.getNombre(), controlador.consultarInstitucion(dt.getNombre()).getNombre());
        assertThrows(IllegalArgumentException.class, () -> controlador.altaInstitucion(entrar(org), dt, null));
        assertThrows(IllegalArgumentException.class, () -> controlador.altaInstitucion(entrar(org),
                new DtInstitucion("otra" + sufijo, "Institución", "javascript:alert(1)"), null));
    }
    @Test void perfilOcultaEdicionesPrivadasYRegistrosAjenos() throws Exception {
        DtOrganizador org = organizador("org");
        String edicion = edicion(org, evento(), EstadoEdicion.INGRESADA);
        assertTrue(controlador.consultarPerfil(null, org.getNickname()).getEdiciones().isEmpty());
        assertEquals(edicion, controlador.consultarPerfil(entrar(org), org.getNickname()).getEdiciones().getFirst().getIdNombre());
        DtAsistente ana = asistente("ana");
        assertFalse(controlador.consultarPerfil(null, ana.getNickname()).isPropio());
        assertTrue(controlador.consultarPerfil(null, ana.getNickname()).getRegistros().isEmpty());
    }
    @Test void autorizacionPatrocinioYRedaccionDelCodigoOcurrenEnElServidor() throws Exception {
        DtOrganizador duenio = organizador("duenio"), ajeno = organizador("ajeno");
        String evento = evento(), edicion = edicion(duenio, evento, EstadoEdicion.ACEPTADA);
        assertTrue(sistema.altaTipoRegistro(new DtTipoRegistro("General", "Entrada", 10, 50), edicion));
        String institucion = "inst" + sufijo;
        sistema.altaInstitucion(new DtInstitucion(institucion, "Descripción", ""));
        DtPatrocinio patrocinio = new DtPatrocinio(LocalDate.of(1990, 1, 1), 1000, 2, "COD" + sufijo,
                NivelPatrocinio.values()[0], institucion, edicion, "General");
        assertThrows(ErrorAccesoUsuarios.class, () -> controlador.altaPatrocinio(entrar(ajeno), evento, patrocinio));
        assertThrows(ErrorAccesoUsuarios.class, () -> controlador.formularioPatrocinio(entrar(ajeno), evento, edicion));
        assertThrows(IllegalArgumentException.class, () -> controlador.altaPatrocinio(entrar(duenio), "otro", patrocinio));
        controlador.altaPatrocinio(entrar(duenio), evento, patrocinio);
        assertNull(controlador.consultarPatrocinio(null, "", edicion, institucion).getPatrocinio().getCodigoPatrocinio());
        assertNull(controlador.consultarPatrocinio(entrar(ajeno), "", edicion, institucion).getPatrocinio().getCodigoPatrocinio());
        DtPatrocinio visible = controlador.consultarPatrocinio(entrar(duenio), "", edicion, institucion).getPatrocinio();
        assertEquals(patrocinio.getCodigoPatrocinio(), visible.getCodigoPatrocinio());
        assertEquals(LocalDate.now(), visible.getFecha());
        assertThrows(IllegalArgumentException.class, () -> controlador.altaPatrocinio(entrar(duenio), evento, patrocinio));
    }
    @Test void noConsultaPatrociniosDeUnaEdicionIngresadaAjena() throws Exception {
        DtOrganizador org = organizador("org");
        String edicion = edicion(org, evento(), EstadoEdicion.INGRESADA);
        assertThrows(ErrorAccesoUsuarios.class, () -> controlador.consultarPatrocinio(null, "", edicion, ""));
        assertNotNull(controlador.consultarPatrocinio(entrar(org), "", edicion, ""));
    }
}