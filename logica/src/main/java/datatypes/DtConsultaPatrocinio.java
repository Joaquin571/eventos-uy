package datatypes;

import java.util.ArrayList;
import java.util.List;
import jakarta.xml.bind.annotation.*;

@XmlAccessorType(XmlAccessType.FIELD)
public class DtConsultaPatrocinio {
    private List<DtEvento> eventos = new ArrayList<>();
    private List<DtEdicion> ediciones = new ArrayList<>();
    private List<String> patrocinadoras = new ArrayList<>();
    private DtPatrocinio patrocinio;
    private String eventoElegido;
    private boolean organizadorDuenio;

    public DtConsultaPatrocinio() { }
    public List<DtEvento> getEventos() { return eventos; }
    public void setEventos(List<DtEvento> valor) { eventos = valor; }
    public List<DtEdicion> getEdiciones() { return ediciones; }
    public void setEdiciones(List<DtEdicion> valor) { ediciones = valor; }
    public List<String> getPatrocinadoras() { return patrocinadoras; }
    public void setPatrocinadoras(List<String> valor) { patrocinadoras = valor; }
    public DtPatrocinio getPatrocinio() { return patrocinio; }
    public void setPatrocinio(DtPatrocinio valor) { patrocinio = valor; }
    public String getEventoElegido() { return eventoElegido; }
    public void setEventoElegido(String valor) { eventoElegido = valor; }
    public boolean isOrganizadorDuenio() { return organizadorDuenio; }
    public void setOrganizadorDuenio(boolean valor) { organizadorDuenio = valor; }
}