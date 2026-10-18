package clases;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

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
    private Set<Patrocinio> patrocinios = new HashSet<Patrocinio>();

    @OneToMany(mappedBy = "edicion")
    private Set<TipoRegistro> tiposRegistros = new HashSet<>();

    @OneToMany(mappedBy = "edicion")
    private Set<Registro> registros = new HashSet<>();

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

    public Set<TipoRegistro> getTiposRegistro() {return tiposRegistros;}
    public void setTiposRegistro(Set<TipoRegistro> tiposRegistros) {this.tiposRegistros = tiposRegistros;}

    public void agregarTipoRegistro(TipoRegistro tipoRegistro) {tiposRegistros.add(tipoRegistro);}
    public TipoRegistro obtenerTipoRegistro(String nombreTipoRegistro) {
        for (TipoRegistro tipoRegistro : tiposRegistros) {
            if (tipoRegistro.getIdNombre().equalsIgnoreCase(nombreTipoRegistro)) {
                return tipoRegistro;
            }
        }
        return null;
    }

    public Set<Patrocinio> getPatrocinios() {return patrocinios;}
    public void setPatrocinios(Set<Patrocinio> patrocinios) {this.patrocinios = patrocinios;}
    public void agregarPatrocinio(Patrocinio patrocinio) {patrocinios.add(patrocinio);}

    public Set<Registro> getRegistros() {return registros;}
    public void setRegistros(Set<Registro> registros) {this.registros = registros;}

    public Organizador getOrganizador() {return organizador;}
    public void setOrganizador(Organizador organizador) {this.organizador = organizador;}

    public Evento getEvento() {return evento;}
    public void setEvento(Evento evento) {this.evento = evento;}

    public String getIdNombre() {
        return idNombre;
    }
    public void setIdNombre(String idNombre) {
        this.idNombre = idNombre;
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
}