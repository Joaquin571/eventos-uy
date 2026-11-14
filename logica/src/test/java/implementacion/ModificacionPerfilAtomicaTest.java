package implementacion;

import datatypes.*;
import interfaces.ISistema;
import jakarta.persistence.PersistenceException;
import org.junit.jupiter.api.Test;
import persistencia.BaseDeDatos;
import soporte.PruebaConBaseSegura;

import java.sql.SQLException;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/** H05: fallos reales de PostgreSQL y consultas posteriores con otro EntityManager. */
class ModificacionPerfilAtomicaTest extends PruebaConBaseSegura {
    private final ISistema sistema = Fabrica.getInstance().getISistema();
    private final String sufijo = Long.toString(System.nanoTime());

    private DtAsistente crearAsistente() {
        String nick = "asAtomico" + sufijo;
        DtAsistente dt = new DtAsistente(nick, "Ana", nick + "@test.uy", "Pérez",
                LocalDate.of(2000, 1, 1), null);
        dt.setContrasenia("anterior"); dt.setConfirmacionContrasenia("anterior");
        assertTrue(sistema.altaAsistente(dt));
        return (DtAsistente) sistema.consultarUsuario(nick);
    }

    private DtOrganizador crearOrganizador() {
        String nick = "orgAtomico" + sufijo;
        DtOrganizador dt = new DtOrganizador(nick, "Organización", nick + "@test.uy", "Original", "https://original.test");
        dt.setContrasenia("anterior"); dt.setConfirmacionContrasenia("anterior");
        assertTrue(sistema.altaOrganizador(dt));
        return (DtOrganizador) sistema.consultarUsuario(nick);
    }

    private void nuevaClave(DtUsuario dt) {
        dt.setContrasenia("nueva"); dt.setConfirmacionContrasenia("nueva");
    }

    private void conservaClaveAnterior(String nick) {
        assertEquals(nick, sistema.autenticar(nick, "anterior").getNickname());
        assertThrows(IllegalArgumentException.class, () -> sistema.autenticar(nick, "nueva"));
    }

    @Test
    void noModificaUnAsistenteComoOrganizadorNiCambiaSuClave() {
        DtAsistente original = crearAsistente();
        DtOrganizador falso = new DtOrganizador(original.getNickname(), "No guardar", "otro@test.uy", "No guardar", "");
        nuevaClave(falso);
        assertEquals("El usuario no es un organizador.", assertThrows(IllegalArgumentException.class,
                () -> sistema.modificarOrganizador(falso)).getMessage());
        DtAsistente actual = (DtAsistente) sistema.consultarUsuario(original.getNickname());
        assertEquals(original.getNombre(), actual.getNombre());
        assertEquals(original.getApellido(), actual.getApellido());
        assertEquals(original.getFechaAlta(), actual.getFechaAlta());
        conservaClaveAnterior(original.getNickname());
    }

    @Test
    void noModificaUnOrganizadorComoAsistenteNiCambiaSuClave() {
        DtOrganizador original = crearOrganizador();
        DtAsistente falso = new DtAsistente(original.getNickname(), "No guardar", "otro@test.uy", "Apellido",
                LocalDate.of(2001, 1, 1), null);
        nuevaClave(falso);
        assertEquals("El usuario no es un asistente.", assertThrows(IllegalArgumentException.class,
                () -> sistema.modificarAsistente(falso)).getMessage());
        DtOrganizador actual = (DtOrganizador) sistema.consultarUsuario(original.getNickname());
        assertEquals(original.getNombre(), actual.getNombre());
        assertEquals(original.getDescripcion(), actual.getDescripcion());
        assertEquals(original.getSitioWeb(), actual.getSitioWeb());
        conservaClaveAnterior(original.getNickname());
    }

    @Test
    void asistenteActualizaPerfilInstitucionYClaveConservandoIdentidadYFechaAlta() {
        DtAsistente original = crearAsistente();
        String institucion = "Institución atómica " + sufijo;
        assertTrue(sistema.altaInstitucion(new DtInstitucion(institucion, "Descripción", "")));
        DtAsistente cambio = new DtAsistente(original.getNickname(), "Nombre nuevo", "ignorar@test.uy", "Apellido nuevo",
                LocalDate.of(2002, 2, 2), institucion);
        nuevaClave(cambio); sistema.modificarAsistente(cambio);
        DtAsistente actual = (DtAsistente) sistema.consultarUsuario(original.getNickname());
        assertEquals(cambio.getNombre(), actual.getNombre());
        assertEquals(cambio.getApellido(), actual.getApellido());
        assertEquals(cambio.getFechaNacimiento(), actual.getFechaNacimiento());
        assertEquals(institucion, actual.getNombreInstitucion());
        assertEquals(original.getCorreoElectronico(), actual.getCorreoElectronico());
        assertEquals(original.getFechaAlta(), actual.getFechaAlta());
        assertEquals(actual.getNickname(), sistema.autenticar(actual.getNickname(), "nueva").getNickname());
        assertThrows(IllegalArgumentException.class, () -> sistema.autenticar(actual.getNickname(), "anterior"));
    }

    @Test
    void organizadorActualizaPerfilYClaveConservandoIdentidadYFechaAlta() {
        DtOrganizador original = crearOrganizador();
        DtOrganizador cambio = new DtOrganizador(original.getNickname(), "Nombre nuevo", "ignorar@test.uy",
                "Descripción nueva", "https://nuevo.test");
        nuevaClave(cambio); sistema.modificarOrganizador(cambio);
        DtOrganizador actual = (DtOrganizador) sistema.consultarUsuario(original.getNickname());
        assertEquals(cambio.getNombre(), actual.getNombre());
        assertEquals(cambio.getDescripcion(), actual.getDescripcion());
        assertEquals(cambio.getSitioWeb(), actual.getSitioWeb());
        assertEquals(original.getCorreoElectronico(), actual.getCorreoElectronico());
        assertEquals(original.getFechaAlta(), actual.getFechaAlta());
        assertEquals(actual.getNickname(), sistema.autenticar(actual.getNickname(), "nueva").getNickname());
        assertThrows(IllegalArgumentException.class, () -> sistema.autenticar(actual.getNickname(), "anterior"));
    }

    @Test
    void falloSqlDelAsistenteRevierteDatosYHashEnLaMismaTransaccion() {
        DtAsistente original = crearAsistente();
        DtAsistente cambio = new DtAsistente(original.getNickname(), "No guardar", original.getCorreoElectronico(),
                excederColumna("asistente", "apellido"), LocalDate.of(2003, 3, 3), null);
        nuevaClave(cambio);
        // S02 rechaza este dato en Sistema; aquí se fuerza el fallo SQL dentro del manejador para comprobar rollback real.
        comprobarFalloDeLongitud(assertThrows(PersistenceException.class, () -> manejadores.ManejadorUsuarios.getInstance()
                .modificarAsistente(cambio.getNickname(), cambio.getNombre(), cambio.getApellido(),
                        cambio.getFechaNacimiento(), null, cambio.getContrasenia())));
        DtAsistente actual = (DtAsistente) sistema.consultarUsuario(original.getNickname());
        assertEquals(original.getNombre(), actual.getNombre());
        assertEquals(original.getApellido(), actual.getApellido());
        assertEquals(original.getFechaNacimiento(), actual.getFechaNacimiento());
        assertEquals(original.getNombreInstitucion(), actual.getNombreInstitucion());
        assertEquals(original.getFechaAlta(), actual.getFechaAlta());
        conservaClaveAnterior(original.getNickname());
    }

    @Test
    void falloSqlDelOrganizadorRevierteDatosYHashEnLaMismaTransaccion() {
        DtOrganizador original = crearOrganizador();
        DtOrganizador cambio = new DtOrganizador(original.getNickname(), "No guardar", original.getCorreoElectronico(),
                excederColumna("organizador", "descripcion"), "https://no-guardar.test");
        nuevaClave(cambio);
        // Se omite deliberadamente la validación preventiva de Sistema para alcanzar el commit SQL.
        comprobarFalloDeLongitud(assertThrows(PersistenceException.class, () -> manejadores.ManejadorUsuarios.getInstance()
                .modificarOrganizador(cambio.getNickname(), cambio.getNombre(), cambio.getDescripcion(),
                        cambio.getSitioWeb(), cambio.getContrasenia())));
        DtOrganizador actual = (DtOrganizador) sistema.consultarUsuario(original.getNickname());
        assertEquals(original.getNombre(), actual.getNombre());
        assertEquals(original.getDescripcion(), actual.getDescripcion());
        assertEquals(original.getSitioWeb(), actual.getSitioWeb());
        assertEquals(original.getFechaAlta(), actual.getFechaAlta());
        conservaClaveAnterior(original.getNickname());
    }

    @Test
    void confirmacionInvalidaDelAsistenteNoActualizaNingunDato() {
        DtAsistente original = crearAsistente();
        DtAsistente cambio = new DtAsistente(original.getNickname(), "No guardar", original.getCorreoElectronico(),
                "Otro", LocalDate.of(2003, 3, 3), null);
        nuevaClave(cambio); cambio.setConfirmacionContrasenia("distinta");
        assertThrows(IllegalArgumentException.class, () -> sistema.modificarAsistente(cambio));
        DtAsistente actual = (DtAsistente) sistema.consultarUsuario(original.getNickname());
        assertEquals(original.getNombre(), actual.getNombre());
        assertEquals(original.getApellido(), actual.getApellido());
        assertEquals(original.getFechaNacimiento(), actual.getFechaNacimiento());
        conservaClaveAnterior(original.getNickname());
    }

    private String excederColumna(String tabla, String columna) {
        // Solo consulta metadatos: no crea triggers, restricciones ni modifica el esquema.
        try (var em = BaseDeDatos.getEntityManager()) {
            Number limite = (Number) em.createNativeQuery("""
                    SELECT character_maximum_length FROM information_schema.columns
                    WHERE table_schema = current_schema() AND table_name = :tabla AND column_name = :columna
                    """
            ).setParameter("tabla", tabla).setParameter("columna", columna).getSingleResult();
            assertNotNull(limite, "La prueba requiere la columna varchar del mapeo JPA.");
            assertTrue(limite.intValue() > 0 && limite.intValue() < 100_000);
            return "x".repeat(limite.intValue() + 1);
        }
    }

    private void comprobarFalloDeLongitud(Throwable error) {
        for (Throwable causa = error; causa != null; causa = causa.getCause()) {
            if (causa instanceof SQLException sql && "22001".equals(sql.getSQLState())) return;
        }
        fail("Se esperaba SQLState 22001 por longitud de columna, no otro fallo.", error);
    }
}
