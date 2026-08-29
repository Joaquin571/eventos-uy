package clases;

import java.time.LocalDate;

public class Asistente extends Usuario {
    private String apellido;
    private LocalDate fechaNacimiento;
    private Institucion institucion;

    public Asistente(String nickname, String nombre, String correoElectronico, String apellido, LocalDate fechaNacimiento,  Institucion institucion) {
        super(nickname,nombre,correoElectronico);
        this.apellido = apellido;
        this.fechaNacimiento = fechaNacimiento;
        this.institucion = institucion;
    }

    public String getApellido() {
        return apellido;
    }
    public LocalDate getFechaNacimiento(){
        return fechaNacimiento;
    }

    public Institucion getInstitucion() {    return institucion;}

    public void setInstitucion(Institucion institucion) { this.institucion = institucion;    }
    public void setApellido(String apellido){this.apellido=apellido;}
    public void setFechaNacimiento(LocalDate fechaNacimiento){this.fechaNacimiento=fechaNacimiento;}

}
