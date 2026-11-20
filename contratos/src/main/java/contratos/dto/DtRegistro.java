package contratos.dto;

import jakarta.xml.bind.annotation.*;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import java.time.LocalDate;

@XmlAccessorType(XmlAccessType.FIELD)
public class DtRegistro {
    public DtRegistro() { }

    @XmlJavaTypeAdapter(FechaXml.class)
    private LocalDate fechaRegistro;
    private float costo;
    private String nombreTipoRegistro;
    private String nombreEdicion;
    // Solo las consultas los completan: quién se registró y si usó código de patrocinio.
    // El código en sí nunca viaja en el DtRegistro.
    private String nicknameAsistente;
    private boolean usoCodigo;
    private boolean asistenciaConfirmada;

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

    public boolean isAsistenciaConfirmada() {
        return asistenciaConfirmada;
    }

    @Override
    public String toString() {
        return nombreEdicion + " - "
                + nombreTipoRegistro
                + " (" + fechaRegistro + ") - $"
                + costo;
    }

    public void setFechaRegistro(LocalDate valor) { this.fechaRegistro = valor; }
    public void setCosto(float valor) { this.costo = valor; }
    public void setNombreTipoRegistro(String valor) { this.nombreTipoRegistro = valor; }
    public void setNombreEdicion(String valor) { this.nombreEdicion = valor; }
    public void setNicknameAsistente(String valor) { this.nicknameAsistente = valor; }
    public void setUsoCodigo(boolean valor) { this.usoCodigo = valor; }
    public void setAsistenciaConfirmada(boolean valor) { this.asistenciaConfirmada = valor; }
}