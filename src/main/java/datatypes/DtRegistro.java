package datatypes;

import java.time.LocalDate;

public class DtRegistro {

    private LocalDate fechaRegistro;
    private float costo;

    public DtRegistro(
            LocalDate fechaRegistro,
            float costo
    ) {
        this.fechaRegistro = fechaRegistro;
        this.costo = costo;
    }

    public LocalDate getFechaRegistro() {
        return fechaRegistro;
    }

    public float getCosto() {
        return costo;
    }
}