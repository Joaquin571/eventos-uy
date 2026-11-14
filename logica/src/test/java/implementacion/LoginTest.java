package implementacion;

import datatypes.DtAsistente;
import datatypes.DtUsuario;
import interfaces.ISistema;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class LoginTest extends soporte.PruebaConBaseSegura {

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

    @Test
    void identificadorCompartidoSeleccionaSoloLaContraseniaCoincidente() {
        String id = "compartido" + sufijo + "@test.uy";
        DtAsistente a = new DtAsistente(id, "A", "propioA" + sufijo + "@test.uy", "A", LocalDate.of(2000,1,1), null);
        a.setContrasenia("claveA"); a.setConfirmacionContrasenia("claveA");
        DtAsistente b = new DtAsistente("otro" + sufijo, "B", id, "B", LocalDate.of(2000,1,1), null);
        b.setContrasenia("claveB"); b.setConfirmacionContrasenia("claveB");
        assertTrue(sistema.altaAsistente(a)); assertTrue(sistema.altaAsistente(b));
        assertEquals(a.getNickname(), sistema.autenticar(id, "claveA").getNickname());
        assertEquals(b.getNickname(), sistema.autenticar(id, "claveB").getNickname());
        String error = assertThrows(IllegalArgumentException.class, () -> sistema.autenticar(id, "incorrecta")).getMessage();
        assertEquals(error, assertThrows(IllegalArgumentException.class,
                () -> sistema.autenticar("noExiste" + sufijo, "incorrecta")).getMessage());
    }

    @Test
    void identificadorYContraseniaAmbiguosSeRechazanSinElegirCuenta() {
        String id = "ambiguo" + sufijo + "@test.uy";
        DtAsistente a = new DtAsistente(id, "A", "propioIgual" + sufijo + "@test.uy", "A", LocalDate.of(2000,1,1), null);
        a.setContrasenia("igual"); a.setConfirmacionContrasenia("igual");
        DtAsistente b = new DtAsistente("ambB" + sufijo, "B", id, "B", LocalDate.of(2000,1,1), null);
        b.setContrasenia("igual"); b.setConfirmacionContrasenia("igual");
        assertTrue(sistema.altaAsistente(a)); assertTrue(sistema.altaAsistente(b));
        String error = assertThrows(IllegalArgumentException.class, () -> sistema.autenticar(id, "igual")).getMessage();
        assertEquals(error, assertThrows(IllegalArgumentException.class,
                () -> sistema.autenticar(id, "incorrecta")).getMessage());
        assertEquals(b.getNickname(), sistema.autenticar(b.getNickname(), "igual").getNickname());
    }

    @Test
    void identificadoresNoPersistiblesConservanElErrorGenerico() {
        String mensaje = assertThrows(IllegalArgumentException.class,
                () -> sistema.autenticar("inexistente" + sufijo, "clave")).getMessage();
        for (String id : java.util.List.of("x".repeat(256), "a\0b")) {
            assertEquals(mensaje, assertThrows(IllegalArgumentException.class,
                    () -> sistema.autenticar(id, "clave")).getMessage());
        }
    }
}