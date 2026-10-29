package implementacion;

import clases.NivelPatrocinio;
import datatypes.DtAsistente;
import datatypes.DtEdicion;
import datatypes.DtPatrocinio;
import datatypes.DtTipoRegistro;
import interfaces.ISistema;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

/** Casos de borde adicionales; ejecutarlos exclusivamente contra eventosuy_test. */
class ValidacionesAdicionalesTest {
    private final ISistema sistema = Fabrica.getInstance().getISistema();
    private final String unico = Long.toString(System.nanoTime());

    private DtAsistente asistente(String nick, String nombre, String correo) {
        DtAsistente dt = new DtAsistente(nick, nombre, correo, "Apellido", LocalDate.of(2000, 1, 1), null);
        dt.setContrasenia("clave");
        dt.setConfirmacionContrasenia("clave");
        return dt;
    }

    private DtPatrocinio patrocinio(String codigo, String institucion, String edicion,
                                   String tipo, NivelPatrocinio nivel, float monto, int gratis) {
        return new DtPatrocinio(LocalDate.now(), monto, gratis, codigo, nivel,
                institucion, edicion, tipo);
    }

    @Test void altaAsistenteNulo() {
        assertThrows(IllegalArgumentException.class, () -> sistema.altaAsistente(null));
    }
    @Test void altaOrganizadorNulo() {
        assertThrows(IllegalArgumentException.class, () -> sistema.altaOrganizador(null));
    }
    @Test void nicknameNulo() {
        assertThrows(IllegalArgumentException.class,
                () -> sistema.altaAsistente(asistente(null, "Ana", "ana" + unico + "@mail.com")));
    }
    @Test void nicknameBlanco() {
        assertThrows(IllegalArgumentException.class,
                () -> sistema.altaAsistente(asistente(" ", "Ana", "ana" + unico + "@mail.com")));
    }
    @Test void nombreNulo() {
        assertThrows(IllegalArgumentException.class,
                () -> sistema.altaAsistente(asistente("ana" + unico, null, "ana" + unico + "@mail.com")));
    }
    @Test void nombreBlanco() {
        assertThrows(IllegalArgumentException.class,
                () -> sistema.altaAsistente(asistente("ana" + unico, " ", "ana" + unico + "@mail.com")));
    }
    @Test void correoNulo() {
        assertThrows(IllegalArgumentException.class,
                () -> sistema.altaAsistente(asistente("ana" + unico, "Ana", null)));
    }
    @Test void correoSinArroba() {
        assertThrows(IllegalArgumentException.class,
                () -> sistema.altaAsistente(asistente("ana" + unico, "Ana", "correo-invalido")));
    }
    @Test void autenticarNulo() {
        assertThrows(IllegalArgumentException.class, () -> sistema.autenticar(null, "clave"));
    }
    @Test void autenticarBlanco() {
        assertThrows(IllegalArgumentException.class, () -> sistema.autenticar(" ", "clave"));
    }
    @Test void autenticarSinContrasenia() {
        assertThrows(IllegalArgumentException.class, () -> sistema.autenticar("usuario", null));
    }
    @Test void cambiarContraseniaNula() {
        assertThrows(IllegalArgumentException.class, () -> sistema.cambiarContrasenia("usuario", null, null));
    }
    @Test void cambiarContraseniaDistinta() {
        assertThrows(IllegalArgumentException.class, () -> sistema.cambiarContrasenia("usuario", "uno", "dos"));
    }
    @Test void edicionNula() {
        assertThrows(IllegalArgumentException.class, () -> sistema.altaEdicion(null, "evento"));
    }
    @Test void edicionSinNombre() {
        DtEdicion dt = new DtEdicion(" ", "ED", LocalDate.now(), LocalDate.now(),
                LocalDate.now(), "Montevideo", "Uruguay", "organizador");
        assertThrows(IllegalArgumentException.class, () -> sistema.altaEdicion(dt, "evento"));
    }
    @Test void tipoRegistroNulo() {
        assertThrows(IllegalArgumentException.class, () -> sistema.altaTipoRegistro(null, "edicion"));
    }
    @Test void tipoRegistroSinNombre() {
        assertThrows(IllegalArgumentException.class,
                () -> sistema.altaTipoRegistro(new DtTipoRegistro(" ", "Descripción", 10, 10), "edicion"));
    }
    @Test void patrocinioNulo() {
        assertThrows(IllegalArgumentException.class, () -> sistema.altaPatrocinio(null));
    }
    @Test void patrocinioSinCodigo() {
        assertThrows(IllegalArgumentException.class, () -> sistema.altaPatrocinio(
                patrocinio(" ", "institucion", "edicion", "tipo", NivelPatrocinio.ORO, 100, 1)));
    }
    @Test void patrocinioSinInstitucion() {
        assertThrows(IllegalArgumentException.class, () -> sistema.altaPatrocinio(
                patrocinio("codigo" + unico, " ", "edicion", "tipo", NivelPatrocinio.ORO, 100, 1)));
    }
    @Test void patrocinioSinEdicion() {
        assertThrows(IllegalArgumentException.class, () -> sistema.altaPatrocinio(
                patrocinio("codigo" + unico, "institucion", " ", "tipo", NivelPatrocinio.ORO, 100, 1)));
    }
    @Test void patrocinioSinTipo() {
        assertThrows(IllegalArgumentException.class, () -> sistema.altaPatrocinio(
                patrocinio("codigo" + unico, "institucion", "edicion", " ", NivelPatrocinio.ORO, 100, 1)));
    }
    @Test void patrocinioSinNivel() {
        assertThrows(IllegalArgumentException.class, () -> sistema.altaPatrocinio(
                patrocinio("codigo" + unico, "institucion", "edicion", "tipo", null, 100, 1)));
    }
    @Test void patrocinioAporteCero() {
        assertThrows(IllegalArgumentException.class, () -> sistema.altaPatrocinio(
                patrocinio("codigo" + unico, "institucion", "edicion", "tipo", NivelPatrocinio.ORO, 0, 1)));
    }
    @Test void patrocinioAporteInfinito() {
        assertThrows(IllegalArgumentException.class, () -> sistema.altaPatrocinio(
                patrocinio("codigo" + unico, "institucion", "edicion", "tipo", NivelPatrocinio.ORO, Float.POSITIVE_INFINITY, 1)));
    }
    @Test void patrocinioRegistrosNegativos() {
        assertThrows(IllegalArgumentException.class, () -> sistema.altaPatrocinio(
                patrocinio("codigo" + unico, "institucion", "edicion", "tipo", NivelPatrocinio.ORO, 100, -1)));
    }

    // Pruebas de consultas y persistencia de los manejadores.
    // No alteran eventosuy: la base se comprueba en @BeforeAll.
    @org.junit.jupiter.api.BeforeAll
    static void exigirBaseAislada() {
        org.junit.jupiter.api.Assumptions.assumeTrue(
                "eventosuy_test".equals(System.getenv("EVENTOS_DB_NAME")),
                "Se omiten pruebas de persistencia fuera de eventosuy_test");
    }

    @Test void consultasDeUsuarioInexistente() {
        String nick = "ausente" + unico;
        manejadores.ManejadorUsuarios m = manejadores.ManejadorUsuarios.getInstance();
        assertNull(m.obtenerUsuario(nick));
        assertNull(m.obtenerOrganizador(nick));
        assertNull(m.obtenerPorNicknameOCorreo(nick));
        assertFalse(m.existeUsuario(nick));
        assertFalse(m.existeCorreo(nick + "@example.test"));
        assertFalse(m.modificarOrganizador(nick, "Nuevo", "Desc", "https://example.test"));
        assertFalse(m.modificarAsistente(nick, "Nuevo", "Apellido", LocalDate.of(2001,1,1), null));
        assertFalse(m.cambiarContrasenia(nick, "hash-no-real"));
        assertNotNull(m.listarUsuarios());
    }

    @Test void consultasDeEventoYEdicionInexistentes() {
        String nombre = "ausente" + unico;
        manejadores.ManejadorEventos m = manejadores.ManejadorEventos.getInstance();
        assertNull(m.obtenerEvento(nombre));
        assertFalse(m.existeEvento(nombre));
        assertNull(m.obtenerEdicion(nombre));
        assertFalse(m.existeEdicion(nombre));
        assertNotNull(m.obtenerEventos());
        assertNotNull(m.obtenerEdicionesEvento(nombre));
        assertNotNull(m.obtenerEdicionesOrganizador(nombre));
        assertNull(m.obtenerCategoria(nombre));
        assertNotNull(m.obtenerCategorias());
        assertNull(m.obtenerTipoRegistro(nombre, nombre));
        assertFalse(m.existeTipoRegistro(nombre, nombre));
        assertNotNull(m.obtenerTiposRegistroEdicion(nombre));
    }

    @Test void consultasDeRegistrosInexistentes() {
        String nombre = "ausente" + unico;
        manejadores.ManejadorRegistros m = manejadores.ManejadorRegistros.getInstance();
        assertFalse(m.existeRegistroAsistenteEdicion(nombre, nombre));
        assertEquals(0L, m.contarRegistrosTipo(nombre, nombre));
        assertNotNull(m.obtenerRegistrosAsistente(nombre));
        assertNotNull(m.obtenerRegistrosEdicion(nombre));
        assertNull(m.obtenerRegistro(nombre, nombre));
    }

    @Test void consultasDePatrociniosInexistentes() {
        String nombre = "ausente" + unico;
        manejadores.ManejadorPatrocinios m = manejadores.ManejadorPatrocinios.getInstance();
        assertFalse(m.existePatrocinio(nombre));
        assertNull(m.obtenerPatrocinio(nombre));
        assertNull(m.obtenerPatrocinio(nombre, nombre));
        assertFalse(m.existePatrocinioInstitucionEdicion(nombre, nombre));
        assertNotNull(m.listarPatrocinios());
    }

    @Test void institucionSeGuardaConsultaYRechazaDuplicado() {
        String nombre = "InstitucionExtra" + unico;
        manejadores.ManejadorInstituciones m = manejadores.ManejadorInstituciones.getInstance();
        assertFalse(m.existeInstitucion(nombre));
        assertNull(m.obtenerInstitucion(nombre));
        assertTrue(m.addInstitucion(new clases.Institucion(nombre, "Institución de prueba", "https://example.test")));
        assertTrue(m.existeInstitucion(nombre));
        assertEquals(nombre, m.obtenerInstitucion(nombre).getNombre());
        assertTrue(m.listarInstituciones().stream().anyMatch(i -> nombre.equals(i.getNombre())));
        assertFalse(m.addInstitucion(new clases.Institucion(nombre, "Duplicada", "https://example.test")));
    }

    @Test void usuarioSeGuardaConsultaYDetectaDuplicado() {
        String nick = "consultaExtra" + unico;
        String correo = nick + "@example.test";
        assertTrue(sistema.altaAsistente(asistente(nick, "Ana", correo)));
        manejadores.ManejadorUsuarios m = manejadores.ManejadorUsuarios.getInstance();
        assertTrue(m.existeUsuario(nick));
        assertTrue(m.existeCorreo(correo.toUpperCase(java.util.Locale.ROOT)));
        assertEquals(nick, m.obtenerUsuario(nick).getNickname());
        assertEquals(nick, m.obtenerPorNicknameOCorreo(correo).getNickname());
        assertFalse(m.addUsuario(m.obtenerUsuario(nick)));
    }
}
