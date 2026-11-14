package datatypes;

import java.util.ArrayList;
import java.util.List;
import jakarta.xml.bind.annotation.*;

@XmlAccessorType(XmlAccessType.FIELD)
public class DtSesion {
    private String token;
    private DtUsuario usuario;

    public DtSesion() { }
    public String getToken() { return token; }
    public void setToken(String valor) { token = valor; }
    public DtUsuario getUsuario() { return usuario; }
    public void setUsuario(DtUsuario valor) { usuario = valor; }
}