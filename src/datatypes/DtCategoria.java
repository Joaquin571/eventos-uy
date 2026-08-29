package datatypes;

public class DtCategoria {

    private String idNombre;

    public DtCategoria(String idNombre) {
        this.idNombre = idNombre;
    }

    public String getIdNombre() {
        return idNombre;
    }

    @Override
    public String toString() {
        return idNombre;
    }
}