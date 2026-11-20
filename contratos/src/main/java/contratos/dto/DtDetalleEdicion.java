package contratos.dto;
import java.util.*;
import jakarta.xml.bind.annotation.*;
@XmlAccessorType(XmlAccessType.FIELD)
public class DtDetalleEdicion {
 private DtEdicion edicion;
 private DtUsuario organizador;
 private boolean tieneImagen;
 private List<DtTipoRegistro> tipos = new ArrayList<>();
 private List<String> patrocinadoras = new ArrayList<>();
 private List<DtRegistro> registros = new ArrayList<>();
 private DtRegistro registroPropio;
 private boolean propietario;
 private boolean puedeRegistrarse;
 public DtDetalleEdicion() { }
 public DtEdicion getEdicion() { return edicion; }
 public void setEdicion(DtEdicion valor) { edicion=valor; }
 public DtUsuario getOrganizador() { return organizador; }
 public void setOrganizador(DtUsuario valor) { organizador=valor; }
 public boolean isTieneImagen() { return tieneImagen; }
 public void setTieneImagen(boolean valor) { tieneImagen=valor; }
 public List<DtTipoRegistro> getTipos() { return tipos; }
 public void setTipos(List<DtTipoRegistro> valor) { tipos=valor; }
 public List<String> getPatrocinadoras() { return patrocinadoras; }
 public void setPatrocinadoras(List<String> valor) { patrocinadoras=valor; }
 public List<DtRegistro> getRegistros() { return registros; }
 public void setRegistros(List<DtRegistro> valor) { registros=valor; }
 public DtRegistro getRegistroPropio() { return registroPropio; }
 public void setRegistroPropio(DtRegistro valor) { registroPropio=valor; }
 public boolean isPropietario() { return propietario; }
 public void setPropietario(boolean valor) { propietario=valor; }
 public boolean isPuedeRegistrarse() { return puedeRegistrarse; }
 public void setPuedeRegistrarse(boolean valor) { puedeRegistrarse=valor; }
}
