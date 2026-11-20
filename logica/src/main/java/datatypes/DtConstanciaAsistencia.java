package datatypes;

import java.time.LocalDate;

public class DtConstanciaAsistencia {

    private String nombreAsistente;
    private String apellidoAsistente;
    private String nombreEvento;
    private String nombreEdicion;
    private String ciudad;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private LocalDate fechaEmision;

    public DtConstanciaAsistencia() {
    }

    public DtConstanciaAsistencia(
            String nombreAsistente,
            String apellidoAsistente,
            String nombreEvento,
            String nombreEdicion,
            String ciudad,
            LocalDate fechaInicio,
            LocalDate fechaFin,
            LocalDate fechaEmision
    ) {
        this.nombreAsistente = nombreAsistente;
        this.apellidoAsistente = apellidoAsistente;
        this.nombreEvento = nombreEvento;
        this.nombreEdicion = nombreEdicion;
        this.ciudad = ciudad;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.fechaEmision = fechaEmision;
    }

    public String getNombreAsistente() {
        return nombreAsistente;
    }

    public void setNombreAsistente(String nombreAsistente) {
        this.nombreAsistente = nombreAsistente;
    }

    public String getApellidoAsistente() {
        return apellidoAsistente;
    }

    public void setApellidoAsistente(String apellidoAsistente) {
        this.apellidoAsistente = apellidoAsistente;
    }

    public String getNombreEvento() {
        return nombreEvento;
    }

    public void setNombreEvento(String nombreEvento) {
        this.nombreEvento = nombreEvento;
    }

    public String getNombreEdicion() {
        return nombreEdicion;
    }

    public void setNombreEdicion(String nombreEdicion) {
        this.nombreEdicion = nombreEdicion;
    }

    public String getCiudad() {
        return ciudad;
    }

    public void setCiudad(String ciudad) {
        this.ciudad = ciudad;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDate fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public LocalDate getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(LocalDate fechaFin) {
        this.fechaFin = fechaFin;
    }

    public LocalDate getFechaEmision() {
        return fechaEmision;
    }

    public void setFechaEmision(LocalDate fechaEmision) {
        this.fechaEmision = fechaEmision;
    }
}