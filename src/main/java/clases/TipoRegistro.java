package clases;

import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "tipo_registro")
public class TipoRegistro {

    @Id
    private String idNombre;

    private String descripcion;
    private float costo;
    private int cupo;

    @ManyToOne
    @JoinColumn(name = "EDICION_ID")
    private Edicion edicion;

    @OneToMany(mappedBy = "tipoRegistro")
    private Set<Registro> registros = new HashSet<>();

    @OneToMany(mappedBy = "tipoRegistro")
    private Set<Patrocinio> patrocinios = new HashSet<>();

    protected TipoRegistro() {
    }

    public TipoRegistro(
            String idNombre,
            String descripcion,
            float costo,
            int cupo
    ) {
        this.idNombre = idNombre;
        this.descripcion = descripcion;
        this.costo = costo;
        this.cupo = cupo;
    }

    public String getIdNombre() {
        return idNombre;
    }

    public void setIdNombre(String idNombre) {
        this.idNombre = idNombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public float getCosto() {
        return costo;
    }

    public void setCosto(float costo) {
        this.costo = costo;
    }

    public int getCupo() {
        return cupo;
    }

    public void setCupo(int cupo) {
        this.cupo = cupo;
    }

    public Edicion getEdicion() {
        return edicion;
    }

    public void setEdicion(Edicion edicion) {
        this.edicion = edicion;
    }

    public Set<Registro> getRegistros() {
        return registros;
    }

    public Set<Patrocinio> getPatrocinios() {
        return patrocinios;
    }
}