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
    private String nombreEdicion;
    private String nombreTipoRegistro;

    public DtPatrocinio(
            LocalDate fecha,
            float montoAporte,
            int cantRegistrosGrat,
            String codigoPatrocinio,
            NivelPatrocinio nivel,
            String nombreInstituto,
            String nombreEdicion,
            String nombreTipoRegistro
    ) {
        this.fecha = fecha;
        this.montoAporte = montoAporte;
        this.cantRegistrosGrat = cantRegistrosGrat;
        this.codigoPatrocinio = codigoPatrocinio;
        this.nivel = nivel;
        this.nombreInstituto = nombreInstituto;
        this.nombreEdicion = nombreEdicion;
        this.nombreTipoRegistro = nombreTipoRegistro;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public float getMontoAporte() {
        return montoAporte;
    }

    public int getCantRegistrosGrat() {
        return cantRegistrosGrat;
    }

    public String getCodigoPatrocinio() {
        return codigoPatrocinio;
    }

    public NivelPatrocinio getNivel() {
        return nivel;
    }

    public String getNombreInstituto() {
        return nombreInstituto;
    }

    public String getNombreEdicion() {
        return nombreEdicion;
    }

    public String getNombreTipoRegistro() {
        return nombreTipoRegistro;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public void setMontoAporte(float montoAporte) {
        this.montoAporte = montoAporte;
    }

    public void setCantRegistrosGrat(int cantRegistrosGrat) {
        this.cantRegistrosGrat = cantRegistrosGrat;
    }

    public void setCodigoPatrocinio(String codigoPatrocinio) {
        this.codigoPatrocinio = codigoPatrocinio;
    }

    public void setNivel(NivelPatrocinio nivel) {
        this.nivel = nivel;
    }

    public void setNombreInstituto(String nombreInstituto) {
        this.nombreInstituto = nombreInstituto;
    }

    public void setNombreEdicion(String nombreEdicion) {
        this.nombreEdicion = nombreEdicion;
    }

    public void setNombreTipoRegistro(String nombreTipoRegistro) {
        this.nombreTipoRegistro = nombreTipoRegistro;
    }

    @Override
    public String toString() {
        return codigoPatrocinio;
    }
}