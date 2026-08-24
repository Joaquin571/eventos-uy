package clases;

public class Usuario {
    private String nickname;
    private String nombre;
    private String correoElectronico;

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