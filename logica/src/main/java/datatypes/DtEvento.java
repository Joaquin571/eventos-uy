package datatypes;

import jakarta.xml.bind.annotation.*;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import java.time.LocalDate;
import java.util.Collection;
import java.util.ArrayList;

@XmlAccessorType(XmlAccessType.FIELD)
public class DtEvento {
    public DtEvento() { }

    private String nombre;
    private String sigla;
    private String descripcion;
    @XmlJavaTypeAdapter(FechaXml.class)
    private LocalDate fechaAlta;
    private Collection<String> categorias;

    public DtEvento(String nombre, String sigla, String descripcion, LocalDate fechaAlta, Collection<String> categorias) {
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
    public Collection<String> getCategorias() {return categorias;}
    public void setNombre(String nombre) {this.nombre = nombre;}
    public void setSigla(String sigla) {this.sigla = sigla;}
    public void setDescripcion(String descripcion) {this.descripcion = descripcion;}
    public void setFechaAlta(LocalDate fechaAlta) {this.fechaAlta = fechaAlta;}
    @Override
    public String toString() {
        return nombre;
    }
    public void setCategorias(Collection<String> valor) { this.categorias = valor; }
}