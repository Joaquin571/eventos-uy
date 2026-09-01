package datatypes;

import java.time.LocalDate;

public class DtRegistro {

    private LocalDate fechaRegistro;
    private float costo;
    private String nombreTipoRegistro;
    private String nombreEdicion;

    public DtRegistro(LocalDate fechaRegistro, float costo
    ) {
        this.fechaRegistro = fechaRegistro;
        this.costo = costo;
        this.nombreTipoRegistro = nombreTipoRegistro;
        this.nombreEdicion = nombreEdicion;
    }

    public String getNombreTipoRegistro(){return nombreTipoRegistro;}
    public LocalDate getFechaRegistro() {
        return fechaRegistro;
    }
    public String getNombreEdicion(){return nombreEdicion;}
    public float getCosto() {
        return costo;
    }

    @Override
    public String toString(){
        return nombreEdicion + "-" + nombreTipoRegistro + " (" + fechaRegistro + ")";
    }
}