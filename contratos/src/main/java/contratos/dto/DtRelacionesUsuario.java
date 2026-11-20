package contratos.dto;

import jakarta.xml.bind.annotation.*;
import java.util.*;

@XmlAccessorType(XmlAccessType.FIELD)
public class DtRelacionesUsuario {
    private List<String> seguidores = new ArrayList<>();
    private List<String> seguidos = new ArrayList<>();
    private boolean siguiendo;
    public DtRelacionesUsuario() { }
    public List<String> getSeguidores() { return seguidores; }
    public void setSeguidores(List<String> valor) { seguidores = valor; }
    public List<String> getSeguidos() { return seguidos; }
    public void setSeguidos(List<String> valor) { seguidos = valor; }
    public boolean isSiguiendo() { return siguiendo; }
    public void setSiguiendo(boolean valor) { siguiendo = valor; }
}
