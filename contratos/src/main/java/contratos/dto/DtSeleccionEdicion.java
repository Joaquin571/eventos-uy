package contratos.dto;
import java.util.*;
import jakarta.xml.bind.annotation.*;
@XmlAccessorType(XmlAccessType.FIELD)
public class DtSeleccionEdicion {
 private String evento;
 private DtEdicion edicion;
 private List<DtEdicion> ediciones = new ArrayList<>();
 private List<DtEvento> eventos = new ArrayList<>();
 public DtSeleccionEdicion() { }
 public String getEvento() { return evento; }
 public void setEvento(String valor) { evento=valor; }
 public DtEdicion getEdicion() { return edicion; }
 public void setEdicion(DtEdicion valor) { edicion=valor; }
 public List<DtEdicion> getEdiciones() { return ediciones; }
 public void setEdiciones(List<DtEdicion> valor) { ediciones=valor; }
 public List<DtEvento> getEventos() { return eventos; }
 public void setEventos(List<DtEvento> valor) { eventos=valor; }
}
