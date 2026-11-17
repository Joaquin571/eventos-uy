package datatypes;

/** Proyección administrativa del contador persistido, sin cargar el evento completo. */
public final class DtEventoVisitado {
    private final String nombre;
    private final long visitas;
    private final boolean finalizado;

    public DtEventoVisitado(String nombre, long visitas, boolean finalizado) {
        this.nombre = nombre;
        this.visitas = visitas;
        this.finalizado = finalizado;
    }

    public String getNombre() { return nombre; }
    public long getVisitas() { return visitas; }
    public boolean isFinalizado() { return finalizado; }
}
