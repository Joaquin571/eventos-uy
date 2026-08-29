package datatypes;

import java.time.LocalDate;
import java.util.Set;

public class DtEvento {
    private String nombre;
    private String sigla;
    private String descripcion;
    private LocalDate fechaAlta;
    private Set<String> categorias;

    public DtEvento(String nombre, String sigla, String descripcion, LocalDate fechaAlta, Set<String> categorias) {
        this.nombre = nombre;
        this.sigla = sigla;
        this.descripcion = descripcion;
        this.fechaAlta = fechaAlta;
        this.categorias = categorias;
    }
    public String getNombre() {return this.nombre;}
    public String getSigla() {return this.sigla;}
    public String getDescripcion() {return this.descripcion;}
    public LocalDate getFechaAlta() {return this.fechaAlta;}
    public Set<String> getCategorias() { return categorias; }
    public void setNombre(String nombre) {this.nombre = nombre;}
    public void setSigla(String sigla) {this.sigla = sigla;}
    public void setDescripcion(String descripcion) {this.descripcion = descripcion;}
    public void setFechaAlta(LocalDate fechaAlta) {this.fechaAlta = fechaAlta;}
}
