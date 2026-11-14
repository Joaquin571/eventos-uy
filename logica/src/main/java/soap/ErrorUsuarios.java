package soap;

import jakarta.xml.ws.WebFault;

@WebFault(name = "ErrorUsuarios", targetNamespace = "urn:eventosuy:usuarios:v1")
public class ErrorUsuarios extends Exception {
    private final FalloUsuarios faultInfo;
    public ErrorUsuarios(String mensaje, FalloUsuarios faultInfo) {
        super(mensaje);
        this.faultInfo = faultInfo;
    }
    public FalloUsuarios getFaultInfo() { return faultInfo; }
}