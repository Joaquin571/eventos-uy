package clases;

import java.time.LocalDate;

public class Asistente extends Usuario {
    private String apellido;
    private LocalDate fechaNacimiento;

    public Asistente(String nickname, String nombre, String email, String apellido, LocalDate fechaNacimiento){
        super(nickname,nombre,email);
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
