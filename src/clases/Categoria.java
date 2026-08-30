package clases;

import java.util.ArrayList;
import java.util.List;

public class Categoria {
    private String nombre;
    private Categoria padre; // Será null si es una categoría raíz (sin padre)
    private List<Categoria> subcategorias;

    // Constructor para categorías raíz
    public Categoria(String nombre) {
        this(nombre, null);
    }

    // Constructor completo
    public Categoria(String nombre, Categoria padre) {
        this.nombre = nombre;
        this.padre = padre;
        this.subcategorias = new ArrayList<>();
    }

    public void agregarSubcategoria(Categoria subcategoria) {
        this.subcategorias.add(subcategoria);
    }

    // Getters y Setters
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public Categoria getPadre() { return padre; }
    public void setPadre(Categoria padre) { this.padre = padre; }

    public List<Categoria> getSubcategorias() { return subcategorias; }
}