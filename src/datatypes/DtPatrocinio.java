package datatypes;

import clases.NivelPatrocinio;
import java.time.LocalDate;

public class DtPatrocinio {

    private LocalDate fecha;
    private float montoAporte;
    private int cantRegistrosGrat;
    private String codigoPatrocinio;
    private NivelPatrocinio nivel;
    private String nombreInstituto;
    public DtPatrocinio(
            LocalDate fecha,
            float montoAporte,
            int cantRegistrosGrat,
            String codigoPatrocinio,
            NivelPatrocinio nivel,
            String nombreInstituto) {

        this.fecha = fecha;
        this.montoAporte = montoAporte;
        this.cantRegistrosGrat = cantRegistrosGrat;
        this.codigoPatrocinio = codigoPatrocinio;
        this.nivel = nivel;
        this.nombreInstituto=nombreInstituto;
    }

    public void setNivel(NivelPatrocinio nivel) {
        this.nivel = nivel;
    }

    public String getNombreInstituto() {
        return nombreInstituto;
    }

    public void setNombreInstituto(String nombreInstituto) {
        this.nombreInstituto = nombreInstituto;
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