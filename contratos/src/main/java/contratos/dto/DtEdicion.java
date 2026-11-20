package contratos.dto;

import jakarta.xml.bind.annotation.*;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import java.time.LocalDate;

@XmlAccessorType(XmlAccessType.FIELD)
public class DtEdicion {
    public DtEdicion() { }

    private String idNombre;
    private String sigla;
    @XmlJavaTypeAdapter(FechaXml.class)
    private LocalDate fechaInicio;
    @XmlJavaTypeAdapter(FechaXml.class)
    private LocalDate fechaFin;
    @XmlJavaTypeAdapter(FechaXml.class)
    private LocalDate fechaAlta;
    private String ciudad;
    private String pais;
    private String videoYoutube;
    private String descripcion;
    private String nombreOrganizador;
    private EstadoEdicion estado = EstadoEdicion.INGRESADA;
    @XmlJavaTypeAdapter(FechaXml.class)
    private LocalDate fechaArchivo;

    public DtEdicion(
            String idNombre,
            String sigla,
            LocalDate fechaInicio,
            LocalDate fechaFin,
            LocalDate fechaAlta,
            String ciudad,
            String pais,
            String nombreOrganizador
    ) {
        this.idNombre = idNombre;
        this.sigla = sigla;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.fechaAlta = fechaAlta;
        this.ciudad = ciudad;
        this.pais = pais;
        this.nombreOrganizador = nombreOrganizador;
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

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public String getVideoYoutube() { return videoYoutube; }
    public void setVideoYoutube(String valor) { this.videoYoutube = valor; }

    public String getPais() {
        return pais;
    }

    public String getNombreOrganizador() {
        return nombreOrganizador;
    }

    public EstadoEdicion getEstado() {
        return estado;
    }

    public void setEstado(EstadoEdicion estado) {
        this.estado = estado;
    }

    public LocalDate getFechaArchivo() {
        return fechaArchivo;
    }

    public void setFechaArchivo(LocalDate valor) {
        this.fechaArchivo = valor;
    }

    @Override
    public String toString() {
        return this.getIdNombre();
    }

    public void setIdNombre(String valor) { this.idNombre = valor; }
    public void setSigla(String valor) { this.sigla = valor; }
    public void setFechaInicio(LocalDate valor) { this.fechaInicio = valor; }
    public void setFechaFin(LocalDate valor) { this.fechaFin = valor; }
    public void setFechaAlta(LocalDate valor) { this.fechaAlta = valor; }
    public void setCiudad(String valor) { this.ciudad = valor; }
    public void setPais(String valor) { this.pais = valor; }
    public void setNombreOrganizador(String valor) { this.nombreOrganizador = valor; }
}