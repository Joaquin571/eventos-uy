package central.soap;

import jakarta.xml.ws.WebFault;

@WebFault(name = "SesionFault", targetNamespace = SesionesPublicador.NAMESPACE)
public class SesionSoapException extends Exception {
    private final Detalle detalle;

    SesionSoapException(String codigo, String mensaje) {
        super(mensaje);
        this.detalle = new Detalle();
        this.detalle.setCodigo(codigo);
    }

    public Detalle getFaultInfo() { return detalle; }

    public static class Detalle {
        private String codigo;

        public Detalle() { }
        public String getCodigo() { return codigo; }
        public void setCodigo(String codigo) { this.codigo = codigo; }
    }
}
