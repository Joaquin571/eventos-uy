package clases;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Evento {

    private String nombre;
    private String sigla;
    private String descripcion;
    private LocalDate fechaAlta;

    private List<Categoria> categorias;
    private List<Edicion> ediciones;


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

        this.categorias = new ArrayList<>();
        this.ediciones = new ArrayList<>();
    }


    // =========================
    // CATEGORÍAS
    // =========================

    public void agregarCategoria(Categoria categoria) {
        categorias.add(categoria);
    }

    public List<Categoria> getCategorias() {

        return this.categorias;
    }

    public void setCategorias(List<Categoria> categorias) {
        this.categorias = categorias;
    }


    // =========================
    // EDICIONES
    // =========================

    public void agregarEdicion(Edicion edicion) {
        ediciones.add(edicion);
    }

    public List<Edicion> getEdiciones() {
        return ediciones;
    }

    public void setEdiciones(List<Edicion> ediciones) {
        this.ediciones = ediciones;
    }


    // =========================
    // DATOS EVENTO
    // =========================

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getSigla() {
        return sigla;
    }

    public void setSigla(String sigla) {
        this.sigla = sigla;
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

    public void setFechaAlta(LocalDate fechaAlta) {
        this.fechaAlta = fechaAlta;
    }
}