package central.soap;
import jakarta.xml.ws.WebFault;
@WebFault(name="OperacionFault",targetNamespace="https://eventos.uy/soap/portal")
public class OperacionSoapException extends Exception {
 private final OperacionFault faultInfo;
 public OperacionSoapException(String codigo,String mensaje){super(mensaje);faultInfo=new OperacionFault(codigo,mensaje);}
 public OperacionFault getFaultInfo(){return faultInfo;}
}
