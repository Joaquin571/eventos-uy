package implementacion;

import datatypes.DtImagen;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.MemoryCacheImageInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.zip.CRC32;

/** Valida sin convertir ni modificar los bytes que se persisten. */
final class ValidadorImagen {
    static final int TAMANIO_MAXIMO = 2 * 1024 * 1024;
    static final int LADO_MAXIMO = 4096;
    static final long PIXELES_MAXIMOS = 8_000_000;
    static final int FOTOGRAMAS_MAXIMOS = 100;
    static final long PIXELES_ACUMULADOS_MAXIMOS = 32_000_000;
    private static final Set<String> TIPOS = Set.of("image/png", "image/jpeg", "image/gif", "image/webp");

    private ValidadorImagen() { }

    static void validar(DtImagen imagen) {
        if (imagen == null || imagen.getContenido() == null || imagen.getContenido().length == 0)
            throw new IllegalArgumentException("La imagen está vacía.");
        String tipo = imagen.getTipoContenido() == null ? "" : imagen.getTipoContenido().toLowerCase(Locale.ROOT);
        if (!TIPOS.contains(tipo))
            throw new IllegalArgumentException("Formato de imagen no soportado. Use PNG, JPG, GIF o WEBP.");
        byte[] bytes = imagen.getContenido();
        if (bytes.length > TAMANIO_MAXIMO)
            throw new IllegalArgumentException("La imagen supera el tamaño máximo de 2 MB.");

        // Recorrer contenedores no crea mapas de píxeles. Acota lienzo y fotogramas
        // antes de reservar memoria para los datos descomprimidos.
        int esperados = switch (tipo) {
            case "image/png" -> validarPng(bytes);
            case "image/gif" -> validarGif(bytes);
            case "image/webp" -> validarWebp(bytes);
            default -> {
                exigir(bytes.length >= 4 && u8(bytes,0)==255 && u8(bytes,1)==216
                        && u8(bytes,bytes.length-2)==255 && u8(bytes,bytes.length-1)==217);
                yield 1;
            }
        };
        try (var entrada = new MemoryCacheImageInputStream(new ByteArrayInputStream(bytes))) {
            var lectores = ImageIO.getImageReaders(entrada);
            exigir(lectores.hasNext());
            ImageReader lector = lectores.next();
            try {
                exigir(Arrays.stream(lector.getOriginatingProvider().getMIMETypes()).anyMatch(tipo::equalsIgnoreCase));
                lector.setInput(entrada, false, true);
                int cantidad = lector.getNumImages(true);
                exigir(cantidad == esperados && cantidad > 0 && cantidad <= FOTOGRAMAS_MAXIMOS);
                long acumulados = 0;
                for (int i=0;i<cantidad;i++) {
                    acumulados += dimensiones(lector.getWidth(i), lector.getHeight(i));
                    presupuesto(acumulados);
                }
                boolean[] advertencia = {false};
                lector.addIIOReadWarningListener((origen,mensaje) -> advertencia[0] = true);
                for (int i=0;i<cantidad;i++) {
                    var decodificada = lector.read(i);
                    exigir(decodificada != null);
                    try {
                        dimensiones(decodificada.getWidth(), decodificada.getHeight());
                        exigir(!advertencia[0]); // Rechazar recuperaciones de datos dañados.
                    } finally { decodificada.flush(); }
                }
            } finally { lector.dispose(); }
        } catch (IOException | IndexOutOfBoundsException e) {
            throw new IllegalArgumentException("La imagen está corrupta o incompleta.", e);
        }
    }

    private static long dimensiones(int ancho, int alto) {
        if (ancho <= 0 || alto <= 0 || ancho > LADO_MAXIMO || alto > LADO_MAXIMO
                || (long)ancho*alto > PIXELES_MAXIMOS)
            throw new IllegalArgumentException("La imagen supera las dimensiones permitidas: 4096 por lado y 8 millones de píxeles.");
        return (long)ancho*alto;
    }

    private static void presupuesto(long pixeles) {
        if (pixeles > PIXELES_ACUMULADOS_MAXIMOS)
            throw new IllegalArgumentException("La animación supera los 32 millones de píxeles acumulados.");
    }

    private static void fotogramas(int cantidad) {
        if (cantidad > FOTOGRAMAS_MAXIMOS)
            throw new IllegalArgumentException("La imagen supera el máximo de 100 fotogramas.");
    }

    private static int validarPng(byte[] b) {
        exigir(b.length >= 33 && Arrays.equals(Arrays.copyOf(b,8),
                new byte[]{(byte)137,80,78,71,13,10,26,10}));
        int pos=8; boolean primero=true;
        while (pos < b.length) {
            exigir((long)pos+12 <= b.length);
            long largo=big(b,pos); exigir(largo <= b.length-pos-12);
            int n=(int)largo; String tipo=ascii(b,pos+4);
            var crc=new CRC32(); crc.update(b,pos+4,n+4);
            exigir(crc.getValue()==big(b,pos+8+n));
            if (primero) {
                exigir(tipo.equals("IHDR") && n==13);
                long ancho=big(b,pos+8),alto=big(b,pos+12);
                exigir(ancho<=Integer.MAX_VALUE && alto<=Integer.MAX_VALUE);
                dimensiones((int)ancho,(int)alto); primero=false;
            }
            pos+=n+12;
            if (tipo.equals("IEND")) { exigir(n==0 && pos==b.length); return 1; }
        }
        throw invalida();
    }

    private static int validarGif(byte[] b) {
        exigir(b.length>=14 && (new String(b,0,6,StandardCharsets.US_ASCII).equals("GIF87a")
                || new String(b,0,6,StandardCharsets.US_ASCII).equals("GIF89a")));
        long lienzo=dimensiones(shortLittle(b,6),shortLittle(b,8));
        int pos=13,cantidad=0;
        if ((u8(b,10)&128)!=0) pos+=3*(1<<((u8(b,10)&7)+1));
        while (pos<b.length) {
            int bloque=u8(b,pos++);
            if (bloque==59) { exigir(pos==b.length && cantidad>0); return cantidad; }
            if (bloque==33) { exigir(pos<b.length); pos++; }
            else {
                exigir(bloque==44 && (long)pos+9<=b.length);
                long pixeles=dimensiones(shortLittle(b,pos+4),shortLittle(b,pos+6));
                fotogramas(++cantidad); presupuesto(Math.max(lienzo,pixeles)*cantidad);
                int packed=u8(b,pos+8); pos+=9;
                if ((packed&128)!=0) pos+=3*(1<<((packed&7)+1));
                exigir(pos<b.length); pos++; // El lector comprueba el código LZW y los píxeles.
            }
            int largo;
            do { exigir(pos<b.length); largo=u8(b,pos++); exigir(largo<=b.length-pos); pos+=largo; } while(largo!=0);
        }
        throw invalida();
    }

    private static int validarWebp(byte[] b) {
        exigir(b.length>=20 && ascii(b,0).equals("RIFF") && ascii(b,8).equals("WEBP")
                && little(b,4)+8==b.length);
        int pos=12,cantidad=0; long lienzo=0;
        while(pos<b.length) {
            exigir((long)pos+8<=b.length); long largo=little(b,pos+4);
            exigir(largo<=b.length-pos-8); int n=(int)largo; String tipo=ascii(b,pos); int datos=pos+8;
            if(tipo.equals("VP8X")) {
                exigir(pos==12 && n==10); lienzo=dimensiones(triple(b,datos+4)+1,triple(b,datos+7)+1);
            } else if(tipo.equals("ANMF")) {
                exigir(lienzo>0 && n>=16); long pixeles=dimensiones(triple(b,datos+6)+1,triple(b,datos+9)+1);
                fotogramas(++cantidad); presupuesto(Math.max(lienzo,pixeles)*cantidad);
            }
            pos+=8+n+(n&1); exigir(pos<=b.length);
        }
        exigir(pos==b.length); return Math.max(1,cantidad);
    }

    private static int u8(byte[] b,int pos) { return b[pos]&255; }
    private static int shortLittle(byte[] b,int pos) { return u8(b,pos)|(u8(b,pos+1)<<8); }
    private static int triple(byte[] b,int pos) { return shortLittle(b,pos)|(u8(b,pos+2)<<16); }
    private static long little(byte[] b,int pos) { return (long)triple(b,pos)|((long)u8(b,pos+3)<<24); }
    private static long big(byte[] b,int pos) { return ((long)u8(b,pos)<<24)|((long)u8(b,pos+1)<<16)|((long)u8(b,pos+2)<<8)|u8(b,pos+3); }
    private static String ascii(byte[] b,int pos) { return new String(b,pos,4,StandardCharsets.US_ASCII); }
    private static void exigir(boolean valido) { if (!valido) throw invalida(); }
    private static IllegalArgumentException invalida() { return new IllegalArgumentException("El archivo no es una imagen válida del formato indicado."); }
}
