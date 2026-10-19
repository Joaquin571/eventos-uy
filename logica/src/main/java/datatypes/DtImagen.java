package datatypes;

public class DtImagen {

    private final String tipoContenido;
    private final byte[] contenido;

    public DtImagen(String tipoContenido, byte[] contenido) {
        this.tipoContenido = tipoContenido;
        this.contenido = contenido;
    }

    public String getTipoContenido() {
        return tipoContenido;
    }

    public byte[] getContenido() {
        return contenido;
    }
}