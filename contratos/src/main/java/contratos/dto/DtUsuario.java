package contratos.dto;

import jakarta.xml.bind.annotation.*;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import java.time.LocalDate;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlSeeAlso({DtAsistente.class, DtOrganizador.class})
public class DtUsuario {
    public DtUsuario() { }

    private String nickname;
    private String nombre;
    private String correoElectronico;
    @XmlJavaTypeAdapter(FechaXml.class)
    private LocalDate fechaAlta;

    // Solo se usan en el alta y la modificación; las consultas nunca los completan.
    @XmlTransient
    private String contrasenia;
    @XmlTransient
    private String confirmacionContrasenia;

    public DtUsuario(String nickname, String nombre, String correoElectronico) {
        this(nickname, nombre, correoElectronico, null);
    }

    public DtUsuario(String nickname, String nombre, String correoElectronico, LocalDate fechaAlta) {
        this.fechaAlta = fechaAlta;
        this.nickname = nickname;
        this.nombre = nombre;
        this.correoElectronico = correoElectronico;
    }

    public LocalDate getFechaAlta() {return fechaAlta;}

    public String getNickname(){return this.nickname;}
    public String getNombre(){return this.nombre;}
    public String getCorreoElectronico(){return this.correoElectronico;}

    public void setNickname(String nickname){this.nickname = nickname;}
    public void setNombre(String nombre){this.nombre = nombre;}
    public void setCorreoElectronico(String correoElectronico){this.correoElectronico = correoElectronico;}

    public String getContrasenia() {return contrasenia;}
    public void setContrasenia(String contrasenia) {this.contrasenia = contrasenia;}
    public String getConfirmacionContrasenia() {return confirmacionContrasenia;}
    public void setConfirmacionContrasenia(String confirmacionContrasenia) {this.confirmacionContrasenia = confirmacionContrasenia;}

    @Override
    public String toString() {
        return nombre + " (" + nickname + ")";
    }

    public void setFechaAlta(LocalDate valor) { this.fechaAlta = valor; }
}