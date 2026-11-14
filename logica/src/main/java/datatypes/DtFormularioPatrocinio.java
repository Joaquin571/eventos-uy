package datatypes;

import java.util.ArrayList;
import java.util.List;
import jakarta.xml.bind.annotation.*;

@XmlAccessorType(XmlAccessType.FIELD)
public class DtFormularioPatrocinio {
    private List<DtEvento> eventos = new ArrayList<>();
    private List<DtEdicion> ediciones = new ArrayList<>();
    private List<DtTipoRegistro> tipos = new ArrayList<>();
    private List<DtInstitucion> instituciones = new ArrayList<>();
    private String eventoElegido;

    public DtFormularioPatrocinio() { }
    public List<DtEvento> getEventos() { return eventos; }
    public void setEventos(List<DtEvento> valor) { eventos = valor; }
    public List<DtEdicion> getEdiciones() { return ediciones; }
    public void setEdiciones(List<DtEdicion> valor) { ediciones = valor; }
    public List<DtTipoRegistro> getTipos() { return tipos; }
    public void setTipos(List<DtTipoRegistro> valor) { tipos = valor; }
    public List<DtInstitucion> getInstituciones() { return instituciones; }
    public void setInstituciones(List<DtInstitucion> valor) { instituciones = valor; }
    public String getEventoElegido() { return eventoElegido; }
    public void setEventoElegido(String valor) { eventoElegido = valor; }
}