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

    public LocalDate getFechaRegistro() {
        return fechaRegistro;
    }

    public String getNombreEdicion() {
        return nombreEdicion;
    }

    public float getCosto() {
        return costo;
    }

    public String getNicknameAsistente() {
        return nicknameAsistente;
    }

    public boolean isUsoCodigo() {
        return usoCodigo;
    }

    @Override
    public String toString() {
        return nombreEdicion + " - "
                + nombreTipoRegistro
                + " (" + fechaRegistro + ") - $"
                + costo;
    }
}