package clases;

import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;

public class Asistente extends Usuario {
    private String apellido;
    private LocalDate fechaNacimiento;

    private String nombreInstitucion;
    private List<Registro> registros;

    private Institucion institucion;


    public Asistente(String nickname, String nombre, String correoElectronico, String apellido, LocalDate fechaNacimiento,  Institucion institucion) {
        super(nickname,nombre,correoElectronico);
        this.apellido = apellido;
        this.fechaNacimiento = fechaNacimiento;

        this.nombreInstitucion = nombreInstitucion;
        this.registros = new ArrayList<>();

        this.institucion = institucion;

    }

    public String getApellido() {
        return apellido;
    }
    public LocalDate getFechaNacimiento(){
        return fechaNacimiento;
    }

    public String getNombreInstitucion() {return nombreInstitucion;}
    public List<Registro> getRegistros(){
        return registros;
    }


    public Institucion getInstitucion() {    return institucion;}

    public void setInstitucion(Institucion institucion) { this.institucion = institucion;    }
    public void setApellido(String apellido){this.apellido=apellido;}
    public void setFechaNacimiento(LocalDate fechaNacimiento){this.fechaNacimiento=fechaNacimiento;}
    public void setNombreInstitucion(String nombreInstitucion){this.nombreInstitucion=nombreInstitucion;}
    public void agregarRegistro(Registro registro){
        this.registros.add(registro);
    }
}
