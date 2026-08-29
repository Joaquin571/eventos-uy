package datatypes;

import java.time.LocalDate;

public class DtEdicion {

    private String idNombre;
    private String sigla;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private LocalDate fechaAlta;
    private String ciudad;
    private String pais;

    public DtEdicion(
            String idNombre,
            String sigla,
            LocalDate fechaInicio,
            LocalDate fechaFin,
            LocalDate fechaAlta,
            String ciudad,
            String pais
    ) {
        this.idNombre = idNombre;
        this.sigla = sigla;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.fechaAlta = fechaAlta;
        this.ciudad = ciudad;
        this.pais = pais;
    }

    public String getIdNombre() {
        return idNombre;
    }

    public String getSigla() {
        return sigla;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public LocalDate getFechaFin() {
        return fechaFin;
    }

    public LocalDate getFechaAlta() {
        return fechaAlta;
    }

    public String getCiudad() {
        return ciudad;
    }

    public String getPais() {
        return pais;
    }

    @Override
    public String toString() {
        return idNombre;
    }
}