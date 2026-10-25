package implementacion;

import datatypes.DtAsistente;
import datatypes.DtOrganizador;
import datatypes.DtUsuario;
import interfaces.ISistema;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Alta, modificación y contraseña de usuarios. Necesitan la base de pruebas (eventosuy_test). */
class UsuarioTest {

    private final ISistema sistema = Fabrica.getInstance().getISistema();
    private final String sufijo = String.valueOf(System.nanoTime());

    private DtAsistente asistente(String nick, String correo, String clave, String confirmacion) {
        DtAsistente dt = new DtAsistente(nick, "Ana", correo, "Pérez", LocalDate.of(2000, 1, 1), null);
        dt.setContrasenia(clave);
        dt.setConfirmacionContrasenia(confirmacion);
        return dt;
    }

    private DtOrganizador organizador(String nick, String clave) {
        DtOrganizador dt = new DtOrganizador(nick, "Organizadora", nick + "@mail.com",
                "Descripción general", "https://ejemplo.com");
        dt.setContrasenia(clave);
        dt.setConfirmacionContrasenia(clave);
        return dt;
    }

    private String crearAsistente(String prefijo) {
        String nick = prefijo + sufijo;
        assertTrue(sistema.altaAsistente(asistente(nick, nick + "@mail.com", "clave1", "clave1")));
        return nick;
    }

    // ----------------------------------------------------------------- altas

    @Test
    void nicknameRepetidoSeRechaza() {
        String nick = crearAsistente("nickRep");

        assertThrows(IllegalArgumentException.class, () ->
                sistema.altaAsistente(asistente(nick, "otro" + sufijo + "@mail.com", "clave1", "clave1")));
    }

    @Test
    void correoRepetidoSeRechaza() {
        String nick = crearAsistente("correoRep");

        assertThrows(IllegalArgumentException.class, () ->
                sistema.altaAsistente(asistente("otro" + sufijo, nick + "@mail.com", "clave1", "clave1")));
    }

    @Test
    void laConfirmacionDebeCoincidirConLaContrasenia() {
        assertThrows(IllegalArgumentException.class, () ->
                sistema.altaAsistente(asistente("conf" + sufijo, "conf" + sufijo + "@mail.com", "clave1", "otra")));
    }

    @Test
    void laContraseniaEsObligatoria() {
        assertThrows(IllegalArgumentException.class, () ->
                sistema.altaAsistente(asistente("vacia" + sufijo, "vacia" + sufijo + "@mail.com", "", "")));
    }

    @Test
    void unOrganizadorPuedeIniciarSesionPorNicknameOCorreo() {
        String nick = "org" + sufijo;
        assertTrue(sistema.altaOrganizador(organizador(nick, "clave1")));

        assertEquals(nick, sistema.autenticar(nick, "clave1").getNickname());
        assertEquals(nick, sistema.autenticar(nick + "@mail.com", "clave1").getNickname());
    }

    @Test
    void lasConsultasNuncaDevuelvenLaContrasenia() {
        String nick = crearAsistente("sinClave");

        DtUsuario consultado = sistema.consultarUsuario(nick);

        assertNull(consultado.getContrasenia());
        assertNull(consultado.getConfirmacionContrasenia());
    }

    // ----------------------------------------------------------- modificación

    @Test
    void modificarCambiaLosDatosYConservaLaContrasenia() {
        String nick = crearAsistente("modOk");

        sistema.modificarAsistente(new DtAsistente(nick, "Nombre nuevo", nick + "@mail.com",
                "Apellido nuevo", LocalDate.of(1999, 5, 5), null));

        DtAsistente modificado = (DtAsistente) sistema.consultarUsuario(nick);

        assertEquals("Nombre nuevo", modificado.getNombre());
        assertEquals("Apellido nuevo", modificado.getApellido());
        // No se envió contraseña nueva: sigue valiendo la anterior
        assertEquals(nick, sistema.autenticar(nick, "clave1").getNickname());
    }

    @Test
    void modificarConContraseniaNuevaLaReemplaza() {
        String nick = crearAsistente("modClave");

        DtAsistente dt = new DtAsistente(nick, "Ana", nick + "@mail.com",
                "Pérez", LocalDate.of(2000, 1, 1), null);
        dt.setContrasenia("nueva1");
        dt.setConfirmacionContrasenia("nueva1");
        sistema.modificarAsistente(dt);

        assertEquals(nick, sistema.autenticar(nick, "nueva1").getNickname());
        assertThrows(IllegalArgumentException.class, () -> sistema.autenticar(nick, "clave1"));
    }

    @Test
    void modificarRechazaDatosInvalidos() {
        String nick = crearAsistente("modMal");
        String correo = nick + "@mail.com";

        assertThrows(IllegalArgumentException.class, () -> sistema.modificarAsistente(
                new DtAsistente(nick, " ", correo, "Pérez", LocalDate.of(2000, 1, 1), null)));
        assertThrows(IllegalArgumentException.class, () -> sistema.modificarAsistente(
                new DtAsistente(nick, "Ana", correo, "", LocalDate.of(2000, 1, 1), null)));
        assertThrows(IllegalArgumentException.class, () -> sistema.modificarAsistente(
                new DtAsistente(nick, "Ana", correo, "Pérez", LocalDate.now().plusDays(1), null)));
        assertThrows(IllegalArgumentException.class, () -> sistema.modificarAsistente(
                new DtAsistente(nick, "Ana", correo, "Pérez", LocalDate.of(2000, 1, 1), "NoExiste" + sufijo)));
    }

    @Test
    void modificarUnUsuarioInexistenteLanzaError() {
        assertThrows(IllegalArgumentException.class, () -> sistema.modificarAsistente(
                new DtAsistente("noexiste" + sufijo, "Ana", "x@mail.com", "Pérez",
                        LocalDate.of(2000, 1, 1), null)));
    }

    @Test
    void modificarUnOrganizadorExigeDescripcion() {
        String nick = "orgMod" + sufijo;
        assertTrue(sistema.altaOrganizador(organizador(nick, "clave1")));

        assertThrows(IllegalArgumentException.class, () -> sistema.modificarOrganizador(
                new DtOrganizador(nick, "Organizadora", nick + "@mail.com", " ", "")));
    }

    @Test
    void laContraseniaNuevaDebeCoincidirConSuConfirmacionAlModificar() {
        String nick = crearAsistente("modConf");

        DtAsistente dt = new DtAsistente(nick, "Ana", nick + "@mail.com",
                "Pérez", LocalDate.of(2000, 1, 1), null);
        dt.setContrasenia("nueva1");
        dt.setConfirmacionContrasenia("distinta");

        assertThrows(IllegalArgumentException.class, () -> sistema.modificarAsistente(dt));
        // Nada cambió: la contraseña original sigue valiendo
        assertEquals(nick, sistema.autenticar(nick, "clave1").getNickname());
    }
}