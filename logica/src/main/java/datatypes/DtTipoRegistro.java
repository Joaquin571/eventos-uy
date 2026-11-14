package datatypes;

import jakarta.xml.bind.annotation.*;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlAccessorType(XmlAccessType.FIELD)
public class DtTipoRegistro {
    public DtTipoRegistro() { }


    private String idNombre;
    private String descripcion;
    private float costo;
    private int cupo;

    public DtTipoRegistro(
            String idNombre,
            String descripcion,
            float costo,
            int cupo
    ) {
        this.idNombre = idNombre;
        this.descripcion = descripcion;
        this.costo = costo;
        this.cupo = cupo;
    }

    public String getIdNombre() {
        return idNombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public float getCosto() {
        return costo;
    }

    public int getCupo() {
        return cupo;
    }

    @Override
    public String toString() {
        return idNombre;
    }
    public void setIdNombre(String valor) { this.idNombre = valor; }
    public void setDescripcion(String valor) { this.descripcion = valor; }
    public void setCosto(float valor) { this.costo = valor; }
    public void setCupo(int valor) { this.cupo = valor; }
}