
package contratos.dto;

import jakarta.xml.bind.annotation.*;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlAccessorType(XmlAccessType.FIELD)
public class DtCategoria {
    public DtCategoria() { }


    private String idNombre;
    private String nombrePadre;

    public DtCategoria(String idNombre, String nombrePadre) {
        this.idNombre = idNombre;
        this.nombrePadre = nombrePadre;
    }

    public String getIdNombre() {
        return idNombre;
    }

    public String getNombrePadre() {
        return nombrePadre;
    }

    @Override
    public String toString() {
        return idNombre;
    }

    public void setIdNombre(String valor) { this.idNombre = valor; }
    public void setNombrePadre(String valor) { this.nombrePadre = valor; }
}
