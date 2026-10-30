package implementacion;

import datatypes.DtAsistente;
import datatypes.DtImagen;
import datatypes.DtInstitucion;
import datatypes.DtEvento;
import datatypes.DtEdicion;
import datatypes.DtOrganizador;
import datatypes.TipoEntidadImagen;
import interfaces.ISistema;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

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

    private String crearEntidadConImagen(TipoEntidadImagen tipo) throws Exception {
        String categoria = "Categoría imagen " + sufijo;
        String evento = "Evento imagen " + sufijo;
        sistema.altaCategoria(categoria, null);
        assertTrue(sistema.altaEvento(new DtEvento(evento, "IM", "Evento con imagen", LocalDate.now(), List.of(categoria))));
        if (tipo == TipoEntidadImagen.EVENTO) return evento;
        String nick = "orgImagen" + sufijo;
        DtOrganizador organizador = new DtOrganizador(nick, "Organizadora", nick + "@test.uy", "Descripción", null);
        organizador.setContrasenia("clave");
        organizador.setConfirmacionContrasenia("clave");
        assertTrue(sistema.altaOrganizador(organizador));
        String edicion = "Edición imagen " + sufijo;
        assertTrue(sistema.altaEdicion(new DtEdicion(edicion, "IM", LocalDate.now().plusDays(10),
                LocalDate.now().plusDays(11), LocalDate.now(), "Montevideo", "Uruguay", nick), evento));
        return edicion;
    }

    private void comprobarImagen(TipoEntidadImagen tipo, String clave, String contenido, byte[] bytes) {
        DtImagen recuperada = sistema.obtenerImagen(tipo, clave);
        assertNotNull(recuperada);
        assertEquals(contenido, recuperada.getTipoContenido());
        assertArrayEquals(bytes, recuperada.getContenido());
    }

    private void comprobarAsociacion(TipoEntidadImagen tipo) throws Exception {
        String clave = crearEntidadConImagen(tipo);
        assertNull(sistema.obtenerImagen(tipo, clave));
        sistema.asociarImagen(tipo, clave, new DtImagen("IMAGE/PNG", png()));
        comprobarImagen(tipo, clave, "image/png", png());
        assertNull(sistema.obtenerImagen(tipo, "ausente" + sufijo));
    }

    private void comprobarReemplazo(TipoEntidadImagen tipo) throws Exception {
        String clave = crearEntidadConImagen(tipo);
        sistema.asociarImagen(tipo, clave, new DtImagen("image/png", png()));
        sistema.asociarImagen(tipo, clave, new DtImagen("image/jpeg", jpeg()));
        comprobarImagen(tipo, clave, "image/jpeg", jpeg());
        // La consulta devuelve datos desconectados: alterarlos no debe cambiar la base.
        sistema.obtenerImagen(tipo, clave).getContenido()[0] = 0;
        comprobarImagen(tipo, clave, "image/jpeg", jpeg());
    }

    private void comprobarRechazoSinPerdida(TipoEntidadImagen tipo) throws Exception {
        String clave = crearEntidadConImagen(tipo);
        sistema.asociarImagen(tipo, clave, new DtImagen("image/png", png()));
        DtImagen[] invalidas = {null, new DtImagen("image/png", jpeg()),
                new DtImagen("image/jpeg", new byte[ValidadorImagen.TAMANIO_MAXIMO + 1])};
        for (DtImagen invalida : invalidas) {
            assertThrows(IllegalArgumentException.class, () -> sistema.asociarImagen(tipo, clave, invalida));
            comprobarImagen(tipo, clave, "image/png", png());
        }
    }

    @Test
    void asociaYRecuperaImagenDeEvento() throws Exception {
        comprobarAsociacion(TipoEntidadImagen.EVENTO);
    }

    @Test
    void asociaYRecuperaImagenDeEdicion() throws Exception {
        comprobarAsociacion(TipoEntidadImagen.EDICION);
    }

    @Test
    void reemplazaImagenDeEventoSinConservarLaAnterior() throws Exception {
        comprobarReemplazo(TipoEntidadImagen.EVENTO);
    }

    @Test
    void reemplazaImagenDeEdicionSinConservarLaAnterior() throws Exception {
        comprobarReemplazo(TipoEntidadImagen.EDICION);
    }

    @Test
    void rechazaImagenInvalidaDeEventoSinPerderLaAnterior() throws Exception {
        comprobarRechazoSinPerdida(TipoEntidadImagen.EVENTO);
    }

    @Test
    void rechazaImagenInvalidaDeEdicionSinPerderLaAnterior() throws Exception {
        comprobarRechazoSinPerdida(TipoEntidadImagen.EDICION);
    }

    @Test
    void referenciasInvalidasDeImagenNoModificanLaImagenExistente() throws Exception {
        String clave = crearEntidadConImagen(TipoEntidadImagen.EVENTO);
        sistema.asociarImagen(TipoEntidadImagen.EVENTO, clave, new DtImagen("image/png", png()));
        for (String invalida : new String[]{null, " "}) {
            assertNull(sistema.obtenerImagen(TipoEntidadImagen.EVENTO, invalida));
            assertThrows(IllegalArgumentException.class, () -> sistema.asociarImagen(
                    TipoEntidadImagen.EVENTO, invalida, new DtImagen("image/jpeg", jpeg())));
        }
        assertNull(sistema.obtenerImagen(null, clave));
        assertThrows(IllegalArgumentException.class, () -> sistema.asociarImagen(null, clave, new DtImagen("image/jpeg", jpeg())));
        comprobarImagen(TipoEntidadImagen.EVENTO, clave, "image/png", png());
    }

    @Test
    void validarImagenNoGuardaNada() {
        sistema.validarImagen(new DtImagen("image/png", png()));

        assertThrows(IllegalArgumentException.class,
                () -> sistema.validarImagen(new DtImagen("image/gif", png())));
    }
}
