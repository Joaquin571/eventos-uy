package clases;

import java.time.LocalDate;

public class Evento {
    private String nombre;
    private String sigla;
    private String descripcion;
    private LocalDate fechaAlta;

    public Evento(String nombre, String sigla, String descripcion, LocalDate fechaAlta) {
        this.nombre = nombre;
        this.sigla = sigla;
        this.descripcion = descripcion;
        this.fechaAlta = fechaAlta;
    }
    public String getNombre() {return this.nombre;}
    public String getSigla() {return this.sigla;}
    public String getDescripcion() {return this.descripcion;}
    public LocalDate getFechaAlta() {return this.fechaAlta;}
    public void setNombre(String nombre) {this.nombre = nombre;}
    public void setSigla(String sigla) {this.sigla = sigla;}
    public void setDescripcion(String descripcion) {this.descripcion = descripcion;}
    public void setFechaAlta(LocalDate fechaAlta) {this.fechaAlta = fechaAlta;}
}
