package clases;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "evento")
public class Evento implements ConImagen {

    @Id
    private String nombre;

    private String sigla;
    private String descripcion;
    private LocalDate fechaAlta;

    // Solo el UPDATE atómico de visitas escribe este campo; los merges no lo restauran.
    @Column(name = "visitas", nullable = false, insertable = false, updatable = false,
            columnDefinition = "bigint default 0")
    private long visitas;

    // Marca compartida con la futura operación de finalización; no implica implementar ese caso.
    @Column(name = "finalizado", nullable = false, columnDefinition = "boolean default false")
    private boolean finalizado;

    public long getVisitas() { return visitas; }
    public boolean isFinalizado() { return finalizado; }
    public void setFinalizado(boolean finalizado) { this.finalizado = finalizado; }

    @ManyToMany
    @JoinTable(
            name = "evento_categoria",
            joinColumns = @JoinColumn(name = "EVENTO_NOMBRE"),
            inverseJoinColumns = @JoinColumn(name = "CATEGORIA_NOMBRE")
    )
    private Set<Categoria> categorias = new HashSet<>();

    @OneToMany(mappedBy = "evento")
    private Set<Edicion> ediciones = new HashSet<>();
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "imagen_id")
    private Imagen imagen;

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

    @Override
    public Imagen getImagen() {
        return imagen;
    }

    @Override
    public void setImagen(Imagen imagen) {
        this.imagen = imagen;
    }

}
