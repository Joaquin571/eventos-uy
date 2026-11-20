package implementacion;

import clases.Imagen;
import clases.Usuario;
import datatypes.*;
import excepciones.AccesoDenegadoException;
import interfaces.ISistema;
import jakarta.persistence.LockModeType;
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
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

class ModificacionPerfilConImagenTest extends PruebaConBaseSegura {
    private final ISistema sistema = Fabrica.getInstance().getISistema();

    private DtImagen imagen(String formato) {
        return new DtImagen(formato.equals("png") ? "image/png" : "image/jpeg", ImagenesPrueba.imagen(formato));
    }

    private DtUsuario crear(boolean organizador) {
        String nick = "modImg" + UUID.randomUUID().toString().replace("-", "");
        DtUsuario dt = organizador
                ? new DtOrganizador(nick, "Original", nick + "@test.uy", "Descripción", "https://original.test")
                : new DtAsistente(nick, "Original", nick + "@test.uy", "Apellido", LocalDate.of(2000, 1, 1), null);
        clave(dt, "anterior");
        if (organizador) assertTrue(sistema.altaOrganizador((DtOrganizador) dt, imagen("png")));
        else assertTrue(sistema.altaAsistente((DtAsistente) dt, imagen("png")));
        return sistema.consultarUsuario(nick);
    }

    private void clave(DtUsuario dt, String clave) {
        dt.setContrasenia(clave); dt.setConfirmacionContrasenia(clave);
    }

    private void modificar(DtUsuario dt, DtImagen imagen, Actor actor) {
        if (dt instanceof DtAsistente asistente) sistema.modificarAsistente(asistente, imagen, actor);
        else sistema.modificarOrganizador((DtOrganizador) dt, imagen, actor);
    }

    private long cantidadImagenes() {
        try (var em = BaseDeDatos.getEntityManager()) {
            return em.createQuery("SELECT COUNT(i) FROM Imagen i", Long.class).getSingleResult();
        }
    }

    private record Estado(String nombre, String correo, LocalDate fechaAlta, String hash,
                          Long imagenId, String mime, byte[] bytes) { }

    private Estado estado(String nick) {
        try (var em = BaseDeDatos.getEntityManager()) {
            Usuario u = em.find(Usuario.class, nick);
            String hash = (String) em.createNativeQuery("SELECT contrasenia_hash FROM usuario WHERE nickname=:nick")
                    .setParameter("nick", nick).getSingleResult();
            Long id = (Long) em.createNativeQuery("SELECT imagen_id FROM usuario WHERE nickname=:nick", Long.class)
                    .setParameter("nick", nick).getSingleResult();
            return new Estado(u.getNombre(), u.getCorreoElectronico(), u.getFechaAlta(), hash, id,
                    u.getImagen().getTipoContenido(), u.getImagen().getContenido());
        }
    }

    private void sinCambios(Estado antes, String nick, long imagenes) {
        Estado actual = estado(nick);
        assertEquals(antes.nombre(), actual.nombre()); assertEquals(antes.correo(), actual.correo());
        assertEquals(antes.fechaAlta(), actual.fechaAlta()); assertEquals(antes.hash(), actual.hash());
        assertEquals(antes.imagenId(), actual.imagenId()); assertEquals(antes.mime(), actual.mime());
        assertArrayEquals(antes.bytes(), actual.bytes()); assertEquals(imagenes, cantidadImagenes());
        assertEquals(nick, sistema.autenticar(nick, "anterior").getNickname());
    }

    @Test void propietarioModificaDatosClaveEImagenSinCambiarIdentidad() {
        for (boolean org : new boolean[]{false, true}) {
            DtUsuario dt = crear(org); Actor actor = Actor.usuarioAutenticado(dt);
            Estado antes = estado(dt.getNickname()); long imagenes = cantidadImagenes();
            dt.setNombre("Nuevo"); dt.setCorreoElectronico("ignorar@test.uy"); clave(dt, "nueva");
            DtImagen nueva = imagen("jpg"); modificar(dt, nueva, actor);
            Estado actual = estado(dt.getNickname());
            assertEquals("Nuevo", actual.nombre()); assertEquals(antes.correo(), actual.correo());
            assertEquals(antes.fechaAlta(), actual.fechaAlta()); assertNotEquals(antes.hash(), actual.hash());
            assertNotEquals(antes.imagenId(), actual.imagenId()); assertArrayEquals(nueva.getContenido(), actual.bytes());
            assertEquals(imagenes, cantidadImagenes(), "El reemplazo elimina la imagen anterior.");
            assertEquals(dt.getNickname(), sistema.autenticar(dt.getNickname(), "nueva").getNickname());
            assertThrows(IllegalArgumentException.class, () -> sistema.autenticar(dt.getNickname(), "anterior"));
        }
    }

    @Test void administradorLocalPuedeModificarAmbosSubtipos() {
        for (boolean org : new boolean[]{false, true}) {
            DtUsuario dt = crear(org); dt.setNombre("Administrado");
            modificar(dt, imagen("jpg"), Actor.administradorLocal());
            assertEquals("Administrado", estado(dt.getNickname()).nombre());
            assertEquals(dt.getNickname(), sistema.autenticar(dt.getNickname(), "anterior").getNickname());
        }
    }

    @Test void imagenNulaYClaveAusenteConservanImagenYHashEnSobrecargasAutorizadas() {
        for (boolean org : new boolean[]{false, true}) {
            DtUsuario dt = crear(org); Actor actor = Actor.usuarioAutenticado(dt);
            Estado antes = estado(dt.getNickname()); long imagenes = cantidadImagenes();
            for (String clave : new String[]{null, ""}) {
                clave(dt, clave);
                if (dt instanceof DtAsistente a) sistema.modificarAsistente(a, actor);
                else sistema.modificarOrganizador((DtOrganizador) dt, actor);
                sinCambios(antes, dt.getNickname(), imagenes);
            }
        }
    }

    @Test void actorAusenteAjenoODeOtroRolNoModificaNada() {
        for (boolean org : new boolean[]{false, true}) {
            DtUsuario dt = crear(org); Estado antes = estado(dt.getNickname()); long imagenes = cantidadImagenes();
            Actor ajeno = Actor.usuarioAutenticado(crear(org));
            imagenes = cantidadImagenes();
            DtUsuario otroRol = org
                    ? new DtAsistente(dt.getNickname(), "Falso", "falso@test.uy", "Apellido", LocalDate.of(2000, 1, 1), null)
                    : new DtOrganizador(dt.getNickname(), "Falso", "falso@test.uy", "Desc", "");
            dt.setNombre("No guardar"); clave(dt, "nueva");
            for (Actor actor : new Actor[]{null, ajeno, Actor.usuarioAutenticado(otroRol)}) {
                assertThrows(AccesoDenegadoException.class, () -> modificar(dt, imagen("jpg"), actor));
                sinCambios(antes, dt.getNickname(), imagenes);
            }
        }
    }

    @Test void datosClaveOImagenInvalidosRechazanTodaLaOperacion() {
        for (boolean org : new boolean[]{false, true}) {
            DtUsuario original = crear(org); Estado antes = estado(original.getNickname()); long imagenes = cantidadImagenes();
            Actor actor = Actor.usuarioAutenticado(original);
            for (String caso : List.of("nombre", "confirmacion", "imagen")) {
                DtUsuario cambio = sistema.consultarUsuario(original.getNickname()); clave(cambio, "nueva");
                cambio.setNombre(caso.equals("nombre") ? " " : "No guardar");
                if (caso.equals("confirmacion")) cambio.setConfirmacionContrasenia("distinta");
                DtImagen nueva = caso.equals("imagen") ? new DtImagen("image/png", new byte[]{1, 2, 3}) : imagen("jpg");
                assertThrows(IllegalArgumentException.class, () -> modificar(cambio, nueva, actor));
                sinCambios(antes, original.getNickname(), imagenes);
            }
        }
    }

    private void persistirDirectamente(DtUsuario dt, Imagen imagen) {
        if (dt instanceof DtAsistente a) ManejadorUsuarios.getInstance().modificarAsistente(
                a.getNickname(), a.getNombre(), a.getApellido(), a.getFechaNacimiento(), null, a.getContrasenia(), imagen);
        else {
            DtOrganizador o = (DtOrganizador) dt;
            ManejadorUsuarios.getInstance().modificarOrganizador(o.getNickname(), o.getNombre(),
                    o.getDescripcion(), o.getSitioWeb(), o.getContrasenia(), imagen);
        }
    }

    private void sqlLongitud(Throwable error) {
        for (Throwable causa = error; causa != null; causa = causa.getCause()) {
            if (causa instanceof SQLException sql && "22001".equals(sql.getSQLState())) return;
        }
        fail("Se esperaba SQLState 22001 real.", error);
    }

    @Test void falloSqlDeImagenRevierteDatosClaveYConservaImagenOriginal() {
        for (boolean org : new boolean[]{false, true}) {
            DtUsuario dt = crear(org); Estado antes = estado(dt.getNickname()); long imagenes = cantidadImagenes();
            dt.setNombre("No guardar"); clave(dt, "nueva");
            // Se supera la validación preventiva para probar la transacción real del manejador.
            sqlLongitud(assertThrows(PersistenceException.class, () -> persistirDirectamente(dt,
                    new Imagen("x".repeat(256), imagen("jpg").getContenido()))));
            sinCambios(antes, dt.getNickname(), imagenes);
            modificar(dt, imagen("jpg"), Actor.administradorLocal());
            assertEquals("No guardar", estado(dt.getNickname()).nombre());
        }
    }

    @Test void falloSqlDeDatosRevierteNuevaImagenYClaveSinHuerfanos() {
        for (boolean org : new boolean[]{false, true}) {
            DtUsuario dt = crear(org); Estado antes = estado(dt.getNickname()); long imagenes = cantidadImagenes();
            dt.setNombre("x".repeat(256)); clave(dt, "nueva");
            sqlLongitud(assertThrows(PersistenceException.class, () -> persistirDirectamente(dt,
                    new Imagen("image/jpeg", imagen("jpg").getContenido()))));
            sinCambios(antes, dt.getNickname(), imagenes);
        }
    }

    @Test void noQuedanEntradasPublicasSinActorNiEnInterfazNiEnSistema() {
        for (Class<?> tipo : new Class<?>[]{ISistema.class, Sistema.class}) {
            assertThrows(NoSuchMethodException.class, () -> tipo.getMethod("modificarAsistente", DtAsistente.class));
            assertThrows(NoSuchMethodException.class, () -> tipo.getMethod("modificarOrganizador", DtOrganizador.class));
            assertThrows(NoSuchMethodException.class, () -> tipo.getMethod("modificarAsistente", DtAsistente.class, DtImagen.class));
            assertThrows(NoSuchMethodException.class, () -> tipo.getMethod("modificarOrganizador", DtOrganizador.class, DtImagen.class));
            assertThrows(NoSuchMethodException.class, () -> tipo.getMethod("cambiarContrasenia", String.class, String.class, String.class));
        }
    }

    @Test void cambioDeClaveHeredadoTambienExigeTitularOAdministrador() {
        for (boolean org : new boolean[]{false, true}) {
            DtUsuario dt = crear(org); Actor propio = Actor.usuarioAutenticado(dt);
            Actor ajeno = Actor.usuarioAutenticado(crear(org));
            Estado antes = estado(dt.getNickname()); long imagenes = cantidadImagenes();
            for (Actor actor : new Actor[]{null, ajeno}) {
                assertThrows(AccesoDenegadoException.class,
                        () -> sistema.cambiarContrasenia(dt.getNickname(), "nueva", "nueva", actor));
                sinCambios(antes, dt.getNickname(), imagenes);
            }
            sistema.cambiarContrasenia(dt.getNickname(), "propia", "propia", propio);
            assertEquals(dt.getNickname(), sistema.autenticar(dt.getNickname(), "propia").getNickname());
            sistema.cambiarContrasenia(dt.getNickname(), "administrada", "administrada", Actor.administradorLocal());
            assertEquals(dt.getNickname(), sistema.autenticar(dt.getNickname(), "administrada").getNickname());
            assertEquals(antes.imagenId(), estado(dt.getNickname()).imagenId());
            assertEquals(imagenes, cantidadImagenes());
        }
    }

    @Test void modificacionesConcurrentesEsperanElUsuarioYNoMezclanDatosClaveOImagen() throws Exception {
        for (boolean org : new boolean[]{false, true}) {
            DtUsuario original = crear(org); Actor actor = Actor.usuarioAutenticado(original);
            long imagenes = cantidadImagenes();
            DtUsuario uno = sistema.consultarUsuario(original.getNickname()); uno.setNombre("Uno"); clave(uno, "clave-uno");
            DtUsuario dos = sistema.consultarUsuario(original.getNickname()); dos.setNombre("Dos"); clave(dos, "clave-dos");
            var pool = Executors.newFixedThreadPool(2);
            try (var em = BaseDeDatos.getEntityManager()) {
                var tx = em.getTransaction(); tx.begin();
                try {
                    em.find(Usuario.class, original.getNickname(), LockModeType.PESSIMISTIC_WRITE);
                    Future<?> a = pool.submit(() -> modificar(uno, imagen("jpg"), actor));
                    Future<?> b = pool.submit(() -> modificar(dos, imagen("png"), actor));
                    assertThrows(TimeoutException.class, () -> a.get(250, TimeUnit.MILLISECONDS));
                    assertThrows(TimeoutException.class, () -> b.get(250, TimeUnit.MILLISECONDS));
                    tx.rollback();
                    a.get(15, TimeUnit.SECONDS); b.get(15, TimeUnit.SECONDS);
                } finally { if (tx.isActive()) tx.rollback(); }
            } finally { pool.shutdownNow(); }
            Estado actual = estado(original.getNickname());
            boolean ganoUno = actual.nombre().equals("Uno");
            assertTrue(ganoUno || actual.nombre().equals("Dos"));
            assertArrayEquals(imagen(ganoUno ? "jpg" : "png").getContenido(), actual.bytes());
            assertEquals(original.getNickname(), sistema.autenticar(original.getNickname(), ganoUno ? "clave-uno" : "clave-dos").getNickname());
            assertEquals(imagenes, cantidadImagenes());
        }
    }
}
