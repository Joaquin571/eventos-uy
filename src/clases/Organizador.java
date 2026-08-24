package clases;

public class Organizador extends Usuario{
    private String descripcion;
    private String sitioWeb;

    public Organizador(String nickname, String nombre, String correoElectronico, String descripcion, String sitioWeb){
        super(nickname,nombre,correoElectronico);
        this.descripcion = descripcion;
        this.sitioWeb = sitioWeb;
    }

    public String getDescripcion() {
        return descripcion;
    }
    public String getSitioWeb() {
        return sitioWeb;
    }
    public void setDescripcion(String descripcion) {}
    public void setSitioWeb(String sitioWeb) {}
}
