package datatypes;

public class DtInstitucion {
    private String nombre;
    private String descripcion;
    private String sitioWeb;

    public DtInstitucion(String nombre, String descripcion, String sitioWeb) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.sitioWeb = sitioWeb;
    }
    public String getNombre() {return nombre;}
    public void setNombre(String idNombre) {this.nombre = idNombre;}
    public String getDescripcion() {return descripcion;}
    public void setDescripcion(String descripcion) {this.descripcion = descripcion;}
    public String getSitioWeb() {return sitioWeb;}
    public void setSitioWeb(String sitioWeb) {this.sitioWeb = sitioWeb;}
    @Override
    public String toString() {
        return nombre;
    }
}
