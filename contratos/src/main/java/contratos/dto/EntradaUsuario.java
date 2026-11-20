package contratos.dto;
import java.util.*;
import jakarta.xml.bind.annotation.*;
@XmlAccessorType(XmlAccessType.FIELD)
public class EntradaUsuario {
 private DtUsuario usuario;
 private String contrasenia;
 private String confirmacion;
 private DtImagen imagen;
 public EntradaUsuario() { }
 public DtUsuario getUsuario() { return usuario; }
 public void setUsuario(DtUsuario valor) { usuario=valor; }
 public String getContrasenia() { return contrasenia; }
 public void setContrasenia(String valor) { contrasenia=valor; }
 public String getConfirmacion() { return confirmacion; }
 public void setConfirmacion(String valor) { confirmacion=valor; }
 public DtImagen getImagen() { return imagen; }
 public void setImagen(DtImagen valor) { imagen=valor; }
}
