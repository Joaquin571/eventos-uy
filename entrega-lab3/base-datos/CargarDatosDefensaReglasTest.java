/** Prueba focalizada sin conexión ni escritura en PostgreSQL. */
class CargarDatosDefensaReglasTest {
    public static void main(String[] args) {
        CargarDatosDefensa.validarReglaPatrocinio(1500,3,25000);
        CargarDatosDefensa.validarReglaPatrocinio(850,3,20000);
        rechazar(1500,4,25000);
        rechazar(1500,3,20000);
        rechazar(850,-1,20000);
        rechazar(Float.NaN,1,20000);
        System.out.println("Regla del 20%: dos casos preparados válidos y cuatro casos inválidos rechazados.");
    }
    private static void rechazar(float costo, int cantidad, float aporte) {
        try {
            CargarDatosDefensa.validarReglaPatrocinio(costo,cantidad,aporte);
            throw new AssertionError("Se aceptó un patrocinio inválido.");
        } catch (IllegalArgumentException esperado) { /* correcto */ }
    }
}
