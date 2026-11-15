package web.soap;

import java.nio.file.Path;
import javax.net.ssl.*;

/** Certificados temporales exclusivos de las pruebas, con validación TLS real. */
public final class SoporteTls {
    private SoporteTls() { }
    public static void generar(Path archivo,char[] clave)throws Exception {EncabezadosSoapHttpsTest.generarCertificado(archivo,clave);}
    public static SSLSocketFactory confianza(Path archivo,char[] clave)throws Exception {return EncabezadosSoapHttpsTest.confianzaLocal(archivo,clave);}
    public static SSLContext contexto(Path archivo,char[] clave)throws Exception {return PruebaHttpsServidor.contextoTls(archivo,clave);}
}
