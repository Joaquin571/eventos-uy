package clases;

import java.time.LocalDate;

public class Registro {

    private LocalDate fechaRegistro;
    private float costo;

    public Registro(
            LocalDate fechaRegistro,
            float costo
    ) {
        this.fechaRegistro = fechaRegistro;
        this.costo = costo;
    }

    public LocalDate getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(LocalDate fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    public float getCosto() {
        return costo;
    }

    public void setCosto(float costo) {
        this.costo = costo;
    }
}