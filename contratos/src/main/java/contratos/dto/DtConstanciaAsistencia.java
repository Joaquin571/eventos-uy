package contratos.dto;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import java.time.LocalDate;

@XmlAccessorType(XmlAccessType.FIELD)
public class DtConstanciaAsistencia {

    private String nombreAsistente;
    private String apellidoAsistente;
    private String nombreEvento;
    private String nombreEdicion;
    private String ciudad;

    @XmlJavaTypeAdapter(FechaXml.class)
    private LocalDate fechaInicio;

    @XmlJavaTypeAdapter(FechaXml.class)
    private LocalDate fechaFin;

    @XmlJavaTypeAdapter(FechaXml.class)
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

    public void setNombreAsistente(String valor) {
        this.nombreAsistente = valor;
    }

    public String getApellidoAsistente() {
        return apellidoAsistente;
    }

    public void setApellidoAsistente(String valor) {
        this.apellidoAsistente = valor;
    }

    public String getNombreEvento() {
        return nombreEvento;
    }

    public void setNombreEvento(String valor) {
        this.nombreEvento = valor;
    }

    public String getNombreEdicion() {
        return nombreEdicion;
    }

    public void setNombreEdicion(String valor) {
        this.nombreEdicion = valor;
    }

    public String getCiudad() {
        return ciudad;
    }

    public void setCiudad(String valor) {
        this.ciudad = valor;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDate valor) {
        this.fechaInicio = valor;
    }

    public LocalDate getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(LocalDate valor) {
        this.fechaFin = valor;
    }

    public LocalDate getFechaEmision() {
        return fechaEmision;
    }

    public void setFechaEmision(LocalDate valor) {
        this.fechaEmision = valor;
    }
}