package contratos.dto;
import java.util.*;
import jakarta.xml.bind.annotation.*;
@XmlAccessorType(XmlAccessType.FIELD)
public class DtFormularioRegistro {
 private String evento;
 private List<DtEdicion> ediciones = new ArrayList<>();
 private List<DtTipoRegistro> tipos = new ArrayList<>();
 private DtEdicion edicion;
 private DtTipoRegistro tipo;
 private List<DtEvento> eventos = new ArrayList<>();
 public DtFormularioRegistro() { }
 public String getEvento() { return evento; }
 public void setEvento(String valor) { evento=valor; }
 public List<DtEdicion> getEdiciones() { return ediciones; }
 public void setEdiciones(List<DtEdicion> valor) { ediciones=valor; }
 public List<DtTipoRegistro> getTipos() { return tipos; }
 public void setTipos(List<DtTipoRegistro> valor) { tipos=valor; }
 public DtEdicion getEdicion() { return edicion; }
 public void setEdicion(DtEdicion valor) { edicion=valor; }
 public DtTipoRegistro getTipo() { return tipo; }
 public void setTipo(DtTipoRegistro valor) { tipo=valor; }
 public List<DtEvento> getEventos() { return eventos; }
 public void setEventos(List<DtEvento> valor) { eventos=valor; }
}
