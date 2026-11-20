package clases;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(
        name = "registro",
        uniqueConstraints = @UniqueConstraint(
                name = "UK_REGISTRO_ASISTENTE_EDICION",
                columnNames = {"ASISTENTE_NICKNAME", "EDICION_ID"}
        )
)
public class Registro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate fechaRegistro;
    private float costo;

    @Column(nullable = false)
    private boolean asistenciaConfirmada = false;

    @ManyToOne
    @JoinColumn(name = "TIPO_REGISTRO_ID")
    private TipoRegistro tipoRegistro;

    @ManyToOne
    @JoinColumn(name = "EDICION_ID")
    private Edicion edicion;

    @ManyToOne
    @JoinColumn(name = "ASISTENTE_NICKNAME")
    private Asistente asistente;

    @ManyToOne
    @JoinColumn(name = "PATROCINIO_CODIGO")
    private Patrocinio patrocinio;

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

    public Long getId() { return id; }
    public LocalDate getFechaRegistro() { return fechaRegistro; }
    public float getCosto() { return costo; }
    public void setCosto(float costo) { this.costo = costo; }
    public boolean isAsistenciaConfirmada() { return asistenciaConfirmada; }
    public void setAsistenciaConfirmada(boolean asistenciaConfirmada) { this.asistenciaConfirmada = asistenciaConfirmada; }
    public TipoRegistro getTipoRegistro() { return tipoRegistro; }
    public void setTipoRegistro(TipoRegistro tipoRegistro) { this.tipoRegistro = tipoRegistro; }
    public Edicion getEdicion() { return edicion; }
    public void setEdicion(Edicion edicion) { this.edicion = edicion; }
    public Asistente getAsistente() { return asistente; }
    public void setAsistente(Asistente asistente) { this.asistente = asistente; }

    public Patrocinio getPatrocinio() {
        return patrocinio;
    }

    public void setPatrocinio(Patrocinio patrocinio) {
        this.patrocinio = patrocinio;
    }

    public void confirmarAsistencia() {
        if (this.asistenciaConfirmada) {
            throw new IllegalStateException("La asistencia ya fue confirmada para este registro.");
        }
        this.asistenciaConfirmada = true;
    }
}