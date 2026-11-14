package usuarios;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/** Registro del servidor, compartido por todas las instancias de la fachada. */
public final class SesionesUsuarios {
    private record Sesion(String nickname, Instant vence) { }
    private final Map<String, Sesion> sesiones = new HashMap<>();
    private final SecureRandom aleatorio = new SecureRandom();
    private final Clock reloj;
    private final Duration duracion;

    public SesionesUsuarios(Clock reloj, Duration duracion) {
        if (duracion.isNegative() || duracion.isZero()) throw new IllegalArgumentException("Duración inválida.");
        this.reloj = reloj;
        this.duracion = duracion;
    }

    public synchronized String crear(String nickname) {
        sesiones.values().removeIf(s -> !s.vence().isAfter(reloj.instant()));
        if (sesiones.size() >= 10000) throw new IllegalStateException("Límite de sesiones alcanzado.");
        byte[] bytes = new byte[32];
        String token;
        do {
            aleatorio.nextBytes(bytes);
            token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        } while (sesiones.containsKey(token));
        sesiones.put(token, new Sesion(nickname, reloj.instant().plus(duracion)));
        return token;
    }

    public synchronized String validar(String token) {
        Sesion sesion = sesiones.get(token);
        if (sesion == null || !sesion.vence().isAfter(reloj.instant())) {
            sesiones.remove(token);
            throw new ErrorAccesoUsuarios("SESION", "La sesión venció. Iniciá sesión nuevamente.");
        }
        sesiones.put(token, new Sesion(sesion.nickname(), reloj.instant().plus(duracion)));
        return sesion.nickname();
    }

    public synchronized void cerrar(String token) { sesiones.remove(token); }
}