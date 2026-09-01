package clases;

import java.util.ArrayList;
import java.util.List;

public class Categoria {

    private String nombre;
    private Categoria padre;
    private List<Categoria> subcategorias;

    public Categoria(String nombre) {
        this(nombre, null);
    }

    public Categoria(String nombre, Categoria padre) {
        this.nombre = nombre;
        this.padre = padre;
        this.subcategorias = new ArrayList<>();
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

    public List<Categoria> getSubcategorias() {
        return subcategorias;
    }
}