package usuarios;

import clases.*;
import datatypes.*;
import implementacion.Fabrica;
import interfaces.ISistema;
import jakarta.persistence.PersistenceException;
import manejadores.ManejadorInstituciones;
import manejadores.ManejadorUsuarios;
import org.junit.jupiter.api.Test;
import persistencia.BaseDeDatos;
import soporte.ImagenesPrueba;
import soporte.PruebaConBaseSegura;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

/** Incluye fallos de commit reales, además del rechazo preventivo de una imagen inválida. */
class ImagenesAtomicasUsuariosTest extends PruebaConBaseSegura {
    private final ISistema sistema = Fabrica.getInstance().getISistema();
    private final String sufijo = UUID.randomUUID().toString().replace("-", "");
    private DtImagen imagen() { return new DtImagen("image/png", ImagenesPrueba.imagen("png")); }
    private DtAsistente asistente() {
        String nick = "atomico" + sufijo;
        DtAsistente dt = new DtAsistente(nick, "Ana", nick + "@test.uy", "Original", LocalDate.of(2000, 1, 1), null);
        dt.setContrasenia("anterior"); dt.setConfirmacionContrasenia("anterior");
        return dt;
    }
    private long cantidadImagenes() {
        try (var em = BaseDeDatos.getEntityManager()) { return em.createQuery("SELECT COUNT(i) FROM Imagen i", Long.class).getSingleResult(); }
    }
    private void falloLongitud(Throwable error) {
        for (Throwable causa = error; causa != null; causa = causa.getCause()) {
            if (causa instanceof SQLException sql && "22001".equals(sql.getSQLState())) return;
        }
        fail("Se esperaba un fallo SQL 22001 real.", error);
    }
    @Test void altaConImagenYModificacionSinImagenLaConservan() {
        DtAsistente dt = asistente(); DtImagen original = imagen();
        assertTrue(sistema.altaAsistente(dt, original));
        dt.setNombre("Nuevo nombre"); dt.setContrasenia(null);
        sistema.modificarAsistente(dt, null);
        assertEquals("Nuevo nombre", sistema.consultarUsuario(dt.getNickname()).getNombre());
        assertArrayEquals(original.getContenido(), sistema.obtenerImagen(TipoEntidadImagen.USUARIO, dt.getNickname()).getContenido());
    }
    @Test void imagenInvalidaNoCreaUsuarioNiInstitucion() {
        DtAsistente dt = asistente(); DtImagen mala = new DtImagen("image/png", new byte[]{1, 2, 3});
        assertThrows(IllegalArgumentException.class, () -> sistema.altaAsistente(dt, mala));
        assertFalse(sistema.existeUsuario(dt.getNickname()));
        String nombre = "inst" + sufijo;
        assertThrows(IllegalArgumentException.class, () -> sistema.altaInstitucion(new DtInstitucion(nombre, "Desc", ""), mala));
        assertFalse(sistema.existeInstitucion(nombre));
    }
    @Test void falloSqlDeAltaRevierteUsuarioEImagen() {
        // Se llama al manejador para llegar al commit y verificar rollback, sin la validación preventiva.
        String nick = "fallo" + sufijo;
        Asistente usuario = new Asistente(nick, "Ana", nick + "@test.uy", "Apellido", LocalDate.of(2000, 1, 1), null);
        usuario.setContrasenia("clave");
        usuario.setImagen(new Imagen("x".repeat(256), imagen().getContenido()));
        long antes = cantidadImagenes();
        falloLongitud(assertThrows(PersistenceException.class, () -> ManejadorUsuarios.getInstance().addUsuario(usuario)));
        assertFalse(sistema.existeUsuario(nick));
        assertEquals(antes, cantidadImagenes());
    }
    @Test void falloSqlDeInstitucionNoDejaEntidadNiImagen() {
        Institucion institucion = new Institucion("inst" + sufijo, "Desc", "");
        institucion.setImagen(new Imagen("x".repeat(256), imagen().getContenido()));
        long antes = cantidadImagenes();
        falloLongitud(assertThrows(PersistenceException.class, () -> ManejadorInstituciones.getInstance().addInstitucion(institucion)));
        assertFalse(sistema.existeInstitucion(institucion.getNombre()));
        assertEquals(antes, cantidadImagenes());
    }
    @Test void falloSqlAlReemplazarImagenRevierteDatosClaveYConservaImagenAnterior() {
        DtAsistente dt = asistente(); DtImagen original = imagen();
        assertTrue(sistema.altaAsistente(dt, original));
        long antes = cantidadImagenes();
        DtImagen demasiadoLarga = new DtImagen("x".repeat(256), original.getContenido());
        falloLongitud(assertThrows(PersistenceException.class, () -> ManejadorUsuarios.getInstance()
                .modificarAsistente(dt.getNickname(), "No guardar", "No guardar", LocalDate.of(2002, 1, 1), null, "nueva", demasiadoLarga)));
        assertEquals("Ana", sistema.consultarUsuario(dt.getNickname()).getNombre());
        assertEquals(dt.getNickname(), sistema.autenticar(dt.getNickname(), "anterior").getNickname());
        assertThrows(IllegalArgumentException.class, () -> sistema.autenticar(dt.getNickname(), "nueva"));
        assertArrayEquals(original.getContenido(), sistema.obtenerImagen(TipoEntidadImagen.USUARIO, dt.getNickname()).getContenido());
        assertEquals(antes, cantidadImagenes());
    }
    @Test void organizadorGuardaImagenConDatosYConservaLaAnteriorCuandoEsNula() {
        String nick = "org" + sufijo;
        DtOrganizador org = new DtOrganizador(nick, "Org", nick + "@test.uy", "Desc", "");
        org.setContrasenia("clave"); org.setConfirmacionContrasenia("clave");
        DtImagen original = imagen();
        assertTrue(sistema.altaOrganizador(org, original));
        org.setContrasenia(null); org.setNombre("Nuevo");
        sistema.modificarOrganizador(org, null);
        assertEquals("Nuevo", sistema.consultarUsuario(nick).getNombre());
        assertArrayEquals(original.getContenido(), sistema.obtenerImagen(TipoEntidadImagen.USUARIO, nick).getContenido());
    }
    @Test void reemplazoValidoEliminaImagenAnteriorYGuardaNuevaClave() {
        DtAsistente dt = asistente();
        assertTrue(sistema.altaAsistente(dt, imagen()));
        long antes = cantidadImagenes();
        DtImagen nueva = new DtImagen("image/jpeg", ImagenesPrueba.imagen("jpg"));
        dt.setContrasenia("nueva"); dt.setConfirmacionContrasenia("nueva");
        sistema.modificarAsistente(dt, nueva);
        assertEquals(dt.getNickname(), sistema.autenticar(dt.getNickname(), "nueva").getNickname());
        assertArrayEquals(nueva.getContenido(), sistema.obtenerImagen(TipoEntidadImagen.USUARIO, dt.getNickname()).getContenido());
        assertEquals(antes, cantidadImagenes());
    }
    @Test void falloSqlDeImagenDelOrganizadorRevierteDescripcionYClave() {
        String nick = "orgFallido" + sufijo;
        DtOrganizador org = new DtOrganizador(nick, "Org", nick + "@test.uy", "Original", "");
        org.setContrasenia("anterior"); org.setConfirmacionContrasenia("anterior");
        DtImagen original = imagen();
        assertTrue(sistema.altaOrganizador(org, original));
        long antes = cantidadImagenes();
        falloLongitud(assertThrows(PersistenceException.class, () -> ManejadorUsuarios.getInstance()
                .modificarOrganizador(nick, "No guardar", "No guardar", "", "nueva",
                        new DtImagen("x".repeat(256), original.getContenido()))));
        assertEquals("Original", ((DtOrganizador) sistema.consultarUsuario(nick)).getDescripcion());
        assertEquals(nick, sistema.autenticar(nick, "anterior").getNickname());
        assertThrows(IllegalArgumentException.class, () -> sistema.autenticar(nick, "nueva"));
        assertArrayEquals(original.getContenido(), sistema.obtenerImagen(TipoEntidadImagen.USUARIO, nick).getContenido());
        assertEquals(antes, cantidadImagenes());
    }
}