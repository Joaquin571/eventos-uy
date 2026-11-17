package contratos.dto;
import java.time.LocalDate;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

public class DtResultadoBusqueda {
    private String tipo;
    private String nombre;
    private String descripcion;
    @XmlJavaTypeAdapter(FechaXml.class)
    private LocalDate fechaAlta;
    private String evento;
    public DtResultadoBusqueda() {}
    public DtResultadoBusqueda(String tipo, String nombre, String descripcion, LocalDate fechaAlta, String evento) {
        this.tipo=tipo;this.nombre=nombre;this.descripcion=descripcion;this.fechaAlta=fechaAlta;this.evento=evento;
    }
    public String getTipo() {return tipo;}
    public void setTipo(String v) {tipo=v;}
    public String getNombre() {return nombre;}
    public void setNombre(String v) {nombre=v;}
    public String getDescripcion() {return descripcion;}
    public void setDescripcion(String v) {descripcion=v;}
    public LocalDate getFechaAlta() {return fechaAlta;}
    public void setFechaAlta(LocalDate v) {fechaAlta=v;}
    public String getEvento() {return evento;}
    public void setEvento(String v) {evento=v;}
}
