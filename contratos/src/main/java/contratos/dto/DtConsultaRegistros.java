package contratos.dto;
import java.util.*;
import jakarta.xml.bind.annotation.*;
@XmlAccessorType(XmlAccessType.FIELD)
public class DtConsultaRegistros {
 private boolean organizador;
 private List<DtEdicion> ediciones = new ArrayList<>();
 private List<DtRegistro> registros = new ArrayList<>();
 private DtRegistro detalle;
 private List<DtUsuario> asistentes = new ArrayList<>();
 public DtConsultaRegistros() { }
 public boolean isOrganizador() { return organizador; }
 public void setOrganizador(boolean valor) { organizador=valor; }
 public List<DtEdicion> getEdiciones() { return ediciones; }
 public void setEdiciones(List<DtEdicion> valor) { ediciones=valor; }
 public List<DtRegistro> getRegistros() { return registros; }
 public void setRegistros(List<DtRegistro> valor) { registros=valor; }
 public DtRegistro getDetalle() { return detalle; }
 public void setDetalle(DtRegistro valor) { detalle=valor; }
 public List<DtUsuario> getAsistentes() { return asistentes; }
 public void setAsistentes(List<DtUsuario> valor) { asistentes=valor; }
}
