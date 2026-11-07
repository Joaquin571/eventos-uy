package central.soap;

import jakarta.xml.ws.WebFault;

@WebFault(name = "ConsultaFault", targetNamespace = "https://eventos.uy/soap/eventos")
public class ConsultaException extends Exception {
    public ConsultaException(String mensaje) { super(mensaje); }
}
