
package datatypes;

public class DtCategoria {

    private String idNombre;
    private String nombrePadre;

    public DtCategoria(String idNombre, String nombrePadre) {
        this.idNombre = idNombre;
        this.nombrePadre = nombrePadre;
    }

    public String getIdNombre() {
        return idNombre;
    }

    public String getNombrePadre() {
        return nombrePadre;
    }

    @Override
    public String toString() {
        return idNombre;
    }
}
