package clases;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Edicion {

    private String idNombre;
    private String sigla;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private LocalDate fechaAlta;
    private String ciudad;
    private String pais;
    private List<TipoRegistro> tiposRegistro;

    public Edicion(
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
        this.tiposRegistro = new ArrayList<>();
    }

    public List<TipoRegistro> getTiposRegistro(){
        return this.tiposRegistro;
    }
    public void agregarTipoRegistro(TipoRegistro tipoRegistro){
        this.tiposRegistro.add(tipoRegistro);
    }

    public TipoRegistro obtenerTipoRegistro(String nombreTipoRegistro) {
        for (TipoRegistro tr : tiposRegistro) {
            if (tr.getIdNombre().equalsIgnoreCase(nombreTipoRegistro)) {
                return tr;
            }
        }
        return null;
    }

    public String getIdNombre() {
        return idNombre;
    }

    public void setIdNombre(String idNombre) {
        this.idNombre = idNombre;
    }

    public String getSigla() {
        return sigla;
    }

    public void setSigla(String sigla) {
        this.sigla = sigla;
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

    public LocalDate getFechaAlta() {
        return fechaAlta;
    }

    public void setFechaAlta(LocalDate fechaAlta) {
        this.fechaAlta = fechaAlta;
    }

    public String getCiudad() {
        return ciudad;
    }

    public void setCiudad(String ciudad) {
        this.ciudad = ciudad;
    }

    public String getPais() {
        return pais;
    }

    public void setPais(String pais) {
        this.pais = pais;
    }
}