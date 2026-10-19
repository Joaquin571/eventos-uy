
package clases;

import jakarta.persistence.*;
import seguridad.HashContrasenia;

@Entity
@Table(name = "usuario")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Usuario {

    @Id
    private String nickname;

    private String nombre;

    @Column(nullable = false, unique = true)
    private String correoElectronico;
    @Column(name = "contrasenia_hash")
    private String contraseniaHash;

    protected Usuario() {}

    public Usuario(String nickname, String nombre, String correoElectronico) {
        this.nickname = nickname;
        this.nombre = nombre;
        this.correoElectronico = correoElectronico;
    }

    public String getNickname() {
        return nickname;
    }

    public String getNombre() {
        return nombre;
    }

    public String getCorreoElectronico() {
        return correoElectronico;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }


    public void setContrasenia(String contrasenia) {
        this.contraseniaHash = HashContrasenia.hashear(contrasenia);
    }

    public boolean contraseniaCoincide(String contrasenia) {
        return HashContrasenia.coincide(contrasenia, contraseniaHash);
    }
}
