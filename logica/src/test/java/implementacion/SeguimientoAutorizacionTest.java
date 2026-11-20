package implementacion;

import datatypes.Actor;
import excepciones.AccesoDenegadoException;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/** El rechazo sucede antes de abrir una conexion o consultar la existencia del destino. */
class SeguimientoAutorizacionTest {
    @Test void visitanteYAdministradorSinIdentidadNoPuedenCambiarRelaciones() {
        var sistema = Fabrica.getInstance().getISistema();
        for (Actor actor : new Actor[]{null, Actor.administradorLocal()}) {
            assertThrows(AccesoDenegadoException.class, () -> sistema.seguirUsuario("privado", actor));
            assertThrows(AccesoDenegadoException.class, () -> sistema.dejarDeSeguirUsuario("privado", actor));
        }
    }
}
