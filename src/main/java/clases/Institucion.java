package clases;
import java.util.HashSet;
import java.util.Set;
import jakarta.persistence.*;

@Entity
@Table(name = "institucion")
public class Institucion {
    @Id
    private String nombre;
    private String descripcion;
    private String sitioWeb;
    @OneToMany(mappedBy = "institucion")
    private Set<Asistente> asistentes = new HashSet<>();
    @OneToMany(mappedBy = "institucion")
    private Set<Patrocinio> patrocinios = new HashSet<>();

    protected Institucion() {}
    public Institucion(String Nombre, String descripcion, String sitioWeb) {
        this.nombre = Nombre;
        this.descripcion = descripcion;
        this.sitioWeb = sitioWeb;
    }
    public String getNombre() {return nombre;}
    public void setNombre(String nombre) {this.nombre = nombre;}
    public String getDescripcion() {return descripcion;}
    public void setDescripcion(String descripcion) {this.descripcion = descripcion;}
    public String getSitioWeb() {return sitioWeb;}
    public void setSitioWeb(String sitioWeb) {this.sitioWeb = sitioWeb;}
}
