package usuarios;

import org.junit.jupiter.api.Test;
import java.time.*;
import static org.junit.jupiter.api.Assertions.*;

class SesionesUsuariosTest {
    private static class Reloj extends Clock {
        Instant ahora = Instant.parse("2026-11-14T12:00:00Z");
        public ZoneId getZone() { return ZoneOffset.UTC; }
        public Clock withZone(ZoneId zona) { return this; }
        public Instant instant() { return ahora; }
        void avanzar(int minutos) { ahora = ahora.plusSeconds(minutos * 60L); }
    }
    @Test void generaTokensDistintosYNoAceptaNicknameComoToken() {
        var sesiones = new SesionesUsuarios(Clock.systemUTC(), Duration.ofMinutes(30));
        String uno = sesiones.crear("ana"), dos = sesiones.crear("ana");
        assertNotEquals(uno, dos);
        assertEquals("ana", sesiones.validar(uno));
        assertThrows(ErrorAccesoUsuarios.class, () -> sesiones.validar("ana"));
        assertThrows(ErrorAccesoUsuarios.class, () -> sesiones.validar(null));
    }
    @Test void vencimientoDeslizanteYCierreIndependiente() {
        Reloj reloj = new Reloj();
        var sesiones = new SesionesUsuarios(reloj, Duration.ofMinutes(30));
        String uno = sesiones.crear("ana"), dos = sesiones.crear("bob");
        reloj.avanzar(29);
        assertEquals("ana", sesiones.validar(uno));
        reloj.avanzar(2);
        assertThrows(ErrorAccesoUsuarios.class, () -> sesiones.validar(dos));
        assertEquals("ana", sesiones.validar(uno));
        sesiones.cerrar(uno);
        sesiones.cerrar(uno);
        assertThrows(ErrorAccesoUsuarios.class, () -> sesiones.validar(uno));
    }
    @Test void expiraExactamenteAlCumplirElPlazo() {
        Reloj reloj = new Reloj();
        var sesiones = new SesionesUsuarios(reloj, Duration.ofMinutes(30));
        String token = sesiones.crear("ana");
        reloj.avanzar(30);
        assertThrows(ErrorAccesoUsuarios.class, () -> sesiones.validar(token));
    }
    @Test void rechazaDuracionesNoPositivas() {
        assertThrows(IllegalArgumentException.class, () -> new SesionesUsuarios(Clock.systemUTC(), Duration.ZERO));
        assertThrows(IllegalArgumentException.class, () -> new SesionesUsuarios(Clock.systemUTC(), Duration.ofMinutes(-1)));
    }
}