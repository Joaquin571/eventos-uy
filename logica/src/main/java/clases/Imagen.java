package clases;

import jakarta.persistence.*;

@Entity
@Table(name = "imagen")
public class Imagen {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String tipoContenido;

    @Column(nullable = false)
    private byte[] contenido;

    protected Imagen() {
    }

    public Imagen(String tipoContenido, byte[] contenido) {
        this.tipoContenido = tipoContenido;
        this.contenido = contenido;
    }

    public String getTipoContenido() {
        return tipoContenido;
    }

    public byte[] getContenido() {
        return contenido;
    }
}