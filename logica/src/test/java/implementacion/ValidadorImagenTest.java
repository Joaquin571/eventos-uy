package implementacion;

import datatypes.DtImagen;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

/** Pruebas del validador de imágenes. No necesitan base de datos. */
class ValidadorImagenTest {

    private static byte[] png() {
        return new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A};
    }

    private static byte[] jpeg() {
        return new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0};
    }

    private static byte[] gif() {
        return "GIF89a....".getBytes();
    }

    private static byte[] webp() {
        return "RIFF....WEBPVP8 ".getBytes();
    }

    @Test
    void aceptaLosCuatroFormatosPermitidos() {
        ValidadorImagen.validar(new DtImagen("image/png", png()));
        ValidadorImagen.validar(new DtImagen("image/jpeg", jpeg()));
        ValidadorImagen.validar(new DtImagen("image/gif", gif()));
        ValidadorImagen.validar(new DtImagen("image/webp", webp()));
    }

    @Test
    void aceptaElTipoEnMayusculas() {
        ValidadorImagen.validar(new DtImagen("IMAGE/PNG", png()));
    }

    @Test
    void aceptaUnArchivoDeExactamente2Mb() {
        byte[] exacto = new byte[ValidadorImagen.TAMANIO_MAXIMO];
        System.arraycopy(png(), 0, exacto, 0, png().length);

        ValidadorImagen.validar(new DtImagen("image/png", exacto));
    }

    @Test
    void rechazaUnArchivoDeMasDe2Mb() {
        byte[] grande = new byte[ValidadorImagen.TAMANIO_MAXIMO + 1];
        System.arraycopy(png(), 0, grande, 0, png().length);

        assertThrows(IllegalArgumentException.class,
                () -> ValidadorImagen.validar(new DtImagen("image/png", grande)));
    }

    @Test
    void rechazaHtmlDisfrazadoDeImagen() {
        byte[] html = "<script>alert(1)</script>".getBytes();

        assertThrows(IllegalArgumentException.class,
                () -> ValidadorImagen.validar(new DtImagen("image/png", html)));
    }

    @Test
    void rechazaUnaImagenConLaFirmaDeOtroFormato() {
        assertThrows(IllegalArgumentException.class,
                () -> ValidadorImagen.validar(new DtImagen("image/png", jpeg())));
    }

    @Test
    void rechazaFormatosNoPermitidos() {
        byte[] svg = "<svg></svg>".getBytes();

        assertThrows(IllegalArgumentException.class,
                () -> ValidadorImagen.validar(new DtImagen("image/svg+xml", svg)));
        assertThrows(IllegalArgumentException.class,
                () -> ValidadorImagen.validar(new DtImagen("application/pdf", png())));
    }

    @Test
    void rechazaTipoNuloImagenVaciaONula() {
        assertThrows(IllegalArgumentException.class,
                () -> ValidadorImagen.validar(new DtImagen(null, png())));
        assertThrows(IllegalArgumentException.class,
                () -> ValidadorImagen.validar(new DtImagen("image/png", new byte[0])));
        assertThrows(IllegalArgumentException.class,
                () -> ValidadorImagen.validar(new DtImagen("image/png", null)));
        assertThrows(IllegalArgumentException.class,
                () -> ValidadorImagen.validar(null));
    }
}