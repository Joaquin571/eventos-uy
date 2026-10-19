package implementacion;

import datatypes.DtAsistente;
import datatypes.DtUsuario;
import interfaces.ISistema;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class LoginTest {

    private final ISistema sistema = Fabrica.getInstance().getISistema();
    private final String sufijo = String.valueOf(System.nanoTime());

    private DtAsistente asistente(String nick, String clave, String confirmacion) {
        DtAsistente dt = new DtAsistente(
                nick, "Ana", nick + "@mail.com",
                "Pérez", LocalDate.of(2000, 1, 1), null);
        dt.setContrasenia(clave);
        dt.setConfirmacionContrasenia(confirmacion);
        return dt;
    }

    @Test
    void loginPorNickname() {
        String nick = "login" + sufijo;
        assertTrue(sistema.altaAsistente(asistente(nick, "clave1", "clave1")));

        DtUsuario usuario = sistema.autenticar(nick, "clave1");

        assertEquals(nick, usuario.getNickname());
        assertNull(usuario.getContrasenia());
    }

    @Test
    void loginPorCorreo() {
        String nick = "correo" + sufijo;
        assertTrue(sistema.altaAsistente(asistente(nick, "clave1", "clave1")));

        assertEquals(nick, sistema.autenticar(nick + "@mail.com", "clave1").getNickname());
    }

    @Test
    void contraseniaIncorrectaSeRechaza() {
        String nick = "mala" + sufijo;
        assertTrue(sistema.altaAsistente(asistente(nick, "clave1", "clave1")));

        assertThrows(IllegalArgumentException.class,
                () -> sistema.autenticar(nick, "otra"));
    }

    @Test
    void altaConConfirmacionDistintaSeRechaza() {
        assertThrows(IllegalArgumentException.class,
                () -> sistema.altaAsistente(asistente("dist" + sufijo, "clave1", "clave2")));
    }

    @Test
    void cambiarContrasenia() {
        String nick = "cambio" + sufijo;
        assertTrue(sistema.altaAsistente(asistente(nick, "clave1", "clave1")));

        sistema.cambiarContrasenia(nick, "nueva1", "nueva1");

        assertEquals(nick, sistema.autenticar(nick, "nueva1").getNickname());
        assertThrows(IllegalArgumentException.class,
                () -> sistema.autenticar(nick, "clave1"));
    }
}