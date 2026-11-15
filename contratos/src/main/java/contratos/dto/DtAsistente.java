package contratos.dto;

import jakarta.xml.bind.annotation.*;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import java.time.LocalDate;

@XmlAccessorType(XmlAccessType.FIELD)
public class DtAsistente extends DtUsuario {
    public DtAsistente() { }

    private String apellido;
    @XmlJavaTypeAdapter(FechaXml.class)
    private LocalDate fechaNacimiento;
    private  String nombreInstitucion;

    public DtAsistente(String nickname, String nombre, String correoElectronico, String apellido, LocalDate fechaNacimiento, String nombreInstitucion) {
        this(nickname, nombre, correoElectronico, apellido, fechaNacimiento, nombreInstitucion, null);
    }

    public DtAsistente(String nickname, String nombre, String correoElectronico, String apellido,
                       LocalDate fechaNacimiento, String nombreInstitucion, LocalDate fechaAlta) {
        super(nickname, nombre, correoElectronico, fechaAlta);
        this.apellido = apellido;
        this.fechaNacimiento = fechaNacimiento;
        this.nombreInstitucion = nombreInstitucion;
    }
    public String getApellido() {return apellido;}
    public void setApellido(String apellido){this.apellido = apellido;}
    public LocalDate getFechaNacimiento() {return fechaNacimiento;}
    public  String getNombreInstitucion() {return nombreInstitucion;}
    public void setFechaNacimiento(LocalDate fechaNacimiento) {this.fechaNacimiento = fechaNacimiento;}
    public void setNombreInstitucion(String nombreInstitucion) {this.nombreInstitucion = nombreInstitucion;}



}
