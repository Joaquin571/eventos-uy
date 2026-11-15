package contratos.dto;

import jakarta.xml.bind.annotation.*;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlAccessorType(XmlAccessType.FIELD)
public class DtImagen {
    public DtImagen() { }


    private String tipoContenido;
    private byte[] contenido;

    public DtImagen(String tipoContenido, byte[] contenido) {
        this.tipoContenido = tipoContenido;
        this.contenido = contenido;
    }

    public String getTipoContenido() {
        return tipoContenido;
    }

    public byte[] getContenido() {
        return contenido;
    }

    public void setTipoContenido(String valor) { this.tipoContenido = valor; }
    public void setContenido(byte[] valor) { this.contenido = valor; }
}