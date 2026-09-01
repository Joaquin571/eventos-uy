package clases;

import java.time.LocalDate;

public class Patrocinio {


    private LocalDate fecha;
    private float montoAporte;
    private int cantRegistrosGrat;
    private String codigoPatrocinio;
    private NivelPatrocinio nivel;
    private Institucion  institucion;

    public Patrocinio(
            LocalDate fecha,
            float montoAporte,
            int cantRegistrosGrat,
            String codigoPatrocinio,
            NivelPatrocinio nivel,Institucion institucion) {

        this.fecha = fecha;
        this.institucion=institucion;
        this.montoAporte = montoAporte;
        this.cantRegistrosGrat = cantRegistrosGrat;
        this.codigoPatrocinio = codigoPatrocinio;
        this.nivel = nivel;
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

    public LocalDate getFecha() {return fecha;}
    public float getMontoAporte() {return montoAporte;}
    public int getCantRegistrosGrat() {return cantRegistrosGrat;}
    public String getCodigoPatrocinio() {return codigoPatrocinio;}
    public NivelPatrocinio getNivel() {return nivel;}

    public void setFecha(LocalDate fecha) {this.fecha = fecha;}
    public void setMontoAporte(float montoAporte) {this.montoAporte = montoAporte;}
    public void setCantRegistrosGrat(int cantRegistrosGrat) {this.cantRegistrosGrat = cantRegistrosGrat;}
    public void setCodigoPatrocinio(String codigoPatrocinio) {this.codigoPatrocinio = codigoPatrocinio;}
    public void setNivelPatrocinio(NivelPatrocinio nivel) {this.nivel = nivel;}
}