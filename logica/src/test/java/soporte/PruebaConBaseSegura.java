package soporte;

/**
 * La JVM inicializa esta superclase antes de los campos estáticos, constructores,
 * campos de instancia y preparaciones JUnit de cualquier prueba que la extienda.
 * No colocar preparaciones persistentes en interfaces o extensiones externas.
 */
public abstract class PruebaConBaseSegura {
    static {
        ProteccionBasePruebas.verificar();
    }
}
