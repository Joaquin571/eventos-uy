package central.soap;
import jakarta.xml.bind.annotation.*;
@XmlAccessorType(XmlAccessType.FIELD)
public class OperacionFault {
 private String codigo;private String mensaje;
 public OperacionFault() { }
 public OperacionFault(String codigo,String mensaje){this.codigo=codigo;this.mensaje=mensaje;}
 public String getCodigo(){return codigo;} public void setCodigo(String valor){codigo=valor;}
 public String getMensaje(){return mensaje;} public void setMensaje(String valor){mensaje=valor;}
}
