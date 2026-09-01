package datatypes;

public class DtTipoRegistro {

    private String idNombre;
    private String descripcion;
    private float costo;
    private int cupo;

    public DtTipoRegistro(
            String idNombre,
            String descripcion,
            float costo,
            int cupo
    ) {
        this.idNombre = idNombre;
        this.descripcion = descripcion;
        this.costo = costo;
        this.cupo = cupo;
    }

    public String getIdNombre() {
        return idNombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public float getCosto() {
        return costo;
    }

    public int getCupo() {
        return cupo;
    }

    @Override
    public String toString() {
        return idNombre;
    }
}