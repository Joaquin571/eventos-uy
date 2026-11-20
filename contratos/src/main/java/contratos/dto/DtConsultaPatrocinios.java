package contratos.dto;
import java.util.*;
import jakarta.xml.bind.annotation.*;
@XmlAccessorType(XmlAccessType.FIELD)
public class DtConsultaPatrocinios {
 private String evento;
 private List<DtEvento> eventos = new ArrayList<>();
 private List<DtEdicion> ediciones = new ArrayList<>();
 private List<String> patrocinadoras = new ArrayList<>();
 private DtPatrocinio patrocinio;
 private boolean puedeVerCodigo;
 public DtConsultaPatrocinios() { }
 public String getEvento() { return evento; }
 public void setEvento(String valor) { evento=valor; }
 public List<DtEvento> getEventos() { return eventos; }
 public void setEventos(List<DtEvento> valor) { eventos=valor; }
 public List<DtEdicion> getEdiciones() { return ediciones; }
 public void setEdiciones(List<DtEdicion> valor) { ediciones=valor; }
 public List<String> getPatrocinadoras() { return patrocinadoras; }
 public void setPatrocinadoras(List<String> valor) { patrocinadoras=valor; }
 public DtPatrocinio getPatrocinio() { return patrocinio; }
 public void setPatrocinio(DtPatrocinio valor) { patrocinio=valor; }
 public boolean isPuedeVerCodigo() { return puedeVerCodigo; }
 public void setPuedeVerCodigo(boolean valor) { puedeVerCodigo=valor; }
}
