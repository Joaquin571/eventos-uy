package datatypes;

import java.time.LocalDate;

public class DtAsistente extends DtUsuario {
    private String apellido;
    private LocalDate fechaNacimiento;
    private  String nombreInstitucion;

    public DtAsistente(String nickname, String nombre, String correoElectronico, String apellido, LocalDate fechaNacimiento, String nombreInstitucion) {
        super(nickname, nombre, correoElectronico);
        this.apellido = apellido;
        this.fechaNacimiento = fechaNacimiento;
        this.nombreInstitucion = nombreInstitucion;
    }
    public String getApellido() {return apellido;}
    public void setApellido(String apellido){this.apellido = apellido;}
    public LocalDate getFechaNacimiento() {return fechaNacimiento;}
    public void setFechaNacimiento(){this.fechaNacimiento = fechaNacimiento;}
    public  String getNombreInstitucion() {return nombreInstitucion;}
    public void setNombreInstitucion(){this.nombreInstitucion = nombreInstitucion;}
}
