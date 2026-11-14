package web;

/** Fila de "Mis registros" lista para mostrar: las fechas y el costo ya vienen formateados. */
public class RegistroVista {

    private final String nombreEdicion;
    private final String nombreTipoRegistro;
    private final String costo;
    private final String fecha;

    public RegistroVista(String nombreEdicion, String nombreTipoRegistro, String costo, String fecha) {
        this.nombreEdicion = nombreEdicion;
        this.nombreTipoRegistro = nombreTipoRegistro;
        this.costo = costo;
        this.fecha = fecha;
    }

    public String getNombreEdicion() {
        return nombreEdicion;
    }

    public String getNombreTipoRegistro() {
        return nombreTipoRegistro;
    }

    public String getCosto() {
        return costo;
    }

    public String getFecha() {
        return fecha;
    }
}