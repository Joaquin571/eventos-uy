package contratos.dto;
import java.util.*;
import jakarta.xml.bind.annotation.*;
@XmlAccessorType(XmlAccessType.FIELD)
public class DtPerfilUsuario {
 private DtUsuario usuario;
 private boolean propio;
 private List<DtRegistro> registros = new ArrayList<>();
 private List<DtEdicion> ediciones = new ArrayList<>();
 public DtPerfilUsuario() { }
 public DtUsuario getUsuario() { return usuario; }
 public void setUsuario(DtUsuario valor) { usuario=valor; }
 public boolean isPropio() { return propio; }
 public void setPropio(boolean valor) { propio=valor; }
 public List<DtRegistro> getRegistros() { return registros; }
 public void setRegistros(List<DtRegistro> valor) { registros=valor; }
 public List<DtEdicion> getEdiciones() { return ediciones; }
 public void setEdiciones(List<DtEdicion> valor) { ediciones=valor; }
}
