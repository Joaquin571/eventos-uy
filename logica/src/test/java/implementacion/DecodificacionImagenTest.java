package implementacion;

import datatypes.DtImagen;
import org.junit.jupiter.api.Test;
import soporte.ImagenesPrueba;

import java.util.Arrays;
import java.util.zip.CRC32;

import static org.junit.jupiter.api.Assertions.*;

/** Datos reales y ataques de cabecera; no necesitan PostgreSQL. */
class DecodificacionImagenTest {
    private void validar(String mime, byte[] bytes) { ValidadorImagen.validar(new DtImagen(mime,bytes)); }

    @Test
    void webpConYSinPerdidaSeDecodificaSinModificarSusBytes() {
        for(byte[] bytes: new byte[][]{ImagenesPrueba.webp(),ImagenesPrueba.webpSinPerdida()}) {
            byte[] copia=bytes.clone();assertDoesNotThrow(()->validar("image/webp",bytes));assertArrayEquals(copia,bytes);
        }
    }

    @Test
    void gifYWebpAnimadosSeValidanCompletosSinConversion() {
        for(String mime:new String[]{"image/gif","image/webp"}) {
            byte[] bytes=mime.endsWith("gif")?ImagenesPrueba.gifAnimado(2):ImagenesPrueba.webpAnimado(2);
            byte[] copia=bytes.clone();assertDoesNotThrow(()->validar(mime,bytes));assertArrayEquals(copia,bytes);
        }
    }

    @Test
    void rechazaCabecerasFalsificadasDeLosCuatroFormatos() {
        assertThrows(IllegalArgumentException.class,()->validar("image/png",new byte[]{(byte)137,80,78,71,13,10,26,10}));
        assertThrows(IllegalArgumentException.class,()->validar("image/jpeg",new byte[]{(byte)255,(byte)216,(byte)255,(byte)217}));
        assertThrows(IllegalArgumentException.class,()->validar("image/gif","GIF89a........".getBytes()));
        assertThrows(IllegalArgumentException.class,()->validar("image/webp","RIFF........WEBPVP8 ".getBytes()));
    }

    @Test
    void rechazaTruncadosEnCadaPuntoDeUnaImagenReal() {
        for(String formato:new String[]{"png","jpeg","gif","webp"}) {
            byte[] bytes=formato.equals("webp")?ImagenesPrueba.webp():ImagenesPrueba.imagen(formato);
            for(int n=0;n<bytes.length;n++) {
                byte[] truncada=Arrays.copyOf(bytes,n);
                assertThrows(IllegalArgumentException.class,()->validar("image/"+formato,truncada),formato+" longitud "+n);
            }
        }
    }

    @Test
    void rechazaPngConCrcIncorrecto() {
        byte[] png=ImagenesPrueba.imagen("png");png[29]^=1;
        assertThrows(IllegalArgumentException.class,()->validar("image/png",png));
    }

    @Test
    void rechazaPngConDatosComprimidosCorruptosAunqueSuCrcSeaCorrecto() {
        byte[] png=ImagenesPrueba.imagen("png");
        for(int p=8;p<png.length;) {
            int n=entero(png,p);
            if(new String(png,p+4,4,java.nio.charset.StandardCharsets.US_ASCII).equals("IDAT")) {
                Arrays.fill(png,p+8,p+8+n,(byte)0);crc(png,p,n);break;
            }
            p+=12+n;
        }
        assertThrows(IllegalArgumentException.class,()->validar("image/png",png));
    }

    @Test
    void dimensionesExcesivasSeRechazanPorMetadatosSinCrearElRaster() {
        for(int[] tamano:new int[][]{{0,1},{4097,1},{1,4097},{4096,2048}}) {
            byte[] png=ImagenesPrueba.imagen("png");poner(png,16,tamano[0]);poner(png,20,tamano[1]);crc(png,8,13);
            var error=assertThrows(IllegalArgumentException.class,()->validar("image/png",png));
            assertTrue(error.getMessage().contains("dimensiones"));
        }
    }

    @Test
    void gifYWebpRechazanFotogramasExcesivosAntesDeDecodificarlos() {
        assertTrue(assertThrows(IllegalArgumentException.class,()->validar("image/gif",ImagenesPrueba.gifAnimado(101))).getMessage().contains("100 fotogramas"));
        assertTrue(assertThrows(IllegalArgumentException.class,()->validar("image/webp",ImagenesPrueba.webpAnimado(101))).getMessage().contains("100 fotogramas"));
    }

    @Test
    void limitaElTrabajoAcumuladoAunqueCadaFotogramaSeaPequeno() {
        byte[] gif=ImagenesPrueba.gifAnimado(5);
        gif[6]=(byte)4000;gif[7]=(byte)(4000>>8);gif[8]=(byte)2000;gif[9]=(byte)(2000>>8);
        assertTrue(assertThrows(IllegalArgumentException.class,()->validar("image/gif",gif)).getMessage().contains("acumulados"));
    }

    @Test
    void webpRechazaLongitudesRiffYChunksFalsificados() {
        byte[] webp=ImagenesPrueba.webp();webp[4]++;
        assertThrows(IllegalArgumentException.class,()->validar("image/webp",webp));
        byte[] alterada=ImagenesPrueba.webp();alterada[16]=(byte)255;
        assertThrows(IllegalArgumentException.class,()->validar("image/webp",alterada));
    }

    @Test
    void webpRechazaLienzoGiganteAntesDeDecodificar() {
        byte[] webp=ImagenesPrueba.webpAnimado(2);webp[24]=(byte)255;webp[25]=(byte)255;webp[26]=(byte)255;
        assertTrue(assertThrows(IllegalArgumentException.class,()->validar("image/webp",webp)).getMessage().contains("dimensiones"));
    }

    @Test
    void rechazaUnFotogramaAnimadoCorruptoAunqueElPrimeroSeaValido() {
        byte[] webp=ImagenesPrueba.webpAnimado(2);
        // Última trama VP8: cabecera y longitudes intactas, datos comprimidos inválidos.
        Arrays.fill(webp,webp.length-15,webp.length,(byte)255);
        assertThrows(IllegalArgumentException.class,()->validar("image/webp",webp));
    }

    private static int entero(byte[] b,int p) {return java.nio.ByteBuffer.wrap(b,p,4).getInt();}
    private static void poner(byte[] b,int p,int n) {java.nio.ByteBuffer.wrap(b,p,4).putInt(n);}
    private static void crc(byte[] b,int p,int n) {
        CRC32 crc=new CRC32();crc.update(b,p+4,n+4);poner(b,p+8+n,(int)crc.getValue());
    }
}
