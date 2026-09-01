package clases;
import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;


@Entity
@Table(name = "organizador")
public class Organizador extends Usuario{
    private String descripcion;
    private String sitioWeb;
    @OneToMany(mappedBy = "organizador")
    private Set<Edicion> ediciones = new HashSet<Edicion>();

    protected Organizador() {}
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
    public void setDescripcion(String descripcion) {this.descripcion = descripcion;}
    public void setSitioWeb(String sitioWeb) {this.sitioWeb = sitioWeb;}
    public void agregarEdicion(Edicion edicion) {
        ediciones.add(edicion);
        edicion.setOrganizador(this);
    }
}
