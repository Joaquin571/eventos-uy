package clases;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Usuario {
    @Id
    private String nickname;
    private String nombre;
    private String correoElectronico;

    protected Usuario() {}
    public Usuario(String nickname, String nombre, String correoElectronico) {
        this.nickname = nickname;
        this.nombre = nombre;
        this.correoElectronico = correoElectronico;
    }

    public String getNickname(){return nickname;};
    public String getNombre(){return  nombre;};
    public String getCorreoElectronico(){return   correoElectronico;};

    public void setNickname(String nickname){this.nickname = nickname;};
    public void setNombre(String nombre){this.nombre = nombre;};
    public void setCorreoElectronico(String correoElectronico){this.correoElectronico = correoElectronico;};

}