package web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.Part;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class SubidasTest {
    private static final int MAXIMO = 2 * 1024 * 1024;

    @Test
    void archivoAusenteOVacioSigueSiendoOpcional() throws Exception {
        assertNull(Subidas.leerImagen(peticion(null), "imagen"));
        assertNull(Subidas.leerImagen(peticion(parte(0, InputStream.nullInputStream())), "imagen"));
    }

    @Test
    void tamanoDeclaradoExcesivoSeRechazaAntesDeAbrirElArchivo() {
        Part parte = CsrfTest.doble(Part.class, (p,m,a) -> {
            if (m.getName().equals("getSize")) return (long)MAXIMO + 1;
            throw new AssertionError("No debe leer el archivo excesivo: " + m.getName());
        });
        assertThrows(IllegalArgumentException.class, () -> Subidas.leerImagen(peticion(parte), "imagen"));
    }

    @Test
    void lecturaSeAcotaAunqueElTamanoDeclaradoNoCoincidaConElFlujo() {
        AtomicInteger leidos = new AtomicInteger(); AtomicBoolean cerrado = new AtomicBoolean();
        InputStream enorme = new InputStream() {
            @Override public int read() { leidos.incrementAndGet(); return 1; }
            @Override public int read(byte[] b, int off, int len) {
                java.util.Arrays.fill(b, off, off + len, (byte)1); leidos.addAndGet(len); return len;
            }
            @Override public void close() { cerrado.set(true); }
        };
        assertThrows(IllegalArgumentException.class,
                () -> Subidas.leerImagen(peticion(parte(1, enorme)), "imagen"));
        assertEquals(MAXIMO + 1, leidos.get()); assertTrue(cerrado.get());
    }

    @Test
    void conservaLosBytesSinConvertirlos() throws Exception {
        byte[] original = {1,2,3,4};
        var dt = Subidas.leerImagen(peticion(parte(original.length, new ByteArrayInputStream(original))), "imagen");
        assertArrayEquals(original, dt.getContenido()); assertEquals("image/png", dt.getTipoContenido());
        // La validación del contenido corresponde a ISistema, no a este lector de Part.
    }

    private Part parte(long tamano, InputStream entrada) {
        return CsrfTest.doble(Part.class, (p,m,a) -> switch (m.getName()) {
            case "getSize" -> tamano;
            case "getInputStream" -> entrada;
            case "getContentType" -> "image/png";
            default -> throw new AssertionError(m.getName());
        });
    }

    private HttpServletRequest peticion(Part parte) {
        return CsrfTest.doble(HttpServletRequest.class, (p,m,a) -> {
            assertEquals("getPart", m.getName()); assertEquals("imagen", a[0]); return parte;
        });
    }
}
