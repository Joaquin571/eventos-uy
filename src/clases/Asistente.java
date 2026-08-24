package clases;

import java.time.LocalDate;

public class Asistente extends Usuario {
    private String apellido;
    private LocalDate fechaNacimiento;

    public Asistente(String nickname, String nombre, String correoElectronico, String apellido, LocalDate fechaNacimiento){
        super(nickname,nombre,correoElectronico);
        this.apellido = apellido;
        this.fechaNacimiento = fechaNacimiento;
    }

    public String getApellido() {
        return apellido;
    }
    public LocalDate getFechaNacimiento(){
        return fechaNacimiento;
    }

    public void setApellido(String apellido){}
    public void setFechaNacimiento(String fechaNacimiento){}
}
