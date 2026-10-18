package clases;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "asistente")
public class Asistente extends Usuario {

    private String apellido;
    private LocalDate fechaNacimiento;

    @ManyToOne
    @JoinColumn(name = "INSTITUCION_ID")
    private Institucion institucion;
    @OneToMany(mappedBy = "asistente")
    private Set<Registro> registros = new HashSet<>();

    protected Asistente(){}
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
    public Set<Registro> getRegistros() {return registros;}
    public void agregarRegistro(Registro registro) {registros.add(registro);}
}