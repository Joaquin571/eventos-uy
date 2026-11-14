package datatypes;

public enum EstadoEdicion {
    INGRESADA("Ingresada"),
    ACEPTADA("Aceptada"),
    RECHAZADA("Rechazada");

    private final String descripcion;

    EstadoEdicion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }

    @Override
    public String toString() {
        return descripcion;
    }
}