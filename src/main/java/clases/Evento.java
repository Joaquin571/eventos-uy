package clases;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "evento")
public class Evento {

    @Id
    private String nombre;

    private String sigla;
    private String descripcion;
    private LocalDate fechaAlta;

    @ManyToMany
    @JoinTable(
            name = "evento_categoria",
            joinColumns = @JoinColumn(name = "EVENTO_NOMBRE"),
            inverseJoinColumns = @JoinColumn(name = "CATEGORIA_NOMBRE")
    )
    private Set<Categoria> categorias = new HashSet<>();

    @OneToMany(mappedBy = "evento")
    private Set<Edicion> ediciones = new HashSet<>();

    protected Evento() {
    }

    public Evento(
            String nombre,
            String sigla,
            String descripcion,
            LocalDate fechaAlta
    ) {
        this.nombre = nombre;
        this.sigla = sigla;
        this.descripcion = descripcion;
        this.fechaAlta = fechaAlta;
    }

    public void agregarCategoria(Categoria categoria) {
        categorias.add(categoria);
    }
    public Set<Categoria> getCategorias() {
        return categorias;
    }
    public void setCategorias(Set<Categoria> categorias) {
        this.categorias = categorias;
    }

    public void agregarEdicion(Edicion edicion) {
        ediciones.add(edicion);
    }
    public Set<Edicion> getEdiciones() {
        return ediciones;
    }
    public void setEdiciones(Set<Edicion> ediciones) {
        this.ediciones = ediciones;
    }
    public String getNombre() {
        return nombre;
    }
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
    public String getSigla() {
        return sigla;
    }
    public String getDescripcion() {
        return descripcion;
    }
    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
    public LocalDate getFechaAlta() {
        return fechaAlta;
    }

}