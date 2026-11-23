package web.remoto;

import jakarta.xml.ws.WebServiceException;
import org.junit.jupiter.api.Test;

import javax.net.ssl.SSLHandshakeException;

import static org.junit.jupiter.api.Assertions.*;

class DiagnosticoSoapTest {
    @Test void informaLaCausaTlsSinExponerCredenciales() {
        var causa = new SSLHandshakeException("PKIX path building failed; password=clave-secreta");
        var error = new WebServiceException("Authorization: Bearer credencial-secreta", causa);
        String resumen = DiagnosticoSoap.describir(error);
        assertTrue(resumen.contains("jakarta.xml.ws.WebServiceException"));
        assertTrue(resumen.contains("javax.net.ssl.SSLHandshakeException"));
        assertTrue(resumen.contains("PKIX path building failed"));
        assertFalse(resumen.contains("clave-secreta"));
        assertFalse(resumen.contains("credencial-secreta"));
    }
}
