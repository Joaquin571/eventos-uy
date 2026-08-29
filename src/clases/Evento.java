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

    public Evento(String nombre, String sigla, String descripcion, LocalDate fechaAlta) {
        this.nombre = nombre;
        this.sigla = sigla;
        this.descripcion = descripcion;
        this.fechaAlta = fechaAlta;
        this.categorias = new ArrayList<>();
        this.ediciones = new ArrayList<>();
    }

    public void agregarCategoria(Categoria categoria) {
        this.categorias.add(categoria);
    }

    public List<Categoria> getCategorias() {
        return this.categorias;
    }
    public List<Edicion> getEdiciones(){
        return this.ediciones;
    }
    public String getNombre() {return this.nombre;}
    public String getSigla() {return this.sigla;}
    public String getDescripcion() {return this.descripcion;}
    public LocalDate getFechaAlta() {return this.fechaAlta;}
    public void setNombre(String nombre) {this.nombre = nombre;}
    public void setSigla(String sigla) {this.sigla = sigla;}
    public void setDescripcion(String descripcion) {this.descripcion = descripcion;}
    public void setFechaAlta(LocalDate fechaAlta) {this.fechaAlta = fechaAlta;}
    public void agregarEdicion(Edicion edicion){
        this.ediciones.add(edicion);
    }

}
