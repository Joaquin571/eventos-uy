package clases;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Asistente extends Usuario {

    private String apellido;
    private LocalDate fechaNacimiento;

    private Institucion institucion;

    private List<Registro> registros;


    public Asistente(
            String nickname,
            String nombre,
            String correoElectronico,
            String apellido,
            LocalDate fechaNacimiento,
            Institucion institucion
    ) {
        super(nickname, nombre, correoElectronico);
        this.apellido = apellido;
        this.fechaNacimiento = fechaNacimiento;
        this.institucion = institucion;
        this.registros = new ArrayList<>();
    }

    public String getApellido() {return apellido;}
    public void setApellido(String apellido) {this.apellido = apellido;}
    public LocalDate getFechaNacimiento() {return fechaNacimiento;}
    public void setFechaNacimiento(LocalDate fechaNacimiento) {this.fechaNacimiento = fechaNacimiento;}
    public Institucion getInstitucion() {return institucion;}
    public void setInstitucion(Institucion institucion) {this.institucion = institucion;}
    public String getNombreInstitucion() {
        if (institucion == null) {
            return null;
        }
        return institucion.getNombre();}
    public List<Registro> getRegistros() {return registros;}
    public void agregarRegistro(Registro registro) {registros.add(registro);}
}