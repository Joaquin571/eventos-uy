package web;

import contratos.dto.DtRegistro;

import java.time.format.DateTimeFormatter;

public class DetalleRegistroVista {

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final String nickname;
    private final String nombreAsistente;
    private final String nombreEdicion;
    private final String nombreTipoRegistro;
    private final String fecha;
    private final String costo;
    private final String usoCodigo;
    private final boolean asistenciaConfirmada;

    public DetalleRegistroVista(String nickname, String nombreAsistente, String nombreEdicion,
                                String nombreTipoRegistro, String fecha, String costo,
                                String usoCodigo, boolean asistenciaConfirmada) {
        this.nickname = nickname;
        this.nombreAsistente = nombreAsistente;
        this.nombreEdicion = nombreEdicion;
        this.nombreTipoRegistro = nombreTipoRegistro;
        this.fecha = fecha;
        this.costo = costo;
        this.usoCodigo = usoCodigo;
        this.asistenciaConfirmada = asistenciaConfirmada;
    }

    public static DetalleRegistroVista de(DtRegistro registro, String nombreAsistente) {
        return new DetalleRegistroVista(
                registro.getNicknameAsistente(),
                nombreAsistente,
                registro.getNombreEdicion(),
                registro.getNombreTipoRegistro(),
                registro.getFechaRegistro() == null ? "" : registro.getFechaRegistro().format(FECHA),
                formatearCosto(registro.getCosto()),
                registro.isUsoCodigo() ? "Sí" : "No",
                registro.isAsistenciaConfirmada());
    }
    /** 1500 se muestra como "1500" y 800.5 se muestra como 800.50 */
    public static String formatearCosto(float costo) {
        return costo == Math.rint(costo) ? String.valueOf((long) costo) : String.format("%.2f", costo);
    }

    public String getNickname() { return nickname; }
    public String getNombreAsistente() { return nombreAsistente; }
    public String getNombreEdicion() { return nombreEdicion; }
    public String getNombreTipoRegistro() { return nombreTipoRegistro; }
    public String getFecha() { return fecha; }
    public String getCosto() { return costo; }
    public String getUsoCodigo() { return usoCodigo; }
    public boolean isAsistenciaConfirmada() { return asistenciaConfirmada; }
}