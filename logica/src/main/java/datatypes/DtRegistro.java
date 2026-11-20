package datatypes;

import java.time.LocalDate;

public class DtRegistro {

    private LocalDate fechaRegistro;
    private float costo;
    private String nombreTipoRegistro;
    private String nombreEdicion;
    // Solo las consultas los completan: quién se registró y si usó código de patrocinio.
    // El código en sí nunca viaja en el DtRegistro.
    private String nicknameAsistente;
    private boolean usoCodigo;
    private boolean asistenciaConfirmada;

    public DtRegistro() {
    }

    public DtRegistro(
            LocalDate fechaRegistro,
            float costo
    ) {
        this.fechaRegistro = fechaRegistro;
        this.costo = costo;
    }

    public DtRegistro(
            LocalDate fechaRegistro,
            float costo,
            String nombreTipoRegistro,
            String nombreEdicion
    ) {
        this.fechaRegistro = fechaRegistro;
        this.costo = costo;
        this.nombreTipoRegistro = nombreTipoRegistro;
        this.nombreEdicion = nombreEdicion;
    }

    public DtRegistro(
            LocalDate fechaRegistro,
            float costo,
            String nombreTipoRegistro,
            String nombreEdicion,
            String nicknameAsistente,
            boolean usoCodigo
    ) {
        this(fechaRegistro, costo, nombreTipoRegistro, nombreEdicion);
        this.nicknameAsistente = nicknameAsistente;
        this.usoCodigo = usoCodigo;
    }

    public String getNombreTipoRegistro() {
        return nombreTipoRegistro;
    }

    public void setNombreTipoRegistro(String nombreTipoRegistro) {
        this.nombreTipoRegistro = nombreTipoRegistro;
    }

    public LocalDate getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(LocalDate fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    public String getNombreEdicion() {
        return nombreEdicion;
    }

    public void setNombreEdicion(String nombreEdicion) {
        this.nombreEdicion = nombreEdicion;
    }

    public float getCosto() {
        return costo;
    }

    public void setCosto(float costo) {
        this.costo = costo;
    }

    public String getNicknameAsistente() {
        return nicknameAsistente;
    }

    public void setNicknameAsistente(String nicknameAsistente) {
        this.nicknameAsistente = nicknameAsistente;
    }

    public boolean isUsoCodigo() {
        return usoCodigo;
    }

    public void setUsoCodigo(boolean usoCodigo) {
        this.usoCodigo = usoCodigo;
    }

    public boolean isAsistenciaConfirmada() {
        return asistenciaConfirmada;
    }

    public void setAsistenciaConfirmada(boolean asistenciaConfirmada) {
        this.asistenciaConfirmada = asistenciaConfirmada;
    }

    @Override
    public String toString() {
        return nombreEdicion + " - "
                + nombreTipoRegistro
                + " (" + fechaRegistro + ") - $"
                + costo;
    }
}