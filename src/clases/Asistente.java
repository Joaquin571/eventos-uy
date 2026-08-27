package clases;

import java.time.LocalDate;

public class Asistente extends Usuario {
    private String apellido;
    private LocalDate fechaNacimiento;
    private String nombreInstitucion;

    public Asistente(String nickname, String nombre, String correoElectronico, String apellido, LocalDate fechaNacimiento,  String nombreInstitucion) {
        super(nickname,nombre,correoElectronico);
        this.apellido = apellido;
        this.fechaNacimiento = fechaNacimiento;
        this.nombreInstitucion = nombreInstitucion;
    }

    public String getApellido() {
        return apellido;
    }
    public LocalDate getFechaNacimiento(){
        return fechaNacimiento;
    }
    public String getNombreInstitucion() {return nombreInstitucion;}

    public void setApellido(String apellido){this.apellido=apellido;}
    public void setFechaNacimiento(LocalDate fechaNacimiento){this.fechaNacimiento=fechaNacimiento;}
    public void setNombreInstitucion(String nombreInstitucion){this.nombreInstitucion=nombreInstitucion;}
}
