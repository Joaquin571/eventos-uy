package datatypes;

public class DtUsuario {
    private String nickname;
    private String nombre;
    private String correoElectronico;

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
    @Override
    public String toString() {
        return nombre + " (" + nickname + ")";
    }

}
