package clases;

import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "categoria")
public class Categoria {

    @Id
    private String nombre;

    @ManyToOne
    @JoinColumn(name = "PADRE_NOMBRE")
    private Categoria padre;

    @OneToMany(mappedBy = "padre")
    private Set<Categoria> subcategorias = new HashSet<>();

    @ManyToMany(mappedBy = "categorias")
    private Set<Evento> eventos = new HashSet<>();

    protected Categoria() {
    }

    public Categoria(String nombre) {
        this(nombre, null);
    }

    public Categoria(String nombre, Categoria padre) {
        this.nombre = nombre;
        this.padre = padre;
    }

    public void agregarSubcategoria(Categoria subcategoria) {
        subcategorias.add(subcategoria);
        subcategoria.setPadre(this);
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Categoria getPadre() {
        return padre;
    }

    public void setPadre(Categoria padre) {
        this.padre = padre;
    }

    public Set<Categoria> getSubcategorias() {
        return subcategorias;
    }

    public Set<Evento> getEventos() {
        return eventos;
    }
}