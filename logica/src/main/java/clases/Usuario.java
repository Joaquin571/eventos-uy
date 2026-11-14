
package clases;

import jakarta.persistence.*;
import seguridad.HashContrasenia;
import java.time.LocalDate;

@Entity
@Table(name = "usuario")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Usuario implements ConImagen{

    @Id
    private String nickname;

    private String nombre;

    // Nullable para usuarios anteriores: no se inventa su fecha histórica.
    @Column(name = "fecha_alta", updatable = false)
    private LocalDate fechaAlta;

    @Column(nullable = false, unique = true)
    private String correoElectronico;
    @Column(name = "contrasenia_hash")
    private String contraseniaHash;
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "imagen_id")
    private Imagen imagen;

    protected Usuario() {}

    public Usuario(String nickname, String nombre, String correoElectronico) {
        this.fechaAlta = LocalDate.now();
        this.nickname = nickname;
        this.nombre = nombre;
        this.correoElectronico = correoElectronico;
    }

    public LocalDate getFechaAlta() {
        return fechaAlta;
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

    @Override
    public Imagen getImagen() {
        return imagen;
    }

    @Override
    public void setImagen(Imagen imagen) {
        this.imagen = imagen;
    }
}
