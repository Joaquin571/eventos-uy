package clases;

import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;

public class Asistente extends Usuario {
    private String apellido;
    private LocalDate fechaNacimiento;


    private List<Registro> registros;

    private Institucion institucion;


    public Asistente(String nickname, String nombre, String correoElectronico, String apellido, LocalDate fechaNacimiento,  Institucion institucion) {
        super(nickname,nombre,correoElectronico);
        this.apellido = apellido;
        this.fechaNacimiento = fechaNacimiento;


        this.registros = new ArrayList<>();

        this.institucion = institucion;

    }

    public String getApellido() {
        return apellido;
    }
    public LocalDate getFechaNacimiento(){
        return fechaNacimiento;
    }


    public List<Registro> getRegistros(){
        return registros;
    }


    public Institucion getInstitucion() {    return institucion;}

    public void setInstitucion(Institucion institucion) { this.institucion = institucion;    }
    public void setApellido(String apellido){this.apellido=apellido;}
    public void setFechaNacimiento(LocalDate fechaNacimiento){this.fechaNacimiento=fechaNacimiento;}
    public void agregarRegistro(Registro registro){
        this.registros.add(registro);
    }

    public boolean equals(Object object)
    {
        if( object instanceof Asistente)
        {
            Asistente asistente=(Asistente)object;
            if(asistente.getNickname().equals(getNickname()))
            {
                return true;
            }
            else{
                return false;
            }
        }
        else{
            return false;
        }
    }
}
