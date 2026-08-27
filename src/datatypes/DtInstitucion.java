package datatypes;

public class DtInstitucion {
    private String Nombre;
    private String descripcion;
    private String sitioWeb;

    public DtInstitucion(String Nombre, String descripcion, String sitioWeb) {
        this.Nombre = Nombre;
        this.descripcion = descripcion;
        this.sitioWeb = sitioWeb;
    }
    public String getNombre() {return Nombre;}
    public void setNombre(String idNombre) {this.Nombre = idNombre;}
    public String getDescripcion() {return descripcion;}
    public void setDescripcion(String descripcion) {this.descripcion = descripcion;}
    public String getSitioWeb() {return sitioWeb;}
    public void setSitioWeb(String sitioWeb) {this.sitioWeb = sitioWeb;}
}
