package clases;

import java.util.ArrayList;

public class TipoRegistro {

    private String idNombre;
    private String descripcion;
    private float costo;
    private int cupo;
    private ArrayList<Registro> listaRegistro;
    public TipoRegistro(
            String idNombre,
            String descripcion,
            float costo,
            int cupo
    ) {
        this.idNombre = idNombre;
        this.descripcion = descripcion;
        this.costo = costo;
        this.cupo = cupo;
        this.listaRegistro=new ArrayList<>();
    }

    public String getIdNombre() {
        return idNombre;
    }

    public void setIdNombre(String idNombre) {
        this.idNombre = idNombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public float getCosto() {
        return costo;
    }

    public void setCosto(float costo) {
        this.costo = costo;
    }

    public int getCupo() {
        return cupo;
    }

    public void setCupo(int cupo) {
        this.cupo = cupo;
    }

    public boolean hayCupo()
    {
        return listaRegistro.size()<cupo;
    }

    public boolean existeAsistente(Asistente asistente)
    {
        for(Registro registro : listaRegistro)
        {
            if(registro.getAsistente().equals(asistente))
            {
                return true;
            }
        }
        return false;
    }

    public void agregarRegistro(Registro registro)throws Exception
    {
        if(hayCupo())
            if(!existeAsistente(registro.getAsistente()))
                listaRegistro.add(registro);
            else
                throw new Exception("El asistente ya esta registrado");
        else
            throw new Exception("No hay mas cupos");
    }
}