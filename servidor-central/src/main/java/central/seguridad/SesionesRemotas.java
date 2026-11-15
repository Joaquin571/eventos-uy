package central.seguridad;

import datatypes.Actor;
import datatypes.DtAsistente;
import datatypes.DtOrganizador;
import datatypes.DtUsuario;
import interfaces.ISistema;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/** Registro en memoria del Central. La autorización de recursos sigue en ISistema. */
public final class SesionesRemotas {
    private static final String LOGIN_INVALIDO = "El nickname, el correo o la contraseña no son correctos.";
    private static final String SESION_INVALIDA = "La sesión remota no es válida.";
    private static final Pattern FORMATO = Pattern.compile("[A-Za-z0-9_-]{43}");
    private static final Base64.Encoder CODIFICADOR = Base64.getUrlEncoder().withoutPadding();

    private final ISistema sistema;
    private final SecureRandom aleatorio;
    private final Clock reloj;
    private final Duration duracion;
    private final ConcurrentHashMap<String, SesionRemota> sesiones = new ConcurrentHashMap<>();

    public SesionesRemotas(ISistema sistema, SecureRandom aleatorio, Clock reloj, Duration duracion) {
        this.sistema = Objects.requireNonNull(sistema);
        this.aleatorio = Objects.requireNonNull(aleatorio);
        this.reloj = Objects.requireNonNull(reloj);
        this.duracion = Objects.requireNonNull(duracion);
        if (duracion.isZero() || duracion.isNegative()) {
            throw new IllegalArgumentException("La duración de una sesión debe ser positiva.");
        }
    }

    public SesionEmitida iniciar(String identificador, String contrasenia) {
        DtUsuario usuario;
        try {
            usuario = sistema.autenticar(identificador, contrasenia);
        } catch (IllegalArgumentException error) {
            throw new IllegalArgumentException(LOGIN_INVALIDO);
        } catch (RuntimeException error) {
            throw new IllegalStateException("No se pudo completar la autenticación.");
        }
        if (!(usuario instanceof DtAsistente || usuario instanceof DtOrganizador)
                || usuario.getNickname() == null || usuario.getNickname().isBlank()) {
            throw new IllegalArgumentException(LOGIN_INVALIDO);
        }

        Actor actor = Actor.usuarioAutenticado(usuario);
        String nombre = usuario.getNombre();
        limpiarVencidas();
        Instant venceEn = reloj.instant().plus(duracion);
        var sesion = new SesionRemota(actor, venceEn);
        byte[] bytes = new byte[32];
        String credencial;
        do {
            aleatorio.nextBytes(bytes);
            credencial = CODIFICADOR.encodeToString(bytes);
        } while (sesiones.putIfAbsent(credencial, sesion) != null);

        return new SesionEmitida(credencial, actor, nombre, venceEn);
    }

    public Actor requerirActor(String credencial) {
        if (!formatoValido(credencial)) throw new IllegalArgumentException(SESION_INVALIDA);
        SesionRemota sesion = sesiones.get(credencial);
        if (sesion == null) throw new IllegalArgumentException(SESION_INVALIDA);
        if (!reloj.instant().isBefore(sesion.venceEn())) {
            sesiones.remove(credencial, sesion);
            throw new IllegalArgumentException(SESION_INVALIDA);
        }
        return sesion.actor();
    }

    /** Cierre idempotente: no informa si la referencia existía, venció o ya fue revocada. */
    public void revocar(String credencial) {
        if (formatoValido(credencial)) sesiones.remove(credencial);
    }

    /** Purga oportunista al emitir sesiones, además de la eliminación al consultar una vencida. */
    int limpiarVencidas() {
        Instant ahora = reloj.instant();
        int eliminadas = 0;
        for (var entrada : sesiones.entrySet()) {
            SesionRemota sesion = entrada.getValue();
            if (!ahora.isBefore(sesion.venceEn()) && sesiones.remove(entrada.getKey(), sesion)) {
                eliminadas++;
            }
        }
        return eliminadas;
    }

    private static boolean formatoValido(String credencial) {
        return credencial != null && credencial.length() == 43 && FORMATO.matcher(credencial).matches();
    }

    private record SesionRemota(Actor actor, Instant venceEn) { }

    /** Resultado interno de autenticación. La credencial se entrega solo al adaptador autorizado. */
    public record SesionEmitida(String credencial, Actor actor, String nombre, Instant venceEn) {
        @Override
        public String toString() {
            return "SesionEmitida[credencial omitida]";
        }
    }
}
