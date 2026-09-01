package clases;

import java.time.LocalDate;

public class Registro {

    private LocalDate fechaRegistro;
    private float costo;
    private Asistente asistente;
    private Edicion edicion;
    private TipoRegistro tipoRegistro;

    public Registro(LocalDate fechaRegistro, float costo, Asistente asistente, Edicion edicion, TipoRegistro tipoRegistro) {
        this.fechaRegistro = fechaRegistro;
        this.costo = costo;
        this.asistente = asistente;
        this.edicion = edicion;
        this.tipoRegistro = tipoRegistro;
    }

    public Edicion getEdicion() {
        return edicion;
    }

    public void setEdicion(Edicion edicion) {
        this.edicion = edicion;
    }

    public TipoRegistro getTipoRegistro() {
        return tipoRegistro;
    }

    public void setTipoRegistro(TipoRegistro tipoRegistro) {
        this.tipoRegistro = tipoRegistro;
    }

    public Asistente getAsistente() {
        return asistente;
    }

    public void setAsistente(Asistente asistente) {
        this.asistente = asistente;
    }

    public LocalDate getFechaRegistro() {
        return fechaRegistro;
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




}
