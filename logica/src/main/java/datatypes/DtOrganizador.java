package datatypes;

public class DtOrganizador extends DtUsuario{
    private String descripcion;
    private String sitioWeb;

    public DtOrganizador(String nickname, String nombre, String correoElectronico, String descripcion, String sitioWeb){
        super(nickname, nombre, correoElectronico);
        this.descripcion = descripcion;
        this.sitioWeb = sitioWeb;
    }
    public String getDescripcion() {return descripcion;}
    public String getSitioWeb() {return sitioWeb;}
    public void setDescripcion(String descripcion) {this.descripcion = descripcion;}
    public void setSitioWeb(String sitioWeb) {this.sitioWeb = sitioWeb;}
}
