package clases;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "patrocinio")
public class Patrocinio {

    @Id
    private String codigoPatrocinio;

    private LocalDate fecha;
    private float montoAporte;
    private int cantRegistrosGrat;

    @Enumerated(EnumType.STRING)
    private NivelPatrocinio nivel;

    @ManyToOne
    @JoinColumn(name = "INSTITUCION_ID")
    private Institucion institucion;

    @ManyToOne
    @JoinColumn(name = "EDICION_ID")
    private Edicion edicion;

    @ManyToOne
    @JoinColumn(name = "TIPO_REGISTRO_ID")
    private TipoRegistro tipoRegistro;

    protected Patrocinio() {
    }

    public Patrocinio(
            LocalDate fecha,
            float montoAporte,
            int cantRegistrosGrat,
            String codigoPatrocinio,
            NivelPatrocinio nivel,
            Institucion institucion
    ) {
        this.fecha = fecha;
        this.montoAporte = montoAporte;
        this.cantRegistrosGrat = cantRegistrosGrat;
        this.codigoPatrocinio = codigoPatrocinio;
        this.nivel = nivel;
        this.institucion = institucion;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public float getMontoAporte() {
        return montoAporte;
    }

    public void setMontoAporte(float montoAporte) {
        this.montoAporte = montoAporte;
    }

    public int getCantRegistrosGrat() {
        return cantRegistrosGrat;
    }

    public void setCantRegistrosGrat(int cantRegistrosGrat) {
        this.cantRegistrosGrat = cantRegistrosGrat;
    }

    public String getCodigoPatrocinio() {
        return codigoPatrocinio;
    }

    public void setCodigoPatrocinio(String codigoPatrocinio) {
        this.codigoPatrocinio = codigoPatrocinio;
    }

    public NivelPatrocinio getNivel() {
        return nivel;
    }

    public void setNivel(NivelPatrocinio nivel) {
        this.nivel = nivel;
    }

    public Institucion getInstitucion() {
        return institucion;
    }

    public void setInstitucion(Institucion institucion) {
        this.institucion = institucion;
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
}