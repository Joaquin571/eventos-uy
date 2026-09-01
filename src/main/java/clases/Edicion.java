package clases;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Edicion {

    @Id
    private String idNombre;

    private String sigla;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private LocalDate fechaAlta;
    private String ciudad;
    private String pais;

    @ManyToOne
    @JoinColumn(name = "ORG_NICKNAME")
    private Organizador organizador;

    @ManyToOne
    @JoinColumn(name = "EVE_NOMBRE")
    private Evento evento;

    @OneToMany(mappedBy = "edicion")
    private List<Patrocinio> patrocinios = new ArrayList<>();

    @OneToMany(mappedBy = "edicion")
    private List<TipoRegistro> tiposRegistros = new ArrayList<>();

    @OneToMany(mappedBy = "edicion")
    private List<Registro> registros = new ArrayList<>();


    // Constructor requerido por JPA
    protected Edicion() {
    }


    public Edicion(
            String idNombre,
            String sigla,
            LocalDate fechaInicio,
            LocalDate fechaFin,
            LocalDate fechaAlta,
            String ciudad,
            String pais,
            Organizador organizador
    ) {
        this.idNombre = idNombre;
        this.sigla = sigla;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.fechaAlta = fechaAlta;
        this.ciudad = ciudad;
        this.pais = pais;
        this.organizador = organizador;
    }


    // =========================
    // TIPOS DE REGISTRO
    // =========================

    public List<TipoRegistro> getTiposRegistro() {
        return tiposRegistros;
    }

    public void setTiposRegistro(List<TipoRegistro> tiposRegistros) {
        this.tiposRegistros = tiposRegistros;
    }

    public void agregarTipoRegistro(TipoRegistro tipoRegistro) {
        tiposRegistros.add(tipoRegistro);
    }

    public TipoRegistro obtenerTipoRegistro(String nombreTipoRegistro) {

        for (TipoRegistro tipoRegistro : tiposRegistros) {

            if (tipoRegistro.getIdNombre()
                    .equalsIgnoreCase(nombreTipoRegistro)) {

                return tipoRegistro;
            }
        }

        return null;
    }


    // =========================
    // PATROCINIOS
    // =========================

    public List<Patrocinio> getPatrocinios() {
        return patrocinios;
    }

    public void setPatrocinios(List<Patrocinio> patrocinios) {
        this.patrocinios = patrocinios;
    }

    public void agregarPatrocinio(Patrocinio patrocinio) {
        patrocinios.add(patrocinio);
    }


    // =========================
    // REGISTROS
    // =========================

    public List<Registro> getRegistros() {
        return registros;
    }

    public void setRegistros(List<Registro> registros) {
        this.registros = registros;
    }

    public void agregarRegistro(Registro registro) {
        registros.add(registro);
    }


    // =========================
    // ORGANIZADOR
    // =========================

    public Organizador getOrganizador() {
        return organizador;
    }

    public void setOrganizador(Organizador organizador) {
        this.organizador = organizador;
    }


    // =========================
    // EVENTO
    // =========================

    public Evento getEvento() {
        return evento;
    }

    public void setEvento(Evento evento) {
        this.evento = evento;
    }


    // =========================
    // DATOS EDICIÓN
    // =========================

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