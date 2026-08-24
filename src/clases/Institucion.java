package clases;

public class Institucion {
    private String idNombre;
    private String descripcion;
    private String sitioWeb;

    public Institucion(String idNombre, String descripcion, String sitioWeb) {
        this.idNombre = idNombre;
        this.descripcion = descripcion;
        this.sitioWeb = sitioWeb;
    }
    public String getIdNombre() {return idNombre;}
    public void setIdNombre(String idNombre) {this.idNombre = idNombre;}
    public String getDescripcion() {return descripcion;}
    public void setDescripcion(String descripcion) {this.descripcion = descripcion;}
    public String getSitioWeb() {return sitioWeb;}
    public void setSitioWeb(String sitioWeb) {this.sitioWeb = sitioWeb;}
}
