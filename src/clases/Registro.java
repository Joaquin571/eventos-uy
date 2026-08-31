package clases;

import java.time.LocalDate;

public class Registro {

    private LocalDate fechaRegistro;
    private float costo;
    private TipoRegistro tipoRegistro;
    private Edicion edicion;

    public Registro(LocalDate fechaRegistro, float costo, TipoRegistro tipoRegistro,Edicion edicion) {
        this.fechaRegistro = fechaRegistro;
        this.costo = costo;
        this.tipoRegistro = tipoRegistro;
        this.edicion = edicion;
    }

    public LocalDate getFechaRegistro() {
        return fechaRegistro;
    }

    public TipoRegistro getTipoRegistro(){
        return tipoRegistro;
    }

    public Edicion getEdicion(){
        return edicion;
    }

    public float getCosto() {
        return costo;
    }

    public void setCosto(float costo) {
        this.costo = costo;
    }

    public void setFechaRegistro(LocalDate fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    public void setTipoRegistro(TipoRegistro tipoRegistro){
        this.tipoRegistro = tipoRegistro;
    }

    public void setEdicion(Edicion edicion){
        this.edicion = edicion;
    }
}
