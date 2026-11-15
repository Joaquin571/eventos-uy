package implementacion;

import clases.Asistente;
import clases.Imagen;
import clases.Organizador;
import clases.Usuario;
import datatypes.*;
import interfaces.ISistema;
import jakarta.persistence.PersistenceException;
import manejadores.ManejadorUsuarios;
import org.junit.jupiter.api.Test;
import persistencia.BaseDeDatos;
import soporte.ImagenesPrueba;
import soporte.PruebaConBaseSegura;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/** Adapta la atomicidad de Bentancor al alta local, sin incorporar sesiones ni transporte. */
class AltaUsuarioConImagenTest extends PruebaConBaseSegura {
    private final ISistema sistema = Fabrica.getInstance().getISistema();

    private DtUsuario datos(boolean organizador) {
        String nick = "altaImg" + UUID.randomUUID().toString().replace("-", "");
        DtUsuario dt = organizador
                ? new DtOrganizador(nick, "Organización", nick + "@test.uy", "Descripción", "https://test.uy")
                : new DtAsistente(nick, "Ana", nick + "@test.uy", "Pérez", LocalDate.of(2000, 1, 1), null);
        dt.setContrasenia("clave-de-prueba");
        dt.setConfirmacionContrasenia("clave-de-prueba");
        return dt;
    }

    private DtImagen imagen() {
        return new DtImagen("image/png", ImagenesPrueba.imagen("png"));
    }

    private boolean alta(DtUsuario dt, DtImagen imagen) {
        return dt instanceof DtAsistente asistente
                ? sistema.altaAsistente(asistente, imagen)
                : sistema.altaOrganizador((DtOrganizador) dt, imagen);
    }

    private long cantidadImagenes() {
        try (var em = BaseDeDatos.getEntityManager()) {
            return em.createQuery("SELECT COUNT(i) FROM Imagen i", Long.class).getSingleResult();
        }
    }

    private void comprobarPersistido(DtUsuario dt, DtImagen imagen) {
        // Un EntityManager nuevo comprueba lo confirmado, no el objeto usado para dar de alta.
        try (var em = BaseDeDatos.getEntityManager()) {
            Usuario usuario = em.find(Usuario.class, dt.getNickname());
            assertNotNull(usuario);
            assertEquals(dt.getNombre(), usuario.getNombre());
            assertEquals(dt.getCorreoElectronico(), usuario.getCorreoElectronico());
            assertEquals(LocalDate.now(), usuario.getFechaAlta());
            assertTrue(usuario.contraseniaCoincide("clave-de-prueba"));
            if (imagen == null) {
                assertNull(usuario.getImagen());
            } else {
                assertNotNull(usuario.getImagen());
                assertEquals("image/png", usuario.getImagen().getTipoContenido());
                assertArrayEquals(imagen.getContenido(), usuario.getImagen().getContenido());
            }
            String hash = (String) em.createNativeQuery(
                    "SELECT contrasenia_hash FROM usuario WHERE nickname = :nick")
                    .setParameter("nick", dt.getNickname()).getSingleResult();
            assertNotNull(hash);
            assertNotEquals(dt.getContrasenia(), hash);
        }
        DtUsuario autenticado = sistema.autenticar(dt.getNickname(), "clave-de-prueba");
        assertEquals(dt.getNickname(), autenticado.getNickname());
        assertNull(autenticado.getContrasenia());
        assertNull(autenticado.getConfirmacionContrasenia());
    }

    private void comprobarAusente(String nick, long imagenesAntes) {
        try (var em = BaseDeDatos.getEntityManager()) {
            assertNull(em.find(Usuario.class, nick));
            assertEquals(0L, em.createNativeQuery(
                    "SELECT COUNT(*) FROM asistente WHERE nickname = :nick", Long.class)
                    .setParameter("nick", nick).getSingleResult());
            assertEquals(0L, em.createNativeQuery(
                    "SELECT COUNT(*) FROM organizador WHERE nickname = :nick", Long.class)
                    .setParameter("nick", nick).getSingleResult());
        }
        assertEquals(imagenesAntes, cantidadImagenes(), "No deben quedar imágenes huérfanas.");
    }

    @Test
    void ambosSubtiposGuardanDatosClaveEImagenOriginalEnLaMismaAlta() {
        for (boolean organizador : new boolean[]{false, true}) {
            DtUsuario dt = datos(organizador);
            DtImagen imagen = new DtImagen("IMAGE/PNG", imagen().getContenido());
            long antes = cantidadImagenes();
            assertTrue(alta(dt, imagen));
            assertEquals(antes + 1, cantidadImagenes());
            comprobarPersistido(dt, imagen);
        }
    }

    @Test
    void firmasAntiguasSiguenCreandoAmbosSubtiposSinImagen() {
        for (boolean organizador : new boolean[]{false, true}) {
            DtUsuario dt = datos(organizador);
            long antes = cantidadImagenes();
            assertTrue(organizador ? sistema.altaOrganizador((DtOrganizador) dt)
                    : sistema.altaAsistente((DtAsistente) dt));
            comprobarPersistido(dt, null);
            assertEquals(antes, cantidadImagenes());
        }
    }

    @Test
    void sobrecargasAceptanImagenNulaSinCambiarLaAutenticacion() {
        for (boolean organizador : new boolean[]{false, true}) {
            DtUsuario dt = datos(organizador);
            long antes = cantidadImagenes();
            assertTrue(alta(dt, null));
            comprobarPersistido(dt, null);
            assertEquals(antes, cantidadImagenes());
        }
    }

    @Test
    void imagenInvalidaNoCreaUsuarioNiImagenEnNingunSubtipo() {
        List<DtImagen> invalidas = List.of(
                new DtImagen("image/png", new byte[]{1, 2, 3}),
                new DtImagen("image/png", new byte[0]),
                new DtImagen("image/png", null),
                new DtImagen("text/plain", imagen().getContenido()),
                new DtImagen("image/png", new byte[2 * 1024 * 1024 + 1]));
        for (boolean organizador : new boolean[]{false, true}) {
            for (DtImagen invalida : invalidas) {
                DtUsuario dt = datos(organizador);
                long antes = cantidadImagenes();
                assertThrows(IllegalArgumentException.class, () -> alta(dt, invalida));
                comprobarAusente(dt.getNickname(), antes);
            }
        }
    }

    @Test
    void datosInvalidosNoDejanImagenAunqueLaImagenSeaValida() {
        for (boolean organizador : new boolean[]{false, true}) {
            for (String campo : List.of("nombre", "confirmacion")) {
                DtUsuario dt = datos(organizador);
                if (campo.equals("nombre")) dt.setNombre(" ");
                else dt.setConfirmacionContrasenia("distinta");
                long antes = cantidadImagenes();
                assertThrows(IllegalArgumentException.class, () -> alta(dt, imagen()));
                comprobarAusente(dt.getNickname(), antes);
            }
        }
    }

    @Test
    void nicknameDuplicadoNoReemplazaDatosClaveOImagenExistentes() {
        for (boolean organizador : new boolean[]{false, true}) {
            DtUsuario original = datos(organizador);
            DtImagen imagenOriginal = imagen();
            assertTrue(alta(original, imagenOriginal));
            DtUsuario duplicado = datos(!organizador);
            duplicado.setNickname(original.getNickname());
            duplicado.setContrasenia("otra-clave");
            duplicado.setConfirmacionContrasenia("otra-clave");
            long antes = cantidadImagenes();
            assertThrows(IllegalArgumentException.class, () -> alta(duplicado, imagen()));
            assertEquals(antes, cantidadImagenes());
            comprobarPersistido(original, imagenOriginal);
            assertThrows(IllegalArgumentException.class,
                    () -> sistema.autenticar(original.getNickname(), "otra-clave"));
        }
    }

    @Test
    void correoDuplicadoSinDistinguirMayusculasNoCreaUsuarioNiImagen() {
        for (boolean organizador : new boolean[]{false, true}) {
            DtUsuario original = datos(organizador);
            assertTrue(alta(original, imagen()));
            DtUsuario duplicado = datos(!organizador);
            duplicado.setCorreoElectronico(original.getCorreoElectronico().toUpperCase(java.util.Locale.ROOT));
            long antes = cantidadImagenes();
            assertThrows(IllegalArgumentException.class, () -> alta(duplicado, imagen()));
            comprobarAusente(duplicado.getNickname(), antes);
            comprobarPersistido(original, imagen());
        }
    }

    private Usuario entidad(DtUsuario dt) {
        Usuario usuario = dt instanceof DtAsistente asistente
                ? new Asistente(dt.getNickname(), dt.getNombre(), dt.getCorreoElectronico(),
                        asistente.getApellido(), asistente.getFechaNacimiento(), null)
                : new Organizador(dt.getNickname(), dt.getNombre(), dt.getCorreoElectronico(),
                        ((DtOrganizador) dt).getDescripcion(), ((DtOrganizador) dt).getSitioWeb());
        usuario.setContrasenia(dt.getContrasenia());
        return usuario;
    }

    private void comprobarFalloSqlDeLongitud(Throwable error) {
        for (Throwable causa = error; causa != null; causa = causa.getCause()) {
            if (causa instanceof SQLException sql && "22001".equals(sql.getSQLState())) return;
        }
        fail("Se esperaba un fallo SQL 22001 real, no solo una validación preventiva.", error);
    }

    @Test
    void falloRealAlPersistirImagenRevierteElAltaDeAmbosSubtipos() {
        for (boolean organizador : new boolean[]{false, true}) {
            DtUsuario dt = datos(organizador);
            Usuario usuario = entidad(dt);
            usuario.setImagen(new Imagen("x".repeat(256), imagen().getContenido()));
            long antes = cantidadImagenes();
            // Se entra por el manejador para provocar el error SQL que Sistema previene.
            comprobarFalloSqlDeLongitud(assertThrows(PersistenceException.class,
                    () -> ManejadorUsuarios.getInstance().addUsuario(usuario)));
            comprobarAusente(dt.getNickname(), antes);
            // Una operación válida posterior prueba que no queda una transacción fallida abierta.
            assertTrue(alta(dt, imagen()));
            comprobarPersistido(dt, imagen());
        }
    }

    @Test
    void falloRealDelUsuarioRevierteTambienSuImagenSinDejarHuerfanos() {
        for (boolean organizador : new boolean[]{false, true}) {
            DtUsuario dt = datos(organizador);
            dt.setNombre("x".repeat(256));
            Usuario usuario = entidad(dt);
            usuario.setImagen(new Imagen("image/png", imagen().getContenido()));
            long antes = cantidadImagenes();
            comprobarFalloSqlDeLongitud(assertThrows(PersistenceException.class,
                    () -> ManejadorUsuarios.getInstance().addUsuario(usuario)));
            comprobarAusente(dt.getNickname(), antes);
        }
    }
}
