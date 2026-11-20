package central.seguridad;

import datatypes.DtAsistente;
import datatypes.DtOrganizador;
import datatypes.DtUsuario;
import interfaces.ISistema;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.function.BiFunction;

import static org.junit.jupiter.api.Assertions.*;

class SesionesRemotasTest {
    private static final Duration DURACION = Duration.ofMinutes(10);
    private final RelojPrueba reloj = new RelojPrueba();
    private final DtOrganizador organizador = new DtOrganizador("org", "Organizador", "org@test.uy", "Descripción", "");
    private final DtAsistente asistente = new DtAsistente("asis", "Asistente", "asis@test.uy", "Apellido", null, null);

    @Test
    void loginValidoDelegaLasCredencialesYEmite32Bytes() {
        var registro = registro((identificador, contrasenia) -> {
            assertEquals("org@test.uy", identificador);
            assertEquals("clave de prueba", contrasenia);
            return organizador;
        });

        var sesion = registro.iniciar("org@test.uy", "clave de prueba");

        assertTrue(sesion.credencial().matches("[A-Za-z0-9_-]{43}"));
        assertEquals(32, Base64.getUrlDecoder().decode(sesion.credencial()).length);
        assertEquals(reloj.instant().plus(DURACION), sesion.venceEn());
        assertEquals("Organizador", sesion.nombre());
        assertEquals("org", registro.requerirActor(sesion.credencial()).nickname());
        assertFalse(sesion.toString().contains(sesion.credencial()));
    }

    @Test
    void loginInvalidoNoPropagaCredencialesDelErrorOriginal() {
        var registro = registro((identificador, contrasenia) -> {
            throw new IllegalArgumentException(identificador + " " + contrasenia);
        });
        var error = assertThrows(IllegalArgumentException.class,
                () -> registro.iniciar("usuario inválido", "secreto de prueba"));
        assertEquals("El nickname, el correo o la contraseña no son correctos.", error.getMessage());
        assertNull(error.getCause());
    }

    @Test
    void fallasTecnicasDeAutenticacionNoPropaganCredenciales() {
        var registro = registro((identificador, contrasenia) -> {
            throw new IllegalStateException(contrasenia);
        });
        var error = assertThrows(IllegalStateException.class, () -> registro.iniciar("org", "secreto de prueba"));
        assertEquals("No se pudo completar la autenticación.", error.getMessage());
        assertNull(error.getCause());
    }

    @Test
    void dosLoginsDelMismoUsuarioSonIndependientesYRevocarUnoNoAfectaAlOtro() {
        var registro = registro((id, clave) -> organizador);
        var primera = registro.iniciar("org", "clave");
        var segunda = registro.iniciar("org", "clave");
        assertNotEquals(primera.credencial(), segunda.credencial());
        assertEquals("org", registro.requerirActor(primera.credencial()).nickname());
        assertEquals("org", registro.requerirActor(segunda.credencial()).nickname());

        assertDoesNotThrow(() -> registro.revocar(primera.credencial()));
        comprobarSesionInvalida(registro, primera.credencial());
        assertDoesNotThrow(() -> registro.revocar(primera.credencial()));
        assertEquals("org", registro.requerirActor(segunda.credencial()).nickname());
    }

    @Test
    void venceEnElInstanteExactoAunqueSeConsulteJustoAntes() {
        var registro = registro((id, clave) -> organizador);
        var sesion = registro.iniciar("org", "clave");
        reloj.avanzar(DURACION.minusNanos(1));
        assertEquals("org", registro.requerirActor(sesion.credencial()).nickname());
        reloj.avanzar(Duration.ofNanos(1));
        comprobarSesionInvalida(registro, sesion.credencial());
        assertEquals(0, registro.limpiarVencidas());
    }

    @Test
    void revocarUnaReferenciaDesconocidaOVencidaEsIdempotente() {
        var registro = registro((id, clave) -> organizador);
        String desconocida = Base64.getUrlEncoder().withoutPadding().encodeToString(new byte[32]);
        assertDoesNotThrow(() -> registro.revocar(desconocida));
        var sesion = registro.iniciar("org", "clave");
        reloj.avanzar(DURACION);
        assertDoesNotThrow(() -> registro.revocar(sesion.credencial()));
        assertDoesNotThrow(() -> registro.revocar(sesion.credencial()));
        comprobarSesionInvalida(registro, sesion.credencial());
    }

    @Test
    void rechazaReferenciasAusentesDesconocidasManipuladasYMalformadasSinExponerlas() {
        var registro = registro((id, clave) -> organizador);
        var sesion = registro.iniciar("org", "clave");
        String referencia = sesion.credencial();
        String alterada = (referencia.startsWith("A") ? "B" : "A") + referencia.substring(1);
        var otroRegistro = registro((id, clave) -> organizador);

        comprobarSesionInvalida(otroRegistro, referencia);
        comprobarSesionInvalida(registro, alterada);
        comprobarSesionInvalida(registro, referencia + "=");
        comprobarSesionInvalida(registro, "referencia inválida");
        comprobarSesionInvalida(registro, null);
        assertEquals("org", registro.requerirActor(referencia).nickname());
    }

    @Test
    void actorReflejaLaIdentidadAutenticadaYNoElIdentificadorAportado() {
        var registro = registro((id, clave) -> asistente);
        var sesion = registro.iniciar("asis@test.uy", "clave");
        asistente.setNickname("otro usuario");

        var actor = registro.requerirActor(sesion.credencial());
        assertEquals("asis", actor.nickname());
        assertFalse(actor.esOrganizador());
        assertFalse(actor.esAdministradorLocal());
    }

    @Test
    void loginRemotoNoPuedeCrearUnActorAdministrativo() {
        var registro = registro((id, clave) -> organizador);
        var actor = registro.requerirActor(registro.iniciar("org", "clave").credencial());
        assertTrue(actor.esOrganizador());
        assertFalse(actor.esAdministradorLocal());

        var sinRol = registro((id, clave) -> new DtUsuario("admin", "Administrador", "admin@test.uy"));
        assertThrows(IllegalArgumentException.class, () -> sinRol.iniciar("admin", "clave"));
    }

    @Test
    void limpiaVencidasSinAfectarVigentesYEmisionesPurganSesionesSinUso() {
        var registro = registro((id, clave) -> organizador);
        var vencida = registro.iniciar("org", "clave");
        reloj.avanzar(DURACION.dividedBy(2));
        var vigente = registro.iniciar("org", "clave");
        reloj.avanzar(DURACION.dividedBy(2));
        assertEquals(1, registro.limpiarVencidas());
        comprobarSesionInvalida(registro, vencida.credencial());
        assertEquals("org", registro.requerirActor(vigente.credencial()).nickname());

        reloj.avanzar(DURACION);
        var nueva = registro.iniciar("org", "clave");
        assertEquals(0, registro.limpiarVencidas());
        comprobarSesionInvalida(registro, vigente.credencial());
        assertEquals("org", registro.requerirActor(nueva.credencial()).nickname());
    }

    private SesionesRemotas registro(BiFunction<String, String, DtUsuario> autenticar) {
        ISistema sistema = (ISistema) Proxy.newProxyInstance(ISistema.class.getClassLoader(),
                new Class<?>[]{ISistema.class}, (proxy, metodo, args) -> {
                    assertEquals("autenticar", metodo.getName());
                    return autenticar.apply((String) args[0], (String) args[1]);
                });
        return new SesionesRemotas(sistema, new SecureRandom(), reloj, DURACION);
    }

    private static void comprobarSesionInvalida(SesionesRemotas registro, String credencial) {
        var error = assertThrows(IllegalArgumentException.class, () -> registro.requerirActor(credencial));
        assertEquals("La sesión remota no es válida.", error.getMessage());
        assertNull(error.getCause());
    }

    private static final class RelojPrueba extends Clock {
        private Instant ahora = Instant.parse("2026-11-10T12:00:00Z");

        void avanzar(Duration tiempo) { ahora = ahora.plus(tiempo); }
        @Override public ZoneId getZone() { return ZoneOffset.UTC; }
        @Override public Clock withZone(ZoneId zona) { return Clock.fixed(ahora, zona); }
        @Override public Instant instant() { return ahora; }
    }
}
