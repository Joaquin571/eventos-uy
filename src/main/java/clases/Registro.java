package clases;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "registro")
public class Registro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate fechaRegistro;
    private float costo;

    @ManyToOne
    @JoinColumn(name = "TIPO_REGISTRO_ID")
    private TipoRegistro tipoRegistro;

    @ManyToOne
    @JoinColumn(name = "EDICION_ID")
    private Edicion edicion;

    @ManyToOne
    @JoinColumn(name = "ASISTENTE_NICKNAME")
    private Asistente asistente;

    protected Registro() {
    }

    public Registro(
            LocalDate fechaRegistro,
            float costo,
            TipoRegistro tipoRegistro,
            Edicion edicion
    ) {
        this.fechaRegistro = fechaRegistro;
        this.costo = costo;
        this.tipoRegistro = tipoRegistro;
        this.edicion = edicion;
    }

    public Long getId() {
        return id;
    }

    public LocalDate getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(LocalDate fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    public float getCosto() {
        return costo;
    }

    public void setCosto(float costo) {
        this.costo = costo;
    }

    public TipoRegistro getTipoRegistro() {
        return tipoRegistro;
    }

    public void setTipoRegistro(TipoRegistro tipoRegistro) {
        this.tipoRegistro = tipoRegistro;
    }

    public Edicion getEdicion() {
        return edicion;
    }

    public void setEdicion(Edicion edicion) {
        this.edicion = edicion;
    }

    public Asistente getAsistente() {
        return asistente;
    }

    public void setAsistente(Asistente asistente) {
        this.asistente = asistente;
    }
}