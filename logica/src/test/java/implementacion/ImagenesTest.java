package implementacion;

import datatypes.DtAsistente;
import datatypes.DtImagen;
import datatypes.DtInstitucion;
import datatypes.TipoEntidadImagen;
import interfaces.ISistema;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Casos de uso de imágenes. Necesitan la base de pruebas (eventosuy_test). */
class ImagenesTest {

    private final ISistema sistema = Fabrica.getInstance().getISistema();
    private final String sufijo = String.valueOf(System.nanoTime());

    private static byte[] png() {
        return new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, 1, 2, 3};
    }

    private static byte[] jpeg() {
        return new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, 9, 8, 7};
    }

    private String crearAsistente(String prefijo) {
        String nick = prefijo + sufijo;

        DtAsistente dt = new DtAsistente(nick, "Ana", nick + "@mail.com",
                "Pérez", LocalDate.of(2000, 1, 1), null);
        dt.setContrasenia("clave1");
        dt.setConfirmacionContrasenia("clave1");

        assertTrue(sistema.altaAsistente(dt));
        return nick;
    }

    @Test
    void asociaYRecuperaLaImagenDeUnUsuario() {
        String nick = crearAsistente("imgA");

        sistema.asociarImagen(TipoEntidadImagen.USUARIO, nick, new DtImagen("image/png", png()));

        DtImagen recuperada = sistema.obtenerImagen(TipoEntidadImagen.USUARIO, nick);

        assertNotNull(recuperada);
        assertEquals("image/png", recuperada.getTipoContenido());
        assertArrayEquals(png(), recuperada.getContenido());
    }

    @Test
    void reemplazarLaImagenDejaSoloLaNueva() {
        String nick = crearAsistente("imgR");

        sistema.asociarImagen(TipoEntidadImagen.USUARIO, nick, new DtImagen("image/png", png()));
        sistema.asociarImagen(TipoEntidadImagen.USUARIO, nick, new DtImagen("image/jpeg", jpeg()));

        DtImagen recuperada = sistema.obtenerImagen(TipoEntidadImagen.USUARIO, nick);

        assertEquals("image/jpeg", recuperada.getTipoContenido());
        assertArrayEquals(jpeg(), recuperada.getContenido());
    }

    @Test
    void unaEntidadSinImagenDevuelveNull() {
        String nick = crearAsistente("imgN");

        assertNull(sistema.obtenerImagen(TipoEntidadImagen.USUARIO, nick));
    }

    @Test
    void asociarAUnaEntidadInexistenteLanzaError() {
        assertThrows(IllegalArgumentException.class, () -> sistema.asociarImagen(
                TipoEntidadImagen.USUARIO, "noexiste" + sufijo, new DtImagen("image/png", png())));
    }

    @Test
    void unaImagenInvalidaNoSeGuarda() {
        String nick = crearAsistente("imgI");

        assertThrows(IllegalArgumentException.class, () -> sistema.asociarImagen(
                TipoEntidadImagen.USUARIO, nick,
                new DtImagen("image/png", "<html></html>".getBytes())));

        assertNull(sistema.obtenerImagen(TipoEntidadImagen.USUARIO, nick));
    }

    @Test
    void tambienFuncionaParaUnaInstitucion() {
        String nombre = "Institucion" + sufijo;

        assertTrue(sistema.altaInstitucion(
                new DtInstitucion(nombre, "Descripción", "https://ejemplo.com")));

        sistema.asociarImagen(TipoEntidadImagen.INSTITUCION, nombre, new DtImagen("image/png", png()));

        assertArrayEquals(png(),
                sistema.obtenerImagen(TipoEntidadImagen.INSTITUCION, nombre).getContenido());
    }

    @Test
    void validarImagenNoGuardaNada() {
        sistema.validarImagen(new DtImagen("image/png", png()));

        assertThrows(IllegalArgumentException.class,
                () -> sistema.validarImagen(new DtImagen("image/gif", png())));
    }
}