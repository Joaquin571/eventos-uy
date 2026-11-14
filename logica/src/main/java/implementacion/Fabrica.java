package implementacion;

import interfaces.ISistema;
import interfaces.IUsuariosWeb;
import usuarios.ControladorUsuariosWeb;
import usuarios.SesionesUsuarios;
import java.time.Clock;
import java.time.Duration;

public class Fabrica {

    private static Fabrica instancia;
    private static final SesionesUsuarios SESIONES = new SesionesUsuarios(Clock.systemUTC(), Duration.ofMinutes(30));

    public IUsuariosWeb getIUsuariosWeb() {
        return new ControladorUsuariosWeb(getISistema(), SESIONES);
    }

    private Fabrica() {
    }

    public static synchronized Fabrica getInstance() {

        if (instancia == null) {
            instancia = new Fabrica();
        }

        return instancia;
    }

    public ISistema getISistema() {
        return new Sistema();
    }
}