package datatypes;

public class DtUsuario {
    private String nickname;
    private String nombre;
    private String correoElectronico;

    // Solo se usan en el alta y la modificación; las consultas nunca los completan.
    private String contrasenia;
    private String confirmacionContrasenia;

    public DtUsuario(String nickname, String nombre, String correoElectronico) {
        this.nickname = nickname;
        this.nombre = nombre;
        this.correoElectronico = correoElectronico;
    }

    public String getNickname(){return this.nickname;}
    public String getNombre(){return this.nombre;}
    public String getCorreoElectronico(){return this.correoElectronico;}

    public void setNickname(String nickname){this.nickname = nickname;}
    public void setNombre(String nombre){this.nombre = nombre;}
    public void setCorreoElectronico(String correoElectronico){this.correoElectronico = correoElectronico;}

    public String getContrasenia() {return contrasenia;}
    public void setContrasenia(String contrasenia) {this.contrasenia = contrasenia;}
    public String getConfirmacionContrasenia() {return confirmacionContrasenia;}
    public void setConfirmacionContrasenia(String confirmacionContrasenia) {this.confirmacionContrasenia = confirmacionContrasenia;}

    @Override
    public String toString() {
        return nombre + " (" + nickname + ")";
    }
}