package central.soap;

import jakarta.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;

/** Contrato de transporte: no contiene entidades ni tipos de persistencia. */
@XmlType(name = "EventoSoap", propOrder = {"nombre", "sigla", "descripcion", "fechaAlta", "categorias"})
public class EventoSoap {
    private String nombre;
    private String sigla;
    private String descripcion;
    private String fechaAlta;
    private List<String> categorias = new ArrayList<>();

    public EventoSoap() { }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getSigla() { return sigla; }
    public void setSigla(String sigla) { this.sigla = sigla; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public String getFechaAlta() { return fechaAlta; }
    public void setFechaAlta(String fechaAlta) { this.fechaAlta = fechaAlta; }
    public List<String> getCategorias() { return categorias; }
    public void setCategorias(List<String> categorias) { this.categorias = categorias; }
}
