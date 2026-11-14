package soporte;

import javax.imageio.*;
import javax.imageio.stream.MemoryCacheImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.zip.CRC32;

/** Imágenes pequeñas reales; ninguna preparación de base de datos. */
public final class ImagenesPrueba {
    private ImagenesPrueba() { }

    public static byte[] imagen(String formato) {
        try {
            var imagen = new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB);
            imagen.setRGB(0, 0, 0xff336699); imagen.setRGB(1, 1, 0xffbb7733);
            var salida = new ByteArrayOutputStream();
            if (!ImageIO.write(imagen, formato, salida)) throw new AssertionError("Falta escritor de prueba: " + formato);
            imagen.flush(); return salida.toByteArray();
        } catch (IOException e) { throw new UncheckedIOException(e); }
    }

    public static byte[] webp() {
        // VP8, 1x1: fixture binario completo, comprobado mediante el lector autorizado.
        return Base64.getDecoder().decode("UklGRiIAAABXRUJQVlA4IBYAAAAwAQCdASoBAAEADsD+JaQAA3AAAAAA");
    }

    public static byte[] webpSinPerdida() {
        return Base64.getDecoder().decode("UklGRh4AAABXRUJQVlA4TBIAAAAvAAAAAAfQ//73v/+BiOh/AAA=");
    }

    public static byte[] webpAnimado(int fotogramas) {
        try {
            var contenido = new ByteArrayOutputStream(); contenido.write("WEBP".getBytes(StandardCharsets.US_ASCII));
            chunk(contenido, "VP8X", new byte[]{2,0,0,0,0,0,0,0,0,0});
            chunk(contenido, "ANIM", new byte[]{0,0,0,(byte)255,0,0});
            for (int i=0;i<fotogramas;i++) {
                var frame = new ByteArrayOutputStream();
                frame.write(new byte[]{0,0,0,0,0,0,0,0,0,0,0,0,100,0,0,2});
                byte[] simple=webp(); frame.write(simple,12,simple.length-12);
                chunk(contenido,"ANMF",frame.toByteArray());
            }
            var salida=new ByteArrayOutputStream(); salida.write("RIFF".getBytes(StandardCharsets.US_ASCII));
            little(salida,contenido.size()); salida.write(contenido.toByteArray()); return salida.toByteArray();
        } catch (IOException e) { throw new UncheckedIOException(e); }
    }

    private static void chunk(ByteArrayOutputStream salida,String tipo,byte[] datos) throws IOException {
        salida.write(tipo.getBytes(StandardCharsets.US_ASCII));little(salida,datos.length);salida.write(datos);
        if((datos.length & 1)!=0)salida.write(0);
    }

    private static void little(ByteArrayOutputStream salida,int valor) {
        for(int i=0;i<4;i++)salida.write((valor >>> (8*i)) & 255);
    }

    public static byte[] gifAnimado(int fotogramas) {
        ImageWriter escritor=ImageIO.getImageWritersByFormatName("gif").next();
        try(var salida=new ByteArrayOutputStream();var flujo=new MemoryCacheImageOutputStream(salida)) {
            escritor.setOutput(flujo);escritor.prepareWriteSequence(null);
            var imagen=new BufferedImage(1,1,BufferedImage.TYPE_INT_RGB);
            for(int i=0;i<fotogramas;i++)escritor.writeToSequence(new IIOImage(imagen,null,null),null);
            escritor.endWriteSequence();flujo.flush();imagen.flush();return salida.toByteArray();
        } catch(IOException e) {throw new UncheckedIOException(e);} finally {escritor.dispose();}
    }

    public static byte[] pngConTamano(int tamano) {
        byte[] png=imagen("png");int relleno=tamano-png.length-12;
        if(relleno<0)throw new IllegalArgumentException("Tamaño insuficiente.");
        try {
            var salida=new ByteArrayOutputStream(tamano);salida.write(png,0,png.length-12);
            var datos=new byte[relleno];var tipo="paDd".getBytes(StandardCharsets.US_ASCII);
            var out=new DataOutputStream(salida);out.writeInt(relleno);out.write(tipo);out.write(datos);
            var crc=new CRC32();crc.update(tipo);crc.update(datos);out.writeInt((int)crc.getValue());
            salida.write(png,png.length-12,12);return salida.toByteArray();
        } catch(IOException e){throw new UncheckedIOException(e);}
    }
}
