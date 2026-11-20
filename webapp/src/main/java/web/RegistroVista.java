package web;

public class RegistroVista {

    private final String nombreEdicion;
    private final String nombreTipoRegistro;
    private final String costo;
    private final String fecha;
    private final boolean asistenciaConfirmada;

    public RegistroVista(String nombreEdicion, String nombreTipoRegistro, String costo,
                         String fecha, boolean asistenciaConfirmada) {
        this.nombreEdicion = nombreEdicion;
        this.nombreTipoRegistro = nombreTipoRegistro;
        this.costo = costo;
        this.fecha = fecha;
        this.asistenciaConfirmada = asistenciaConfirmada;
    }

    public String getNombreEdicion() { return nombreEdicion; }
    public String getNombreTipoRegistro() { return nombreTipoRegistro; }
    public String getCosto() { return costo; }
    public String getFecha() { return fecha; }
    public boolean isAsistenciaConfirmada() { return asistenciaConfirmada; }
}