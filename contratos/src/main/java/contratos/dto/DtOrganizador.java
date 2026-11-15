package contratos.dto;

import jakarta.xml.bind.annotation.*;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import java.time.LocalDate;

@XmlAccessorType(XmlAccessType.FIELD)
public class DtOrganizador extends DtUsuario{
    public DtOrganizador() { }

    private String descripcion;
    private String sitioWeb;

    public DtOrganizador(String nickname, String nombre, String correoElectronico, String descripcion, String sitioWeb){
        this(nickname, nombre, correoElectronico, descripcion, sitioWeb, null);
    }

    public DtOrganizador(String nickname, String nombre, String correoElectronico, String descripcion,
                         String sitioWeb, LocalDate fechaAlta) {
        super(nickname, nombre, correoElectronico, fechaAlta);
        this.descripcion = descripcion;
        this.sitioWeb = sitioWeb;
    }
    public String getDescripcion() {return descripcion;}
    public String getSitioWeb() {return sitioWeb;}
    public void setDescripcion(String descripcion) {this.descripcion = descripcion;}
    public void setSitioWeb(String sitioWeb) {this.sitioWeb = sitioWeb;}


}
