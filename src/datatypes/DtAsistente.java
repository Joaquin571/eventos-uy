package datatypes;

import java.time.LocalDate;

public class DtAsistente extends DtUsuario {
    private String apellido;
    private LocalDate fechaNacimiento;

    public DtAsistente(String nickname, String nombre, String correoElectronico, String apellido, LocalDate fechaNacimiento) {
        super(nickname, nombre, correoElectronico);
        this.apellido = apellido;
        this.fechaNacimiento = fechaNacimiento;
    }
    public String getApellido() {return apellido;}
    public void setApellido(String apellido){this.apellido = apellido;}
    public LocalDate getFechaNacimiento() {return fechaNacimiento;}
    public void setFechaNacimiento(){this.fechaNacimiento = fechaNacimiento;}
}
